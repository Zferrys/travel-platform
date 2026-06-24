<template>
  <div class="map-wrap">
    <div id="amap-container" style="width:100%;height:450px;border-radius:12px"></div>
    <div class="map-tips">💡 需申请<a href="https://lbs.amap.com/" target="_blank">高德地图Key</a>替换后可使用地图</div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
const props = defineProps({ center: Array, markers: Array })

const initMap = () => {
  // 高德地图占位：实际使用时替换 your_amap_key
  const script = document.createElement('script')
  script.src = `https://webapi.amap.com/maps?v=2.0&key=your_amap_key&callback=onAMapLoaded`
  window.onAMapLoaded = () => {
    const AMap = window.AMap
    const map = new AMap.Map('amap-container', {
      zoom: 12,
      center: props.center || [116.397, 39.908]
    })
    if (props.markers) {
      props.markers.forEach(m => {
        new AMap.Marker({ position: m.position, title: m.title, map })
      })
    }
  }
  document.head.appendChild(script)
}

onMounted(() => { /* 有 Key 时取消注释: initMap() */ })
</script>

<style scoped>
.map-wrap { position: relative; margin: 16px 0; }
.map-tips { text-align: center; padding: 10px; color: var(--text-secondary); font-size: 0.82rem; background:#f8fafc; border-radius:0 0 12px 12px; }
</style>
