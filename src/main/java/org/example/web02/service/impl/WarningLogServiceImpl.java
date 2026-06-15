package org.example.web02.service.impl;

import org.example.web02.entity.WarningLog;
import org.example.web02.mapper.ClockRecordMapper;
import org.example.web02.mapper.DailyCheckinMapper;
import org.example.web02.mapper.UserMapper;
import org.example.web02.mapper.WarningLogMapper;
import org.example.web02.mapper.WeightRecordMapper;
import org.example.web02.service.WarningLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.List;

/**
 * 警告日志服务实现类
 * 负责健康预警的创建、查询和管理，包括体重波动预警、BMI异常预警、打卡提醒等
 */
@Service
public class WarningLogServiceImpl implements WarningLogService {

    /** 体重波动阈值（kg），超过此值触发预警 */
    private static final int WEIGHT_FLUCTUATION_THRESHOLD_KG = 3;
    /** 连续未打卡天数阈值，超过此值触发预警 */
    private static final int CLOCK_MISS_DAYS = 3;

    /** 警告日志数据访问层 */
    private final WarningLogMapper warningLogMapper;
    /** 体重记录数据访问层 */
    private final WeightRecordMapper weightRecordMapper;
    /** 打卡记录数据访问层 */
    private final ClockRecordMapper clockRecordMapper;
    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 每日签到数据访问层 */
    private final DailyCheckinMapper dailyCheckinMapper;

    /**
     * 构造函数注入依赖
     * @param warningLogMapper 警告日志Mapper
     * @param weightRecordMapper 体重记录Mapper
     * @param clockRecordMapper 打卡记录Mapper
     * @param userMapper 用户Mapper
     * @param dailyCheckinMapper 每日签到Mapper
     */
    public WarningLogServiceImpl(WarningLogMapper warningLogMapper,
                                 WeightRecordMapper weightRecordMapper,
                                 ClockRecordMapper clockRecordMapper,
                                 UserMapper userMapper,
                                 DailyCheckinMapper dailyCheckinMapper) {
        this.warningLogMapper = warningLogMapper;
        this.weightRecordMapper = weightRecordMapper;
        this.clockRecordMapper = clockRecordMapper;
        this.userMapper = userMapper;
        this.dailyCheckinMapper = dailyCheckinMapper;
    }

    /**
     * 创建警告日志
     * 
     * @param userId 用户ID
     * @param warningType 警告类型（见WarningLog.TYPE_*常量）
     * @param content 警告内容
     */
    @Override
    @Transactional
    public void createWarning(Long userId, String warningType, String content) {
        WarningLog warningLog = new WarningLog();
        warningLog.setUserId(userId);
        warningLog.setWarningType(warningType);
        warningLog.setWarningContent(content);
        warningLog.setIsRead(0);
        warningLog.setCreateTime(new java.util.Date());
        warningLog.setUpdateTime(new java.util.Date());
        warningLog.setIsDeleted(0);
        warningLogMapper.insert(warningLog);
    }

    /**
     * 获取用户所有警告日志
     * 同时检查今日是否已签到和记录体重，未完成则创建提醒
     * 
     * @param userId 用户ID
     * @return 警告日志列表
     */
    @Override
    @Transactional
    public List<WarningLog> getWarnings(Long userId) {
        Date today = new Date(System.currentTimeMillis());
        
        boolean checkedIn = dailyCheckinMapper.checkTodayCheckin(userId, today) > 0;
        if (!checkedIn) {
            int todayCount = warningLogMapper.countTodayWarning(userId, WarningLog.TYPE_CHECKIN_REMIND);
            if (todayCount == 0) {
                createWarning(userId, WarningLog.TYPE_CHECKIN_REMIND, "今日还未签到，记得完成每日签到哦");
            }
        }
        
        boolean recordedWeight = weightRecordMapper.checkTodayRecord(userId, today) > 0;
        if (!recordedWeight) {
            int todayCount = warningLogMapper.countTodayWarning(userId, WarningLog.TYPE_WEIGHT_RECORD_REMIND);
            if (todayCount == 0) {
                createWarning(userId, WarningLog.TYPE_WEIGHT_RECORD_REMIND, "今日还未录入体重，记得记录今日体重变化");
            }
        }
        
        return warningLogMapper.findByUserId(userId);
    }

    /**
     * 获取用户未读警告日志
     * 
     * @param userId 用户ID
     * @return 未读警告日志列表
     */
    @Override
    public List<WarningLog> getUnreadWarnings(Long userId) {
        return warningLogMapper.findUnreadByUserId(userId);
    }

    /**
     * 统计用户未读警告数量
     * 
     * @param userId 用户ID
     * @return 未读警告数量
     */
    @Override
    public long countUnreadWarnings(Long userId) {
        return warningLogMapper.countUnreadByUserId(userId);
    }

    /**
     * 标记用户所有警告为已读
     * 
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        warningLogMapper.updateReadStatus(userId, 1);
    }

    /**
     * 标记单条警告为已读
     * 
     * @param id 警告日志ID
     */
    @Override
    @Transactional
    public void markAsRead(Long id) {
        warningLogMapper.updateReadStatusById(id, 1);
    }

