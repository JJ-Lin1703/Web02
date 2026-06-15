import request from '@/utils/request'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

/**
 * 用户登录
 * @param {object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 登录结果
 */
export const userLogin = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 * @param {object} data - 注册信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.email - 邮箱
 * @returns {Promise} 注册结果
 */
export const userRegister = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} 用户信息
 */
export const getUserInfo = () => {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

/**
 * 修改密码
 * @param {object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise} 修改结果
 */
export const changePassword = (data) => {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}

/**
 * 获取所有用户列表（管理员）
 * @returns {Promise} 用户列表
 */
export const getAllUsers = () => {
  return request({
    url: '/admin/users',
    method: 'get'
  })
}

/**
 * 重置用户密码（管理员）
 * @param {number} userId - 用户ID
 * @param {object} data - 密码信息
 * @returns {Promise} 重置结果
 */
export const resetUserPassword = (userId, data) => {
  return request({
    url: `/admin/users/${userId}/reset-password`,
    method: 'post',
    data
  })
}

/**
 * 获取今日打卡状态
 * @returns {Promise} 打卡状态
 */
export const getCheckinStatus = () => {
  return request({
    url: '/checkin/status',
    method: 'get'
  })
}

/**
 * 每日打卡
 * @returns {Promise} 打卡结果
 */
export const dailyCheckin = () => {
  return request({
    url: '/checkin/daily',
    method: 'post'
  })
}

/**
 * 获取健康档案
 * @returns {Promise} 健康档案数据
 */
export const getHealthRecord = () => {
  return request({
    url: '/health-record',
    method: 'get'
  })
}

/**
 * 创建健康档案
 * @param {object} data - 健康档案数据
 * @returns {Promise} 创建结果
 */
export const createHealthRecord = (data) => {
  return request({
    url: '/health-record',
    method: 'post',
    data
  })
}

/**
 * 更新健康档案
 * @param {object} data - 健康档案数据
 * @returns {Promise} 更新结果
 */
export const updateHealthRecord = (data) => {
  return request({
    url: '/health-record',
    method: 'put',
    data
  })
}

/**
 * 检查健康档案是否存在
 * @returns {Promise} 是否存在
 */
export const checkHealthRecordExists = () => {
  return request({
    url: '/health-record/exists',
    method: 'get'
  })
}

/**
 * 获取打卡历史记录
 * @param {object} params - 查询参数
 * @returns {Promise} 打卡历史列表
 */
export const getCheckinHistory = (params = {}) => {
  return request({
    url: '/checkin/history',
    method: 'get',
    params
  })
}

/**
 * 获取体重历史记录
 * @param {object} params - 查询参数
 * @returns {Promise} 体重历史列表
 */
export const getWeightHistory = (params = {}) => {
  return request({
    url: '/weight-record/history',
    method: 'get',
    params
  })
}

/**
 * 记录体重
 * @param {object} data - 体重数据
 * @param {number} data.weight - 体重(kg)
 * @param {string} data.remark - 备注
 * @returns {Promise} 记录结果
 */
export const recordWeight = (data) => {
  return request({
    url: '/weight-record',
    method: 'post',
    data
  })
}

/**
 * 删除体重记录
 * @param {number} id - 记录ID
 * @returns {Promise} 删除结果
 */
export const deleteWeightRecord = (id) => {
  return request({
    url: `/weight-record/${id}`,
    method: 'delete'
  })
}

/**
 * 更新体重记录
 * @param {number} id - 记录ID
 * @param {object} data - 体重数据
 * @returns {Promise} 更新结果
 */
export const updateWeightRecord = (id, data) => {
  return request({
    url: `/weight-record/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取体重趋势
 * @returns {Promise} 体重趋势数据
 */
export const getWeightTrend = () => {
  return request({
    url: '/weight-record/trend',
    method: 'get'
  })
}

/**
 * 生成AI健康计划
 * @returns {Promise} AI计划数据
 */
export const generateAiPlan = () => {
  return request({
    url: '/ai-plan/generate',
    method: 'post',
    timeout: 120000
  })
}

/**
 * 微调AI计划
 * @param {object} data - 微调参数
 * @returns {Promise} 微调后的计划
 */
export const tweakAiPlan = (data) => {
  return request({
    url: '/ai-plan/tweak',
    method: 'post',
    data,
    timeout: 120000
  })
}

/**
 * 获取AI计划历史
 * @param {object} params - 查询参数
 * @returns {Promise} 计划历史列表
 */
export const getAiPlanHistory = (params = {}) => {
  return request({
    url: '/ai-plan/history',
    method: 'get',
    params
  })
}

/**
 * 获取最新AI计划
 * @returns {Promise} 最新计划数据
 */
export const getLatestAiPlan = () => {
  return request({
    url: '/ai-plan/latest',
    method: 'get'
  })
}

/**
 * 删除AI计划
 * @param {number} id - 计划ID
 * @returns {Promise} 删除结果
 */
export const deleteAiPlan = (id) => {
  return request({
    url: `/ai-plan/${id}`,
    method: 'delete'
  })
}

/**
 * 更新AI计划内容
 * @param {number} planId - 计划ID
 * @param {string} planContent - 计划内容(JSON)
 * @returns {Promise} 更新结果
 */
export const updateAiPlanContent = (planId, planContent) => {
  return request({
    url: `/ai-plan/${planId}/content`,
    method: 'put',
    data: { planContent: planContent }
  })
}

/**
 * 保存打卡记录
 * @param {object} data - 打卡数据
 * @returns {Promise} 保存结果
 */
export const saveClockRecord = (data) => {
  return request({
    url: '/clock-record/save',
    method: 'post',
    data
  })
}

/**
 * 获取今日打卡记录
 * @param {number} planId - 计划ID
 * @returns {Promise} 今日打卡记录
 */
export const getTodayClockRecord = (planId) => {
  return request({
    url: '/clock-record/today',
    method: 'get',
    params: { planId }
  })
}

/**
 * 获取周统计数据
 * @returns {Promise} 周统计信息
 */
export const getWeeklyStats = () => {
  return request({
    url: '/clock-record/week',
    method: 'get'
  })
}

/**
 * 获取指定日期范围的打卡记录
 * @param {string} startDate - 开始日期
 * @param {string} endDate - 结束日期
 * @returns {Promise} 打卡记录列表
 */
export const getClockRecordsByRange = (startDate, endDate) => {
  return request({
    url: '/clock-record/range',
    method: 'get',
    params: { startDate, endDate }
  })
}

/**
 * 删除打卡记录
 * @param {number} id - 记录ID
 * @returns {Promise} 删除结果
 */
export const deleteClockRecord = (id) => {
  return request({
    url: `/clock-record/${id}`,
    method: 'delete'
  })
}

/**
 * 导出AI计划为PDF
 * @param {number} [planId] - 计划ID，不传则导出最新计划
 * @returns {Promise} 导出结果
 */
export const exportAiPlanPdf = async (planId = null) => {
  const userStore = useUserStore()
  const url = planId ? `/api/ai-plan/export/${planId}` : '/api/ai-plan/export/latest'
  
  try {
    const response = await axios({
      url: url,
      method: 'get',
      headers: {
        Authorization: `Bearer ${userStore.token}`
      },
      responseType: 'blob',
      timeout: 60000
    })
    
    const contentDisposition = response.headers['content-disposition']
    let filename = '健康计划.pdf'
    if (contentDisposition) {
      const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (match && match[1]) {
        filename = decodeURIComponent(match[1].replace(/['"]/g, ''))
      }
    }
    
    const urlObject = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = urlObject
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(urlObject)
    
    return { success: true, message: '导出成功' }
  } catch (error) {
    console.error('导出PDF失败', error)
    // blob 响应的错误体需要读取为文本才能看到后端错误信息
    let errMsg = '导出失败'
    if (error.response?.data instanceof Blob) {
      try {
        const text = await new Response(error.response.data).text()
        const parsed = JSON.parse(text)
        errMsg = parsed.message || text
      } catch { /* ignore */ }
    }
    throw new Error(errMsg)
  }
}

// ======================== 知识库管理 ===========================

/**
 * 获取知识库列表
 * @returns {Promise} 知识库列表
 */
export const getKnowledgeList = () => {
  return request({
    url: '/knowledge-base/list',
    method: 'get'
  })
}

/**
 * 根据ID获取知识库详情
 * @param {number} id - 知识库ID
 * @returns {Promise} 知识库详情
 */
export const getKnowledgeById = (id) => {
  return request({
    url: `/knowledge-base/${id}`,
    method: 'get'
  })
}

/**
 * 创建知识库条目
 * @param {object} data - 知识库数据
 * @returns {Promise} 创建结果
 */
export const createKnowledge = (data) => {
  return request({
    url: '/knowledge-base',
    method: 'post',
    data
  })
}

/**
 * 更新知识库条目
 * @param {number} id - 知识库ID
 * @param {object} data - 更新数据
 * @returns {Promise} 更新结果
 */
export const updateKnowledge = (id, data) => {
  return request({
    url: `/knowledge-base/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除知识库条目
 * @param {number} id - 知识库ID
 * @returns {Promise} 删除结果
 */
export const deleteKnowledge = (id) => {
  return request({
    url: `/knowledge-base/${id}`,
    method: 'delete'
  })
}

/**
 * 获取有效的标签列表
 * @returns {Promise} 标签列表
 */
export const getValidTags = () => {
  return request({
    url: '/knowledge-base/tags',
    method: 'get'
  })
}

/**
 * 获取标签字典（分组），用于健康档案下拉框和知识库标签池
 * @returns {Promise} 标签字典数据
 */
export const getDictLabelOptions = () => {
  return request({
    url: '/dict-label-options',
    method: 'get'
  })
}

/**
 * 获取标签字典管理端列表
 * @returns {Promise} 标签字典列表
 */
export const getDictLabelList = () => {
  return request({
    url: '/dict-label-options/list',
    method: 'get'
  })
}

/**
 * 新增标签字典
 * @param {object} data - 标签数据
 * @returns {Promise} 创建结果
 */
export const createDictLabel = (data) => {
  return request({
    url: '/dict-label-options',
    method: 'post',
    data
  })
}

/**
 * 修改标签字典
 * @param {number} id - 标签ID
 * @param {object} data - 更新数据
 * @returns {Promise} 更新结果
 */
export const updateDictLabel = (id, data) => {
  return request({
    url: `/dict-label-options/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除标签字典
 * @param {number} id - 标签ID
 * @returns {Promise} 删除结果
 */
export const deleteDictLabel = (id) => {
  return request({
    url: `/dict-label-options/${id}`,
    method: 'delete'
  })
}

// ======================== 健康预警 ===========================

/**
 * 获取健康预警列表
 * @returns {Promise} 预警列表
 */
export const getWarnings = () => {
  return request({
    url: '/warnings',
    method: 'get'
  })
}

/**
 * 获取未读预警数量
 * @returns {Promise} 未读数量
 */
export const getUnreadWarningCount = () => {
  return request({
    url: '/warnings/unread/count',
    method: 'get'
  })
}

// ======================== 向量RAG ===========================

/**
 * 上传TXT文档（multipart文件上传）
 * @param {File} file - 要上传的文件
 * @returns {Promise} 上传结果
 */
export const uploadPdfDoc = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const token = useUserStore().token
  return axios.post('http://localhost:8081/api/doc-rag/upload', formData, {
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      'Content-Type': 'multipart/form-data'
    },
    timeout: 300000
  })
}

/**
 * 文档问答
 * @param {string} question - 用户问题
 * @param {string} sessionId - 会话ID
 * @returns {Promise} 问答结果
 */
export const docAsk = (question, sessionId) => {
  return request({
    url: '/doc-rag/ask',
    method: 'post',
    data: { question, sessionId },
    timeout: 60000
  })
}

/**
 * 获取已上传文档列表
 * @returns {Promise} 文档列表
 */
export const listDocRagDocs = () => {
  return request({
    url: '/doc-rag/docs',
    method: 'get'
  })
}

/**
 * 查询上传任务状态
 * @param {string} taskId - 任务ID
 * @returns {Promise} 任务状态
 */
export const getUploadStatus = (taskId) => {
  return request({
    url: `/doc-rag/upload/status/${taskId}`,
    method: 'get'
  })
}

/**
 * 获取历史会话列表
 * @returns {Promise} 会话列表
 */
export const listConversations = () => {
  return request({
    url: '/doc-rag/conversations',
    method: 'get'
  })
}

/**
 * 标记所有预警为已读
 * @returns {Promise} 标记结果
 */
export const markAllWarningsAsRead = () => {
  return request({
    url: '/warnings/read-all',
    method: 'post'
  })
}

/**
 * 标记单个预警为已读
 * @param {number} id - 预警ID
 * @returns {Promise} 标记结果
 */
export const markWarningAsRead = (id) => {
  return request({
    url: `/warnings/${id}/read`,
    method: 'post'
  })
}

/**
 * 删除预警
 * @param {number} id - 预警ID
 * @returns {Promise} 删除结果
 */
export const deleteWarning = (id) => {
  return request({
    url: `/warnings/${id}`,
    method: 'delete'
  })
}

/**
 * 获取指定会话的消息
 * @param {string} sessionId - 会话ID
 * @returns {Promise} 会话消息
 */
export const getConversation = (sessionId) => {
  return request({
    url: `/doc-rag/conversations/${sessionId}`,
    method: 'get'
  })
}
