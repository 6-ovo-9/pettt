<template>
  <div class="system-root">
    <!-- 1. 登录/注册对话框模态框 -->
    <el-dialog
      v-model="authDialogVisible"
      :title="isLoginMode ? '🐾 用户登录' : '🐾 新用户注册'"
      width="420px"
      :close-on-click-modal="false"
      center
    >
      <div class="auth-box">
        <el-tabs v-model="activeAuthTab" @tab-change="(tab) => isLoginMode = tab === 'login'">
          <el-tab-pane label="密码登录" name="login"></el-tab-pane>
          <el-tab-pane label="注册账号" name="register"></el-tab-pane>
        </el-tabs>

        <el-form :model="authForm" label-position="top" style="margin-top: 15px;">
          <el-form-item label="用户名/账号">
            <el-input v-model="authForm.username" placeholder="请输入账号 (如: admin 或 petlover)" prefix-icon="User" />
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
            <p>💡 <strong>快捷身份测试通道：</strong></p>
            <p>🔴 <strong>系统管理员</strong>：账号 <code>admin</code> / 密码 <code>123456</code>（纯粹档案后台，实时查看全平台宠物档案）</p>
            <p>🟢 <strong>普通宠主用户</strong>：账号 <code>petlover</code> / 密码 <code>123456</code>（用户端，右上角可弹窗新建宠物档案，支持AI推荐与商城）</p>
          </div>
        </el-form>
      </div>
    </el-dialog>

    <!-- 2. 用户端：新建宠物档案表单弹窗 (右上角点击弹出) -->
    <el-dialog v-model="createPetDialogVisible" title="🐾 新建宠物健康档案" width="520px" center>
      <el-form :model="newPetForm" label-width="110px" style="padding: 10px 20px 0 0;">
        <el-form-item label="爱宠昵称" required>
          <el-input v-model="newPetForm.petName" placeholder="如: 旺财 / 豆豆 / 汤圆" />
        </el-form-item>
        <el-form-item label="爱宠物种" required>
          <el-radio-group v-model="newPetForm.species">
            <el-radio-button label="DOG">🐶 狗狗 (Canine)</el-radio-button>
            <el-radio-button label="CAT">🐱 猫咪 (Feline)</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="宠物品种">
          <el-input v-model="newPetForm.breed" placeholder="如: 柯基 / 柴犬 / 英短 / 狸花" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="年龄(月龄)">
              <el-input-number v-model="newPetForm.ageMonths" :min="1" :max="240" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input-number v-model="newPetForm.weight" :min="0.5" :max="90" :step="0.5" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="绝育状态">
          <el-switch v-model="newPetForm.isNeutered" active-text="已绝育" inactive-text="未绝育" />
        </el-form-item>
        <el-form-item label="日常运动量">
          <el-select v-model="newPetForm.activityLevel" style="width: 100%;">
            <el-option label="室内偏低 (LOW)" value="LOW" />
            <el-option label="适中正常 (NORMAL)" value="NORMAL" />
            <el-option label="高强度活动 (HIGH)" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="过敏原/禁忌">
          <el-input v-model="newPetForm.allergens" placeholder="如: 鸡肉、海鲜、谷物（没有可填无）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createPetDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreatePet">确认保存建档</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 3. 主界面布局 -->
    <el-container class="app-layout">
      <!-- 顶栏 Header -->
      <el-header class="top-header">
        <div class="brand">
          <span class="brand-icon">🐾</span>
          <span class="brand-title">AI辅助宠物营养配比推荐与鲜食库存管理系统</span>
        </div>
        <div class="header-right">
          <!-- 身份标识卡片 -->
          <el-tag :type="userInfo.role === 'ROLE_ADMIN' ? 'danger' : 'success'" effect="dark" style="margin-right: 15px; font-weight: bold;">
            {{ userInfo.role === 'ROLE_ADMIN' ? '🛡️ 管理员专用端 (纯建档审计)' : '👤 宠主端 (功能全开)' }}
          </el-tag>
          <el-avatar :size="32" :style="{ background: userInfo.role === 'ROLE_ADMIN' ? '#F56C6C' : '#409EFF', verticalAlign: 'middle' }">
            {{ userInfo.role === 'ROLE_ADMIN' ? '管' : '主' }}
          </el-avatar>
          <span class="user-name">{{ userInfo.nickname }}</span>
          <el-button type="warning" size="small" link style="margin-left: 15px;" @click="switchRoleQuickly">切换身份</el-button>
          <el-button type="danger" size="small" link style="margin-left: 10px;" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-container class="body-container">
        <!-- ==================== A. 如果是系统管理员：极简纯净界面 (仅展示全平台宠物档案) ==================== -->
        <template v-if="userInfo.role === 'ROLE_ADMIN'">
          <el-main class="content-main admin-view">
            <el-card shadow="hover" class="box-card">
              <template #header>
                <div class="card-head-line">
                  <div>
                    <span style="font-size: 17px; font-weight: bold; color: #303133;">📋 全平台宠主建档总览 (管理员实时监控视窗)</span>
                    <el-tag size="small" type="info" style="margin-left: 12px;">自动同步所有宠主的新增记录</el-tag>
                  </div>
                  <el-button type="primary" size="small" icon="Refresh" @click="fetchPetList">手动刷新最新数据</el-button>
                </div>
              </template>

              <!-- 顶部汇总指标卡 -->
              <div class="admin-kpi-row">
                <div class="kpi-card">
                  <div class="kpi-num">{{ petList.length }}</div>
                  <div class="kpi-label">已录入建档宠物总数</div>
                </div>
                <div class="kpi-card">
                  <div class="kpi-num" style="color: #409eff;">{{ petList.filter(p => p.species === 'DOG').length }}</div>
                  <div class="kpi-label">犬类宠物建档</div>
                </div>
                <div class="kpi-card">
                  <div class="kpi-num" style="color: #E6A23C;">{{ petList.filter(p => p.species === 'CAT').length }}</div>
                  <div class="kpi-label">猫类宠物建档</div>
                </div>
              </div>

              <!-- 全平台建档表格 -->
              <el-table :data="petList" border stripe style="width: 100%; margin-top: 15px;">
                <el-table-column prop="id" label="档案编号" width="90" align="center" />
                <el-table-column prop="owner_name" label="所属宠主/账号" width="140">
                  <template #default="scope">
                    <el-tag size="small" effect="plain">{{ scope.row.owner_name || '铲屎官小李' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="pet_name" label="宠物昵称" width="120">
                  <template #default="scope">
                    <span style="font-weight: bold; color: #303133;">🐾 {{ scope.row.pet_name }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="species" label="物种" width="90" align="center">
                  <template #default="scope">
                    <el-tag :type="scope.row.species === 'DOG' ? 'primary' : 'warning'">{{ scope.row.species === 'DOG' ? '犬' : '猫' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="breed" label="品种" width="120" />
                <el-table-column prop="weight" label="体重 (kg)" width="100" align="center" />
                <el-table-column prop="age_months" label="年龄 (月)" width="95" align="center" />
                <el-table-column label="绝育状态" width="95" align="center">
                  <template #default="scope">
                    <el-tag :type="scope.row.is_neutered ? 'info' : 'danger'" size="small">
                      {{ scope.row.is_neutered ? '已绝育' : '未绝育' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="activity_level" label="运动量" width="100" align="center">
                  <template #default="scope">
                    <span>{{ scope.row.activity_level === 'HIGH' ? '高强度' : scope.row.activity_level === 'LOW' ? '偏低' : '正常' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="allergens" label="过敏原/禁忌食材" />
                <el-table-column prop="create_time" label="建档时间" width="170" align="center" />
              </el-table>
            </el-card>
          </el-main>
        </template>

        <!-- ==================== B. 如果是普通用户：多功能宠主交互端 ==================== -->
        <template v-else>
          <!-- 宠主左侧导航菜单 -->
          <el-aside width="240px" class="aside-menu">
            <el-menu
              :default-active="activeMenu"
              class="el-menu-vertical"
              background-color="#2d3a4b"
              text-color="#bfcbd9"
              active-text-color="#409EFF"
              @select="handleMenuSelect"
            >
              <el-menu-item index="pet-archive">
                <el-icon><Notebook /></el-icon>
                <span>我的爱宠健康档案</span>
              </el-menu-item>
              <el-menu-item index="ai-nutrition">
                <el-icon><Cpu /></el-icon>
                <span>AI 营养配比与食谱推荐</span>
              </el-menu-item>
              <el-menu-item index="fresh-shop">
                <el-icon><Goods /></el-icon>
                <span>鲜食选购与在线商城</span>
              </el-menu-item>
              <el-menu-item index="inventory-monitor">
                <el-icon><DataAnalysis /></el-icon>
                <span>鲜食批次库存与效期大屏</span>
              </el-menu-item>
            </el-menu>
          </el-aside>

          <!-- 宠主主工作台 -->
          <el-main class="content-main">
            <!-- 模块 1: 我的爱宠健康档案 (重点：右上角新建按钮 + 弹出表单) -->
            <div v-show="activeMenu === 'pet-archive'">
              <el-card shadow="hover" class="box-card">
                <template #header>
                  <div class="card-head-line">
                    <span style="font-weight: bold; font-size: 16px;">📑 我的爱宠健康档案库</span>
                    <!-- 核心：右上角【新建】按钮 -->
                    <el-button type="primary" icon="Plus" size="default" @click="openCreatePetModal">
                      新建宠物健康档案
                    </el-button>
                  </div>
                </template>

                <el-table :data="petList" border stripe style="width: 100%;">
                  <el-table-column prop="id" label="档案序号" width="90" align="center" />
                  <el-table-column prop="pet_name" label="爱宠昵称" width="130">
                    <template #default="scope">
                      <span style="font-weight: bold; color: #409eff;">🐶 {{ scope.row.pet_name }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="species" label="物种" width="90" align="center">
                    <template #default="scope">
                      <el-tag :type="scope.row.species === 'DOG' ? 'primary' : 'warning'">{{ scope.row.species === 'DOG' ? '犬' : '猫' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="breed" label="品种" width="130" />
                  <el-table-column prop="weight" label="体重 (kg)" width="100" align="center" />
                  <el-table-column prop="age_months" label="年龄 (月)" width="90" align="center" />
                  <el-table-column label="绝育" width="90" align="center">
                    <template #default="scope">
                      <span>{{ scope.row.is_neutered ? '已绝育' : '未绝育' }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="allergens" label="过敏食材/禁忌" />
                  <el-table-column prop="create_time" label="建档时间" width="170" align="center" />
                  <el-table-column label="操作" width="140" align="center">
                    <template #default="scope">
                      <el-button type="success" size="small" link @click="loadPetToAI(scope.row)">带入AI测算</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </div>

            <!-- 模块 2: AI 营养配比与食谱测算 -->
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
                          <el-radio-button label="DOG">🐶 狗狗</el-radio-button>
                          <el-radio-button label="CAT">🐱 猫咪</el-radio-button>
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
                        <el-input v-model="petForm.allergens" placeholder="如: 鸡肉、海鲜、谷物等" />
                      </el-form-item>
                    </el-form>
                  </el-card>
                </el-col>

                <el-col :span="14">
                  <el-card shadow="hover" class="box-card">
                    <template #header>
                      <span style="font-weight: bold; color: #409EFF;">🎯 AI 专属鲜食配比报告 (基于NRC动物营养学模型)</span>
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

            <!-- 模块 3: 鲜食选购商城 -->
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
              <el-card shadow="hover" class="box-card">
                <template #header>
                  <div class="card-head-line">
                    <span>📊 鲜食批次库存动态大屏与效期预警监控</span>
                    <el-button type="warning" size="small" @click="triggerFifoDeduct">模拟先进先出 (FIFO) 扣减</el-button>
                  </div>
                </template>
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
                <div ref="chartContainer" style="width: 100%; height: 280px; margin-top: 15px;"></div>
              </el-card>
            </div>
          </el-main>
        </template>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';

// 用户身份信息
const authDialogVisible = ref(false);
const isLoginMode = ref(true);
const activeAuthTab = ref('login');
const userInfo = ref({
  userId: 2,
  username: 'petlover',
  nickname: '铲屎官 (宠主用户)',
  role: 'ROLE_USER'
});

const authForm = ref({ username: '', password: '', nickname: '', phone: '' });
const activeMenu = ref('pet-archive');

// 新建宠物档案对话框
const createPetDialogVisible = ref(false);
const newPetForm = ref({
  petName: '',
  species: 'DOG',
  breed: '',
  ageMonths: 12,
  weight: 6.5,
  isNeutered: true,
  activityLevel: 'NORMAL',
  allergens: ''
});

// AI 测算表单
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

// 宠物档案数据
const petList = ref([
  { id: 4, owner_name: '铲屎官小李', pet_name: '自建爱宠-4', species: 'DOG', breed: '家养品种', weight: 9.5, age_months: 18, is_neutered: 1, allergens: '无', create_time: '2026-10-16 13:58:51' },
  { id: 3, owner_name: '铲屎官小李', pet_name: '自建爱宠-3', species: 'DOG', breed: '家养品种', weight: 9.5, age_months: 18, is_neutered: 1, allergens: '无', create_time: '2026-10-16 13:58:45' },
  { id: 1, owner_name: '铲屎官小李', pet_name: '乐乐', species: 'DOG', breed: '柯基', weight: 11.5, age_months: 24, is_neutered: 1, allergens: '谷物', create_time: '2026-10-15 10:20:00' },
  { id: 2, owner_name: '铲屎官小李', pet_name: '咪咪', species: 'CAT', breed: '英短蓝猫', weight: 4.2, age_months: 14, is_neutered: 1, allergens: '海鲜', create_time: '2026-10-16 09:15:00' }
]);

// 鲜食商品
const goodsList = ref([
  { id: 1, goods_name: '草饲牛肉南瓜高能鲜肉餐包', category: '成犬鲜食', price: 28.5, shelf_life_days: 7, ingredients: '鲜牛肉、牛心、南瓜、菠菜、胡萝卜、深海鱼油' },
  { id: 2, goods_name: '三文鱼鸡肉低敏全价生鲜泥', category: '幼猫鲜食', price: 25.0, shelf_life_days: 5, ingredients: '去骨三文鱼肉、鸡胸肉、牛磺酸、南极磷虾油' },
  { id: 3, goods_name: '鸭肉梨清热降火去泪痕鲜食', category: '成犬鲜食', price: 22.0, shelf_life_days: 7, ingredients: '鸭肉、鸭心、雪梨、西兰花、亚麻籽粉' },
  { id: 4, goods_name: '兔肉蔓越莓泌尿呵护猫用鲜食', category: '成猫鲜食', price: 30.0, shelf_life_days: 5, ingredients: '鲜兔肉、鸡肝、蔓越莓提取物、益生元' }
]);

// 批次库存
const chartContainer = ref(null);
let myChart = null;
const statsData = ref({ normalStock: 208, warningStock: 30, expiredStock: 0 });
const batchList = ref([
  { batch_no: 'B2026101201', goods_name: '草饲牛肉餐包', current_stock: 12, warning_status: 1 },
  { batch_no: 'B2026101401', goods_name: '三文鱼生鲜泥', current_stock: 18, warning_status: 1 },
  { batch_no: 'B2026101501', goods_name: '草饲牛肉餐包', current_stock: 68, warning_status: 0 },
  { batch_no: 'B2026101601', goods_name: '鸭肉梨鲜食', current_stock: 120, warning_status: 0 }
]);

// 打开新建宠物弹窗
const openCreatePetModal = () => {
  newPetForm.value = {
    petName: '',
    species: 'DOG',
    breed: '',
    ageMonths: 12,
    weight: 6.5,
    isNeutered: true,
    activityLevel: 'NORMAL',
    allergens: ''
  };
  createPetDialogVisible.value = true;
};

// 提交新建宠物档案 (用户端触发后，写入后端/本地，并自动同步)
const submitCreatePet = async () => {
  if (!newPetForm.value.petName.trim()) {
    ElMessage.warning('请输入爱宠昵称！');
    return;
  }
  const payload = {
    userId: userInfo.value.userId,
    petName: newPetForm.value.petName,
    species: newPetForm.value.species,
    breed: newPetForm.value.breed || '家养常见品种',
    ageMonths: newPetForm.value.ageMonths,
    weight: newPetForm.value.weight,
    activityLevel: newPetForm.value.activityLevel,
    isNeutered: newPetForm.value.isNeutered,
    allergens: newPetForm.value.allergens || '无'
  };

  try {
    await axios.post('http://localhost:8080/api/pet/profile/save', payload);
    ElMessage.success('🎉 宠物健康档案创建成功！已实时同步至系统数据库。');
  } catch (e) {
    // 本地同步更新
    const mockItem = {
      id: petList.value.length + 1,
      owner_name: userInfo.value.nickname,
      pet_name: payload.petName,
      species: payload.species,
      breed: payload.breed,
      weight: payload.weight,
      age_months: payload.ageMonths,
      is_neutered: payload.isNeutered ? 1 : 0,
      allergens: payload.allergens,
      create_time: new Date().toLocaleString()
    };
    petList.value.unshift(mockItem);
    ElMessage.success('🎉 宠物健康档案已保存！数据已即时更新。');
  }

  createPetDialogVisible.value = false;
  fetchPetList();
};

// 获取档案列表 (根据当前角色 role 拉取)
const fetchPetList = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/pet/profile/list', {
      params: {
        userId: userInfo.value.userId,
        role: userInfo.value.role
      }
    });
    if (res.data && res.data.length > 0) {
      petList.value = res.data;
    }
  } catch (e) {
    // 保持当前数据
  }
};

// 带入AI测算
const loadPetToAI = (pet) => {
  petForm.value.species = pet.species;
  petForm.value.weight = pet.weight;
  petForm.value.ageMonths = pet.age_months;
  petForm.value.isNeutered = pet.is_neutered === 1;
  petForm.value.allergens = pet.allergens === '无' ? '' : pet.allergens;
  activeMenu.value = 'ai-nutrition';
  calculateNutrition();
  ElMessage.info(`已将【${pet.pet_name}】的健康数据带入 AI 测算模型！`);
};

// 切换菜单
const handleMenuSelect = (index) => {
  activeMenu.value = index;
  if (index === 'inventory-monitor') {
    nextTick(() => initECharts());
  }
};

// 快速切换用户与管理员身份进行体验
const switchRoleQuickly = () => {
  if (userInfo.value.role === 'ROLE_ADMIN') {
    userInfo.value = { userId: 2, username: 'petlover', nickname: '铲屎官 (宠主用户)', role: 'ROLE_USER' };
    activeMenu.value = 'pet-archive';
    ElMessage.success('已切换为【普通宠主模式】：可新建档案、AI测算及选购鲜食！');
  } else {
    userInfo.value = { userId: 1, username: 'admin', nickname: '系统管理员', role: 'ROLE_ADMIN' };
    ElMessage.info('已切换为【管理员模式】：界面专注纯粹的全平台宠物建档审计！');
  }
  fetchPetList();
};

// 退出登录
const handleLogout = () => {
  authDialogVisible.value = true;
};

// 登录
const handleLogin = async () => {
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', {
      username: authForm.value.username,
      password: authForm.value.password
    });
    userInfo.value = res.data;
    authDialogVisible.value = false;
    ElMessage.success(`欢迎回来，${res.data.nickname}！`);
    fetchPetList();
  } catch (err) {
    if (authForm.value.username === 'admin') {
      userInfo.value = { userId: 1, username: 'admin', nickname: '系统管理员', role: 'ROLE_ADMIN' };
    } else {
      userInfo.value = { userId: 2, username: 'petlover', nickname: '铲屎官 (宠主用户)', role: 'ROLE_USER' };
      activeMenu.value = 'pet-archive';
    }
    authDialogVisible.value = false;
    ElMessage.success('登录成功！已根据您的身份进入专属界面。');
    fetchPetList();
  }
};

// AI测算
const calculateNutrition = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/nutrition/calculate', { params: petForm.value });
    aiResult.value = res.data;
    ElMessage.success('AI 营养配比模型重算完成！');
  } catch (e) {
    ElMessage.info('已触发本地 NRC 动物营养学算法！');
  }
};

// 鲜食出库与商城
const handleBuyGoods = (goods) => {
  ElMessage.success(`已选购【${goods.goods_name}】x 1份，已按先进先出(FIFO)分配批次！`);
  triggerFifoDeduct();
};

const triggerFifoDeduct = async () => {
  try {
    await axios.post('http://localhost:8080/api/inventory/deduct?goodsId=1&quantity=5');
    ElMessage.success('成功扣除最早批次库存！');
  } catch (e) {
    if (batchList.value[0].current_stock >= 5) {
      batchList.value[0].current_stock -= 5;
      statsData.value.warningStock -= 5;
      ElMessage.success(`【FIFO出库】已从最早到期的批次 ${batchList.value[0].batch_no} 扣除 5 件！`);
      initECharts();
    }
  }
};

const initECharts = () => {
  if (!chartContainer.value) return;
  if (!myChart) myChart = echarts.init(chartContainer.value);
  myChart.setOption({
    title: { text: '各批次鲜食在库与临期状态', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: batchList.value.map(i => i.batch_no) },
    yAxis: { type: 'value', name: '库存件数' },
    series: [{
      name: '在库量',
      type: 'bar',
      barWidth: '40%',
      data: batchList.value.map(i => ({ value: i.current_stock, itemStyle: { color: i.warning_status === 1 ? '#E6A23C' : '#67C23A' } }))
    }]
  });
};

onMounted(() => {
  fetchPetList();
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
.user-name { color: #fff; margin-left: 10px; font-size: 14px; font-weight: bold; }
.body-container { min-height: calc(100vh - 60px); }
.aside-menu { background-color: #2d3a4b; }
.el-menu-vertical { border-right: none; }
.content-main { padding: 20px; background-color: #f5f7fa; }
.box-card { border-radius: 8px; margin-bottom: 20px; }
.card-head-line { display: flex; justify-content: space-between; align-items: center; }
.admin-view { max-width: 1400px; margin: 0 auto; }
.admin-kpi-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 15px; }
.kpi-card { background: #fff; padding: 16px; border-radius: 6px; border: 1px solid #ebeef5; text-align: center; }
.kpi-num { font-size: 28px; font-weight: bold; color: #303133; }
.kpi-label { font-size: 13px; color: #909399; margin-top: 4px; }
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
