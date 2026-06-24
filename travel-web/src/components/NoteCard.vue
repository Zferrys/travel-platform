<template>
  <div class="note-card" @click="$router.push(`/note/${note.noteId}`)">
    <div class="card-img">
      <img :src="note.coverImage || FALLBACK.note" :alt="note.title" @error="e => e.target.src = FALLBACK.note" />
      <div class="card-overlay">
        <span class="read-btn">阅读全文</span>
      </div>
    </div>
    <div class="card-body">
      <h3>{{ note.title || '无标题' }}</h3>
      <p class="card-excerpt" v-if="note.content">
        {{ stripHtml(note.content).slice(0, 60) }}...
      </p>
      <div class="meta">
        <div class="author">
          <el-avatar :size="22" :src="note.avatar || FALLBACK.avatar" />
          <span>{{ note.nickname || note.username || '匿名' }}</span>
        </div>
        <div class="stats">
          <span><el-icon :size="13"><View /></el-icon> {{ note.viewCount || 0 }}</span>
          <span><el-icon :size="13"><StarFilled /></el-icon> {{ note.likeCount || 0 }}</span>
          <span><el-icon :size="13"><ChatDotRound /></el-icon> {{ note.commentCount || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { View, StarFilled, ChatDotRound } from '@element-plus/icons-vue'
import { FALLBACK } from '@/utils/fallback'

defineProps({ note: Object })

const stripHtml = (html) => {
  const tmp = document.createElement('div')
  tmp.innerHTML = html || ''
  return tmp.textContent || tmp.innerText || ''
}
</script>

<style scoped>
.note-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--border-light);
}

.note-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
}

.card-img {
  position: relative;
  height: 190px;
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.note-card:hover .card-img img {
  transform: scale(1.1);
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.note-card:hover .card-overlay {
  opacity: 1;
}

.read-btn {
  padding: 10px 28px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 25px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text);
}

.card-body {
  padding: 16px;
}

.card-body h3 {
  font-size: 1.05rem;
  font-weight: 600;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.card-excerpt {
  font-size: 0.82rem;
  color: var(--text-secondary);
  margin-bottom: 12px;
  line-height: 1.5;
}

.meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
}

.author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.82rem;
  color: var(--text-secondary);
}

.stats {
  display: flex;
  gap: 14px;
  font-size: 0.78rem;
  color: var(--text-secondary);
}

.stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}
</style>
