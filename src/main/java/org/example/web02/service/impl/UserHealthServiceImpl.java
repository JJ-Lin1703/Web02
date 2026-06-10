package org.example.web02.service.impl;

import org.example.web02.dto.request.HealthRecordRequest;
import org.example.web02.dto.response.HealthRecordResponse;
import org.example.web02.entity.UserHealth;
import org.example.web02.exception.BusinessException;
import org.example.web02.mapper.UserHealthMapper;
import org.example.web02.service.UserHealthService;
import org.example.web02.service.WarningLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Service
public class UserHealthServiceImpl implements UserHealthService {

    private final UserHealthMapper userHealthMapper;
    private final WarningLogService warningLogService;

    public UserHealthServiceImpl(UserHealthMapper userHealthMapper, WarningLogService warningLogService) {
        this.userHealthMapper = userHealthMapper;
        this.warningLogService = warningLogService;
    }

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

        userHealth.setCreateTime(new Date());
        userHealth.setUpdateTime(new Date());
        userHealth.setIsDeleted(0);

        userHealthMapper.insert(userHealth);

        warningLogService.checkBmiAbnormal(userId, userHealth.getBmi().doubleValue());

        return buildResponse(userHealth);
    }

    @Override
    @Transactional
    public HealthRecordResponse updateHealthRecord(Long userId, HealthRecordRequest request) {
        validateRequest(request);

        UserHealth userHealth = userHealthMapper.findByUserId(userId);
        if (userHealth == null) {
            throw new BusinessException("健康档案不存在");
        }

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

        userHealth.setUpdateTime(new Date());

        userHealthMapper.update(userHealth);

        warningLogService.checkBmiAbnormal(userId, userHealth.getBmi().doubleValue());

        return buildResponse(userHealth);
    }

    @Override
    public HealthRecordResponse getHealthRecord(Long userId) {
        UserHealth userHealth = userHealthMapper.findByUserId(userId);
        if (userHealth == null) {
            throw new BusinessException(404, "健康档案不存在");
        }
        return buildResponse(userHealth);
    }

    @Override
    public boolean hasHealthRecord(Long userId) {
        return userHealthMapper.existsByUserId(userId) > 0;
    }

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
