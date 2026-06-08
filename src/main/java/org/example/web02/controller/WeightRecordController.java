package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.WeightRecord;
import org.example.web02.service.WeightRecordService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weight-record")
public class WeightRecordController {

    private final WeightRecordService weightRecordService;

    public WeightRecordController(WeightRecordService weightRecordService) {
        this.weightRecordService = weightRecordService;
    }

    @PostMapping
    public ApiResponse<WeightRecord> recordWeight(Authentication authentication,
                                                   @RequestBody Map<String, Object> request) {
        Long userId = (Long) authentication.getPrincipal();
        BigDecimal weight = new BigDecimal(request.get("weight").toString());
        String remark = (String) request.getOrDefault("remark", null);
        WeightRecord record = weightRecordService.recordWeight(userId, weight, remark);
        return ApiResponse.success("体重记录成功", record);
    }

    @GetMapping("/history")
    public ApiResponse<List<WeightRecord>> getWeightHistory(Authentication authentication,
                                                             @RequestParam(required = false) String startDate,
                                                             @RequestParam(required = false) String endDate,
                                                             @RequestParam(required = false) String sortBy) {
        Long userId = (Long) authentication.getPrincipal();
        if (startDate != null || endDate != null || sortBy != null) {
            List<WeightRecord> history = weightRecordService.getWeightHistoryFiltered(userId, startDate, endDate, sortBy);
            return ApiResponse.success(history);
        }
        List<WeightRecord> history = weightRecordService.getWeightHistory(userId);
        return ApiResponse.success(history);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWeightRecord(Authentication authentication,
                                                 @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        weightRecordService.deleteWeightRecord(userId, id);
        return ApiResponse.success("删除成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateWeight(Authentication authentication,
                                           @PathVariable Long id,
                                           @RequestBody Map<String, Object> request) {
        Long userId = (Long) authentication.getPrincipal();
        BigDecimal weight = new BigDecimal(request.get("weight").toString());
        weightRecordService.updateWeight(userId, id, weight);
        return ApiResponse.success("体重更新成功");
    }

    @GetMapping("/trend")
    public ApiResponse<List<WeightRecord>> getWeightTrend(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<WeightRecord> trend = weightRecordService.getRecent30DaysWeight(userId);
        return ApiResponse.success(trend);
    }
}
