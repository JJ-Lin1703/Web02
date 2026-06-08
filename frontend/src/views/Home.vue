<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card stat-card-blue">
          <div class="stat-content">
            <div class="stat-icon-wrapper">
              <el-icon size="28" color="#ffffff"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.healthRecords }}</div>
              <div class="stat-label">健康记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-green">
          <div class="stat-content">
            <div class="stat-icon-wrapper">
              <el-icon size="28" color="#ffffff"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.exerciseDays }}</div>
              <div class="stat-label">运动天数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-orange">
          <div class="stat-content">
            <div class="stat-icon-wrapper">
              <el-icon size="28" color="#ffffff"><Trophy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.continuousDays }}</div>
              <div class="stat-label">连续签到</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-red">
          <div class="stat-content">
            <div class="stat-icon-wrapper">
              <el-icon size="28" color="#ffffff"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.weekDays }}</div>
              <div class="stat-label">本周签到</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 24px">
      <el-col :span="12">
        <el-card class="action-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-icon" size="20" color="#409eff"><Lightning /></el-icon>
              <span class="card-title">快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button class="action-btn action-btn-primary" @click="$router.push('/record')">
              <el-icon size="18"><Edit /></el-icon>
              添加健康记录
            </el-button>
            <el-button class="action-btn action-btn-success" @click="$router.push('/record')">
              <el-icon size="18"><ScaleToOriginal /></el-icon>
              更新健康档案
            </el-button>
            <el-button class="action-btn action-btn-warning" @click="$router.push('/ai-plan')">
              <el-icon size="18"><MagicStick /></el-icon>
              获取AI建议
            </el-button>
            <el-button 
              :class="['action-btn', checkinStatus.checkedInToday ? 'action-btn-disabled' : 'action-btn-danger']" 
              :disabled="checkinStatus.checkedInToday"
              @click="handleCheckin"
              :loading="checkinLoading"
            >
              <el-icon size="18"><Check /></el-icon>
              {{ checkinStatus.checkedInToday ? '今日已签到' : '立即签到' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="reminder-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-icon" size="20" color="#67c23a"><Bell /></el-icon>
              <span class="card-title">今日提醒</span>
            </div>
          </template>
          <div class="reminder-content">
            <el-empty class="empty-reminder" description="暂无待办事项" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 24px">
      <el-col :span="18">
        <el-card class="health-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-icon" size="20" color="#409eff"><Histogram /></el-icon>
              <span class="card-title">健康数据概览（根据最新的健康档案推算）</span>
            </div>
          </template>
          <div class="health-overview">
            <div class="overview-item" :class="healthMetrics.bmiClass">
              <div class="overview-icon" :class="`icon-${healthMetrics.bmiClass}`">
                <el-icon size="24" color="#fff"><ScaleToOriginal /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">身体质量指数</div>
                <div class="overview-value">{{ healthMetrics.bmi || '--' }}</div>
                <div class="overview-unit">BMI · {{ healthMetrics.bmiDesc || '--' }}</div>
              </div>
            </div>
            <div class="overview-item">
              <div class="overview-icon" style="background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%)">
                <el-icon size="24" color="#fff"><Lightning /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">基础代谢率</div>
                <div class="overview-value">{{ healthMetrics.bmr || '--' }}</div>
                <div class="overview-unit">kcal/天</div>
              </div>
            </div>
            <div class="overview-item">
              <div class="overview-icon" style="background: linear-gradient(135deg, #722ed1 0%, #9254de 100%)">
                <el-icon size="24" color="#fff"><DataAnalysis /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">每日能量消耗</div>
                <div class="overview-value">{{ healthMetrics.tdee || '--' }}</div>
                <div class="overview-unit">TDEE (kcal)</div>
              </div>
            </div>
            <div class="overview-item">
              <div class="overview-icon" style="background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%)">
                <el-icon size="24" color="#fff"><Trophy /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">建议热量</div>
                <div class="overview-value">
                  <template v-if="healthMetrics.calorieRange">
                    <template v-if="healthMetrics.healthTarget === '减肥'">{{ healthMetrics.calorieRange.loseWeight.min }}-{{ healthMetrics.calorieRange.loseWeight.max }}</template>
                    <template v-else-if="healthMetrics.healthTarget === '增肌'">{{ healthMetrics.calorieRange.gainMuscle.min }}-{{ healthMetrics.calorieRange.gainMuscle.max }}</template>
                    <template v-else-if="healthMetrics.healthTarget === '维持健康'">{{ healthMetrics.calorieRange.maintain.min }}-{{ healthMetrics.calorieRange.maintain.max }}</template>
                  </template>
                  <template v-else>--</template>
                </div>
                <div class="overview-unit">kcal/天</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="tips-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-icon" size="20" color="#e6a23c"><Help /></el-icon>
              <span class="card-title">健康小贴士</span>
            </div>
          </template>
          <div class="tips-content">
            <div class="tip-item">
              <div class="tip-number">01</div>
              <div class="tip-text">每天至少喝8杯水，保持身体水分充足</div>
            </div>
            <div class="tip-item">
              <div class="tip-number">02</div>
              <div class="tip-text">饭后半小时适当运动，有助于消化</div>
            </div>
            <div class="tip-item">
              <div class="tip-number">03</div>
              <div class="tip-text">保持良好的睡眠习惯，每天睡够7-8小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, DataAnalysis, Trophy, Calendar, Lightning, Edit, ScaleToOriginal, MagicStick, Check, Bell, Histogram, Help } from '@element-plus/icons-vue'
import { getCheckinStatus, dailyCheckin, getHealthRecord } from '@/api/user'

const checkinStatus = reactive({
  checkedInToday: false,
  totalDays: 0,
  continuousDays: 0,
  weekDays: 0
})

const statistics = reactive({
  healthRecords: 0,
  exerciseDays: 0,
  continuousDays: 0,
  weekDays: 0
})

const healthMetrics = reactive({
  bmi: null,
  bmiClass: '',
  bmiDesc: '',
  bmr: null,
  tdee: null,
  calorieRange: null,
  healthTarget: ''
})

const checkinLoading = ref(false)

const fetchCheckinStatus = async () => {
  try {
    const res = await getCheckinStatus()
    if (res.data) {
      checkinStatus.checkedInToday = res.data.checkedInToday
      checkinStatus.totalDays = res.data.totalDays
      checkinStatus.continuousDays = res.data.continuousDays
      checkinStatus.weekDays = res.data.weekDays
      
      statistics.exerciseDays = res.data.totalDays
      statistics.continuousDays = res.data.continuousDays
      statistics.weekDays = res.data.weekDays
    }
  } catch (error) {
    console.error('获取签到状态失败', error)
  }
}

const fetchHealthMetrics = async () => {
  try {
    const res = await getHealthRecord()
    if (res.data) {
      const bmi = parseFloat(res.data.bmi)
      healthMetrics.bmi = bmi
      healthMetrics.bmr = Math.round(res.data.bmr)
      healthMetrics.tdee = parseFloat(res.data.tdee)
      healthMetrics.healthTarget = res.data.healthTarget
      
      if (bmi < 18.5) {
        healthMetrics.bmiClass = 'underweight'
        healthMetrics.bmiDesc = '偏瘦'
      } else if (bmi < 24) {
        healthMetrics.bmiClass = 'normal'
        healthMetrics.bmiDesc = '正常'
      } else if (bmi < 28) {
        healthMetrics.bmiClass = 'overweight'
        healthMetrics.bmiDesc = '偏胖'
      } else {
        healthMetrics.bmiClass = 'obese'
        healthMetrics.bmiDesc = '肥胖'
      }
      
      const tdee = healthMetrics.tdee
      healthMetrics.calorieRange = {
        loseWeight: { min: Math.round(tdee - 400), max: Math.round(tdee - 200) },
        gainMuscle: { min: Math.round(tdee + 200), max: Math.round(tdee + 400) },
        maintain: { min: Math.round(tdee - 50), max: Math.round(tdee + 50) }
      }
    }
  } catch (error) {
    console.error('获取健康数据失败', error)
  }
}

const handleCheckin = async () => {
  if (checkinStatus.checkedInToday) return
  
  checkinLoading.value = true
  try {
    const res = await dailyCheckin()
    ElMessage.success(res.message)
    await fetchCheckinStatus()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '签到失败')
  } finally {
    checkinLoading.value = false
  }
}

