package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.ClockRecord;

import java.sql.Date;
import java.util.List;

@Mapper
public interface ClockRecordMapper {

    int insert(ClockRecord record);

    int update(ClockRecord record);

    ClockRecord findById(@Param("id") Long id);

    ClockRecord findByUserPlanDate(@Param("userId") Long userId, 
                                   @Param("planId") Long planId, 
                                   @Param("recordDate") Date recordDate);

    List<ClockRecord> findByUserId(@Param("userId") Long userId);

    List<ClockRecord> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                               @Param("startDate") Date startDate, 
                                               @Param("endDate") Date endDate);

    List<ClockRecord> findByPlanId(@Param("planId") Long planId);

    int deleteById(@Param("id") Long id);

    int countByUserIdAndDateRange(@Param("userId") Long userId, 
                                   @Param("startDate") Date startDate, 
                                   @Param("endDate") Date endDate);

    int sumCompletedItems(@Param("userId") Long userId, 
                          @Param("startDate") Date startDate, 
                          @Param("endDate") Date endDate);

    int sumTotalItems(@Param("userId") Long userId, 
                      @Param("startDate") Date startDate, 
                      @Param("endDate") Date endDate);
}