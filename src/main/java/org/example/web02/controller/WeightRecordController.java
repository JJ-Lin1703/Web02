package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.WeightRecord;
import org.example.web02.service.WeightRecordService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 体重记录控制器
 *
 * 提供体重记录相关的 API 接口，包括记录体重、获取历史记录、
 * 删除记录、更新体重、获取体重趋势等功能
 */
@RestController
@RequestMapping("/api/weight-record")
public class WeightRecordController {

    /** 体重记录服务，用于处理体重记录相关的业务逻辑 */
    private final WeightRecordService weightRecordService;

    /**
     * 构造函数，注入体重记录服务
     *
     * @param weightRecordService 体重记录服务实例
     */
    public WeightRecordController(WeightRecordService weightRecordService) {
        this.weightRecordService = weightRecordService;
    }

    /**
     * 记录体重
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param request 请求体，包含体重和备注信息
     * @return 包含创建后的体重记录的响应
     */
    @PostMapping
    public ApiResponse<WeightRecord> recordWeight(Authentication authentication,
                                                   @RequestBody Map<String, Object> request) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 从请求体中获取体重值
        BigDecimal weight = new BigDecimal(request.get("weight").toString());
        // 从请求体中获取备注（可选）
        String remark = (String) request.getOrDefault("remark", null);
        // 调用服务记录体重
        WeightRecord record = weightRecordService.recordWeight(userId, weight, remark);
        return ApiResponse.success("体重记录成功", record);
    }

    /**
     * 获取体重历史记录（分页）
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param sortBy 排序字段（可选）
     * @param pageNum 页码，默认为 1
     * @param pageSize 每页大小，默认为 10
     * @return 包含分页体重记录结果的响应
     */
    @GetMapping("/history")
    public ApiResponse<PageResult<WeightRecord>> getWeightHistory(Authentication authentication,
                                                                  @RequestParam(required = false) String startDate,
                                                                  @RequestParam(required = false) String endDate,
                                                                  @RequestParam(required = false) String sortBy,
                                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务获取分页体重历史记录
        PageResult<WeightRecord> result = weightRecordService.getWeightHistoryPaginated(userId, startDate, endDate, sortBy, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    /**
     * 删除体重记录
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param id 体重记录 ID
     * @return 操作结果响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWeightRecord(Authentication authentication,
                                                 @PathVariable Long id) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务删除体重记录（软删除）
        weightRecordService.deleteWeightRecord(userId, id);
        return ApiResponse.success("删除成功");
    }

    /**
     * 更新体重记录
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param id 体重记录 ID
     * @param request 请求体，包含新的体重值
     * @return 操作结果响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateWeight(Authentication authentication,
                                           @PathVariable Long id,
                                           @RequestBody Map<String, Object> request) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 从请求体中获取新的体重值
        BigDecimal weight = new BigDecimal(request.get("weight").toString());
        // 调用服务更新体重记录
        weightRecordService.updateWeight(userId, id, weight);
        return ApiResponse.success("体重更新成功");
    }

    /**
     * 获取体重趋势（最近 30 天）
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含最近 30 天体重记录列表的响应
     */
    @GetMapping("/trend")
    public ApiResponse<List<WeightRecord>> getWeightTrend(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务获取最近 30 天的体重记录
        List<WeightRecord> trend = weightRecordService.getRecent30DaysWeight(userId);
        return ApiResponse.success(trend);
    }
}
