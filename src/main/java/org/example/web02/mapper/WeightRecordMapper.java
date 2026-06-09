package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.WeightRecord;

import java.sql.Date;
import java.util.List;

@Mapper
public interface WeightRecordMapper {

    int insert(WeightRecord record);

    int update(WeightRecord record);

    List<WeightRecord> findByUserId(@Param("userId") Long userId);

    List<WeightRecord> findByUserIdWithFilter(@Param("userId") Long userId,
                                               @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate,
                                               @Param("sortBy") String sortBy);

    WeightRecord findById(@Param("id") Long id);

    WeightRecord findByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") Date recordDate);

    int deleteById(@Param("id") Long id);

    List<WeightRecord> findRecent30DaysWeight(@Param("userId") Long userId);
}
