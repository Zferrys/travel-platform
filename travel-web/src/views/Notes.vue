<template>
  <div class="page-container">
    <el-breadcrumb separator="›" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>游记社区</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="notes-header">
      <div>
        <h2 class="section-title" style="margin:0;border:none">📝 游记社区</h2>
        <p class="subtitle">阅读旅行故事，分享精彩瞬间</p>
      </div>
      <el-button type="primary" size="large" round @click="$router.push('/publish')" v-if="store.getters.isLoggedIn">
        <el-icon><Edit /></el-icon> 写游记
      </el-button>
    </div>

    <div class="card-grid">
      <NoteCard v-for="(n, i) in list" :key="n.noteId" :note="n" :style="{ animationDelay: i * 0.06 + 's' }" class="fade-in-up" />
    </div>

    <el-empty v-if="!list.length" description="还没有游记，快来写一篇吧">
      <el-button type="primary" @click="$router.push('/publish')" v-if="store.getters.isLoggedIn">写一篇游记</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Edit } from '@element-plus/icons-vue'
import { noteApi } from '@/api'
import store from '@/store'
import NoteCard from '@/components/NoteCard.vue'

const list = ref([])
onMounted(async () => {
  try { const r = await noteApi.getList(); list.value = r.data.list || [] } catch {}
})
</script>

<style scoped>
.breadcrumb { margin-bottom: 16px; }
.breadcrumb :deep(.el-breadcrumb__inner) { color: var(--text-secondary) !important; font-size: 0.85rem; }

.notes-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.subtitle { color: var(--text-secondary); font-size: 0.9rem; margin-top: 4px; }
</style>
