<template>
  <div class="page-container">
    <el-breadcrumb separator="›" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>后台管理</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="admin-header">
      <div>
        <h2>⚙️ 后台管理</h2>
        <p class="admin-subtitle">管理景点、酒店、用户、游记和评论</p>
      </div>
      <el-tag type="danger" size="small" effect="dark">管理员</el-tag>
    </div>

    <!-- Dashboard Stats -->
    <div class="stats-row">
      <div class="stat-card" v-for="stat in dashboardStats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bg }">
          <el-icon :size="22" color="#fff"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="admin-card">
      <el-tabs v-model="tab" @tab-change="onTabChange" lazy>
        <!-- ========== 景点管理 ========== -->
        <el-tab-pane label="景点管理" name="scenic">
          <div class="section-toolbar">
            <div class="toolbar-left">
              <el-input v-model="scenic.search" placeholder="搜索景点名称..." :prefix-icon="Search"
                size="default" clearable style="width: 240px" @keyup.enter="loadScenic(1)" @clear="scenic.search='';loadScenic(1)" />
              <el-button @click="loadScenic(1)">搜索</el-button>
            </div>
            <el-button type="primary" @click="showScenicDialog()" round>
              <el-icon><Plus /></el-icon> 添加景点
            </el-button>
          </div>

          <el-table :data="scenic.data" stripe v-loading="scenic.loading" class="data-table">
            <el-table-column prop="scenicId" label="ID" width="70" />
            <el-table-column label="景点名称" min-width="160">
              <template #default="{row}">
                <div class="table-name-cell">
                  <img v-if="row.coverImage" :src="row.coverImage" class="table-thumb" @error="e => e.target.style.display='none'" />
                  <span>{{ row.scenicName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="city" label="城市" width="90" />
            <el-table-column label="等级" width="100" align="center">
              <template #default="{row}">{{ '⭐'.repeat(row.recommendLevel || 0) }}</template>
            </el-table-column>
            <el-table-column label="门票" width="90" align="right">
              <template #default="{row}">¥{{ row.ticketPrice || 0 }}</template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览" width="80" align="right" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{row}">
                <el-button size="small" @click="showScenicDialog(row)"><el-icon><Edit /></el-icon>编辑</el-button>
                <el-button size="small" type="danger" @click="delScenic(row.scenicId)" plain><el-icon><Delete /></el-icon></el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="scenic.total > 0">
            <el-pagination background layout="total, prev, pager, next, sizes" :current-page="scenic.page"
              :page-size="scenic.pageSize" :total="scenic.total" :page-sizes="[10,20,50]"
              @current-change="p => loadScenic(p)" @size-change="s => { scenic.pageSize = s; loadScenic(1) }" />
          </div>
          <el-empty v-if="!scenic.data.length && !scenic.loading" description="暂无匹配的景点" />
        </el-tab-pane>

        <!-- ========== 酒店管理 ========== -->
        <el-tab-pane label="酒店管理" name="hotel">
          <div class="section-toolbar">
            <div class="toolbar-left">
              <el-input v-model="hotel.search" placeholder="搜索酒店名称..." :prefix-icon="Search"
                size="default" clearable style="width: 240px" @keyup.enter="loadHotel(1)" @clear="hotel.search='';loadHotel(1)" />
              <el-button @click="loadHotel(1)">搜索</el-button>
            </div>
            <el-button type="primary" @click="showHotelDialog()" round>
              <el-icon><Plus /></el-icon> 添加酒店
            </el-button>
          </div>

          <el-table :data="hotel.data" stripe v-loading="hotel.loading" class="data-table">
            <el-table-column prop="hotelId" label="ID" width="70" />
            <el-table-column prop="hotelName" label="酒店名称" min-width="160" />
            <el-table-column prop="city" label="城市" width="90" />
            <el-table-column label="星级" width="120" align="center">
              <template #default="{row}">{{ '⭐'.repeat(row.starLevel || 0) }}</template>
            </el-table-column>
            <el-table-column prop="phone" label="电话" width="130" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{row}">
                <el-button size="small" @click="showHotelDialog(row)"><el-icon><Edit /></el-icon>编辑</el-button>
                <el-button size="small" type="danger" @click="delHotel(row.hotelId)" plain><el-icon><Delete /></el-icon></el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="hotel.total > 0">
            <el-pagination background layout="total, prev, pager, next, sizes" :current-page="hotel.page"
              :page-size="hotel.pageSize" :total="hotel.total" :page-sizes="[10,20,50]"
              @current-change="p => loadHotel(p)" @size-change="s => { hotel.pageSize = s; loadHotel(1) }" />
          </div>
          <el-empty v-if="!hotel.data.length && !hotel.loading" description="暂无匹配的酒店" />
        </el-tab-pane>

        <!-- ========== 用户管理 ========== -->
        <el-tab-pane label="用户管理" name="user">
          <div class="section-toolbar">
            <div class="toolbar-left">
              <el-input v-model="user.search" placeholder="搜索用户名/昵称/手机..." :prefix-icon="Search"
                size="default" clearable style="width: 240px" @keyup.enter="loadUser(1)" @clear="user.search='';loadUser(1)" />
              <el-button @click="loadUser(1)">搜索</el-button>
            </div>
            <el-button type="primary" @click="showCreateUserDialog" round>
              <el-icon><Plus /></el-icon> 创建用户
            </el-button>
          </div>

          <el-table :data="user.data" stripe v-loading="user.loading" class="data-table">
            <el-table-column prop="userId" label="ID" width="70" />
            <el-table-column prop="username" label="用户名" width="140" />
            <el-table-column prop="nickname" label="昵称" min-width="140" />
            <el-table-column label="角色" width="100" align="center">
              <template #default="{row}">
                <el-tag :type="row.userType === 3 ? 'danger' : 'info'" size="small">
                  {{ row.userType === 3 ? '管理员' : '用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{row}">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{row}">
                <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'"
                  @click="toggleUser(row)" plain>{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
                <el-button v-if="row.userType !== 3" size="small" type="danger"
                  @click="deleteUser(row)" plain><el-icon><Delete /></el-icon></el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="user.total > 0">
            <el-pagination background layout="total, prev, pager, next, sizes" :current-page="user.page"
              :page-size="user.pageSize" :total="user.total" :page-sizes="[10,20,50]"
              @current-change="p => loadUser(p)" @size-change="s => { user.pageSize = s; loadUser(1) }" />
          </div>
        </el-tab-pane>

        <!-- ========== 游记管理 ========== -->
        <el-tab-pane label="游记管理" name="note">
          <div class="section-toolbar">
            <div class="toolbar-left">
              <el-input v-model="note.search" placeholder="搜索游记标题..." :prefix-icon="Search"
                size="default" clearable style="width: 240px" @keyup.enter="loadNote(1)" @clear="note.search='';loadNote(1)" />
              <el-button @click="loadNote(1)">搜索</el-button>
            </div>
          </div>

          <el-table :data="note.data" stripe v-loading="note.loading" class="data-table">
            <el-table-column prop="noteId" label="ID" width="70" />
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{row}">
                <div class="table-name-cell">
                  <img v-if="row.coverImage" :src="row.coverImage" class="table-thumb" @error="e => e.target.style.display='none'" />
                  <span>{{ row.title }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="nickname" label="作者" width="110" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{row}">
                <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'" size="small" effect="dark">
                  {{ row.status === 1 ? '正常' : row.status === 2 ? '已删除' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="互动" width="160" align="center">
              <template #default="{row}">
                <span class="interact-stat">👁 {{ row.viewCount || 0 }}</span>
                <span class="interact-stat">⭐ {{ row.likeCount || 0 }}</span>
                <span class="interact-stat">💬 {{ row.commentCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="110" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{row}">
                <el-button size="small" type="danger" @click="delNote(row.noteId)" plain><el-icon><Delete /></el-icon> 删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="note.total > 0">
            <el-pagination background layout="total, prev, pager, next, sizes" :current-page="note.page"
              :page-size="note.pageSize" :total="note.total" :page-sizes="[10,20,50]"
              @current-change="p => loadNote(p)" @size-change="s => { note.pageSize = s; loadNote(1) }" />
          </div>
          <el-empty v-if="!note.data.length && !note.loading" description="暂无游记" />
        </el-tab-pane>

        <!-- ========== 评论管理 ========== -->
        <el-tab-pane label="评论管理" name="comment">
          <div class="section-toolbar">
            <div class="toolbar-left">
              <el-input v-model="comment.search" placeholder="搜索评论内容..." :prefix-icon="Search"
                size="default" clearable style="width: 240px" @keyup.enter="loadComment(1)" @clear="comment.search='';loadComment(1)" />
              <el-button @click="loadComment(1)">搜索</el-button>
            </div>
          </div>

          <el-table :data="comment.data" stripe v-loading="comment.loading" class="data-table">
            <el-table-column prop="commentId" label="ID" width="70" />
            <el-table-column prop="nickname" label="用户" width="120" />
            <el-table-column label="类型" width="90" align="center">
              <template #default="{row}">
                <el-tag :type="row.targetType == 1 ? 'primary' : row.targetType == 2 ? 'success' : 'warning'" size="small">
                  {{ row.targetType == 1 ? '景点' : row.targetType == 2 ? '酒店' : '游记' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
            <el-table-column label="状态" width="80" align="center">
              <template #default="{row}">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
                  {{ row.status === 1 ? '正常' : '已删除' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="110" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{row}">
                <el-button size="small" type="danger" @click="delComment(row.commentId)" plain><el-icon><Delete /></el-icon> 删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="comment.total > 0">
            <el-pagination background layout="total, prev, pager, next, sizes" :current-page="comment.page"
              :page-size="comment.pageSize" :total="comment.total" :page-sizes="[10,20,50]"
              @current-change="p => loadComment(p)" @size-change="s => { comment.pageSize = s; loadComment(1) }" />
          </div>
          <el-empty v-if="!comment.data.length && !comment.loading" description="暂无评论" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- ===== Scenic Dialog ===== -->
    <el-dialog v-model="scenicDialog" :title="editingScenic.scenicId ? '编辑景点' : '添加景点'" width="600px" :close-on-click-modal="false">
      <el-form :model="editingScenic" label-position="top">
        <el-form-item label="封面图片">
          <div class="cover-upload-row">
            <div class="cover-preview-box" v-if="scenicCoverPreview || editingScenic.coverImage">
              <img :src="scenicCoverPreview || editingScenic.coverImage" class="cover-preview" />
              <span class="cover-delete" @click="scenicCoverPreview = ''; editingScenic.coverImage = ''">✕</span>
            </div>
            <el-upload :auto-upload="false" :show-file-list="false" :on-change="onScenicCoverChange" accept="image/*" class="cover-upload-btn">
              <div class="upload-placeholder-small"><el-icon :size="28"><Plus /></el-icon><span>{{ editingScenic.coverImage || scenicCoverPreview ? '更换' : '上传' }}</span></div>
            </el-upload>
          </div>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="16"><el-form-item label="景点名称"><el-input v-model="editingScenic.scenicName" placeholder="输入景点名称" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="推荐等级"><el-rate v-model="editingScenic.recommendLevel" :max="5" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="城市"><el-input v-model="editingScenic.city" placeholder="所在城市" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="门票"><el-input-number v-model="editingScenic.ticketPrice" :precision="2" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="地址"><el-input v-model="editingScenic.address" placeholder="详细地址" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="editingScenic.tags" placeholder="逗号分隔，如：自然,登山,摄影" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editingScenic.description" type="textarea" :rows="3" placeholder="景点详细介绍" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scenicDialog = false">取消</el-button>
        <el-button type="primary" @click="saveScenic" :loading="savingScenic">保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== Hotel Dialog ===== -->
    <el-dialog v-model="hotelDialog" :title="editingHotel.hotelId ? '编辑酒店' : '添加酒店'" width="600px" :close-on-click-modal="false">
      <el-form :model="editingHotel" label-position="top">
        <el-form-item label="封面图片">
          <div class="cover-upload-row">
            <div class="cover-preview-box" v-if="hotelCoverPreview || hotelFirstImage">
              <img :src="hotelCoverPreview || hotelFirstImage" class="cover-preview" />
              <span class="cover-delete" @click="hotelCoverPreview = ''; editingHotel.images = ''">✕</span>
            </div>
            <el-upload :auto-upload="false" :show-file-list="false" :on-change="onHotelCoverChange" accept="image/*" class="cover-upload-btn">
              <div class="upload-placeholder-small"><el-icon :size="28"><Plus /></el-icon><span>{{ hotelFirstImage || hotelCoverPreview ? '更换' : '上传' }}</span></div>
            </el-upload>
          </div>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="16"><el-form-item label="酒店名称"><el-input v-model="editingHotel.hotelName" placeholder="输入酒店名称" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="星级"><el-rate v-model="editingHotel.starLevel" :max="5" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="城市"><el-input v-model="editingHotel.city" placeholder="所在城市" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="editingHotel.phone" placeholder="联系电话" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="地址"><el-input v-model="editingHotel.address" placeholder="详细地址" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="hotelDialog = false">取消</el-button>
        <el-button type="primary" @click="saveHotel" :loading="savingHotel">保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== Create User Dialog ===== -->
    <el-dialog v-model="createUserDialog" title="创建用户" width="420px" :close-on-click-modal="false">
      <el-form :model="newUser" label-position="top">
        <el-form-item label="用户名"><el-input v-model="newUser.username" placeholder="至少3个字符" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="newUser.password" type="password" placeholder="至少8位，含字母和数字" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="newUser.nickname" placeholder="可选" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="newUser.phone" placeholder="可选" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createUserDialog = false">取消</el-button>
        <el-button type="primary" @click="doCreateUser" :loading="creatingUser">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Place, OfficeBuilding, User, Tickets } from '@element-plus/icons-vue'
import http from '@/api'
import store from '@/store'

const router = useRouter()

// Admin gate
if (store.getters.user?.userType !== 3) {
  ElMessage.error('无权限访问管理后台')
  router.push('/')
}

const tab = ref('scenic')

// ===== Per-tab pagination state =====
const mkPage = (search = '') => reactive({ data: [], page: 1, pageSize: 10, total: 0, search, loading: false })
const scenic = mkPage()
const hotel = mkPage()
const user = mkPage()
const note = mkPage()
const comment = mkPage()

// Dashboard counts (loaded once with first page)
const dashCounts = reactive({ scenic: 0, hotel: 0, user: 0, userActive: 0, note: 0, comment: 0 })
const dashLoading = ref(true)

// ===== Dashboard =====
const dashboardStats = computed(() => [
  { label: '景点总数', value: dashCounts.scenic, loading: dashLoading.value, icon: Place, bg: 'linear-gradient(135deg, #ff6b6b, #ee5a24)' },
  { label: '酒店总数', value: dashCounts.hotel, loading: dashLoading.value, icon: OfficeBuilding, bg: 'linear-gradient(135deg, #0abde3, #0984e3)' },
  { label: '用户总数', value: dashCounts.user, loading: dashLoading.value, icon: User, bg: 'linear-gradient(135deg, #10ac84, #1dd1a1)' },
  { label: '游记/评论', value: `${dashCounts.note} / ${dashCounts.comment}`, loading: dashLoading.value, icon: Tickets, bg: 'linear-gradient(135deg, #f368e0, #be2edd)' },
])

// ===== Load functions =====
const loadScenic = async (p = scenic.page) => {
  scenic.loading = true
  scenic.page = p
  try {
    const r = await http.get('/admin/scenics', { params: { keyword: scenic.search, page: p, pageSize: scenic.pageSize } })
    scenic.data = r.data?.list || []
    scenic.total = r.data?.total || 0
    if (dashLoading.value) dashCounts.scenic = r.data?.total || 0
  } catch {} finally { scenic.loading = false }
}

const loadHotel = async (p = hotel.page) => {
  hotel.loading = true
  hotel.page = p
  try {
    const r = await http.get('/admin/hotels', { params: { keyword: hotel.search, page: p, pageSize: hotel.pageSize } })
    hotel.data = r.data?.list || []
    hotel.total = r.data?.total || 0
    if (dashLoading.value) dashCounts.hotel = r.data?.total || 0
  } catch {} finally { hotel.loading = false }
}

const loadUser = async (p = user.page) => {
  user.loading = true
  user.page = p
  try {
    const r = await http.get('/admin/users', { params: { keyword: user.search, page: p, pageSize: user.pageSize } })
    user.data = r.data?.list || []
    user.total = r.data?.total || 0
    if (dashLoading.value) {
      dashCounts.user = r.data?.total || 0
      dashCounts.userActive = (r.data?.list || []).filter(u => u.status === 1).length
    }
  } catch {} finally { user.loading = false }
}

const loadNote = async (p = note.page) => {
  note.loading = true
  note.page = p
  try {
    const r = await http.get('/admin/notes', { params: { keyword: note.search, page: p, pageSize: note.pageSize } })
    note.data = r.data?.list || []
    note.total = r.data?.total || 0
    if (dashLoading.value) dashCounts.note = r.data?.total || 0
  } catch {} finally { note.loading = false }
}

const loadComment = async (p = comment.page) => {
  comment.loading = true
  comment.page = p
  try {
    const r = await http.get('/admin/comments', { params: { keyword: comment.search, page: p, pageSize: comment.pageSize } })
    comment.data = r.data?.list || []
    comment.total = r.data?.total || 0
    if (dashLoading.value) dashCounts.comment = r.data?.total || 0
  } catch {} finally { comment.loading = false }
}

// Load all dashboard counts via parallel small queries
const loadDashboardCounts = async () => {
  dashLoading.value = true
  try {
    const res = await Promise.all([
      http.get('/admin/scenics', { params: { page: 1, pageSize: 1 } }),
      http.get('/admin/hotels', { params: { page: 1, pageSize: 1 } }),
      http.get('/admin/users', { params: { page: 1, pageSize: 1 } }),
      http.get('/admin/notes', { params: { page: 1, pageSize: 1 } }),
      http.get('/admin/comments', { params: { page: 1, pageSize: 1 } }),
    ])
    dashCounts.scenic = res[0].data?.total || 0
    dashCounts.hotel = res[1].data?.total || 0
    dashCounts.user = res[2].data?.total || 0
    dashCounts.note = res[3].data?.total || 0
    dashCounts.comment = res[4].data?.total || 0
  } catch {} finally { dashLoading.value = false }
}

// Load current tab data
const loadCurrentTab = () => {
  const loaders = { scenic: loadScenic, hotel: loadHotel, user: loadUser, note: loadNote, comment: loadComment }
  const fn = loaders[tab.value]
  if (fn) fn()
}

const onTabChange = () => loadCurrentTab()

// ===== Scenic CRUD =====
const scenicDialog = ref(false)
const savingScenic = ref(false)
const editingScenic = ref({})
const scenicCoverPreview = ref('')
const scenicCoverFile = ref(null)

const showScenicDialog = (row) => {
  editingScenic.value = row ? { ...row } : { recommendLevel: 3 }
  scenicCoverPreview.value = ''
  scenicCoverFile.value = null
  scenicDialog.value = true
}

const onScenicCoverChange = (file) => {
  const raw = file.raw
  if (!raw || raw.size > 5 * 1024 * 1024) { ElMessage.warning('图片大小不能超过 5MB'); return }
  scenicCoverFile.value = raw
  const reader = new FileReader()
  reader.onload = e => { scenicCoverPreview.value = e.target.result }
  reader.readAsDataURL(raw)
}

const saveScenic = async () => {
  savingScenic.value = true
  try {
    if (scenicCoverFile.value) {
      const fd = new FormData(); fd.append('file', scenicCoverFile.value)
      const ur = await http.post('/file/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      editingScenic.value.coverImage = ur.data?.data || ur.data || ''
    }
    const s = editingScenic.value
    if (s.scenicId) await http.put(`/admin/scenic/${s.scenicId}`, s)
    else await http.post('/admin/scenic', s)
    scenicDialog.value = false
    ElMessage.success('保存成功')
    loadScenic(1)
  } catch {} finally { savingScenic.value = false }
}

const delScenic = async (id) => {
  try {
    await ElMessageBox.confirm('确认下架该景点？', '提示', { confirmButtonText: '确认下架', cancelButtonText: '取消', type: 'warning' })
    await http.delete(`/admin/scenic/${id}`)
    ElMessage.success('已下架')
    loadScenic(1)
  } catch {}
}

// ===== Hotel CRUD =====
const hotelDialog = ref(false)
const savingHotel = ref(false)
const editingHotel = ref({})
const hotelCoverPreview = ref('')
const hotelCoverFile = ref(null)

const hotelFirstImage = computed(() => {
  try {
    if (editingHotel.value.images) {
      const arr = JSON.parse(editingHotel.value.images)
      if (arr?.length) return arr[0]
    }
  } catch {}
  return ''
})

const showHotelDialog = (row) => {
  editingHotel.value = row ? { ...row } : { starLevel: 3 }
  hotelCoverPreview.value = ''
  hotelCoverFile.value = null
  hotelDialog.value = true
}

const onHotelCoverChange = (file) => {
  const raw = file.raw
  if (!raw || raw.size > 5 * 1024 * 1024) { ElMessage.warning('图片大小不能超过 5MB'); return }
  hotelCoverFile.value = raw
  const reader = new FileReader()
  reader.onload = e => { hotelCoverPreview.value = e.target.result }
  reader.readAsDataURL(raw)
}

const saveHotel = async () => {
  savingHotel.value = true
  try {
    if (hotelCoverFile.value) {
      const fd = new FormData(); fd.append('file', hotelCoverFile.value)
      const ur = await http.post('/file/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      editingHotel.value.images = JSON.stringify([ur.data?.data || ur.data || ''])
    }
    const h = editingHotel.value
    if (h.hotelId) await http.put(`/admin/hotel/${h.hotelId}`, h)
    else await http.post('/admin/hotel', h)
    hotelDialog.value = false
    ElMessage.success('保存成功')
    loadHotel(1)
  } catch {} finally { savingHotel.value = false }
}

const delHotel = async (id) => {
  try {
    await ElMessageBox.confirm('确认下架该酒店？', '提示', { confirmButtonText: '确认下架', cancelButtonText: '取消', type: 'warning' })
    await http.put(`/admin/hotel/${id}`, { status: 0 })
    ElMessage.success('已下架')
    loadHotel(1)
  } catch {}
}

// ===== User =====
const toggleUser = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await http.put(`/admin/user/${row.userId}/status?status=${newStatus}`)
    ElMessage.success(`已${newStatus === 1 ? '启用' : '禁用'}用户 ${row.username}`)
    loadUser(1)
  } catch {}
}

const deleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除用户「${row.username}」？此操作不可恢复！`, '警告', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' })
    await http.delete(`/admin/user/${row.userId}`)
    ElMessage.success('用户已删除')
    loadUser(1)
  } catch {}
}

const createUserDialog = ref(false)
const creatingUser = ref(false)
const newUser = ref({ username: '', password: '', nickname: '', phone: '' })

const showCreateUserDialog = () => {
  newUser.value = { username: '', password: '', nickname: '', phone: '' }
  createUserDialog.value = true
}

const doCreateUser = async () => {
  if (!newUser.value.username || newUser.value.username.length < 3) { ElMessage.warning('用户名至少3个字符'); return }
  if (!newUser.value.password || newUser.value.password.length < 6) { ElMessage.warning('密码至少6个字符'); return }
  creatingUser.value = true
  try {
    await http.post('/admin/user', newUser.value)
    ElMessage.success('用户创建成功')
    createUserDialog.value = false
    loadUser(1)
  } catch {} finally { creatingUser.value = false }
}

// ===== Note & Comment =====
const delNote = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该游记？', '提示', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' })
    await http.delete(`/admin/note/${id}`)
    ElMessage.success('游记已删除')
    loadNote(1)
  } catch {}
}

const delComment = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该评论？', '提示', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' })
    await http.delete(`/admin/comment/${id}`)
    ElMessage.success('评论已删除')
    loadComment(1)
  } catch {}
}

// ===== Init =====
onMounted(async () => {
  await loadDashboardCounts()
  loadCurrentTab()
})
</script>

<style scoped>
.breadcrumb { margin-bottom: 16px; }
.breadcrumb :deep(.el-breadcrumb__inner) { color: var(--text-secondary) !important; font-size: 0.85rem; }

.admin-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.admin-header h2 { font-family: var(--font-display); font-size: 1.6rem; font-weight: 700; }
.admin-subtitle { color: var(--text-secondary); font-size: 0.85rem; margin-top: 2px; }

/* Dashboard Stats */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: var(--bg-card); border-radius: var(--radius); padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); transition: transform 0.2s, box-shadow 0.2s; }
.stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow); }
.stat-icon { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 1.5rem; font-weight: 800; font-family: var(--font-display); color: var(--text); line-height: 1.2; }
.stat-label { font-size: 0.78rem; color: var(--text-secondary); }

/* Admin Card */
.admin-card { background: var(--bg-card); border-radius: var(--radius-lg); padding: 8px 24px 24px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); }

/* Toolbar */
.section-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 0; gap: 12px; flex-wrap: wrap; }
.toolbar-left { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

/* Table */
.data-table { margin-top: 4px; }
.table-name-cell { display: flex; align-items: center; gap: 8px; }
.table-thumb { width: 36px; height: 36px; border-radius: 8px; object-fit: cover; flex-shrink: 0; }
.interact-stat { font-size: 0.78rem; margin: 0 4px; color: var(--text-secondary); }

/* Pagination */
.pagination-wrap { display: flex; justify-content: center; padding: 20px 0 4px; }

/* Cover upload */
.cover-upload-row { display: flex; align-items: flex-start; gap: 12px; }
.cover-preview-box { position: relative; flex-shrink: 0; }
.cover-preview { width: 120px; height: 80px; object-fit: cover; border-radius: var(--radius-sm); border: 1px solid var(--border); }
.cover-delete { position: absolute; top: -6px; right: -6px; width: 20px; height: 20px; background: var(--danger); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 0.7rem; cursor: pointer; }
.cover-upload-btn { flex-shrink: 0; }
.upload-placeholder-small { width: 80px; height: 80px; border: 2px dashed var(--border); border-radius: var(--radius-sm); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px; cursor: pointer; color: var(--text-secondary); font-size: 0.75rem; transition: all 0.2s; }
.upload-placeholder-small:hover { border-color: var(--primary); color: var(--primary); }

@media (max-width: 992px) { .stats-row { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .stats-row { grid-template-columns: 1fr; } }
</style>
