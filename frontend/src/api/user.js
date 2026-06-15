import request from '@/utils/request'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

export const userLogin = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const userRegister = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export const getUserInfo = () => {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

export const changePassword = (data) => {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}

export const getAllUsers = () => {
  return request({
    url: '/admin/users',
    method: 'get'
  })
}

export const resetUserPassword = (userId, data) => {
  return request({
    url: `/admin/users/${userId}/reset-password`,
    method: 'post',
    data
  })
}

export const getCheckinStatus = () => {
  return request({
    url: '/checkin/status',
    method: 'get'
  })
}

export const dailyCheckin = () => {
  return request({
    url: '/checkin/daily',
    method: 'post'
  })
}

export const getHealthRecord = () => {
  return request({
    url: '/health-record',
    method: 'get'
  })
}

export const createHealthRecord = (data) => {
  return request({
    url: '/health-record',
    method: 'post',
    data
  })
}

export const updateHealthRecord = (data) => {
  return request({
    url: '/health-record',
    method: 'put',
    data
  })
}

export const checkHealthRecordExists = () => {
  return request({
    url: '/health-record/exists',
    method: 'get'
  })
}

export const getCheckinHistory = (params = {}) => {
  return request({
    url: '/checkin/history',
    method: 'get',
    params
  })
}

export const getWeightHistory = (params = {}) => {
  return request({
    url: '/weight-record/history',
    method: 'get',
    params
  })
}

export const recordWeight = (data) => {
  return request({
    url: '/weight-record',
    method: 'post',
    data
  })
}

export const deleteWeightRecord = (id) => {
  return request({
    url: `/weight-record/${id}`,
    method: 'delete'
  })
}

export const updateWeightRecord = (id, data) => {
  return request({
    url: `/weight-record/${id}`,
    method: 'put',
    data
  })
}

export const getWeightTrend = () => {
  return request({
    url: '/weight-record/trend',
    method: 'get'
  })
}

export const generateAiPlan = () => {
  return request({
    url: '/ai-plan/generate',
    method: 'post',
    timeout: 120000
  })
}

export const tweakAiPlan = (data) => {
  return request({
    url: '/ai-plan/tweak',
    method: 'post',
    data,
    timeout: 120000
  })
}

export const getAiPlanHistory = (params = {}) => {
  return request({
    url: '/ai-plan/history',
    method: 'get',
    params
  })
}

export const getLatestAiPlan = () => {
  return request({
    url: '/ai-plan/latest',
    method: 'get'
  })
}

export const deleteAiPlan = (id) => {
  return request({
    url: `/ai-plan/${id}`,
    method: 'delete'
  })
}

export const updateAiPlanContent = (planId, planContent) => {
  return request({
    url: `/ai-plan/${planId}/content`,
    method: 'put',
    data: { planContent: planContent }
  })
}

export const saveClockRecord = (data) => {
  return request({
    url: '/clock-record/save',
    method: 'post',
    data
  })
}

export const getTodayClockRecord = (planId) => {
  return request({
    url: '/clock-record/today',
    method: 'get',
    params: { planId }
  })
}

export const getWeeklyStats = () => {
  return request({
    url: '/clock-record/week',
    method: 'get'
  })
}

export const getClockRecordsByRange = (startDate, endDate) => {
  return request({
    url: '/clock-record/range',
    method: 'get',
    params: { startDate, endDate }
  })
}

export const deleteClockRecord = (id) => {
  return request({
    url: `/clock-record/${id}`,
    method: 'delete'
  })
}

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

export const getKnowledgeList = () => {
  return request({
    url: '/knowledge-base/list',
    method: 'get'
  })
}

export const getKnowledgeById = (id) => {
  return request({
    url: `/knowledge-base/${id}`,
    method: 'get'
  })
}

export const createKnowledge = (data) => {
  return request({
    url: '/knowledge-base',
    method: 'post',
    data
  })
}

export const updateKnowledge = (id, data) => {
  return request({
    url: `/knowledge-base/${id}`,
    method: 'put',
    data
  })
}

export const deleteKnowledge = (id) => {
  return request({
    url: `/knowledge-base/${id}`,
    method: 'delete'
  })
}

export const getValidTags = () => {
  return request({
    url: '/knowledge-base/tags',
    method: 'get'
  })
}

/** 获取标签字典（分组），用于健康档案下拉框和知识库标签池 */
export const getDictLabelOptions = () => {
  return request({
    url: '/dict-label-options',
    method: 'get'
  })
}

/** 标签字典管理端列表 */
export const getDictLabelList = () => {
  return request({
    url: '/dict-label-options/list',
    method: 'get'
  })
}

/** 新增标签字典 */
export const createDictLabel = (data) => {
  return request({
    url: '/dict-label-options',
    method: 'post',
    data
  })
}

/** 修改标签字典 */
export const updateDictLabel = (id, data) => {
  return request({
    url: `/dict-label-options/${id}`,
    method: 'put',
    data
  })
}

/** 删除标签字典 */
export const deleteDictLabel = (id) => {
  return request({
    url: `/dict-label-options/${id}`,
    method: 'delete'
  })
}

// ======================== 健康预警 ===========================

export const getWarnings = () => {
  return request({
    url: '/warnings',
    method: 'get'
  })
}

// ======================== 向量RAG ===========================

/** 上传TXT文档（multipart文件上传） */
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

/** 文档问答 */
export const docAsk = (question, sessionId) => {
  return request({
    url: '/doc-rag/ask',
    method: 'post',
    data: { question, sessionId },
    timeout: 60000
  })
}

/** 获取已上传文档列表 */
export const listDocRagDocs = () => {
  return request({
    url: '/doc-rag/docs',
    method: 'get'
  })
}

/** 查询上传任务状态 */
export const getUploadStatus = (taskId) => {
  return request({
    url: `/doc-rag/upload/status/${taskId}`,
    method: 'get'
  })
}

export const getUnreadWarningCount = () => {
  return request({
    url: '/warnings/unread/count',
    method: 'get'
  })
}

/** 获取历史会话列表 */
export const listConversations = () => {
  return request({
    url: '/doc-rag/conversations',
    method: 'get'
  })
}

export const markAllWarningsAsRead = () => {
  return request({
    url: '/warnings/read-all',
    method: 'post'
  })
}

export const markWarningAsRead = (id) => {
  return request({
    url: `/warnings/${id}/read`,
    method: 'post'
  })
}

export const deleteWarning = (id) => {
  return request({
    url: `/warnings/${id}`,
    method: 'delete'
  })
}

/** 获取指定会话的消息 */
export const getConversation = (sessionId) => {
  return request({
    url: `/doc-rag/conversations/${sessionId}`,
    method: 'get'
  })
}
