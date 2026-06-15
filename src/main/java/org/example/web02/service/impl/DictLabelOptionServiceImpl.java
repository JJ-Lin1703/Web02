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
/**
 * 标签选项服务实现类
 * 负责健康档案标签字典的管理，包括健康目标、饮食偏好、过敏信息、慢性病史、活动水平等分类
 */
public class DictLabelOptionServiceImpl implements DictLabelOptionService {

    /** 标签类型名称映射表 */
    private static final Map<String, String> TYPE_NAME_MAP = new LinkedHashMap<>();
    static {
        TYPE_NAME_MAP.put("health_target", "健康目标");
        TYPE_NAME_MAP.put("diet_hobby", "饮食偏好");
        TYPE_NAME_MAP.put("allergy", "过敏信息");
        TYPE_NAME_MAP.put("medical_history", "慢性病史");
        TYPE_NAME_MAP.put("activity_level", "活动水平");
    }

    /** 标签选项数据访问层 */
    private final DictLabelOptionMapper dictLabelOptionMapper;

    /**
     * 构造函数注入依赖
     * @param dictLabelOptionMapper 标签选项Mapper
     */
    public DictLabelOptionServiceImpl(DictLabelOptionMapper dictLabelOptionMapper) {
        this.dictLabelOptionMapper = dictLabelOptionMapper;
    }

    /**
     * 根据类型获取标签名称列表
     * 
     * @param type 标签类型
     * @return 标签名称列表
     */
    @Override
    public List<String> getLabelNamesByType(String type) {
        return dictLabelOptionMapper.findByType(type).stream()
                .map(DictLabelOption::getLabelName)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有标签选项，按类型分组
     * 
     * @return 分组后的标签Map（类型中文名 -> 标签名称列表）
     */
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

    /**
     * 获取所有有效的标签名称集合
     * 
     * @return 标签名称集合
     */
    @Override
    public Set<String> getAllValidLabelNames() {
        return dictLabelOptionMapper.findAllActive().stream()
                .map(DictLabelOption::getLabelName)
                .collect(Collectors.toSet());
    }

    // ====== 管理端 CRUD ======

    /**
     * 获取所有标签选项（包含禁用状态）
     * 
     * @return 标签选项列表
     */
    @Override
    public List<DictLabelOption> listAll() {
        return dictLabelOptionMapper.findAll();
    }

    /**
     * 根据ID获取标签选项
     * 
     * @param id 标签ID
     * @return 标签选项实体
     * @throws BusinessException 标签不存在时抛出
     */
    @Override
    public DictLabelOption getById(Long id) {
        DictLabelOption opt = dictLabelOptionMapper.findById(id);
        if (opt == null) {
            throw new BusinessException("标签不存在");
        }
        return opt;
    }

    /**
     * 创建标签选项
     * 
     * @param option 标签选项实体
     * @return 创建后的标签选项
     * @throws BusinessException 分类为空、名称为空或分类非法时抛出
     */
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

    /**
     * 更新标签选项
     * 
     * @param id 标签ID
     * @param option 更新内容
     * @return 更新后的标签选项
     * @throws BusinessException 标签不存在时抛出
     */
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

    /**
     * 删除标签选项
     * 
     * @param id 标签ID
     * @throws BusinessException 标签不存在时抛出
     */
    @Override
    @Transactional
    public void delete(Long id) {
        getById(id); // 确保存在
        dictLabelOptionMapper.deleteById(id);
        log.info("标签字典删除成功: id={}", id);
    }
}