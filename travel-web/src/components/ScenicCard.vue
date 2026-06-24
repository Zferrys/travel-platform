<template>
  <div class="scenic-card" @click="$router.push(`/scenic/${scenic.scenicId}`)">
    <div class="card-img">
      <img :src="scenic.coverImage || FALLBACK.scenic" :alt="scenic.scenicName" @error="e => e.target.src = FALLBACK.scenic" />
      <div class="card-badges">
        <span class="badge level">⭐{{ scenic.recommendLevel || 3 }}</span>
        <span class="badge price" v-if="scenic.ticketPrice">¥{{ scenic.ticketPrice }}</span>
      </div>
      <div class="card-gradient"></div>
    </div>
    <div class="card-body">
      <div class="card-location">
        <el-icon :size="14"><LocationFilled /></el-icon>
        <span>{{ scenic.city || '未知城市' }}</span>
      </div>
      <h3>{{ scenic.scenicName }}</h3>
      <p class="card-address">{{ scenic.address?.slice(0, 30) || '探索这片美丽的风景' }}</p>
      <div class="card-tags" v-if="scenic.tags">
        <span v-for="t in scenic.tags.split(',').slice(0, 3)" :key="t" class="tag">{{ t.trim() }}</span>
      </div>
      <div class="card-footer">
        <span class="view-count">
          <el-icon :size="14"><View /></el-icon> {{ scenic.viewCount || 0 }}
        </span>
        <span class="card-action">查看详情 →</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { LocationFilled, View } from '@element-plus/icons-vue'
import { FALLBACK } from '@/utils/fallback'
defineProps({ scenic: Object })
</script>

<style scoped>
.scenic-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--border-light);
}

.scenic-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
}

.card-img {
  position: relative;
  height: 190px;
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.scenic-card:hover .card-img img {
  transform: scale(1.1);
}

.card-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(to top, rgba(0,0,0,0.4), transparent);
}

.card-badges {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  display: flex;
  justify-content: space-between;
}

.badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.72rem;
  font-weight: 600;
  backdrop-filter: blur(8px);
}

.badge.level {
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
}

.badge.price {
  background: var(--primary);
  color: #fff;
}

.card-body {
  padding: 16px;
}

.card-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.78rem;
  color: var(--primary);
  margin-bottom: 6px;
  font-weight: 500;
}

.card-body h3 {
  font-size: 1.08rem;
  font-weight: 600;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-address {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.tag {
  padding: 2px 10px;
  background: var(--bg);
  border-radius: 12px;
  font-size: 0.72rem;
  color: var(--text-secondary);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.78rem;
  color: var(--text-secondary);
}

.card-action {
  font-size: 0.82rem;
  color: var(--primary);
  font-weight: 600;
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.3s;
}

.scenic-card:hover .card-action {
  opacity: 1;
  transform: translateX(0);
}
</style>
