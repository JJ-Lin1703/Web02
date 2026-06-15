package org.example.web02.service.impl;

import org.example.web02.dto.request.HealthRecordRequest;
import org.example.web02.dto.response.HealthRecordResponse;
import org.example.web02.entity.UserHealth;
import org.example.web02.entity.WeightRecord;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.UserHealthMapper;
import org.example.web02.mapper.WeightRecordMapper;
import org.example.web02.service.UserHealthService;
import org.example.web02.service.WarningLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.Calendar;

/**
 * 用户健康档案服务实现类
 * 负责用户健康档案的创建、更新、查询，以及健康指标（BMR、TDEE、BMI）的计算
 */
@Service
public class UserHealthServiceImpl implements UserHealthService {

    /** 用户健康档案数据访问层 */
    private final UserHealthMapper userHealthMapper;
    /** 警告日志服务（用于BMI异常预警） */
    private final WarningLogService warningLogService;
    /** 体重记录数据访问层 */
    private final WeightRecordMapper weightRecordMapper;

    /**
     * 构造函数注入依赖
     * @param userHealthMapper 用户健康档案Mapper
     * @param warningLogService 警告日志服务
     * @param weightRecordMapper 体重记录Mapper
     */
    public UserHealthServiceImpl(UserHealthMapper userHealthMapper,
                                  WarningLogService warningLogService,
                                  WeightRecordMapper weightRecordMapper) {
        this.userHealthMapper = userHealthMapper;
        this.warningLogService = warningLogService;
        this.weightRecordMapper = weightRecordMapper;
    }

    /**
     * 创建用户健康档案
     * 流程：参数校验 → 检查是否已存在 → 计算健康指标 → 保存 → 检查BMI预警 → 同步体重记录
     * 
     * @param userId 用户ID
     * @param request 健康档案请求DTO
     * @return 创建的健康档案响应
     * @throws BusinessException 参数校验失败或档案已存在时抛出
     */
    @Override
    @Transactional
    public HealthRecordResponse createHealthRecord(Long userId, HealthRecordRequest request) {
        validateRequest(request);

        if (hasHealthRecord(userId)) {
            throw new BusinessException("健康档案已存在");
        }

        UserHealth userHealth = new UserHealth();
        userHealth.setUserId(userId);
        userHealth.setAge(request.getAge());
        userHealth.setGender(request.getGender());
        userHealth.setHeight(request.getHeight());
        userHealth.setWeight(request.getWeight());
        userHealth.setActivityLevel(request.getActivityLevel());
        userHealth.setDietHobby(request.getDietHobby());
        userHealth.setHealthTarget(request.getHealthTarget());
        userHealth.setAllergy(request.getAllergy());
        userHealth.setMedicalHistory(request.getMedicalHistory());

        BigDecimal[] metrics = calculateMetrics(userHealth);
        userHealth.setBmr(metrics[0]);
        userHealth.setTdee(metrics[1]);
        userHealth.setBmi(metrics[2]);

        userHealth.setCreateTime(new java.util.Date());
        userHealth.setUpdateTime(new java.util.Date());
        userHealth.setIsDeleted(0);

        userHealthMapper.insert(userHealth);

        warningLogService.checkBmiAbnormal(userId, userHealth.getBmi().doubleValue());

        // 创建健康档案时，同步创建一条体重记录
        syncWeightRecord(userId, request.getWeight(), "通过健康档案创建");

        return buildResponse(userHealth);
    }

