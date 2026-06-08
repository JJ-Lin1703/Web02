<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="体重记录" name="weight">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>记录体重</span>
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
              <span>体重历史</span>
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
                <el-select v-model="searchForm.sortBy" placeholder="排序方式" style="width: 140px; margin-left: 12px" clearable>
                  <el-option label="默认" value="" />
                  <el-option label="日期升序" value="recordDate" />
                  <el-option label="体重升序" value="weight" />
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
                <el-button type="primary" size="small" @click="handleEditWeight(row)">修改</el-button>
                <el-button type="danger" size="small" @click="handleDeleteWeight(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="签到记录" name="checkin">
        <el-card>
          <template #header>
            <span>签到历史</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckinHistory, getWeightHistory, recordWeight, deleteWeightRecord, updateWeightRecord } from '@/api/user'

const activeTab = ref('weight')

const weightForm = reactive({
  weight: null,
  remark: ''
})

const weightHistory = ref([])
const weightLoading = ref(false)
const weightTableLoading = ref(false)

const checkinHistory = ref([])
const checkinLoading = ref(false)

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
    const params = {}
    if (searchForm.startDate) params.startDate = searchForm.startDate
    if (searchForm.endDate) params.endDate = searchForm.endDate
    if (searchForm.sortBy) params.sortBy = searchForm.sortBy
    const res = await getWeightHistory(params)
    weightHistory.value = res.data || []
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
  fetchWeightHistory()
}

const fetchCheckinHistory = async () => {
  checkinLoading.value = true
  try {
    const res = await getCheckinHistory()
    checkinHistory.value = res.data || []
  } finally {
    checkinLoading.value = false
  }
}

const handleTabChange = (name) => {
  if (name === 'weight') {
    fetchWeightHistory()
  } else if (name === 'checkin') {
    fetchCheckinHistory()
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

.weight-form {
  display: flex;
  align-items: flex-end;
}
</style>