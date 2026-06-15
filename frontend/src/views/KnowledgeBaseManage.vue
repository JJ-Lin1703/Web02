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

/** 列表加载状态 */
const loading = ref(false)

/** 表单提交状态 */
const submitting = ref(false)

/** 对话框可见性 */
const dialogVisible = ref(false)

/** 是否为编辑模式 */
const isEdit = ref(false)

/** 当前编辑的知识条目ID */
const editId = ref(null)

/** 知识条目列表 */
const knowledgeList = ref([])

/** 标签池（分组标签） */
const tagPool = ref({})

/** 表单引用 */
const formRef = ref(null)

/** 表单数据 */
const form = reactive({
  title: '',           // 标题
  content: '',         // 内容
  selectedTags: []     // 选中的标签列表
})

/** 表单验证规则 */
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

/**
 * 解析逗号分隔的 tags 字符串为数组
 * @param {string} tags - 逗号分隔的标签字符串
 * @returns {string[]} 标签数组
 */
const parseTags = (tags) => {
  if (!tags) return []
  return tags.split(/[,，]/).map(t => t.trim()).filter(Boolean)
}

/**
 * 加载标签池
 * 从后端获取可用的标签分组数据
 */
const fetchTagPool = async () => {
  try {
    const res = await getValidTags()
    tagPool.value = res.data
  } catch {
    ElMessage.error('加载标签池失败')
  }
}

/**
 * 加载知识列表
 * 从后端获取所有知识条目
 */
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeList()
    knowledgeList.value = res.data || []
  } catch {
    // 拦截器已处理错误
  } finally {
    loading.value = false
  }
}

/**
 * 新增知识条目
 * 重置表单并打开对话框
 */
const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  form.title = ''
  form.content = ''
  form.selectedTags = []
  dialogVisible.value = true
}

/**
 * 编辑知识条目
 * @param {object} row - 知识条目数据
 */
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.title = row.title
  form.content = row.content
  form.selectedTags = parseTags(row.tags)
  dialogVisible.value = true
}

/**
 * 删除知识条目
 * @param {object} row - 知识条目数据
 */
const handleDelete = async (row) => {
  try {
    // 弹出确认对话框
    await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    
    // 调用删除API
    await deleteKnowledge(row.id)
    ElMessage.success('删除成功')
    
    // 刷新列表
    fetchList()
  } catch {
    // 用户取消或删除失败
  }
}

/**
 * 提交表单（新增或编辑）
 */
const handleSubmit = async () => {
  // 表单验证
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 验证标签选择
  if (form.selectedTags.length === 0) {
    ElMessage.warning('请至少选择一个关联标签')
    return
  }

  submitting.value = true
  try {
    const data = {
      title: form.title,
      content: form.content,
      tags: form.selectedTags.join(',')  // 将标签数组转换为逗号分隔字符串
    }

    // 根据模式调用不同API
    if (isEdit.value) {
      await updateKnowledge(editId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createKnowledge(data)
      ElMessage.success('新增成功')
    }

    // 关闭对话框并刷新列表
    dialogVisible.value = false
    fetchList()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 页面初始化生命周期钩子
 */
onMounted(() => {
  fetchTagPool()  // 加载标签池
  fetchList()     // 加载知识列表
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