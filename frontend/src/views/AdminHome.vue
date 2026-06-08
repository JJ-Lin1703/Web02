<template>
  <div class="admin-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="fetchUsers">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="userList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : ''">
              {{ row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginFailCount" label="登录失败次数" width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="resetDialogVisible" title="重置密码" width="500px">
      <el-form :model="resetForm" :rules="resetRules" ref="resetFormRef" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllUsers, resetUserPassword } from '@/api/user'

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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
