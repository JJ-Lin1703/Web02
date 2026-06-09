package org.example.web02.service;

import java.util.Map;

public interface DashScopeService {

    String generateText(String prompt);

    String generateText(String prompt, int maxTokens);

    Map<String, Object> generateHealthPlan(Long userId);
}
