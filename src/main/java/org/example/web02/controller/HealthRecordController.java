package org.example.web02.controller;

import org.example.web02.dto.request.HealthRecordRequest;
import org.example.web02.dto.response.ApiResponse;
import org.example.web02.dto.response.HealthRecordResponse;
import org.example.web02.service.UserHealthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health-record")
public class HealthRecordController {

    private final UserHealthService userHealthService;

    public HealthRecordController(UserHealthService userHealthService) {
        this.userHealthService = userHealthService;
    }

    @GetMapping
    public ApiResponse<HealthRecordResponse> getHealthRecord(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        HealthRecordResponse response = userHealthService.getHealthRecord(userId);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<HealthRecordResponse> createHealthRecord(Authentication authentication,
                                                               @RequestBody HealthRecordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        HealthRecordResponse response = userHealthService.createHealthRecord(userId, request);
        return ApiResponse.success("健康档案创建成功", response);
    }

    @PutMapping
    public ApiResponse<HealthRecordResponse> updateHealthRecord(Authentication authentication,
                                                               @RequestBody HealthRecordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        HealthRecordResponse response = userHealthService.updateHealthRecord(userId, request);
        return ApiResponse.success("健康档案更新成功", response);
    }

    @GetMapping("/exists")
    public ApiResponse<HealthRecordExistsResponse> checkHealthRecordExists(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean exists = userHealthService.hasHealthRecord(userId);
        return ApiResponse.success(new HealthRecordExistsResponse(exists));
    }
    
    private static class HealthRecordExistsResponse {
        private final boolean exists;
        
        public HealthRecordExistsResponse(boolean exists) {
            this.exists = exists;
        }
        
        public boolean isExists() {
            return exists;
        }
    }
}
