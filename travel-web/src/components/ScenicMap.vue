<template>
  <div class="map-container">
    <div class="map-header">
      <div class="map-title">
        <el-icon><MapLocation /></el-icon>
        <span>📍 景点分布图</span>
      </div>
      <div class="map-stats">
        <el-tag size="small" effect="plain" round>{{ validMarkers.length }} 个地点</el-tag>
        <!-- Layer Toggle -->
        <el-button-group size="small">
          <el-button :type="layerMode === 'normal' ? 'primary' : 'default'" @click="switchLayer('normal')">地图</el-button>
          <el-button :type="layerMode === 'satellite' ? 'primary' : 'default'" @click="switchLayer('satellite')">卫星</el-button>
        </el-button-group>
        <el-button size="small" text @click="resetView" v-if="mapReady">
          <el-icon><Aim /></el-icon> 重置
        </el-button>
      </div>
    </div>

    <div class="map-wrapper" :style="{ height: mapHeight }">
      <div ref="mapContainer" class="leaflet-map"></div>
      <!-- Loading -->
      <div class="map-loading" v-if="!mapReady">
        <div class="loading-spinner"></div>
        <span>加载地图中...</span>
      </div>
      <!-- Zoom controls hint -->
      <div class="map-zoom-hint" v-if="mapReady">🖱 滚轮缩放 | 拖拽平移</div>
    </div>

    <!-- Marker List -->
    <div class="map-list" v-if="validMarkers.length">
      <div class="map-list-header">
        <span>📍 地点列表</span>
        <el-input v-model="markerFilter" placeholder="筛选地名..." size="small" clearable style="width:180px" :prefix-icon="Search" />
      </div>
      <div class="map-list-items">
        <div
          v-for="m in filteredMarkers"
          :key="m.id"
          class="map-city-item"
          @click="focusMarker(m)"
          :class="{ active: activeMarkerId === m.id }"
        >
          <span class="marker-dot" :style="{ background: markerColor(m) }"></span>
          <span class="city-name">{{ m.name }}</span>
          <el-tag size="small" effect="plain" type="info">{{ m.city || '未知' }}</el-tag>
          <el-icon :size="14" class="goto-icon"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="map-empty">
      <el-icon :size="40" color="#dfe6e9"><MapLocation /></el-icon>
      <p>暂无地点坐标数据</p>
      <span class="empty-hint">添加经纬度信息后即可在地图上展示</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { MapLocation, Aim, ArrowRight, Search } from '@element-plus/icons-vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const props = defineProps({
  markers: { type: Array, default: () => [] },
  city: String,
  height: { type: String, default: '400px' }
})

const mapContainer = ref(null)
const mapHeight = computed(() => props.height || '400px')
const mapReady = ref(false)
const activeMarkerId = ref(null)
const layerMode = ref('normal')
const markerFilter = ref('')

let map = null
let markerLayer = null
let tileLayer = null
let allMarkers = []

const MARKER_COLORS = ['#ff6b6b', '#0abde3', '#f368e0', '#10ac84', '#feca57', '#0984e3', '#a55eea', '#26de81']

const validMarkers = computed(() =>
  props.markers
    .filter(m => m.lat && m.lng)
    .map(m => ({ ...m, lat: Number(m.lat), lng: Number(m.lng) }))
)

const filteredMarkers = computed(() => {
  const q = markerFilter.value.trim().toLowerCase()
  if (!q) return validMarkers.value
  return validMarkers.value.filter(m =>
    (m.name || '').toLowerCase().includes(q) ||
    (m.city || '').toLowerCase().includes(q)
  )
})

const markerColor = (m) => {
  const idx = validMarkers.value.indexOf(m)
  return MARKER_COLORS[idx % MARKER_COLORS.length]
}

// ---- Tile URLs (高德地图，国内秒加载) ----
const TILE_URLS = {
  normal: 'https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
  satellite: 'https://webst0{s}.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}'
}

// Custom icon factory
const createIcon = (color, number) => {
  return L.divIcon({
    className: 'custom-div-icon',
    html: `
      <div class="marker-pin">
        <div class="marker-pin-body" style="background:${color};border-color:#fff">
          <span class="marker-pin-number">${number}</span>
        </div>
        <div class="marker-pin-arrow" style="border-top-color:${color}"></div>
      </div>
    `,
    iconSize: [36, 48],
    iconAnchor: [18, 48],
    popupAnchor: [0, -50]
  })
}

const initMap = () => {
  if (!mapContainer.value) return

  map = L.map(mapContainer.value, {
    attributionControl: false,
    zoomControl: true
  }).setView([35, 105], 5)

  // Use 高德 tiles — fast in China
  tileLayer = L.tileLayer(TILE_URLS.normal, {
    maxZoom: 18,
    subdomains: ['1', '2', '3', '4'],
    attribution: '&copy; 高德地图'
  }).addTo(map)

  markerLayer = L.layerGroup().addTo(map)

  map.on('tileload', () => { mapReady.value = true })
  setTimeout(() => { mapReady.value = true }, 2000)

  updateMarkers()
}

const switchLayer = (mode) => {
  layerMode.value = mode
  if (map && tileLayer) {
    map.removeLayer(tileLayer)
    tileLayer = L.tileLayer(TILE_URLS[mode], {
      maxZoom: 18,
      subdomains: ['1', '2', '3', '4']
    }).addTo(map)
  }
}

