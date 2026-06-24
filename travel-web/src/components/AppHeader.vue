<template>
  <header class="app-header" :class="{ scrolled: scrolled, 'menu-open': mobileMenuOpen }">
    <div class="header-inner">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <div class="logo-icon">
          <svg width="32" height="32" viewBox="0 0 32 32">
            <defs>
              <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#ff6b6b"/>
                <stop offset="100%" style="stop-color:#ee5a24"/>
              </linearGradient>
            </defs>
            <circle cx="16" cy="16" r="15" fill="url(#logoGrad)" opacity="0.15"/>
            <path d="M10 22 Q16 8 22 22" fill="none" stroke="url(#logoGrad)" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="16" cy="12" r="3" fill="#ff6b6b"/>
          </svg>
        </div>
        <span class="logo-text">漫游记</span>
      </router-link>

      <!-- Desktop Navigation -->
      <nav class="nav-links">
        <router-link to="/" class="nav-item" exact-active-class="active">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </router-link>
        <router-link to="/scenic" class="nav-item" active-class="active">
          <el-icon><LocationFilled /></el-icon>
          <span>景点</span>
        </router-link>
        <router-link to="/hotel" class="nav-item" active-class="active">
          <el-icon><OfficeBuilding /></el-icon>
          <span>酒店</span>
        </router-link>
        <router-link to="/notes" class="nav-item" active-class="active">
          <el-icon><Notebook /></el-icon>
          <span>游记</span>
        </router-link>
      </nav>

      <!-- Right Side -->
      <div class="header-right">
        <!-- Search -->
        <div class="search-trigger" @click="showSearch = !showSearch">
          <el-icon :size="20"><Search /></el-icon>
        </div>
        <transition name="slide-down">
          <div v-if="showSearch" class="search-dropdown">
            <el-input
              ref="searchInput"
              v-model="searchKeyword"
              placeholder="搜索景点、酒店、游记..."
              :prefix-icon="Search"
              size="large"
              @keyup.enter="doSearch"
              @blur="closeSearch"
            />
          </div>
        </transition>

        <!-- User Menu -->
        <template v-if="store.getters.isLoggedIn">
          <el-dropdown @command="handleCommand" trigger="click" placement="bottom-end">
            <div class="user-area">
              <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99">
                <el-avatar :size="36" :src="store.getters.user?.avatar" class="user-avatar" />
              </el-badge>
              <span class="nickname">{{ store.getters.user?.nickname || store.getters.user?.username }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="user">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><Tickets /></el-icon>我的订单
                </el-dropdown-item>
                <el-dropdown-item command="notes">
                  <el-icon><Notebook /></el-icon>我的游记
                </el-dropdown-item>
                <el-dropdown-item command="publish">
                  <el-icon><Edit /></el-icon>写游记
                </el-dropdown-item>
                <el-dropdown-item v-if="store.getters.user?.userType === 3" command="admin" divided>
                  <el-icon><Setting /></el-icon>后台管理
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <div class="auth-buttons">
            <el-button text class="login-btn" @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" round @click="$router.push('/register')">免费注册</el-button>
          </div>
        </template>

        <!-- Mobile Toggle -->
        <div class="mobile-toggle" @click="mobileMenuOpen = !mobileMenuOpen">
          <span :class="{ open: mobileMenuOpen }"></span>
        </div>
      </div>
    </div>

    <!-- Mobile Menu -->
    <transition name="slide-down">
      <div v-if="mobileMenuOpen" class="mobile-menu">
        <router-link to="/" @click="mobileMenuOpen = false">首页</router-link>
        <router-link to="/scenic" @click="mobileMenuOpen = false">景点</router-link>
        <router-link to="/hotel" @click="mobileMenuOpen = false">酒店</router-link>
        <router-link to="/notes" @click="mobileMenuOpen = false">游记</router-link>
        <div class="mobile-search">
          <el-input v-model="searchKeyword" placeholder="搜索..." @keyup.enter="doSearch" size="large" />
        </div>
      </div>
    </transition>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Search, HomeFilled, LocationFilled, OfficeBuilding,
  Notebook, User, Tickets, Edit, Setting, SwitchButton, ArrowDown
} from '@element-plus/icons-vue'
import store from '@/store'

const router = useRouter()
const route = useRoute()
const scrolled = ref(false)
const mobileMenuOpen = ref(false)
const showSearch = ref(false)
const searchKeyword = ref('')
const searchInput = ref(null)
const unreadCount = ref(0)

const onScroll = () => { scrolled.value = window.scrollY > 10 }

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

// Close mobile menu on route change
watch(() => route.path, () => { mobileMenuOpen.value = false; showSearch.value = false })

