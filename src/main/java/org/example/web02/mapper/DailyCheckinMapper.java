package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.DailyCheckin;

import java.sql.Date;
import java.util.List;

@Mapper
public interface DailyCheckinMapper {

    int insert(DailyCheckin checkin);

    DailyCheckin findByUserIdAndDate(@Param("userId") Long userId, @Param("checkinDate") Date checkinDate);

    long countByUserId(@Param("userId") Long userId);

    int countByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    List<DailyCheckin> findByUserId(@Param("userId") Long userId);

    List<DailyCheckin> findByUserIdPaginated(@Param("userId") Long userId,
                                              @Param("offset") long offset,
                                              @Param("limit") long limit);

    int getContinuousDays(@Param("userId") Long userId);

    int checkTodayCheckin(@Param("userId") Long userId, @Param("checkinDate") Date checkinDate);
}
