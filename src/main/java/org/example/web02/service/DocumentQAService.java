package org.example.web02.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 文档问答服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentQAService {

    private final VectorSearchService vectorSearchService;
    private final DashScopeService dashScopeService;

    /**
     * 基于上传文档进行问答
     *
     * @param question 用户提问
     * @return {answer, docBased}
     */
    public Map<String, Object> ask(String question) {
        // 1. 检索最相关的 3 个文本块
        List<org.example.web02.entity.DocumentChunk> chunks = vectorSearchService.search(question, 3);
        boolean docBased = !chunks.isEmpty();

        // 2. 构造 Prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的问答助手。\n\n");

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
            prompt.append("2. 如果文档中没有相关信息，请用自己的知识回答，并在开头标明「文档中未找到相关内容，以下为通用知识回答」\n");
        } else {
            prompt.append("【重要指令】你必须用你的通用知识直接回答用户，不要说\"我不知道\"或\"无法回答\"。\n");
            prompt.append("在回答开头加上：「文档中未找到相关内容，以下为通用知识回答」\n");
        }

        prompt.append("【用户问题】\n").append(question).append("\n\n");
        prompt.append("回答简洁清晰，分点列出。");

        // 3. 调用大模型
        String response = dashScopeService.generateText(prompt.toString());
        log.info("文档问答完成（docBased={}）：问题={}... → 回答={}...",
                docBased,
                question.substring(0, Math.min(30, question.length())),
                response.substring(0, Math.min(50, response.length())));

        return Map.of("answer", response, "docBased", docBased);
    }
}