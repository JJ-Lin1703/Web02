package org.example.web02.service;

import org.example.web02.entity.ClockRecord;

import java.util.List;
import java.util.Map;

/**
 * 打卡记录服务接口
 * 提供健康计划打卡记录的保存、查询和统计功能
 */
public interface ClockRecordService {

    /**
     * 保存或更新打卡记录
     * 
     * @param userId 用户ID
     * @param planId 计划ID
     * @param finishItems 已完成项目列表
     * @param totalItems 全部项目列表
     * @param unfinishReasons 未完成原因映射
     * @return 保存后的打卡记录
     */
    ClockRecord saveOrUpdateClockRecord(Long userId, Long planId, List<Map<String, Object>> finishItems, 
                                        List<Map<String, Object>> totalItems, Map<String, String> unfinishReasons);

    /**
     * 获取用户今日的打卡记录
     * 
     * @param userId 用户ID
     * @param planId 计划ID
     * @return 今日打卡记录，如果不存在则返回null
     */
    ClockRecord getTodayClockRecord(Long userId, Long planId);

    /**
     * 获取用户本周的打卡记录
     * 
     * @param userId 用户ID
     * @return 本周打卡记录列表
     */
    List<ClockRecord> getWeekClockRecords(Long userId);

    /**
     * 根据日期范围获取用户的打卡记录
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 打卡记录列表
     */
    List<ClockRecord> getClockRecordsByDateRange(Long userId, String startDate, String endDate);

    /**
     * 根据ID获取打卡记录
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     * @return 打卡记录，如果不存在则返回null
     */
    ClockRecord getClockRecordById(Long userId, Long recordId);

    /**
     * 删除打卡记录
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     */
    void deleteClockRecord(Long userId, Long recordId);

    /**
     * 获取用户本周打卡统计数据
     * 
     * @param userId 用户ID
     * @return 统计数据Map，包含完成天数、完成率等信息
     */
    Map<String, Object> getWeeklyStats(Long userId);
}