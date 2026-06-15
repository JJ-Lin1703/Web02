package org.example.web02.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块工具类
 * 将长文本按指定大小切块，块间有重叠
 */
@Slf4j
public class TextChunkUtil {

    /** 每块最大字符数（1200字符/块，块数减少约60%） */
    private static final int CHUNK_SIZE = 1200;

    /** 块间重叠字符数 */
    private static final int OVERLAP = 120;

    /**
     * 将文本切分为多个块
     */
    public static List<String> split(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        log.info("开始文本分块，原始文本长度：{} 字符", text.length());

        // 只去除首尾空白，不做全局 replaceAll（避免创建大字符串副本）
        text = text.trim();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());

            // 尽量在句号、换行等自然分隔处切断
            if (end < text.length()) {
                int naturalBreak = findNaturalBreak(text, start, end);
                if (naturalBreak > start + CHUNK_SIZE / 2) {
                    end = naturalBreak + 1;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // 已处理到文本末尾，退出
            if (end >= text.length()) {
                break;
            }

            // 下一块的起始位置 = 当前块末尾 - 重叠量（至少前进 1）
            start = Math.max(end - OVERLAP, start + 1);
        }

        log.info("文本分块完成：{} 字符 → {} 个块", text.length(), chunks.size());
        return chunks;
    }

    /**
     * 在指定范围内寻找最佳自然切断位置
     */
    private static int findNaturalBreak(String text, int start, int end) {
        // 从 end 向前查找自然分隔符
        for (int i = end; i > start; i--) {
            char c = text.charAt(i);
            if (c == '。' || c == '！' || c == '？' || c == '\n' || c == '；') {
                return i;
            }
        }
        return end;
    }
}