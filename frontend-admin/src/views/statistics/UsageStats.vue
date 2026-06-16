<template>
  <div class="usage-stats">
    <div class="page-header"><h2>使用情况统计</h2></div>

    <div class="search-bar">
      <el-select v-model="query.departmentId" placeholder="单位" clearable style="width: 160px" @change="loadData">
        <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-date-picker v-model="query.year" type="year" placeholder="选择年份" value-format="YYYY" style="width: 140px" @change="loadData" />
      <el-select v-model="query.month" placeholder="月份" clearable style="width: 120px" @change="loadData">
        <el-option v-for="m in 12" :key="m" :label="`${m}月`" :value="m" />
      </el-select>
      <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="6"><div class="stat-card"><div class="stat-value">{{ stats.totalRooms || 0 }}</div><div class="stat-label">房屋总数</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="stat-value" style="color: #1890ff;">{{ stats.inUseCount || 0 }}</div><div class="stat-label">使用中</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="stat-value" style="color: #faad14;">{{ stats.rentedCount || 0 }}</div><div class="stat-label">出租中</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="stat-value" style="color: #52c41a;">{{ stats.utilizationRate || 0 }}%</div><div class="stat-label">综合利用率</div></div></el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <div class="page-card chart-card">
          <h3>审批情况</h3>
          <div class="approval-stats">
            <div class="mini-stat">
              <span class="num">{{ stats.totalApprovals || 0 }}</span>
              <span class="label">总申请</span>
            </div>
            <div class="mini-stat">
              <span class="num" style="color: #52c41a;">{{ stats.approvedCount || 0 }}</span>
              <span class="label">已通过</span>
            </div>
            <div class="mini-stat">
              <span class="num" style="color: #ff4d4f;">{{ stats.rejectedCount || 0 }}</span>
              <span class="label">已驳回</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card chart-card">
          <h3>用途类型分布</h3>
          <div v-if="hasUsageData" ref="chartRef" style="height: 280px;"></div>
          <div v-else class="empty-chart">
            <el-empty description="暂无用途分布数据" :image-size="100" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getUsageStats } from '@/api/statistics'
import { getDepartments } from '@/api/building'

const stats = ref({}); const departments = ref([]); const chartRef = ref()
const query = reactive({ departmentId: null, year: String(new Date().getFullYear()), month: null })

// 检查是否有用途分布数据
const hasUsageData = computed(() => {
  const dist = stats.value.usageTypeDistribution || {}
  return Object.keys(dist).length > 0
})

async function loadData() {
  const params = { ...query, year: query.year ? Number(query.year) : null }
  const res = await getUsageStats(params)
  stats.value = res.data
  await nextTick()
  renderChart()
}

function renderChart() {
  if (!hasUsageData.value || !chartRef.value) return
  const dist = stats.value.usageTypeDistribution || {}
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    color: ['#1890ff', '#52c41a', '#faad14', '#ff4d4f', '#722ed1', '#909399'],
    series: [{ type: 'pie', radius: '65%', data: Object.entries(dist).map(([name, value]) => ({ name, value })), label: { formatter: '{b}: {c}' } }]
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(async () => { const res = await getDepartments(); departments.value = res.data; loadData() })
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
.page-card h3 { font-size: $font-size-md; font-weight: 600; margin-bottom: $spacing-md; }

/* 审批情况与用途类型卡片等高对齐 */
.chart-row {
  display: flex;
  :deep(.el-col) {
    display: flex;
  }
}
.chart-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.approval-stats {
  flex: 1;
  display: flex;
  justify-content: space-around;
  align-items: center;
  text-align: center;
  min-height: 280px;
}
.mini-stat {
  .num { display: block; font-size: 32px; font-weight: 700; margin-bottom: 8px; }
  .label { font-size: $font-size-sm; color: $text-secondary; }
}
.empty-chart {
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-lighter, #f5f7fa);
  border-radius: 4px;
}
</style>
