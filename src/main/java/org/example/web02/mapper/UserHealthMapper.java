package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.UserHealth;

/**
 * 用户健康档案数据访问接口
 *
 * 提供用户健康档案表的 CRUD 操作，包括创建档案、查询档案、
 * 更新档案、检查档案是否存在等功能
 */
@Mapper
public interface UserHealthMapper {

    /**
     * 根据档案 ID 查询健康档案
     *
     * @param id 档案 ID
     * @return 健康档案实体
     */
    UserHealth findById(Long id);

    /**
     * 根据用户 ID 查询健康档案
     *
     * @param userId 用户 ID
     * @return 健康档案实体
     */
    UserHealth findByUserId(Long userId);

    /**
     * 插入健康档案
     *
     * @param userHealth 健康档案实体
     * @return 影响行数
     */
    int insert(UserHealth userHealth);

    /**
     * 更新健康档案
     *
     * @param userHealth 健康档案实体
     * @return 影响行数
     */
    int update(UserHealth userHealth);

    /**
     * 删除健康档案（软删除）
     *
     * @param id 档案 ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 检查用户是否已有健康档案
     *
     * @param userId 用户 ID
     * @return 影响行数（0-不存在，>0-存在）
     */
    int existsByUserId(Long userId);
}