const updateMarkers = () => {
  if (!markerLayer) return
  markerLayer.clearLayers()
  allMarkers = []

  if (validMarkers.value.length === 0) return

  const bounds = []

  validMarkers.value.forEach((m, idx) => {
    const color = MARKER_COLORS[idx % MARKER_COLORS.length]
    const icon = createIcon(color, idx + 1)

    const popupContent = `
      <div class="map-popup">
        <div class="map-popup-title">${m.name}</div>
        <div class="map-popup-city">📍 ${m.city || '未知城市'}</div>
        <div class="map-popup-coord">${m.lat.toFixed(4)}, ${m.lng.toFixed(4)}</div>
        <a href="/scenic/${m.id}" class="map-popup-link">查看详情 →</a>
      </div>
    `

    const marker = L.marker([m.lat, m.lng], { icon })
      .bindPopup(popupContent, {
        maxWidth: 260,
        className: 'custom-popup'
      })
      .on('click', () => {
        activeMarkerId.value = m.id
        marker.openPopup()
      })

    markerLayer.addLayer(marker)
    allMarkers.push({ marker, data: m })
    bounds.push([m.lat, m.lng])
  })

  if (bounds.length > 1) {
    map.fitBounds(bounds, { padding: [50, 50], maxZoom: 12 })
  } else if (bounds.length === 1) {
    map.setView(bounds[0], 14)
  }
  map.invalidateSize()
}

const focusMarker = (m) => {
  activeMarkerId.value = m.id
  if (map) {
    map.setView([m.lat, m.lng], 15, { animate: true })
    const found = allMarkers.find(am => am.data.id === m.id)
    if (found) {
      found.marker.openPopup()
    }
  }
}

const resetView = () => {
  activeMarkerId.value = null
  updateMarkers()
}

watch(() => props.markers, () => nextTick(() => updateMarkers()), { deep: true })

onMounted(() => nextTick(() => initMap()))

onBeforeUnmount(() => {
  if (map) { map.remove(); map = null }
})
</script>

<style scoped>
.map-container {
  background: var(--bg-card);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  border: 1px solid var(--border-light);
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-light);
  flex-wrap: wrap;
  gap: 8px;
}

.map-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 0.95rem;
}

.map-stats {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.map-wrapper {
  position: relative;
  overflow: hidden;
  background: #e8e4df;
}

.leaflet-map {
  width: 100%;
  height: 100%;
  z-index: 1;
}

.map-zoom-hint {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 0.72rem;
  pointer-events: none;
}

.map-loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #e8e4df;
  z-index: 2;
  gap: 12px;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #d0d0d0;
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* Marker List */
.map-list {
  border-top: 1px solid var(--border-light);
}

.map-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.map-list-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 6px 20px 16px;
  max-height: 160px;
  overflow-y: auto;
}

.map-city-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 25px;
  font-size: 0.82rem;
  background: var(--bg);
  border: 1px solid var(--border-light);
  transition: all 0.2s;
}

.map-city-item:hover,
.map-city-item.active {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.map-city-item:hover .goto-icon,
.map-city-item.active .goto-icon { color: rgba(255,255,255,0.8); }

.map-city-item:hover :deep(.el-tag),
.map-city-item.active :deep(.el-tag) {
  background: rgba(255,255,255,0.25) !important;
  border-color: rgba(255,255,255,0.3) !important;
  color: #fff !important;
}

.marker-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.city-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.goto-icon { color: var(--text-secondary); flex-shrink: 0; }

.map-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.empty-hint { font-size: 0.78rem; color: #b2bec3; }
</style>

<!-- Global styles for markers/popups (not scoped so they apply to Leaflet DOM) -->
<style>
/* Custom Marker Pin */
.custom-div-icon { background: transparent !important; border: none !important; }

.marker-pin {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.marker-pin-body {
  width: 30px;
  height: 30px;
  border-radius: 50% 50% 50% 0;
  transform: rotate(-45deg);
  border: 3px solid #fff;
  box-shadow: 0 3px 10px rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.marker-pin-number {
  transform: rotate(45deg);
  color: #fff;
  font-size: 0.7rem;
  font-weight: 700;
  font-family: sans-serif;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

.marker-pin-arrow {
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 8px solid;
  margin-top: -2px;
  filter: drop-shadow(0 1px 2px rgba(0,0,0,0.2));
}

/* Custom Popup */
.custom-popup .leaflet-popup-content-wrapper {
  border-radius: 14px !important;
  box-shadow: 0 8px 30px rgba(0,0,0,0.2) !important;
  padding: 0 !important;
}

.custom-popup .leaflet-popup-content {
  margin: 0 !important;
  padding: 14px 18px !important;
  font-family: var(--font-body), sans-serif;
}

.map-popup-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 4px;
}

.map-popup-city {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.map-popup-coord {
  font-size: 0.7rem;
  color: #b2bec3;
  font-family: monospace;
  margin-bottom: 10px;
}

.map-popup-link {
  display: inline-block;
  padding: 4px 16px;
  background: var(--primary);
  color: #fff;
  border-radius: 20px;
  font-size: 0.78rem;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.2s;
}

.map-popup-link:hover {
  background: var(--primary-dark);
  color: #fff;
}
</style>
