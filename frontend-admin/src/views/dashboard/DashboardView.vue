<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>数据概览</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6f7ff;">
            <el-icon :size="28" color="#1890ff"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashData.buildingCount || 0 }}</div>
            <div class="stat-label">楼宇总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f6ffed;">
            <el-icon :size="28" color="#52c41a"><House /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashData.roomCount || 0 }}</div>
            <div class="stat-label">房屋总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #fff7e6;">
            <el-icon :size="28" color="#faad14"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatMoney(dashData.monthlyRentIncome) }}</div>
            <div class="stat-label">月租金收入(元)</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #fff1f0;">
            <el-icon :size="28" color="#ff4d4f"><Bell /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ dashData.pendingApprovalCount || 0 }}</div>
            <div class="stat-label">待审批申请</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="page-card">
          <h3>房屋状态分布</h3>
          <div ref="statusChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card">
          <h3>房屋用途分布</h3>
          <div ref="purposeChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '@/api/statistics'

const dashData = ref({})
const statusChartRef = ref()
const purposeChartRef = ref()

function formatMoney(val) {
  if (!val) return '0'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 0 })
}

async function loadData() {
  try {
    const res = await getDashboard()
    dashData.value = res.data
    await nextTick()
    renderCharts()
  } catch (e) {
    // fallback
  }
}

function renderCharts() {
  // 房屋状态饼图
  const statusData = dashData.value.roomStatusCount || {}
  const statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    color: ['#52c41a', '#1890ff', '#faad14', '#909399'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      label: { show: true, formatter: '{b}\n{c}间' },
      data: Object.entries(statusData).map(([name, value]) => ({ name, value }))
    }]
  })

  // 用途分布柱状图
  const purposeData = dashData.value.purposeDistribution || {}
  const purposeChart = echarts.init(purposeChartRef.value)
  purposeChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: Object.keys(purposeData),
      axisLabel: { interval: 0 }
    },
    yAxis: { type: 'value', name: '数量(间)' },
    color: ['#1890ff'],
    series: [{
      type: 'bar',
      data: Object.values(purposeData),
      barWidth: '40%',
      itemStyle: { borderRadius: [4, 4, 0, 0] }
    }]
  })

  window.addEventListener('resize', () => {
    statusChart.resize()
    purposeChart.resize()
  })
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.stat-row {
  margin-bottom: $spacing-md;
}

.stat-card {
  background: $bg-white;
  border-radius: $radius-md;
  box-shadow: $shadow-base;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  transition: transform $transition-fast, box-shadow $transition-fast;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: $text-primary;
      line-height: 1.2;
    }

    .stat-label {
      font-size: $font-size-sm;
      color: $text-secondary;
      margin-top: 4px;
    }
  }
}

.page-card h3 {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
