package org.example.web02.service.impl;

import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.UserHealth;
import org.example.web02.entity.WeightRecord;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.UserHealthMapper;
import org.example.web02.mapper.WeightRecordMapper;
import org.example.web02.service.WarningLogService;
import org.example.web02.service.WeightRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * 体重记录服务实现类
 * 负责用户体重数据的记录、查询、更新和删除，以及相关健康指标的计算
 */
@Service
public class WeightRecordServiceImpl implements WeightRecordService {

    /** 体重记录数据访问层 */
    private final WeightRecordMapper weightRecordMapper;
    /** 警告日志服务（用于体重波动预警） */
    private final WarningLogService warningLogService;
    /** 用户健康档案数据访问层 */
    private final UserHealthMapper userHealthMapper;

    /**
     * 构造函数注入依赖
     * @param weightRecordMapper 体重记录Mapper
     * @param warningLogService 警告日志服务
     * @param userHealthMapper 用户健康档案Mapper
     */
    public WeightRecordServiceImpl(WeightRecordMapper weightRecordMapper, 
                                   WarningLogService warningLogService,
                                   UserHealthMapper userHealthMapper) {
        this.weightRecordMapper = weightRecordMapper;
        this.warningLogService = warningLogService;
        this.userHealthMapper = userHealthMapper;
    }

    /**
     * 记录用户体重
     * 流程：检查今日是否已记录 → 创建体重记录 → 检查体重波动预警 → 更新用户健康档案
     * 
     * @param userId 用户ID
     * @param weight 体重值（单位：kg）
     * @param remark 备注信息
     * @return 创建的体重记录实体
     * @throws BusinessException 今日已记录体重时抛出
     */
    @Override
    @Transactional
    public WeightRecord recordWeight(Long userId, BigDecimal weight, String remark) {
        Date today = Date.valueOf(LocalDate.now());

        WeightRecord existing = weightRecordMapper.findByUserIdAndDate(userId, today);
        if (existing != null) {
            throw new BusinessException("今日已记录体重，无需重复插入");
        }

        WeightRecord record = new WeightRecord();
        record.setUserId(userId);
        record.setWeight(weight);
        record.setRecordDate(today);
        record.setRemark(remark);
        record.setCreateTime(new java.util.Date());
        record.setUpdateTime(new java.util.Date());

        weightRecordMapper.insert(record);

        warningLogService.checkWeightFluctuation(userId, weight.doubleValue());

        UserHealth userHealth = userHealthMapper.findByUserId(userId);
        if (userHealth != null) {
            userHealth.setWeight(weight);
            
            BigDecimal[] metrics = calculateMetrics(userHealth);
            userHealth.setBmr(metrics[0]);
            userHealth.setTdee(metrics[1]);
            userHealth.setBmi(metrics[2]);
            
            userHealth.setUpdateTime(new java.util.Date());
            userHealthMapper.update(userHealth);
        }

        return record;
    }

    /**
     * 计算健康指标（BMR、TDEE、BMI）
     * BMR（基础代谢率）使用Mifflin-St Jeor公式计算
     * TDEE（每日总能量消耗）= BMR × 活动系数
     * BMI（身体质量指数）= 体重(kg) / 身高(m)²
     * 
     * @param userHealth 用户健康档案
     * @return 包含BMR、TDEE、BMI的数组
     */
    private BigDecimal[] calculateMetrics(UserHealth userHealth) {
        BigDecimal height = userHealth.getHeight();
        BigDecimal weight = userHealth.getWeight();
        int age = userHealth.getAge();
        int gender = userHealth.getGender();
        int activityLevel = userHealth.getActivityLevel();

        BigDecimal bmi = weight.divide(height.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP).pow(2), 2, RoundingMode.HALF_UP);

