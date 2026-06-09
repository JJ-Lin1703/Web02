package org.example.web02.service;

import org.example.web02.entity.AiPlan;

public interface PdfExportService {

    byte[] exportHealthPlanToPdf(Long userId, AiPlan plan);
}