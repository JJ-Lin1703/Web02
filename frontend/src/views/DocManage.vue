<template>
  <div class="doc-manage-container">
    <el-card class="doc-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="20" color="#409eff"><Upload /></el-icon>
          <span class="card-title">向量RAG — 文档管理</span>
        </div>
      </template>

      <!-- 上传区域 -->
      <div class="upload-section">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".txt"
          :on-change="handleFileChange"
          :on-exceed="() => ElMessage.warning('一次只能上传一个文件')"
          :file-list="fileList"
          drag
        >
          <el-icon class="upload-icon" :size="48" color="#409eff"><UploadFilled /></el-icon>
          <div class="upload-text">
            <p>将文件拖到此处，或<em>点击上传</em></p>
            <p class="upload-tip">支持 .txt 格式，上传后将自动解析并向量化存储</p>
          </div>
        </el-upload>

        <el-button
          type="primary"
          :loading="uploading"
          :disabled="!selectedFile"
          @click="handleUpload"
          class="upload-btn"
        >
          {{ uploading ? (uploadStatusText || '解析中...') : '上传并解析' }}
        </el-button>
      </div>

      <!-- 上传加载动画 -->
      <div v-if="uploading" class="loading-container">
        <Vue3Lottie :animationLink="'/loading-animation.json'" :width="180" :height="180" />
        <p class="loading-text">{{ uploadStatusText || '正在处理文档...' }}</p>
        <div class="progress-wrapper">
          <el-progress :percentage="uploadProgress" :stroke-width="6" />
          <span class="progress-text">{{ uploadProgress }}%</span>
        </div>
      </div>

      <!-- 已上传文档列表 -->
      <el-divider v-if="!uploading" />
      <div v-if="!uploading" class="doc-list-header">
        <h4>已上传文档</h4>
        <el-button @click="fetchDocs" :loading="loadingDocs">
          <el-icon :size="16"><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <EmptyState v-if="!uploading && !loadingDocs && docs.length === 0" description="暂无已上传文档" />

      <el-table v-if="!uploading" :data="docs" v-loading="loadingDocs" class="doc-table">
        <el-table-column label="文档名称" min-width="300">
          <template #default="{ row }">
            <el-icon :size="16" color="#409eff"><Document /></el-icon>
            <span style="margin-left: 8px;">{{ row }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
/**
 * @file DocManage.vue
 * @description 文档管理页面，支持TXT文件上传、解析、向量化入库
 * @author SmartHealth Team
 * @date 2024
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, UploadFilled, Refresh, Document } from '@element-plus/icons-vue'
import { Vue3Lottie } from 'vue3-lottie'
import { uploadPdfDoc, listDocRagDocs, getUploadStatus } from '@/api/user'
import EmptyState from '@/components/EmptyState.vue'

// 上传组件引用
const uploadRef = ref(null)
// 文件列表
const fileList = ref([])
// 选中的文件
const selectedFile = ref(null)
// 是否正在上传
const uploading = ref(false)
// 上传状态文本
const uploadStatusText = ref('')
// 上传进度
const uploadProgress = ref(0)
// 文档列表
const docs = ref([])
// 加载状态
const loadingDocs = ref(false)
// 轮询定时器
let pollTimer = null
// 轮询计数
let pollCount = 0

/**
 * 页面初始化
 */
onMounted(() => {
  fetchDocs()
})

/**
 * 文件选择变更处理
 * @param {object} file - 选中的文件对象
 */
const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

/**
 * 启动任务状态轮询
 * @param {string} taskId - 任务ID
 */
const startPollStatus = (taskId) => {
  uploadStatusText.value = '正在向量化入库...'
  uploadProgress.value = 30
  pollCount = 0
  pollTimer = setInterval(async () => {
    pollCount++
    try {
      const res = await getUploadStatus(taskId)
      const status = res.status
      
      // 更新进度
      if (res.progress) {
        uploadProgress.value = res.progress
      } else {
        // 模拟进度：30% -> 90% 逐渐增加
        uploadProgress.value = Math.min(90, 30 + pollCount * 10)
      }
      
      if (status === 'DONE') {
        clearInterval(pollTimer)
        pollTimer = null
        uploadProgress.value = 100
        uploading.value = false
        uploadStatusText.value = ''
        ElMessage.success('文件解析完成')
        fileList.value = []
        selectedFile.value = null
        await fetchDocs()
      } else if (status === 'FAILED') {
        clearInterval(pollTimer)
        pollTimer = null
        uploading.value = false
        uploadStatusText.value = ''
        uploadProgress.value = 0
        ElMessage.error(res.error || '文件解析失败')
        fileList.value = []
        selectedFile.value = null
      } else if (status === 'PROCESSING') {
        uploadStatusText.value = '正在向量化入库...'
      }
    } catch (e) {
      // 轮询出错忽略，继续等
    }
  }, 3000)
}

/**
 * 处理文件上传
 */
const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  uploadStatusText.value = '正在上传文件...'
  uploadProgress.value = 10
  try {
    const res = await uploadPdfDoc(selectedFile.value)
    const data = res.data || res
    if (data.taskId) {
      uploadStatusText.value = '文件上传完成，正在解析中...'
      uploadProgress.value = 25
      startPollStatus(data.taskId)
    } else {
      // 兼容旧版同步响应
      ElMessage.success(data.message || 'TXT 上传并解析成功')
      fileList.value = []
      selectedFile.value = null
      uploading.value = false
      uploadStatusText.value = ''
      uploadProgress.value = 0
      await fetchDocs()
    }
  } catch (error) {
    console.error('上传失败:', error)
    const errMsg = error.response?.data?.error
      || error.response?.data?.message
      || error.message
      || '上传失败'
    ElMessage.error(errMsg)
    uploading.value = false
    uploadStatusText.value = ''
    uploadProgress.value = 0
  }
}

/**
 * 获取文档列表
 */
const fetchDocs = async () => {
  loadingDocs.value = true
  try {
    const res = await listDocRagDocs()
    docs.value = res.docs || []
  } catch (error) {
    ElMessage.error('获取文档列表失败')
  } finally {
    loadingDocs.value = false
  }
}

/**
 * 页面卸载，清理资源
 */
onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.doc-manage-container {
  padding: 0;
}

.doc-card {
  border-radius: 12px;
  border: 1px solid var(--border);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.upload-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.upload-icon {
  margin-bottom: 8px;
}

.upload-text p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}

.upload-tip {
  font-size: 12px !important;
  color: #909399 !important;
}

.upload-btn {
  width: 200px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.loading-text {
  margin-top: 16px;
  font-size: 14px;
  color: #606266;
}

.progress-wrapper {
  width: 300px;
  margin-top: 16px;
  position: relative;
}

.progress-text {
  position: absolute;
  right: 0;
  top: -24px;
  font-size: 12px;
  color: #909399;
}

.doc-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.doc-list-header h4 {
  margin: 0;
  font-size: 15px;
  color: #303133;
}

.doc-table {
  margin-top: 12px;
}
</style>