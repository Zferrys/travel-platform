import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器 — 自动带 Token
http.interceptors.request.use(config => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 — 统一错误处理
http.interceptors.response.use(
  response => {
    const res = response.data
    if (!res || typeof res !== 'object') {
      return Promise.reject(new Error('Invalid response'))
    }
    if (Number(res.code) === 401) {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      router.push('/login')
      ElMessage.error('请先登录')
      return Promise.reject(new Error(res.message))
    }
    if (Number(res.code) !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response && error.response.status === 401) {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('网络异常，请稍后再试')
    }
    return Promise.reject(error)
  }
)

// ---- API ----

export const userApi = {
  login: data => http.post('/user/login', data),
  register: data => http.post('/user/register', data),
  getUserInfo: () => http.get('/user/info'),
  updateUserInfo: data => http.put('/user/info', data),
}

export const scenicApi = {
  getList: params => http.get('/scenic/list', { params }),
  getDetail: id => http.get(`/scenic/detail/${id}`),
  getHot: params => http.get('/scenic/hot', { params }),
  search: params => http.get('/scenic/search', { params }),
  recommend: params => http.get('/scenic/recommend', { params }),
  nearby: params => http.get('/scenic/nearby', { params }),
}

export const hotelApi = {
  getList: params => http.get('/hotel/list', { params }),
  getDetail: id => http.get(`/hotel/detail/${id}`),
  search: params => http.get('/hotel/search', { params }),
  getRooms: hotelId => http.get(`/hotel/rooms/${hotelId}`),
  getNearby: params => http.get('/hotel/nearby', { params }),
}

export const orderApi = {
  createHotel: data => http.post('/order/hotel', data),
  seckill: data => http.post('/order/seckill', data),
  getDetail: orderId => http.get(`/order/detail/${orderId}`),
  getList: params => http.get('/order/list', { params }),
  pay: (orderId, payType) => http.post(`/order/pay/${orderId}`, null, { params: { payType } }),
  cancel: orderId => http.post(`/order/cancel/${orderId}`),
  complete: orderId => http.post(`/order/complete/${orderId}`),
}

export const noteApi = {
  getList: params => http.get('/travel-note/list', { params }),
  getDetail: id => http.get(`/travel-note/detail/${id}`),
  getMy: () => http.get('/travel-note/my'),
  publish: data => http.post('/travel-note/publish', data),
  update: (id, data) => http.put(`/travel-note/${id}`, data),
  delete: id => http.delete(`/travel-note/${id}`),
  like: id => http.post(`/travel-note/like/${id}`),
  unlike: id => http.post(`/travel-note/unlike/${id}`),
}

export const commentApi = {
  getList: (targetType, targetId) => http.get(`/comment/list/${targetType}/${targetId}`),
  publish: data => http.post('/comment/publish', data),
  delete: id => http.delete(`/comment/${id}`),
}

export const favoriteApi = {
  add: data => http.post('/favorite/add', data),
  remove: params => http.delete('/favorite/remove', { params }),
  check: params => http.get('/favorite/check', { params }),
  getList: params => http.get('/favorite/list', { params }),
}

export const fileApi = {
  upload: file => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' }, timeout: 30000 })
  }
}

export const captchaApi = {
  generate: () => http.get('/captcha/generate'),
}

export const adminApi = {
  // 景点
  listScenics: () => http.get('/admin/scenics'),
  addScenic: data => http.post('/admin/scenic', data),
  updateScenic: (id, data) => http.put(`/admin/scenic/${id}`, data),
  deleteScenic: id => http.delete(`/admin/scenic/${id}`),
  // 酒店
  listHotels: () => http.get('/admin/hotels'),
  addHotel: data => http.post('/admin/hotel', data),
  updateHotel: (id, data) => http.put(`/admin/hotel/${id}`, data),
  deleteHotel: id => http.delete(`/admin/hotel/${id}`),
  // 用户
  listUsers: () => http.get('/admin/users'),
  updateUserStatus: (id, status) => http.put(`/admin/user/${id}/status`, null, { params: { status } }),
  deleteUser: id => http.delete(`/admin/user/${id}`),
  createUser: data => http.post('/admin/user', data),
  // 游记管理
  listNotes: params => http.get('/admin/notes', { params }),
  deleteNote: id => http.delete(`/admin/note/${id}`),
  // 评论管理
  listComments: params => http.get('/admin/comments', { params }),
  deleteComment: id => http.delete(`/admin/comment/${id}`),
  // 订单
  listOrders: params => http.get('/admin/orders', { params }),
}

export default http
