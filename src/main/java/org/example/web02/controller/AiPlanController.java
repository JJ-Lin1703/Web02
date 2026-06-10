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

@RestController
@RequestMapping("/api/ai-plan")
public class AiPlanController {

    private final AiPlanService aiPlanService;
    private final PdfExportService pdfExportService;
    private final AiPlanMapper aiPlanMapper;

    public AiPlanController(AiPlanService aiPlanService, PdfExportService pdfExportService, AiPlanMapper aiPlanMapper) {
        this.aiPlanService = aiPlanService;
        this.pdfExportService = pdfExportService;
        this.aiPlanMapper = aiPlanMapper;
    }

    @PostMapping("/generate")
    public ApiResponse<AiPlan> generatePlan(Authentication authentication,
                                             @RequestHeader(value = "X-DashScope-API-Key", required = false) String apiKey) {
        Long userId = (Long) authentication.getPrincipal();
        AiPlan plan = aiPlanService.generatePlan(userId, apiKey);
        return ApiResponse.success("AI计划生成成功", plan);
    }

    @PostMapping("/tweak")
    public ApiResponse<AiPlan> tweakPlan(Authentication authentication,
                                          @RequestBody TweakPlanRequest request,
                                          @RequestHeader(value = "X-DashScope-API-Key", required = false) String apiKey) {
        Long userId = (Long) authentication.getPrincipal();
        AiPlan plan = aiPlanService.tweakPlan(userId, request, apiKey);
        return ApiResponse.success("计划微调成功", plan);
    }

    @GetMapping("/history")
    public ApiResponse<PageResult<AiPlan>> getPlanHistory(Authentication authentication,
                                                           @RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(aiPlanService.getPlanHistoryPaginated(userId, pageNum, pageSize));
    }

    @GetMapping("/latest")
    public ApiResponse<AiPlan> getLatestPlan(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AiPlan plan = aiPlanService.getLatestPlan(userId);
        return ApiResponse.success(plan);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePlan(Authentication authentication,
                                         @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        aiPlanService.deletePlan(userId, id);
        return ApiResponse.success("删除成功");
    }

    @PutMapping("/{id}/content")
    public ApiResponse<AiPlan> updatePlanContent(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @RequestBody UpdatePlanContentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        AiPlan plan = aiPlanService.updatePlanContent(userId, id, request.getPlanContent());
        return ApiResponse.success("计划内容更新成功", plan);
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> exportPlanPdf(Authentication authentication,
                                                 @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        
        AiPlan plan = aiPlanMapper.findById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException("计划不存在或无权访问");
        }

        byte[] pdfBytes = pdfExportService.exportHealthPlanToPdf(userId, plan);

        String filename = "health_plan_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/export/latest")
    public ResponseEntity<byte[]> exportLatestPlanPdf(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        
        AiPlan plan = aiPlanService.getLatestPlan(userId);
        if (plan == null) {
            throw new BusinessException("暂无健康计划");
        }

        byte[] pdfBytes = pdfExportService.exportHealthPlanToPdf(userId, plan);

        String filename = "health_plan_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
