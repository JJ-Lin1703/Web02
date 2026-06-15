package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.WeightRecord;

import java.sql.Date;
import java.util.List;

/**
 * 体重记录数据访问接口
 *
 * 提供体重记录表的 CRUD 操作，包括记录体重、查询历史、
 * 分页查询、统计等功能
 */
@Mapper
public interface WeightRecordMapper {

    /**
     * 插入体重记录
     *
     * @param record 体重记录实体
     * @return 影响行数
     */
    int insert(WeightRecord record);

    /**
     * 更新体重记录
     *
     * @param record 体重记录实体
     * @return 影响行数
     */
    int update(WeightRecord record);

    /**
     * 根据用户 ID 查询所有体重记录
     *
     * @param userId 用户 ID
     * @return 体重记录列表
     */
    List<WeightRecord> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 和筛选条件查询体重记录
     *
     * @param userId 用户 ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param sortBy 排序字段（可选）
     * @return 体重记录列表
     */
    List<WeightRecord> findByUserIdWithFilter(@Param("userId") Long userId,
                                               @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate,
                                               @Param("sortBy") String sortBy);

    /**
     * 根据用户 ID 和筛选条件分页查询体重记录
     *
     * @param userId 用户 ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param sortBy 排序字段（可选）
     * @param offset 偏移量
     * @param limit 每页大小
     * @return 体重记录列表
     */
    List<WeightRecord> findByUserIdWithFilterPaginated(@Param("userId") Long userId,
                                                        @Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("sortBy") String sortBy,
                                                        @Param("offset") long offset,
                                                        @Param("limit") long limit);

    /**
     * 根据用户 ID 和筛选条件统计体重记录总数
     *
     * @param userId 用户 ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 记录总数
     */
    long countByUserIdWithFilter(@Param("userId") Long userId,
                                  @Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate);

    /**
     * 根据记录 ID 查询体重记录
     *
     * @param id 记录 ID
     * @return 体重记录实体
     */
    WeightRecord findById(@Param("id") Long id);

    /**
     * 根据用户 ID 和记录日期查询体重记录
     *
     * @param userId 用户 ID
     * @param recordDate 记录日期
     * @return 体重记录实体
     */
    WeightRecord findByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") Date recordDate);

    /**
     * 删除体重记录（软删除）
     *
     * @param id 记录 ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询最近 30 天的体重记录
     *
     * @param userId 用户 ID
     * @return 体重记录列表
     */
    List<WeightRecord> findRecent30DaysWeight(@Param("userId") Long userId);

    /**
     * 获取最近 7 天的体重值列表
     *
     * @param userId 用户 ID
     * @return 体重值列表
     */
    List<java.math.BigDecimal> getWeightsInLast7Days(@Param("userId") Long userId);

    /**
     * 检查今日是否已有体重记录
     *
     * @param userId 用户 ID
     * @param recordDate 记录日期
     * @return 影响行数（0-无记录，>0-有记录）
     */
    int checkTodayRecord(@Param("userId") Long userId, @Param("recordDate") Date recordDate);
}
