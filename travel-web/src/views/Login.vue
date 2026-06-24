<template>
  <div class="auth-page">
    <!-- Background decoration -->
    <div class="auth-bg">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
    </div>

    <div class="auth-card glass">
      <div class="auth-header">
        <div class="auth-logo">
          <svg width="48" height="48" viewBox="0 0 32 32">
            <defs>
              <linearGradient id="logo" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#ff6b6b"/>
                <stop offset="100%" style="stop-color:#ee5a24"/>
              </linearGradient>
            </defs>
            <circle cx="16" cy="16" r="15" fill="url(#logo)" opacity="0.15"/>
            <path d="M10 22 Q16 8 22 22" fill="none" stroke="url(#logo)" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="16" cy="12" r="3" fill="#ff6b6b"/>
          </svg>
        </div>
        <h2>欢迎回来</h2>
        <p>登录漫游记，继续探索世界</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin" label-position="top">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="验证码" size="large" maxlength="4" style="flex:1" />
            <img :src="captchaImage" class="captcha-img" @click="refreshCaptcha" title="点击刷新" alt="验证码" />
          </div>
        </el-form-item>
        <el-button type="primary" size="large" native-type="submit" :loading="loading" class="submit-btn" round>
          登 录
        </el-button>
      </el-form>

      <div class="auth-footer">
        <p>还没有账号？<router-link to="/register">立即注册 →</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { userApi, captchaApi } from '@/api'
import store from '@/store'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const captchaImage = ref('')
const form = reactive({ username: '', password: '', captchaKey: '', captchaCode: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

const refreshCaptcha = async () => {
  try {
    const r = await captchaApi.generate()
    form.captchaKey = r.data.key
    captchaImage.value = r.data.image
  } catch {}
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await userApi.login(form)
    store.dispatch('login', { token: res.data.token, user: res.data.user })
    ElMessage.success('🎉 登录成功，欢迎回来！')
    router.push('/')
  } finally { loading.value = false }
}

onMounted(refreshCaptcha)
</script>

<style scoped>
.auth-page {
  position: relative;
  display: flex; align-items: center; justify-content: center;
  min-height: calc(100vh - 180px); padding: 40px 20px;
  overflow: hidden;
}

.auth-bg { position: absolute; inset: 0; overflow: hidden; }
.bg-circle {
  position: absolute; border-radius: 50%;
  opacity: 0.1;
}
.c1 { width: 500px; height: 500px; background: var(--primary); top: -150px; right: -100px; }
.c2 { width: 300px; height: 300px; background: var(--accent); bottom: -80px; left: -60px; }
.c3 { width: 200px; height: 200px; background: var(--primary-light); top: 40%; left: 10%; }

.auth-card {
  position: relative; z-index: 1;
  width: 100%; max-width: 420px;
  padding: 44px 40px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.auth-header { text-align: center; margin-bottom: 32px; }
.auth-logo { margin-bottom: 12px; }
.auth-header h2 { font-family: var(--font-display); font-size: 1.6rem; font-weight: 700; margin-bottom: 4px; }
.auth-header p { color: var(--text-secondary); font-size: 0.9rem; }

.captcha-row { display: flex; gap: 12px; align-items: center; }
.captcha-img { height: 42px; width: 120px; border-radius: 8px; cursor: pointer; border: 1px solid var(--border); object-fit: cover; }

.submit-btn { width: 100%; margin-top: 8px; }

.auth-footer { text-align: center; margin-top: 20px; padding-top: 20px; border-top: 1px solid var(--border-light); }
.auth-footer a { color: var(--primary); font-weight: 600; }
.auth-footer a:hover { text-decoration: underline; }
</style>
