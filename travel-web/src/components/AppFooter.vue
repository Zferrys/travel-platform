<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <div class="footer-col brand-col">
        <div class="footer-logo">
          <svg width="36" height="36" viewBox="0 0 32 32">
            <circle cx="16" cy="16" r="15" fill="rgba(255,255,255,0.1)"/>
            <path d="M10 22 Q16 8 22 22" fill="none" stroke="#ff6b6b" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="16" cy="12" r="3" fill="#ff6b6b"/>
          </svg>
          <span>漫游记</span>
        </div>
        <p>发现世界之美，记录旅途精彩。<br/>让每一次出发都充满期待。</p>
        <div class="social-links">
          <span>微博</span>
          <span>微信</span>
          <span>小红书</span>
        </div>
      </div>

      <div class="footer-col">
        <h4>探索</h4>
        <router-link to="/scenic">热门景点</router-link>
        <router-link to="/hotel">精选酒店</router-link>
        <router-link to="/notes">旅行游记</router-link>
        <router-link to="/search">搜索目的地</router-link>
      </div>

      <div class="footer-col">
        <h4>服务</h4>
        <a href="#">帮助中心</a>
        <a href="#">预订须知</a>
        <a href="#">退改政策</a>
        <a href="#">用户协议</a>
      </div>

      <div class="footer-col">
        <h4>关于</h4>
        <a href="#">关于我们</a>
        <a href="#">联系我们</a>
        <a href="#">加入我们</a>
        <a href="#">合作伙伴</a>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="footer-bottom-inner">
        <p>&copy; 2024 漫游记 Travel Platform. All rights reserved.</p>
        <div class="footer-bottom-links">
          <a href="#">隐私政策</a>
          <a href="#">Cookie 政策</a>
          <a href="#">法律声明</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: linear-gradient(180deg, #2d3436 0%, #1e272e 100%);
  color: #b2bec3;
  margin-top: 80px;
}

.footer-inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 60px 24px 40px;
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  gap: 40px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.footer-logo span {
  color: #fff;
  font-family: var(--font-display);
  font-size: 1.2rem;
  font-weight: 700;
}

.brand-col p {
  font-size: 0.9rem;
  line-height: 1.7;
  margin-bottom: 16px;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-links span {
  padding: 4px 14px;
  background: rgba(255,255,255,0.08);
  border-radius: 20px;
  font-size: 0.78rem;
  cursor: pointer;
  transition: all 0.2s;
  color: #dfe6e9;
}

.social-links span:hover {
  background: rgba(255,107,107,0.3);
  color: #fff;
}

.footer-col h4 {
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 16px;
}

.footer-col a {
  display: block;
  color: #94a3b8;
  font-size: 0.85rem;
  margin-bottom: 10px;
  transition: color 0.2s;
}

.footer-col a:hover {
  color: var(--primary-light);
}

.footer-bottom {
  border-top: 1px solid rgba(255,255,255,0.08);
  padding: 20px 24px;
}

.footer-bottom-inner {
  max-width: 1240px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.footer-bottom p {
  font-size: 0.78rem;
}

.footer-bottom-links {
  display: flex;
  gap: 20px;
}

.footer-bottom-links a {
  color: #94a3b8;
  font-size: 0.78rem;
}

.footer-bottom-links a:hover {
  color: var(--primary-light);
}

@media (max-width: 768px) {
  .footer-inner {
    grid-template-columns: 1fr 1fr;
    gap: 32px;
  }
}

@media (max-width: 480px) {
  .footer-inner {
    grid-template-columns: 1fr;
  }
}
</style>
