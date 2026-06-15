package org.example.web02.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.web02.entity.ClockRecord;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.ClockRecordMapper;
import org.example.web02.service.ClockRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClockRecordServiceImpl implements ClockRecordService {

    private final ClockRecordMapper clockRecordMapper;
    private final ObjectMapper objectMapper;

    public ClockRecordServiceImpl(ClockRecordMapper clockRecordMapper, ObjectMapper objectMapper) {
        this.clockRecordMapper = clockRecordMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ClockRecord saveOrUpdateClockRecord(Long userId, Long planId, List<Map<String, Object>> finishItems,
                                               List<Map<String, Object>> totalItems, Map<String, String> unfinishReasons) {
        Date today = Date.valueOf(LocalDate.now());
        
        ClockRecord existingRecord = clockRecordMapper.findByUserPlanDate(userId, planId, today);
        
        BigDecimal finishRate = BigDecimal.ZERO;
        if (!totalItems.isEmpty()) {
            finishRate = BigDecimal.valueOf((finishItems.size() * 100.0) / totalItems.size())
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        String finishItemJson;
        String totalItemJson;
        String unfinishReasonJson;

        try {
            finishItemJson = objectMapper.writeValueAsString(finishItems);
            totalItemJson = objectMapper.writeValueAsString(totalItems);
            unfinishReasonJson = unfinishReasons != null ? objectMapper.writeValueAsString(unfinishReasons) : null;
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON序列化失败");
        }

        ClockRecord record;
        if (existingRecord != null) {
            record = existingRecord;
            record.setFinishItem(finishItemJson);
            record.setTotalItem(totalItemJson);
            record.setUnfinishReason(unfinishReasonJson);
            record.setFinishRate(finishRate);
            record.setUpdateTime(new java.util.Date());
            clockRecordMapper.update(record);
        } else {
            record = new ClockRecord();
            record.setUserId(userId);
            record.setPlanId(planId);
            record.setRecordDate(today);
            record.setFinishItem(finishItemJson);
            record.setTotalItem(totalItemJson);
            record.setUnfinishReason(unfinishReasonJson);
            record.setFinishRate(finishRate);
            record.setIsDeleted(0);
            clockRecordMapper.insert(record);
        }

        return record;
    }

    @Override
    public ClockRecord getTodayClockRecord(Long userId, Long planId) {
        Date today = Date.valueOf(LocalDate.now());
        return clockRecordMapper.findByUserPlanDate(userId, planId, today);
    }

    @Override
    public List<ClockRecord> getWeekClockRecords(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        
        return clockRecordMapper.findByUserIdAndDateRange(userId, Date.valueOf(weekStart), Date.valueOf(weekEnd));
    }

    @Override
    public List<ClockRecord> getClockRecordsByDateRange(Long userId, String startDate, String endDate) {
        Date start = Date.valueOf(startDate);
        Date end = Date.valueOf(endDate);
        return clockRecordMapper.findByUserIdAndDateRange(userId, start, end);
    }

    @Override
    public ClockRecord getClockRecordById(Long userId, Long recordId) {
        ClockRecord record = clockRecordMapper.findById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("打卡记录不存在");
        }
        return record;
    }

    @Override
    @Transactional
    public void deleteClockRecord(Long userId, Long recordId) {
        //ClockRecord record = getClockRecordById(userId, recordId);
        clockRecordMapper.deleteById(recordId);
    }

    @Override
    public Map<String, Object> getWeeklyStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        List<ClockRecord> weekRecords = clockRecordMapper.findByUserIdAndDateRange(userId, 
                Date.valueOf(weekStart), Date.valueOf(weekEnd));

        int totalCompleted = 0;
        int totalItems = 0;

        for (ClockRecord record : weekRecords) {
            try {
                List<Map<String, Object>> finishItems = objectMapper.readValue(
                        record.getFinishItem(), new TypeReference<List<Map<String, Object>>>() {});
                List<Map<String, Object>> allItems = objectMapper.readValue(
                        record.getTotalItem(), new TypeReference<List<Map<String, Object>>>() {});
                
                totalCompleted += finishItems.size();
                totalItems += allItems.size();
            } catch (JsonProcessingException e) {
                // 忽略解析错误
            }
        }

        BigDecimal weekRate = totalItems > 0 ? 
                BigDecimal.valueOf((totalCompleted * 100.0) / totalItems).setScale(2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;

        Map<String, Object> stats = new HashMap<>();
        stats.put("weekCompleted", totalCompleted);
        stats.put("weekTotal", totalItems);
        stats.put("weekRate", weekRate);
        stats.put("records", weekRecords);

        return stats;
    }
}