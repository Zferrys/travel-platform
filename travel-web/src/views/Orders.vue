<template>
  <div class="page-container">
    <el-breadcrumb separator="›" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>我的订单</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="page-header">
      <h2 class="section-title" style="margin:0;border:none">📋 我的订单</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="load" class="order-tabs">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="1" />
      <el-tab-pane label="已支付" name="2" />
      <el-tab-pane label="已完成" name="4" />
      <el-tab-pane label="已取消" name="3" />
    </el-tabs>

    <div v-if="orders.length" class="order-list">
      <div v-for="order in orders" :key="order.orderId" class="order-card fade-in-up">
        <div class="order-header">
          <div>
            <span class="order-id">订单号：{{ order.orderId }}</span>
            <el-tag :class="statusClass(order.orderStatus)" size="small" effect="plain">
              {{ statusText(order.orderStatus) }}
            </el-tag>
          </div>
          <span class="order-time">{{ order.createTime }}</span>
        </div>
        <div class="order-body">
          <div class="order-type">
            <el-icon :size="20"><component :is="order.orderType === 1 ? 'OfficeBuilding' : 'Place'" /></el-icon>
            <span>{{ order.orderType === 1 ? '酒店预订' : '门票预订' }}</span>
          </div>
          <div class="order-amount">¥{{ order.totalAmount || order.payAmount }}</div>
        </div>
        <div class="order-actions">
          <el-button size="small" @click="showDetail(order.orderId)">查看详情</el-button>
          <el-button v-if="order.orderStatus === 1" type="primary" size="small" @click="pay(order)">去支付</el-button>
          <el-button v-if="order.orderStatus === 1" size="small" @click="cancel(order)" type="danger" plain>取消</el-button>
          <el-button v-if="order.orderStatus === 2" type="success" size="small" @click="complete(order)">确认完成</el-button>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无订单">
      <el-button type="primary" @click="$router.push('/scenic')">去探索景点</el-button>
    </el-empty>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailDialog" title="订单详情" width="520px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="订单号">{{ detail.order?.orderId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :class="statusClass(detail.order?.orderStatus)" size="small">{{ statusText(detail.order?.orderStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ detail.order?.payAmount || detail.order?.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.order?.contactName }} / {{ detail.order?.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ detail.order?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <h4 style="margin-top:20px;margin-bottom:12px">📦 明细</h4>
      <div v-for="item in (detail?.items || [])" :key="item.itemId" class="item-row">
        <span>{{ item.productName }}</span>
        <span>x{{ item.quantity }} ¥{{ item.price }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api'

const activeTab = ref('')
const orders = ref([])
const detailDialog = ref(false)
const detail = ref(null)

const statusText = (s) => ({ 1: '待支付', 2: '已支付', 3: '已取消', 4: '已完成' }[s] || '-')
const statusClass = (s) => ({ 1: 'tag-pending', 2: 'tag-paid', 4: 'tag-done', 3: 'tag-cancel' }[s] || '')

const load = async () => {
  try { const r = await orderApi.getList({ status: activeTab.value || undefined }); orders.value = r.data.list || [] } catch {}
}

const pay = async (row) => {
  try {
    await orderApi.pay(row.orderId, 1)
    ElMessage.success('支付成功！')
    load()
  } catch {}
}

const cancel = async (row) => {
  try {
    await orderApi.cancel(row.orderId)
    ElMessage.success('订单已取消')
    load()
  } catch {}
}

const complete = async (row) => {
  try {
    await orderApi.complete(row.orderId)
    ElMessage.success('订单已完成！')
    load()
  } catch {}
}

const showDetail = async (id) => {
  try { const r = await orderApi.getDetail(id); detail.value = r.data; detailDialog.value = true } catch {}
}

onMounted(load)
</script>

<style scoped>
.breadcrumb { margin-bottom: 16px; }
.breadcrumb :deep(.el-breadcrumb__inner) { color: var(--text-secondary) !important; font-size: 0.85rem; }

.page-header { margin-bottom: 16px; }

.order-tabs { margin-bottom: 20px; }

.order-list { display: flex; flex-direction: column; gap: 14px; }

.order-card {
  background: var(--bg-card); border-radius: var(--radius);
  padding: 20px 24px; box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  transition: all 0.2s;
}
.order-card:hover { border-color: var(--primary-light); }

.order-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
}
.order-header > div { display: flex; gap: 10px; align-items: center; }
.order-id { font-size: 0.82rem; color: var(--text-secondary); font-family: monospace; }
.order-time { font-size: 0.8rem; color: var(--text-secondary); }

.order-body {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 0; border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 12px;
}

.order-type { display: flex; align-items: center; gap: 6px; font-weight: 500; }
.order-amount { font-size: 1.2rem; font-weight: 700; color: var(--danger); }

.order-actions { display: flex; gap: 8px; justify-content: flex-end; }

.item-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--border-light); font-size: 0.9rem; }
</style>
