package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.AiPlan;

import java.util.List;

/**
 * AI 健康计划数据访问接口
 *
 * 提供 AI 健康计划表的 CRUD 操作，包括生成计划、查询历史、
 * 分页查询、更新计划内容等功能
 */
@Mapper
public interface AiPlanMapper {

    /**
     * 插入健康计划
     *
     * @param plan 健康计划实体
     * @return 影响行数
     */
    int insert(AiPlan plan);

    /**
     * 根据用户 ID 查询所有健康计划
     *
     * @param userId 用户 ID
     * @return 健康计划列表
     */
    List<AiPlan> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 分页查询健康计划
     *
     * @param userId 用户 ID
     * @param offset 偏移量
     * @param limit 每页大小
     * @return 健康计划列表
     */
    List<AiPlan> findByUserIdPaginated(@Param("userId") Long userId,
                                       @Param("offset") long offset,
                                       @Param("limit") long limit);

    /**
     * 根据用户 ID 统计健康计划总数
     *
     * @param userId 用户 ID
     * @return 计划总数
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * 根据计划 ID 查询健康计划
     *
     * @param id 计划 ID
     * @return 健康计划实体
     */
    AiPlan findById(@Param("id") Long id);

    /**
     * 查询用户最新的健康计划
     *
     * @param userId 用户 ID
     * @return 最新的健康计划实体
     */
    AiPlan findLatestByUserId(@Param("userId") Long userId);

    /**
     * 删除健康计划（软删除）
     *
     * @param id 计划 ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新健康计划内容
     *
     * @param plan 健康计划实体
     * @return 影响行数
     */
    int updateById(AiPlan plan);
}
