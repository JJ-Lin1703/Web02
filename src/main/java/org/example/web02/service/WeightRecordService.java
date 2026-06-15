package org.example.web02.service;

import org.example.web02.dto.response.PageResult;
import org.example.web02.entity.WeightRecord;

import java.math.BigDecimal;
import java.util.List;

/**
 * 体重记录服务接口
 * 提供体重记录的创建、查询、更新和删除功能
 */
public interface WeightRecordService {

    /**
     * 记录体重
     * 
     * @param userId 用户ID
     * @param weight 体重值（单位：kg）
     * @param remark 备注信息（可选）
     * @return 体重记录实体
     */
    WeightRecord recordWeight(Long userId, BigDecimal weight, String remark);

    /**
     * 获取用户体重历史记录
     * 
     * @param userId 用户ID
     * @return 体重记录列表（按时间倒序）
     */
    List<WeightRecord> getWeightHistory(Long userId);

    /**
     * 获取筛选后的体重历史记录
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param sortBy 排序字段（可选）
     * @return 体重记录列表
     */
    List<WeightRecord> getWeightHistoryFiltered(Long userId, String startDate, String endDate, String sortBy);

    /**
     * 分页获取体重历史记录
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param sortBy 排序字段（可选）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<WeightRecord> getWeightHistoryPaginated(Long userId, String startDate, String endDate, String sortBy, int pageNum, int pageSize);

    /**
     * 删除体重记录
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     */
    void deleteWeightRecord(Long userId, Long recordId);

    /**
     * 更新体重记录
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     * @param weight 新的体重值（单位：kg）
     */
    void updateWeight(Long userId, Long recordId, BigDecimal weight);

    /**
     * 获取用户最近30天的体重记录
     * 
     * @param userId 用户ID
     * @return 最近30天的体重记录列表
     */
    List<WeightRecord> getRecent30DaysWeight(Long userId);
}
