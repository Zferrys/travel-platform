<template>
  <div class="page-container">
    <div class="search-hero">
      <h2>🔍 搜索目的地</h2>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          size="large"
          placeholder="搜索景点、酒店、游记..."
          :prefix-icon="Search"
          @keyup.enter="doSearch"
          clearable
          class="search-main-input"
        />
        <el-button type="primary" size="large" @click="doSearch" :loading="searching" round>
          <el-icon><Search /></el-icon> 搜索
        </el-button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="searching" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在搜索 "{{ currentQuery }}" ...</p>
    </div>

    <!-- Results -->
    <template v-else-if="searched">
      <div class="search-stats" v-if="totalResults > 0">
        找到 <strong>{{ totalResults }}</strong> 个结果
      </div>

      <el-tabs v-model="tab" class="search-tabs">
        <el-tab-pane :label="`景点 (${scenicResults.length})`" name="scenic">
          <div class="card-grid">
            <ScenicCard v-for="s in scenicResults" :key="s.scenicId" :scenic="s" />
          </div>
          <el-empty v-if="!scenicResults.length" description="暂无景点结果" />
        </el-tab-pane>
        <el-tab-pane :label="`酒店 (${hotelResults.length})`" name="hotel">
          <div class="card-grid">
            <HotelCard v-for="h in hotelResults" :key="h.hotelId" :hotel="h" />
          </div>
          <el-empty v-if="!hotelResults.length" description="暂无酒店结果" />
        </el-tab-pane>
        <el-tab-pane :label="`游记 (${noteResults.length})`" name="note">
          <div class="card-grid">
            <NoteCard v-for="n in noteResults" :key="n.noteId" :note="n" />
          </div>
          <el-empty v-if="!noteResults.length" description="暂无游记结果" />
        </el-tab-pane>
      </el-tabs>

      <el-empty v-if="totalResults === 0" description="没有找到相关内容，试试其他关键词">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </el-empty>
    </template>

    <!-- Initial state -->
    <div v-else class="search-placeholder">
      <el-icon :size="64" color="#dfe6e9"><Search /></el-icon>
      <p>输入关键词，探索精彩目的地</p>
      <div class="hot-searches">
        <span>热门搜索：</span>
        <el-tag v-for="t in ['北京', '西湖', '成都', '三亚', '古城', '雪山']" :key="t" @click="keyword = t; doSearch()" effect="plain" round class="hot-tag">{{ t }}</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { scenicApi, hotelApi, noteApi } from '@/api'
import ScenicCard from '@/components/ScenicCard.vue'
import HotelCard from '@/components/HotelCard.vue'
import NoteCard from '@/components/NoteCard.vue'

const route = useRoute()
const keyword = ref(route.query.q || '')
const currentQuery = ref('')
const tab = ref('scenic')
const searched = ref(false)
const searching = ref(false)
const scenicResults = ref([])
const hotelResults = ref([])
const noteResults = ref([])

const totalResults = computed(() => scenicResults.value.length + hotelResults.value.length + noteResults.value.length)

const doSearch = async () => {
  if (!keyword.value.trim()) return
  currentQuery.value = keyword.value.trim()
  searching.value = true
  searched.value = true
  try { const r = await scenicApi.search({ keyword: keyword.value }); scenicResults.value = r.data.list || [] } catch { scenicResults.value = [] }
  try { const r = await hotelApi.search({ keyword: keyword.value }); hotelResults.value = r.data.list || [] } catch { hotelResults.value = [] }
  try { const r = await noteApi.getList({ keyword: keyword.value }); noteResults.value = r.data.list || [] } catch { noteResults.value = [] }
  searching.value = false

  // Switch to tab with most results
  if (scenicResults.value.length >= hotelResults.value.length && scenicResults.value.length >= noteResults.value.length) tab.value = 'scenic'
  else if (hotelResults.value.length >= noteResults.value.length) tab.value = 'hotel'
  else tab.value = 'note'
}

onMounted(() => { if (keyword.value) doSearch() })
</script>

<style scoped>
.search-hero {
  text-align: center; padding: 32px 0;
}

.search-hero h2 {
  font-size: 1.8rem; font-weight: 700;
  font-family: var(--font-display); margin-bottom: 20px;
}

.search-bar {
  display: flex; gap: 12px;
  max-width: 600px; margin: 0 auto;
}

.search-main-input :deep(.el-input__wrapper) {
  border-radius: 30px !important;
  height: 48px;
}

.search-stats {
  text-align: center; font-size: 0.9rem; color: var(--text-secondary);
  margin-bottom: 16px;
}

.search-stats strong { color: var(--primary); }

.search-tabs { margin-top: 8px; }

.loading-state {
  text-align: center; padding: 80px 0; color: var(--text-secondary);
}

.loading-spinner {
  width: 40px; height: 40px;
  border: 3px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin { to { transform: rotate(360deg); } }

.search-placeholder {
  text-align: center; padding: 80px 0; color: var(--text-secondary);
}

.search-placeholder p {
  margin: 16px 0 24px; font-size: 1rem;
}

.hot-searches {
  display: flex; align-items: center; gap: 8px;
  justify-content: center; flex-wrap: wrap;
}

.hot-tag { cursor: pointer; }
.hot-tag:hover { color: var(--primary); border-color: var(--primary); }
</style>
