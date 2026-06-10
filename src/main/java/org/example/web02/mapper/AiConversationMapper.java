package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.AiConversation;

import java.util.List;

@Mapper
public interface AiConversationMapper {

    int insert(AiConversation conversation);

    List<AiConversation> findByUserId(@Param("userId") Long userId);

    List<AiConversation> findBySessionId(@Param("sessionId") String sessionId);

    List<AiConversation> findSessionsByUserId(@Param("userId") Long userId);
}