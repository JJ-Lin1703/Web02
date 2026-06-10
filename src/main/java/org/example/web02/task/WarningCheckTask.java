package org.example.web02.task;

import org.example.web02.mapper.DailyCheckinMapper;
import org.example.web02.mapper.UserMapper;
import org.example.web02.mapper.WeightRecordMapper;
import org.example.web02.service.WarningLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

@Component
public class WarningCheckTask {

    private static final Logger logger = LoggerFactory.getLogger(WarningCheckTask.class);

    private final WarningLogService warningLogService;
    private final UserMapper userMapper;
    private final DailyCheckinMapper dailyCheckinMapper;
    private final WeightRecordMapper weightRecordMapper;

    public WarningCheckTask(WarningLogService warningLogService,
                           UserMapper userMapper,
                           DailyCheckinMapper dailyCheckinMapper,
                           WeightRecordMapper weightRecordMapper) {
        this.warningLogService = warningLogService;
        this.userMapper = userMapper;
        this.dailyCheckinMapper = dailyCheckinMapper;
        this.weightRecordMapper = weightRecordMapper;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkClockMissEveryDay() {
        logger.info("开始执行连续未打卡预警检测任务");
        try {
            warningLogService.checkClockMiss();
            logger.info("连续未打卡预警检测任务执行完成");
        } catch (Exception e) {
            logger.error("连续未打卡预警检测任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 0 20 * * ?")
    public void checkCheckinRemind() {
        logger.info("开始执行未签到提醒任务");
        try {
            Date today = new Date(System.currentTimeMillis());
            List<Long> userIds = userMapper.findAllActiveUserIds();
            
            for (Long userId : userIds) {
                boolean checkedIn = dailyCheckinMapper.checkTodayCheckin(userId, today) > 0;
                if (!checkedIn) {
                    warningLogService.checkCheckinRemind(userId);
                }
            }
            logger.info("未签到提醒任务执行完成");
        } catch (Exception e) {
            logger.error("未签到提醒任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 0 20 * * ?")
    public void checkWeightRecordRemind() {
        logger.info("开始执行未录入体重提醒任务");
        try {
            Date today = new Date(System.currentTimeMillis());
            List<Long> userIds = userMapper.findAllActiveUserIds();
            
            for (Long userId : userIds) {
                boolean recorded = weightRecordMapper.checkTodayRecord(userId, today) > 0;
                if (!recorded) {
                    warningLogService.checkWeightRecordRemind(userId);
                }
            }
            logger.info("未录入体重提醒任务执行完成");
        } catch (Exception e) {
            logger.error("未录入体重提醒任务执行失败", e);
        }
    }
}