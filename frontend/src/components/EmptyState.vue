<template>
  <div class="empty-state">
    <div class="empty-animation">
      <Vue3Lottie :animationLink="'/no-data-animation.json'" :width="200" :height="200" :loop="true" :autoplay="true" />
    </div>
    <p class="empty-text">{{ description }}</p>
  </div>
</template>

<script setup>
import { Vue3Lottie } from 'vue3-lottie'

defineProps({
  description: {
    type: String,
    default: '暂无数据'
  }
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.empty-animation {
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
</style>