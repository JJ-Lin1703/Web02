<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span class="card-title">标签字典管理</span>
        <div>
          <el-button type="primary" @click="handleAdd">
            <el-icon :size="16"><Plus /></el-icon>
            新增标签
          </el-button>
          <el-button @click="fetchList" class="refresh-btn">
            <el-icon :size="16"><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </template>

    <!-- 分类筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="selectedType" size="default">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button v-for="(name, key) in typeNameMap" :key="key" :value="key">
          {{ name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="filteredList" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          <el-tag>{{ typeNameMap[row.type] || row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="labelName" label="标签名" min-width="160" />
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && dictList.length === 0" description="暂无标签" />
    <el-empty v-if="!loading && dictList.length > 0 && filteredList.length === 0" description="该分类暂无标签" />
  </el-card>

  <!-- 新增 / 编辑对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑标签' : '新增标签'"
    width="500px"
    top="10vh"
    destroy-on-close
    append-to-body
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="分类" prop="type">
        <el-select v-model="form.type" placeholder="请选择标签分类" :disabled="isEdit">
          <el-option
            v-for="(name, key) in typeNameMap"
            :key="key"
            :label="name"
            :value="key"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="标签名" prop="labelName">
        <el-input v-model="form.labelName" placeholder="如：减肥" maxlength="50" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="999" placeholder="数字越小越靠前" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEdit ? '保存修改' : '确认新增' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  getDictLabelList,
  createDictLabel,
  updateDictLabel,
  deleteDictLabel
} from '@/api/user'

const typeNameMap = {
  health_target: '健康目标',
  diet_hobby: '饮食偏好',
  allergy: '过敏信息',
  medical_history: '慢性病史',
  activity_level: '活动水平'
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const dictList = ref([])
const selectedType = ref('')
const filteredList = computed(() => {
  if (!selectedType.value) return dictList.value
  return dictList.value.filter(item => item.type === selectedType.value)
})
const formRef = ref(null)

const form = reactive({
  type: '',
  labelName: '',
  sort: 0
})

const rules = {
  type: [{ required: true, message: '请选择分类', trigger: 'change' }],
  labelName: [{ required: true, message: '请输入标签名', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getDictLabelList()
    dictList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  form.type = ''
  form.labelName = ''
  form.sort = 0
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.type = row.type
  form.labelName = row.labelName
  form.sort = row.sort
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除标签「${row.labelName}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteDictLabel(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch { /* 取消 */ }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      type: form.type,
      labelName: form.labelName,
      sort: form.sort
    }
    if (isEdit.value) {
      await updateDictLabel(editId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createDictLabel(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.filter-bar {
  margin-bottom: 16px;
}
</style>