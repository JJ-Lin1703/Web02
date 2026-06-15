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

/**
 * 预警检测定时任务
 * 负责每日定时检查用户的打卡、签到、体重记录等情况，并发送相应提醒或预警
 * 任务调度：使用Spring的@Scheduled注解实现定时执行
 */
@Component
public class WarningCheckTask {

    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(WarningCheckTask.class);

    /** 警告日志服务 */
    private final WarningLogService warningLogService;
    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 每日签到数据访问层 */
    private final DailyCheckinMapper dailyCheckinMapper;
    /** 体重记录数据访问层 */
    private final WeightRecordMapper weightRecordMapper;

    /**
     * 构造函数注入依赖
     * @param warningLogService 警告日志服务
     * @param userMapper 用户Mapper
     * @param dailyCheckinMapper 每日签到Mapper
     * @param weightRecordMapper 体重记录Mapper
     */
    public WarningCheckTask(WarningLogService warningLogService,
                           UserMapper userMapper,
                           DailyCheckinMapper dailyCheckinMapper,
                           WeightRecordMapper weightRecordMapper) {
        this.warningLogService = warningLogService;
        this.userMapper = userMapper;
        this.dailyCheckinMapper = dailyCheckinMapper;
        this.weightRecordMapper = weightRecordMapper;
    }

    /**
     * 每日检查连续未打卡预警
     * 执行时间：每天早上9点
     * 遍历所有活跃用户，检查是否连续多日未打卡，生成预警通知
     */
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

    /**
     * 每日检查未签到提醒
     * 执行时间：每天晚上20点
     * 遍历所有活跃用户，检查今日是否已签到，未签到则发送提醒
     */
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

    /**
     * 每日检查未录入体重提醒
     * 执行时间：每天晚上20点
     * 遍历所有活跃用户，检查今日是否已录入体重，未录入则发送提醒
     */
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