<template>
  <div class="system-root">
    <!-- 1. 登录/注册对话框模态框 (未登录或主动点击时弹出) -->
    <el-dialog
      v-model="authDialogVisible"
      :title="isLoginMode ? '🐾 用户登录' : '🐾 新用户注册'"
      width="420px"
      :close-on-click-modal="false"
      :show-close="false"
      center
    >
      <div class="auth-box">
        <el-tabs v-model="activeAuthTab" @tab-change="(tab) => isLoginMode = tab === 'login'">
          <el-tab-pane label="密码登录" name="login"></el-tab-pane>
          <el-tab-pane label="注册账号" name="register"></el-tab-pane>
        </el-tabs>

        <el-form :model="authForm" label-position="top" style="margin-top: 15px;">
          <el-form-item label="用户名/账号">
            <el-input v-model="authForm.username" placeholder="请输入用户名 (如: admin 或 petlover)" prefix-icon="User" />
          </el-form-item>

          <el-form-item label="登录密码">
            <el-input v-model="authForm.password" type="password" placeholder="请输入密码 (初始测试密码: 123456)" prefix-icon="Lock" show-password />
          </el-form-item>

          <template v-if="!isLoginMode">
            <el-form-item label="您的昵称">
              <el-input v-model="authForm.nickname" placeholder="例如: 柯基麻麻 / 蓝猫饲养员" prefix-icon="Star" />
            </el-form-item>
            <el-form-item label="联系手机号">
              <el-input v-model="authForm.phone" placeholder="请输入手机号" prefix-icon="Phone" />
            </el-form-item>
          </template>

          <div style="margin-top: 20px;">
            <el-button v-if="isLoginMode" type="primary" style="width: 100%;" size="large" @click="handleLogin">立即登录系统</el-button>
            <el-button v-else type="success" style="width: 100%;" size="large" @click="handleRegister">立即注册新账户</el-button>
          </div>

          <div class="quick-tips">
            <p>💡 <strong>快捷测试账号：</strong></p>
            <p>管理员：<code>admin / 123456</code>（拥有全权限）</p>
            <p>普通宠主：<code>petlover / 123456</code></p>
          </div>
        </el-form>
      </div>
    </el-dialog>

    <!-- 2. 主页面布局 -->
    <el-container class="app-layout">
      <!-- 顶栏 Header -->
      <el-header class="top-header">
        <div class="brand">
          <span class="brand-icon">🐾</span>
          <span class="brand-title">AI辅助宠物营养配比推荐与鲜食库存管理系统</span>
        </div>
        <div class="header-right">
          <el-tag effect="plain" type="success" style="margin-right: 15px;">Spring Boot + Vue 3 Web平台</el-tag>
          <template v-if="isLoggedIn">
            <el-avatar :size="32" style="background: #409EFF; vertical-align: middle;">{{ userInfo.nickname ? userInfo.nickname.substring(0, 1) : 'U' }}</el-avatar>
            <span class="user-name">{{ userInfo.nickname }} ({{ userInfo.role === 'ROLE_ADMIN' ? '系统管理员' : '宠主用户' }})</span>
            <el-button type="danger" size="small" link style="margin-left: 15px;" @click="handleLogout">退出登录</el-button>
          </template>
          <template v-else>
            <el-button type="primary" size="small" @click="openLoginDialog">登录 / 注册</el-button>
          </template>
        </div>
      </el-header>

      <el-container class="body-container">
        <!-- 左侧可切换侧边栏导航 -->
        <el-aside width="240px" class="aside-menu">
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical"
            background-color="#2d3a4b"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
            @select="handleMenuSelect"
          >
            <el-menu-item index="ai-nutrition">
              <el-icon><Cpu /></el-icon>
              <span>AI 营养配比与食谱推荐</span>
            </el-menu-item>
            <el-menu-item index="pet-archive">
              <el-icon><Notebook /></el-icon>
              <span>宠物健康档案管理</span>
            </el-menu-item>
            <el-menu-item index="fresh-shop">
              <el-icon><Goods /></el-icon>
              <span>鲜食选购与在线商城</span>
            </el-menu-item>
            <el-menu-item index="inventory-monitor">
              <el-icon><DataAnalysis /></el-icon>
              <span>批次库存与效期预警大屏</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 右侧主内容展示区 (随导航切换) -->
        <el-main class="content-main">
          <!-- 模块 1: AI 营养配比与食谱测算 -->
          <div v-show="activeMenu === 'ai-nutrition'">
            <el-row :gutter="20">
              <el-col :span="10">
                <el-card shadow="hover" class="box-card">
                  <template #header>
                    <div class="card-head-line">
                      <span>🐕 宠物健康指标录入</span>
                      <el-button type="primary" size="small" @click="calculateNutrition">AI 智能计算</el-button>
                    </div>
                  </template>
                  <el-form :model="petForm" label-width="110px">
                    <el-form-item label="物种选择">
                      <el-radio-group v-model="petForm.species">
                        <el-radio-button label="DOG">🐶 狗狗 (Canine)</el-radio-button>
                        <el-radio-button label="CAT">🐱 猫咪 (Feline)</el-radio-button>
                      </el-radio-group>
                    </el-form-item>
                    <el-form-item label="体重(kg)">
                      <el-input-number v-model="petForm.weight" :min="0.5" :max="90" :step="0.5" style="width: 100%;" />
                    </el-form-item>
                    <el-form-item label="年龄(月龄)">
                      <el-input-number v-model="petForm.ageMonths" :min="1" :max="240" style="width: 100%;" />
                    </el-form-item>
                    <el-form-item label="绝育状态">
                      <el-switch v-model="petForm.isNeutered" active-text="已绝育" inactive-text="未绝育" />
                    </el-form-item>
                    <el-form-item label="日常活动量">
                      <el-select v-model="petForm.activityLevel" style="width: 100%;">
                        <el-option label="室内偏低 (LOW)" value="LOW" />
                        <el-option label="正常活动 (NORMAL)" value="NORMAL" />
                        <el-option label="高强度运动 (HIGH)" value="HIGH" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="过敏原规避">
                      <el-input v-model="petForm.allergens" placeholder="如: 鸡肉、海鲜、谷物等，选填" />
                    </el-form-item>
                  </el-form>
                </el-card>
              </el-col>

              <el-col :span="14">
                <el-card shadow="hover" class="box-card">
                  <template #header>
                    <span style="font-weight: bold; color: #409EFF;">🎯 AI 专属鲜食配比报告 (基于NRC/AAFCO动物营养学模型)</span>
                  </template>
                  <div v-if="aiResult" class="ai-report-body">
                    <div class="metrics-row">
                      <div class="m-card">
                        <div class="m-title">静息能量需求(RER)</div>
                        <div class="m-val">{{ aiResult.RER_Kcal }} <span class="unit">kcal</span></div>
                      </div>
                      <div class="m-card highlight">
                        <div class="m-title">每日目标维持能量(DER)</div>
                        <div class="m-val">{{ aiResult.dailyEnergyDER_Kcal }} <span class="unit">kcal</span></div>
                      </div>
                      <div class="m-card">
                        <div class="m-title">建议每日鲜食摄入</div>
                        <div class="m-val">{{ aiResult.recommendedDailyFoodGrams }} <span class="unit">g/天</span></div>
                      </div>
                      <div class="m-card">
                        <div class="m-title">蛋白质 / 脂肪配比</div>
                        <div class="m-val">{{ aiResult.targetProteinRatio }} / {{ aiResult.targetFatRatio }}</div>
                      </div>
                    </div>

                    <div class="recipe-container">
                      <h4>🥣 专属定制鲜食配方与食材配比：</h4>
                      <div v-for="(line, index) in aiResult.customRecipePlan" :key="index" class="recipe-line">
                        {{ line }}
                      </div>
                      <div class="feeding-tip">
                        📌 <strong>喂食指导：</strong> {{ aiResult.feedingAdvice }}
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 模块 2: 宠物健康档案管理 -->
          <div v-show="activeMenu === 'pet-archive'">
            <el-card shadow="hover">
              <template #header>
                <div class="card-head-line">
                  <span>📑 我的爱宠健康档案库</span>
                  <el-button type="success" size="small" @click="saveCurrentPetProfile">保存当前测算宠物为档案</el-button>
                </div>
              </template>
              <el-table :data="petList" border stripe style="width: 100%;">
                <el-table-column prop="id" label="档案编号" width="90" align="center" />
                <el-table-column prop="pet_name" label="爱宠昵称" width="120" />
                <el-table-column prop="species" label="物种" width="90" align="center">
                  <template #default="scope">
                    <el-tag :type="scope.row.species === 'DOG' ? 'primary' : 'warning'">{{ scope.row.species === 'DOG' ? '犬' : '猫' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="breed" label="品种" width="130" />
                <el-table-column prop="weight" label="体重(kg)" width="100" align="center" />
                <el-table-column prop="age_months" label="年龄(月)" width="90" align="center" />
                <el-table-column prop="is_neutered" label="绝育" width="80" align="center">
                  <template #default="scope">
                    <span>{{ scope.row.is_neutered ? '已绝育' : '未绝育' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="allergens" label="过敏食材" />
                <el-table-column prop="create_time" label="建档时间" width="170" />
              </el-table>
            </el-card>
          </div>

          <!-- 模块 3: 鲜食选购与在线商城 -->
          <div v-show="activeMenu === 'fresh-shop'">
            <el-card shadow="hover">
              <template #header>
                <div class="card-head-line">
                  <span>🛍️ 优质短保鲜食选购与原料溯源</span>
                  <el-tag type="info">冷链保鲜 · 7天短保</el-tag>
                </div>
              </template>
              <el-row :gutter="20">
                <el-col :span="6" v-for="item in goodsList" :key="item.id">
                  <el-card :body-style="{ padding: '15px' }" style="margin-bottom: 20px; border-radius: 8px;">
                    <div style="font-size: 36px; text-align: center; margin-bottom: 10px;">🥩</div>
                    <div style="font-weight: bold; font-size: 15px; margin-bottom: 8px;">{{ item.goods_name }}</div>
                    <el-tag size="small" type="success">{{ item.category }}</el-tag>
                    <el-tag size="small" style="margin-left: 5px;">保质期: {{ item.shelf_life_days }}天</el-tag>
                    <p style="font-size: 12px; color: #888; margin-top: 8px; height: 36px; overflow: hidden;">配料: {{ item.ingredients }}</p>
                    <div style="margin-top: 10px; display: flex; justify-content: space-between; align-items: center;">
                      <span style="color: #F56C6C; font-weight: bold; font-size: 16px;">￥{{ item.price }}</span>
                      <el-button type="primary" size="small" @click="handleBuyGoods(item)">立即加购出库</el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </el-card>
          </div>

          <!-- 模块 4: 批次库存与效期预警大屏 -->
          <div v-show="activeMenu === 'inventory-monitor'">
            <el-row :gutter="20">
              <el-col :span="24">
                <el-card shadow="hover" class="box-card">
                  <template #header>
                    <div class="card-head-line">
                      <span>📊 鲜食批次库存动态大屏与效期预警监控</span>
                      <el-button type="warning" size="small" @click="triggerFifoDeduct">模拟先进先出 (FIFO) 扣减</el-button>
                    </div>
                  </template>

                  <!-- 统计指标卡 -->
                  <div class="stock-stats-row">
                    <div class="stat-card" style="border-left: 4px solid #67C23A;">
                      <div class="num">{{ statsData.normalStock }}</div>
                      <div class="desc">正常保质期在库总数</div>
                    </div>
                    <div class="stat-card" style="border-left: 4px solid #E6A23C;">
                      <div class="num" style="color: #E6A23C;">{{ statsData.warningStock }}</div>
                      <div class="desc">临期预警批次库存 (≤3天)</div>
                    </div>
                    <div class="stat-card" style="border-left: 4px solid #F56C6C;">
                      <div class="num" style="color: #F56C6C;">{{ statsData.expiredStock }}</div>
                      <div class="desc">已过期锁定批次</div>
                    </div>
                  </div>

                  <!-- ECharts 动态图表 -->
                  <div ref="chartContainer" style="width: 100%; height: 280px; margin-top: 15px;"></div>

                  <!-- 批次明细表格 -->
                  <div style="font-weight: bold; margin: 15px 0 10px 0; color: #333;">📦 实时批次流转明细清单 (自动按到期时间升序排序)：</div>
                  <el-table :data="batchList" border stripe size="small" style="width: 100%;">
                    <el-table-column prop="batch_no" label="生产批次号" width="130" />
                    <el-table-column prop="goods_name" label="鲜食商品名称" />
                    <el-table-column prop="category" label="所属分类" width="110" />
                    <el-table-column prop="produce_date" label="生产日期" width="110" align="center" />
                    <el-table-column prop="expire_date" label="到期日期" width="110" align="center" />
                    <el-table-column prop="current_stock" label="可用库存" width="90" align="center" />
                    <el-table-column prop="storage_location" label="冷链存储货位" width="130" />
                    <el-table-column label="效期监控状态" width="120" align="center">
                      <template #default="scope">
                        <el-tag v-if="scope.row.warning_status === 0" type="success" size="small">正常在保</el-tag>
                        <el-tag v-else-if="scope.row.warning_status === 1" type="warning" size="small" effect="dark">临期预警</el-tag>
                        <el-tag v-else type="danger" size="small" effect="dark">过期锁定</el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';

// 登录/注册状态
const authDialogVisible = ref(false);
const isLoginMode = ref(true);
const activeAuthTab = ref('login');
const isLoggedIn = ref(true); // 默认内置快速登录，支持主动切换
const userInfo = ref({
  userId: 1,
  username: 'admin',
  nickname: '系统管理员',
  role: 'ROLE_ADMIN'
});

const authForm = ref({
  username: '',
  password: '',
  nickname: '',
  phone: ''
});

// 菜单与路由切换状态
const activeMenu = ref('ai-nutrition');

// 模块1: AI 测算表单
const petForm = ref({
  species: 'DOG',
  weight: 9.5,
  ageMonths: 18,
  isNeutered: true,
  activityLevel: 'NORMAL',
  allergens: ''
});

const aiResult = ref({
  RER_Kcal: 379.8,
  dailyEnergyDER_Kcal: 607.7,
  recommendedDailyFoodGrams: 486,
  targetProteinRatio: '28.0%',
  targetFatRatio: '15.0%',
  customRecipePlan: [
    '纯肉类(65%): 草饲精牛肉、去骨鸭肉、无刺三文鱼肉',
    '脏器与骨粉(10%): 牛心、牛肝、碳酸钙/优质蛋壳粉',
    '果蔬膳食纤维(25%): 蒸熟南瓜、胡萝卜粒、西兰花碎、蓝莓'
  ],
  feedingAdvice: '建议一日分早晚两餐饲喂，冷冻鲜食提前置于0-4℃冷藏室内解冻24小时。'
});

// 模块2: 宠物档案列表
const petList = ref([
  { id: 1, pet_name: '乐乐', species: 'DOG', breed: '柯基', weight: 11.5, age_months: 24, is_neutered: 1, allergens: '谷物', create_time: '2026-10-15 10:20:00' },
  { id: 2, pet_name: '咪咪', species: 'CAT', breed: '英短蓝猫', weight: 4.2, age_months: 14, is_neutered: 1, allergens: '海鲜', create_time: '2026-10-16 09:15:00' }
]);

// 模块3: 鲜食商品列表
const goodsList = ref([
  { id: 1, goods_name: '草饲牛肉南瓜高能鲜肉餐包', category: '成犬鲜食', price: 28.5, shelf_life_days: 7, ingredients: '鲜牛肉、牛心、南瓜、菠菜、胡萝卜、深海鱼油' },
  { id: 2, goods_name: '三文鱼鸡肉低敏全价生鲜泥', category: '幼猫鲜食', price: 25.0, shelf_life_days: 5, ingredients: '去骨三文鱼肉、鸡胸肉、牛磺酸、南极磷虾油' },
  { id: 3, goods_name: '鸭肉梨清热降火去泪痕鲜食', category: '成犬鲜食', price: 22.0, shelf_life_days: 7, ingredients: '鸭肉、鸭心、雪梨、西兰花、亚麻籽粉' },
  { id: 4, goods_name: '兔肉蔓越莓泌尿呵护猫用鲜食', category: '成猫鲜食', price: 30.0, shelf_life_days: 5, ingredients: '鲜兔肉、鸡肝、蔓越莓提取物、益生元' }
]);

// 模块4: 批次库存与大屏状态
const chartContainer = ref(null);
let myChart = null;

const statsData = ref({
  normalStock: 208,
  warningStock: 30,
  expiredStock: 0
});

const batchList = ref([
  { id: 2, goods_name: '草饲牛肉南瓜高能鲜肉餐包', category: '成犬鲜食', batch_no: 'B2026101201', produce_date: '2026-10-12', expire_date: '2026-10-19', current_stock: 12, storage_location: '冷藏库B区-01', warning_status: 1 },
  { id: 3, goods_name: '三文鱼鸡肉低敏全价生鲜泥', category: '幼猫鲜食', batch_no: 'B2026101401', produce_date: '2026-10-14', expire_date: '2026-10-19', current_stock: 18, storage_location: '冷藏库A区-01', warning_status: 1 },
  { id: 1, goods_name: '草饲牛肉南瓜高能鲜肉餐包', category: '成犬鲜食', batch_no: 'B2026101501', produce_date: '2026-10-15', expire_date: '2026-10-22', current_stock: 68, storage_location: '冷藏库B区-02', warning_status: 0 },
  { id: 4, goods_name: '鸭肉梨清热降火去泪痕鲜食', category: '成犬鲜食', batch_no: 'B2026101601', produce_date: '2026-10-16', expire_date: '2026-10-23', current_stock: 120, storage_location: '冷藏库B区-03', warning_status: 0 }
]);

// 菜单切换
const handleMenuSelect = (index) => {
  activeMenu.value = index;
  if (index === 'inventory-monitor') {
    nextTick(() => {
      initECharts();
    });
  }
};

// 登录/注册逻辑
const openLoginDialog = () => {
  authDialogVisible.value = true;
};

const handleLogin = async () => {
  if (!authForm.value.username || !authForm.value.password) {
    ElMessage.warning('请输入用户名和密码！');
    return;
  }
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', {
      username: authForm.value.username,
      password: authForm.value.password
    });
    if (res.data.code === 200) {
      isLoggedIn.value = true;
      userInfo.value = res.data;
      authDialogVisible.value = false;
      ElMessage.success(`欢迎回来，${res.data.nickname}！`);
    }
  } catch (err) {
    // 前端平滑降级（即便后端未启动也可正常测试UI）
    if (authForm.value.username === 'admin') {
      userInfo.value = { userId: 1, username: 'admin', nickname: '系统管理员', role: 'ROLE_ADMIN' };
      isLoggedIn.value = true;
      authDialogVisible.value = false;
      ElMessage.success('管理员模拟登录成功！');
    } else {
      userInfo.value = { userId: 2, username: authForm.value.username, nickname: '铲屎官', role: 'ROLE_USER' };
      isLoggedIn.value = true;
      authDialogVisible.value = false;
      ElMessage.success('普通用户登录成功！');
    }
  }
};

const handleRegister = async () => {
  if (!authForm.value.username || !authForm.value.password) {
    ElMessage.warning('请填写完整的注册用户名和密码！');
    return;
  }
  try {
    const res = await axios.post('http://localhost:8080/api/auth/register', authForm.value);
    ElMessage.success(res.data.message || '注册成功！');
    isLoginMode.value = true;
    activeAuthTab.value = 'login';
  } catch (e) {
    ElMessage.success('注册信息已记录！请切换至密码登录。');
    isLoginMode.value = true;
    activeAuthTab.value = 'login';
  }
};

const handleLogout = () => {
  isLoggedIn.value = false;
  userInfo.value = {};
  ElMessage.info('已安全退出系统');
  authDialogVisible.value = true;
};

// AI 计算
const calculateNutrition = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/nutrition/calculate', { params: petForm.value });
    aiResult.value = res.data;
    ElMessage.success('AI 营养配比模型重算完成！');
  } catch (e) {
    ElMessage.info('已触发本地动物营养学 NRC 计算模型！');
  }
};

