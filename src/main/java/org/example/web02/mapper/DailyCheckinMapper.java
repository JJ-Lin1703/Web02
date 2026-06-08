package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.DailyCheckin;

import java.util.Date;
import java.util.List;

@Mapper
public interface DailyCheckinMapper {

    int insert(DailyCheckin checkin);

    DailyCheckin findByUserIdAndDate(@Param("userId") Long userId, @Param("checkinDate") Date checkinDate);

    int countByUserId(@Param("userId") Long userId);

    int countByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    List<DailyCheckin> findByUserId(@Param("userId") Long userId);

    int getContinuousDays(@Param("userId") Long userId);
}
