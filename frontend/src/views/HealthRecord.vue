<template>
  <div class="page-container">
    <div class="form-wrapper">
      <div class="form-header">
        <div class="header-icon">
          <el-icon :size="40" color="#fff"><FirstAidKit /></el-icon>
        </div>
        <div class="header-text">
          <h2>{{ isEditMode ? '修改健康档案' : '填写健康档案' }}</h2>
          <p>请填写您的个人健康信息，以便为您提供个性化服务</p>
        </div>
      </div>

      <el-card class="form-card">
        <el-form :model="formData" :rules="rules" ref="formRef" label-width="0">
          <div class="form-section">
            <div class="section-title">
              <el-icon :size="20" color="#5b9bd5"><User /></el-icon>
              <span>基本信息</span>
            </div>
            
            <el-row :gutter="24">
              <el-col :span="12">
                <div class="form-field">
                  <label class="field-label">
                    <el-icon :size="16" color="#909399"><User /></el-icon>
                    年龄
                  </label>
                  <el-input v-model.number="formData.age" type="number" placeholder="请输入年龄（12-80岁）" class="field-input" />
                </div>
              </el-col>
              <el-col :span="12">
                <div class="form-field">
                  <label class="field-label">
                    <el-icon :size="16" color="#909399"><User /></el-icon>
                    性别
                  </label>
                  <el-select v-model="formData.gender" placeholder="请选择性别" class="field-input">
                    <el-option label="男" :value="0" />
                    <el-option label="女" :value="1" />
                  </el-select>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="form-section">
            <div class="section-title">
              <el-icon :size="20" color="#67c23a"><ScaleToOriginal /></el-icon>
              <span>身体数据</span>
            </div>
            
            <el-row :gutter="24">
              <el-col :span="12">
                <div class="form-field">
                  <label class="field-label">
                    <el-icon :size="16" color="#909399"><Minus /></el-icon>
                    身高
                    <span class="field-unit">cm</span>
                  </label>
                  <el-input v-model.number="formData.height" type="number" step="0.1" placeholder="请输入身高（100-250cm）" class="field-input" />
                </div>
              </el-col>
              <el-col :span="12">
                <div class="form-field">
                  <label class="field-label">
                    <el-icon :size="16" color="#909399"><ScaleToOriginal /></el-icon>
                    体重
                    <span class="field-unit">kg</span>
                  </label>
                  <el-input v-model.number="formData.weight" type="number" step="0.1" placeholder="请输入体重（30-300kg）" class="field-input" />
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="form-section">
            <div class="section-title">
              <el-icon :size="20" color="#e6a23c"><TrendCharts /></el-icon>
              <span>生活方式</span>
            </div>
            
            <el-row :gutter="24">
              <el-col :span="12">
                <div class="form-field">
                  <label class="field-label">
                    <el-icon :size="16" color="#909399"><TrendCharts /></el-icon>
                    日常活动水平
                  </label>
                  <el-select v-model="formData.activityLevel" placeholder="请选择活动水平" class="field-input">
                    <el-option label="低（久坐少动）" :value="1" />
                    <el-option label="中（轻度运动）" :value="2" />
                    <el-option label="高（经常锻炼）" :value="3" />
                  </el-select>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="form-field">
                  <label class="field-label">
                    <el-icon :size="16" color="#909399"><Food /></el-icon>
                    饮食偏好
                  </label>
                  <el-select v-model="formData.dietHobby" placeholder="请选择饮食偏好" class="field-input">
                    <el-option label="素食" value="素食" />
                    <el-option label="均衡" value="均衡" />
                    <el-option label="高蛋白" value="高蛋白" />
                    <el-option label="低碳水" value="低碳水" />
                    <el-option label="控糖" value="控糖" />
                  </el-select>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="form-section">
            <div class="section-title">
              <el-icon :size="20" color="#722ed1"><Aim /></el-icon>
              <span>健康目标</span>
            </div>
            
            <el-row :gutter="24">
              <el-col :span="24">
                <div class="form-field">
                  <label class="field-label">
                    <el-icon :size="16" color="#909399"><Aim /></el-icon>
                    您的健康目标
                  </label>
                  <div class="goal-options">
                    <div v-for="goal in healthGoals" :key="goal.value" :class="['goal-option', { active: formData.healthTarget === goal.value }]" @click="formData.healthTarget = goal.value">
                      <div class="goal-icon" :style="{ background: goal.bgColor }">
                        <el-icon v-if="goal.icon === 'down'" :size="24" color="#fff"><ArrowDown /></el-icon>
                        <el-icon v-else-if="goal.icon === 'up'" :size="24" color="#fff"><ArrowUp /></el-icon>
                        <el-icon v-else :size="24" color="#fff"><Minus /></el-icon>
                      </div>
                      <div class="goal-name">{{ goal.label }}</div>
                      <div class="goal-desc">{{ goal.desc }}</div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="form-section">
            <div class="section-title">
              <el-icon :size="20" color="#f56c6c"><InfoFilled /></el-icon>
              <span>健康状况</span>
            </div>
            
            <el-row :gutter="24">
              <el-col :span="24">
                <div class="form-field">
                  <label class="field-label">
                    <el-icon :size="16" color="#909399"><Warning /></el-icon>
                    食物过敏
                    <span class="field-hint">（多选，如无过敏可不选）</span>
                  </label>
                  <el-select v-model="formData.allergy" multiple placeholder="请选择食物过敏" class="field-input field-input-multiple">
                    <el-option v-for="option in allergyOptions" :key="option.value" :label="option.label" :value="option.value" />
                  </el-select>
                </div>
              </el-col>
            </el-row>
            
            <el-row :gutter="24">
              <el-col :span="24">
                <div class="form-field">
                  <label class="field-label">
                    <el-icon :size="16" color="#909399"><Document /></el-icon>
                    慢性病史
                  </label>
                  <el-input v-model="formData.medicalHistory" type="textarea" placeholder="请输入慢性病史（如：高血压、糖尿病等），无则留空" :rows="3" class="field-input field-input-textarea" />
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="form-actions">
            <el-button type="primary" @click="handleSubmit" :loading="loading" size="large">
              <el-icon :size="18"><Check v-if="isEditMode" /><Promotion v-else /></el-icon>
              {{ isEditMode ? '保存修改' : '提交档案' }}
            </el-button>
            <el-button v-if="isEditMode" @click="handleReset" size="large">
              <el-icon :size="18"><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, ScaleToOriginal, Minus, TrendCharts, Warning, Document, Refresh, ArrowDown, ArrowUp, FirstAidKit, Food, Aim, InfoFilled, Check, Promotion } from '@element-plus/icons-vue'
