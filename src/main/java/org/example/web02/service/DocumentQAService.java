package org.example.web02.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.web02.entity.AiConversation;
import org.example.web02.mapper.AiConversationMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 文档问答服务（RAG问答核心组件）
 * 实现基于上传文档的问答功能：检索相关文档片段 → 构建Prompt → 调用大模型生成回答
 * 支持对话记忆：在Prompt中拼接最近3轮对话历史，提升回答连贯性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentQAService {

    /** 向量检索服务（用于查找相关文档片段） */
    private final VectorSearchService vectorSearchService;
    /** 大模型服务（用于生成最终回答） */
    private final DashScopeService dashScopeService;
    /** AI会话Mapper（用于获取对话历史） */
    private final AiConversationMapper aiConversationMapper;

    /** 对话历史保留轮数 */
    private static final int HISTORY_LIMIT = 6;  // 最近6条记录 ≈ 3轮对话

    /**
     * 基于上传文档进行问答（核心方法）
     * 流程：获取对话历史 → 向量检索相关文档 → 构建RAG Prompt（含历史） → 调用大模型生成回答
     *
     * @param question 用户提问
     * @param sessionId 会话ID（用于获取对话历史，可为空）
     * @return Map包含answer(回答内容)和docBased(是否基于文档)
     */
    public Map<String, Object> ask(String question, String sessionId) {
        // Step 0: 获取对话历史（第一层：生成阶段拼接历史）
        String conversationHistory = buildConversationHistory(sessionId);

        // Step 1: 向量检索最相关的3个文档片段
        List<org.example.web02.entity.DocumentChunk> chunks = vectorSearchService.search(question, 3);
        boolean docBased = !chunks.isEmpty();  // 是否基于文档回答

        // Step 2: 构建RAG Prompt（包含对话历史）
        StringBuilder prompt = buildPrompt(question, chunks, docBased, conversationHistory);

        // Step 3: 调用大模型生成回答
        String response = dashScopeService.generateText(prompt.toString());
        log.info("文档问答完成（docBased={}, 历史长度={}）：问题={}... → 回答={}...",
                docBased,
                conversationHistory.length(),
                question.substring(0, Math.min(30, question.length())),
                response.substring(0, Math.min(50, response.length())));

        return Map.of("answer", response, "docBased", docBased);
    }

    /**
     * 基于上传文档进行问答（无sessionId版本，用于兼容）
     *
     * @param question 用户提问
     * @return Map包含answer(回答内容)和docBased(是否基于文档)
     */
    public Map<String, Object> ask(String question) {
        return ask(question, null);
    }

    /**
     * 构建对话历史字符串
     * 从数据库获取最近 N 轮对话，拼接到 Prompt 中供大模型理解上下文
     *
     * @param sessionId 会话ID
     * @return 对话历史字符串，如果无历史则返回空字符串
     */
    private String buildConversationHistory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return "";
        }

        try {
            // 获取最近 N 条对话记录（按时间升序）
            List<AiConversation> history = aiConversationMapper.findRecentBySessionId(sessionId, HISTORY_LIMIT);
            if (history == null || history.isEmpty()) {
                return "";
            }

            // 拼接对话历史
            StringBuilder sb = new StringBuilder();
            for (AiConversation conv : history) {
                sb.append("用户：").append(conv.getQuestion()).append("\n");
                sb.append("助手：").append(conv.getAnswer() != null ? conv.getAnswer() : "").append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("获取对话历史失败: sessionId={}, error={}", sessionId, e.getMessage());
            return "";
        }
    }

    /**
     * 构建完整的Prompt
     * 结构：【系统角色】【对话历史】【参考文档】【当前问题】
     *
     * @param question 当前问题
     * @param chunks 检索到的文档片段
     * @param docBased 是否有相关文档
     * @param conversationHistory 对话历史字符串
     * @return 完整的Prompt
     */
    private StringBuilder buildPrompt(String question,
                                       List<org.example.web02.entity.DocumentChunk> chunks,
                                       boolean docBased,
                                       String conversationHistory) {
        StringBuilder prompt = new StringBuilder();

        // 【系统角色】
        prompt.append("你是一个专业的问答助手。\n\n");

        // 【对话历史】（如果存在）
        if (!conversationHistory.isEmpty()) {
            prompt.append("【对话历史】（请结合历史上下文理解当前问题）\n");
            prompt.append(conversationHistory);
            prompt.append("\n");
        }

        // 【参考文档】
        if (docBased) {
            prompt.append("【参考文档内容】\n");
            for (int i = 0; i < chunks.size(); i++) {
                org.example.web02.entity.DocumentChunk c = chunks.get(i);
                prompt.append("--- 文档片段 ").append(i + 1)
                        .append("（来源：").append(c.getDocName()).append("）---\n");
                prompt.append(c.getChunkText()).append("\n\n");
            }
            prompt.append("【要求】\n");
            prompt.append("1. 优先基于上述文档内容回答\n");
            prompt.append("2. 如果文档中没有相关信息，请结合对话历史用你自己的知识回答\n");
        } else {
            // 无相关文档：使用通用知识回答
            prompt.append("【重要指令】你必须用你的通用知识直接回答用户，不要说\"我不知道\"或\"无法回答\"。\n");
            prompt.append("在回答开头加上：「文档中未找到相关内容，以下为通用知识回答」\n");
        }

        // 【当前问题】
        prompt.append("【当前问题】\n").append(question).append("\n\n");
        prompt.append("请用自然、流畅的对话方式回答，像朋友聊天一样亲切，避免机械的列表格式。");

        return prompt;
    }
}