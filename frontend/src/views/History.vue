<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="体重记录" name="weight">
        <el-card>
          <template #header>
            <div class="card-header">
<span class="card-title">记录体重</span>
            </div>
          </template>
          <el-form :inline="true" :model="weightForm" class="weight-form">
            <el-form-item label="体重(kg)">
              <el-input v-model.number="weightForm.weight" type="number" step="0.1" placeholder="请输入体重" style="width: 200px" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="weightForm.remark" placeholder="可选备注" style="width: 200px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRecordWeight" :loading="weightLoading">记录</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 16px">
          <template #header>
            <div class="card-header">
<span class="card-title">体重历史</span>
              <div class="search-bar">
                <el-date-picker
                  v-model="searchForm.startDate"
                  type="date"
                  placeholder="开始日期"
                  value-format="YYYY-MM-DD"
                  style="width: 150px"
                />
                <span style="margin: 0 8px">至</span>
                <el-date-picker
                  v-model="searchForm.endDate"
                  type="date"
                  placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  style="width: 150px"
                />
                <el-select v-model="searchForm.sortBy" placeholder="排序方式" style="width: 140px; margin-left: 12px" clearable @change="fetchWeightHistory">
                  <el-option label="默认(日期倒序)" value="" />
                  <el-option label="日期升序" value="recordDate" />
                  <el-option label="日期倒序" value="recordDateDesc" />
                  <el-option label="体重升序" value="weight" />
                  <el-option label="体重降序" value="weightDesc" />
                </el-select>
                <el-button type="primary" @click="fetchWeightHistory" style="margin-left: 12px">搜索</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </div>
            </div>
          </template>
          <el-table :data="weightHistory" stripe v-loading="weightTableLoading" empty-text="暂无体重记录">
            <el-table-column prop="recordDate" label="日期" width="150">
              <template #default="{ row }">
                {{ formatDate(row.recordDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="weight" label="体重(kg)" width="150" />
            <el-table-column prop="remark" label="备注" min-width="200">
              <template #default="{ row }">
                {{ row.remark || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
<el-button type="primary" @click="handleEditWeight(row)">修改</el-button>
                <el-button type="danger" @click="handleDeleteWeight(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="weightPagination.pageNum"
              v-model:page-size="weightPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="weightPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchWeightHistory"
              @current-change="fetchWeightHistory"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="AI计划历史" name="ai-plan">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="card-title">AI计划历史</span>
              <div class="search-bar">
                <el-select v-model="planSortBy" placeholder="排序方式" style="width: 140px" clearable @change="fetchAiPlanHistory">
                  <el-option label="生成时间倒序" value="desc" />
                  <el-option label="生成时间正序" value="asc" />
                </el-select>
              </div>
            </div>
          </template>

          <div v-loading="aiPlanLoading" class="plan-cards-wrapper">
            <EmptyState v-if="!aiPlanLoading && aiPlanHistory.length === 0" description="暂无AI计划记录" />
            <div v-else class="plan-cards-grid">
              <div v-for="item in aiPlanHistory" :key="item.id" class="plan-card-item">
                <div class="plan-card-header">
                  <div class="plan-card-icon">
                    <el-icon :size="24" color="#fff"><Document /></el-icon>
                  </div>
                  <div class="plan-card-title-wrap">
                    <div class="plan-card-title">{{ item.planTitle }}</div>
                    <div class="plan-card-version">版本 V{{ item.versionNo }}</div>
                  </div>
                </div>
                <div class="plan-card-body">
                  <div class="plan-info-row">
                    <span class="info-label">每日热量</span>
                    <span class="info-value calorie-value">{{ item.totalCalorie }} <small>kcal</small></span>
                  </div>
                  <div class="plan-info-row">
                    <span class="info-label">生成时间</span>
                    <span class="info-value">{{ formatDateTime(item.generateTime) }}</span>
                  </div>
                </div>
                <div class="plan-card-footer">
                  <el-button class="plan-btn plan-btn-primary" @click="showPlanDetail(item)">
                    <el-icon :size="14"><View /></el-icon>
                    <span>查看详情</span>
                  </el-button>
                  <el-button class="plan-btn plan-btn-danger" @click="handleDeletePlan(item.id)">
                    <el-icon :size="14"><Delete /></el-icon>
                    <span>删除</span>
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="aiPlanPagination.pageNum"
              v-model:page-size="aiPlanPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="aiPlanPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchAiPlanHistory"
              @current-change="fetchAiPlanHistory"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="签到记录" name="checkin">
        <el-card>
          <template #header>
            <span class="card-title">签到历史</span>
          </template>

          <div v-loading="checkinLoading" class="checkin-cards-wrapper">
            <EmptyState v-if="!checkinLoading && checkinHistory.length === 0" description="暂无签到记录" />
            <div v-else class="checkin-timeline">
              <div v-for="item in checkinHistory" :key="item.id" class="checkin-item">
                <div class="checkin-dot">
                  <el-icon :size="14" color="#fff"><Check /></el-icon>
                </div>
                <div class="checkin-card">
                  <div class="checkin-card-top">
                    <div class="checkin-date">{{ formatDate(item.checkinDate) }}</div>
                    <div class="checkin-time">{{ formatTime(item.checkinTime) }}</div>
                  </div>
                  <div class="checkin-card-bottom">
                    <div class="checkin-streak">
                      <el-icon :size="16" color="#f56c6c"><Sunny /></el-icon>
                      <span class="streak-number">{{ item.continuousDays }}</span>
                      <span class="streak-label">连续签到</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="checkinPagination.pageNum"
              v-model:page-size="checkinPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="checkinPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchCheckinHistory"
              @current-change="fetchCheckinHistory"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="editDialogVisible" title="修改体重" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="日期">
          <span>{{ formatDate(editForm.recordDate) }}</span>
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input v-model.number="editForm.weight" type="number" step="0.1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmEdit" :loading="editLoading">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="planDetailVisible" title="AI计划详情" width="600px">
      <div class="plan-detail-content">
        <div class="plan-info">
          <h3>{{ selectedPlan?.planTitle }}</h3>
          <div class="plan-meta">
            <span>版本：V{{ selectedPlan?.versionNo }}</span>
            <span>每日热量：{{ selectedPlan?.totalCalorie }} kcal</span>
            <span>生成时间：{{ formatDateTime(selectedPlan?.generateTime) }}</span>
          </div>
        </div>
        <div class="plan-content">
          <div v-if="planDetailText" class="plan-detail-text">
            <!-- 总结区域 -->
            <div v-if="planDetailText.summary" class="detail-summary">
              <div v-if="planDetailText.summary.diet && planDetailText.summary.diet.length" class="summary-block">
                <h3 class="summary-title">饮食建议</h3>
                <ul class="summary-list">
                  <li v-for="(item, i) in planDetailText.summary.diet" :key="'d'+i">{{ item }}</li>
                </ul>
              </div>
              <div v-if="planDetailText.summary.exercise && planDetailText.summary.exercise.length" class="summary-block">
                <h3 class="summary-title">运动建议</h3>
                <ul class="summary-list">
                  <li v-for="(item, i) in planDetailText.summary.exercise" :key="'e'+i">{{ item }}</li>
                </ul>
              </div>
              <div v-if="planDetailText.summary.tips && planDetailText.summary.tips.length" class="summary-block">
                <h3 class="summary-title">健康提示</h3>
                <ul class="summary-list">
                  <li v-for="(item, i) in planDetailText.summary.tips" :key="'t'+i">{{ item }}</li>
                </ul>
              </div>
            </div>
            <!-- 每日计划 -->
            <h2 class="detail-section-title">一周详细计划</h2>
            <div v-for="(day, dIdx) in planDetailText.days" :key="dIdx" class="detail-day">
              <h3 class="detail-day-title">{{ day.dayName }}</h3>
              <div class="detail-section">
                <h4>饮食安排</h4>
                <div v-for="(meal, mIdx) in day.diet" :key="mIdx" class="detail-item">
                  <span class="detail-label">{{ meal.type }}</span>
                  <span class="detail-value">{{ meal.name }}（{{ meal.calorie }}）</span>
                </div>
              </div>
              <div class="detail-section">
                <h4>运动安排</h4>
                <div v-for="(ex, eIdx) in day.exercise" :key="eIdx" class="detail-item">
                  <span class="detail-value">{{ ex.name }}：{{ ex.duration }}</span>
                </div>
              </div>
            </div>
          </div>
          <pre v-else>{{ formatPlanContent(selectedPlan?.planContent) }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="planDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, View, Delete, Check, Sunny } from '@element-plus/icons-vue'
import { getCheckinHistory, getWeightHistory, recordWeight, deleteWeightRecord, updateWeightRecord, getAiPlanHistory, deleteAiPlan } from '@/api/user'
import EmptyState from '@/components/EmptyState.vue'

const activeTab = ref('weight')

const weightForm = reactive({
  weight: null,
  remark: ''
})

const weightHistory = ref([])
const weightLoading = ref(false)
const weightTableLoading = ref(false)
const weightPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const checkinHistory = ref([])
const checkinLoading = ref(false)
const checkinPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const aiPlanHistory = ref([])
const aiPlanLoading = ref(false)
const planSortBy = ref('desc')
const aiPlanPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const planDetailVisible = ref(false)
const selectedPlan = ref(null)
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  id: null,
  weight: null,
  recordDate: null
})

const searchForm = reactive({
  startDate: null,
  endDate: null,
  sortBy: ''
})

const fetchWeightHistory = async () => {
  weightTableLoading.value = true
  try {
    const params = {
      pageNum: weightPagination.pageNum,
      pageSize: weightPagination.pageSize
    }
    if (searchForm.startDate) params.startDate = searchForm.startDate
    if (searchForm.endDate) params.endDate = searchForm.endDate
    if (searchForm.sortBy) params.sortBy = searchForm.sortBy
    const res = await getWeightHistory(params)
    weightHistory.value = res.data?.records || []
    weightPagination.total = res.data?.total || 0
  } finally {
    weightTableLoading.value = false
  }
}

const handleRecordWeight = async () => {
  if (!weightForm.weight) {
    ElMessage.warning('请输入体重')
    return
  }
  weightLoading.value = true
  try {
    await recordWeight({
      weight: weightForm.weight,
      remark: weightForm.remark
    })
    ElMessage.success('体重记录成功')
    weightForm.weight = null
    weightForm.remark = ''
    await fetchWeightHistory()
  } catch {
    // 错误提示已在请求拦截器中统一处理
  } finally {
    weightLoading.value = false
  }
}

const handleDeleteWeight = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条体重记录吗？', '提示', {
      type: 'warning'
    })
    await deleteWeightRecord(id)
    ElMessage.success('删除成功')
    await fetchWeightHistory()
  } catch {
    // 取消或错误均由拦截器处理
  }
}

