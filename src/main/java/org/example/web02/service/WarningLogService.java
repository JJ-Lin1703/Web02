package org.example.web02.service;

import org.example.web02.entity.WarningLog;

import java.util.List;

public interface WarningLogService {

    void createWarning(Long userId, String warningType, String content);

    List<WarningLog> getWarnings(Long userId);

    List<WarningLog> getUnreadWarnings(Long userId);

    long countUnreadWarnings(Long userId);

    void markAllAsRead(Long userId);

    void markAsRead(Long id);

    void deleteWarning(Long id);

    void checkWeightFluctuation(Long userId, double newWeight);

    void checkBmiAbnormal(Long userId, double bmi);

    void checkClockMiss();

    void checkCheckinRemind(Long userId);

    void checkWeightRecordRemind(Long userId);
}