package org.example.web02.service;

import java.util.Map;

public interface DashScopeService {

    String generateText(String prompt);

    Map<String, Object> generateHealthPlan(Long userId);
}
