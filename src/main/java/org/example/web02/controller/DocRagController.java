package org.example.web02.controller;

import org.example.web02.entity.AiConversation;
import org.example.web02.entity.UploadTask;
import org.example.web02.mapper.AiConversationMapper;
import org.example.web02.mapper.UploadTaskMapper;
import org.example.web02.service.VectorSearchService;
import org.example.web02.service.DocumentQAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 向量 RAG 控制器
 *
 * 提供 TXT 文档上传（异步 + DB 状态追踪 + 失败重试）和文档问答功能
 * 支持文档向量化入库、基于文档的问答、会话管理等
 */
@Slf4j
@RestController
@RequestMapping("/api/doc-rag")
@RequiredArgsConstructor
public class DocRagController {

    /** 向量搜索服务，用于文档向量化入库和检索 */
    private final VectorSearchService vectorSearchService;

    /** 文档问答服务，用于基于文档的问答 */
    private final DocumentQAService documentQAService;

    /** AI 会话 Mapper，用于对话记录的数据库操作 */
    private final AiConversationMapper aiConversationMapper;

    /** 上传任务 Mapper，用于上传任务状态的数据库操作 */
    private final UploadTaskMapper uploadTaskMapper;

    /** 异步任务线程池（IO 密集型，线程数不宜过多） */
    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(2);

    /**
     * 上传 TXT 文档（异步处理，立即返回任务ID）
     *
     * @param file 上传的 TXT 文件
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含任务 ID 和上传信息的响应
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        Authentication authentication) {
        try {
            // 获取文件名
            String fileName = file.getOriginalFilename();

            // 校验文件名
            if (fileName == null || fileName.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件名不能为空"));
            }
            // 校验文件内容
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件内容不能为空"));
            }
            // 校验文件类型（仅支持 TXT）
            if (!fileName.toLowerCase().endsWith(".txt")) {
                return ResponseEntity.badRequest().body(Map.of("error", "仅支持 TXT 文件"));
            }

            // 读取文件内容（UTF-8 编码）
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (text.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件内容为空，无法处理"));
            }

            // 获取用户 ID（可选认证）
            Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
            // 生成任务 ID
            String taskId = "upload_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

            // 创建 DB 任务记录（初始状态 PENDING）
            UploadTask task = UploadTask.builder()
                    .taskId(taskId)
                    .fileName(fileName)
                    .textLength(text.length())
                    .status("PENDING")
                    .retryCount(0)
                    .maxRetries(3)
                    .userId(userId)
                    .build();
            uploadTaskMapper.insert(task);

            // 异步执行向量化入库
            CompletableFuture.runAsync(() -> {
                processUploadTask(taskId, text, fileName);
            }, taskExecutor);

            log.info("上传任务已提交：taskId={}, fileName={}, size={}", taskId, fileName, text.length());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "taskId", taskId,
                    "fileName", fileName,
                    "textLength", text.length(),
                    "message", "文件已提交，正在解析中..."
            ));
        } catch (Exception e) {
            log.error("上传请求异常", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "上传失败：" + e.getMessage()));
        }
    }

    /**
     * 异步执行上传任务（含失败重试）
     *
     * @param taskId 任务 ID
     * @param text 文档文本内容
     * @param fileName 文件名
     */
    private void processUploadTask(String taskId, String text, String fileName) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 更新状态为 PROCESSING
                UploadTask update = UploadTask.builder()
                        .taskId(taskId)
                        .status("PROCESSING")
                        .build();
                uploadTaskMapper.updateStatus(update);

                // 执行向量化入库
                String docId = vectorSearchService.indexText(text, fileName);

                // 更新为 DONE
                update = UploadTask.builder()
                        .taskId(taskId)
                        .status("DONE")
                        .docId(docId)
                        .build();
                uploadTaskMapper.updateStatus(update);

