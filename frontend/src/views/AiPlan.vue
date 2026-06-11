<template>
  <div class="page-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="一周计划" name="plan">
        <el-card>
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <Vue3Lottie :animationLink="'/robot.json'" :width="48" :height="48" />
                <span class="card-title">AI健康计划</span>
              </div>
              <div class="card-header-right">
                <el-button type="success" @click="handleExportPdf" :loading="exporting" :disabled="!currentPlan">
                  导出PDF
                </el-button>
                <el-button type="warning" @click="showTweakDialog" :disabled="!currentPlan">
                  微调计划
                </el-button>
                <el-button type="primary" @click="handleGeneratePlan" :loading="generating">
                  生成新计划
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="loading || generating || tweaking" class="loading-container">
            <Vue3Lottie :animationLink="'/robot.json'" :width="240" :height="240" />
            <p>{{ tweaking ? 'AI 正在微调健康计划...' : 'AI 正在生成健康计划...' }}</p>
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
              <div class="day-tabs">
                <button
                  v-for="day in weeklyPlanData"
                  :key="day.dayName"
                  :class="['day-tab', { active: selectedDay === day.dayName }]"
                  @click="selectedDay = day.dayName"
                >
                  <span class="day-tab-name">{{ day.dayName }}</span>
                  <span class="day-tab-date">{{ day.dateStr }}</span>
                </button>
              </div>

              <el-card v-if="selectedDayData" class="day-card" shadow="hover">
                <template #header>
                  <div class="day-header">
                    <span class="day-name">{{ selectedDayData.dayName }}</span>
                    <span class="day-date">{{ selectedDayData.dateStr }}</span>
                  </div>
                </template>
                <div class="day-content">
                  <div class="section">
                    <h4><el-icon><Bowl /></el-icon> 饮食</h4>
                    <div class="meal-cards">
                      <div v-for="(meal, mealIndex) in selectedDayData.diet" :key="meal.type" class="meal-card">
                        <span class="meal-type-tag">{{ meal.type }}</span>
                        <div class="meal-card-body">
                          <span class="meal-name">{{ meal.name }}</span>
                          <span class="meal-calorie">{{ meal.calorie }}</span>
                        </div>
                        <el-button 
                          link 
                          class="edit-btn" 
                          @click="openEditDialog('diet', mealIndex)"
                        >
                          <Edit />
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div class="section">
                    <h4><el-icon><TrendCharts /></el-icon> 运动</h4>
                    <div class="exercise-cards">
                      <div v-for="(ex, exIndex) in selectedDayData.exercise" :key="ex.name" class="exercise-card">
                        <span class="ex-name">{{ ex.name }}</span>
                        <span class="ex-duration">{{ ex.duration }}</span>
                        <el-button 
                          link 
                          class="edit-btn" 
                          @click="openEditDialog('exercise', exIndex)"
                        >
                          <Edit />
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
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

    <!-- 直接编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="editDialogTitle" width="480px" append-to-body>
      <el-form :model="editForm" label-width="80px">
        <el-form-item v-if="editType === 'diet'" label="餐食类型">
          <el-select v-model="editForm.type" placeholder="请选择">
            <el-option label="早餐" value="早餐" />
            <el-option label="午餐" value="午餐" />
            <el-option label="晚餐" value="晚餐" />
          </el-select>
        </el-form-item>
        <el-form-item :label="editType === 'diet' ? '餐食名称' : '运动名称'">
          <el-input v-model="editForm.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item :label="editType === 'diet' ? '热量' : '时长'">
          <el-input v-model="editForm.extra" placeholder="如：350kcal 或 30分钟" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editing">
          保存修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 微调计划弹窗 -->
    <el-dialog v-model="tweakDialogVisible" title="计划微调反馈" width="550px" append-to-body>
      <el-form :model="tweakForm" label-width="100px">
        <el-form-item label="星期">
          <el-select v-model="tweakForm.dayName" placeholder="请选择">
            <el-option v-for="d in dayOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="tweakForm.module" placeholder="请选择">
            <el-option label="饮食" value="饮食" />
            <el-option label="运动" value="运动" />
          </el-select>
        </el-form-item>
        <el-form-item label="具体内容">
          <el-input v-model="tweakForm.itemDesc" placeholder="如：早餐的燕麦粥、慢跑" />
        </el-form-item>
        <el-form-item label="不满原因">
          <el-input v-model="tweakForm.reason" type="textarea" :rows="3" placeholder="请描述为什么不满意..." />
        </el-form-item>
      </el-form>

      <div class="tweak-original" v-if="tweakForm.dayName && tweakForm.module && tweakOriginalContent">
        <p class="tweak-section-label">当前计划内容：</p>
        <div class="tweak-original-items">
          <div v-for="(item, i) in tweakOriginalContent" :key="i" class="tweak-original-row">
            <el-tag size="small" :type="tweakIsDiet ? 'info' : 'warning'">{{ tweakIsDiet ? item.type : item.name }}</el-tag>
            <span v-if="tweakIsDiet" class="tweak-original-name">{{ item.name }}</span>
            <span class="tweak-original-extra">{{ tweakIsDiet ? item.calorie : item.duration }}</span>
          </div>
        </div>
      </div>

      <div class="tweak-preview" v-if="tweakForm.dayName && tweakForm.module">
        <p class="tweak-preview-label">反馈预览：</p>
        <p class="tweak-preview-text">
          我对<strong>【{{ tweakForm.dayName }}】</strong>的<strong>【{{ tweakForm.module }}】</strong>不满意<span v-if="tweakForm.itemDesc">，具体是<strong>【{{ tweakForm.itemDesc }}】</strong></span><span v-if="tweakForm.reason">，因为<strong>【{{ tweakForm.reason }}】</strong></span>。请仅修改这一天这一模块，其余保持完全不变。
        </p>
      </div>

      <template #footer>
        <el-button @click="tweakDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTweakSubmit" :loading="tweaking">
          提交微调
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bowl, TrendCharts, Edit } from '@element-plus/icons-vue'
import { Vue3Lottie } from 'vue3-lottie'
import { generateAiPlan, getLatestAiPlan, exportAiPlanPdf, tweakAiPlan, updateAiPlanContent } from '@/api/user'

