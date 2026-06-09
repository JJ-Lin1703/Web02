<template>
  <div class="page-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="一周计划" name="plan">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="card-title">AI健康计划</span>
              <div class="card-actions">
                <el-button 
                  type="success" 
                  @click="handleExportPdf" 
                  :loading="exporting"
                  :disabled="!currentPlan"
                  icon="Download"
                >
                  导出PDF
                </el-button>
                <el-button type="primary" @click="handleGeneratePlan" :loading="generating">
                  生成新计划
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="loading" class="loading-container">
            <el-icon class="is-loading" :size="40"><Loading /></el-icon>
            <p>加载中...</p>
          </div>

          <div v-else-if="!currentPlan" class="empty-container">
            <el-empty description="暂无健康计划，点击上方按钮生成" />
          </div>

          <div v-else class="plan-container">
            <div class="plan-header">
              <h2>{{ currentPlan.planTitle }}</h2>
              <div class="plan-meta">
                <el-tag type="success">每日建议热量: {{ currentPlan.totalCalorie }} kcal</el-tag>
                <el-tag>版本 V{{ currentPlan.versionNo }}</el-tag>
              </div>
            </div>

            <div v-if="weeklyPlanData" class="weekly-plan">
              <el-row :gutter="16">
                <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="day in weeklyPlanData" :key="day.dayName">
                  <el-card class="day-card" shadow="hover">
                    <template #header>
                      <div class="day-header">
                        <span class="day-name">{{ day.dayName }}</span>
                        <span class="day-date">{{ day.dateStr }}</span>
                      </div>
                    </template>
                    <div class="day-content">
                      <div class="section">
                        <h4><el-icon><Bowl /></el-icon> 饮食</h4>
                        <div v-for="meal in day.diet" :key="meal.type" class="meal-item">
                          <span class="meal-type">{{ meal.type }}</span>
                          <span class="meal-name">{{ meal.name }}</span>
                          <span class="meal-calorie">{{ meal.calorie }}</span>
                        </div>
                      </div>
                      <div class="section">
                        <h4><el-icon><TrendCharts /></el-icon> 运动</h4>
                        <div v-for="ex in day.exercise" :key="ex.name" class="exercise-item">
                          <span class="ex-name">{{ ex.name }}</span>
                          <span class="ex-duration">{{ ex.duration }}</span>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="计划详情" name="detail">
        <el-card>
          <template #header>
            <span class="card-title">计划详情</span>
          </template>
          <div v-if="!currentPlan" class="empty-container">
            <el-empty description="暂无计划详情" />
          </div>
          <div v-else class="detail-content">
            <pre>{{ formatPlanContent(currentPlan.planContent) }}</pre>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Bowl, TrendCharts } from '@element-plus/icons-vue'
import { generateAiPlan, getLatestAiPlan, exportAiPlanPdf } from '@/api/user'

const activeTab = ref('plan')
const loading = ref(false)
const generating = ref(false)
const exporting = ref(false)
const currentPlan = ref(null)

const weeklyPlanData = computed(() => {
  if (!currentPlan.value || !currentPlan.value.planContent) return null
  
  try {
    const content = JSON.parse(currentPlan.value.planContent)
    const weeklyPlan = content.weeklyPlan || []
    
    const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    const today = new Date()
    const dayOfWeek = today.getDay()
    const weekDates = []
    
    for (let i = 0; i < 7; i++) {
      const date = new Date(today)
      date.setDate(today.getDate() - dayOfWeek + 1 + i)
      weekDates.push({
        dayName: days[i],
        dateStr: `${date.getMonth() + 1}/${date.getDate()}`
      })
    }
    
    return weeklyPlan.map((day, index) => ({
      ...day,
      dateStr: weekDates[index]?.dateStr || day.date
    }))
  } catch {
    return null
  }
})

const fetchLatestPlan = async () => {
  loading.value = true
  try {
    const res = await getLatestAiPlan()
    currentPlan.value = res.data
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

const handleGeneratePlan = async () => {
  generating.value = true
  try {
    const res = await generateAiPlan()
    currentPlan.value = res.data
    ElMessage.success('AI计划生成成功')
  } catch {
    // 错误已在拦截器处理
  } finally {
    generating.value = false
  }
}

const formatPlanContent = (content) => {
  if (!content) return ''
  try {
    const obj = JSON.parse(content)
    return JSON.stringify(obj, null, 2)
  } catch {
    return content
  }
}

const handleExportPdf = async () => {
  if (!currentPlan.value) {
    ElMessage.warning('请先生成健康计划')
    return
  }
  
  exporting.value = true
  try {
    await exportAiPlanPdf(currentPlan.value.id)
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('导出失败', error)
    await ElMessageBox.alert(`导出失败：${error.message}`, '导出失败', {
      type: 'error'
    })
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  fetchLatestPlan()
})
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

.card-actions {
  display: flex;
  gap: 10px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.plan-container {
  padding: 20px 0;
}

.plan-header {
  text-align: center;
  margin-bottom: 30px;
}

.plan-header h2 {
  margin: 0 0 16px 0;
  font-size: 24px;
  color: #303133;
}

.plan-meta {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.weekly-plan {
  margin-top: 20px;
}

.day-card {
  margin-bottom: 16px;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.day-name {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.day-date {
  font-size: 14px;
  color: #909399;
}

.day-content {
  padding: 8px 0;
}

.section {
  margin-bottom: 16px;
}

.section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.meal-item,
.exercise-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 13px;
}

.meal-type {
  color: #909399;
  width: 50px;
}

.meal-name {
  flex: 1;
  color: #303133;
}

.meal-calorie {
  color: #67c23a;
  font-size: 12px;
}

.ex-name {
  color: #303133;
}

.ex-duration {
  color: #e6a23c;
  font-size: 12px;
}

.detail-content {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
}

.detail-content pre {
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}
</style>
