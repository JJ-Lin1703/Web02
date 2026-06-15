package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

/**
 * 标签字典选项实体类
 *
 * 对应数据库表 dict_label_option，存储标签字典选项，
 * 用于健康档案下拉框和 RAG 检索的标签池
 */
@Data
public class DictLabelOption {

    /** 标签选项 ID（主键） */
    private Long id;

    /** 标签分类：health_target/diet_hobby/allergy/medical_history/activity_level */
    private String type;

    /** 标签名称（前端下拉展示和 RAG 检索用） */
    private String labelName;

    /** 排序值（用于前端下拉框的显示顺序） */
    private Integer sort;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 是否删除（0-未删除，1-已删除，软删除标记） */
    private Integer isDeleted;
}