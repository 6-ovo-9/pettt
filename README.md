# AI辅助宠物营养配比推荐与鲜食库存管理系统 (Web工程包)

## 一、 系统架构与技术栈
* **后端 (Backend)**: Java 17 + Spring Boot 3.2.5 + Spring Data JDBC + MySQL 8.0 + JWT
* **前端 (Frontend)**: Vue 3 + Vite + Element Plus + ECharts + Axios
* **核心业务逻辑**:
  1. **宠物档案与 AI 营养测算**: 基于静息能量需求(RER)与每日维持能量(DER)公式推导个性化鲜食克重与营养配比。
  2. **短保鲜食批次库存管理**: 抛弃单一总数库存，支持生产批次号(BatchNo)、冷链货位管理。
  3. **先进先出 (FIFO) 出库**: 自动锁定并优先扣减到期日最近(ExpireDate ASC)的健康批次。
  4. **短保效期动态监测大屏**: 自动识别正常、临期(≤3天)和过期状态，通过 ECharts 实现动态柱状预警大屏。

---

## 二、 快速部署与运行步骤

### 1. 数据库准备
1. 打开本地 MySQL（版本 5.7 或 8.0 均可）；
2. 运行 `sql/init_schema.sql` 脚本，将自动创建数据库 `pet_fresh_db` 并初始化演示数据。

### 2. 后端服务启动
1. 打开终端进入 `backend/` 目录；
2. 检查 `src/main/resources/application.yml` 中的 MySQL 用户名和密码；
3. 执行 Maven 编译与启动：
   ```bash
   mvn clean spring-boot:run
   ```
   后端将在 `http://localhost:8080` 启动就绪。

### 3. 前端界面启动
1. 打开终端进入 `frontend/` 目录；
2. 安装依赖并启动 Vite 开发服务器：
   ```bash
   npm install
   npm run dev
   ```
3. 浏览器访问终端输出的本地地址（默认 `http://localhost:5173`）即可进入系统操作。
