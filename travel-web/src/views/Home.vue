<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero-banner">
      <div class="hero-content">
        <div class="hero-badge pulse">✨ 探索世界之美</div>
        <h1>漫游记</h1>
        <p class="hero-subtitle">发现属于你的旅行故事，让每一次出发都充满期待</p>
        <div class="hero-search-wrapper">
          <div class="hero-search">
            <el-input
              v-model="keyword"
              size="large"
              placeholder="搜索景点、酒店、游记..."
              class="search-input"
              @keyup.enter="doSearch"
              :prefix-icon="Search"
            >
              <template #append>
                <el-button type="primary" @click="doSearch" class="search-btn">
                  探索
                </el-button>
              </template>
            </el-input>
          </div>
          <div class="hero-tags">
            <span
              v-for="t in hotTags"
              :key="t.name"
              class="hero-tag"
              @click="keyword = t.name; doSearch()"
            >
              {{ t.icon }} {{ t.name }}
            </span>
          </div>
        </div>
      </div>
      <!-- Stats -->
      <div class="hero-stats">
        <div class="stat-item" v-for="stat in stats" :key="stat.label">
          <span class="stat-number">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </section>

    <div class="page-container">
      <!-- Featured Destinations Carousel -->
      <section class="featured-section">
        <h2 class="section-title">
          <span>🔥 热门目的地</span>
        </h2>
        <div class="featured-carousel" v-if="scenics.length">
          <div
            v-for="(item, idx) in featuredScenics"
            :key="item.scenicId"
            class="featured-card"
            :style="{ animationDelay: idx * 0.1 + 's' }"
            @click="$router.push(`/scenic/${item.scenicId}`)"
          >
            <div class="featured-img">
              <img :src="item.coverImage || FALLBACK.scenic" :alt="item.scenicName" @error="e => e.target.src = FALLBACK.scenic" />
              <div class="featured-overlay">
                <span class="featured-city">{{ item.city }}</span>
                <span class="featured-level">{{ '⭐'.repeat(item.recommendLevel || 3) }}</span>
              </div>
            </div>
            <div class="featured-info">
              <h3>{{ item.scenicName }}</h3>
              <p>{{ item.address?.slice(0, 20) || '探索精彩目的地' }}</p>
              <span class="featured-price" v-if="item.ticketPrice">¥{{ item.ticketPrice }}起</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Quick Category -->
      <section class="quick-categories">
        <div class="category-card" v-for="cat in categories" :key="cat.label" @click="$router.push(cat.link)">
          <div class="cat-icon" :style="{ background: cat.bg }">
            <el-icon :size="28" color="#fff"><component :is="cat.icon" /></el-icon>
          </div>
          <span>{{ cat.label }}</span>
        </div>
      </section>

      <!-- 热门景点 -->
      <h2 class="section-title">
        <span>🏔️ 热门景点</span>
        <el-button text class="view-all" @click="$router.push('/scenic')">查看全部 →</el-button>
      </h2>
      <div v-if="scenics.length" class="card-grid">
        <ScenicCard v-for="(s, i) in scenics" :key="s.scenicId" :scenic="s" :style="{ animationDelay: i * 0.08 + 's' }" class="fade-in-up" />
      </div>
      <div v-else class="skeleton-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="skeleton skeleton-image"></div>
          <div class="skeleton skeleton-title"></div>
          <div class="skeleton skeleton-text" style="width:80%"></div>
        </div>
      </div>

      <!-- 精选酒店 -->
      <h2 class="section-title">
        <span>🏨 精选酒店</span>
        <el-button text class="view-all" @click="$router.push('/hotel')">查看全部 →</el-button>
      </h2>
      <div v-if="hotels.length" class="card-grid">
        <HotelCard v-for="(h, i) in hotels" :key="h.hotelId" :hotel="h" :style="{ animationDelay: i * 0.08 + 's' }" class="fade-in-up" />
      </div>
      <div v-else class="skeleton-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="skeleton skeleton-image"></div>
          <div class="skeleton skeleton-title"></div>
          <div class="skeleton skeleton-text" style="width:60%"></div>
        </div>
      </div>

      <!-- 精彩游记 -->
      <h2 class="section-title">
        <span>📝 精彩游记</span>
        <el-button text class="view-all" @click="$router.push('/notes')">查看全部 →</el-button>
      </h2>
      <div v-if="notes.length" class="card-grid">
        <NoteCard v-for="(n, i) in notes" :key="n.noteId" :note="n" :style="{ animationDelay: i * 0.08 + 's' }" class="fade-in-up" />
      </div>
      <div v-else class="skeleton-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="skeleton skeleton-image"></div>
          <div class="skeleton skeleton-title"></div>
          <div class="skeleton skeleton-text" style="width:70%"></div>
        </div>
      </div>

      <!-- Newsletter -->
      <section class="newsletter">
        <div class="newsletter-card glass">
          <div class="newsletter-content">
            <h2>🎒 开启你的旅程</h2>
            <p>注册漫游记，发现更多精彩目的地，记录你的旅行故事</p>
            <div class="newsletter-actions">
              <el-button type="primary" size="large" round @click="$router.push('/register')" v-if="!store.getters.isLoggedIn">免费注册</el-button>
              <el-button size="large" round @click="$router.push('/publish')" v-else>写一篇游记</el-button>
              <el-button size="large" round @click="$router.push('/scenic')">探索景点</el-button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, LocationFilled, OfficeBuilding, Notebook, Place, Promotion, Tickets
} from '@element-plus/icons-vue'
import { scenicApi, hotelApi, noteApi } from '@/api'
import { FALLBACK } from '@/utils/fallback'
import store from '@/store'
import ScenicCard from '@/components/ScenicCard.vue'
import HotelCard from '@/components/HotelCard.vue'
import NoteCard from '@/components/NoteCard.vue'

