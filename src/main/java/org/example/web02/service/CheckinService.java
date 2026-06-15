package org.example.web02.service;

import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.DailyCheckin;

import java.util.List;

/**
 * 签到服务接口
 * 提供用户每日签到、签到统计和历史查询功能
 */
public interface CheckinService {

    /**
     * 用户签到
     * 
     * @param userId 用户ID
     * @return 签到成功返回true
     */
    boolean checkin(Long userId);

    /**
     * 检查用户今日是否已签到
     * 
     * @param userId 用户ID
     * @return true表示已签到，false表示未签到
     */
    boolean isCheckedInToday(Long userId);

    /**
     * 获取用户累计签到天数
     * 
     * @param userId 用户ID
     * @return 累计签到天数
     */
    int getTotalCheckinDays(Long userId);

    /**
     * 获取用户当前连续签到天数
     * 
     * @param userId 用户ID
     * @return 连续签到天数
     */
    int getContinuousDays(Long userId);

    /**
     * 获取用户本周签到天数
     * 
     * @param userId 用户ID
     * @return 本周签到天数
     */
    int getWeekCheckinDays(Long userId);

    /**
     * 获取用户签到历史记录
     * 
     * @param userId 用户ID
     * @return 签到记录列表（按时间倒序）
     */
    List<DailyCheckin> getCheckinHistory(Long userId);

    /**
     * 分页获取用户签到历史记录
     * 
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<DailyCheckin> getCheckinHistoryPaginated(Long userId, int pageNum, int pageSize);
}
