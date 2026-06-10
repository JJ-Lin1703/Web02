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
          {{ uploading ? '解析中...' : '上传并解析' }}
        </el-button>
      </div>

      <!-- 已上传文档列表 -->
      <el-divider />
      <div class="doc-list-header">
        <h4>已上传文档</h4>
        <el-button @click="fetchDocs" :loading="loadingDocs">
          <el-icon :size="16"><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-empty v-if="!loadingDocs && docs.length === 0" description="暂无已上传文档" />

      <el-table v-else :data="docs" v-loading="loadingDocs" class="doc-table">
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, UploadFilled, Refresh, Document } from '@element-plus/icons-vue'
import { uploadPdfDoc, listDocRagDocs } from '@/api/user'

const uploadRef = ref(null)
const fileList = ref([])
const selectedFile = ref(null)
const uploading = ref(false)
const docs = ref([])
const loadingDocs = ref(false)

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  try {
    const res = await uploadPdfDoc(selectedFile.value)
    ElMessage.success(res.data.message || 'PDF 上传并解析成功')
    fileList.value = []
    selectedFile.value = null
    await fetchDocs()
  } catch (error) {
    console.error('上传失败详情:', error)
    const errMsg = error.response?.data?.error 
      || error.response?.data?.message
      || error.message 
      || '上传失败'
    ElMessage.error(errMsg)
  } finally {
    uploading.value = false
  }
}

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