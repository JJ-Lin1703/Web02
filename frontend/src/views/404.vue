<template>
  <div class="not-found">
    <Vue3Lottie
      :animationLink="'/error-404.json'"
      :width="400"
      :height="300"
    />
    <h2>页面不存在</h2>
    <p>您访问的页面可能已被移除或链接错误</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup>
import { Vue3Lottie } from 'vue3-lottie'
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 70vh;
  text-align: center;
}

.not-found h2 {
  font-size: 24px;
  color: #2c3e50;
  margin: 16px 0 8px;
}

.not-found p {
  color: #909399;
  margin-bottom: 24px;
}
</style>