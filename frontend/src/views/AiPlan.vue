<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI健康计划</span>
          <el-button type="primary">生成计划</el-button>
        </div>
      </template>
      <el-empty description="暂无AI生成的计划" />
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
