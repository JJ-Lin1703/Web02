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
            <el-icon class="stat-icon" :size="40" color="#e6a23c"><Scale /></el-icon>
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
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>健康档案</span>
          </template>
          <div v-if="healthRecord" class="health-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="年龄">{{ healthRecord.age }} 岁</el-descriptions-item>
              <el-descriptions-item label="性别">{{ healthRecord.gender === 0 ? '男' : '女' }}</el-descriptions-item>
              <el-descriptions-item label="身高">{{ healthRecord.height }} cm</el-descriptions-item>
              <el-descriptions-item label="体重">{{ healthRecord.weight }} kg</el-descriptions-item>
              <el-descriptions-item label="BMI">{{ healthRecord.bmi }}</el-descriptions-item>
              <el-descriptions-item label="BMR">{{ healthRecord.bmr }} kcal</el-descriptions-item>
              <el-descriptions-item label="TDEE">{{ healthRecord.tdee }} kcal</el-descriptions-item>
              <el-descriptions-item label="健康目标">{{ healthRecord.healthTarget }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty v-else description="暂无健康档案" />
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
import { Calendar, TrendCharts, Scale, Document, CircleCheck, Check } from '@element-plus/icons-vue'
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
  } catch {
    // 错误已在拦截器处理
  }
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

.health-info {
  padding: 10px 0;
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