    /**
     * 更新用户健康档案
     * 流程：参数校验 → 检查档案是否存在 → 更新数据 → 计算健康指标 → 检查BMI预警 → 同步体重记录
     * 
     * @param userId 用户ID
     * @param request 健康档案请求DTO
     * @return 更新后的健康档案响应
     * @throws BusinessException 参数校验失败或档案不存在时抛出
     */
    @Override
    @Transactional
    public HealthRecordResponse updateHealthRecord(Long userId, HealthRecordRequest request) {
        validateRequest(request);

        UserHealth userHealth = userHealthMapper.findByUserId(userId);
        if (userHealth == null) {
            throw new BusinessException("健康档案不存在");
        }

        BigDecimal oldWeight = userHealth.getWeight();
        BigDecimal newWeight = request.getWeight();

        userHealth.setAge(request.getAge());
        userHealth.setGender(request.getGender());
        userHealth.setHeight(request.getHeight());
        userHealth.setWeight(newWeight);
        userHealth.setActivityLevel(request.getActivityLevel());
        userHealth.setDietHobby(request.getDietHobby());
        userHealth.setHealthTarget(request.getHealthTarget());
        userHealth.setAllergy(request.getAllergy());
        userHealth.setMedicalHistory(request.getMedicalHistory());

        BigDecimal[] metrics = calculateMetrics(userHealth);
        userHealth.setBmr(metrics[0]);
        userHealth.setTdee(metrics[1]);
        userHealth.setBmi(metrics[2]);

        userHealth.setUpdateTime(new java.util.Date());

        userHealthMapper.update(userHealth);

        warningLogService.checkBmiAbnormal(userId, userHealth.getBmi().doubleValue());

        // 如果体重发生变化，同步更新体重记录
        if (newWeight.compareTo(oldWeight) != 0) {
            syncWeightRecord(userId, newWeight, "通过健康档案修改");
        }

        return buildResponse(userHealth);
    }

    /**
     * 同步体重记录：如果当天已有记录则更新，否则创建新记录
     * 
     * @param userId 用户ID
     * @param weight 体重值
     * @param remark 备注信息
     */
    private void syncWeightRecord(Long userId, BigDecimal weight, String remark) {
        Date today = Date.valueOf(java.time.LocalDate.now());

        WeightRecord existingRecord = weightRecordMapper.findByUserIdAndDate(userId, today);
        if (existingRecord != null) {
            // 更新已有记录
            existingRecord.setWeight(weight);
            existingRecord.setRemark(remark);
            weightRecordMapper.update(existingRecord);
        } else {
            // 创建新记录
            WeightRecord newRecord = new WeightRecord();
            newRecord.setUserId(userId);
            newRecord.setWeight(weight);
            newRecord.setRecordDate(today);
            newRecord.setRemark(remark);
            newRecord.setCreateTime(new java.util.Date());
            newRecord.setUpdateTime(new java.util.Date());
            newRecord.setIsDeleted(0);
            weightRecordMapper.insert(newRecord);
        }
    }

    /**
     * 获取用户健康档案
     * 
     * @param userId 用户ID
     * @return 健康档案响应
     * @throws BusinessException 健康档案不存在时抛出
     */
    @Override
    public HealthRecordResponse getHealthRecord(Long userId) {
        UserHealth userHealth = userHealthMapper.findByUserId(userId);
        if (userHealth == null) {
            throw new BusinessException(404, "健康档案不存在");
        }
        return buildResponse(userHealth);
    }

    /**
     * 检查用户是否已有健康档案
     * 
     * @param userId 用户ID
     * @return true表示已存在，false表示不存在
     */
    @Override
    public boolean hasHealthRecord(Long userId) {
        return userHealthMapper.existsByUserId(userId) > 0;
    }