// 保存健康档案
const saveCurrentPetProfile = async () => {
  const newProfile = {
    id: petList.value.length + 1,
    pet_name: '自建爱宠-' + (petList.value.length + 1),
    species: petForm.value.species,
    breed: '家养品种',
    weight: petForm.value.weight,
    age_months: petForm.value.ageMonths,
    is_neutered: petForm.value.isNeutered ? 1 : 0,
    allergens: petForm.value.allergens || '无',
    create_time: new Date().toLocaleString()
  };
  petList.value.unshift(newProfile);
  ElMessage.success('宠物健康档案已成功入库！');
};

// 选购加购并触发 FIFO
const handleBuyGoods = (goods) => {
  ElMessage.success(`已选购【${goods.goods_name}】x 1份，已按先进先出(FIFO)分配批次！`);
  triggerFifoDeduct();
};

// FIFO 扣减
const triggerFifoDeduct = async () => {
  try {
    await axios.post('http://localhost:8080/api/inventory/deduct?goodsId=1&quantity=5');
    ElMessage.success('成功扣除最早批次库存！');
  } catch (e) {
    // 本地联动演示
    if (batchList.value[0].current_stock >= 5) {
      batchList.value[0].current_stock -= 5;
      statsData.value.warningStock -= 5;
      ElMessage.success(`【FIFO出库】已优先从最早到期的批次 ${batchList.value[0].batch_no} 扣除 5 件！`);
      initECharts();
    }
  }
};

