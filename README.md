# 漫游记 Travel Platform

基于 **Vue 3 + Spring MVC + MyBatis + MySQL** 的轻量级旅游服务平台。

## 功能

- 🏔️ **景点探索**：浏览、搜索、地图定位、附近推荐
- 🏨 **酒店预订**：房型选择、在线预订
- ✍️ **游记分享**：写游记、点赞、评论、收藏
- 👤 **用户中心**：个人资料、收藏管理、订单管理
- ⚙️ **后台管理**：景点/酒店/用户/游记/评论的 CRUD 管理

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 + Vite + Element Plus + Leaflet + Axios |
| 后端 | Java 8 + Spring MVC 5.3 + MyBatis 3.5 |
| 数据库 | MySQL 5.7 |
| 认证 | JWT (jjwt 0.11.5) + BCrypt |
| 缓存 | Redis (Jedis) / LocalCache fallback |

## 项目结构

```
travel_control/
├── travel-web/          # Vue 3 前端
│   ├── src/
│   │   ├── api/         # API 接口层
│   │   ├── components/  # 公共组件
│   │   ├── views/       # 页面组件
│   │   ├── router/      # 路由配置
│   │   ├── store/       # Vuex 状态管理
│   │   └── utils/       # 工具函数
│   └── vite.config.js
├── travel-server/       # Java 后端
│   ├── src/main/java/com/travel/
│   │   ├── controller/  # REST 控制器
│   │   ├── service/     # 业务逻辑
│   │   ├── mapper/      # MyBatis Mapper
│   │   └── entity/      # 实体类
│   └── src/main/resources/
│       ├── mapper/      # MyBatis XML
│       └── spring/      # Spring 配置
└── sql/                 # 数据库脚本
    ├── init.sql         # 建表脚本
    ├── seed.sql         # 种子数据
    └── more_seed.sql    # 扩展种子数据
```

## 快速启动

### 1. 数据库
```bash
mysql -u root -p travel_db < sql/init.sql
mysql -u root -p travel_db < sql/seed.sql
```

### 2. 后端 (Tomcat 8.0 + JDK 8)
```bash
cd travel-server
mvn clean package
# 将 target/travel-platform.war 部署到 Tomcat
```

### 3. 前端 (Vite)
```bash
cd travel-web
npm install
npm run dev
# 访问 http://localhost:3000
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `JWT_SECRET` | JWT 签名密钥 | `travel-platform-secret-key-2024!!` (开发) |
| `UPLOAD_DIR` | 图片上传目录 | `~/travel_upload` |
| `CORS_ALLOWED_ORIGINS` | 允许的跨域来源 | `localhost:3000` (开发) |
