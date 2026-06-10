package org.example.web02.util;

import java.util.List;

/**
 * 余弦相似度计算工具类（纯 Java，零依赖）
 */
public class CosineSimilarityUtil {

    /**
     * 计算两个向量的余弦相似度
     *
     * @param a 向量A
     * @param b 向量B
     * @return 相似度 [0, 1]，越大越相似
     */
    public static double cosine(List<Float> a, List<Float> b) {
        if (a.size() != b.size()) {
            throw new IllegalArgumentException("向量维度不一致: " + a.size() + " vs " + b.size());
        }

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.size(); i++) {
            double va = a.get(i);
            double vb = b.get(i);
            dotProduct += va * vb;
            normA += va * va;
            normB += vb * vb;
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}