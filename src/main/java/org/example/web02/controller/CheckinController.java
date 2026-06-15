package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.DailyCheckin;
import org.example.web02.service.CheckinService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签到控制器
 *
 * 提供用户签到相关的 API 接口，包括每日签到、签到状态查询和签到历史记录等功能
 */
@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    /** 签到服务，用于处理签到相关的业务逻辑 */
    private final CheckinService checkinService;

    /**
     * 构造函数，注入签到服务
     *
     * @param checkinService 签到服务实例
     */
    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    /**
     * 每日签到
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含签到统计信息的响应（总天数、连续天数、本周天数）
     */
    @PostMapping("/daily")
    public ApiResponse<Map<String, Object>> dailyCheckin(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用签到服务执行签到
        checkinService.checkin(userId);

        // 构建签到统计结果
        Map<String, Object> result = new HashMap<>();
        // 总签到天数
        result.put("totalDays", checkinService.getTotalCheckinDays(userId));
        // 连续签到天数
        result.put("continuousDays", checkinService.getContinuousDays(userId));
        // 本周签到天数
        result.put("weekDays", checkinService.getWeekCheckinDays(userId));

        return ApiResponse.success("签到成功", result);
    }

    /**
     * 获取签到状态
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含签到状态和统计信息的响应
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getCheckinStatus(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();

        // 构建签到状态结果
        Map<String, Object> result = new HashMap<>();
        // 今日是否已签到
        result.put("checkedInToday", checkinService.isCheckedInToday(userId));
        // 总签到天数
        result.put("totalDays", checkinService.getTotalCheckinDays(userId));
        // 连续签到天数
        result.put("continuousDays", checkinService.getContinuousDays(userId));
        // 本周签到天数
        result.put("weekDays", checkinService.getWeekCheckinDays(userId));

        return ApiResponse.success(result);
    }

    /**
     * 获取签到历史记录（分页）
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param pageNum 页码，默认为 1
     * @param pageSize 每页大小，默认为 10
     * @return 包含分页签到历史记录的响应
     */
    @GetMapping("/history")
    public ApiResponse<PageResult<DailyCheckin>> getCheckinHistory(Authentication authentication,
                                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用签到服务获取分页历史记录
        return ApiResponse.success(checkinService.getCheckinHistoryPaginated(userId, pageNum, pageSize));
    }
}
