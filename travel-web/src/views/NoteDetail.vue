<template>
  <div class="page-container" v-loading="loading">
    <div v-if="loadError" style="text-align:center;padding-top:80px">
      <el-empty description="游记不存在或已删除">
        <el-button type="primary" @click="$router.back()">返回</el-button>
      </el-empty>
    </div>

    <template v-else>
      <el-breadcrumb separator="›" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/notes' }">游记</el-breadcrumb-item>
        <el-breadcrumb-item>{{ note.title }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="cover" v-if="note.coverImage">
        <img :src="note.coverImage" :alt="note.title" @error="e => e.target.style.display = 'none'" />
      </div>

      <article class="note-article">
        <h1>{{ note.title }}</h1>
        <div class="meta-bar">
          <div class="author-info">
            <el-avatar :size="40" :src="note.avatar || FALLBACK.avatar" />
            <div>
              <strong>{{ note.nickname || note.username }}</strong>
              <span>发布于 {{ note.createTime }}</span>
            </div>
          </div>
          <div class="meta-actions" v-if="isOwner">
            <el-button size="small" text @click="$router.push(`/publish?id=${note.noteId}`)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-popconfirm title="确定要删除这篇游记吗？" @confirm="doDelete">
              <template #reference>
                <el-button size="small" text type="danger">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-popconfirm>
          </div>
          <div class="interactions">
            <div class="interact-btn" @click="toggleLike" :class="{ liked }">
              <el-icon :size="20"><StarFilled /></el-icon>
              <span>{{ note.likeCount || 0 }}</span>
            </div>
            <div class="interact-btn">
              <el-icon :size="20"><View /></el-icon>
              <span>{{ note.viewCount || 0 }}</span>
            </div>
            <div class="interact-btn" @click="scrollTo('comments-section')">
              <el-icon :size="20"><ChatDotRound /></el-icon>
              <span>{{ note.commentCount || 0 }}</span>
            </div>
          </div>
        </div>

        <div class="content" v-html="note.content || '暂无内容'"></div>

        <div class="tag-section" v-if="note.tags">
          <el-tag v-for="t in note.tags.split(',')" :key="t" type="info" effect="plain" round>{{ t.trim() }}</el-tag>
        </div>
      </article>

      <div id="comments-section" class="comments-section">
        <h3 class="section-title">💬 评论 ({{ note.commentCount || 0 }})</h3>
        <CommentList targetType="3" :targetId="note.noteId" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { StarFilled, View, ChatDotRound, Edit, Delete } from '@element-plus/icons-vue'
import { noteApi } from '@/api'
import { FALLBACK } from '@/utils/fallback'
import store from '@/store'
import CommentList from '@/components/CommentList.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const loadError = ref(false)
const note = ref({})
const liked = ref(false)

const isOwner = computed(() => {
  const uid = store.getters.user?.userId
  return uid && uid === note.value.userId
})

const doDelete = async () => {
  try {
    await noteApi.delete(note.value.noteId)
    ElMessage.success('游记已删除')
    router.push('/notes')
  } catch {}
}

const scrollTo = (id) => {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

const load = async () => {
  try {
    const r = await noteApi.getDetail(route.params.id)
    note.value = r.data || {}
    if (!r.data?.noteId) { loadError.value = true; return }
  } catch { loadError.value = true
  } finally { loading.value = false }
}

const toggleLike = async () => {
  if (!store.getters.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  const prevLiked = liked.value
  const prevCount = note.value.likeCount || 0
  liked.value = !prevLiked
  note.value.likeCount = prevCount + (liked.value ? 1 : -1)
  try {
    if (liked.value) {
      await noteApi.like(note.value.noteId)
      ElMessage.success('已点赞 👍')
    } else {
      await noteApi.unlike(note.value.noteId)
    }
  } catch {
    liked.value = prevLiked
    note.value.likeCount = prevCount
  }
}

onMounted(load)
watch(() => route.params.id, () => { if (route.params.id) { loading.value = true; load() } })
</script>

<style scoped>
.breadcrumb { margin-bottom: 20px; }
.breadcrumb :deep(.el-breadcrumb__inner) { color: var(--text-secondary) !important; }
.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) { color: var(--text) !important; font-weight: 600; }

.cover { margin-bottom: 24px; border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-lg); }
.cover img { width: 100%; max-height: 420px; object-fit: cover; }

.note-article { max-width: 800px; margin: 0 auto; }

.note-article h1 {
  font-family: var(--font-display);
  font-size: 2rem; font-weight: 800;
  margin-bottom: 20px; line-height: 1.3;
}

.meta-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 0;
  border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 28px;
  flex-wrap: wrap; gap: 12px;
}

.author-info {
  display: flex; align-items: center; gap: 10px;
}

.author-info strong {
  display: block; font-size: 0.92rem;
}

.author-info span {
  font-size: 0.78rem; color: var(--text-secondary);
}

.meta-actions {
  display: flex; gap: 4px; align-items: center;
}

.interactions {
  display: flex; gap: 16px;
}

.interact-btn {
  display: flex; flex-direction: column; align-items: center;
  gap: 2px; cursor: pointer; padding: 6px 12px;
  border-radius: var(--radius-sm); transition: all 0.2s;
  font-size: 0.75rem; color: var(--text-secondary);
}

.interact-btn:hover {
  background: var(--bg);
}

.interact-btn.liked {
  color: var(--danger);
}

.content {
  line-height: 2; font-size: 1.05rem;
  color: var(--text);
}

.content :deep(img) {
  max-width: 100%; border-radius: var(--radius-sm);
  margin: 16px 0;
}

.content :deep(p) {
  margin-bottom: 16px;
}

.content :deep(h2),
.content :deep(h3) {
  margin: 24px 0 12px;
}

.tag-section {
  margin-top: 32px;
  display: flex; gap: 8px; flex-wrap: wrap;
}

.comments-section {
  max-width: 800px; margin: 48px auto 0;
}

@media (max-width: 768px) {
  .note-article h1 { font-size: 1.5rem; }
}
</style>
