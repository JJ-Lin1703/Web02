package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.DailyCheckin;

import java.sql.Date;
import java.util.List;

/**
 * 每日签到数据访问接口
 *
 * 提供每日签到表的 CRUD 操作，包括签到、查询签到状态、
 * 统计签到天数、分页查询等功能
 */
@Mapper
public interface DailyCheckinMapper {

    /**
     * 插入签到记录
     *
     * @param checkin 签记记录实体
     * @return 影响行数
     */
    int insert(DailyCheckin checkin);

    /**
     * 根据用户 ID 和签到日期查询签到记录
     *
     * @param userId 用户 ID
     * @param checkinDate 签到日期
     * @return 签记记录实体
     */
    DailyCheckin findByUserIdAndDate(@Param("userId") Long userId, @Param("checkinDate") Date checkinDate);

    /**
     * 根据用户 ID 统计签到总天数
     *
     * @param userId 用户 ID
     * @return 签到总天数
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 和日期范围统计签到天数
     *
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 签到天数
     */
    int countByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 根据用户 ID 查询所有签到记录
     *
     * @param userId 用户 ID
     * @return 签记记录列表
     */
    List<DailyCheckin> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 分页查询签到记录
     *
     * @param userId 用户 ID
     * @param offset 偏移量
     * @param limit 每页大小
     * @return 签记记录列表
     */
    List<DailyCheckin> findByUserIdPaginated(@Param("userId") Long userId,
                                              @Param("offset") long offset,
                                              @Param("limit") long limit);

    /**
     * 获取用户连续签到天数
     *
     * @param userId 用户 ID
     * @return 连续签到天数
     */
    int getContinuousDays(@Param("userId") Long userId);

    /**
     * 检查今日是否已签到
     *
     * @param userId 用户 ID
     * @param checkinDate 签到日期
     * @return 影响行数（0-未签到，>0-已签到）
     */
    int checkTodayCheckin(@Param("userId") Long userId, @Param("checkinDate") Date checkinDate);
}
