<template>
  <div class="page-container not-found-page">
    <div class="nf-card glass">
      <div class="nf-icon">🔭</div>
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>你访问的页面不存在，可能已被删除或地址输入有误</p>
      <div class="nf-actions">
        <el-button type="primary" size="large" round @click="$router.push('/')">
          <el-icon><HomeFilled /></el-icon> 返回首页
        </el-button>
        <el-button size="large" round @click="$router.back()">
          <el-icon><Back /></el-icon> 返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { HomeFilled, Back } from '@element-plus/icons-vue'
</script>

<style scoped>
.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 180px);
  padding: 40px 20px;
}

.nf-card {
  text-align: center;
  padding: 60px 48px;
  border-radius: var(--radius-xl);
  max-width: 480px;
  width: 100%;
}

.nf-icon { font-size: 4rem; margin-bottom: 16px; }

.nf-card h1 {
  font-family: var(--font-display);
  font-size: 5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #ff6b6b, #0abde3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 8px;
}

.nf-card h2 {
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 12px;
}

.nf-card p {
  color: var(--text-secondary);
  font-size: 0.95rem;
  margin-bottom: 32px;
  line-height: 1.6;
}

.nf-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
