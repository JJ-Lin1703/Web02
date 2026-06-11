package org.example.web02.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConversation {
    private Long id;
    private Long userId;
    private String sessionId;
    private String question;
    private String answer;
    private Integer docBased;
    private LocalDateTime createTime;
    private Integer isDeleted;
}