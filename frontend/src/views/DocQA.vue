<template>
  <div class="doc-qa-wrapper">
    <!-- 左侧历史面板 -->
    <div class="history-panel" :class="{ collapsed: !showHistory }">
      <div class="history-header">
        <div class="header-title">
          <el-icon :size="18" color="#409eff"><Document /></el-icon>
          <span>历史对话</span>
        </div>
        <el-button type="primary" size="small" @click="newChat" :icon="Plus" class="new-chat-btn">
          新对话
        </el-button>
      </div>
      <div class="history-list" ref="historyListRef">
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          :class="['history-item', { active: sessionId === s.sessionId }]"
          @click="loadSession(s.sessionId)"
        >
          <div class="history-icon">
            <el-icon :size="16" color="#409eff"><Document /></el-icon>
          </div>
          <div class="history-content">
            <div class="history-title">{{ s.question || '新对话' }}</div>
            <div class="history-time">{{ formatTime(s.createTime) }}</div>
          </div>
        </div>
        <div v-if="sessions.length === 0" class="history-empty">
          <el-icon :size="32" color="#d9d9d9"><Document /></el-icon>
          <p>暂无历史对话</p>
        </div>
      </div>
    </div>

    <!-- 右侧问答区 -->
    <div class="qa-main">
      <!-- 折叠按钮 -->
      <div class="toggle-bar">
        <el-button :icon="showHistory ? ArrowLeftBold : ArrowRightBold" text size="small" @click="showHistory = !showHistory" class="toggle-btn" />
      </div>

      <div class="qa-container">
        <!-- 顶部标题栏 -->
        <div class="qa-header">
          <div class="header-left">
            <div class="avatar-wrapper">
              <el-icon :size="28" color="#fff"><ChatDotRound /></el-icon>
            </div>
            <div class="header-info">
              <h3 class="qa-title">智能助手</h3>
              <p class="qa-subtitle">优先基于知识库回答，未命中则用通用知识</p>
            </div>
          </div>
          <div class="header-actions">
            <el-button text size="small" @click="newChat">
              新对话
            </el-button>
          </div>
        </div>

        <!-- 聊天消息区 -->
        <div class="chat-area" ref="chatArea">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="welcome-section">
            <div class="welcome-avatar">
              <Vue3Lottie :animationLink="'/chatbot.json'" :width="160" :height="160" :loop="true" :autoplay="true" />
            </div>
            <h2 class="welcome-title">你好！我是智能助手</h2>
            <p class="welcome-desc">输入问题，我会优先从知识库检索答案，未命中则用通用知识回答</p>
            <div class="welcome-tags">
              <el-tag type="info" size="small" effect="plain">健康咨询</el-tag>
              <el-tag type="info" size="small" effect="plain">饮食建议</el-tag>
              <el-tag type="info" size="small" effect="plain">疾病预防</el-tag>
            </div>
          </div>

          <div v-for="(msg, idx) in messages" :key="idx" :class="['message-wrapper', msg.role]">
            <div :class="['message', msg.role]">
              <div class="message-avatar">
                <div v-if="msg.role === 'user'" class="avatar user-avatar">
                <el-icon :size="24" color="#fff"><User /></el-icon>
              </div>
              <div v-else class="avatar ai-avatar">
                <el-icon :size="24" color="#fff"><ChatDotRound /></el-icon>
              </div>
              </div>
              <div class="message-body">
                <div class="message-header">
                  <span class="message-name">{{ msg.role === 'user' ? '我' : '智能助手' }}</span>
                  <span v-if="msg.role === 'assistant' && msg.docBased !== undefined" :class="['doc-tag', msg.docBased ? 'doc-based' : 'not-doc-based']">
                    {{ msg.docBased ? '基于文档' : '通用回答' }}
                  </span>
                </div>
                <div class="message-content" v-text="msg.content"></div>
              </div>
            </div>
          </div>

          <!-- 打字状态 -->
          <div v-if="asking" class="message-wrapper assistant">
            <div class="message assistant">
              <div class="message-avatar">
                <div class="avatar ai-avatar">
                <el-icon :size="24" color="#fff"><ChatDotRound /></el-icon>
              </div>
              </div>
              <div class="message-body">
                <div class="message-header">
                  <span class="message-name">智能助手</span>
                </div>
                <div class="typing-container">
                  <div class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                  <span class="typing-text">正在思考...</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="input-area">
          <div class="input-wrapper">
            <el-input
              v-model="question"
              placeholder="输入任意问题，AI 为你解答"
              @keyup.enter="handleAsk"
              :disabled="asking"
              class="question-input"
            />
            <el-button 
              type="primary" 
              :loading="asking" 
              :disabled="!question.trim()" 
              @click="handleAsk"
              class="send-btn"
              :icon="Promotion"
            >
              {{ asking ? '思考中...' : '' }}
            </el-button>
          </div>
          <p class="input-hint">按 Enter 键发送 | 支持健康咨询、饮食建议等问题</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Vue3Lottie } from 'vue3-lottie'
import { 
  User, Plus, ArrowLeftBold, ArrowRightBold, Document, ChatDotRound, Promotion 
} from '@element-plus/icons-vue'
import { docAsk, listConversations, getConversation } from '@/api/user'

const question = ref('')
const asking = ref(false)
const messages = ref([])
const chatArea = ref(null)
const sessionId = ref('')
const showHistory = ref(true)

const sessions = ref([])

onMounted(async () => {
  newChat()
  await loadSessions()
})

const newChat = () => {
  sessionId.value = crypto.randomUUID()
  messages.value = []
}

const loadSessions = async () => {
  try {
    const res = await listConversations()
    sessions.value = res.sessions || []
  } catch (e) {
    // 静默忽略
  }
}