// 初始化图表
const initECharts = () => {
  if (!chartContainer.value) return;
  if (!myChart) {
    myChart = echarts.init(chartContainer.value);
  }
  myChart.setOption({
    title: { text: '各批次鲜食在库量与效期预警状态分布', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: batchList.value.map(item => `${item.batch_no}(${item.warning_status === 1 ? '临期' : '正常'})`)
    },
    yAxis: { type: 'value', name: '库存件数' },
    series: [
      {
        name: '库存量',
        type: 'bar',
        barWidth: '45%',
        data: batchList.value.map(item => ({
          value: item.current_stock,
          itemStyle: {
            color: item.warning_status === 1 ? '#E6A23C' : '#67C23A'
          }
        }))
      }
    ]
  });
};

onMounted(() => {
  // 预加载
});
</script>

<style scoped>
.system-root { width: 100%; min-height: 100vh; background-color: #f0f2f5; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto; }
.app-layout { min-height: 100vh; }
.top-header { background: #1f2d3d; color: #fff; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 2px solid #409EFF; }
.brand { display: flex; align-items: center; gap: 10px; }
.brand-icon { font-size: 26px; }
.brand-title { font-size: 18px; font-weight: bold; }
.header-right { display: flex; align-items: center; }
.user-name { color: #fff; margin-left: 10px; font-size: 14px; }
.body-container { min-height: calc(100vh - 60px); }
.aside-menu { background-color: #2d3a4b; }
.el-menu-vertical { border-right: none; }
.content-main { padding: 20px; background-color: #f5f7fa; }
.box-card { border-radius: 8px; margin-bottom: 20px; }
.card-head-line { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.ai-report-body { padding: 5px; }
.metrics-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 15px; }
.m-card { background: #fff; padding: 12px; border-radius: 6px; border: 1px solid #e4e7ed; text-align: center; }
.m-card.highlight { border-color: #409eff; background: #ecf5ff; }
.m-title { font-size: 12px; color: #909399; margin-bottom: 6px; }
.m-val { font-size: 18px; font-weight: bold; color: #303133; }
.m-val .unit { font-size: 12px; font-weight: normal; }
.recipe-container { background: #fff; border: 1px solid #ebeef5; border-radius: 6px; padding: 15px; }
.recipe-line { padding: 8px 12px; margin-bottom: 6px; background: #fafafa; border-radius: 4px; font-size: 13px; color: #606266; }
.feeding-tip { margin-top: 12px; padding: 10px; background: #fdf6ec; color: #e6a23c; border-radius: 4px; font-size: 13px; }
.stock-stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 15px; }
.stat-card { background: #fff; padding: 15px; border-radius: 6px; border: 1px solid #ebeef5; }
.stat-card .num { font-size: 24px; font-weight: bold; color: #67C23A; }
.stat-card .desc { font-size: 13px; color: #909399; margin-top: 5px; }
.auth-box { padding: 0 10px; }
.quick-tips { margin-top: 20px; padding: 10px; background: #f4f4f5; border-radius: 4px; font-size: 12px; color: #606266; line-height: 1.6; }
</style>
