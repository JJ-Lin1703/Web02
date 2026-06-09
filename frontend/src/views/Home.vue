<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon" :size="40" color="#409eff"><Calendar /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ checkinStatus.totalDays }}</span>
              <span class="stat-label">累计签到</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon" :size="40" color="#67c23a"><TrendCharts /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ checkinStatus.continuousDays }}</span>
              <span class="stat-label">连续签到</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon" :size="40" color="#e6a23c"><ScaleToOriginal /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ statistics.weightRecords }}</span>
              <span class="stat-label">体重记录</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon class="stat-icon" :size="40" color="#f56c6c"><Document /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ statistics.aiPlans }}</span>
              <span class="stat-label">AI计划</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日签到</span>
              <el-tag v-if="checkinStatus.checkedInToday" type="success">已签到</el-tag>
              <el-tag v-else type="info">未签到</el-tag>
            </div>
          </template>
          <div class="checkin-content">
            <div v-if="checkinStatus.checkedInToday" class="checked">
              <el-icon :size="60" color="#67c23a"><CircleCheck /></el-icon>
              <p>今日已签到</p>
              <p class="sub-text">连续签到 {{ checkinStatus.continuousDays }} 天</p>
            </div>
            <div v-else class="not-checked">
              <el-button type="primary" size="large" @click="handleCheckin" :loading="checkinLoading">
                立即签到
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8" v-if="healthRecord">
        <div class="metric-card" :class="getBmiClass(parseFloat(healthRecord.bmi))">
          <div class="metric-icon">
            <el-icon :size="24" color="#fff"><ScaleToOriginal /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-label">身体质量指数 (BMI)</div>
            <div class="metric-value">{{ healthRecord.bmi }}</div>
            <div class="metric-desc">{{ getBmiDescription(parseFloat(healthRecord.bmi)) }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8" v-if="healthRecord">
        <div class="metric-card metric-card-bmr">
          <div class="metric-icon">
            <el-icon :size="24" color="#fff"><Lightning /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-label">基础代谢率 (BMR)</div>
            <div class="metric-value">{{ healthRecord.bmr }}<span class="metric-unit">kcal</span></div>
            <div class="metric-desc">每日基础消耗热量</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8" v-if="healthRecord">
        <div class="metric-card metric-card-tdee">
          <div class="metric-icon">
            <el-icon :size="24" color="#fff"><DataAnalysis /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-label">每日总能量消耗 (TDEE)</div>
            <div class="metric-value">{{ healthRecord.tdee }}<span class="metric-unit">kcal</span></div>
            <div class="metric-desc">每日建议摄入热量</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px" v-if="healthRecord && calorieRange">
      <el-col :span="24">
        <el-card>
          <div class="calorie-card">
            <div class="calorie-header">
              <el-icon :size="20" color="#722ed1"><Lightning /></el-icon>
              <span class="calorie-title">建议热量摄入范围</span>
            </div>
            <div class="calorie-content">
              <div class="calorie-item" :class="{ active: healthRecord.healthTarget === '减肥' }">
                <div class="calorie-icon" style="background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%)">
                  <el-icon :size="18" color="#fff"><ArrowDown /></el-icon>
                </div>
                <div class="calorie-info">
                  <div class="calorie-label">减肥</div>
                  <div class="calorie-range">{{ calorieRange.loseWeight.min }} - {{ calorieRange.loseWeight.max }} <span class="calorie-unit">kcal/天</span></div>
                  <div class="calorie-hint">TDEE - 400 ~ TDEE - 200</div>
                </div>
              </div>
              <div class="calorie-item" :class="{ active: healthRecord.healthTarget === '增肌' }">
                <div class="calorie-icon" style="background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%)">
                  <el-icon :size="18" color="#fff"><ArrowUp /></el-icon>
                </div>
                <div class="calorie-info">
                  <div class="calorie-label">增肌</div>
                  <div class="calorie-range">{{ calorieRange.gainMuscle.min }} - {{ calorieRange.gainMuscle.max }} <span class="calorie-unit">kcal/天</span></div>
                  <div class="calorie-hint">TDEE + 200 ~ TDEE + 400</div>
                </div>
              </div>
              <div class="calorie-item" :class="{ active: healthRecord.healthTarget === '维持健康' }">
                <div class="calorie-icon" style="background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%)">
                  <el-icon :size="18" color="#fff"><Minus /></el-icon>
                </div>
                <div class="calorie-info">
                  <div class="calorie-label">维持健康</div>
                  <div class="calorie-range">{{ calorieRange.maintain.min }} - {{ calorieRange.maintain.max }} <span class="calorie-unit">kcal/天</span></div>
                  <div class="calorie-hint">≈ TDEE</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>饮食建议</span>
          </template>
          <div v-if="aiSummary && aiSummary.diet" class="summary-list">
            <div v-for="(item, index) in aiSummary.diet" :key="index" class="summary-item">
              <el-icon color="#67c23a"><Check /></el-icon>
              <span>{{ item }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无建议，请先生成AI计划" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>运动建议</span>
          </template>
          <div v-if="aiSummary && aiSummary.exercise" class="summary-list">
            <div v-for="(item, index) in aiSummary.exercise" :key="index" class="summary-item">
              <el-icon color="#409eff"><Check /></el-icon>
              <span>{{ item }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无建议，请先生成AI计划" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>健康提示</span>
          </template>
          <div v-if="aiSummary && aiSummary.tips" class="summary-list">
            <div v-for="(item, index) in aiSummary.tips" :key="index" class="summary-item">
              <el-icon color="#e6a23c"><Check /></el-icon>
              <span>{{ item }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无提示，请先生成AI计划" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Calendar, TrendCharts, ScaleToOriginal, Document, CircleCheck, Check, Lightning, DataAnalysis, ArrowDown, ArrowUp, Minus } from '@element-plus/icons-vue'
import { getCheckinStatus, dailyCheckin, getHealthRecord, getLatestAiPlan, getWeightHistory, getAiPlanHistory } from '@/api/user'

const checkinStatus = reactive({
  checkedInToday: false,
  totalDays: 0,
  continuousDays: 0,
  weekDays: 0
})

const statistics = reactive({
  weightRecords: 0,
  aiPlans: 0
})

const healthRecord = ref(null)
const aiSummary = ref(null)
const checkinLoading = ref(false)
const calorieRange = ref(null)

const fetchCheckinStatus = async () => {
  try {
    const res = await getCheckinStatus()
    Object.assign(checkinStatus, res.data)
  } catch {
    // 错误已在拦截器处理
  }
}

const fetchStatistics = async () => {
  try {
    const weightRes = await getWeightHistory()
    if (weightRes.data) {
      statistics.weightRecords = weightRes.data.length
    }
  } catch {
    // 错误已在拦截器处理
  }
  
  try {
    const planRes = await getAiPlanHistory()
    if (planRes.data) {
      statistics.aiPlans = planRes.data.length
    }
  } catch {
    // 错误已在拦截器处理
  }
}

const fetchHealthRecord = async () => {
  try {
    const res = await getHealthRecord()
    healthRecord.value = res.data
    if (res.data && res.data.tdee) {
      const tdee = parseFloat(res.data.tdee)
      calorieRange.value = {
        loseWeight: { min: Math.round(tdee - 400), max: Math.round(tdee - 200) },
        gainMuscle: { min: Math.round(tdee + 200), max: Math.round(tdee + 400) },
        maintain: { min: Math.round(tdee - 50), max: Math.round(tdee + 50) }
      }
    }
  } catch {
    // 错误已在拦截器处理
  }
}

const getBmiClass = (bmi) => {
  if (bmi < 18.5) return 'metric-card-underweight'
  if (bmi < 24) return 'metric-card-normal'
  if (bmi < 28) return 'metric-card-overweight'
  return 'metric-card-obese'
}

const getBmiDescription = (bmi) => {
  if (bmi < 18.5) return '偏瘦，建议增加营养摄入'
  if (bmi < 24) return '正常，继续保持'
  if (bmi < 28) return '偏胖，建议控制饮食'
  return '肥胖，建议就医咨询'
}

const fetchAiSummary = async () => {
  try {
    const res = await getLatestAiPlan()
    if (res.data && res.data.planContent) {
      const content = JSON.parse(res.data.planContent)
      aiSummary.value = content.summary
    }
  } catch {
    // 错误已在拦截器处理
  }
}

const handleCheckin = async () => {
  checkinLoading.value = true
  try {
    await dailyCheckin()
    ElMessage.success('签到成功')
    await fetchCheckinStatus()
  } catch {
    // 错误已在拦截器处理
  } finally {
    checkinLoading.value = false
  }
}

onMounted(() => {
  fetchCheckinStatus()
  fetchStatistics()
  fetchHealthRecord()
  fetchAiSummary()
})
</script>

<style scoped>
.home-container {
  padding: 0;
}

.stat-card {
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkin-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.checked {
  text-align: center;
}

.checked p {
  margin: 8px 0 0 0;
  font-size: 16px;
  color: #303133;
}

.checked .sub-text {
  font-size: 14px;
  color: #909399;
}

.not-checked {
  padding: 20px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  border-radius: 16px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  }
}

.metric-card-normal {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.metric-card-underweight {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.metric-card-overweight {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.metric-card-obese {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
}

.metric-card-bmr {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
}

.metric-card-tdee {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.metric-icon {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(4px);
}

.metric-info {
  flex: 1;
}

.metric-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 8px;
  font-weight: 500;
}

.metric-value {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.1;
  display: flex;
  align-items: baseline;
  gap: 6px;
  letter-spacing: -1px;
}

.metric-unit {
  font-size: 16px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.85);
}

.metric-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 6px;
}

.calorie-card {
  background: #fafafa;
  border-radius: 16px;
  padding: 24px;
}

.calorie-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.calorie-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.calorie-content {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.calorie-item {
  flex: 1;
  min-width: calc(33.33% - 14px);
  background: #ffffff;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  }
  
  &.active {
    border-color: #409eff;
    background: rgba(64, 158, 255, 0.05);
  }
}

.calorie-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.calorie-info {
  flex: 1;
}

.calorie-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.calorie-range {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.calorie-unit {
  font-size: 12px;
  font-weight: 500;
  color: #909399;
  margin-left: 4px;
}

.calorie-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.summary-list {
  padding: 10px 0;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
}
</style>
