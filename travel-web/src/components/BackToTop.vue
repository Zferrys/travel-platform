<template>
  <transition name="fade">
    <div v-show="visible" class="back-to-top" @click="scrollTop">
      <el-icon :size="20"><Top /></el-icon>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
const visible = ref(false)
const onScroll = () => { visible.value = window.scrollY > 400 }
const scrollTop = () => { window.scrollTo({ top: 0, behavior: 'smooth' }) }
onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.back-to-top {
  position: fixed; bottom: 40px; right: 40px; z-index: 999;
  width: 44px; height: 44px; border-radius: 50%;
  background: var(--primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; box-shadow: var(--shadow-lg); transition: transform 0.2s;
}
.back-to-top:hover { transform: scale(1.1); }
</style>