const activeTab = ref('plan')
const loading = ref(false)
const generating = ref(false)
const exporting = ref(false)
const editing = ref(false)
const currentPlan = ref(null)
const selectedDay = ref('周一')

// 编辑相关状态
const editDialogVisible = ref(false)
const editType = ref('diet') // 'diet' or 'exercise'
const editIndex = ref(0)
const editForm = ref({
  type: '',
  name: '',
  extra: ''
})

const editDialogTitle = computed(() => {
  const dayName = selectedDay.value
  const moduleName = editType.value === 'diet' ? '饮食' : '运动'
  return `${dayName} - ${moduleName}编辑`
})

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

const selectedDayData = computed(() => {
  if (!weeklyPlanData.value) return null
  return weeklyPlanData.value.find(d => d.dayName === selectedDay.value)
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

// ==================== 微调计划 ====================
const dayOptions = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const tweakDialogVisible = ref(false)
const tweaking = ref(false)
const tweakForm = ref({
  dayName: '',
  module: '',
  itemDesc: '',
  reason: ''
})

const tweakOriginalContent = computed(() => {
  if (!weeklyPlanData.value || !tweakForm.value.dayName || !tweakForm.value.module) return null
  const day = weeklyPlanData.value.find(d => d.dayName === tweakForm.value.dayName)
  if (!day) return null
  return tweakForm.value.module === '饮食' ? day.diet : day.exercise
})

const tweakIsDiet = computed(() => tweakForm.value.module === '饮食')

const openEditDialog = (type, index) => {
  editType.value = type
  editIndex.value = index
  
  const dayData = selectedDayData.value
  if (!dayData) return
  
  const item = type === 'diet' ? dayData.diet[index] : dayData.exercise[index]
  if (item) {
    editForm.value = {
      type: item.type || '',
      name: item.name || '',
      extra: item.calorie || item.duration || ''
    }
  }
  
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editForm.value.name) {
    ElMessage.warning('请输入名称')
    return
  }
  if (!editForm.value.extra) {
    ElMessage.warning('请输入热量或时长')
    return
  }
  
  editing.value = true
  try {
    const planContent = JSON.parse(currentPlan.value.planContent)
    const dayIndex = planContent.weeklyPlan.findIndex(d => d.dayName === selectedDay.value)
    
    if (dayIndex !== -1) {
      if (editType.value === 'diet') {
        planContent.weeklyPlan[dayIndex].diet[editIndex.value] = {
          type: editForm.value.type || planContent.weeklyPlan[dayIndex].diet[editIndex.value].type,
          name: editForm.value.name,
          calorie: editForm.value.extra
        }
      } else {
        planContent.weeklyPlan[dayIndex].exercise[editIndex.value] = {
          name: editForm.value.name,
          duration: editForm.value.extra
        }
      }
    }
    
    const updatedContent = JSON.stringify(planContent)
    await updateAiPlanContent(currentPlan.value.id, updatedContent)
    
    currentPlan.value.planContent = updatedContent
    editDialogVisible.value = false
    ElMessage.success('修改成功')
  } catch (error) {
    console.error('编辑失败', error)
    ElMessage.error('编辑失败')
  } finally {
    editing.value = false
  }
}

const showTweakDialog = () => {
  tweakForm.value = { dayName: '', module: '', itemDesc: '', reason: '' }
  tweakDialogVisible.value = true
}

const handleTweakSubmit = async () => {
  if (!tweakForm.value.dayName || !tweakForm.value.module) {
    ElMessage.warning('请选择星期和模块')
    return
  }
  if (!tweakForm.value.reason && !tweakForm.value.itemDesc) {
    ElMessage.warning('请填写具体内容或不满原因')
    return
  }

  tweakDialogVisible.value = false
  tweaking.value = true
  try {
    const res = await tweakAiPlan({
      planId: currentPlan.value.id,
      ...tweakForm.value
    })
    currentPlan.value = res.data
    selectedDay.value = tweakForm.value.dayName
    ElMessage.success('计划微调成功')
  } catch (error) {
    console.error('微调失败', error)
    tweakDialogVisible.value = true
  } finally {
    tweaking.value = false
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

.card-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header-right {
  display: flex;
  gap: 8px;
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
  margin: 0 0 12px 0;
  font-size: 26px;
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

.day-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  justify-content: space-evenly;
}

.day-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 10px 20px;
  border: 1.5px solid #e4e7ed;
  border-radius: 16px;
  background: #fff;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: inherit;
}

.day-tab:hover {
  border-color: #26B5B5;
  background: rgba(38, 181, 181, 0.05);
}

.day-tab.active {
  background: #26B5B5;
  border-color: #26B5B5;
  color: #fff;
  box-shadow: 0 4px 12px rgba(38, 181, 181, 0.3);
}

.day-tab-name {
  font-size: 15px;
  font-weight: 600;
}

.day-tab-date {
  font-size: 13px;
  opacity: 0.7;
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
  margin: 0 0 6px 0;
  font-size: 15px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.meal-cards,
.exercise-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meal-card {
  display: flex;
  align-items: center;
  border-radius: 14px;
  border: 1px solid #e8ecef;
  transition: all 0.2s ease;
}

.meal-card:hover {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  border-color: #26B5B5;
}

.meal-type-tag {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  padding: 10px 6px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: #26B5B5;
  writing-mode: vertical-rl;
  letter-spacing: 2px;
  flex-shrink: 0;
}

.meal-card-body {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
}

.meal-card .edit-btn,
.exercise-card .edit-btn {
  opacity: 1 !important;
  color: #409eff !important;
  margin-left: 12px !important;
  padding: 6px 12px;
  flex-shrink: 0;
  border: 1px solid #409eff;
  border-radius: 4px;
  background: #fff;
  transition: all 0.2s ease;
  display: flex !important;
  align-items: center;
  justify-content: center;
  
  &:hover {
    background: #ecf5ff;
  }
}

.exercise-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e8ecef;
  transition: all 0.2s ease;
}

.exercise-card:hover {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  border-color: #26B5B5;
}

.meal-name {
  flex: 1;
  color: #303133;
  font-size: 15px;
}

.meal-calorie {
  color: #67c23a;
  font-size: 14px;
}

.ex-name {
  color: #303133;
  font-size: 15px;
}

.ex-duration {
  color: #e6a23c;
  font-size: 14px;
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

.tweak-preview {
  margin-top: 12px;
  padding: 12px 16px;
  background: #f0f9f9;
  border-radius: 10px;
  border: 1px solid #c8e8e8;
}

.tweak-preview-label {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #909399;
}

.tweak-preview-text {
  margin: 0;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.tweak-preview-text strong {
  color: #3aafaf;
}

.tweak-original {
  margin-top: 12px;
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
}

.tweak-section-label {
  margin: 0 0 6px 0;
  font-size: 14px;
  color: #909399;
}

.tweak-original-items {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.tweak-original-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.tweak-original-name {
  color: #303133;
}

.tweak-original-extra {
  color: #909399;
  font-size: 13px;
  margin-left: auto;
}
</style>
