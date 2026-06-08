package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.AiPlan;

import java.util.List;

@Mapper
public interface AiPlanMapper {

    int insert(AiPlan plan);

    List<AiPlan> findByUserId(@Param("userId") Long userId);

    AiPlan findById(@Param("id") Long id);

    AiPlan findLatestByUserId(@Param("userId") Long userId);

    int deleteById(@Param("id") Long id);
}
