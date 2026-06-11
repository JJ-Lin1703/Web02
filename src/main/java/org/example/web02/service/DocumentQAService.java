package org.example.web02.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 文档问答服务（RAG问答核心组件）
 * 实现基于上传文档的问答功能：检索相关文档片段 → 构建Prompt → 调用大模型生成回答
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentQAService {

    /** 向量检索服务（用于查找相关文档片段） */
    private final VectorSearchService vectorSearchService;
    /** 大模型服务（用于生成最终回答） */
    private final DashScopeService dashScopeService;

    /**
     * 基于上传文档进行问答（核心方法）
     * 流程：向量检索相关文档 → 构建RAG Prompt → 调用大模型生成回答
     *
     * @param question 用户提问
     * @return Map包含answer(回答内容)和docBased(是否基于文档)
     */
    public Map<String, Object> ask(String question) {
        // Step 1: 向量检索最相关的3个文档片段
        List<org.example.web02.entity.DocumentChunk> chunks = vectorSearchService.search(question, 3);
        boolean docBased = !chunks.isEmpty();  // 是否基于文档回答

        // Step 2: 构建RAG Prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的问答助手。\n\n");

        if (docBased) {
            // 有相关文档：将文档片段作为参考信息
            prompt.append("【参考文档内容】\n");
            for (int i = 0; i < chunks.size(); i++) {
                org.example.web02.entity.DocumentChunk c = chunks.get(i);
                prompt.append("--- 文档片段 ").append(i + 1)
                        .append("（来源：").append(c.getDocName()).append("）---\n");
                prompt.append(c.getChunkText()).append("\n\n");
            }
            prompt.append("【要求】\n");
            prompt.append("1. 优先基于上述文档内容回答\n");
            prompt.append("2. 如果文档中没有相关信息，请用自己的知识回答，并在开头标明「文档中未找到相关内容，以下为通用知识回答」\n");
        } else {
            // 无相关文档：使用通用知识回答
            prompt.append("【重要指令】你必须用你的通用知识直接回答用户，不要说\"我不知道\"或\"无法回答\"。\n");
            prompt.append("在回答开头加上：「文档中未找到相关内容，以下为通用知识回答」\n");
        }

        // 添加用户问题
        prompt.append("【用户问题】\n").append(question).append("\n\n");
        prompt.append("回答简洁清晰，分点列出。");

        // Step 3: 调用大模型生成回答
        String response = dashScopeService.generateText(prompt.toString());
        log.info("文档问答完成（docBased={}）：问题={}... → 回答={}...",
                docBased,
                question.substring(0, Math.min(30, question.length())),
                response.substring(0, Math.min(50, response.length())));

        return Map.of("answer", response, "docBased", docBased);
    }
}