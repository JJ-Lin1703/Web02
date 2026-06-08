package org.example.web02.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private Integer role;
    private Date lockUntil;
    private Integer loginFailCount;
    private Date createTime;
    private Date updateTime;
    private Integer isDeleted;

    public boolean isAdmin() {
        return role != null && role == 1;
    }

    public boolean isLocked() {
        return lockUntil != null && lockUntil.after(new Date());
    }
}