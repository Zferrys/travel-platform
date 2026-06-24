<template>
  <div class="page-container">
    <el-breadcrumb separator="›" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>个人中心</el-breadcrumb-item>
    </el-breadcrumb>

    <el-row :gutter="24">
      <el-col :span="8">
        <div class="profile-card glass">
          <div class="avatar-section" @click="avatarDialog = true">
            <el-avatar :size="90" :src="user.avatar || FALLBACK.avatar" class="user-avatar" />
            <div class="avatar-overlay">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
          <h3>{{ user.nickname || user.username }}</h3>
          <p class="user-role" v-if="user.userType === 3">
            <el-tag type="danger" size="small">管理员</el-tag>
          </p>
          <div class="contact-info">
            <p><el-icon><Phone /></el-icon> {{ user.phone || '未绑定手机' }}</p>
            <p><el-icon><Message /></el-icon> {{ user.email || '未绑定邮箱' }}</p>
          </div>
          <el-button @click="editDialog = true" type="primary" round class="edit-btn">
            <el-icon><Edit /></el-icon> 编辑资料
          </el-button>
        </div>
      </el-col>

      <el-col :span="16">
        <el-tabs v-model="activeTab" class="content-tabs">
          <el-tab-pane label="我的收藏" name="fav">
            <div class="fav-list">
              <div v-for="f in favorites" :key="f.favoriteId" class="fav-item clickable" @click="goToTarget(f)">
                <div class="fav-info">
                  <img :src="getTargetCover(f)" class="fav-cover" @error="onCoverError" />
                  <div class="fav-meta">
                    <div class="fav-name-row">
                      <el-tag :type="f.targetType == 1 ? 'primary' : f.targetType == 2 ? 'success' : 'warning'" size="small">
                        {{ typeText(f.targetType) }}
                      </el-tag>
                      <span class="fav-name">{{ f.targetName || '加载中...' }}</span>
                    </div>
                    <span class="fav-id-sub">ID: {{ f.targetId }}</span>
                  </div>
                </div>
                <div class="fav-right">
                  <span class="fav-time">{{ formatTime(f.createTime) }}</span>
                  <el-icon class="fav-arrow"><ArrowRight /></el-icon>
                </div>
              </div>
              <el-empty v-if="!favorites.length" description="暂无收藏" :image-size="80">
                <el-button type="primary" @click="$router.push('/scenic')">去探索</el-button>
              </el-empty>
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的游记" name="notes">
            <div class="fav-list">
              <div v-for="n in myNotes" :key="n.noteId" class="fav-item clickable" @click="$router.push(`/note/${n.noteId}`)">
                <div class="fav-info">
                  <span class="note-title">{{ n.title }}</span>
                  <span class="note-stats">
                    <el-icon :size="12"><View /></el-icon> {{ n.viewCount || 0 }}
                    <el-icon :size="12"><StarFilled /></el-icon> {{ n.likeCount || 0 }}
                  </span>
                </div>
                <span class="fav-time">{{ n.createTime }}</span>
              </div>
              <el-empty v-if="!myNotes.length" description="暂无游记" :image-size="80">
                <el-button type="primary" @click="$router.push('/publish')">写一篇</el-button>
              </el-empty>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <!-- Avatar Dialog -->
    <el-dialog v-model="avatarDialog" title="更换头像" width="440px" :close-on-click-modal="false">
      <div class="avatar-dialog-body">
        <div class="avatar-preview">
          <el-avatar :size="120" :src="avatarPreview || user.avatar || FALLBACK.avatar" />
          <p>{{ avatarPreview ? '新头像预览' : '当前头像' }}</p>
        </div>
        <div class="avatar-upload">
          <el-upload :auto-upload="false" :show-file-list="false"
            :on-change="onAvatarFileSelect" accept="image/jpeg,image/png,image/gif,image/webp">
            <el-button type="primary" :icon="Upload">选择图片</el-button>
          </el-upload>
          <p class="upload-limit">支持 JPG/PNG/GIF/WebP，不超过 2MB</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="cancelAvatar">取消</el-button>
        <el-button type="primary" @click="doUploadAvatar" :loading="avatarUploading" :disabled="!avatarFile">
          确认上传
        </el-button>
      </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialog" title="编辑资料" width="400px" :close-on-click-modal="false">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" @click="doEdit" :loading="editing">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http, { userApi, favoriteApi, noteApi } from '@/api'
import { FALLBACK } from '@/utils/fallback'
import { ElMessage } from 'element-plus'
import { Camera, Upload, Edit, Phone, Message, View, StarFilled, ArrowRight } from '@element-plus/icons-vue'
import store from '@/store'

const route = useRoute()
const router = useRouter()
const activeTab = ref('fav')
const user = ref(store.getters.user || {})
const favorites = ref([])
const myNotes = ref([])
const editDialog = ref(false)
const editing = ref(false)
const editForm = ref({ nickname: '', phone: '', email: '' })

const avatarDialog = ref(false)
const avatarUploading = ref(false)
const avatarFile = ref(null)
const avatarPreview = ref('')

const typeText = (t) => ({ '1': '景点', '2': '酒店', '3': '游记' }[String(t)] || '-')

const getTargetCover = (f) => f._cover || FALLBACK.scenic

const onCoverError = (e) => {
  e.target.src = FALLBACK.scenic
}

const goToTarget = (f) => {
  const t = Number(f.targetType)
  const id = f.targetId
  if (t === 1) router.push(`/scenic/${id}`)
  else if (t === 2) router.push(`/hotel/${id}`)
  else if (t === 3) router.push(`/note/${id}`)
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
}