const handleEditWeight = (row) => {
  editForm.id = row.id
  editForm.weight = row.weight
  editForm.recordDate = row.recordDate
  editDialogVisible.value = true
}

const handleConfirmEdit = async () => {
  if (!editForm.weight) {
    ElMessage.warning('请输入体重')
    return
  }
  editLoading.value = true
  try {
    await updateWeightRecord(editForm.id, { weight: editForm.weight })
    ElMessage.success('体重更新成功')
    editDialogVisible.value = false
    await fetchWeightHistory()
  } catch {
    // 错误提示已在请求拦截器中统一处理
  } finally {
    editLoading.value = false
  }
}

const resetSearch = () => {
  searchForm.startDate = null
  searchForm.endDate = null
  searchForm.sortBy = ''
  weightPagination.pageNum = 1
  fetchWeightHistory()
}

const fetchCheckinHistory = async () => {
  checkinLoading.value = true
  try {
    const res = await getCheckinHistory({
      pageNum: checkinPagination.pageNum,
      pageSize: checkinPagination.pageSize
    })
    checkinHistory.value = res.data?.records || []
    checkinPagination.total = res.data?.total || 0
  } finally {
    checkinLoading.value = false
  }
}

const fetchAiPlanHistory = async () => {
  aiPlanLoading.value = true
  try {
    const res = await getAiPlanHistory({
      pageNum: aiPlanPagination.pageNum,
      pageSize: aiPlanPagination.pageSize
    })
    aiPlanHistory.value = res.data?.records || []
    aiPlanPagination.total = res.data?.total || 0
  } finally {
    aiPlanLoading.value = false
  }
}

