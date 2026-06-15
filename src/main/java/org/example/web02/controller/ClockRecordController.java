package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.ClockRecord;
import org.example.web02.service.ClockRecordService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 打卡记录控制器
 *
 * 提供打卡记录相关的 API 接口，包括保存打卡记录、查询今日记录、
 * 获取周统计、按日期范围查询、删除记录等功能
 */
@RestController
@RequestMapping("/api/clock-record")
public class ClockRecordController {

    /** 打卡记录服务，用于处理打卡记录相关的业务逻辑 */
    private final ClockRecordService clockRecordService;

    /**
     * 构造函数，注入打卡记录服务
     *
     * @param clockRecordService 打卡记录服务实例
     */
    public ClockRecordController(ClockRecordService clockRecordService) {
        this.clockRecordService = clockRecordService;
    }

    /**
     * 保存或更新打卡记录
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param request 打卡记录请求，包含计划 ID、完成项列表、总项列表和未完成原因
     * @return 包含保存后的打卡记录的响应
     */
    @PostMapping("/save")
    public ApiResponse<ClockRecord> saveClockRecord(Authentication authentication, 
                                                    @RequestBody Map<String, Object> request) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 从请求中获取计划 ID
        Long planId = ((Number) request.get("planId")).longValue();

        // 从请求中获取已完成项列表
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> finishItems = (List<Map<String, Object>>) request.get("finishItems");

        // 从请求中获取总项列表
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> totalItems = (List<Map<String, Object>>) request.get("totalItems");

        // 从请求中获取未完成原因映射
        @SuppressWarnings("unchecked")
        Map<String, String> unfinishReasons = (Map<String, String>) request.get("unfinishReasons");

        // 调用服务保存或更新打卡记录
        ClockRecord record = clockRecordService.saveOrUpdateClockRecord(userId, planId, finishItems, totalItems, unfinishReasons);
        return ApiResponse.success("保存成功", record);
    }

    /**
     * 获取今日打卡记录
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param planId 计划 ID
     * @return 包含今日打卡记录的响应
     */
    @GetMapping("/today")
    public ApiResponse<ClockRecord> getTodayRecord(Authentication authentication, @RequestParam Long planId) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务获取今日打卡记录
        ClockRecord record = clockRecordService.getTodayClockRecord(userId, planId);
        return ApiResponse.success(record);
    }

    /**
     * 获取本周打卡统计
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含本周打卡统计数据的响应
     */
    @GetMapping("/week")
    public ApiResponse<Map<String, Object>> getWeeklyStats(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务获取本周统计
        Map<String, Object> stats = clockRecordService.getWeeklyStats(userId);
        return ApiResponse.success(stats);
    }

    /**
     * 按日期范围获取打卡记录
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param startDate 开始日期（字符串格式）
     * @param endDate 结束日期（字符串格式）
     * @return 包含日期范围内打卡记录列表的响应
     */
    @GetMapping("/range")
    public ApiResponse<List<ClockRecord>> getRecordsByRange(Authentication authentication,
                                                            @RequestParam String startDate,
                                                            @RequestParam String endDate) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务按日期范围查询打卡记录
        List<ClockRecord> records = clockRecordService.getClockRecordsByDateRange(userId, startDate, endDate);
        return ApiResponse.success(records);
    }

    /**
     * 根据 ID 获取打卡记录
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param id 打卡记录 ID
     * @return 包含指定打卡记录的响应
     */
    @GetMapping("/{id}")
    public ApiResponse<ClockRecord> getRecordById(Authentication authentication, @PathVariable Long id) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务根据 ID 获取打卡记录
        ClockRecord record = clockRecordService.getClockRecordById(userId, id);
        return ApiResponse.success(record);
    }

    /**
     * 删除打卡记录
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param id 打卡记录 ID
     * @return 操作结果响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecord(Authentication authentication, @PathVariable Long id) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务删除打卡记录
        clockRecordService.deleteClockRecord(userId, id);
        return ApiResponse.success("删除成功");
    }
}