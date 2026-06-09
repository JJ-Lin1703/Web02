package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

/**
 * RAG健康知识库实体
 * tags 标签命名规范（必须与 user_health 字段可选值完全一致）：
 *   健康目标：减肥, 增肌, 维持健康
 *   饮食偏好：素食, 均衡, 高蛋白, 低碳水, 控糖
 *   过敏：海鲜, 牛奶, 坚果, 芒果, 鸡蛋
 *   慢性病：高血压, 糖尿病, 脂肪肝, 痛风, 冠心病
 *   活动水平：低活动量, 中活动量, 高活动量
 */
@Data
public class KnowledgeBase {

    private Long id;
    private String title;
    private String content;
    /** 分类标签，逗号分隔，如 "减肥,素食,高蛋白" */
    private String tags;
    private Date createTime;
    private Date updateTime;
    private Integer isDeleted;
}