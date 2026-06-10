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

@Service
public class WeightRecordServiceImpl implements WeightRecordService {

    private final WeightRecordMapper weightRecordMapper;
    private final WarningLogService warningLogService;
    private final UserHealthMapper userHealthMapper;

    public WeightRecordServiceImpl(WeightRecordMapper weightRecordMapper, 
                                   WarningLogService warningLogService,
                                   UserHealthMapper userHealthMapper) {
        this.weightRecordMapper = weightRecordMapper;
        this.warningLogService = warningLogService;
        this.userHealthMapper = userHealthMapper;
    }

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

    @Override
    public List<WeightRecord> getWeightHistory(Long userId) {
        return weightRecordMapper.findByUserId(userId);
    }

    @Override
    public List<WeightRecord> getWeightHistoryFiltered(Long userId, String startDate, String endDate, String sortBy) {
        Date start = (startDate != null && !startDate.isEmpty()) ? Date.valueOf(startDate) : null;
        Date end = (endDate != null && !endDate.isEmpty()) ? Date.valueOf(endDate) : null;
        return weightRecordMapper.findByUserIdWithFilter(userId, start, end, sortBy);
    }

    @Override
    public PageResult<WeightRecord> getWeightHistoryPaginated(Long userId, String startDate, String endDate, String sortBy, int pageNum, int pageSize) {
        Date start = (startDate != null && !startDate.isEmpty()) ? Date.valueOf(startDate) : null;
        Date end = (endDate != null && !endDate.isEmpty()) ? Date.valueOf(endDate) : null;
        
        long offset = (long) (pageNum - 1) * pageSize;
        List<WeightRecord> records = weightRecordMapper.findByUserIdWithFilterPaginated(userId, start, end, sortBy, offset, pageSize);
        long total = weightRecordMapper.countByUserIdWithFilter(userId, start, end);
        
        return PageResult.of(records, total, pageNum, pageSize);
    }

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

    @Override
    public List<WeightRecord> getRecent30DaysWeight(Long userId) {
        return weightRecordMapper.findRecent30DaysWeight(userId);
    }
}