    /**
     * 校验健康档案请求参数
     * 校验规则：
     * - 年龄：12-80岁
     * - 性别：0（男）或1（女）
     * - 身高：100-250cm
     * - 体重：30-300kg
     * - 活动水平：1（低）、2（中）、3（高）
     * - 饮食偏好和健康目标不能为空
     * 
     * @param request 健康档案请求DTO
     * @throws BusinessException 参数校验失败时抛出
     */
    private void validateRequest(HealthRecordRequest request) {
        if (request.getAge() == null || request.getAge() < 12 || request.getAge() > 80) {
            throw new BusinessException("年龄必须在12-80岁之间");
        }

        if (request.getGender() == null || (request.getGender() != 0 && request.getGender() != 1)) {
            throw new BusinessException("性别必须为0（男）或1（女）");
        }

        if (request.getHeight() == null || request.getHeight().compareTo(BigDecimal.valueOf(100)) < 0 ||
            request.getHeight().compareTo(BigDecimal.valueOf(250)) > 0) {
            throw new BusinessException("身高必须在100-250cm之间");
        }

        if (request.getWeight() == null || request.getWeight().compareTo(BigDecimal.valueOf(30)) < 0 ||
            request.getWeight().compareTo(BigDecimal.valueOf(300)) > 0) {
            throw new BusinessException("体重必须在30-300kg之间");
        }

        if (request.getActivityLevel() == null || request.getActivityLevel() < 1 || request.getActivityLevel() > 3) {
            throw new BusinessException("活动水平必须为1（低）、2（中）或3（高）");
        }

        if (request.getDietHobby() == null || request.getDietHobby().trim().isEmpty()) {
            throw new BusinessException("饮食偏好不能为空");
        }

        if (request.getHealthTarget() == null || request.getHealthTarget().trim().isEmpty()) {
            throw new BusinessException("健康目标不能为空");
        }
    }

    /**
     * 计算健康指标（BMR、TDEE、BMI）
     * BMR（基础代谢率）使用Mifflin-St Jeor公式计算
     * TDEE（每日总能量消耗）= BMR × 活动系数
     * BMI（身体质量指数）= 体重(kg) / 身高(m)²
     * 
     * @param userHealth 用户健康档案
     * @return 包含BMR、TDEE、BMI的数组
     */
    private BigDecimal[] calculateMetrics(UserHealth userHealth) {
        BigDecimal height = userHealth.getHeight();
        BigDecimal weight = userHealth.getWeight();
        int age = userHealth.getAge();
        int gender = userHealth.getGender();
        int activityLevel = userHealth.getActivityLevel();

        BigDecimal bmi = weight.divide(height.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP).pow(2), 2, RoundingMode.HALF_UP);

        BigDecimal bmr;
        if (gender == 0) {
            bmr = BigDecimal.valueOf(10).multiply(weight)
                    .add(BigDecimal.valueOf(6.25).multiply(height))
                    .subtract(BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(age)))
                    .subtract(BigDecimal.valueOf(161));
        } else {
            bmr = BigDecimal.valueOf(10).multiply(weight)
                    .add(BigDecimal.valueOf(6.25).multiply(height))
                    .subtract(BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(age)))
                    .add(BigDecimal.valueOf(5));
        }
        bmr = bmr.setScale(2, RoundingMode.HALF_UP);

        BigDecimal activityFactor;
        switch (activityLevel) {
            case 1 -> activityFactor = BigDecimal.valueOf(1.2);
            case 2 -> activityFactor = BigDecimal.valueOf(1.375);
            case 3 -> activityFactor = BigDecimal.valueOf(1.55);
            default -> activityFactor = BigDecimal.valueOf(1.2);
        }

        BigDecimal tdee = bmr.multiply(activityFactor).setScale(2, RoundingMode.HALF_UP);

        return new BigDecimal[]{bmr, tdee, bmi};
    }

    /**
     * 构建健康档案响应对象
     * 
     * @param userHealth 用户健康档案实体
     * @return 健康档案响应DTO
     */
    private HealthRecordResponse buildResponse(UserHealth userHealth) {
        return HealthRecordResponse.builder()
                .id(userHealth.getId())
                .age(userHealth.getAge())
                .gender(userHealth.getGender())
                .height(userHealth.getHeight())
                .weight(userHealth.getWeight())
                .activityLevel(userHealth.getActivityLevel())
                .dietHobby(userHealth.getDietHobby())
                .healthTarget(userHealth.getHealthTarget())
                .allergy(userHealth.getAllergy())
                .medicalHistory(userHealth.getMedicalHistory())
                .bmr(userHealth.getBmr())
                .tdee(userHealth.getTdee())
                .bmi(userHealth.getBmi())
                .createTime(userHealth.getCreateTime())
                .updateTime(userHealth.getUpdateTime())
                .build();
    }
}