import { getHealthRecord, createHealthRecord, updateHealthRecord, getDictLabelOptions } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const isEditMode = ref(false)
const originalData = ref(null)

// 动态下拉选项
const healthTargetOptions = ref([])
const dietHobbyOptions = ref([])
const allergyOptions = ref([])
const activityLevelOptions = ref([])

const formData = reactive({
  age: null,
  gender: null,
  height: null,
  weight: null,
  activityLevel: null,
  dietHobby: '',
  healthTarget: '',
  allergy: [],
  medicalHistory: ''
})

// 加载标签字典，填充所有下拉选项
const loadDictOptions = async () => {
  try {
    const res = await getDictLabelOptions()
    const pool = res.data  // { 健康目标: [...], 饮食偏好: [...], ... }
    healthTargetOptions.value = pool['健康目标'] || []
    dietHobbyOptions.value = pool['饮食偏好'] || []
    allergyOptions.value = (pool['过敏信息'] || []).map(t => ({ label: t, value: t }))

    // 活动水平：服务端约定 sort=value，label_name=显示文本
    // 前端无法拿到 sort，改由后端通过专业字段告知，这里直接单独获取
    const actOpts = (pool['活动水平'] || []).map((label, idx) => ({
      label,
      value: idx + 1  // 按 sort 升序返回，idx+1 即 sort
    }))
    activityLevelOptions.value = actOpts
  } catch {
    ElMessage.error('加载标签字典失败')
  }
}

const healthGoals = [
  { value: '减肥', label: '减肥', desc: '减少体重，塑造身材', icon: 'down', bgColor: 'linear-gradient(135deg, #f56c6c 0%, #f89898 100%)' },
  { value: '增肌', label: '增肌', desc: '增加肌肉，增强体质', icon: 'up', bgColor: 'linear-gradient(135deg, #67c23a 0%, #85ce61 100%)' },
  { value: '维持健康', label: '维持健康', desc: '保持现有健康状态', icon: 'minus', bgColor: 'linear-gradient(135deg, #409eff 0%, #66b1ff 100%)' }
]

