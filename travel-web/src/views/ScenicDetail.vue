<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="loadError" class="page-container" style="text-align:center;padding-top:100px">
      <el-empty description="景点不存在或已被下架">
        <el-button type="primary" @click="$router.back()">返回</el-button>
      </el-empty>
    </div>

    <template v-else>
      <!-- Cover + Gallery -->
      <div class="cover-section">
        <div class="cover-bg" :style="{ backgroundImage: `url(${scenic.coverImage || FALLBACK.scenic})` }"></div>
        <div class="cover-overlay">
          <div class="page-container cover-content">
            <el-breadcrumb separator="›" class="breadcrumb">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item :to="{ path: '/scenic' }">景点</el-breadcrumb-item>
              <el-breadcrumb-item>{{ scenic.scenicName }}</el-breadcrumb-item>
            </el-breadcrumb>
            <h1>{{ scenic.scenicName }}</h1>
            <p class="info">{{ scenic.city }} · {{ scenic.address }}</p>
            <div class="quick-stats">
              <span><el-icon><StarFilled /></el-icon> {{ '⭐'.repeat(scenic.recommendLevel || 0) }}</span>
              <span><el-icon><View /></el-icon> {{ scenic.viewCount || 0 }} 次浏览</span>
            </div>
            <div class="actions">
              <span class="price">¥{{ scenic.ticketPrice || 0 }}</span>
              <el-button type="warning" size="large" round @click="bookDialog = true" :disabled="!store.getters.isLoggedIn">
                <el-icon><Tickets /></el-icon> 立即预订
              </el-button>
              <el-button :type="fav ? 'danger' : 'default'" size="large" round @click="toggleFav" :disabled="!store.getters.isLoggedIn">
                <el-icon><Star /></el-icon> {{ fav ? '已收藏' : '收藏' }}
              </el-button>
              <el-button size="large" round @click="scrollTo('comments-section')">
                <el-icon><ChatDotRound /></el-icon> 评论
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="page-container">
        <el-row :gutter="24">
          <el-col :span="16">
            <!-- Info Card -->
            <div class="info-card">
              <el-descriptions title="📋 景点信息" :column="2" border>
                <el-descriptions-item label="开放时间">{{ scenic.openTime || '全天' }}</el-descriptions-item>
                <el-descriptions-item label="推荐等级">{{ '⭐'.repeat(scenic.recommendLevel || 0) }}</el-descriptions-item>
                <el-descriptions-item label="浏览数">{{ scenic.viewCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="标签">{{ scenic.tags || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <!-- Description -->
            <div class="info-card description-card">
              <h3>📖 景点介绍</h3>
              <div class="description" v-html="scenic.description || '暂无详细介绍'"></div>
            </div>

            <!-- Map -->
            <div class="info-card" v-if="scenic.longitude && scenic.latitude">
              <h3>📍 位置地图</h3>
              <ScenicMap :markers="mapMarkers" height="320px" />
            </div>

            <!-- Comments -->
            <div id="comments-section" class="info-card">
              <h3>💬 游客评论</h3>
              <CommentList targetType="1" :targetId="scenic.scenicId" />
            </div>
          </el-col>

          <el-col :span="8">
            <!-- Sidebar -->
            <div class="sidebar-card glass">
              <h4>💰 预订须知</h4>
              <ul>
                <li>价格：¥{{ scenic.ticketPrice || 0 }}/人</li>
                <li>城市：{{ scenic.city || '-' }}</li>
                <li>地址：{{ scenic.address || '-' }}</li>
                <li>开放时间：{{ scenic.openTime || '全天' }}</li>
              </ul>
              <el-button type="primary" size="large" round class="book-now" @click="bookDialog = true" :disabled="!store.getters.isLoggedIn">
                立即预订
              </el-button>
            </div>

            <!-- Nearby Recommendation -->
            <div class="sidebar-card" v-if="nearbyScenics.length">
              <h4>📍 附近推荐</h4>
              <div class="nearby-list">
                <div v-for="ns in nearbyScenics" :key="ns.scenicId" class="nearby-item" @click="$router.push(`/scenic/${ns.scenicId}`)">
                  <img :src="ns.coverImage || FALLBACK.scenic" :alt="ns.scenicName" @error="e => e.target.src = FALLBACK.scenic" />
                  <div>
                    <strong>{{ ns.scenicName }}</strong>
                    <span>{{ ns.city }} · ⭐{{ ns.recommendLevel }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- Book Dialog -->
      <el-dialog v-model="bookDialog" title="🎫 预订门票" width="440px" :close-on-click-modal="false">
        <el-form :model="bookForm" label-position="top">
          <el-form-item label="景点">
            <el-input :model-value="scenic.scenicName" disabled />
          </el-form-item>
          <el-form-item label="使用日期">
            <el-date-picker v-model="bookForm.useDate" type="date" placeholder="选择日期" style="width:100%" />
          </el-form-item>
          <el-form-item label="联系人">
            <el-input v-model="bookForm.contactName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="bookForm.contactPhone" placeholder="请输入手机号" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <span class="total-price">合计：<strong>¥{{ scenic.ticketPrice || 0 }}</strong></span>
            <div>
              <el-button @click="bookDialog = false">取消</el-button>
              <el-button type="primary" @click="doBook" :loading="booking">确认预订</el-button>
            </div>
          </div>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, Tickets, View, ChatDotRound } from '@element-plus/icons-vue'
import { scenicApi, orderApi, favoriteApi } from '@/api'
import { FALLBACK } from '@/utils/fallback'
import store from '@/store'
import CommentList from '@/components/CommentList.vue'
import ScenicMap from '@/components/ScenicMap.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const loadError = ref(false)
const scenic = ref({})
const fav = ref(false)
const bookDialog = ref(false)
const booking = ref(false)
const bookForm = ref({ useDate: new Date(), contactName: '', contactPhone: '' })
const nearbyScenics = ref([])

const mapMarkers = computed(() => {
  if (!scenic.value.longitude || !scenic.value.latitude) return []
  const markers = [{ id: scenic.value.scenicId, name: scenic.value.scenicName, city: scenic.value.city, lat: Number(scenic.value.latitude), lng: Number(scenic.value.longitude) }]
  nearbyScenics.value.forEach(ns => {
    if (ns.longitude && ns.latitude) {
      markers.push({ id: ns.scenicId, name: ns.scenicName, city: ns.city, lat: Number(ns.latitude), lng: Number(ns.longitude) })
    }
  })
  return markers
})

const scrollTo = (id) => {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const load = async () => {
  try {
    const r = await scenicApi.getDetail(route.params.id)
    scenic.value = r.data || {}
    if (!r.data?.scenicId) { loadError.value = true; return }
    if (store.getters.isLoggedIn) {
      try {
        const f = await favoriteApi.check({ targetType: 1, targetId: route.params.id })
        fav.value = f.data
      } catch {}
    }
    // Load nearby recommendations — 后端需要经纬度参数
    try {
      const nr = await scenicApi.nearby({ lng: scenic.value.longitude, lat: scenic.value.latitude, limit: 4 })
      nearbyScenics.value = (nr.data?.list || nr.data || []).filter(s => s.scenicId !== scenic.value.scenicId).slice(0, 4)
    } catch {}
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

const toggleFav = async () => {
  try {
    if (fav.value) {
      await favoriteApi.remove({ targetType: 1, targetId: scenic.value.scenicId })
      fav.value = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.add({ targetType: 1, targetId: scenic.value.scenicId })
      fav.value = true
      ElMessage.success('收藏成功！')
    }
  } catch {}
}

const doBook = async () => {
  if (!bookForm.value.useDate) {
    ElMessage.warning('请选择使用日期')
    return
  }
  if (!bookForm.value.contactName || !bookForm.value.contactPhone) {
    ElMessage.warning('请填写联系信息')
    return
  }
  booking.value = true
  try {
    await orderApi.seckill({
      scenicId: scenic.value.scenicId,
      contactName: bookForm.value.contactName,
      contactPhone: bookForm.value.contactPhone,
      useDate: bookForm.value.useDate?.getTime()
    })
    bookDialog.value = false
    ElMessage.success('抢购成功！请前往订单页完成支付')
    router.push('/orders')
  } catch {} finally { booking.value = false }
}

onMounted(load)
// 组件复用时（如从附近推荐跳转），重新加载数据
watch(() => route.params.id, () => {
  if (route.params.id) { loading.value = true; load() }
})
</script>

<style scoped>
/* Cover */
.cover-section {
  position: relative;
  height: 420px;
  overflow: hidden;
}

.cover-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: brightness(0.6);
  transform: scale(1.05);
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.2) 60%, rgba(0,0,0,0.1) 100%);
  display: flex;
  align-items: flex-end;
  padding-bottom: 36px;
}

.cover-content {
  width: 100%;
}

.breadcrumb {
  margin-bottom: 12px;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: rgba(255,255,255,0.7) !important;
  font-size: 0.85rem;
}

.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #fff !important;
}

.cover-content h1 {
  color: #fff;
  font-size: 2.2rem;
  font-weight: 800;
  font-family: var(--font-display);
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.cover-content .info {
  color: rgba(255,255,255,0.8);
  font-size: 1rem;
  margin: 8px 0 12px;
}

.quick-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  color: rgba(255,255,255,0.9);
  font-size: 0.9rem;
}

.quick-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.price {
  color: var(--warning);
  font-size: 1.8rem;
  font-weight: 800;
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
  margin-right: 8px;
}

/* Info Cards */
.info-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.info-card h3 {
  font-size: 1.15rem;
  font-weight: 700;
  margin-bottom: 16px;
}

.description-card { margin-top: 0; }

.description {
  line-height: 2;
  color: var(--text);
}

.description :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-sm);
  margin: 12px 0;
}

/* Sidebar */
.sidebar-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.sidebar-card h4 {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 14px;
}

.sidebar-card ul {
  list-style: none;
  padding: 0;
}

.sidebar-card ul li {
  padding: 6px 0;
  font-size: 0.88rem;
  color: var(--text-secondary);
  border-bottom: 1px dashed var(--border-light);
}

.book-now {
  width: 100%;
  margin-top: 16px;
}

/* Nearby */
.nearby-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.nearby-item {
  display: flex;
  gap: 10px;
  padding: 8px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s;
}

.nearby-item:hover {
  background: var(--bg);
}

.nearby-item img {
  width: 70px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.nearby-item strong {
  display: block;
  font-size: 0.88rem;
  margin-bottom: 2px;
}

.nearby-item span {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

/* Dialog */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-price strong {
  color: var(--danger);
  font-size: 1.3rem;
}

/* Responsive */
@media (max-width: 768px) {
  .cover-section { height: 340px; }
  .cover-content h1 { font-size: 1.6rem; }
  .price { font-size: 1.3rem; }
}
</style>
