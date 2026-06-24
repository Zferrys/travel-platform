<template>
  <div class="page-container">
    <el-breadcrumb separator="›" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>景点探索</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="page-header">
      <h2 class="section-title" style="margin:0;border:none">🏔️ 景点探索</h2>
      <span class="result-count">共 {{ totalCount }} 个景点</span>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar glass">
      <el-input v-model="city" placeholder="输入城市" clearable style="width:140px" @change="load" :prefix-icon="LocationFilled" />
      <el-input v-model="keyword" placeholder="搜索景点..." clearable style="width:200px" @change="load" :prefix-icon="Search" />
      <el-select v-model="minLevel" placeholder="推荐等级" clearable style="width:140px" @change="load">
        <el-option label="⭐3以上" :value="3" />
        <el-option label="⭐4以上" :value="4" />
        <el-option label="⭐5" :value="5" />
      </el-select>
      <el-select v-model="sortBy" style="width:140px" @change="load">
        <el-option label="默认排序" value="" />
        <el-option label="价格从低到高" value="price_asc" />
        <el-option label="价格从高到低" value="price_desc" />
        <el-option label="推荐等级" value="level" />
        <el-option label="热度最高" value="views" />
      </el-select>
      <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'" round size="small">
        <el-icon><List /></el-icon>
      </el-button>
      <el-button :type="viewMode === 'map' ? 'primary' : 'default'" @click="viewMode = 'map'" round size="small">
        <el-icon><MapLocation /></el-icon>
      </el-button>
    </div>

    <!-- List View -->
    <div v-if="viewMode === 'list'" class="card-grid">
      <ScenicCard v-for="(s, i) in list" :key="s.scenicId" :scenic="s" :style="{ animationDelay: i * 0.06 + 's' }" class="fade-in-up" />
    </div>

    <!-- Map View -->
    <div v-else class="map-section">
      <ScenicMap :markers="mapMarkers" height="500px" />
    </div>

    <el-empty v-if="!list.length && !loading" description="暂无景点数据">
      <el-button type="primary" @click="load">重新加载</el-button>
    </el-empty>

    <!-- Pagination -->
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
import { ref, computed, onMounted } from 'vue'
import { LocationFilled, Search, List, MapLocation } from '@element-plus/icons-vue'
import { scenicApi } from '@/api'
import ScenicCard from '@/components/ScenicCard.vue'
import ScenicMap from '@/components/ScenicMap.vue'

const list = ref([])
const city = ref('')
const keyword = ref('')
const minLevel = ref(null)
const sortBy = ref('')
const viewMode = ref('list')
const page = ref(1)
const pageSize = 12
const totalCount = ref(0)
const loading = ref(false)

const mapMarkers = computed(() =>
  list.value
    .filter(s => s.longitude && s.latitude)
    .map(s => ({ id: s.scenicId, name: s.scenicName, city: s.city, lat: Number(s.latitude), lng: Number(s.longitude) }))
)

const load = async () => {
  loading.value = true
  try {
    const params = { page: page.value, pageSize, city: city.value || undefined, keyword: keyword.value || undefined, minLevel: minLevel.value || undefined, sortBy: sortBy.value || undefined }
    const r = await scenicApi.search(params)
    list.value = r.data.list || []
    totalCount.value = r.data.total || list.value.length
  } catch {} finally { loading.value = false }
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

.map-section { margin-top: 8px; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }
</style>