const onAvatarFileSelect = (file) => {
  const raw = file.raw
  if (!raw) return
  if (raw.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 2MB')
    return
  }
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowed.includes(raw.type)) {
    ElMessage.warning('仅支持 JPG/PNG/GIF/WebP 格式')
    return
  }
  avatarFile.value = raw
  const reader = new FileReader()
  reader.onload = (e) => { avatarPreview.value = e.target.result }
  reader.readAsDataURL(raw)
}

const doUploadAvatar = async () => {
  if (!avatarFile.value) return
  avatarUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', avatarFile.value)
    const r = await http.post('/user/avatar', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    const newAvatar = r.data.data || r.data
    store.commit('SET_USER', { ...store.getters.user, avatar: newAvatar })
    user.value.avatar = newAvatar
    ElMessage.success('头像更换成功！')
    cancelAvatar()
  } catch { ElMessage.error('头像上传失败') }
  finally { avatarUploading.value = false }
}

const cancelAvatar = () => {
  avatarDialog.value = false
  avatarFile.value = null
  avatarPreview.value = ''
}

const load = async () => {
  const [favRes, noteRes] = await Promise.all([
    favoriteApi.getList().catch(() => ({ data: [] })),
    noteApi.getMy().catch(() => ({ data: { list: [] } }))
  ])
  // Pre-parse cover images to avoid re-parsing on every render
  favorites.value = (favRes.data || []).map(f => {
    if (f.targetType == 2 && f.targetCover) {
      try { f._cover = JSON.parse(f.targetCover)[0] || FALLBACK.hotel } catch { f._cover = FALLBACK.hotel }
    } else {
      f._cover = f.targetCover || FALLBACK.scenic
    }
    return f
  })
  myNotes.value = noteRes.data?.list || []
}

const doEdit = async () => {
  editing.value = true
  try {
    await userApi.updateUserInfo(editForm.value)
    const r = await userApi.getUserInfo()
    store.commit('SET_USER', r.data)
    user.value = r.data
    editDialog.value = false
    ElMessage.success('资料更新成功')
  } finally { editing.value = false }
}

onMounted(() => {
  // 支持 ?tab=notes 从导航菜单跳转过来
  if (route.query.tab === 'notes') activeTab.value = 'notes'
  load()
  const u = store.getters.user
  if (u) editForm.value = { nickname: u.nickname || '', phone: u.phone || '', email: u.email || '' }
})
</script>

<style scoped>
.breadcrumb { margin-bottom: 16px; }
.breadcrumb :deep(.el-breadcrumb__inner) { color: var(--text-secondary) !important; font-size: 0.85rem; }

.profile-card {
  text-align: center; padding: 32px 24px;
  border-radius: var(--radius-lg);
  background: var(--bg-card);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}

.avatar-section { position: relative; display: inline-block; cursor: pointer; border-radius: 50%; margin-bottom: 16px; }
.avatar-section:hover .avatar-overlay { opacity: 1; }
.user-avatar { border: 3px solid var(--border-light); transition: border-color 0.2s; }
.avatar-section:hover .user-avatar { border-color: var(--primary); }
.avatar-overlay {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,0.45); display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: #fff;
  opacity: 0; transition: opacity 0.25s; font-size: 0.8rem; gap: 4px;
}

.profile-card h3 { font-size: 1.2rem; font-weight: 700; margin-bottom: 7px; }
.user-role { margin-bottom: 12px; }

.contact-info { margin: 16px 0; }
.contact-info p {
  display: flex; align-items: center; justify-content: center;
  gap: 6px; font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 6px;
}

.edit-btn { width: 100%; }

.content-tabs { background: var(--bg-card); border-radius: var(--radius); padding: 8px 20px 20px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); }

.fav-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 14px; border-radius: var(--radius-sm);
  transition: all 0.2s; border-bottom: 1px solid var(--border-light);
}
.fav-item:hover { background: var(--bg); transform: translateX(4px); }
.clickable { cursor: pointer; }
.fav-info { display: flex; gap: 12px; align-items: center; flex: 1; min-width: 0; }
.fav-cover {
  width: 56px; height: 56px; border-radius: 8px; object-fit: cover;
  flex-shrink: 0; background: var(--bg);
}
.fav-meta { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.fav-name-row { display: flex; align-items: center; gap: 8px; }
.fav-name {
  font-weight: 500; font-size: 0.92rem; white-space: nowrap;
  overflow: hidden; text-overflow: ellipsis;
}
.fav-id-sub { font-size: 0.75rem; color: #b2bec3; }
.fav-right { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.fav-time { font-size: 0.78rem; color: var(--text-secondary); }
.fav-arrow { color: #b2bec3; font-size: 0.9rem; transition: color 0.2s; }
.fav-item:hover .fav-arrow { color: var(--primary); }
.note-title { font-weight: 500; font-size: 0.92rem; }
.note-stats { display: flex; gap: 8px; font-size: 0.78rem; color: var(--text-secondary); align-items: center; }

.avatar-dialog-body { display: flex; gap: 32px; align-items: center; }
.avatar-preview { text-align: center; }
.avatar-preview p { margin-top: 8px; font-size: 0.85rem; color: var(--text-secondary); }
.avatar-upload { text-align: center; }
.upload-limit { font-size: 0.75rem; color: var(--text-secondary); margin-top: 8px; }
</style>
