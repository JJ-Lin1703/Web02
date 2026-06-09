<template>
  <div class="login-container">
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>
    <div class="login-left">
      <div class="animation-wrapper">
        <Vue3Lottie :animationLink="'/login-animation.json'" :width="500" :height="500" />
      </div>
    </div>
    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <div class="logo">
            <div class="logo-icon">
              <svg viewBox="0 0 100 100" class="health-icon">
                <circle cx="50" cy="50" r="45" fill="url(#logoGradient)" />
                <path d="M50 25 L50 75 M30 50 L70 50" stroke="white" stroke-width="4" stroke-linecap="round" />
                <circle cx="50" cy="50" r="15" fill="white" opacity="0.8" />
                <defs>
                  <linearGradient id="logoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#5cc5c5" />
                    <stop offset="100%" stop-color="#3aafaf" />
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <h1>智能健康助手</h1>
            <p class="subtitle">您的专属健康管理专家</p>
          </div>
        </div>
        
        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="100px">
              <el-form-item label="用户名" prop="username">
                <el-input 
                  v-model="loginForm.username" 
                  placeholder="请输入用户名" 
                  size="large"
                  class="form-input"
                />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input 
                  v-model="loginForm.password" 
                  type="password" 
                  placeholder="请输入密码" 
                  show-password 
                  size="large"
                  class="form-input"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleLogin" size="large" class="login-btn">登录</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="100px">
              <el-form-item label="用户名" prop="username">
                <el-input 
                  v-model="registerForm.username" 
                  placeholder="请输入用户名" 
                  size="large"
                  class="form-input"
                />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input 
                  v-model="registerForm.password" 
                  type="password" 
                  placeholder="请输入密码" 
                  show-password 
                  size="large"
                  class="form-input"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input 
                  v-model="registerForm.confirmPassword" 
                  type="password" 
                  placeholder="请确认密码" 
                  show-password 
                  size="large"
                  class="form-input"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleRegister" size="large" class="login-btn">注册</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
        
        <div class="login-footer">
          <p>© 2024 智能健康助手 | 关爱您的健康</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userLogin, userRegister, getUserInfo } from '@/api/user'
import { Vue3Lottie } from 'vue3-lottie'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateUsername = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (value.length < 4 || value.length > 20) {
    callback(new Error('用户名长度必须在4-20位之间'))
  } else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error('用户名只能包含中文、英文和数字'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 16) {
    callback(new Error('密码长度必须在6-16位之间'))
  } else if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含字母和数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ validator: validateUsername, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    const res = await userLogin(loginForm)
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败', error)
  }
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    await userRegister({
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword
    })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
  } catch (error) {
    console.error('注册失败', error)
  }
}

const fetchUserInfo = async () => {
  const res = await getUserInfo()
  userStore.setUserInfo(res.data)
}

// 粒子动画
const particleCanvas = ref(null)

const initParticles = () => {
  const canvas = particleCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')

  const resizeCanvas = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)

  class Particle {
    constructor() {
      this.x = Math.random() * canvas.width
      this.y = Math.random() * canvas.height
      this.vx = (Math.random() - 0.5) * 0.8
      this.vy = (Math.random() - 0.5) * 0.8
      this.radius = Math.random() * 3 + 1
      this.alpha = Math.random() * 0.3 + 0.1
    }
    update() {
      this.x += this.vx
      this.y += this.vy
      if (this.x < 0 || this.x > canvas.width) this.vx *= -1
      if (this.y < 0 || this.y > canvas.height) this.vy *= -1
    }
    draw() {
      ctx.beginPath()
      ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(255, 255, 255, ${this.alpha})`
      ctx.fill()
    }
  }

  const particles = []
  for (let i = 0; i < 40; i++) {
    particles.push(new Particle())
  }

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    particles.forEach(p => {
      p.update()
      p.draw()
    })
    requestAnimationFrame(animate)
  }
  animate()
}

onMounted(() => {
  initParticles()
})
</script>

<style scoped>
.particle-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 0;
  pointer-events: none;
}

.login-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #a8e6e6 0%, #7dd3d3 40%, #5cc5c5 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 50%, rgba(255,255,255,0.08) 0%, transparent 50%),
              radial-gradient(circle at 70% 30%, rgba(255,255,255,0.05) 0%, transparent 50%),
              radial-gradient(circle at 50% 70%, rgba(255,255,255,0.06) 0%, transparent 50%);
  animation: float-bg 20s ease-in-out infinite;
  pointer-events: none;
}

.login-left {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  z-index: 1;
}

@keyframes float-bg {
  0%, 100% { transform: translate(0, 0); }
  25% { transform: translate(-2%, 1%); }
  50% { transform: translate(1%, -2%); }
  75% { transform: translate(-1%, -1%); }
}

.animation-wrapper {
  position: relative;
  z-index: 1;
}

.login-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  position: relative;
  z-index: 1;
}

.login-box {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 48px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  width: 450px;
  max-width: 100%;
  min-height: 500px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
}

.health-icon {
  width: 100%;
  height: 100%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

.login-tabs {
  margin-bottom: 20px;
}

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 30px;
}

.login-tabs :deep(.el-tabs__nav-wrap) {
  justify-content: center;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  padding: 0 30px;
  color: #6b7280;
}

.login-tabs :deep(.el-tabs__item.is-active) {
  color: #3aafaf;
}

.login-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #3aafaf, #2d9d9d);
  height: 3px;
}

.login-tabs :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-tabs :deep(.el-form-item__label) {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

.login-tabs :deep(.el-form-item__content) {
  margin-left: 100px !important;
}

.login-tabs :deep(.el-form-item:last-child .el-form-item__content) {
  margin-left: 0 !important;
}

.form-input {
  font-size: 15px;
}

.login-tabs :deep(.el-input__wrapper) {
  border-radius: 10px;
  border-color: #e5e7eb;
  transition: all 0.3s ease;
}

.login-tabs :deep(.el-input__wrapper:hover) {
  border-color: #3aafaf;
  box-shadow: 0 0 0 3px rgba(38, 181, 181, 0.1);
}

.login-tabs :deep(.el-input__wrapper.is-focus) {
  border-color: #3aafaf;
  box-shadow: 0 0 0 3px rgba(38, 181, 181, 0.2);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #3aafaf 0%, #2d9d9d 100%);
  border: none;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px -5px rgba(38, 181, 181, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.login-footer p {
  font-size: 13px;
  color: #9ca3af;
  margin: 0;
}

@media (max-width: 768px) {
  .login-left {
    display: none;
  }
  .login-box {
    width: 100%;
    padding: 32px 24px;
  }
}
</style>