                log.info("上传任务完成：taskId={}, docId={}", taskId, docId);
                return; // 成功，退出
            } catch (Exception e) {
                log.warn("上传任务第 {} 次失败：taskId={}, error={}",
                        attempt, taskId, e.getMessage());

                if (attempt < maxRetries) {
                    // 指数退避：2s, 4s
                    try {
                        Thread.sleep(2000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // 最后一次失败，标记 FAILED
                    UploadTask update = UploadTask.builder()
                            .taskId(taskId)
                            .status("FAILED")
                            .errorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 1000)) : "未知错误")
                            .retryCount(attempt)
                            .build();
                    uploadTaskMapper.updateStatus(update);
                    log.error("上传任务最终失败：taskId={}", taskId, e);
                }
            }
        }
    }

    /**
     * 查询上传任务状态（从 DB 读取）
     *
     * @param taskId 任务 ID
     * @return 包含任务状态的响应
     */
    @GetMapping("/upload/status/{taskId}")
    public ResponseEntity<?> getUploadStatus(@PathVariable String taskId) {
        // 从数据库查询任务状态
        UploadTask task = uploadTaskMapper.findByTaskId(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "status", task.getStatus(),
                "fileName", task.getFileName(),
                "textLength", task.getTextLength(),
                "docId", task.getDocId() != null ? task.getDocId() : "",
                "error", task.getErrorMsg() != null ? task.getErrorMsg() : ""
        ));
    }

    /**
     * 文档问答（自动保存对话记录）
     *
     * @param body 请求体，包含问题和会话 ID
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含问答结果的响应
     */
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody Map<String, String> body,
                                 Authentication authentication) {
        // 获取问题
        String question = body.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "问题不能为空"));
        }

        // 获取用户 ID 和会话 ID
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        String sessionId = body.getOrDefault("sessionId", "");

        try {
            // 调用文档问答服务
            Map<String, Object> result = documentQAService.ask(question);

            // 保存对话记录
            if (userId != null && !sessionId.isBlank()) {
                try {
                    AiConversation conv = AiConversation.builder()
                            .userId(userId)
                            .sessionId(sessionId)
                            .question(question)
                            .answer((String) result.get("answer"))
                            .docBased((Boolean) result.get("docBased") ? 1 : 0)
                            .build();
                    aiConversationMapper.insert(conv);
                    log.debug("对话记录已保存: userId={}, sessionId={}", userId, sessionId);
                } catch (Exception e) {
                    log.error("保存对话记录失败", e);
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "问答失败：" + e.getMessage()));
        }
    }

    /**
     * 获取已上传文档列表
     *
     * @return 包含文档列表的响应
     */
    @GetMapping("/docs")
    public ResponseEntity<?> listDocs() {
        try {
            // 调用向量搜索服务获取文档列表
            List<String> docs = vectorSearchService.listDocs();
            return ResponseEntity.ok(Map.of("docs", docs));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取当前用户的会话列表
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含会话列表的响应
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> listConversations(Authentication authentication) {
        try {
            // 从认证信息中获取用户 ID
            Long userId = (Long) authentication.getPrincipal();
            // 查询用户的会话列表
            List<AiConversation> sessions = aiConversationMapper.findSessionsByUserId(userId);
            // 截断问题文本（超过 30 字符）
            sessions.forEach(s -> {
                if (s.getQuestion() != null && s.getQuestion().length() > 30) {
                    s.setQuestion(s.getQuestion().substring(0, 30) + "...");
                }
            });
            return ResponseEntity.ok(Map.of("sessions", sessions));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取指定会话的消息列表
     *
     * @param sessionId 会话 ID
     * @return 包含消息列表的响应
     */
    @GetMapping("/conversations/{sessionId}")
    public ResponseEntity<?> getConversation(@PathVariable String sessionId) {
        try {
            // 查询会话的消息列表
            List<AiConversation> messages = aiConversationMapper.findBySessionId(sessionId);
            return ResponseEntity.ok(Map.of("messages", messages));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}