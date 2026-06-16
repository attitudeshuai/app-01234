<!--
  房屋健康情况页面
  功能：房屋健康检查记录的管理
-->
<template>
  <div class="health-list">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h2>房屋健康情况</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增检查记录
      </el-button>
    </div>

    <!-- 搜索过滤栏 -->
    <div class="search-bar">
      <!-- 检查类型筛选 -->
      <el-select
        v-model="query.checkType"
        placeholder="检查类型"
        clearable
        style="width: 160px"
        @change="loadData"
      >
        <el-option v-for="t in checkTypes" :key="t" :label="t" :value="t" />
      </el-select>

      <!-- 检查结果筛选 -->
      <el-select
        v-model="query.result"
        placeholder="检查结果"
        clearable
        style="width: 120px"
        @change="loadData"
      >
        <el-option v-for="r in results" :key="r" :label="r" :value="r" />
      </el-select>

      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="page-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="buildingName" label="楼宇" width="140" />
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="checkDate" label="检查日期" width="110" />
        <el-table-column prop="checkType" label="检查类型" width="110" />
        <el-table-column label="检查结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.result)" size="small">
              {{ row.result }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="问题描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="handler" label="处理人" width="90" />
        <el-table-column prop="nextCheckDate" label="下次检查" width="110" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑检查记录' : '新增检查记录'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 房间选择 -->
        <el-form-item label="所属房间" prop="roomId">
          <el-select
            v-model="form.roomId"
            placeholder="选择房间"
            filterable
            style="width: 100%"
          >
            <el-option-group v-for="b in buildingRooms" :key="b.id" :label="b.name">
              <el-option
                v-for="r in b.rooms"
                :key="r.id"
                :label="`${b.name} - ${r.roomNumber}`"
                :value="r.id"
              />
            </el-option-group>
          </el-select>
        </el-form-item>

        <!-- 检查日期与类型 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="检查日期" prop="checkDate">
              <el-date-picker
                v-model="form.checkDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择检查日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检查类型" prop="checkType">
              <el-select
                v-model="form.checkType"
                placeholder="请选择检查类型"
                style="width: 100%"
              >
                <el-option v-for="t in checkTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 检查结果与处理人 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="检查结果" prop="result">
              <el-select
                v-model="form.result"
                placeholder="请选择检查结果"
                style="width: 100%"
              >
                <el-option v-for="r in results" :key="r" :label="r" :value="r" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理人">
              <el-input v-model="form.handler" placeholder="请输入处理人" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 问题描述 -->
        <el-form-item label="问题描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入问题描述"
          />
        </el-form-item>

        <!-- 下次检查日期 -->
        <el-form-item label="下次检查">
          <el-date-picker
            v-model="form.nextCheckDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择下次检查日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 房屋健康情况管理
 * - 展示健康检查记录列表
 * - 支持新增和编辑检查记录
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHealthList, createHealth, updateHealth } from '@/api/health'
import { getAllBuildings } from '@/api/building'
import { getRoomsByBuilding } from '@/api/room'

// ==================== 状态定义 ====================

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitLoading = ref(false)
/** 表格数据 */
const tableData = ref([])
/** 数据总数 */
const total = ref(0)

/** 对话框显示状态 */
const dialogVisible = ref(false)
/** 当前编辑的记录ID（新增时为null） */
const editId = ref(null)
/** 表单引用 */
const formRef = ref()

/** 楼宇-房间列表（用于下拉选择） */
const buildingRooms = ref([])

/** 检查类型选项 */
const checkTypes = ['日常巡检', '专项检查', '安全检查']

/** 检查结果选项 */
const results = ['良好', '一般', '较差']

// ==================== 查询参数 ====================

const query = reactive({
  current: 1,
  size: 10,
  checkType: '',
  result: ''
})

// ==================== 表单数据 ====================

const form = reactive({
  roomId: null,
  checkDate: '',
  checkType: '',
  result: '',
  description: '',
  handler: '',
  nextCheckDate: ''
})

// ==================== 表单校验 ====================

const rules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  checkDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  checkType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  result: [{ required: true, message: '请选择结果', trigger: 'change' }]
}

// ==================== 工具函数 ====================

/**
 * 获取检查结果标签类型
 */
function getResultType(result) {
  const map = {
    '良好': 'success',
    '一般': 'warning',
    '较差': 'danger'
  }
  return map[result] || 'info'
}

// ==================== 数据加载 ====================

/**
 * 加载楼宇和房间数据（用于下拉选择）
 */
async function loadBuildingRooms() {
  const bldRes = await getAllBuildings()
  const list = []
  for (const b of bldRes.data) {
    const rRes = await getRoomsByBuilding(b.id)
    list.push({
      id: b.id,
      name: b.name,
      rooms: rRes.data
    })
  }
  buildingRooms.value = list
}

/**
 * 加载健康检查列表数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getHealthList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// ==================== 搜索相关 ====================

/**
 * 重置搜索条件
 */
function handleReset() {
  query.checkType = ''
  query.result = ''
  loadData()
}

// ==================== 对话框操作 ====================

/**
 * 打开新增/编辑对话框
 * @param {Object} row - 编辑时传入行数据，新增时不传
 */
function openDialog(row) {
  dialogVisible.value = true

  if (row) {
    // 编辑模式
    editId.value = row.id
    Object.assign(form, {
      roomId: row.roomId,
      checkDate: row.checkDate,
      checkType: row.checkType,
      result: row.result,
      description: row.description,
      handler: row.handler,
      nextCheckDate: row.nextCheckDate
    })
  } else {
    // 新增模式
    editId.value = null
    Object.assign(form, {
      roomId: null,
      checkDate: '',
      checkType: '',
      result: '',
      description: '',
      handler: '',
      nextCheckDate: ''
    })
  }
}

/**
 * 提交表单（新增或更新）
 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (editId.value) {
      await updateHealth(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createHealth(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadBuildingRooms()
  loadData()
})
</script>
