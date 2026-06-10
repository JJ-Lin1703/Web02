package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.WarningLog;
import org.example.web02.service.WarningLogService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warnings")
public class WarningLogController {

    private final WarningLogService warningLogService;

    public WarningLogController(WarningLogService warningLogService) {
        this.warningLogService = warningLogService;
    }

    @GetMapping
    public ApiResponse<List<WarningLog>> getWarnings(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<WarningLog> warnings = warningLogService.getWarnings(userId);
        return ApiResponse.success(warnings);
    }

    @GetMapping("/unread")
    public ApiResponse<List<WarningLog>> getUnreadWarnings(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<WarningLog> warnings = warningLogService.getUnreadWarnings(userId);
        return ApiResponse.success(warnings);
    }

    @GetMapping("/unread/count")
    public ApiResponse<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        long count = warningLogService.countUnreadWarnings(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return ApiResponse.success(result);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        warningLogService.markAllAsRead(userId);
        return ApiResponse.success("全部已读");
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        warningLogService.markAsRead(id);
        return ApiResponse.success("已读");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWarning(@PathVariable Long id) {
        warningLogService.deleteWarning(id);
        return ApiResponse.success("删除成功");
    }
}