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
          <el-table :data="aiPlanHistory" stripe v-loading="aiPlanLoading" empty-text="暂无AI计划记录">
            <el-table-column prop="planTitle" label="计划标题" width="200" />
            <el-table-column prop="versionNo" label="版本" width="80" />
            <el-table-column prop="totalCalorie" label="每日热量(kcal)" width="120" />
            <el-table-column prop="generateTime" label="生成时间" width="200">
              <template #default="{ row }">
                {{ formatDateTime(row.generateTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link @click="showPlanDetail(row)">查看</el-button>
                <el-button type="danger" link @click="handleDeletePlan(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
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
          <el-table :data="checkinHistory" stripe v-loading="checkinLoading" empty-text="暂无签到记录">
            <el-table-column prop="checkinDate" label="签到日期" width="150">
              <template #default="{ row }">
                {{ formatDate(row.checkinDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="checkinTime" label="签到时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.checkinTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="continuousDays" label="连续签到天数" width="150" />
          </el-table>
          
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
          <pre>{{ formatPlanContent(selectedPlan?.planContent) }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="planDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckinHistory, getWeightHistory, recordWeight, deleteWeightRecord, updateWeightRecord, getAiPlanHistory, deleteAiPlan } from '@/api/user'

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
</style>
