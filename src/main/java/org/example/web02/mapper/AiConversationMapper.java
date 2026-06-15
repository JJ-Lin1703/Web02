package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.AiConversation;

import java.util.List;

/**
 * AI 会话数据访问接口
 *
 * 提供 AI 会话表的 CRUD 操作，包括保存对话、查询对话历史、
 * 查询会话列表等功能
 */
@Mapper
public interface AiConversationMapper {

    /**
     * 插入会话记录
     *
     * @param conversation 会话记录实体
     * @return 影响行数
     */
    int insert(AiConversation conversation);

    /**
     * 根据用户 ID 查询所有会话记录
     *
     * @param userId 用户 ID
     * @return 会话记录列表
     */
    List<AiConversation> findByUserId(@Param("userId") Long userId);

    /**
     * 根据会话 ID 查询会话记录（同一会话的多轮对话）
     *
     * @param sessionId 会话 ID
     * @return 会话记录列表
     */
    List<AiConversation> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据用户 ID 查询会话列表（每个会话只返回最新一条）
     *
     * @param userId 用户 ID
     * @return 会话记录列表
     */
    List<AiConversation> findSessionsByUserId(@Param("userId") Long userId);
}