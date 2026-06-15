package org.example.web02.service.impl;

import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.DailyCheckin;
import org.example.web02.entity.User;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.DailyCheckinMapper;
import org.example.web02.mapper.UserMapper;
import org.example.web02.service.CheckinService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 签到服务实现类
 * 负责用户每日签到、连续签到统计、签到历史查询等功能
 */
@Service
public class CheckinServiceImpl implements CheckinService {

    /** 每日签到数据访问层 */
    private final DailyCheckinMapper dailyCheckinMapper;
    /** 用户数据访问层 */
    private final UserMapper userMapper;

    /**
     * 构造函数注入依赖
     * @param dailyCheckinMapper 每日签到Mapper
     * @param userMapper 用户Mapper
     */
    public CheckinServiceImpl(DailyCheckinMapper dailyCheckinMapper, UserMapper userMapper) {
        this.dailyCheckinMapper = dailyCheckinMapper;
        this.userMapper = userMapper;
    }

    /**
     * 用户签到
     * 流程：检查今日是否已签到 → 计算连续签到天数 → 创建签到记录 → 更新用户锻炼天数
     * 
     * @param userId 用户ID
     * @return 签到成功返回true
     * @throws BusinessException 今日已签到时抛出
     */
    @Override
    @Transactional
    public boolean checkin(Long userId) {
        Date today = Date.valueOf(LocalDate.now());
        
        if (isCheckedInToday(userId)) {
            throw new BusinessException("今日已签到");
        }

        int continuousDays = calculateContinuousDays(userId);
        
        DailyCheckin checkin = new DailyCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setCheckinTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        checkin.setContinuousDays(continuousDays);
        checkin.setCreateTime(new java.util.Date());

        dailyCheckinMapper.insert(checkin);

        User user = userMapper.findById(userId);
        if (user != null) {
            int newExerciseDays = (user.getExerciseDays() != null ? user.getExerciseDays() : 0) + 1;
            user.setExerciseDays(newExerciseDays);
            user.setUpdateTime(new java.util.Date());
            userMapper.update(user);
        }

        return true;
    }

    /**
     * 检查用户今日是否已签到
     * 
     * @param userId 用户ID
     * @return true表示已签到，false表示未签到
     */
    @Override
    public boolean isCheckedInToday(Long userId) {
        Date today = Date.valueOf(LocalDate.now());
        DailyCheckin checkin = dailyCheckinMapper.findByUserIdAndDate(userId, today);
        return checkin != null;
    }

    /**
     * 获取用户累计签到天数
     * 
     * @param userId 用户ID
     * @return 累计签到天数
     */
    @Override
    public int getTotalCheckinDays(Long userId) {
        return (int) dailyCheckinMapper.countByUserId(userId);
    }

    /**
     * 获取用户当前连续签到天数
     * 如果今日未签到，返回0
     * 
     * @param userId 用户ID
     * @return 连续签到天数
     */
    @Override
    public int getContinuousDays(Long userId) {
        if (!isCheckedInToday(userId)) {
            return 0;
        }
        return calculateContinuousDays(userId);
    }

    /**
     * 获取用户本周签到天数
     * 统计从周一到周日的签到次数
     * 
     * @param userId 用户ID
     * @return 本周签到天数
     */
    @Override
    public int getWeekCheckinDays(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        
        Date startDate = Date.valueOf(weekStart);
        Date endDate = Date.valueOf(weekEnd);
        
        return dailyCheckinMapper.countByUserIdAndDateRange(userId, startDate, endDate);
    }

    /**
     * 计算连续签到天数
     * 从今日向前回溯，统计连续签到的天数（最多365天）
     * 
     * @param userId 用户ID
     * @return 连续签到天数（包含今日）
     */
    private int calculateContinuousDays(Long userId) {
        LocalDate today = LocalDate.now();
        int continuousDays = 1;
        
        for (int i = 1; i <= 365; i++) {
            LocalDate previousDate = today.minusDays(i);
            Date date = Date.valueOf(previousDate);
            DailyCheckin checkin = dailyCheckinMapper.findByUserIdAndDate(userId, date);
            
            if (checkin != null) {
                continuousDays++;
            } else {
                break;
            }
        }
        
        return continuousDays;
    }

    /**
     * 获取用户签到历史记录
     * 
     * @param userId 用户ID
     * @return 签到记录列表（按时间倒序）
     */
    @Override
    public List<DailyCheckin> getCheckinHistory(Long userId) {
        return dailyCheckinMapper.findByUserId(userId);
    }

    /**
     * 分页获取用户签到历史记录
     * 
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<DailyCheckin> getCheckinHistoryPaginated(Long userId, int pageNum, int pageSize) {
        long offset = (long) (pageNum - 1) * pageSize;
        List<DailyCheckin> records = dailyCheckinMapper.findByUserIdPaginated(userId, offset, pageSize);
        long total = dailyCheckinMapper.countByUserId(userId);
        return PageResult.of(records, total, pageNum, pageSize);
    }
}
