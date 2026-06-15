package org.example.web02.service;

import org.example.web02.entity.WarningLog;

import java.util.List;

/**
 * 警告日志服务接口
 * 提供警告日志的创建、查询、标记已读和自动检测功能
 */
public interface WarningLogService {

    /**
     * 创建警告记录
     * 
     * @param userId 用户ID
     * @param warningType 警告类型
     * @param content 警告内容
     */
    void createWarning(Long userId, String warningType, String content);

    /**
     * 获取用户所有警告记录
     * 
     * @param userId 用户ID
     * @return 警告记录列表（按时间倒序）
     */
    List<WarningLog> getWarnings(Long userId);

    /**
     * 获取用户未读警告记录
     * 
     * @param userId 用户ID
     * @return 未读警告记录列表
     */
    List<WarningLog> getUnreadWarnings(Long userId);

    /**
     * 统计用户未读警告数量
     * 
     * @param userId 用户ID
     * @return 未读警告数量
     */
    long countUnreadWarnings(Long userId);

    /**
     * 标记所有警告为已读
     * 
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 标记单个警告为已读
     * 
     * @param id 警告记录ID
     */
    void markAsRead(Long id);

    /**
     * 删除警告记录
     * 
     * @param id 警告记录ID
     */
    void deleteWarning(Long id);

    /**
     * 检查体重波动异常
     * 
     * @param userId 用户ID
     * @param newWeight 新体重值（单位：kg）
     */
    void checkWeightFluctuation(Long userId, double newWeight);

    /**
     * 检查BMI异常
     * 
     * @param userId 用户ID
     * @param bmi BMI值
     */
    void checkBmiAbnormal(Long userId, double bmi);

    /**
     * 检查打卡缺失（定时任务）
     */
    void checkClockMiss();

    /**
     * 检查签到提醒（定时任务）
     * 
     * @param userId 用户ID
     */
    void checkCheckinRemind(Long userId);

    /**
     * 检查体重记录提醒（定时任务）
     * 
     * @param userId 用户ID
     */
    void checkWeightRecordRemind(Long userId);
}