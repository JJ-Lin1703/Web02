<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEditMode ? '修改健康档案' : '填写健康档案' }}</span>
        </div>
      </template>
      
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
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
        
        <el-row :gutter="20">
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
        
        <el-row :gutter="20">
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
        
        <el-row :gutter="20">
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
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="食物过敏">
              <el-input v-model="formData.allergy" type="textarea" placeholder="请输入食物过敏（多个用逗号分隔，如：海鲜,花生）" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="慢性病史">
              <el-input v-model="formData.medicalHistory" type="textarea" placeholder="请输入慢性病史（如：高血压、糖尿病等）" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <el-row :gutter="20" v-if="calculatedMetrics.bmi">
          <el-col :span="8">
            <div class="metric-card">
              <div class="metric-label">身体质量指数 (BMI)</div>
              <div class="metric-value" :class="getBmiClass(calculatedMetrics.bmi)">{{ calculatedMetrics.bmi }}</div>
              <div class="metric-desc">{{ getBmiDescription(calculatedMetrics.bmi) }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="metric-card">
              <div class="metric-label">基础代谢率 (BMR)</div>
              <div class="metric-value">{{ calculatedMetrics.bmr }} kcal</div>
              <div class="metric-desc">每日基础消耗热量</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="metric-card">
              <div class="metric-label">每日总能量消耗 (TDEE)</div>
              <div class="metric-value">{{ calculatedMetrics.tdee }} kcal</div>
              <div class="metric-desc">每日建议摄入热量</div>
            </div>
          </el-col>
        </el-row>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEditMode ? '保存修改' : '提交档案' }}
          </el-button>
          <el-button v-if="isEditMode" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getHealthRecord, createHealthRecord, updateHealthRecord } from '@/api/user';
const formRef = ref(null);
const loading = ref(false);
const isEditMode = ref(false);
const originalData = ref(null);
const formData = reactive({
 age: null,
 gender: null,
 height: null,
 weight: null,
 activityLevel: null,
 dietHobby: '',
 healthTarget: '',
 allergy: '',
 medicalHistory: ''
});
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
};
const calculatedMetrics = computed(() => {
 if (!formData.height || !formData.weight || !formData.age || !formData.gender || !formData.activityLevel) {
 return { bmi: null, bmr: null, tdee: null };
 }
 const height = parseFloat(formData.height);
 const weight = parseFloat(formData.weight);
 const age = formData.age;
 const gender = formData.gender;
 const activityLevel = formData.activityLevel;
 const heightM = height / 100;
 const bmi = (weight / (heightM * heightM)).toFixed(2);
 let bmr;
 if (gender === 0) {
 bmr = 10 * weight + 6.25 * height - 5 * age - 161;
 }
 else {
 bmr = 10 * weight + 6.25 * height - 5 * age + 5;
 }
 let activityFactor;
 switch (activityLevel) {
 case 1:
 activityFactor = 1.2;
 break;
 case 2:
 activityFactor = 1.55;
 break;
 case 3:
 activityFactor = 1.9;
 break;
 default:
 activityFactor = 1.2;
 }
 const tdee = (bmr * activityFactor).toFixed(2);
 return {
 bmi: parseFloat(bmi),
 bmr: Math.round(bmr),
 tdee: parseFloat(tdee)
 };
});
const getBmiClass = (bmi) => {
 if (bmi < 18.5)
 return 'underweight';
 if (bmi < 24)
 return 'normal';
 if (bmi < 28)
 return 'overweight';
 return 'obese';
};
const getBmiDescription = (bmi) => {
 if (bmi < 18.5)
 return '偏瘦，建议增加营养摄入';
 if (bmi < 24)
 return '正常，继续保持';
 if (bmi < 28)
 return '偏胖，建议控制饮食';
 return '肥胖，建议就医咨询';
};
const loadHealthRecord = async () => {
 try {
 const res = await getHealthRecord();
 if (res.data) {
 isEditMode.value = true;
 originalData.value = { ...res.data };
 formData.age = res.data.age;
 formData.gender = res.data.gender;
 formData.height = parseFloat(res.data.height);
 formData.weight = parseFloat(res.data.weight);
 formData.activityLevel = res.data.activityLevel;
 formData.dietHobby = res.data.dietHobby;
 formData.healthTarget = res.data.healthTarget;
 formData.allergy = res.data.allergy || '';
 formData.medicalHistory = res.data.medicalHistory || '';
 }
 }
 catch (error) {
 if (error.response?.data?.code === 404) {
 isEditMode.value = false;
 }
 else {
 console.error('加载健康档案失败', error);
 }
 }
};
const handleSubmit = async () => {
 try {
 await formRef.value.validate();
 loading.value = true;
 const requestData = {
 age: formData.age,
 gender: formData.gender,
 height: formData.height,
 weight: formData.weight,
 activityLevel: formData.activityLevel,
 dietHobby: formData.dietHobby,
 healthTarget: formData.healthTarget,
 allergy: formData.allergy || null,
 medicalHistory: formData.medicalHistory || null
 };
 if (isEditMode.value) {
 await updateHealthRecord(requestData);
 ElMessage.success('健康档案更新成功');
 }
 else {
 await createHealthRecord(requestData);
 ElMessage.success('健康档案创建成功');
 isEditMode.value = true;
 }
 }
 catch (error) {
 ElMessage.error(error.response?.data?.message || '操作失败');
 }
 finally {
 loading.value = false;
 }
};
const handleReset = () => {
 if (originalData.value) {
 formData.age = originalData.value.age;
 formData.gender = originalData.value.gender;
 formData.height = parseFloat(originalData.value.height);
 formData.weight = parseFloat(originalData.value.weight);
 formData.activityLevel = originalData.value.activityLevel;
 formData.dietHobby = originalData.value.dietHobby;
 formData.healthTarget = originalData.value.healthTarget;
 formData.allergy = originalData.value.allergy || '';
 formData.medicalHistory = originalData.value.medicalHistory || '';
 }
};
onMounted(() => {
 loadHealthRecord();
});
</script>

<style scoped>
.page-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-card {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
}

.metric-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.metric-value.underweight {
  color: #e6a23c;
}

.metric-value.normal {
  color: #67c23a;
}

.metric-value.overweight {
  color: #e6a23c;
}

.metric-value.obese {
  color: #f56c6c;
}

.metric-desc {
  font-size: 12px;
  color: #909399;
}
</style>
