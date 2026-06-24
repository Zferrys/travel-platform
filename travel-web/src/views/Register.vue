<template>
  <div class="auth-page">
    <div class="auth-bg">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
    </div>

    <div class="auth-card glass">
      <div class="auth-header">
        <h2>🎒 创建账号</h2>
        <p>开启你的旅行探索之旅</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleRegister" label-position="top">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（至少3位）" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" size="large" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（选填，用于展示）" size="large" :prefix-icon="UserFilled" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（选填）" size="large" :prefix-icon="Phone" />
        </el-form-item>
        <el-button type="primary" size="large" native-type="submit" :loading="loading" class="submit-btn" round>
          注 册
        </el-button>
      </el-form>

      <div class="auth-footer">
        <p>已有账号？<router-link to="/login">去登录 →</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Phone } from '@element-plus/icons-vue'
import { userApi } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '', phone: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, message: '至少3个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '至少6个字符', trigger: 'blur' }],
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userApi.register(form)
    ElMessage.success('🎉 注册成功！请登录')
    router.push('/login')
  } finally { loading.value = false }
}
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
  opacity: 0.08;
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

.auth-header { text-align: center; margin-bottom: 28px; }
.auth-header h2 { font-family: var(--font-display); font-size: 1.6rem; font-weight: 700; margin-bottom: 4px; }

.submit-btn { width: 100%; margin-top: 8px; }

.auth-footer { text-align: center; margin-top: 20px; padding-top: 20px; border-top: 1px solid var(--border-light); }
.auth-footer a { color: var(--primary); font-weight: 600; }
.auth-footer a:hover { text-decoration: underline; }
</style>
