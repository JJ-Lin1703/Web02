package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DictLabelOption {
    private Long id;
    /** 标签分类：health_target/diet_hobby/allergy/medical_history/activity_level */
    private String type;
    /** 标签名称（前端下拉展示+RAG检索用） */
    private String labelName;
    /** 排序值 */
    private Integer sort;
    private Date createTime;
    private Date updateTime;
    private Integer isDeleted;
}