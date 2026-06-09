<template>
  <div class="page-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <el-icon class="card-icon" :size="20" color="#409eff"><Document /></el-icon>
          <span class="card-title">{{ isEditMode ? '修改健康档案' : '填写健康档案' }}</span>
        </div>
      </template>
      
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="130px">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input v-model.number="formData.age" type="number" placeholder="请输入年龄（12-80岁）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" placeholder="请选择性别">
                <el-option label="男" :value="0" />
                <el-option label="女" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="身高(cm)" prop="height">
              <el-input v-model.number="formData.height" type="number" step="0.1" placeholder="请输入身高（100-250cm）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input v-model.number="formData.weight" type="number" step="0.1" placeholder="请输入体重（30-300kg）" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="日常活动水平" prop="activityLevel">
              <el-select v-model="formData.activityLevel" placeholder="请选择活动水平">
                <el-option label="低（久坐少动）" :value="1" />
                <el-option label="中（轻度运动）" :value="2" />
                <el-option label="高（经常锻炼）" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="饮食偏好" prop="dietHobby">
              <el-select v-model="formData.dietHobby" placeholder="请选择饮食偏好">
                <el-option label="素食" value="素食" />
                <el-option label="均衡" value="均衡" />
                <el-option label="高蛋白" value="高蛋白" />
                <el-option label="低碳水" value="低碳水" />
                <el-option label="控糖" value="控糖" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="健康目标" prop="healthTarget">
              <el-select v-model="formData.healthTarget" placeholder="请选择健康目标">
                <el-option label="减肥" value="减肥" />
                <el-option label="增肌" value="增肌" />
                <el-option label="维持健康" value="维持健康" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="食物过敏">
              <el-select v-model="formData.allergy" multiple placeholder="请选择食物过敏（可多选）" style="max-width: 600px;">
                <el-option v-for="option in allergyOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
              <span class="form-hint">（多选，如无过敏可不选）</span>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="慢性病史">
              <el-input v-model="formData.medicalHistory" type="textarea" placeholder="请输入慢性病史（如：高血压、糖尿病等）" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item class="form-actions">
          <el-button type="primary" @click="handleSubmit" :loading="loading" size="large">
            {{ isEditMode ? '保存修改' : '提交档案' }}
          </el-button>
          <el-button v-if="isEditMode" @click="handleReset" size="large">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { getHealthRecord, createHealthRecord, updateHealthRecord } from '@/api/user'

const formRef = ref(null)
const loading = ref(false)
const isEditMode = ref(false)
const originalData = ref(null)

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

const allergyOptions = [
  { label: '海鲜', value: '海鲜' },
  { label: '花生', value: '花生' },
  { label: '牛奶', value: '牛奶' },
  { label: '鸡蛋', value: '鸡蛋' },
  { label: '大豆', value: '大豆' },
  { label: '小麦', value: '小麦' },
  { label: '坚果', value: '坚果' },
  { label: '鱼类', value: '鱼类' },
  { label: '贝壳类', value: '贝壳类' },
  { label: '其他', value: '其他' }
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
      isEditMode.value = true
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
  loadHealthRecord()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.main-card {
  border: none;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 28px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  margin: -1px -1px 0 -1px;
}

.card-icon {
  flex-shrink: 0;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
}

.main-card :deep(.el-card__body) {
  padding: 32px;
}

.main-card :deep(.el-form-item) {
  margin-bottom: 28px;
  max-width: 500px;
}

.main-card :deep(.el-form-item__label) {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  padding-right: 12px;
}

.main-card :deep(.el-input__inner),
.main-card :deep(.el-select__inner) {
  font-size: 18px;
  padding: 16px 18px;
  border-radius: 12px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
  max-width: 400px;
  
  &:focus {
    border-color: #409eff;
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  }
}

.main-card :deep(.el-select) {
  max-width: 400px;
}

.main-card :deep(.el-textarea__inner) {
  font-size: 18px;
  padding: 16px 18px;
  border-radius: 12px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
  max-width: 600px;
  
  &:focus {
    border-color: #409eff;
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  }
}

.main-card :deep(.el-select--multiple) {
  max-width: 600px;
  
  .el-select__inner {
    min-height: 40px;
    flex-wrap: wrap;
  }
  
  .el-tag {
    margin: 4px 8px 4px 0;
    padding: 6px 12px;
    font-size: 14px;
  }
}

.form-hint {
  font-size: 14px;
  color: #909399;
  margin-left: 8px;
}

.form-actions {
  display: flex;
  gap: 20px;
  margin-top: 36px !important;
  justify-content: flex-start;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.form-actions :deep(.el-button) {
  padding: 14px 40px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px;
}

.form-actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  
  &:hover {
    background: linear-gradient(135deg, #3395ff 0%, #59a8ff 100%);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  }
}

.form-actions :deep(.el-button--default) {
  border: 2px solid #e8e8e8;
  color: #666;
  
  &:hover {
    border-color: #409eff;
    color: #409eff;
  }
}

@media (max-width: 1024px) {
  .main-card :deep(.el-card__body) {
    padding: 24px;
  }
}
</style>