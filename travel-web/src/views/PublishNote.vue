<template>
  <div class="page-container publish-container">
    <el-breadcrumb separator="›" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/notes' }">游记</el-breadcrumb-item>
      <el-breadcrumb-item>{{ isEdit ? '编辑游记' : '写游记' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="publish-layout">
      <!-- 左侧：编辑区 -->
      <div class="publish-editor">
        <h2>{{ isEdit ? '✏️ 编辑游记' : '✍️ 写游记' }}</h2>
        <p class="subtitle">分享你的旅行故事和精彩瞬间</p>

        <el-form :model="form" ref="formRef" :rules="rules" label-position="top">
          <el-form-item label="封面图片（可选）">
            <el-upload :action="''" :auto-upload="false" :show-file-list="false"
              :on-change="onCoverChange" accept="image/*" drag class="cover-upload">
              <div v-if="form.coverImage" class="cover-preview-wrap">
                <img :src="form.coverImage" class="cover-preview-img" alt="封面预览" />
                <span class="cover-change-tip">点击更换封面</span>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon :size="40" color="#b2bec3"><Plus /></el-icon>
                <p>点击或拖拽上传封面图片</p>
                <p class="tip">支持 JPG/PNG，不超过5MB</p>
              </div>
            </el-upload>
          </el-form-item>

          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="给你的游记起个吸引人的标题" size="large"
              maxlength="100" show-word-limit class="title-input" />
          </el-form-item>

          <el-form-item label="内容" prop="content">
            <div class="editor-toolbar">
              <el-radio-group v-model="editorTab" size="small">
                <el-radio-button value="edit">✏️ 编辑</el-radio-button>
                <el-radio-button value="preview">👁️ 手机预览</el-radio-button>
              </el-radio-group>
            </div>
            <div v-show="editorTab === 'edit'">
              <el-input v-model="form.content" type="textarea" :rows="18"
                placeholder="写下你的旅行故事...&#10;&#10;可以分享：&#10;- 旅行路线和规划&#10;- 景点推荐和心得&#10;- 美食和住宿体验&#10;- 旅行中的趣事" />
            </div>
            <div v-show="editorTab === 'preview'" class="mobile-preview-box" v-html="previewHtml"></div>
          </el-form-item>

          <div class="form-actions">
            <el-button size="large" @click="$router.back()">取消</el-button>
            <el-button type="primary" size="large" @click="submit" :loading="submitting" round>
              <el-icon><Promotion /></el-icon> {{ isEdit ? '保存修改' : '发布游记' }}
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- 右侧：实时预览卡片 -->
      <div class="publish-sidebar">
        <div class="sidebar-card preview-card">
          <h3>👁️ 实时预览</h3>
          <div class="sidebar-preview-box" v-html="previewHtml || '<p style=color:#b2bec3>在这里输入内容后，可实时预览效果...</p>'"></div>
        </div>

        <div class="sidebar-card tips-card">
          <h3>💡 写作提示</h3>
          <ul>
            <li><strong>好标题</strong>能吸引更多读者点击</li>
            <li>分享<strong>实用的攻略</strong>和路线规划</li>
            <li>配上<strong>高质量图片</strong>更有感染力</li>
            <li>讲述旅途中的<strong>真实感受和趣事</strong></li>
            <li>适当使用<strong>分段和换行</strong>让阅读更轻松</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Promotion } from '@element-plus/icons-vue'
import { noteApi, fileApi } from '@/api'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const form = ref({ title: '', content: '', coverImage: '' })
const submitting = ref(false)
const editorTab = ref('edit')

const editNoteId = computed(() => route.query.id || null)
const isEdit = computed(() => !!editNoteId.value)

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const previewHtml = computed(() => {
  if (!form.value.content) return '<p style="color:#b2bec3">预览内容将显示在这里...</p>'
  return form.value.content
    .split('\n\n').filter(p => p.trim())
    .map(p => `<p>${p.replace(/\n/g, '<br>')}</p>`)
    .join('')
})

const onCoverChange = async (file) => {
  try {
    const r = await fileApi.upload(file.raw)
    form.value.coverImage = r.data
  } catch {}
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const payload = { ...form.value }
  if (!payload.coverImage) delete payload.coverImage  // prevent clearing existing cover
  submitting.value = true
  try {
    if (isEdit.value) {
      await noteApi.update(editNoteId.value, payload)
      ElMessage.success('游记更新成功')
    } else {
      await noteApi.publish(payload)
      ElMessage.success('🎉 游记发布成功！')
    }
    router.push('/notes')
  } catch {} finally { submitting.value = false }
}

onMounted(async () => {
  if (editNoteId.value) {
    try {
      const r = await noteApi.getDetail(editNoteId.value)
      const note = r.data
      if (note) {
        form.value = { title: note.title || '', content: note.content || '', coverImage: note.coverImage || '' }
      }
    } catch { router.push('/notes') }
  }
})
</script>

<style scoped>
.breadcrumb { margin-bottom: 20px; }
.breadcrumb :deep(.el-breadcrumb__inner) { color: var(--text-secondary) !important; font-size: 0.85rem; }

.publish-container { max-width: 1280px; }

.publish-layout {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 28px;
  align-items: start;
}

.publish-editor {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 36px 40px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border-light);
}

