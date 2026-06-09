package org.example.web02.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.web02.entity.DictLabelOption;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.DictLabelOptionMapper;
import org.example.web02.service.DictLabelOptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DictLabelOptionServiceImpl implements DictLabelOptionService {

    private static final Map<String, String> TYPE_NAME_MAP = new LinkedHashMap<>();
    static {
        TYPE_NAME_MAP.put("health_target", "健康目标");
        TYPE_NAME_MAP.put("diet_hobby", "饮食偏好");
        TYPE_NAME_MAP.put("allergy", "过敏信息");
        TYPE_NAME_MAP.put("medical_history", "慢性病史");
        TYPE_NAME_MAP.put("activity_level", "活动水平");
    }

    private final DictLabelOptionMapper dictLabelOptionMapper;

    public DictLabelOptionServiceImpl(DictLabelOptionMapper dictLabelOptionMapper) {
        this.dictLabelOptionMapper = dictLabelOptionMapper;
    }

    @Override
    public List<String> getLabelNamesByType(String type) {
        return dictLabelOptionMapper.findByType(type).stream()
                .map(DictLabelOption::getLabelName)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getAllGrouped() {
        List<DictLabelOption> all = dictLabelOptionMapper.findAllActive();
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String type : TYPE_NAME_MAP.keySet()) {
            List<String> labels = all.stream()
                    .filter(o -> type.equals(o.getType()))
                    .map(DictLabelOption::getLabelName)
                    .collect(Collectors.toList());
            result.put(TYPE_NAME_MAP.get(type), labels);
        }
        return result;
    }

    @Override
    public Set<String> getAllValidLabelNames() {
        return dictLabelOptionMapper.findAllActive().stream()
                .map(DictLabelOption::getLabelName)
                .collect(Collectors.toSet());
    }

    // ====== 管理端 CRUD ======

    @Override
    public List<DictLabelOption> listAll() {
        return dictLabelOptionMapper.findAll();
    }

    @Override
    public DictLabelOption getById(Long id) {
        DictLabelOption opt = dictLabelOptionMapper.findById(id);
        if (opt == null) {
            throw new BusinessException("标签不存在");
        }
        return opt;
    }

    @Override
    @Transactional
    public DictLabelOption create(DictLabelOption option) {
        if (option.getType() == null || option.getType().isBlank()) {
            throw new BusinessException("标签分类不能为空");
        }
        if (option.getLabelName() == null || option.getLabelName().isBlank()) {
            throw new BusinessException("标签名称不能为空");
        }
        if (!TYPE_NAME_MAP.containsKey(option.getType())) {
            throw new BusinessException("非法标签分类: " + option.getType());
        }
        dictLabelOptionMapper.insert(option);
        log.info("标签字典新增成功: id={}, type={}, label={}", option.getId(), option.getType(), option.getLabelName());
        return option;
    }

    @Override
    @Transactional
    public DictLabelOption update(Long id, DictLabelOption option) {
        DictLabelOption existing = getById(id);
        if (option.getLabelName() != null && !option.getLabelName().isBlank()) {
            existing.setLabelName(option.getLabelName());
        }
        if (option.getSort() != null) {
            existing.setSort(option.getSort());
        }
        dictLabelOptionMapper.update(existing);
        log.info("标签字典更新成功: id={}", id);
        return getById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getById(id); // 确保存在
        dictLabelOptionMapper.deleteById(id);
        log.info("标签字典删除成功: id={}", id);
    }
}