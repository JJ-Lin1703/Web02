<template>
  <div class="admin-container">
    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="用户管理" name="users">
        <el-card class="admin-card">
          <template #header>
            <div class="card-header">
              <el-icon class="card-icon" :size="20" color="#409eff"><Management /></el-icon>
              <span class="card-title">用户管理</span>
              <el-button type="primary" @click="fetchUsers" class="refresh-btn">
                <el-icon :size="16"><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>

          <el-table :data="userList" v-loading="loading" class="user-table">
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="username" label="用户名" min-width="150">
              <template #default="{ row }">
                <div class="username-cell">
                  <el-icon :size="18" color="#409eff"><User /></el-icon>
                  <span>{{ row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="role" label="角色" width="130">
              <template #default="{ row }">
                <el-tag :type="row.role === 1 ? 'danger' : 'info'">
                  {{ row.role === 1 ? '管理员' : '普通用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="loginFailCount" label="登录失败次数" width="160">
              <template #default="{ row }">
                <span :class="row.loginFailCount >= 5 ? 'fail-count-warning' : 'fail-count-normal'">
                  {{ row.loginFailCount }} 次
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="200" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" class="action-btn reset-btn" @click="handleResetPassword(row)">
                  <el-icon :size="14"><Key /></el-icon>
                  重置密码
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-dialog v-model="resetDialogVisible" title="重置密码" width="520px" top="15%">
          <el-form :model="resetForm" :rules="resetRules" ref="resetFormRef" label-width="100px">
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="resetForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="resetDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmResetPassword">确认重置</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <el-tab-pane label="知识库管理" name="knowledge">
        <KnowledgeBaseManage />
      </el-tab-pane>
      <el-tab-pane label="标签字典管理" name="dict">
        <DictLabelManage />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Management, Refresh, User, Key } from '@element-plus/icons-vue'
import { getAllUsers, resetUserPassword } from '@/api/user'
import KnowledgeBaseManage from './KnowledgeBaseManage.vue'
import DictLabelManage from './DictLabelManage.vue'

const activeTab = ref('users')
const loading = ref(false)
const userList = ref([])
const resetDialogVisible = ref(false)
const resetFormRef = ref(null)
const currentUserId = ref(null)

const resetForm = reactive({
  newPassword: ''
})

const resetRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getAllUsers()
    userList.value = res.data
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleResetPassword = (row) => {
  currentUserId.value = row.id
  resetForm.newPassword = ''
  resetDialogVisible.value = true
}

const confirmResetPassword = async () => {
  try {
    await resetFormRef.value.validate()
    await ElMessageBox.confirm('确定要重置该用户密码吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await resetUserPassword(currentUserId.value, { newPassword: resetForm.newPassword })
    ElMessage.success('密码重置成功')
    resetDialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败', error)
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-container {
  padding: 0;
}

.admin-tabs {
  margin-top: -8px;
}

.admin-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
  background: #fff;
  border-radius: 12px;
  padding: 0 20px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}

.admin-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.admin-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
}

.admin-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-icon {
  flex-shrink: 0;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  flex: 1;
}

.refresh-btn {
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 8px;
}

.admin-card :deep(.el-card__body) {
  padding: 24px;
}

.user-table {
  width: 100%;
}

.user-table :deep(.el-table__header-wrapper) {
  border-radius: 12px 12px 0 0;
  overflow: hidden;
}

.user-table :deep(.el-table__body-wrapper) {
  border-radius: 0 0 12px 12px;
  overflow: hidden;
}

.user-table :deep(.el-table th) {
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f0f0 100%);
  font-size: 14px;
  font-weight: 600;
  color: #4a4a4a;
  padding: 16px 14px;
}

.user-table :deep(.el-table td) {
  font-size: 14px;
  padding: 16px 14px;
  color: #4a4a4a;
}

.user-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.user-table :deep(.el-table__row:hover) {
  background: rgba(64, 158, 255, 0.05);
}

.username-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fail-count-normal {
  color: #67c23a;
  font-weight: 500;
}

.fail-count-warning {
  color: #f56c6c;
  font-weight: 600;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.reset-btn {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
  border: none;
  color: #ffffff;
}

.reset-btn:hover {
  background: linear-gradient(135deg, #f0c78a 0%, #f5d6a3 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

.user-table :deep(.el-tag) {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 6px;
}

.user-table :deep(.el-tag--info) {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
  border: none;
}

.user-table :deep(.el-tag--danger) {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  border: none;
}

@media (max-width: 1024px) {
  .admin-card :deep(.el-card__body) {
    padding: 16px;
  }

  .user-table :deep(.el-table th),
  .user-table :deep(.el-table td) {
    padding: 12px 10px;
    font-size: 13px;
  }
}
</style>