const showPlanDetail = (row) => {
  selectedPlan.value = row
  planDetailVisible.value = true
}

const handleDeletePlan = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个AI计划吗？', '提示', {
      type: 'warning'
    })
    await deleteAiPlan(id)
    ElMessage.success('删除成功')
    await fetchAiPlanHistory()
  } catch {
    // 取消或错误均由拦截器处理
  }
}
const handleTabChange = (name) => {
  if (name === 'weight') {
    fetchWeightHistory()
  } else if (name === 'checkin') {
    fetchCheckinHistory()
  } else if (name === 'ai-plan') {
    fetchAiPlanHistory()
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const formatPlanContent = (content) => {
  if (!content)
    return '';
  
  try {
    const obj = JSON.parse(content);
    return JSON.stringify(obj, null, 2);
  } catch {
    return content;
  }
}

const planDetailText = computed(() => {
  if (!selectedPlan.value?.planContent) return null
  try {
    const obj = JSON.parse(selectedPlan.value.planContent)
    if (!obj.weeklyPlan || !Array.isArray(obj.weeklyPlan)) return null
    return {
      summary: obj.summary || null,
      days: obj.weeklyPlan.map(day => ({
        dayName: day.dayName || '',
        diet: (day.diet || []).map(m => ({
          type: m.type || '',
          name: m.name || '',
          calorie: m.calorie || ''
        })),
        exercise: (day.exercise || []).map(e => ({
          name: e.name || '',
          duration: e.duration || ''
        }))
      }))
    }
  } catch {
    return null
  }
})
onMounted(() => {
  fetchWeightHistory()
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

.card-title {
  font-size: 22px;
  font-weight: 600;
}

.weight-form {
  display: flex;
  align-items: flex-end;
}

.search-bar {
  font-size: 16px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.plan-detail-content {
  padding: 10px;
}

.plan-info {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.plan-info h3 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 22px;
}

.plan-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 17px;
}

.plan-content {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.plan-content pre {
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  padding: 0;
  font-size: 18px;
  line-height: 1.8;
  color: #303133;
}

.plan-detail-text {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.detail-day {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-day:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.detail-day-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #409eff;
  font-weight: 600;
}

.detail-section {
  margin-bottom: 10px;
}

.detail-section h4 {
  margin: 0 0 6px 0;
  font-size: 14px;
  color: #606266;
  font-weight: 600;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  margin-bottom: 3px;
  background: #f8fafc;
  border-radius: 6px;
  font-size: 13px;
}

.detail-label {
  display: inline-block;
  min-width: 44px;
  padding: 2px 8px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  margin-right: 10px;
  text-align: center;
}

.detail-value {
  color: #303133;
  line-height: 1.5;
}

.detail-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f0f9ff;
  border-radius: 10px;
  border: 1px solid #d0e8ff;
}

.summary-block {
  flex: 1;
  min-width: 180px;
}

.summary-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
  padding-bottom: 4px;
  border-bottom: 2px solid #b3d8ff;
}

.summary-list {
  margin: 0;
  padding-left: 16px;
  list-style: disc;
}

.summary-list li {
  margin-bottom: 4px;
  font-size: 13px;
  color: #555;
  line-height: 1.5;
}

.detail-section-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #303133;
  font-weight: 700;
  text-align: center;
  padding-bottom: 10px;
  border-bottom: 2px solid #e8e8e8;
}

/* ============ AI计划历史卡片样式 ============ */
.plan-cards-wrapper {
  min-height: 200px;
  padding: 4px 0;
}

.plan-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.plan-card-item {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-card-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.12);
  transform: translateY(-2px);
}

.plan-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #ebeef5;
}

.plan-card-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.plan-card-title-wrap {
  flex: 1;
  min-width: 0;
}

.plan-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.plan-card-version {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.plan-card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.plan-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.calorie-value {
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
}

.calorie-value small {
  font-size: 11px;
  font-weight: normal;
  color: #909399;
}

.plan-card-footer {
  display: flex;
  justify-content: space-around;
  padding-top: 8px;
  border-top: 1px solid #f5f7fa;
}

.plan-btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 32px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
}

.plan-btn-primary {
  background: #ecf5ff;
  color: #409eff;
}

.plan-btn-primary:hover {
  background: #409eff;
  color: #fff;
}

.plan-btn-danger {
  background: #fef0f0;
  color: #f56c6c;
}

.plan-btn-danger:hover {
  background: #f56c6c;
  color: #fff;
}

/* ============ 签到记录时间轴样式 ============ */
.checkin-cards-wrapper {
  min-height: 200px;
  padding: 8px 0;
}

.checkin-timeline {
  position: relative;
  padding-left: 8px;
}

.checkin-timeline::before {
  content: '';
  position: absolute;
  left: 15px;
  top: 12px;
  bottom: 12px;
  width: 2px;
  background: linear-gradient(180deg, #67c23a 0%, #e1f3d8 100%);
}

.checkin-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 8px 0;
}

.checkin-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  z-index: 1;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.checkin-card {
  flex: 1;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 14px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.25s ease;
}

.checkin-card:hover {
  background: #f0f9eb;
  border-color: #67c23a;
  transform: translateX(4px);
}

.checkin-card-top {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.checkin-date {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.checkin-time {
  font-size: 12px;
  color: #909399;
}

.checkin-card-bottom {
  display: flex;
  align-items: center;
}

.checkin-streak {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fff;
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid #fde2e2;
}

.streak-number {
  font-size: 16px;
  font-weight: 700;
  color: #f56c6c;
}

.streak-label {
  font-size: 12px;
  color: #909399;
}
</style>