onMounted(() => {
  fetchCheckinStatus()
  fetchHealthMetrics()
})
</script>

<style scoped>
.home-container {
  padding: 0;
}

.stat-card {
  margin-bottom: 0;
  border: none;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  }
}

.stat-card-blue {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.stat-card-green {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-card-orange {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
}

.stat-card-red {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-icon {
  flex-shrink: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.action-card,
.reminder-card,
.health-card,
.tips-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.action-card :deep(.el-card__body) {
  padding: 24px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
  min-width: calc(50% - 6px);
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  }
  
  &:active:not(:disabled) {
    transform: translateY(0);
  }
}

.action-btn-primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #ffffff;
}

.action-btn-success {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #ffffff;
}

.action-btn-warning {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
  color: #ffffff;
}

.action-btn-danger {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
  color: #ffffff;
}

.action-btn-disabled {
  background: #f0f0f0;
  color: #909399;
  cursor: not-allowed;
}

.reminder-card :deep(.el-card__body) {
  padding: 24px;
  min-height: 200px;
}

.reminder-content {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.empty-reminder :deep(.el-empty__description) {
  font-size: 14px;
  color: #909399;
}

.health-card :deep(.el-card__body) {
  padding: 24px;
}

.health-overview {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.overview-item {
  flex: 1;
  min-width: calc(25% - 15px);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fafafa;
  border-radius: 14px;
  transition: all 0.3s ease;
  
  &:hover {
    background: #f0f0f0;
    transform: translateY(-2px);
  }
}

.overview-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.overview-info {
  flex: 1;
}

.overview-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.overview-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.overview-unit {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.icon-normal {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%) !important;
}

.icon-underweight {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%) !important;
}

.icon-overweight {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%) !important;
}

.icon-obese {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%) !important;
}

.tips-card :deep(.el-card__body) {
  padding: 20px;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tip-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fff9e6;
  border-radius: 10px;
}

.tip-number {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tip-text {
  font-size: 14px;
  color: #666666;
  line-height: 1.5;
  display: flex;
  align-items: center;
}

@media (max-width: 1024px) {
  .health-overview {
    flex-direction: column;
  }
  
  .overview-item {
    min-width: 100%;
  }
  
  .action-btn {
    min-width: 100%;
  }
}
</style>
