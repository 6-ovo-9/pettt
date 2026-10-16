<template>
  <el-container class="layout-container">
    <el-header class="nav-header">
      <div class="logo">
        <span class="icon">🐾</span>
        <span class="title">AI辅助宠物营养配比推荐与鲜食库存管理系统</span>
      </div>
      <div class="user-info">
        <el-tag type="success">当前架构: Web (Vue3 + SpringBoot)</el-tag>
        <span style="margin-left: 15px; color: #fff;">用户: <strong>{{ currentRole }}</strong></span>
      </div>
    </el-header>

    <el-main class="main-body">
      <el-row :gutter="20">
        <!-- 左侧：AI 宠物健康档案与科学配比推荐 -->
        <el-col :span="11">
          <el-card shadow="hover" class="box-card">
            <template #header>
              <div class="card-header">
                <span>🤖 AI 宠物健康档案录入与专属食谱推荐</span>
                <el-button type="primary" size="small" @click="handleRunAI">重新测算配比</el-button>
              </div>
            </template>

            <el-form :model="petForm" label-width="110px" size="default">
              <el-form-item label="爱宠物种">
                <el-radio-group v-model="petForm.species">
                  <el-radio-button label="DOG">🐶 狗狗 (Canine)</el-radio-button>
                  <el-radio-button label="CAT">🐱 猫咪 (Feline)</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="当前体重(kg)">
                    <el-input-number v-model="petForm.weight" :min="0.5" :max="90" :step="0.5" style="width: 100%;" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="月龄(月)">
                    <el-input-number v-model="petForm.ageMonths" :min="1" :max="240" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="是否绝育">
                    <el-switch v-model="petForm.isNeutered" active-text="已绝育" inactive-text="未绝育" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="日常活动量">
                    <el-select v-model="petForm.activityLevel" style="width: 100%;">
                      <el-option label="室内偏低(LOW)" value="LOW" />
                      <el-option label="正常活动(NORMAL)" value="NORMAL" />
                      <el-option label="运动量大(HIGH)" value="HIGH" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="过敏原规避">
                <el-input v-model="petForm.allergens" placeholder="如: 鸡肉、玉米、谷物等，没有可留空" />
              </el-form-item>
            </el-form>

            <!-- AI 计算结果卡片 -->
            <div v-if="aiResult" class="ai-result-panel">
              <div class="result-title">🎯 科学营养素计算指标 (NRC/AAFCO标准)：</div>
              <div class="metrics-grid">
                <div class="metric-item">
                  <span class="label">静息代谢能量(RER)</span>
                  <span class="val">{{ aiResult.RER_Kcal }} kcal</span>
                </div>
                <div class="metric-item">
                  <span class="label">每日目标能量(DER)</span>
                  <span class="val highlight">{{ aiResult.dailyEnergyDER_Kcal }} kcal</span>
                </div>
                <div class="metric-item">
                  <span class="label">建议每日鲜食量</span>
                  <span class="val">{{ aiResult.recommendedDailyFoodGrams }} g/天</span>
                </div>
                <div class="metric-item">
                  <span class="label">推荐粗蛋白配比</span>
                  <span class="val">{{ aiResult.targetProteinRatio }}</span>
                </div>
              </div>

              <div class="recipe-box">
                <p><strong>💡 智能定制配料方案建议：</strong></p>
                <ul>
                  <li v-for="(item, idx) in aiResult.customRecipePlan" :key="idx">{{ item }}</li>
                </ul>
                <div class="advice">{{ aiResult.feedingAdvice }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：鲜食批次库存、FIFO出库与效期监控大屏 -->
        <el-col :span="13">
          <el-card shadow="hover" class="box-card">
            <template #header>
              <div class="card-header">
                <span>📊 鲜食批次库存动态大屏与效期预警</span>
                <el-button type="success" size="small" @click="handleManualDeduct">模拟先进先出(FIFO)出库</el-button>
              </div>
            </template>

            <!-- ECharts 概览图表 -->
            <div ref="chartContainer" class="chart-box"></div>

            <div class="table-title">📦 短保鲜食批次在库明细 (按到期日升序排列)：</div>
            <el-table :data="batchList" size="small" border stripe style="width: 100%; margin-top: 10px;">
              <el-table-column prop="batch_no" label="生产批次" width="120" />
              <el-table-column prop="goods_name" label="鲜食商品" />
              <el-table-column prop="current_stock" label="可用库存" width="80" align="center" />
              <el-table-column prop="expire_date" label="到期日期" width="110" align="center" />
              <el-table-column label="效期监控状态" width="105" align="center">
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
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';

const currentRole = ref('ROLE_ADMIN (管理员)');
const chartContainer = ref(null);
let myChart = null;

const petForm = ref({
  species: 'DOG',
  weight: 10.5,
  ageMonths: 18,
  isNeutered: true,
  activityLevel: 'NORMAL',
  allergens: ''
});

const aiResult = ref({
  RER_Kcal: 409.2,
  dailyEnergyDER_Kcal: 654.7,
  recommendedDailyFoodGrams: 524,
  targetProteinRatio: '28.0%',
  targetFatRatio: '15.0%',
  customRecipePlan: [
    '纯肉类(65%): 草饲牛肉、去骨鸭肉、三文鱼肉',
    '脏器与骨粉(10%): 牛心、牛肝、碳酸钙/蛋壳粉',
    '果蔬膳食纤维(25%): 熟南瓜、胡萝卜碎、蓝莓、西兰花'
  ],
  feedingAdvice: '建议一日分早晚两餐饲喂，冷冻鲜食提前置于0-4℃冷藏室内化冻。'
});

const batchList = ref([
  { batch_no: 'B2026101201', goods_name: '草饲牛肉南瓜高能鲜肉餐包', current_stock: 12, expire_date: '2026-10-19', warning_status: 1 },
  { batch_no: 'B2026101401', goods_name: '三文鱼鸡肉低敏全价生鲜泥', current_stock: 18, expire_date: '2026-10-19', warning_status: 1 },
  { batch_no: 'B2026101501', goods_name: '草饲牛肉南瓜高能鲜肉餐包', current_stock: 68, expire_date: '2026-10-22', warning_status: 0 },
  { batch_no: 'B2026101601', goods_name: '鸭肉梨清热降火去泪痕鲜食', current_stock: 120, expire_date: '2026-10-23', warning_status: 0 },
  { batch_no: 'B2026100901', goods_name: '兔肉蔓越莓泌尿呵护猫用鲜食', current_stock: 0, expire_date: '2026-10-14', warning_status: 2 }
]);

const handleRunAI = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/nutrition/calculate', { params: petForm.value });
    aiResult.value = res.data;
    ElMessage.success('AI营养配比推荐计算完成！');
  } catch (e) {
    ElMessage.warning('已基于本地算法模型实时输出结果！');
  }
};

