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
/**
 * @file History.vue
 * @description 历史记录页面，包含体重记录、打卡记录、AI计划历史
 * @author SmartHealth Team
 * @date 2024
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, View, Delete, Check, Sunny } from '@element-plus/icons-vue'
import { getCheckinHistory, getWeightHistory, recordWeight, deleteWeightRecord, updateWeightRecord, getAiPlanHistory, deleteAiPlan } from '@/api/user'
import EmptyState from '@/components/EmptyState.vue'

/** 当前激活的标签页（weight/ai-plan/checkin） */
const activeTab = ref('weight')

/** 体重记录表单 */
const weightForm = reactive({
  weight: null,   // 体重值(kg)
  remark: ''      // 备注
})

/** 体重历史记录列表 */
const weightHistory = ref([])

/** 体重记录提交加载状态 */
const weightLoading = ref(false)

/** 体重表格数据加载状态 */
const weightTableLoading = ref(false)

/** 体重记录分页配置 */
const weightPagination = reactive({
  pageNum: 1,    // 当前页码
  pageSize: 10,  // 每页大小
  total: 0       // 总记录数
})

/** 打卡历史记录列表 */
const checkinHistory = ref([])

/** 打卡记录加载状态 */
const checkinLoading = ref(false)

/** 打卡记录分页配置 */
const checkinPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

/** AI计划历史列表 */
const aiPlanHistory = ref([])

/** AI计划加载状态 */
const aiPlanLoading = ref(false)

/** AI计划排序方式（desc/asc） */
const planSortBy = ref('desc')

/** AI计划分页配置 */
const aiPlanPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

/** 计划详情弹窗可见性 */
const planDetailVisible = ref(false)

/** 当前选中的AI计划 */
const selectedPlan = ref(null)

/** 体重编辑对话框可见性 */
const editDialogVisible = ref(false)

/** 体重编辑加载状态 */
const editLoading = ref(false)

/** 体重编辑表单 */
const editForm = reactive({
  id: null,          // 记录ID
  weight: null,      // 体重值
  recordDate: null   // 记录日期
})

/** 体重搜索表单 */
const searchForm = reactive({
  startDate: null,  // 开始日期
  endDate: null,    // 结束日期
  sortBy: ''        // 排序方式
})

/**
 * 获取体重历史记录
 */
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

/**
 * 提交体重记录
 */
const handleRecordWeight = async () => {
  // 验证体重输入
  if (!weightForm.weight) {
    ElMessage.warning('请输入体重')
    return
  }
  
  // 设置加载状态
  weightLoading.value = true
  
  try {
    // 调用API记录体重
    await recordWeight({
      weight: weightForm.weight,
      remark: weightForm.remark
    })
    
    // 成功提示并清空表单
    ElMessage.success('体重记录成功')
    weightForm.weight = null
    weightForm.remark = ''
    
    // 刷新体重列表
    await fetchWeightHistory()
  } catch {
    // 错误提示已在请求拦截器中统一处理
  } finally {
    weightLoading.value = false
  }
}

/**
 * 删除体重记录
 * @param {number} id - 体重记录ID
 */
const handleDeleteWeight = async (id) => {
  try {
    // 弹出确认对话框
    await ElMessageBox.confirm('确定要删除这条体重记录吗？', '提示', {
      type: 'warning'
    })
    
    // 调用API删除记录
    await deleteWeightRecord(id)
    
    // 成功提示并刷新列表
    ElMessage.success('删除成功')
    await fetchWeightHistory()
  } catch {
    // 取消或错误均由拦截器处理
  }
}

/**
 * 打开体重编辑对话框
 * @param {object} row - 体重记录数据
 */
const handleEditWeight = (row) => {
  // 填充编辑表单
  editForm.id = row.id
  editForm.weight = row.weight
  editForm.recordDate = row.recordDate
  
  // 显示编辑对话框
  editDialogVisible.value = true
}

/**
 * 确认编辑体重记录
 */
const handleConfirmEdit = async () => {
  // 验证体重输入
  if (!editForm.weight) {
    ElMessage.warning('请输入体重')
    return
  }
  
  // 设置加载状态
  editLoading.value = true
  
  try {
    // 调用API更新体重记录
    await updateWeightRecord(editForm.id, { weight: editForm.weight })
    
    // 成功提示并关闭对话框
    ElMessage.success('体重更新成功')
    editDialogVisible.value = false
    
    // 刷新体重列表
    await fetchWeightHistory()
  } catch {
    // 错误提示已在请求拦截器中统一处理
  } finally {
    editLoading.value = false
  }
}

/**
 * 重置搜索条件
 */
