package org.example.web02.service;

import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.DailyCheckin;

import java.util.List;

public interface CheckinService {

    boolean checkin(Long userId);

    boolean isCheckedInToday(Long userId);

    int getTotalCheckinDays(Long userId);

    int getContinuousDays(Long userId);

    int getWeekCheckinDays(Long userId);

    List<DailyCheckin> getCheckinHistory(Long userId);

    PageResult<DailyCheckin> getCheckinHistoryPaginated(Long userId, int pageNum, int pageSize);
}
