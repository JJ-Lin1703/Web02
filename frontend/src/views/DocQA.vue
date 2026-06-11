<template>
  <div class="doc-qa-wrapper">
    <!-- 左侧历史面板 -->
    <div class="history-panel" :class="{ collapsed: !showHistory }">
      <div class="history-header">
        <el-button type="primary" size="small" @click="newChat" :icon="Plus" style="width: 100%;">
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
          <div class="history-title">{{ s.question || '新对话' }}</div>
          <div class="history-time">{{ formatTime(s.createTime) }}</div>
        </div>
        <div v-if="sessions.length === 0" class="history-empty">暂无历史对话</div>
      </div>
    </div>

    <!-- 右侧问答区 -->
    <div class="qa-main">
      <!-- 折叠按钮 -->
      <div class="toggle-bar">
        <el-button :icon="showHistory ? ArrowLeft : ArrowRight" text size="small" @click="showHistory = !showHistory" />
      </div>

      <el-card class="qa-card">
        <template #header>
          <div class="card-header">
            <el-icon :size="20" color="#409eff"><ChatDotRound /></el-icon>
            <span class="card-title">智能问答</span>
            <span class="card-subtitle">优先基于知识库文档回答，未命中则以通用知识回答</span>
          </div>
        </template>

        <!-- 聊天消息区 -->
        <div class="chat-area" ref="chatArea">
          <div v-if="messages.length === 0" class="chat-empty">
            <el-icon :size="64" color="#c0c4cc"><ChatLineSquare /></el-icon>
            <p>输入问题，AI 优先从知识库检索作答，未命中则用通用知识回答</p>
          </div>

          <div v-for="(msg, idx) in messages" :key="idx" :class="['message', msg.role]">
            <div class="message-avatar">
              <el-icon v-if="msg.role === 'user'" :size="28" color="#409eff"><User /></el-icon>
              <el-icon v-else :size="28" color="#67c23a"><Service /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-role">{{ msg.role === 'user' ? '我' : 'AI 助手' }}</div>
              <div v-if="msg.role === 'assistant' && msg.docBased !== undefined" class="source-tag">
                <el-tag v-if="msg.docBased" type="success" size="small" effect="plain">
                  <el-icon :size="12"><Document /></el-icon> 基于文档内容回答
                </el-tag>
                <el-tag v-else type="warning" size="small" effect="plain">
                  <el-icon :size="12"><InfoFilled /></el-icon> 文档中未找到相关内容，以下为通用回答
                </el-tag>
              </div>
              <div class="message-text" v-text="msg.content"></div>
            </div>
          </div>

          <div v-if="asking" class="message assistant">
            <div class="message-avatar">
              <el-icon :size="28" color="#67c23a"><Service /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-role">AI 助手</div>
              <div class="typing-indicator"><span></span><span></span><span></span></div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="input-area">
          <el-input
            v-model="question"
            placeholder="输入任意健康相关问题，AI 为你解答"
            @keyup.enter="handleAsk"
            :disabled="asking"
            class="question-input"
          >
            <template #suffix>
              <el-button type="primary" :loading="asking" :disabled="!question.trim()" @click="handleAsk">
                {{ asking ? '思考中...' : '发送' }}
              </el-button>
            </template>
          </el-input>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, ChatLineSquare, User, Service, Document, InfoFilled, Plus, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { docAsk, listConversations, getConversation } from '@/api/user'

const question = ref('')
const asking = ref(false)
const messages = ref([])
const chatArea = ref(null)
const sessionId = ref('')
const showHistory = ref(true)

// 历史会话
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
    // 刷新历史列表
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
  max-width: 1100px;
  margin: 0 auto;
}

/* 左侧历史面板 */
.history-panel {
  width: 240px;
  min-width: 240px;
  background: #fff;
  border-radius: 12px 0 0 12px;
  border: 1px solid var(--border);
  border-right: none;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  overflow: hidden;
}

.history-panel.collapsed {
  width: 0;
  min-width: 0;
  border: none;
  opacity: 0;
}

.history-header {
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.history-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: background 0.15s;
}

.history-item:hover {
  background: #f0f2f5;
}

.history-item.active {
  background: #ecf5ff;
}

.history-title {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

.history-empty {
  text-align: center;
  color: #c0c4cc;
  font-size: 13px;
  padding: 24px 0;
}

/* 右侧问答区 */
.qa-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.toggle-bar {
  padding: 4px 0 4px 4px;
}

.qa-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 0 12px 12px 0;
  border: 1px solid var(--border);
}

.qa-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

.card-subtitle {
  font-size: 13px;
  color: #909399;
  margin-left: 12px;
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  margin-bottom: 12px;
}

.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
}

.chat-empty p {
  margin-top: 16px;
  font-size: 14px;
}

/* 消息样式 */
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 0 4px;
}

.message.user { flex-direction: row-reverse; }

.message-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f0f2f5;
}

.message-role {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.source-tag { margin-bottom: 6px; }

.message.user .message-role { text-align: right; }

.message-text {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  max-width: 70%;
  white-space: pre-wrap;
  word-break: break-word;
}

.message.user .message-text {
  background: #ecf5ff;
  color: #303133;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* 输入区 */
.input-area {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.question-input :deep(.el-input__wrapper) {
  border-radius: 24px;
  padding-right: 6px;
}

.question-input :deep(.el-input__suffix) { right: 4px; }
</style>