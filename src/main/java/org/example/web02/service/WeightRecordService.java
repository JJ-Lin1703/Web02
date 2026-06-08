package org.example.web02.service;

import org.example.web02.entity.WeightRecord;

import java.math.BigDecimal;
import java.util.List;

public interface WeightRecordService {

    WeightRecord recordWeight(Long userId, BigDecimal weight, String remark);

    List<WeightRecord> getWeightHistory(Long userId);

    List<WeightRecord> getWeightHistoryFiltered(Long userId, String startDate, String endDate, String sortBy);

    void deleteWeightRecord(Long userId, Long recordId);

    void updateWeight(Long userId, Long recordId, BigDecimal weight);

    List<WeightRecord> getRecent30DaysWeight(Long userId);
}