    /**
     * 删除警告日志
     * 
     * @param id 警告日志ID
     */
    @Override
    @Transactional
    public void deleteWarning(Long id) {
        warningLogMapper.deleteById(id);
    }

    /**
     * 检查体重波动预警
     * 对比最近7天内的体重变化，波动超过阈值时创建预警
     * 
     * @param userId 用户ID
     * @param newWeight 新记录的体重值
     */
    @Override
    @Transactional
    public void checkWeightFluctuation(Long userId, double newWeight) {
        List<BigDecimal> weights = weightRecordMapper.getWeightsInLast7Days(userId);
        
        if (weights.size() < 2) {
            return;
        }

        double minWeight = Double.MAX_VALUE;
        double maxWeight = Double.MIN_VALUE;
        
        for (BigDecimal weight : weights) {
            double w = weight.doubleValue();
            minWeight = Math.min(minWeight, w);
            maxWeight = Math.max(maxWeight, w);
        }

        maxWeight = Math.max(maxWeight, newWeight);
        minWeight = Math.min(minWeight, newWeight);

        double fluctuation = maxWeight - minWeight;
        
        if (fluctuation > WEIGHT_FLUCTUATION_THRESHOLD_KG) {
            int todayCount = warningLogMapper.countTodayWarning(userId, WarningLog.TYPE_WEIGHT_FLUCTUATION);
            if (todayCount == 0) {
                createWarning(userId, WarningLog.TYPE_WEIGHT_FLUCTUATION, "体重短时间波动较大（7天内波动超过" + 
                    BigDecimal.valueOf(fluctuation).setScale(1, RoundingMode.HALF_UP) + "kg）");
            }
        }
    }

    /**
     * 检查BMI异常预警
     * BMI >= 24 为超重，BMI >= 28 为肥胖
     * 
     * @param userId 用户ID
     * @param bmi BMI值
     */
    @Override
    @Transactional
    public void checkBmiAbnormal(Long userId, double bmi) {
        final String warningContent;
        if (bmi >= 28.0) {
            warningContent = "您的BMI值为" + BigDecimal.valueOf(bmi).setScale(1, RoundingMode.HALF_UP) + "，属于肥胖范围，请注意健康";
        } else if (bmi >= 24.0) {
            warningContent = "您的BMI值为" + BigDecimal.valueOf(bmi).setScale(1, RoundingMode.HALF_UP) + "，属于超重范围，请注意健康";
        } else {
            warningContent = null;
        }

        if (warningContent != null) {
            boolean hasActiveWarning = warningLogMapper.findByUserIdAndType(userId, WarningLog.TYPE_BMI_ABNORMAL)
                    .stream()
                    .anyMatch(log -> log.getWarningContent().equals(warningContent));
            
            if (!hasActiveWarning) {
                createWarning(userId, WarningLog.TYPE_BMI_ABNORMAL, warningContent);
            }
        }
    }

    /**
     * 检查用户连续未打卡情况
     * 遍历所有活跃用户，检查是否连续多日未完成计划打卡
     */
    @Override
    @Transactional
    public void checkClockMiss() {
        List<Long> activeUserIds = userMapper.findAllActiveUserIds();
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        
        for (Long userId : activeUserIds) {
            List<org.example.web02.entity.ClockRecord> recentRecords = clockRecordMapper.findByUserId(userId);
            
            int consecutiveMissDays = 0;
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(today);
            
            for (int i = 1; i <= CLOCK_MISS_DAYS; i++) {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -i);
                java.sql.Date checkDate = new java.sql.Date(calendar.getTimeInMillis());
                
                boolean hasRecord = recentRecords.stream()
                        .anyMatch(record -> record.getRecordDate().equals(checkDate));
                
                if (!hasRecord) {
                    consecutiveMissDays++;
                } else {
                    break;
                }
            }
            
            if (consecutiveMissDays >= CLOCK_MISS_DAYS) {
                int todayWarningCount = warningLogMapper.countTodayWarning(userId, WarningLog.TYPE_CLOCK_MISS);
                if (todayWarningCount == 0) {
                    createWarning(userId, WarningLog.TYPE_CLOCK_MISS, "您已多日未完成计划打卡，请继续坚持");
                }
            }
        }
    }

    /**
     * 检查签到提醒
     * 如果今日未签到，创建签到提醒警告
     * 
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void checkCheckinRemind(Long userId) {
        int todayCount = warningLogMapper.countTodayWarning(userId, WarningLog.TYPE_CHECKIN_REMIND);
        if (todayCount == 0) {
            createWarning(userId, WarningLog.TYPE_CHECKIN_REMIND, "今日还未签到，记得完成每日签到哦");
        }
    }

    /**
     * 检查体重记录提醒
     * 如果今日未记录体重，创建体重记录提醒警告
     * 
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void checkWeightRecordRemind(Long userId) {
        int todayCount = warningLogMapper.countTodayWarning(userId, WarningLog.TYPE_WEIGHT_RECORD_REMIND);
        if (todayCount == 0) {
            createWarning(userId, WarningLog.TYPE_WEIGHT_RECORD_REMIND, "今日还未录入体重，记得记录今日体重变化");
        }
    }
}