package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.ClockRecord;

import java.sql.Date;
import java.util.List;

/**
 * 打卡记录数据访问接口
 *
 * 提供打卡记录表的 CRUD 操作，包括记录打卡、查询打卡历史、
 * 统计完成率等功能
 */
@Mapper
public interface ClockRecordMapper {

    /**
     * 插入打卡记录
     *
     * @param record 打卡记录实体
     * @return 影响行数
     */
    int insert(ClockRecord record);

    /**
     * 更新打卡记录
     *
     * @param record 打卡记录实体
     * @return 影响行数
     */
    int update(ClockRecord record);

    /**
     * 根据记录 ID 查询打卡记录
     *
     * @param id 记录 ID
     * @return 打卡记录实体
     */
    ClockRecord findById(@Param("id") Long id);

    /**
     * 根据用户 ID、计划 ID 和日期查询打卡记录
     *
     * @param userId 用户 ID
     * @param planId 计划 ID
     * @param recordDate 记录日期
     * @return 打卡记录实体
     */
    ClockRecord findByUserPlanDate(@Param("userId") Long userId,
                                   @Param("planId") Long planId,
                                   @Param("recordDate") Date recordDate);

    /**
     * 根据用户 ID 查询所有打卡记录
     *
     * @param userId 用户 ID
     * @return 打卡记录列表
     */
    List<ClockRecord> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 和日期范围查询打卡记录
     *
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡记录列表
     */
    List<ClockRecord> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);

    /**
     * 根据计划 ID 查询打卡记录
     *
     * @param planId 计划 ID
     * @return 打卡记录列表
     */
    List<ClockRecord> findByPlanId(@Param("planId") Long planId);

    /**
     * 删除打卡记录（软删除）
     *
     * @param id 记录 ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户 ID 和日期范围统计打卡天数
     *
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡天数
     */
    int countByUserIdAndDateRange(@Param("userId") Long userId,
                                   @Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate);

    /**
     * 根据用户 ID 和日期范围统计已完成项数
     *
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 已完成项数
     */
    int sumCompletedItems(@Param("userId") Long userId,
                          @Param("startDate") Date startDate,
                          @Param("endDate") Date endDate);

    /**
     * 根据用户 ID 和日期范围统计总项数
     *
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总项数
     */
    int sumTotalItems(@Param("userId") Long userId,
                      @Param("startDate") Date startDate,
                      @Param("endDate") Date endDate);
}