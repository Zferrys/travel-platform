<template>
  <div class="hotel-card" @click="$router.push(`/hotel/${hotel.hotelId}`)">
    <div class="card-img">
      <img :src="firstImage" :alt="hotel.hotelName" @error="e => e.target.src = FALLBACK.hotel" />
      <div class="card-badges">
        <span class="badge stars">{{ '⭐'.repeat(hotel.starLevel || 0) }}</span>
      </div>
      <div class="card-gradient"></div>
    </div>
    <div class="card-body">
      <div class="card-location">
        <el-icon :size="14"><OfficeBuilding /></el-icon>
        <span>{{ hotel.city || '未知城市' }}</span>
      </div>
      <h3>{{ hotel.hotelName }}</h3>
      <p class="card-address">{{ hotel.address?.slice(0, 30) || '舒适住宿体验' }}</p>
      <div class="card-footer">
        <span class="facilities" v-if="hotel.facilities">{{ hotel.facilities?.split(',').slice(0, 2).join(' · ') }}</span>
        <span class="card-action">查看详情 →</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { OfficeBuilding } from '@element-plus/icons-vue'
import { FALLBACK } from '@/utils/fallback'

const props = defineProps({ hotel: Object })

const firstImage = computed(() => {
  try {
    if (props.hotel?.images) {
      const arr = JSON.parse(props.hotel.images)
      if (arr?.length) return arr[0]
    }
  } catch {}
  return FALLBACK.hotel
})
</script>

<style scoped>
.hotel-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--border-light);
}

.hotel-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
}

.card-img {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.hotel-card:hover .card-img img {
  transform: scale(1.1);
}

.card-gradient {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 50px;
  background: linear-gradient(to top, rgba(0,0,0,0.4), transparent);
}

.card-badges {
  position: absolute;
  top: 10px;
  left: 10px;
}

.badge.stars {
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  font-size: 0.65rem;
}

.card-body {
  padding: 16px;
}

.card-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.78rem;
  color: var(--accent);
  margin-bottom: 6px;
  font-weight: 500;
}

.card-body h3 {
  font-size: 1.08rem;
  font-weight: 600;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-address {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
}

.facilities {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.card-action {
  font-size: 0.82rem;
  color: var(--accent);
  font-weight: 600;
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.3s;
}

.hotel-card:hover .card-action {
  opacity: 1;
  transform: translateX(0);
}
</style>
