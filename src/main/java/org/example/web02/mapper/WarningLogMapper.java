package org.example.web02.mapper;

import org.example.web02.entity.WarningLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预警日志数据访问接口
 *
 * 提供预警日志表的 CRUD 操作，包括创建预警、查询预警、
 * 更新已读状态、删除预警等功能
 */
@Mapper
public interface WarningLogMapper {

    /**
     * 插入预警日志
     *
     * @param warningLog 预警日志实体
     */
    void insert(WarningLog warningLog);

    /**
     * 根据用户 ID 查询所有预警日志
     *
     * @param userId 用户 ID
     * @return 预警日志列表
     */
    List<WarningLog> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 查询未读预警日志
     *
     * @param userId 用户 ID
     * @return 未读预警日志列表
     */
    List<WarningLog> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 统计未读预警数量
     *
     * @param userId 用户 ID
     * @return 未读预警数量
     */
    long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 更新用户所有预警的已读状态
     *
     * @param userId 用户 ID
     * @param isRead 已读状态（0-未读，1-已读）
     */
    void updateReadStatus(@Param("userId") Long userId, @Param("isRead") Integer isRead);

    /**
     * 更新单个预警的已读状态
     *
     * @param id 预警日志 ID
     * @param isRead 已读状态（0-未读，1-已读）
     */
    void updateReadStatusById(@Param("id") Long id, @Param("isRead") Integer isRead);

    /**
     * 删除预警日志（软删除）
     *
     * @param id 预警日志 ID
     */
    void deleteById(@Param("id") Long id);

    /**
     * 根据预警日志 ID 查询预警
     *
     * @param id 预警日志 ID
     * @return 预警日志实体
     */
    WarningLog findById(@Param("id") Long id);

    /**
     * 根据用户 ID 和预警类型查询预警日志
     *
     * @param userId 用户 ID
     * @param warningType 预警类型
     * @return 预警日志列表
     */
    List<WarningLog> findByUserIdAndType(@Param("userId") Long userId, @Param("warningType") String warningType);

    /**
     * 统计今日某类型预警数量
     *
     * @param userId 用户 ID
     * @param warningType 预警类型
     * @return 今日预警数量
     */
    int countTodayWarning(@Param("userId") Long userId, @Param("warningType") String warningType);
}