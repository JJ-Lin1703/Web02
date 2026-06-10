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

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping("/daily")
    public ApiResponse<Map<String, Object>> dailyCheckin(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        checkinService.checkin(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalDays", checkinService.getTotalCheckinDays(userId));
        result.put("continuousDays", checkinService.getContinuousDays(userId));
        result.put("weekDays", checkinService.getWeekCheckinDays(userId));
        
        return ApiResponse.success("签到成功", result);
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getCheckinStatus(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        
        Map<String, Object> result = new HashMap<>();
        result.put("checkedInToday", checkinService.isCheckedInToday(userId));
        result.put("totalDays", checkinService.getTotalCheckinDays(userId));
        result.put("continuousDays", checkinService.getContinuousDays(userId));
        result.put("weekDays", checkinService.getWeekCheckinDays(userId));
        
        return ApiResponse.success(result);
    }

    @GetMapping("/history")
    public ApiResponse<PageResult<DailyCheckin>> getCheckinHistory(Authentication authentication,
                                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(checkinService.getCheckinHistoryPaginated(userId, pageNum, pageSize));
    }
}
