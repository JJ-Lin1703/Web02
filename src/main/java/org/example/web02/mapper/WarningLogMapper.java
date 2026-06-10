package org.example.web02.mapper;

import org.example.web02.entity.WarningLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WarningLogMapper {

    void insert(WarningLog warningLog);

    List<WarningLog> findByUserId(@Param("userId") Long userId);

    List<WarningLog> findUnreadByUserId(@Param("userId") Long userId);

    long countUnreadByUserId(@Param("userId") Long userId);

    void updateReadStatus(@Param("userId") Long userId, @Param("isRead") Integer isRead);

    void updateReadStatusById(@Param("id") Long id, @Param("isRead") Integer isRead);

    void deleteById(@Param("id") Long id);

    WarningLog findById(@Param("id") Long id);

    List<WarningLog> findByUserIdAndType(@Param("userId") Long userId, @Param("warningType") String warningType);

    int countTodayWarning(@Param("userId") Long userId, @Param("warningType") String warningType);
}