const router = useRouter()
const keyword = ref('')
const scenics = ref([])
const hotels = ref([])
const notes = ref([])

const hotTags = [
  { name: '北京', icon: '🏯' },
  { name: '西湖', icon: '🌊' },
  { name: '成都', icon: '🐼' },
  { name: '三亚', icon: '🏖️' },
  { name: '古城', icon: '🏛️' },
  { name: '雪山', icon: '🏔️' },
]

const stats = ref([
  { label: '热门景点', value: '0' },
  { label: '精选酒店', value: '0' },
  { label: '精彩游记', value: '0' },
  { label: '注册用户', value: '0' },
])

const categories = [
  { label: '景点', icon: 'Place', link: '/scenic', bg: 'linear-gradient(135deg, #ff6b6b, #ee5a24)' },
  { label: '酒店', icon: 'OfficeBuilding', link: '/hotel', bg: 'linear-gradient(135deg, #0abde3, #0984e3)' },
  { label: '游记', icon: 'Notebook', link: '/notes', bg: 'linear-gradient(135deg, #f368e0, #be2edd)' },
  { label: '订单', icon: 'Tickets', link: '/orders', bg: 'linear-gradient(135deg, #10ac84, #1dd1a1)' },
]

const featuredScenics = computed(() => scenics.value.filter(s => s.recommendLevel >= 4).slice(0, 4))

const doSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { q: keyword.value.trim() } })
  }
}

onMounted(async () => {
  try {
    const r = await scenicApi.getHot({ limit: 6 })
    scenics.value = r.data || []
    stats.value[0].value = scenics.value.length || r.data?.length || '12'
  } catch { /* 骨架屏展示中 */ }

  try {
    const r = await hotelApi.getList()
    hotels.value = (r.data || []).slice(0, 6)
    stats.value[1].value = hotels.value.length || '32'
  } catch {}

  try {
    const r = await noteApi.getList()
    notes.value = (r.data.list || []).slice(0, 6)
    stats.value[2].value = notes.value.length || '87'
  } catch {}

  stats.value[3].value = '1,280+'
})
</script>

<style scoped>
/* Hero Content */
.hero-content {
  position: relative; z-index: 1;
  max-width: 700px; margin: 0 auto;
}

.hero-badge {
  display: inline-block;
  padding: 6px 20px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 30px;
  font-size: 0.85rem;
  font-weight: 600;
  letter-spacing: 2px;
  margin-bottom: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.hero-subtitle {
  font-size: 1.15rem !important;
  opacity: 0.85 !important;
  font-weight: 300 !important;
}

.hero-search-wrapper {
  max-width: 560px; margin: 0 auto;
}

.hero-search {
  margin-bottom: 16px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 30px !important;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15) !important;
  border: none !important;
  height: 52px;
}

