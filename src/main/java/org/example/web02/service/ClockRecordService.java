package org.example.web02.service;

import org.example.web02.entity.ClockRecord;

import java.util.List;
import java.util.Map;

public interface ClockRecordService {

    ClockRecord saveOrUpdateClockRecord(Long userId, Long planId, List<Map<String, Object>> finishItems, 
                                        List<Map<String, Object>> totalItems, Map<String, String> unfinishReasons);

    ClockRecord getTodayClockRecord(Long userId, Long planId);

    List<ClockRecord> getWeekClockRecords(Long userId);

    List<ClockRecord> getClockRecordsByDateRange(Long userId, String startDate, String endDate);

    ClockRecord getClockRecordById(Long userId, Long recordId);

    void deleteClockRecord(Long userId, Long recordId);

    Map<String, Object> getWeeklyStats(Long userId);
}