const resetSearch = () => {
  // 清空搜索表单
  searchForm.startDate = null
  searchForm.endDate = null
  searchForm.sortBy = ''
  
  // 重置页码
  weightPagination.pageNum = 1
  
  // 重新获取数据
  fetchWeightHistory()
}

/**
 * 获取打卡历史记录
 */
const fetchCheckinHistory = async () => {
  checkinLoading.value = true
  try {
    // 调用API获取打卡历史
    const res = await getCheckinHistory({
      pageNum: checkinPagination.pageNum,
      pageSize: checkinPagination.pageSize
    })
    
    // 更新打卡列表和总数
    checkinHistory.value = res.data?.records || []
    checkinPagination.total = res.data?.total || 0
  } finally {
    checkinLoading.value = false
  }
}

/**
 * 获取AI计划历史记录
 */
const fetchAiPlanHistory = async () => {
  aiPlanLoading.value = true
  try {
    // 调用API获取AI计划历史
    const res = await getAiPlanHistory({
      pageNum: aiPlanPagination.pageNum,
      pageSize: aiPlanPagination.pageSize
    })
    
    // 更新计划列表和总数
    aiPlanHistory.value = res.data?.records || []
    aiPlanPagination.total = res.data?.total || 0
  } finally {
    aiPlanLoading.value = false
  }
}

/**
 * 显示AI计划详情弹窗
 * @param {object} row - AI计划数据
 */
const showPlanDetail = (row) => {
  // 设置选中的计划
  selectedPlan.value = row
  // 显示详情弹窗
  planDetailVisible.value = true
}

/**
 * 删除AI计划
 * @param {number} id - 计划ID
 */
const handleDeletePlan = async (id) => {
  try {
    // 弹出确认对话框
    await ElMessageBox.confirm('确定要删除这个AI计划吗？', '提示', {
      type: 'warning'
    })
    
    // 调用API删除计划
    await deleteAiPlan(id)
    
    // 成功提示并刷新列表
    ElMessage.success('删除成功')
    await fetchAiPlanHistory()
  } catch {
    // 取消或错误均由拦截器处理
  }
}

/**
 * 标签页切换处理
 * @param {string} name - 标签页名称
 */
const handleTabChange = (name) => {
  // 根据标签页名称加载对应数据
  if (name === 'weight') {
    fetchWeightHistory()
  } else if (name === 'checkin') {
    fetchCheckinHistory()
  } else if (name === 'ai-plan') {
    fetchAiPlanHistory()
  }
}

/**
 * 格式化日期（仅日期部分）
 * @param {string} dateStr - 日期字符串
 * @returns {string} 格式化后的日期
 */
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

/**
 * 格式化时间
 * @param {string} timeStr - 时间字符串
 * @returns {string} 格式化后的时间
 */
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

/**
 * 格式化日期时间
 * @param {string} dateStr - 日期时间字符串
 * @returns {string} 格式化后的日期时间
 */
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

/**
 * 格式化计划内容（JSON格式化）
 * @param {string} content - 计划内容字符串
 * @returns {string} 格式化后的内容
 */
const formatPlanContent = (content) => {
  if (!content) return ''
  
  try {
    // 尝试解析JSON并格式化输出
    const obj = JSON.parse(content)
    return JSON.stringify(obj, null, 2)
  } catch {
    // 非JSON格式直接返回原内容
    return content
  }
}

/**
 * 解析AI计划内容为结构化数据（计算属性）
 * 将JSON格式的计划内容解析为前端可用的结构化对象
 */
const planDetailText = computed(() => {
  // 计划内容为空时返回null
  if (!selectedPlan.value?.planContent) return null
  
  try {
    // 解析JSON内容
    const obj = JSON.parse(selectedPlan.value.planContent)
    
    // 验证weeklyPlan字段
    if (!obj.weeklyPlan || !Array.isArray(obj.weeklyPlan)) return null
    
    // 转换为结构化数据
    return {
      summary: obj.summary || null,  // 总结部分（饮食建议、运动建议、健康提示）
      days: obj.weeklyPlan.map(day => ({
        dayName: day.dayName || '',  // 星期名称
        diet: (day.diet || []).map(m => ({
          type: m.type || '',      // 餐次类型（早餐/午餐/晚餐）
          name: m.name || '',      // 食物名称
          calorie: m.calorie || '' // 热量
        })),
        exercise: (day.exercise || []).map(e => ({
          name: e.name || '',      // 运动名称
          duration: e.duration || '' // 运动时长
        }))
      }))
    }
  } catch {
    // 解析失败返回null
    return null
  }
})

/**
 * 页面初始化生命周期钩子
 * 初始化时加载体重历史记录
 */
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
