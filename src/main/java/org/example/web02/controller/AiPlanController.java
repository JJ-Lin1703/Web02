package org.example.web02.controller;

import org.example.web02.dto.request.TweakPlanRequest;
import org.example.web02.dto.response.ApiResponse;
import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.AiPlan;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.AiPlanMapper;
import org.example.web02.service.AiPlanService;
import org.example.web02.service.PdfExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import org.example.web02.dto.request.UpdatePlanContentRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * AI 健康计划控制器
 *
 * 提供 AI 健康计划相关的 API 接口，包括计划生成、微调、查询、删除和导出等功能
 */
@RestController
@RequestMapping("/api/ai-plan")
public class AiPlanController {

    /** AI 健康计划服务，用于处理计划生成和管理等业务逻辑 */
    private final AiPlanService aiPlanService;

    /** PDF 导出服务，用于将健康计划导出为 PDF 文件 */
    private final PdfExportService pdfExportService;

    /** AI 健康计划 Mapper，用于数据库访问 */
    private final AiPlanMapper aiPlanMapper;

    /**
     * 构造函数，注入相关服务
     *
     * @param aiPlanService AI 健康计划服务实例
     * @param pdfExportService PDF 导出服务实例
     * @param aiPlanMapper AI 健康计划 Mapper 实例
     */
    public AiPlanController(AiPlanService aiPlanService, PdfExportService pdfExportService, AiPlanMapper aiPlanMapper) {
        this.aiPlanService = aiPlanService;
        this.pdfExportService = pdfExportService;
        this.aiPlanMapper = aiPlanMapper;
    }

    /**
     * 生成 AI 健康计划
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param apiKey DashScope API 密钥（可选，用于调用 AI 服务）
     * @return 包含生成的健康计划的响应
     */
    @PostMapping("/generate")
    public ApiResponse<AiPlan> generatePlan(Authentication authentication,
                                             @RequestHeader(value = "X-DashScope-API-Key", required = false) String apiKey) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务生成健康计划
        AiPlan plan = aiPlanService.generatePlan(userId, apiKey);
        return ApiResponse.success("AI计划生成成功", plan);
    }

    /**
     * 微调 AI 健康计划
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param request 微调请求，包含微调指令
     * @param apiKey DashScope API 密钥（可选，用于调用 AI 服务）
     * @return 包含微调后的健康计划的响应
     */
    @PostMapping("/tweak")
    public ApiResponse<AiPlan> tweakPlan(Authentication authentication,
                                          @RequestBody TweakPlanRequest request,
                                          @RequestHeader(value = "X-DashScope-API-Key", required = false) String apiKey) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务微调健康计划
        AiPlan plan = aiPlanService.tweakPlan(userId, request, apiKey);
        return ApiResponse.success("计划微调成功", plan);
    }

    /**
     * 获取健康计划历史记录（分页）
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param pageNum 页码，默认为 1
     * @param pageSize 每页大小，默认为 10
     * @return 包含分页健康计划历史记录的响应
     */
    @GetMapping("/history")
    public ApiResponse<PageResult<AiPlan>> getPlanHistory(Authentication authentication,
                                                           @RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务获取分页历史记录
        return ApiResponse.success(aiPlanService.getPlanHistoryPaginated(userId, pageNum, pageSize));
    }

    /**
     * 获取最新的健康计划
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return 包含最新健康计划的响应
     */
    @GetMapping("/latest")
    public ApiResponse<AiPlan> getLatestPlan(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务获取最新计划
        AiPlan plan = aiPlanService.getLatestPlan(userId);
        return ApiResponse.success(plan);
    }

    /**
     * 删除健康计划
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param id 计划 ID
     * @return 操作结果响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePlan(Authentication authentication,
                                         @PathVariable Long id) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务删除计划
        aiPlanService.deletePlan(userId, id);
        return ApiResponse.success("删除成功");
    }

    /**
     * 更新健康计划内容
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param id 计划 ID
     * @param request 更新请求，包含新的计划内容
     * @return 包含更新后的健康计划的响应
     */
    @PutMapping("/{id}/content")
    public ApiResponse<AiPlan> updatePlanContent(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @RequestBody UpdatePlanContentRequest request) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();
        // 调用服务更新计划内容
        AiPlan plan = aiPlanService.updatePlanContent(userId, id, request.getPlanContent());
        return ApiResponse.success("计划内容更新成功", plan);
    }

    /**
     * 导出指定健康计划为 PDF 文件
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @param id 计划 ID
     * @return PDF 文件的字节流响应
     */
    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> exportPlanPdf(Authentication authentication,
                                                 @PathVariable Long id) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();

        // 查询计划并验证权限
        AiPlan plan = aiPlanMapper.findById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException("计划不存在或无权访问");
        }

        // 调用服务导出 PDF
        byte[] pdfBytes = pdfExportService.exportHealthPlanToPdf(userId, plan);

        // 构建文件名（包含时间戳）
        String filename = "health_plan_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        // 对文件名进行 URL 编码，处理中文和特殊字符
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * 导出最新健康计划为 PDF 文件
     *
     * @param authentication 认证信息，用于获取当前用户 ID
     * @return PDF 文件的字节流响应
     */
    @GetMapping("/export/latest")
    public ResponseEntity<byte[]> exportLatestPlanPdf(Authentication authentication) {
        // 从认证信息中获取用户 ID
        Long userId = (Long) authentication.getPrincipal();

        // 获取最新计划
        AiPlan plan = aiPlanService.getLatestPlan(userId);
        if (plan == null) {
            throw new BusinessException("暂无健康计划");
        }

        // 调用服务导出 PDF
        byte[] pdfBytes = pdfExportService.exportHealthPlanToPdf(userId, plan);

        // 构建文件名（包含时间戳）
        String filename = "health_plan_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        // 对文件名进行 URL 编码，处理中文和特殊字符
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
