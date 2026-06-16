<!--
  内部使用审批页面
  功能：提交房屋使用申请、审批管理
-->
<template>
  <div class="approval-list">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h2>内部使用审批</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>提交申请
      </el-button>
    </div>

    <!-- 搜索过滤栏 -->
    <div class="search-bar">
      <el-select
        v-model="query.status"
        placeholder="审批状态"
        clearable
        style="width: 140px"
        @change="loadData"
      >
        <el-option label="待审批" value="待审批" />
        <el-option label="已通过" value="已通过" />
        <el-option label="已驳回" value="已驳回" />
      </el-select>

      <el-select
        v-model="query.usageType"
        placeholder="用途类型"
        clearable
        style="width: 140px"
        @change="loadData"
      >
        <el-option v-for="t in usageTypes" :key="t" :label="t" :value="t" />
      </el-select>

      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="page-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="buildingName" label="楼宇" width="130" />
        <el-table-column prop="roomNumber" label="房间号" width="90" />
        <el-table-column prop="departmentName" label="申请单位" width="110" />
        <el-table-column prop="applicant" label="申请人" width="80" />
        <el-table-column prop="usageType" label="用途类型" width="90" />
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column
          prop="reason"
          label="申请原因"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approver" label="审批人" width="80" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '待审批'">
              <el-popconfirm title="确定审批通过？" @confirm="handleApprove(row.id)">
                <template #reference>
                  <el-button link type="success" size="small">通过</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm title="确定驳回此申请？" @confirm="handleReject(row.id)">
                <template #reference>
                  <el-button link type="danger" size="small">驳回</el-button>
                </template>
              </el-popconfirm>
            </template>
            <el-tag v-else size="small" type="info">已处理</el-tag>
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

    <!-- 提交申请对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="提交使用申请"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 房间选择 -->
        <el-form-item label="申请房间" prop="roomId">
          <el-select
            v-model="form.roomId"
            placeholder="选择空闲房间"
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

        <!-- 申请单位与申请人 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="申请单位" prop="departmentId">
              <el-select
                v-model="form.departmentId"
                placeholder="请选择申请单位"
                style="width: 100%"
              >
                <el-option
                  v-for="d in departments"
                  :key="d.id"
                  :label="d.name"
                  :value="d.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请人" prop="applicant">
              <el-input v-model="form.applicant" placeholder="请输入申请人" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 用途类型 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用途类型" prop="usageType">
              <el-select
                v-model="form.usageType"
                placeholder="请选择用途类型"
                style="width: 100%"
              >
                <el-option v-for="t in usageTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 使用日期范围 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择开始日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择结束日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 申请原因 -->
        <el-form-item label="申请原因">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入申请原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 内部使用审批管理
 * - 展示审批列表
 * - 支持提交使用申请
 * - 支持审批通过/驳回
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getApprovalList, createApproval, approveApproval, rejectApproval } from '@/api/approval'
import { getAllBuildings, getDepartments } from '@/api/building'
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
/** 表单引用 */
const formRef = ref()

/** 楼宇-房间列表（用于下拉选择） */
const buildingRooms = ref([])
/** 单位列表 */
const departments = ref([])

/** 用途类型选项 */
const usageTypes = ['办公', '宿舍', '公寓', '教学', '病房', '其他']

// ==================== 查询参数 ====================

const query = reactive({
  current: 1,
  size: 10,
  status: '',
  usageType: ''
})

// ==================== 表单数据 ====================

const form = reactive({
  roomId: null,
  departmentId: null,
  applicant: '',
  usageType: '',
  startDate: '',
  endDate: '',
  reason: ''
})

// ==================== 表单校验 ====================

/**
 * 结束日期验证器（必须晚于开始日期）
 */
const endDateValidator = (rule, value, callback) => {
  if (value && form.startDate && new Date(value) <= new Date(form.startDate)) {
    callback(new Error('结束日期必须晚于开始日期'))
  } else {
    callback()
  }
}

const rules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择单位', trigger: 'change' }],
  applicant: [{ required: true, message: '请输入申请人', trigger: 'blur' }],
  usageType: [{ required: true, message: '请选择用途', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ validator: endDateValidator, trigger: 'change' }]
}

// ==================== 工具函数 ====================

/**
 * 获取状态标签类型
 */
function getStatusType(status) {
  const map = {
    '已通过': 'success',
    '已驳回': 'danger',
    '待审批': 'warning'
  }
  return map[status] || 'info'
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
 * 加载审批列表数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getApprovalList(query)
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
  query.status = ''
  query.usageType = ''
  loadData()
}

// ==================== 对话框操作 ====================

/**
 * 打开申请对话框
 */
function openDialog() {
  dialogVisible.value = true
  Object.assign(form, {
    roomId: null,
    departmentId: null,
    applicant: '',
    usageType: '',
    startDate: '',
    endDate: '',
    reason: ''
  })
}

/**
 * 提交申请
 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    await createApproval(form)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

// ==================== 审批操作 ====================

/**
 * 审批通过
 */
async function handleApprove(id) {
  await approveApproval(id, {})
  ElMessage.success('审批通过')
  loadData()
}

/**
 * 审批驳回
 */
async function handleReject(id) {
  await rejectApproval(id, {})
  ElMessage.success('已驳回')
  loadData()
}

// ==================== 生命周期 ====================

onMounted(async () => {
  // 加载单位列表
  const deptRes = await getDepartments()
  departments.value = deptRes.data

  // 加载楼宇房间数据
  loadBuildingRooms()

  // 加载审批列表
  loadData()
})
</script>
