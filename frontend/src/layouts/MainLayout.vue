<template>
  <el-container style="height: 100vh; background: var(--bg)">
    <el-header class="header">
      <div class="logo">
        <el-icon class="logo-icon" size="28"><Cherry /></el-icon>
        <span class="logo-text">智能健康助手</span>
      </div>
      <div class="user-info">
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            <el-icon class="user-icon" size="20"><User /></el-icon>
            <span class="username">{{ userStore.userInfo?.username }}</span>
            <el-icon class="el-icon--right" size="16"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="change-password">
                <el-icon size="16"><Key /></el-icon>
                修改密码
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon size="16"><Lock /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <el-aside width="240px" class="aside">
        <el-menu
          :default-active="$route.path"
          class="menu"
          router
          :background-color="menuBg"
          :text-color="menuText"
          :active-text-color="menuActiveText"
          unique-opened
        >
          <el-menu-item index="/">
            <el-icon size="20"><House /></el-icon>
            <span>首页工作台</span>
          </el-menu-item>
          <el-menu-item index="/record">
            <el-icon size="20"><Document /></el-icon>
            <span>健康档案</span>
          </el-menu-item>
          
          <el-menu-item index="/ai-plan">
            <el-icon size="20"><MagicStick /></el-icon>
            <span>AI健康计划</span>
          </el-menu-item>
          <el-menu-item index="/history">
            <el-icon size="20"><Refresh /></el-icon>
            <span>历史记录</span>
          </el-menu-item>
          <el-menu-item index="/chart">
            <el-icon size="20"><Histogram /></el-icon>
            <span>数据可视化</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.isAdmin()" index="/admin">
            <el-icon size="20"><Setting /></el-icon>
            <span>管理员后台</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="520px" top="15%">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <div v-if="showHealthRecordModal" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div class="modal-icon">
            <el-icon size="48" color="#fff"><Cherry /></el-icon>
          </div>
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
                  <el-select v-model="healthRecordForm.allergy" multiple placeholder="请选择食物过敏（可多选）" style="width: 100%;">
                    <el-option v-for="option in allergyOptions" :key="option.value" :label="option.label" :value="option.value" />
                  </el-select>
                  <span style="font-size: 14px; color: #909399; margin-left: 8px;">（多选，如无过敏可不选）</span>
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
          <el-button type="primary" @click="handleSubmitHealthRecord" :loading="healthRecordLoading" size="large">
            完成填写，进入主页
          </el-button>
        </div>
      </div>
    </div>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown, Cherry, Key, Lock, House, Document, MagicStick, Refresh, Histogram, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { changePassword, checkHealthRecordExists, createHealthRecord } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const menuBg = computed(() => '#1f2937')
const menuText = computed(() => '#e5e7eb')
const menuActiveText = computed(() => '#409eff')

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
  allergy: [],
  medicalHistory: ''
})

const allergyOptions = [
  { label: '海鲜', value: '海鲜' },
  { label: '花生', value: '花生' },
  { label: '牛奶', value: '牛奶' },
  { label: '鸡蛋', value: '鸡蛋' },
  { label: '大豆', value: '大豆' },
  { label: '小麦', value: '小麦' },
  { label: '坚果', value: '坚果' },
  { label: '鱼类', value: '鱼类' },
  { label: '贝壳类', value: '贝壳类' },
  { label: '其他', value: '其他' }
]

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
      allergy: healthRecordForm.allergy.length > 0 ? healthRecordForm.allergy.join(',') : null,
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
  background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  height: 65px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  z-index: 99;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  color: #409eff;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-icon {
  color: #9ca3af;
}

.username {
  margin: 0 8px;
  font-size: 15px;
  color: #e5e7eb;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #9ca3af;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #ffffff;
  }
}

.aside {
  background: #1f2937;
  border-right: 1px solid #374151;
}

.menu {
  border-right: none;
  height: 100%;
  padding: 15px 0;
}

.menu :deep(.el-menu-item) {
  margin: 4px 12px;
  border-radius: 10px;
  padding: 14px 20px;
  font-size: 15px;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(64, 158, 255, 0.15);
  }
  
  &.is-active {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.2) 0%, rgba(64, 158, 255, 0.1) 100%);
    border-left: 3px solid #409eff;
  }
}

.menu :deep(.el-menu-item span) {
  margin-left: 12px;
}

.main {
  background: var(--bg);
  padding: 24px;
  overflow: auto;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background-color: #ffffff;
  border-radius: 20px;
  width: 90%;
  max-width: 720px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
  animation: slideUp 0.4s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  padding: 32px 24px;
  text-align: center;
}

.modal-icon {
  margin-bottom: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: inline-block;
}

.modal-header h3 {
  color: #ffffff;
  font-size: 24px;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.modal-header p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  margin: 0;
}

.modal-body {
  padding: 28px;
  max-height: calc(90vh - 200px);
  overflow-y: auto;
}

.modal-footer {
  padding: 20px 28px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: center;
  background: #fafafa;
}

.modal-footer .el-button {
  width: 220px;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}
</style>
