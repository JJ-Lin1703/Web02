package org.example.web02.controller;

import org.example.web02.entity.AiConversation;
import org.example.web02.mapper.AiConversationMapper;
import org.example.web02.service.VectorSearchService;
import org.example.web02.service.DocumentQAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 向量 RAG 控制器
 * 提供 TXT 文档上传 + 文档问答接口
 */
@Slf4j
@RestController
@RequestMapping("/api/doc-rag")
@RequiredArgsConstructor
public class DocRagController {

    private final VectorSearchService vectorSearchService;
    private final DocumentQAService documentQAService;
    private final AiConversationMapper aiConversationMapper;

    /** 文本最大长度（1MB，防止 OOM） */
    private static final int MAX_TEXT_LENGTH = 1_000_000;

    /**
     * 上传 TXT 文档（Base64 编码）
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestBody Map<String, String> body) {
        try {
            String fileName = body.get("fileName");
            String fileContent = body.get("fileContent");

            if (fileName == null || fileName.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件名不能为空"));
            }
            if (fileContent == null || fileContent.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "文件内容不能为空"));
            }
            if (!fileName.toLowerCase().endsWith(".txt")) {
                return ResponseEntity.badRequest().body(Map.of("error", "仅支持 TXT 文件"));
            }

            byte[] fileBytes = java.util.Base64.getDecoder().decode(fileContent);
            String text = new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);

            if (text == null || text.isBlank()) {
                throw new RuntimeException("文件内容为空，无法处理");
            }

            // 文本长度保护
            if (text.length() > MAX_TEXT_LENGTH) {
                text = text.substring(0, MAX_TEXT_LENGTH);
            }

            String docId = vectorSearchService.indexText(text, fileName);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "docId", docId,
                    "fileName", fileName,
                    "textLength", text.length(),
                    "message", "TXT 上传并解析成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Base64 解码失败：" + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "上传失败：" + e.getMessage()));
        }
    }

    /**
     * 文档问答（自动保存对话记录）
     */
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody Map<String, String> body,
                                 Authentication authentication) {
        String question = body.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "问题不能为空"));
        }

        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        String sessionId = body.getOrDefault("sessionId", "");

        try {
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
     */
    @GetMapping("/docs")
    public ResponseEntity<?> listDocs() {
        try {
            List<String> docs = vectorSearchService.listDocs();
            return ResponseEntity.ok(Map.of("docs", docs));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> listConversations(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<AiConversation> sessions = aiConversationMapper.findSessionsByUserId(userId);
            // 截断过长的首条问题
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
     */
    @GetMapping("/conversations/{sessionId}")
    public ResponseEntity<?> getConversation(@PathVariable String sessionId) {
        try {
            List<AiConversation> messages = aiConversationMapper.findBySessionId(sessionId);
            return ResponseEntity.ok(Map.of("messages", messages));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}