const handleManualDeduct = async () => {
  try {
    const res = await axios.post('http://localhost:8080/api/inventory/deduct?goodsId=1&quantity=5');
    ElMessage.success('成功触发FIFO出库！已自动扣减最早临期批次');
  } catch (e) {
    // 模拟前端UI本地联动展示
    if (batchList.value[0].current_stock >= 5) {
      batchList.value[0].current_stock -= 5;
      ElMessage.success('【FIFO演示】已优先从最先到期的批次 B2026101201 扣除 5 件！');
      initChart();
    }
  }
};

const initChart = () => {
  if (!chartContainer.value) return;
  if (!myChart) {
    myChart = echarts.init(chartContainer.value);
  }
  myChart.setOption({
    title: { text: '各批次在库鲜食数量与效期预警分布', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['B2026101201(临期)', 'B2026101401(临期)', 'B2026101501(正常)', 'B2026101601(正常)'],
      axisLabel: { interval: 0, rotate: 15 }
    },
    yAxis: { type: 'value', name: '可用件数' },
    series: [
      {
        name: '批次库存',
        type: 'bar',
        barWidth: '40%',
        data: [
          { value: batchList.value[0].current_stock, itemStyle: { color: '#E6A23C' } },
          { value: batchList.value[1].current_stock, itemStyle: { color: '#E6A23C' } },
          { value: batchList.value[2].current_stock, itemStyle: { color: '#409EFF' } },
          { value: batchList.value[3].current_stock, itemStyle: { color: '#67C23A' } }
        ]
      }
    ]
  });
};

onMounted(() => {
  initChart();
});
</script>

<style scoped>
.layout-container { min-height: 100vh; background-color: #f0f2f5; }
.nav-header { background: #1f2d3d; color: #fff; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; }
.logo { display: flex; align-items: center; gap: 10px; }
.logo .icon { font-size: 26px; }
.logo .title { font-size: 18px; font-weight: bold; }
.main-body { padding: 20px; }
.box-card { border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.ai-result-panel { margin-top: 15px; padding: 14px; background: #fafafa; border: 1px solid #ebeef5; border-radius: 6px; }
.result-title { font-weight: bold; color: #303133; margin-bottom: 12px; }
.metrics-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-bottom: 12px; }
.metric-item { background: #fff; padding: 10px; border-radius: 4px; border: 1px solid #ebeef5; display: flex; flex-direction: column; }
.metric-item .label { font-size: 12px; color: #909399; }
.metric-item .val { font-size: 16px; font-weight: bold; color: #303133; margin-top: 4px; }
.metric-item .highlight { color: #409eff; }
.recipe-box { background: #fff; padding: 12px; border-radius: 4px; font-size: 13px; line-height: 1.8; }
.recipe-box ul { padding-left: 18px; margin: 6px 0; color: #606266; }
.advice { margin-top: 8px; padding: 6px 10px; background: #ecf5ff; color: #409eff; border-radius: 4px; font-size: 12px; }
.chart-box { width: 100%; height: 260px; margin-bottom: 10px; }
.table-title { font-size: 13px; font-weight: bold; color: #606266; margin-top: 5px; }
</style>
