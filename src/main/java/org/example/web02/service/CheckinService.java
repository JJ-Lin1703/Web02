package org.example.web02.service;

public interface CheckinService {

    boolean checkin(Long userId);

    boolean isCheckedInToday(Long userId);

    int getTotalCheckinDays(Long userId);

    int getContinuousDays(Long userId);

    int getWeekCheckinDays(Long userId);
}
