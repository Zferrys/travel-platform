<template>
  <div class="page-container">
    <el-breadcrumb separator="›" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>酒店预订</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="page-header">
      <h2 class="section-title" style="margin:0;border:none">🏨 酒店预订</h2>
      <span class="result-count">共 {{ totalCount }} 家酒店</span>
    </div>

    <div class="filter-bar glass">
      <el-input v-model="city" placeholder="输入城市" clearable style="width:140px" @change="load" :prefix-icon="LocationFilled" />
      <el-input v-model="keyword" placeholder="搜索酒店..." clearable style="width:200px" @change="load" :prefix-icon="Search" />
      <el-select v-model="minStar" placeholder="最低星级" clearable style="width:130px" @change="load">
        <el-option v-for="i in 5" :key="i" :label="'⭐'.repeat(i)" :value="i" />
      </el-select>
      <el-select v-model="sortBy" style="width:150px" @change="load">
        <el-option label="默认排序" value="" />
        <el-option label="价格从低到高" value="price_asc" />
        <el-option label="价格从高到低" value="price_desc" />
        <el-option label="星级从高到低" value="star_desc" />
      </el-select>
    </div>

    <div v-if="list.length" class="card-grid">
      <HotelCard v-for="(h, i) in list" :key="h.hotelId" :hotel="h" :style="{ animationDelay: i * 0.06 + 's' }" class="fade-in-up" />
    </div>

    <el-empty v-if="!list.length" description="暂无酒店">
      <el-button type="primary" @click="load">重新加载</el-button>
    </el-empty>

    <div class="pagination-wrap" v-if="totalCount > pageSize">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="totalCount"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { LocationFilled, Search } from '@element-plus/icons-vue'
import { hotelApi } from '@/api'
import HotelCard from '@/components/HotelCard.vue'

const list = ref([])
const city = ref('')
const keyword = ref('')
const minStar = ref(null)
const sortBy = ref('')
const page = ref(1)
const pageSize = 12
const totalCount = ref(0)

const load = async () => {
  try {
    const params = { page: page.value, pageSize, city: city.value || undefined, keyword: keyword.value || undefined, minStar: minStar.value || undefined, sortBy: sortBy.value || undefined }
    const r = await hotelApi.search(params)
    list.value = r.data.list || []
    totalCount.value = r.data.total || list.value.length
  } catch {}
}

onMounted(load)
</script>

<style scoped>
.breadcrumb { margin-bottom: 16px; }
.breadcrumb :deep(.el-breadcrumb__inner) { color: var(--text-secondary) !important; font-size: 0.85rem; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.result-count { font-size: 0.85rem; color: var(--text-secondary); }

.filter-bar {
  display: flex; gap: 10px; align-items: center; flex-wrap: wrap;
  padding: 16px 20px; border-radius: var(--radius); margin-bottom: 24px;
  background: var(--bg-card); border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }
</style>
