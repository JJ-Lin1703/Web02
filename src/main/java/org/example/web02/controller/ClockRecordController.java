package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.ClockRecord;
import org.example.web02.service.ClockRecordService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clock-record")
public class ClockRecordController {

    private final ClockRecordService clockRecordService;

    public ClockRecordController(ClockRecordService clockRecordService) {
        this.clockRecordService = clockRecordService;
    }

    @PostMapping("/save")
    public ApiResponse<ClockRecord> saveClockRecord(Authentication authentication, 
                                                    @RequestBody Map<String, Object> request) {
        Long userId = (Long) authentication.getPrincipal();
        Long planId = ((Number) request.get("planId")).longValue();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> finishItems = (List<Map<String, Object>>) request.get("finishItems");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> totalItems = (List<Map<String, Object>>) request.get("totalItems");
        
        @SuppressWarnings("unchecked")
        Map<String, String> unfinishReasons = (Map<String, String>) request.get("unfinishReasons");

        ClockRecord record = clockRecordService.saveOrUpdateClockRecord(userId, planId, finishItems, totalItems, unfinishReasons);
        return ApiResponse.success("保存成功", record);
    }

    @GetMapping("/today")
    public ApiResponse<ClockRecord> getTodayRecord(Authentication authentication, @RequestParam Long planId) {
        Long userId = (Long) authentication.getPrincipal();
        ClockRecord record = clockRecordService.getTodayClockRecord(userId, planId);
        return ApiResponse.success(record);
    }

    @GetMapping("/week")
    public ApiResponse<Map<String, Object>> getWeeklyStats(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Map<String, Object> stats = clockRecordService.getWeeklyStats(userId);
        return ApiResponse.success(stats);
    }

    @GetMapping("/range")
    public ApiResponse<List<ClockRecord>> getRecordsByRange(Authentication authentication,
                                                            @RequestParam String startDate,
                                                            @RequestParam String endDate) {
        Long userId = (Long) authentication.getPrincipal();
        List<ClockRecord> records = clockRecordService.getClockRecordsByDateRange(userId, startDate, endDate);
        return ApiResponse.success(records);
    }

    @GetMapping("/{id}")
    public ApiResponse<ClockRecord> getRecordById(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        ClockRecord record = clockRecordService.getClockRecordById(userId, id);
        return ApiResponse.success(record);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecord(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        clockRecordService.deleteClockRecord(userId, id);
        return ApiResponse.success("删除成功");
    }
}