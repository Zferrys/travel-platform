// 图片加载失败时的兜底 SVG（纯渐变色，无 emoji）
export const FALLBACK = {
  scenic: 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 400"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#4facfe"/><stop offset="100%" style="stop-color:#00f2fe"/></linearGradient></defs><rect width="800" height="400" fill="url(#g)" opacity="0.85"/></svg>'),
  hotel:  'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 400"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#1e3c72"/><stop offset="100%" style="stop-color:#2a5298"/></linearGradient></defs><rect width="800" height="400" fill="url(#g)" opacity="0.85"/></svg>'),
  note:   'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 400"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#f5af19"/><stop offset="100%" style="stop-color:#f12711"/></linearGradient></defs><rect width="800" height="400" fill="url(#g)" opacity="0.85"/></svg>'),
  avatar: 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><defs><linearGradient id="av" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#667eea"/><stop offset="100%" style="stop-color:#764ba2"/></linearGradient></defs><circle cx="100" cy="100" r="100" fill="url(#av)"/><circle cx="100" cy="75" r="32" fill="rgba(255,255,255,0.85)"/><ellipse cx="100" cy="160" rx="55" ry="40" fill="rgba(255,255,255,0.85)"/></svg>'),
  general:'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 400"><rect width="800" height="400" fill="#e2e8f0"/></svg>'),
}

/** 解析酒店 images JSON 数组，返回第一张图片 URL */
export function parseHotelFirstImage(imagesJson) {
  if (!imagesJson) return ''
  try {
    const arr = JSON.parse(imagesJson)
    return (Array.isArray(arr) && arr.length) ? arr[0] : ''
  } catch {
    return ''
  }
}
