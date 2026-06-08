package org.example.web02.service;

import org.example.web02.dto.request.HealthRecordRequest;
import org.example.web02.dto.response.HealthRecordResponse;

public interface UserHealthService {

    HealthRecordResponse createHealthRecord(Long userId, HealthRecordRequest request);

    HealthRecordResponse updateHealthRecord(Long userId, HealthRecordRequest request);

    HealthRecordResponse getHealthRecord(Long userId);

    boolean hasHealthRecord(Long userId);
}
