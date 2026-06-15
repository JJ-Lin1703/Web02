package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.UploadTask;

import java.util.List;

/**
 * 上传任务数据访问接口
 *
 * 提供上传任务表的 CRUD 操作，包括创建任务、更新状态、
 * 查询任务、查询待重试任务等功能
 */
@Mapper
public interface UploadTaskMapper {

    /**
     * 插入上传任务
     *
     * @param task 上传任务实体
     * @return 影响行数
     */
    int insert(UploadTask task);

    /**
     * 更新任务状态
     *
     * @param task 上传任务实体
     */
    void updateStatus(UploadTask task);

    /**
     * 根据任务 ID 查询上传任务
     *
     * @param taskId 任务 ID
     * @return 上传任务实体
     */
    UploadTask findByTaskId(@Param("taskId") String taskId);

    /**
     * 查询待重试的任务（状态为 FAILED 且重试次数未达上限）
     *
     * @param maxRetries 最大重试次数
     * @return 待重试任务列表
     */
    List<UploadTask> findPendingRetry(@Param("maxRetries") int maxRetries);

    /**
     * 根据用户 ID 查询上传任务
     *
     * @param userId 用户 ID
     * @return 上传任务列表
     */
    List<UploadTask> findByUserId(@Param("userId") Long userId);

    /**
     * 根据状态统计任务数量
     *
     * @param status 任务状态
     * @return 任务数量
     */
    int countByStatus(@Param("status") String status);
}