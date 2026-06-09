package org.example.web02.service.impl;

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

@Service
public class CheckinServiceImpl implements CheckinService {

    private final DailyCheckinMapper dailyCheckinMapper;
    private final UserMapper userMapper;

    public CheckinServiceImpl(DailyCheckinMapper dailyCheckinMapper, UserMapper userMapper) {
        this.dailyCheckinMapper = dailyCheckinMapper;
        this.userMapper = userMapper;
    }

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

    @Override
    public boolean isCheckedInToday(Long userId) {
        Date today = Date.valueOf(LocalDate.now());
        DailyCheckin checkin = dailyCheckinMapper.findByUserIdAndDate(userId, today);
        return checkin != null;
    }

    @Override
    public int getTotalCheckinDays(Long userId) {
        return dailyCheckinMapper.countByUserId(userId);
    }

    @Override
    public int getContinuousDays(Long userId) {
        if (!isCheckedInToday(userId)) {
            return 0;
        }
        return calculateContinuousDays(userId);
    }

    @Override
    public int getWeekCheckinDays(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        
        Date startDate = Date.valueOf(weekStart);
        Date endDate = Date.valueOf(weekEnd);
        
        return dailyCheckinMapper.countByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<DailyCheckin> getCheckinHistory(Long userId) {
        return dailyCheckinMapper.findByUserId(userId);
    }

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
}
