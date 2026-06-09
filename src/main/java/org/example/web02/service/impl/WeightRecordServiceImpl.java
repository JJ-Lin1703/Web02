package org.example.web02.service.impl;

import org.example.web02.entity.WeightRecord;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.WeightRecordMapper;
import org.example.web02.service.WeightRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
public class WeightRecordServiceImpl implements WeightRecordService {

    private final WeightRecordMapper weightRecordMapper;

    public WeightRecordServiceImpl(WeightRecordMapper weightRecordMapper) {
        this.weightRecordMapper = weightRecordMapper;
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
        return record;
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
    }

    @Override
    public List<WeightRecord> getRecent30DaysWeight(Long userId) {
        return weightRecordMapper.findRecent30DaysWeight(userId);
    }
}
