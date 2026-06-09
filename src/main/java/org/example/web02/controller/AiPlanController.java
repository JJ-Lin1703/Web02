package org.example.web02.controller;

import org.example.web02.dto.response.ApiResponse;
import org.example.web02.entity.AiPlan;
import org.example.web02.exception.BusinessException;
import org.example.web02.service.AiPlanService;
import org.example.web02.service.PdfExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    public AiPlanController(AiPlanService aiPlanService, PdfExportService pdfExportService) {
        this.aiPlanService = aiPlanService;
        this.pdfExportService = pdfExportService;
    }

    @PostMapping("/generate")
    public ApiResponse<AiPlan> generatePlan(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AiPlan plan = aiPlanService.generatePlan(userId);
        return ApiResponse.success("AI计划生成成功", plan);
    }

    @GetMapping("/history")
    public ApiResponse<List<AiPlan>> getPlanHistory(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<AiPlan> history = aiPlanService.getPlanHistory(userId);
        return ApiResponse.success(history);
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

    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> exportPlanPdf(Authentication authentication,
                                                 @PathVariable Long id) {
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
