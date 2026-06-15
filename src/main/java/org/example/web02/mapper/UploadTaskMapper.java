package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.UploadTask;

import java.util.List;

@Mapper
public interface UploadTaskMapper {

    int insert(UploadTask task);

    void updateStatus(UploadTask task);

    UploadTask findByTaskId(@Param("taskId") String taskId);

    List<UploadTask> findPendingRetry(@Param("maxRetries") int maxRetries);

    List<UploadTask> findByUserId(@Param("userId") Long userId);

    int countByStatus(@Param("status") String status);
}