const rules = {
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 12, max: 80, message: '年龄必须在12-80岁之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  height: [
    { required: true, message: '请输入身高', trigger: 'blur' },
    { type: 'number', min: 100, max: 250, message: '身高必须在100-250cm之间', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' },
    { type: 'number', min: 30, max: 300, message: '体重必须在30-300kg之间', trigger: 'blur' }
  ],
  activityLevel: [
    { required: true, message: '请选择活动水平', trigger: 'change' }
  ],
  dietHobby: [
    { required: true, message: '请选择饮食偏好', trigger: 'change' }
  ],
  healthTarget: [
    { required: true, message: '请选择健康目标', trigger: 'change' }
  ]
}

const calculateBmi = () => {
  if (!formData.height || !formData.weight) return 0
  return formData.weight / Math.pow(formData.height / 100, 2)
}

const getBmiClass = (bmi) => {
  if (bmi < 18.5) return 'bmi-underweight'
  if (bmi < 24) return 'bmi-normal'
  if (bmi < 28) return 'bmi-overweight'
  return 'bmi-obese'
}

const getBmiDescription = (bmi) => {
  if (bmi < 18.5) return '偏瘦，建议增加营养摄入'
  if (bmi < 24) return '正常，继续保持'
  if (bmi < 28) return '偏胖，建议控制饮食'
  return '肥胖，建议就医咨询'
}

