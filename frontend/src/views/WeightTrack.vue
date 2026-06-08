<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>体重追踪</span>
          <el-button type="primary">记录体重</el-button>
        </div>
      </template>
      <el-empty description="暂无体重记录" />
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.page-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