.search-btn {
  border-radius: 0 30px 30px 0 !important;
  padding: 0 28px !important;
}

.hero-tags {
  display: flex; gap: 10px; justify-content: center; flex-wrap: wrap;
}

.hero-tag {
  padding: 6px 18px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 25px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.25s;
}

.hero-tag:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
}

/* Stats */
.hero-stats {
  display: flex; gap: 40px; justify-content: center;
  margin-top: 48px;
  position: relative; z-index: 1;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 2rem; font-weight: 800;
  font-family: var(--font-display);
  letter-spacing: 1px;
}

.stat-label {
  font-size: 0.8rem; opacity: 0.8;
  letter-spacing: 1px;
}

/* Featured Carousel */
.featured-section { margin-bottom: 16px; }

.featured-carousel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.featured-card {
  position: relative; border-radius: var(--radius);
  overflow: hidden; cursor: pointer;
  box-shadow: var(--shadow);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.featured-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
}

.featured-img {
  position: relative; height: 220px; overflow: hidden;
}

.featured-img img {
  width: 100%; height: 100%; object-fit: cover;
  transition: transform 0.5s;
}

.featured-card:hover .featured-img img {
  transform: scale(1.08);
}

.featured-overlay {
  position: absolute; top: 12px; left: 12px; right: 12px;
  display: flex; justify-content: space-between;
}

.featured-city {
  padding: 4px 14px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  color: #fff;
  font-size: 0.78rem;
  font-weight: 500;
}

.featured-level {
  padding: 4px 12px;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  font-size: 0.7rem;
}

.featured-info {
  padding: 16px;
  background: var(--bg-card);
}

.featured-info h3 {
  font-size: 1.05rem; font-weight: 600; margin-bottom: 4px;
}

.featured-info p {
  font-size: 0.82rem; color: var(--text-secondary);
}

.featured-price {
  display: inline-block; margin-top: 8px;
  padding: 4px 14px;
  background: linear-gradient(135deg, #fff5f5, #ffe0e0);
  color: var(--primary-dark);
  font-weight: 700; font-size: 0.85rem;
  border-radius: 20px;
}

/* Quick Categories */
.quick-categories {
  display: flex; gap: 16px; justify-content: center;
  margin: 32px 0 16px;
  flex-wrap: wrap;
}

.category-card {
  display: flex; flex-direction: column;
  align-items: center; gap: 10px;
  padding: 20px 24px;
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.3s;
  min-width: 100px;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.cat-icon {
  width: 52px; height: 52px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
}

.category-card span {
  font-size: 0.85rem; font-weight: 600; color: var(--text);
}

/* View All */
.view-all {
  margin-left: auto !important;
  color: var(--primary) !important;
  font-size: 0.9rem;
}

/* Skeleton */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.skeleton-card {
  padding: 16px;
}

/* Newsletter */
.newsletter { margin-top: 48px; }

.newsletter-card {
  border-radius: var(--radius-lg);
  padding: 48px 32px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.newsletter-card::before {
  content: '';
  position: absolute;
  top: -50%; left: -50%;
  width: 200%; height: 200%;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.08) 0%, transparent 60%);
  animation: heroFloat 15s linear infinite;
}

.newsletter-content {
  position: relative; z-index: 1;
}

.newsletter-content h2 {
  font-family: var(--font-display);
  font-size: 1.8rem; font-weight: 700; margin-bottom: 8px;
}

.newsletter-content p {
  color: var(--text-secondary); margin-bottom: 24px;
}

.newsletter-actions {
  display: flex; gap: 12px; justify-content: center;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-stats { gap: 24px; }
  .stat-number { font-size: 1.5rem; }
  .newsletter-card { padding: 32px 20px; }
  .featured-carousel { grid-template-columns: 1fr 1fr; }
}

@media (max-width: 480px) {
  .featured-carousel { grid-template-columns: 1fr; }
  .hero-stats { gap: 16px; }
  .stat-number { font-size: 1.2rem; }
}
</style>
