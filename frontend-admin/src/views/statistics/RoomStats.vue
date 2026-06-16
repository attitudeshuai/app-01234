<template>
  <div class="room-stats">
    <div class="page-header"><h2>房屋信息统计</h2></div>

    <div class="search-bar">
      <el-select v-model="query.departmentId" placeholder="单位" clearable style="width: 160px" @change="loadData">
        <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-select v-model="query.buildingId" placeholder="楼宇" clearable style="width: 160px" @change="loadData">
        <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
      </el-select>
      <el-select v-model="query.purpose" placeholder="用途" clearable style="width: 120px" @change="loadData">
        <el-option v-for="p in ['办公','宿舍','公寓','教学楼','病房','其他']" :key="p" :label="p" :value="p" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
        <el-option v-for="s in ['空闲','使用中','出租','封存']" :key="s" :label="s" :value="s" />
      </el-select>
      <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
    </div>

    <!-- 统计概要 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="8"><div class="stat-card"><div class="stat-value">{{ stats.totalCount || 0 }}</div><div class="stat-label">房屋总数(间)</div></div></el-col>
      <el-col :span="8"><div class="stat-card"><div class="stat-value">{{ Number(stats.totalArea || 0).toLocaleString() }}</div><div class="stat-label">总面积(㎡)</div></div></el-col>
      <el-col :span="8"><div class="stat-card"><div class="stat-value" style="color: #52c41a;">{{ Object.keys(stats.statusDistribution || {}).length }}</div><div class="stat-label">状态类别</div></div></el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <div class="page-card"><h3>状态分布</h3><div ref="statusChartRef" style="height: 300px;"></div></div>
      </el-col>
      <el-col :span="12">
        <div class="page-card"><h3>用途分布</h3><div ref="purposeChartRef" style="height: 300px;"></div></div>
      </el-col>
    </el-row>

    <div class="page-card" style="margin-top: 16px;">
      <h3>房屋明细</h3>
      <el-table :data="stats.rooms || []" stripe border max-height="400" style="width: 100%;">
        <el-table-column prop="roomNumber" label="房间号" min-width="120" />
        <el-table-column prop="buildingName" label="所属楼宇" min-width="150" />
        <el-table-column prop="departmentName" label="所属单位" min-width="120" />
        <el-table-column prop="floor" label="楼层" width="80" align="center" />
        <el-table-column prop="area" label="面积(㎡)" width="100" align="right" />
        <el-table-column prop="purpose" label="用途" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="row.status === '空闲' ? 'success' : row.status === '使用中' ? 'primary' : row.status === '出租' ? 'warning' : 'info'" size="small">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getRoomStats } from '@/api/statistics'
import { getAllBuildings, getDepartments } from '@/api/building'

const stats = ref({}); const departments = ref([]); const buildings = ref([])
const statusChartRef = ref(); const purposeChartRef = ref()
const query = reactive({ departmentId: null, buildingId: null, purpose: '', status: '' })

async function loadData() {
  const res = await getRoomStats(query)
  stats.value = res.data
  await nextTick()
  renderCharts()
}

function renderCharts() {
  const sd = stats.value.statusDistribution || {}
  const sc = echarts.init(statusChartRef.value)
  sc.setOption({ tooltip: { trigger: 'item' }, color: ['#52c41a', '#1890ff', '#faad14', '#909399'], series: [{ type: 'pie', radius: ['40%', '70%'], data: Object.entries(sd).map(([name, value]) => ({ name, value })) }] })

  const pd = stats.value.purposeDistribution || {}
  const pc = echarts.init(purposeChartRef.value)
  pc.setOption({ tooltip: { trigger: 'axis' }, xAxis: { type: 'category', data: Object.keys(pd) }, yAxis: { type: 'value' }, color: ['#1890ff'], series: [{ type: 'bar', data: Object.values(pd), barWidth: '50%', itemStyle: { borderRadius: [4, 4, 0, 0] } }] })

  window.addEventListener('resize', () => { sc.resize(); pc.resize() })
}

onMounted(async () => {
  const [dRes, bRes] = await Promise.all([getDepartments(), getAllBuildings()])
  departments.value = dRes.data; buildings.value = bRes.data
  loadData()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
.page-card h3 { font-size: $font-size-md; font-weight: 600; margin-bottom: $spacing-md; }
</style>
