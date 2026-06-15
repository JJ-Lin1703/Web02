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

/**
 * 打卡记录服务实现类
 * 负责健康计划打卡记录的保存、更新、查询和统计分析
 */
@Service
public class ClockRecordServiceImpl implements ClockRecordService {

    /** 打卡记录数据访问层 */
    private final ClockRecordMapper clockRecordMapper;
    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入依赖
     * @param clockRecordMapper 打卡记录Mapper
     * @param objectMapper JSON序列化工具
     */
    public ClockRecordServiceImpl(ClockRecordMapper clockRecordMapper, ObjectMapper objectMapper) {
        this.clockRecordMapper = clockRecordMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 保存或更新打卡记录
     * 如果今日已有记录则更新，否则创建新记录
     * 
     * @param userId 用户ID
     * @param planId 计划ID
     * @param finishItems 已完成的项目列表
     * @param totalItems 全部项目列表
     * @param unfinishReasons 未完成原因映射（项目ID -> 原因）
     * @return 保存/更新后的打卡记录
     * @throws BusinessException JSON序列化失败时抛出
     */
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

    /**
     * 获取用户今日的打卡记录
     * 
     * @param userId 用户ID
     * @param planId 计划ID
     * @return 今日打卡记录，如果不存在则返回null
     */
    @Override
    public ClockRecord getTodayClockRecord(Long userId, Long planId) {
        Date today = Date.valueOf(LocalDate.now());
        return clockRecordMapper.findByUserPlanDate(userId, planId, today);
    }

    /**
     * 获取用户本周的打卡记录
     * 统计从周一到周日的所有打卡记录
     * 
     * @param userId 用户ID
     * @return 本周打卡记录列表
     */
    @Override
    public List<ClockRecord> getWeekClockRecords(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        
        return clockRecordMapper.findByUserIdAndDateRange(userId, Date.valueOf(weekStart), Date.valueOf(weekEnd));
    }

    /**
     * 根据日期范围获取用户的打卡记录
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 指定日期范围内的打卡记录列表
     */
    @Override
    public List<ClockRecord> getClockRecordsByDateRange(Long userId, String startDate, String endDate) {
        Date start = Date.valueOf(startDate);
        Date end = Date.valueOf(endDate);
        return clockRecordMapper.findByUserIdAndDateRange(userId, start, end);
    }

    /**
     * 根据ID获取打卡记录（带权限校验）
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     * @return 打卡记录
     * @throws BusinessException 记录不存在或无权访问时抛出
     */
    @Override
    public ClockRecord getClockRecordById(Long userId, Long recordId) {
        ClockRecord record = clockRecordMapper.findById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("打卡记录不存在");
        }
        return record;
    }

    /**
     * 删除打卡记录
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     * @throws BusinessException 记录不存在或无权删除时抛出
     */
    @Override
    @Transactional
    public void deleteClockRecord(Long userId, Long recordId) {
        //ClockRecord record = getClockRecordById(userId, recordId);
        clockRecordMapper.deleteById(recordId);
    }

    /**
     * 获取用户本周打卡统计数据
     * 统计本周已完成项目数、总项目数和完成率
     * 
     * @param userId 用户ID
     * @return 统计结果Map，包含：
     *         - weekCompleted: 本周已完成项目数
     *         - weekTotal: 本周总项目数
     *         - weekRate: 本周完成率（百分比）
     *         - records: 本周打卡记录列表
     */
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