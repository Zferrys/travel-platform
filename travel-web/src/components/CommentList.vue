<template>
  <div class="comment-list">
    <!-- Comment Input -->
    <div class="comment-input" v-if="store.getters.isLoggedIn">
      <el-avatar :size="36" :src="store.getters.user?.avatar || FALLBACK.avatar" class="input-avatar" />
      <div class="input-area">
        <el-input
          v-model="text"
          placeholder="写下你的评论..."
          type="textarea"
          :rows="2"
          @keyup.enter.ctrl="send"
        />
        <div class="input-actions">
          <span class="input-hint">Ctrl+Enter 发送</span>
          <el-button type="primary" size="small" @click="send" :loading="sending" round>
            发表评论
          </el-button>
        </div>
      </div>
    </div>

    <!-- Login prompt -->
    <div v-else class="login-prompt">
      <p>💬 登录后参与评论</p>
      <el-button type="primary" size="small" round @click="$router.push('/login')">去登录</el-button>
    </div>

    <!-- Comment list -->
    <div class="comments-section" v-if="comments.length">
      <div v-for="c in comments" :key="c.commentId" class="comment-item fade-in-up">
        <el-avatar :size="40" :src="c.avatar || FALLBACK.avatar" class="comment-avatar" />
        <div class="comment-body">
          <div class="comment-head">
            <strong>{{ c.nickname || c.username }}</strong>
            <div class="comment-head-right">
              <span class="comment-time">{{ c.createTime }}</span>
              <el-popconfirm
                v-if="isCommentOwner(c)"
                title="确定要删除这条评论吗？"
                @confirm="delComment(c.commentId)"
              >
                <template #reference>
                  <el-button size="small" text type="danger" class="comment-del-btn" @click.stop>
                    <el-icon :size="14"><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
          <p class="comment-content">{{ c.content }}</p>
        </div>
      </div>
    </div>

    <div v-else class="comment-empty">
      <el-empty description="暂无评论，来发表第一条吧" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { commentApi } from '@/api'
import { FALLBACK } from '@/utils/fallback'
import store from '@/store'

const props = defineProps({ targetType: String, targetId: [Number, String] })
const router = useRouter()
const comments = ref([])
const text = ref('')
const sending = ref(false)

const currentUserId = computed(() => store.getters.user?.userId)
const isCommentOwner = (c) => currentUserId.value && c.userId === currentUserId.value

const load = async () => {
  const id = parseInt(props.targetId)
  if (isNaN(id) || id <= 0) return
  try { const r = await commentApi.getList(props.targetType, id); comments.value = r.data.list || [] } catch {}
}

const send = async () => {
  if (!text.value.trim()) return
  sending.value = true
  try {
    const tid = parseInt(props.targetId)
    if (isNaN(tid)) return
    await commentApi.publish({ targetType: parseInt(props.targetType), targetId: tid, content: text.value.trim() })
    text.value = ''
    ElMessage.success('评论发表成功！')
    await load()
  } catch {} finally { sending.value = false }
}

const delComment = async (commentId) => {
  try {
    await commentApi.delete(commentId)
    ElMessage.success('评论已删除')
    await load()
  } catch {}
}

onMounted(load)
</script>

<style scoped>
.comment-list { max-width: 100%; }

.comment-input {
  display: flex; gap: 12px; margin-bottom: 24px;
  padding: 16px; background: var(--bg); border-radius: var(--radius-sm);
}

.input-avatar { flex-shrink: 0; }

.input-area { flex: 1; }

.input-actions {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 10px;
}

.input-hint { font-size: 0.75rem; color: var(--text-secondary); }

.login-prompt {
  text-align: center; padding: 24px;
  background: var(--bg); border-radius: var(--radius-sm);
  margin-bottom: 24px;
}

.login-prompt p { margin-bottom: 12px; color: var(--text-secondary); }

.comment-item {
  display: flex; gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-light);
}

.comment-avatar { flex-shrink: 0; }

.comment-body { flex: 1; }

.comment-head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 6px;
}

.comment-head strong { font-size: 0.9rem; font-weight: 600; }

.comment-head-right { display: flex; align-items: center; gap: 8px; }

.comment-time { font-size: 0.75rem; color: var(--text-secondary); }

.comment-del-btn { opacity: 0; transition: opacity 0.15s; padding: 2px; }
.comment-item:hover .comment-del-btn { opacity: 1; }

.comment-content { font-size: 0.9rem; line-height: 1.6; color: var(--text); }

.comment-empty { padding: 20px 0; }
</style>