.publish-editor h2 {
  font-family: var(--font-display);
  font-size: 1.6rem; font-weight: 700; margin-bottom: 4px;
}

.subtitle { color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 28px; }

.cover-upload :deep(.el-upload-dragger) {
  border-radius: var(--radius);
  border: 2px dashed var(--border);
  transition: all 0.2s;
}
.cover-upload :deep(.el-upload-dragger:hover) {
  border-color: var(--primary);
}

.cover-preview-wrap { position: relative; }
.cover-preview-img { max-width: 100%; max-height: 200px; border-radius: var(--radius-sm); object-fit: cover; }
.cover-change-tip { position: absolute; bottom: 10px; left: 50%; transform: translateX(-50%); background: rgba(0,0,0,0.6); color: #fff; padding: 4px 16px; border-radius: 20px; font-size: 0.78rem; }

.upload-placeholder { text-align: center; padding: 24px; color: var(--text-secondary); }
.upload-placeholder p { margin-top: 8px; font-size: 0.9rem; }
.upload-placeholder .tip { font-size: 0.78rem; color: #b2bec3; }

.title-input :deep(.el-input__inner) { font-size: 1.1rem; font-weight: 600; }

.editor-toolbar { margin-bottom: 8px; }
.editor-toolbar :deep(.el-radio-button__inner) { font-size: 0.85rem; }
:deep(.el-textarea__inner) { font-size: 15px; line-height: 1.8; min-height: 400px; border-radius: var(--radius-sm); }

.mobile-preview-box {
  min-height: 400px; padding: 20px; background: var(--bg);
  border-radius: var(--radius-sm); line-height: 2; font-size: 15px;
  border: 1px solid var(--border-light);
}

.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--border-light); }

/* 右侧预览卡片 */
.publish-sidebar { position: sticky; top: 24px; }

.sidebar-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.sidebar-card h3 { font-size: 1.05rem; font-weight: 700; margin-bottom: 14px; }

.sidebar-preview-box {
  padding: 16px; background: var(--bg);
  border-radius: var(--radius-sm); line-height: 1.9;
  font-size: 14px; min-height: 180px; max-height: 460px;
  overflow-y: auto; border: 1px solid var(--border-light);
}

.tips-card ul { list-style: none; padding: 0; }
.tips-card li {
  padding: 8px 0; font-size: 0.88rem; color: var(--text);
  border-bottom: 1px dashed var(--border-light); line-height: 1.6;
}
.tips-card li:last-child { border-bottom: none; }
.tips-card li strong { color: var(--primary); }

@media (max-width: 1024px) {
  .publish-layout {
    grid-template-columns: 1fr;
  }
  .publish-sidebar { position: static; }
  .publish-editor { padding: 24px 20px; }
}
</style>