const closeSearch = () => {
  setTimeout(() => { showSearch.value = false }, 200)
}

const doSearch = () => {
  if (searchKeyword.value.trim()) {
    showSearch.value = false
    mobileMenuOpen.value = false
    router.push({ path: '/search', query: { q: searchKeyword.value.trim() } })
  }
}

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    store.dispatch('logout')
    router.push('/')
  } else if (cmd === 'notes') {
    // "我的游记" → 跳转到个人中心的"我的游记"标签页
    router.push('/user?tab=notes')
  } else {
    const map = {
      user: '/user',
      orders: '/orders',
      publish: '/publish',
      admin: '/admin',
    }
    router.push(map[cmd])
  }
}
</script>

<style scoped>
.app-header {
  position: fixed; top: 0; left: 0; right: 0; z-index: 1000;
  height: 72px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.app-header.scrolled {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 1px 20px rgba(0, 0, 0, 0.06);
  border-bottom-color: var(--border-light);
}

.header-inner {
  max-width: 1240px; margin: 0 auto; padding: 0 24px;
  display: flex; align-items: center; height: 100%; gap: 12px;
}

/* Logo */
.logo {
  display: flex; align-items: center; gap: 8px;
  font-size: 1.35rem; font-weight: 800;
  font-family: var(--font-display);
  color: var(--text);
  flex-shrink: 0;
  margin-right: 16px;
}

.logo-icon { display: flex; align-items: center; }

/* Navigation */
.nav-links {
  display: flex; gap: 4px; align-items: center;
}

.nav-item {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 12px;
  font-size: 0.92rem; font-weight: 500;
  color: var(--text-secondary);
  transition: all 0.2s ease;
  position: relative;
}

.nav-item:hover {
  color: var(--primary);
  background: rgba(255, 107, 107, 0.06);
}

.nav-item.active {
  color: var(--primary);
  background: rgba(255, 107, 107, 0.1);
  font-weight: 600;
}

/* Search */
.header-right {
  display: flex; align-items: center; gap: 12px;
  margin-left: auto;
}

.search-trigger {
  width: 40px; height: 40px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
}
.search-trigger:hover {
  background: var(--bg);
  color: var(--primary);
}

.search-dropdown {
  position: absolute; top: 72px; right: 24px;
  width: 360px; z-index: 100;
}

/* User Area */
.user-area {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; padding: 4px 12px 4px 4px;
  border-radius: 40px;
  transition: all 0.2s;
}

.user-area:hover {
  background: var(--bg);
}

.user-avatar {
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.user-area:hover .user-avatar {
  border-color: var(--primary-light);
}

.nickname {
  font-size: 0.9rem; font-weight: 500;
  max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.arrow {
  font-size: 0.7rem; color: var(--text-secondary);
}

/* Auth Buttons */
.auth-buttons {
  display: flex; align-items: center; gap: 8px;
}

.login-btn { color: var(--text-secondary); }
.login-btn:hover { color: var(--primary); }

/* Logout Item */
.logout-item { color: var(--danger) !important; }

/* Mobile */
.mobile-toggle {
  display: none; width: 28px; height: 20px;
  position: relative; cursor: pointer;
}

.mobile-toggle span,
.mobile-toggle span::before,
.mobile-toggle span::after {
  position: absolute; height: 2px; width: 100%;
  background: var(--text); border-radius: 2px;
  transition: all 0.3s;
}

.mobile-toggle span { top: 50%; transform: translateY(-50%); }
.mobile-toggle span::before { content: ''; top: -8px; }
.mobile-toggle span::after { content: ''; top: 8px; }

.mobile-toggle span.open { background: transparent; }
.mobile-toggle span.open::before { top: 0; transform: rotate(45deg); }
.mobile-toggle span.open::after { top: 0; transform: rotate(-45deg); }

.mobile-menu {
  display: none; position: absolute; top: 72px; left: 0; right: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  padding: 16px 24px;
  border-bottom: 1px solid var(--border);
  box-shadow: var(--shadow-lg);
}

.mobile-menu a {
  display: block; padding: 12px 0;
  font-size: 1rem; font-weight: 500;
  border-bottom: 1px solid var(--border-light);
}

.mobile-search { margin-top: 12px; }

/* Transitions */
.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.25s ease;
}
.slide-down-enter-from, .slide-down-leave-to {
  opacity: 0; transform: translateY(-8px);
}

/* Responsive */
@media (max-width: 768px) {
  .nav-links { display: none; }
  .nickname { display: none; }
  .mobile-toggle { display: block; }
  .mobile-menu { display: block; }
}

@media (max-width: 480px) {
  .auth-buttons .login-btn { display: none; }
  .search-dropdown { right: 8px; width: calc(100vw - 16px); }
}
</style>
