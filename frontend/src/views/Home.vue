<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff"></div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.healthRecords }}</div>
              <div class="stat-label">健康记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a"></div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.exerciseDays }}</div>
              <div class="stat-label">运动天数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c"></div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.continuousDays }}</div>
              <div class="stat-label">连续签到</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c"></div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.weekDays }}</div>
              <div class="stat-label">本周签到</div>
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
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/record')">添加健康记录</el-button>
            <el-button type="success" @click="$router.push('/history')">记录体重</el-button>
            <el-button type="warning" @click="$router.push('/ai-plan')">获取AI建议</el-button>
            <el-button 
              :type="checkinStatus.checkedInToday ? 'default' : 'danger'" 
              :disabled="checkinStatus.checkedInToday"
              @click="handleCheckin"
              :loading="checkinLoading"
            >
              {{ checkinStatus.checkedInToday ? '今日已签到' : '立即签到' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日提醒</span>
            </div>
          </template>
          <el-empty description="暂无待办事项" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCheckinStatus, dailyCheckin } from '@/api/user'

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
})
</script>

<style scoped>
.home-container {
  padding: 0;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
