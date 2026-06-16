<template>
  <div class="rental-stats">
    <div class="page-header"><h2>租赁收益统计</h2></div>

    <div class="search-bar">
      <el-date-picker v-model="query.year" type="year" placeholder="选择年份" value-format="YYYY" style="width: 140px" @change="loadData" />
      <el-select v-model="query.month" placeholder="月份" clearable style="width: 120px" @change="loadData">
        <el-option v-for="m in 12" :key="m" :label="`${m}月`" :value="m" />
      </el-select>
      <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="8"><div class="stat-card"><div class="stat-value">{{ stats.activeRentalCount || 0 }}</div><div class="stat-label">有效租赁数</div></div></el-col>
      <el-col :span="8"><div class="stat-card"><div class="stat-value" style="color: #faad14;">{{ formatMoney(stats.totalRentAmount) }}</div><div class="stat-label">月总租金(元)</div></div></el-col>
      <el-col :span="8"><div class="stat-card"><div class="stat-value" style="color: #52c41a;">{{ formatMoney((stats.totalRentAmount || 0) * 12) }}</div><div class="stat-label">预估年收入(元)</div></div></el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="14">
        <div class="page-card">
          <h3>月度租金收入趋势</h3>
          <div v-if="hasMonthlyData" ref="lineChartRef" style="height: 320px;"></div>
          <div v-else class="chart-empty">
            <el-empty description="暂无月度收入数据" :image-size="100" />
          </div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="page-card"><h3>租赁状态分布</h3><div ref="pieChartRef" style="height: 320px;"></div></div>
      </el-col>
    </el-row>

    <div class="page-card" style="margin-top: 16px;">
      <h3>各单位租赁情况</h3>
      <el-table :data="stats.departmentStats || []" stripe border>
        <el-table-column prop="departmentName" label="单位" min-width="150" />
        <el-table-column prop="count" label="租赁数量" width="120" align="center" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { getRentalStats } from '@/api/statistics'

const stats = ref({}); const lineChartRef = ref(); const pieChartRef = ref(); const lineChartInstance = ref(null)
const query = reactive({ year: String(new Date().getFullYear()), month: null })

const hasMonthlyData = computed(() => {
  const monthly = stats.value.monthlyIncome || []
  return monthly.length > 0
})

function formatMoney(val) { return val ? Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 0 }) : '0' }

async function loadData() {
  const params = {
    year: query.year ? Number(query.year) : null,
    month: query.month || null
  }
  const res = await getRentalStats(params)
  stats.value = res.data
  await nextTick()
  renderCharts()
}

function renderCharts() {
  // 月度趋势：无数据时占位，有数据时渲染折线图
  const monthly = stats.value.monthlyIncome || []
  if (monthly.length === 0) {
    if (lineChartInstance.value) {
      lineChartInstance.value.dispose()
      lineChartInstance.value = null
    }
  } else if (lineChartRef.value) {
    if (!lineChartInstance.value) lineChartInstance.value = echarts.init(lineChartRef.value)
    lineChartInstance.value.setOption({
      tooltip: { trigger: 'axis' }, grid: { left: 60, right: 20, bottom: 30 },
      xAxis: { type: 'category', data: monthly.map(m => m.month) },
      yAxis: { type: 'value', name: '金额(元)' },
      color: ['#1890ff'],
      series: [{ type: 'line', data: monthly.map(m => m.amount), smooth: true, areaStyle: { opacity: 0.15 }, lineStyle: { width: 2 } }]
    })
  }

  // 状态饼图 - 使用高对比度颜色
  const sd = stats.value.statusDistribution || {}
  const pc = echarts.init(pieChartRef.value)
  // 为不同状态分配明显区分的颜色
  const statusColors = {
    '有效': '#10b981',    // 绿色
    '到期': '#f59e0b',    // 橙色
    '终止': '#ef4444'     // 红色
  }
  const pieData = Object.entries(sd).map(([name, value]) => ({
    name,
    value,
    itemStyle: { color: statusColors[name] || '#6b7280' }
  }))
  pc.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 10, left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: pieData,
      label: { show: true, formatter: '{b}\n{d}%' },
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })

  window.addEventListener('resize', () => {
    if (lineChartInstance.value) lineChartInstance.value.resize()
    pc.resize()
  })
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
.page-card h3 { font-size: $font-size-md; font-weight: 600; margin-bottom: $spacing-md; }
.chart-empty {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-lighter, #f5f7fa);
  border-radius: 4px;
}
</style>
