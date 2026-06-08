<template>
  <el-container style="height: 100vh">
    <el-header class="header">
      <div class="logo">智能健康助手</div>
      <div class="user-info">
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            <el-icon><User /></el-icon>
            {{ userStore.userInfo?.username }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="change-password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <el-aside width="220px" class="aside">
        <el-menu
          :default-active="$route.path"
          class="menu"
          router
          background-color="#2f4050"
          text-color="#fff"
          active-text-color="#409eff"
        >
          <el-menu-item index="/">首页工作台</el-menu-item>
          <el-menu-item index="/record">健康档案</el-menu-item>
          
          <el-menu-item index="/ai-plan">AI健康计划</el-menu-item>
          <el-menu-item index="/history">历史记录</el-menu-item>
          <el-menu-item index="/chart">数据可视化</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin()" index="/admin">管理员后台</el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="500px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认</el-button>
      </template>
    </el-dialog>

    <div v-if="showHealthRecordModal" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>欢迎使用智能健康助手</h3>
          <p>请先填写您的健康档案，以便为您提供个性化服务</p>
        </div>
        <div class="modal-body">
          <el-form :model="healthRecordForm" :rules="healthRecordRules" ref="healthRecordFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="年龄" prop="age">
                  <el-input v-model.number="healthRecordForm.age" type="number" placeholder="请输入年龄（12-80岁）" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别" prop="gender">
                  <el-select v-model="healthRecordForm.gender" placeholder="请选择性别">
                    <el-option label="男" :value="0" />
                    <el-option label="女" :value="1" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="身高(cm)" prop="height">
                  <el-input v-model.number="healthRecordForm.height" type="number" step="0.1" placeholder="请输入身高（100-250cm）" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="体重(kg)" prop="weight">
                  <el-input v-model.number="healthRecordForm.weight" type="number" step="0.1" placeholder="请输入体重（30-300kg）" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="日常活动水平" prop="activityLevel">
                  <el-select v-model="healthRecordForm.activityLevel" placeholder="请选择活动水平">
                    <el-option label="低（久坐少动）" :value="1" />
                    <el-option label="中（轻度运动）" :value="2" />
                    <el-option label="高（经常锻炼）" :value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="饮食偏好" prop="dietHobby">
                  <el-select v-model="healthRecordForm.dietHobby" placeholder="请选择饮食偏好">
                    <el-option label="素食" value="素食" />
                    <el-option label="均衡" value="均衡" />
                    <el-option label="高蛋白" value="高蛋白" />
                    <el-option label="低碳水" value="低碳水" />
                    <el-option label="控糖" value="控糖" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="健康目标" prop="healthTarget">
                  <el-select v-model="healthRecordForm.healthTarget" placeholder="请选择健康目标">
                    <el-option label="减肥" value="减肥" />
                    <el-option label="增肌" value="增肌" />
                    <el-option label="维持健康" value="维持健康" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="食物过敏">
                  <el-input v-model="healthRecordForm.allergy" type="textarea" placeholder="请输入食物过敏（多个用逗号分隔）" :rows="2" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="慢性病史">
                  <el-input v-model="healthRecordForm.medicalHistory" type="textarea" placeholder="请输入慢性病史" :rows="2" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div class="modal-footer">
          <el-button type="primary" @click="handleSubmitHealthRecord" :loading="healthRecordLoading">
            完成填写，进入主页
          </el-button>
        </div>
      </div>
    </div>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { changePassword, checkHealthRecordExists, createHealthRecord } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)

const showHealthRecordModal = ref(false)
const healthRecordFormRef = ref(null)
const healthRecordLoading = ref(false)

const healthRecordForm = reactive({
  age: null,
  gender: null,
  height: null,
  weight: null,
  activityLevel: null,
  dietHobby: '',
  healthTarget: '',
  allergy: '',
  medicalHistory: ''
})

const healthRecordRules = {
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 12, max: 80, message: '年龄必须在12-80岁之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  height: [
    { required: true, message: '请输入身高', trigger: 'blur' },
    { type: 'number', min: 100, max: 250, message: '身高必须在100-250cm之间', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' },
    { type: 'number', min: 30, max: 300, message: '体重必须在30-300kg之间', trigger: 'blur' }
  ],
  activityLevel: [
    { required: true, message: '请选择活动水平', trigger: 'change' }
  ],
  dietHobby: [
    { required: true, message: '请选择饮食偏好', trigger: 'change' }
  ],
  healthTarget: [
    { required: true, message: '请选择健康目标', trigger: 'change' }
  ]
}

const checkAndShowHealthRecordModal = async () => {
  try {
    const res = await checkHealthRecordExists()
    if (!res.data.exists) {
      showHealthRecordModal.value = true
    }
  } catch (error) {
    console.error('检查健康档案失败', error)
  }
}

const handleOverlayClick = () => {
  ElMessage.warning('请先完成健康档案的填写')
}

const handleSubmitHealthRecord = async () => {
  try {
    await healthRecordFormRef.value.validate()
    healthRecordLoading.value = true
    const requestData = {
      age: healthRecordForm.age,
      gender: healthRecordForm.gender,
      height: healthRecordForm.height,
      weight: healthRecordForm.weight,
      activityLevel: healthRecordForm.activityLevel,
      dietHobby: healthRecordForm.dietHobby,
      healthTarget: healthRecordForm.healthTarget,
      allergy: healthRecordForm.allergy || null,
      medicalHistory: healthRecordForm.medicalHistory || null
    }
    await createHealthRecord(requestData)
    ElMessage.success('健康档案创建成功')
    showHealthRecordModal.value = false
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    healthRecordLoading.value = false
  }
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      router.push('/login')
    } catch {
    }
  } else if (command === 'change-password') {
    passwordDialogVisible.value = true
  }
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败', error)
  }
}

onMounted(() => {
  checkAndShowHealthRecordModal()
})
</script>

<style scoped>
.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  z-index: 99;
}

.user-info {
  font-size: 14px;
  font-weight: normal;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #606266;
}

.aside {
  background-color: #2f4050;
}

.menu {
  border-right: none;
  height: 100%;
}

.main {
  background-color: #f3f3f4;
  padding: 20px;
  overflow: auto;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background-color: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  padding: 24px;
  text-align: center;
}

.modal-header h3 {
  color: #fff;
  font-size: 20px;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.modal-header p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  margin: 0;
}

.modal-body {
  padding: 24px;
  max-height: calc(90vh - 180px);
  overflow-y: auto;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #ebf0f5;
  display: flex;
  justify-content: center;
}

.modal-footer .el-button {
  width: 200px;
}
</style>