<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="loadError" class="page-container" style="text-align:center;padding-top:100px">
      <el-empty description="酒店不存在或已下架">
        <el-button type="primary" @click="$router.back()">返回</el-button>
      </el-empty>
    </div>

    <template v-else>
      <div class="cover-section">
        <div class="cover-bg" :style="{ backgroundImage: `url(${coverImg})` }"></div>
        <div class="cover-overlay">
          <div class="page-container cover-content">
            <el-breadcrumb separator="›" class="breadcrumb">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item :to="{ path: '/hotel' }">酒店</el-breadcrumb-item>
              <el-breadcrumb-item>{{ hotel.hotelName }}</el-breadcrumb-item>
            </el-breadcrumb>
            <div class="star-row">{{ '⭐'.repeat(hotel.starLevel || 0) }}</div>
            <h1>{{ hotel.hotelName }}</h1>
            <p>{{ hotel.city }} · {{ hotel.address }}</p>
          </div>
        </div>
      </div>

      <div class="page-container">
        <el-row :gutter="24">
          <el-col :span="16">
            <div class="info-card">
              <el-descriptions title="🏨 酒店信息" :column="2" border>
                <el-descriptions-item label="电话">{{ hotel.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="设施">{{ hotel.facilities || '-' }}</el-descriptions-item>
                <el-descriptions-item label="城市">{{ hotel.city || '-' }}</el-descriptions-item>
                <el-descriptions-item label="地址">{{ hotel.address || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="info-card" v-if="hotel.description">
              <h3>📖 酒店介绍</h3>
              <p class="desc">{{ hotel.description }}</p>
            </div>

            <!-- Map -->
            <div class="info-card" v-if="hotel.longitude && hotel.latitude">
              <h3>📍 酒店位置</h3>
              <ScenicMap :markers="hotelMarkers" height="300px" />
            </div>

            <!-- Rooms -->
            <div class="info-card">
              <h3>🛏️ 可选房型</h3>
              <div class="room-list">
                <div v-for="room in rooms" :key="room.roomId" class="room-card">
                  <div class="room-info">
                    <h4>{{ room.roomType }}</h4>
                    <p>{{ room.bedType || '标准床型' }} | {{ room.facilities || '标准配置' }}</p>
                    <el-tag :type="room.stock > 0 ? 'success' : 'danger'" size="small">
                      {{ room.stock > 0 ? `库存 ${room.stock} 间` : '已售罄' }}
                    </el-tag>
                  </div>
                  <div class="room-price">
                    <span class="price">¥{{ room.price }}</span>
                    <span class="per-night">/晚</span>
                    <el-button type="primary" round @click="bookRoom(room)" :disabled="!store.getters.isLoggedIn || room.stock <= 0">
                      {{ room.stock <= 0 ? '售罄' : '预订' }}
                    </el-button>
                  </div>
                </div>
              </div>
              <el-empty v-if="!rooms.length" description="暂无可选房型" :image-size="60" />
            </div>

            <div id="comments-section" class="info-card">
              <h3>💬 住客点评</h3>
              <CommentList targetType="2" :targetId="hotel.hotelId" />
            </div>
          </el-col>

          <el-col :span="8">
            <div class="sidebar-card glass">
              <h4>🏨 酒店服务</h4>
              <div class="facility-tags" v-if="hotel.facilities">
                <el-tag v-for="f in hotel.facilities.split(',')" :key="f" type="info" effect="plain" round>{{ f.trim() }}</el-tag>
              </div>
              <el-divider />
              <p><strong>电话：</strong>{{ hotel.phone || '暂无' }}</p>
              <p><strong>地址：</strong>{{ hotel.address || '暂无' }}</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- Book Dialog -->
      <el-dialog v-model="bookDialog" title="🛏️ 预订酒店" width="440px" :close-on-click-modal="false">
        <el-form :model="bookForm" label-position="top">
          <el-form-item label="房型">
            <el-input :model-value="bookRoomData?.roomType" disabled />
          </el-form-item>
          <el-form-item label="入住日期">
            <el-date-picker v-model="bookForm.checkInDate" type="date" placeholder="选择日期" style="width:100%" />
          </el-form-item>
          <el-form-item label="数量">
            <el-input-number v-model="bookForm.quantity" :min="1" :max="bookRoomData?.stock || 1" style="width:100%" />
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
            <span class="total-price">合计：<strong>¥{{ totalAmount }}</strong></span>
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
import { hotelApi, orderApi } from '@/api'
import { FALLBACK } from '@/utils/fallback'
import store from '@/store'
import CommentList from '@/components/CommentList.vue'
import ScenicMap from '@/components/ScenicMap.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const loadError = ref(false)
const hotel = ref({})
const rooms = ref([])
const bookDialog = ref(false)
const booking = ref(false)
const bookRoomData = ref(null)
const bookForm = ref({ quantity: 1, checkInDate: new Date(), contactName: '', contactPhone: '' })

const coverImg = computed(() => {
  if (!hotel.value.images) return FALLBACK.hotel
  try {
    const arr = JSON.parse(hotel.value.images)
    return arr[0] || FALLBACK.hotel
  } catch { return FALLBACK.hotel }
})

const totalAmount = computed(() => (bookRoomData.value?.price || 0) * (bookForm.value.quantity || 1))

const hotelMarkers = computed(() => {
  if (!hotel.value.longitude || !hotel.value.latitude) return []
  return [{ id: hotel.value.hotelId, name: hotel.value.hotelName, city: hotel.value.city, lat: Number(hotel.value.latitude), lng: Number(hotel.value.longitude) }]
})

const load = async () => {
  try {
    const r = await hotelApi.getDetail(route.params.id)
    hotel.value = r.data.hotel || {}
    rooms.value = r.data.rooms || []
    if (!r.data?.hotel?.hotelId) { loadError.value = true; return }
  } catch { loadError.value = true
  } finally { loading.value = false }
}

const bookRoom = (room) => {
  bookRoomData.value = room
  bookForm.value = { quantity: 1, checkInDate: new Date(), contactName: '', contactPhone: '' }
  bookDialog.value = true
}

const doBook = async () => {
  if (!bookForm.value.checkInDate) { ElMessage.warning('请选择入住日期'); return }
  if (!bookForm.value.contactName || !bookForm.value.contactPhone) {
    ElMessage.warning('请填写联系信息')
    return
  }
  booking.value = true
  try {
    await orderApi.createHotel({
      roomId: bookRoomData.value.roomId,
      quantity: bookForm.value.quantity,
      checkInDate: bookForm.value.checkInDate?.getTime(),
      contactName: bookForm.value.contactName,
      contactPhone: bookForm.value.contactPhone
    })
    bookDialog.value = false
    ElMessage.success('预订成功！请前往订单页完成支付')
    router.push('/orders')
  } catch {} finally { booking.value = false }
}

onMounted(load)
// 组件复用时重新加载数据
watch(() => route.params.id, () => {
  if (route.params.id) { loading.value = true; load() }
})
</script>

<style scoped>
.cover-section { position: relative; height: 350px; overflow: hidden; }
.cover-bg { position: absolute; inset: 0; background-size: cover; background-position: center; filter: brightness(0.55); transform: scale(1.05); }
.cover-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.15) 100%);
  display: flex; align-items: flex-end; padding-bottom: 30px;
}
.cover-content { width: 100%; }
.breadcrumb { margin-bottom: 10px; }
.breadcrumb :deep(.el-breadcrumb__inner) { color: rgba(255,255,255,0.7) !important; font-size: 0.85rem; }
.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) { color: #fff !important; }
.star-row { margin-bottom: 6px; color: var(--warning); font-size: 0.9rem; }
.cover-content h1 { color: #fff; font-size: 2rem; font-weight: 800; font-family: var(--font-display); text-shadow: 0 2px 10px rgba(0,0,0,0.3); }
.cover-content p { color: rgba(255,255,255,0.8); margin-top: 8px; }

.info-card {
  background: var(--bg-card); border-radius: var(--radius);
  padding: 24px; margin-bottom: 20px;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border-light);
}
.info-card h3 { font-size: 1.15rem; font-weight: 700; margin-bottom: 16px; }
.desc { line-height: 1.9; color: var(--text); }

.room-list { display: flex; flex-direction: column; gap: 12px; }
.room-card {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px; background: var(--bg); border-radius: var(--radius-sm);
  transition: all 0.2s; border: 1px solid var(--border-light);
}
.room-card:hover { border-color: var(--primary-light); box-shadow: var(--shadow-sm); }
.room-info h4 { font-size: 1.05rem; margin-bottom: 4px; }
.room-info p { font-size: 0.82rem; color: var(--text-secondary); margin-bottom: 6px; }
.room-price { text-align: right; }
.room-price .price { font-size: 1.5rem; font-weight: 800; color: var(--danger); display: block; }
.per-night { font-size: 0.75rem; color: var(--text-secondary); display: block; margin-bottom: 8px; }

.sidebar-card {
  background: var(--bg-card); border-radius: var(--radius);
  padding: 20px; margin-bottom: 20px;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border-light);
}
.sidebar-card h4 { font-size: 1rem; font-weight: 700; margin-bottom: 12px; }
.sidebar-card p { font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 6px; }
.facility-tags { display: flex; flex-wrap: wrap; gap: 6px; }

.dialog-footer { display: flex; justify-content: space-between; align-items: center; }
.total-price strong { color: var(--danger); font-size: 1.3rem; }

@media (max-width: 768px) {
  .cover-section { height: 280px; }
  .cover-content h1 { font-size: 1.5rem; }
}
</style>