const loadHealthRecord = async () => {
  try {
    const res = await getHealthRecord()
    if (res.data) {
      isEditMode.value = true
      originalData.value = { ...res.data }
      formData.age = res.data.age
      formData.gender = res.data.gender
      formData.height = parseFloat(res.data.height)
      formData.weight = parseFloat(res.data.weight)
      formData.activityLevel = res.data.activityLevel
      formData.dietHobby = res.data.dietHobby
      formData.healthTarget = res.data.healthTarget
      formData.allergy = res.data.allergy ? res.data.allergy.split(',') : []
      formData.medicalHistory = res.data.medicalHistory || ''
    }
  } catch (error) {
    if (error.response?.data?.code === 404) {
      isEditMode.value = false
    } else {
      console.error('加载健康档案失败', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    const requestData = {
      age: formData.age,
      gender: formData.gender,
      height: formData.height,
      weight: formData.weight,
      activityLevel: formData.activityLevel,
      dietHobby: formData.dietHobby,
      healthTarget: formData.healthTarget,
      allergy: formData.allergy.length > 0 ? formData.allergy.join(',') : null,
      medicalHistory: formData.medicalHistory || null
    }
    if (isEditMode.value) {
      await updateHealthRecord(requestData)
      ElMessage.success('健康档案更新成功')
    } else {
      await createHealthRecord(requestData)
      ElMessage.success('健康档案创建成功')
      setTimeout(() => {
        router.push('/')
      }, 1500)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  if (originalData.value) {
    formData.age = originalData.value.age
    formData.gender = originalData.value.gender
    formData.height = parseFloat(originalData.value.height)
    formData.weight = parseFloat(originalData.value.weight)
    formData.activityLevel = originalData.value.activityLevel
    formData.dietHobby = originalData.value.dietHobby
    formData.healthTarget = originalData.value.healthTarget
    formData.allergy = originalData.value.allergy ? originalData.value.allergy.split(',') : []
    formData.medicalHistory = originalData.value.medicalHistory || ''
  }
}

onMounted(() => {
  loadDictOptions()
  loadHealthRecord()
})
</script>

<style scoped>
.page-container { padding: 0; min-height: 100%; }
.form-wrapper { max-width: 800px; margin: 0 auto; }
.form-header { display: flex; align-items: center; gap: 20px; padding: 32px; background: linear-gradient(135deg, #409eff 0%, #67c23a 100%); border-radius: 24px; margin-bottom: 24px; box-shadow: 0 8px 32px rgba(64, 158, 255, 0.3); }
.header-icon { width: 72px; height: 72px; background: rgba(255, 255, 255, 0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.header-text h2 { font-size: 31px; font-weight: 700; color: #ffffff; margin: 0 0 8px 0; }
.header-text p { font-size: 19px; color: rgba(255, 255, 255, 0.9); margin: 0; }
.form-card { border: none; border-radius: 24px; box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06); overflow: hidden; }
.form-card :deep(.el-card__body) { padding: 32px; }
.form-section { margin-bottom: 32px; }
.form-section:last-of-type { margin-bottom: 0; }
.section-title { display: flex; align-items: center; gap: 10px; font-size: 21px; font-weight: 700; color: #333; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 2px solid #f0ece6; }
.form-field { display: flex; flex-direction: column; gap: 10px; }
.field-label { font-size: 19px; font-weight: 600; color: #4a4a4a; display: flex; align-items: center; gap: 8px; }
.field-unit { font-size: 17px; font-weight: 500; color: #909399; margin-left: 4px; }
.field-hint { font-size: 16px; font-weight: 400; color: #909399; margin-left: 8px; }
.field-input { font-size: 19px; padding: 16px 20px; border-radius: 14px; border: 2px solid #e8e8e8; transition: all 0.3s ease; background: #ffffff; }
.field-input:focus { border-color: #409eff; box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1); }
.field-input-multiple { min-height: 56px; }
.field-input-textarea { resize: vertical; }

.form-card :deep(.el-input__inner), .form-card :deep(.el-select__inner) { font-size: 19px; padding: 16px 20px; border-radius: 14px; border: 2px solid #e8e8e8; transition: all 0.3s ease; }
.form-card :deep(.el-input__inner):focus, .form-card :deep(.el-select__inner):focus { border-color: #409eff; box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1); }
.form-card :deep(.el-textarea__inner) { font-size: 19px; padding: 16px 20px; border-radius: 14px; border: 2px solid #e8e8e8; transition: all 0.3s ease; }
.form-card :deep(.el-textarea__inner):focus { border-color: #409eff; box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1); }
.form-card :deep(.el-select--multiple) .el-tag { margin: 6px 8px 6px 0; padding: 8px 14px; font-size: 18px; border-radius: 20px; background: rgba(64, 158, 255, 0.1); border: 1px solid rgba(64, 158, 255, 0.3); color: #409eff; }

.bmi-display { margin-top: 20px; }
.bmi-card { display: flex; flex-direction: column; align-items: center; padding: 24px; border-radius: 20px; color: #ffffff; transition: transform 0.3s ease; }
.bmi-card:hover { transform: translateY(-2px); }
.bmi-normal { background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%); }
.bmi-underweight { background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%); }
.bmi-overweight { background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%); }
.bmi-obese { background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%); }
.bmi-label { font-size: 16px; font-weight: 500; opacity: 0.9; margin-bottom: 8px; }
.bmi-value { font-size: 48px; font-weight: 800; line-height: 1.1; margin-bottom: 8px; }
.bmi-desc { font-size: 15px; opacity: 0.95; }

.goal-options { display: flex; gap: 16px; flex-wrap: wrap; }
.goal-option { flex: 1; min-width: 200px; padding: 20px; border-radius: 16px; border: 2px solid #f0ece6; cursor: pointer; transition: all 0.3s ease; text-align: center; }
.goal-option:hover { border-color: #409eff; background: rgba(64, 158, 255, 0.02); }
.goal-option.active { border-color: #409eff; background: rgba(64, 158, 255, 0.05); box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15); }
.goal-icon { width: 52px; height: 52px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 12px; }
.goal-name { font-size: 19px; font-weight: 600; color: #333; margin-bottom: 6px; }
.goal-desc { font-size: 16px; color: #909399; }

.form-actions { display: flex; gap: 16px; margin-top: 36px; padding-top: 24px; border-top: 1px solid #f0ece6; }
.form-actions :deep(.el-button) { padding: 14px 36px; font-size: 19px; font-weight: 600; border-radius: 14px; display: flex; align-items: center; gap: 8px; }
.form-actions :deep(.el-button--primary) { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); border: none; box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3); }
.form-actions :deep(.el-button--primary):hover { background: linear-gradient(135deg, #3395ff 0%, #59a8ff 100%); box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4); transform: translateY(-2px); }
.form-actions :deep(.el-button--default) { border: 2px solid #e8e8e8; color: #666; }
.form-actions :deep(.el-button--default):hover { border-color: #409eff; color: #409eff; }

@media (max-width: 768px) {
  .form-wrapper { padding: 0 12px; }
  .form-header { padding: 24px; flex-direction: column; text-align: center; }
  .header-text h2 { font-size: 22px; }
  .form-card :deep(.el-card__body) { padding: 20px; }
  .goal-options { flex-direction: column; }
  .goal-option { min-width: 100%; }
}
</style>