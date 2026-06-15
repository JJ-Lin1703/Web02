<template>
  <div class="kb-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">RAG 健康知识库管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">
              <el-icon :size="16"><Plus /></el-icon>
              新增知识
            </el-button>
            <el-button @click="fetchList" class="refresh-btn">
              <el-icon :size="16"><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="knowledgeList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="关联标签" width="220">
          <template #default="{ row }">
            <el-tag
              v-for="tag in parseTags(row.tags)"
              :key="tag"
              size="small"
              class="tag-item"
            >{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState v-if="!loading && knowledgeList.length === 0" description="暂无知识条目" />
    </el-card>

    <!-- 新增 / 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑知识条目' : '新增知识条目'"
      width="680px"
      top="5vh"
      destroy-on-close
      append-to-body
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="如：减脂期热量摄入标准" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="知识内容正文..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="关联标签" prop="selectedTags">
          <div class="tag-group-panel">
            <el-checkbox-group v-model="form.selectedTags">
              <div v-for="(tags, groupName) in tagPool" :key="groupName" class="tag-group">
                <div class="tag-group-label">{{ groupName }}</div>
                <div class="tag-group-items">
                  <el-checkbox
                    v-for="tag in tags"
                    :key="tag"
                    :value="tag"
                  >{{ tag }}</el-checkbox>
                </div>
              </div>
            </el-checkbox-group>
          </div>
          <div v-if="form.selectedTags.length > 0" class="selected-tags-preview">
            已选：<el-tag v-for="t in form.selectedTags" :key="t" size="small" class="tag-item">{{ t }}</el-tag>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存修改' : '确认新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  getKnowledgeList,
  createKnowledge,
  updateKnowledge,
  deleteKnowledge,
  getValidTags
} from '@/api/user'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const knowledgeList = ref([])
const tagPool = ref({})
const formRef = ref(null)

const form = reactive({
  title: '',
  content: '',
  selectedTags: []
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 解析逗号分隔的 tags 字符串为数组
const parseTags = (tags) => {
  if (!tags) return []
  return tags.split(/[,，]/).map(t => t.trim()).filter(Boolean)
}

// 加载标签池
const fetchTagPool = async () => {
  try {
    const res = await getValidTags()
    tagPool.value = res.data
  } catch {
    ElMessage.error('加载标签池失败')
  }
}

// 加载列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeList()
    knowledgeList.value = res.data || []
  } catch {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  form.title = ''
  form.content = ''
  form.selectedTags = []
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.title = row.title
  form.content = row.content
  form.selectedTags = parseTags(row.tags)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteKnowledge(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // 取消或错误
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.selectedTags.length === 0) {
    ElMessage.warning('请至少选择一个关联标签')
    return
  }

  submitting.value = true
  try {
    const data = {
      title: form.title,
      content: form.content,
      tags: form.selectedTags.join(',')
    }
    if (isEdit.value) {
      await updateKnowledge(editId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createKnowledge(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchTagPool()
  fetchList()
})
</script>

<style scoped>
.kb-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.tag-item {
  margin: 2px 4px 2px 0;
}

.tag-group-panel {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px 16px;
  max-height: 300px;
  overflow-y: auto;
  width: 100%;
}

.tag-group {
  margin-bottom: 12px;
}

.tag-group:last-child {
  margin-bottom: 0;
}

.tag-group-label {
  font-weight: 600;
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
  padding-left: 2px;
}

.tag-group-items {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
}

.selected-tags-preview {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
</style>