        BigDecimal bmr;
        if (gender == 0) {
            bmr = BigDecimal.valueOf(10).multiply(weight)
                    .add(BigDecimal.valueOf(6.25).multiply(height))
                    .subtract(BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(age)))
                    .subtract(BigDecimal.valueOf(161));
        } else {
            bmr = BigDecimal.valueOf(10).multiply(weight)
                    .add(BigDecimal.valueOf(6.25).multiply(height))
                    .subtract(BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(age)))
                    .add(BigDecimal.valueOf(5));
        }
        bmr = bmr.setScale(2, RoundingMode.HALF_UP);

        BigDecimal activityFactor;
        switch (activityLevel) {
            case 1 -> activityFactor = BigDecimal.valueOf(1.2);
            case 2 -> activityFactor = BigDecimal.valueOf(1.375);
            case 3 -> activityFactor = BigDecimal.valueOf(1.55);
            default -> activityFactor = BigDecimal.valueOf(1.2);
        }

        BigDecimal tdee = bmr.multiply(activityFactor).setScale(2, RoundingMode.HALF_UP);

        return new BigDecimal[]{bmr, tdee, bmi};
    }

    /**
     * 获取用户体重历史记录
     * 
     * @param userId 用户ID
     * @return 体重记录列表（按时间倒序）
     */
    @Override
    public List<WeightRecord> getWeightHistory(Long userId) {
        return weightRecordMapper.findByUserId(userId);
    }

    /**
     * 获取用户体重历史记录（带过滤条件）
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param sortBy 排序方式（可选）
     * @return 过滤后的体重记录列表
     */
    @Override
    public List<WeightRecord> getWeightHistoryFiltered(Long userId, String startDate, String endDate, String sortBy) {
        Date start = (startDate != null && !startDate.isEmpty()) ? Date.valueOf(startDate) : null;
        Date end = (endDate != null && !endDate.isEmpty()) ? Date.valueOf(endDate) : null;
        return weightRecordMapper.findByUserIdWithFilter(userId, start, end, sortBy);
    }

    /**
     * 分页获取用户体重历史记录
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param sortBy 排序方式（可选）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<WeightRecord> getWeightHistoryPaginated(Long userId, String startDate, String endDate, String sortBy, int pageNum, int pageSize) {
        Date start = (startDate != null && !startDate.isEmpty()) ? Date.valueOf(startDate) : null;
        Date end = (endDate != null && !endDate.isEmpty()) ? Date.valueOf(endDate) : null;
        
        long offset = (long) (pageNum - 1) * pageSize;
        List<WeightRecord> records = weightRecordMapper.findByUserIdWithFilterPaginated(userId, start, end, sortBy, offset, pageSize);
        long total = weightRecordMapper.countByUserIdWithFilter(userId, start, end);
        
        return PageResult.of(records, total, pageNum, pageSize);
    }

    /**
     * 删除体重记录
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     * @throws BusinessException 记录不存在或无权删除时抛出
     */
    @Override
    @Transactional
    public void deleteWeightRecord(Long userId, Long recordId) {
        WeightRecord record = weightRecordMapper.findById(recordId);
        if (record == null) {
            throw new BusinessException("体重记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此记录");
        }
        weightRecordMapper.deleteById(recordId);
    }

    /**
     * 更新体重记录
     * 同时更新用户健康档案中的体重及相关健康指标
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     * @param weight 新的体重值
     * @throws BusinessException 记录不存在或无权修改时抛出
     */
    @Override
    @Transactional
    public void updateWeight(Long userId, Long recordId, BigDecimal weight) {
        WeightRecord record = weightRecordMapper.findById(recordId);
        if (record == null) {
            throw new BusinessException("体重记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此记录");
        }
        record.setWeight(weight);
        weightRecordMapper.update(record);

        UserHealth userHealth = userHealthMapper.findByUserId(userId);
        if (userHealth != null) {
            userHealth.setWeight(weight);
            
            BigDecimal[] metrics = calculateMetrics(userHealth);
            userHealth.setBmr(metrics[0]);
            userHealth.setTdee(metrics[1]);
            userHealth.setBmi(metrics[2]);
            
            userHealth.setUpdateTime(new java.util.Date());
            userHealthMapper.update(userHealth);
        }
    }

    /**
     * 获取用户最近30天的体重记录
     * 用于体重趋势图表展示
     * 
     * @param userId 用户ID
     * @return 最近30天的体重记录列表
     */
    @Override
    public List<WeightRecord> getRecent30DaysWeight(Long userId) {
        return weightRecordMapper.findRecent30DaysWeight(userId);
    }
}