const loadSession = async (sid) => {
  try {
    const res = await getConversation(sid)
    const list = res.messages || []
    messages.value = []
    list.forEach(m => {
      messages.value.push({ role: 'user', content: m.question })
      messages.value.push({
        role: 'assistant',
        content: m.answer || '未能获取回答',
        docBased: m.docBased === 1
      })
    })
    sessionId.value = sid
    await nextTick()
    scrollToBottom()
  } catch (e) {
    ElMessage.error('加载对话失败')
  }
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatArea.value) {
    chatArea.value.scrollTop = chatArea.value.scrollHeight
  }
}

const handleAsk = async () => {
  const q = question.value.trim()
  if (!q || asking.value) return

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  asking.value = true
  await scrollToBottom()

  try {
    const res = await docAsk(q, sessionId.value)
    messages.value.push({
      role: 'assistant',
      content: res.answer || '未能获取回答',
      docBased: res.docBased
    })
    await loadSessions()
  } catch (error) {
    messages.value.push({ role: 'assistant', content: '问答请求失败：' + (error.response?.data?.error || error.message) })
  } finally {
    asking.value = false
    await scrollToBottom()
  }
}
</script>

<style scoped>
.doc-qa-wrapper {
  display: flex;
  height: calc(100vh - 140px);
  gap: 0;
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

/* 左侧历史面板 */
.history-panel {
  width: 280px;
  min-width: 280px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.history-panel.collapsed {
  width: 0;
  min-width: 0;
  border: none;
  opacity: 0;
}

.history-header {
  padding: 16px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 12px;
}

.new-chat-btn {
  width: 100%;
  border-radius: 10px;
  padding: 10px;
  font-weight: 500;
  color: #fff !important;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%) !important;
  border: none !important;
}

.new-chat-btn:hover {
  background: linear-gradient(135deg, #3088e8 0%, #55a8f5 100%) !important;
  color: #fff !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.history-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  margin-bottom: 6px;
  transition: all 0.2s ease;
  background: #fff;
  border: 1px solid transparent;
}

.history-item:hover {
  background: #fff;
  border-color: #e2e8f0;
  transform: translateX(4px);
}

.history-item.active {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-color: #3b82f6;
}

.history-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 10px;
  flex-shrink: 0;
}

.history-item.active .history-icon {
  background: #dbeafe;
}

.history-content {
  flex: 1;
  min-width: 0;
}

.history-title {
  font-size: 13px;
  color: #1e293b;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  color: #94a3b8;
}

.history-empty p {
  margin-top: 12px;
  font-size: 13px;
}

/* 右侧问答区 */
.qa-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #fff;
}

.toggle-bar {
  padding: 6px 0 6px 6px;
  background: #fff;
}

.toggle-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: #64748b;
}

.toggle-btn:hover {
  background: #f1f5f9;
}

.qa-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 顶部标题栏 */
.qa-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.header-info {
  color: #fff;
}

.qa-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
}

.qa-subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  opacity: 0.85;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.header-actions .el-button {
  color: rgba(255, 255, 255, 0.9);
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
}

.header-actions .el-button:hover {
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  padding-bottom: 140px;
  background: #f8fafc;
}

/* 欢迎消息 */
.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60%;
  text-align: center;
}

.welcome-avatar {
  width: 160px;
  height: 160px;
  border-radius: 24px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f4ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px rgba(64, 158, 255, 0.15);
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}

.welcome-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 20px;
  max-width: 400px;
}

.welcome-tags {
  display: flex;
  gap: 8px;
}

.welcome-tags .el-tag {
  background: #fff;
  border-color: #e2e8f0;
}

/* 消息样式 */
.message-wrapper {
  display: flex;
  margin-bottom: 20px;
}

.message-wrapper.user {
  justify-content: flex-end;
}

.message-wrapper.user .message {
  flex-direction: row-reverse;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.message-name {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
}

.doc-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.doc-tag.doc-based {
  background: #dcfce7;
  color: #16a34a;
}

.doc-tag.not-doc-based {
  background: #fef3c7;
  color: #d97706;
}

.message-content {
  background: #fff;
  padding: 14px 18px;
  border-radius: 0 16px 16px 16px;
  font-size: 14px;
  line-height: 1.7;
  color: #334155;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  white-space: pre-wrap;
  word-break: break-word;
}

.message-wrapper.user .message-content {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  border-radius: 16px 0 16px 16px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.message-wrapper.user .message-name {
  color: #409eff;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
}

.ai-avatar {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

/* 打字状态 */
.typing-container {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: #fff;
  border-radius: 0 16px 16px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #94a3b8;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}

.typing-text {
  font-size: 13px;
  color: #94a3b8;
}

/* 输入区 */
.input-area {
  position: absolute;
  bottom: 20px;
  left: 20px;
  right: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;
  z-index: 100;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
  background: #f8fafc;
  border-radius: 24px;
  padding: 6px 6px 6px 20px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.input-wrapper:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.question-input {
  flex: 1;
}

.question-input :deep(.el-input__wrapper) {
  border: none;
  box-shadow: none;
  padding: 0;
}

.question-input :deep(.el-input__inner) {
  font-size: 14px;
  color: #334155;
}

.send-btn {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
}

.send-btn:not(:disabled):hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
}

.input-hint {
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
  margin: 10px 0 0;
}

/* 滚动条样式 */
.chat-area::-webkit-scrollbar,
.history-list::-webkit-scrollbar {
  width: 6px;
}

.chat-area::-webkit-scrollbar-track,
.history-list::-webkit-scrollbar-track {
  background: transparent;
}

.chat-area::-webkit-scrollbar-thumb,
.history-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.chat-area::-webkit-scrollbar-thumb:hover,
.history-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
