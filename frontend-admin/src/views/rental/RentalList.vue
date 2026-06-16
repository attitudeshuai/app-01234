<!--
  对外出租管理页面
  功能：租赁记录的增删改查、终止租赁、查看合约
-->
<template>
  <div class="rental-list">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h2>对外出租管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增租赁
      </el-button>
    </div>

    <!-- 搜索过滤栏 -->
    <div class="search-bar">
      <el-input
        v-model="query.tenantName"
        placeholder="租赁方名称"
        clearable
        style="width: 200px"
      />
      <el-select
        v-model="query.status"
        placeholder="状态"
        clearable
        style="width: 120px"
        @change="loadData"
      >
        <el-option label="有效" value="有效" />
        <el-option label="到期" value="到期" />
        <el-option label="终止" value="终止" />
      </el-select>
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="page-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="buildingName" label="楼宇" width="130" />
        <el-table-column prop="roomNumber" label="房间号" width="90" />
        <el-table-column prop="tenantName" label="租赁方" width="100" />
        <el-table-column
          prop="tenantCompany"
          label="租赁公司"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column prop="tenantContact" label="联系方式" width="120" />
        <el-table-column prop="startDate" label="起始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column label="月租金(元)" width="120" align="right">
          <template #default="{ row }">
            {{ Number(row.rentAmount).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentCycle" label="支付周期" width="90" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">
              编辑
            </el-button>
            <el-button link type="info" size="small" @click="viewContracts(row)">
              合约
            </el-button>
            <el-popconfirm
              v-if="row.status === '有效'"
              title="确定终止该租赁？"
              @confirm="handleTerminate(row.id)"
            >
              <template #reference>
                <el-button link type="danger" size="small">终止</el-button>
              </template>
            </el-popconfirm>
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
      :title="editId ? '编辑租赁' : '新增租赁'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 房间选择 -->
        <el-form-item label="租赁房间" prop="roomId">
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

        <!-- 租赁方信息 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="租赁方" prop="tenantName">
              <el-input v-model="form.tenantName" placeholder="请输入租赁方名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="tenantContact">
              <el-input v-model="form.tenantContact" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="租赁公司">
          <el-input v-model="form.tenantCompany" placeholder="请输入租赁公司" />
        </el-form-item>

        <!-- 租赁日期 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="起始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择起始日期"
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

        <!-- 租金信息 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="月租金(元)" prop="rentAmount">
              <el-input-number
                v-model="form.rentAmount"
                :min="0"
                :precision="2"
                placeholder="月租金"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付周期">
              <el-select
                v-model="form.paymentCycle"
                placeholder="请选择支付周期"
                style="width: 100%"
              >
                <el-option label="月付" value="月付" />
                <el-option label="季付" value="季付" />
                <el-option label="半年付" value="半年付" />
                <el-option label="年付" value="年付" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
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
 * 对外出租管理
 * - 展示所有租赁记录
 * - 支持新增、编辑、终止租赁
 * - 支持跳转查看关联合约
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRentalList, createRental, updateRental, terminateRental } from '@/api/rental'
import { getAllBuildings } from '@/api/building'
import { getRoomsByBuilding } from '@/api/room'

// ==================== 路由 ====================
const router = useRouter()

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

// ==================== 查询参数 ====================

const query = reactive({
  current: 1,
  size: 10,
  tenantName: '',
  status: ''
})

// ==================== 表单数据 ====================

const form = reactive({
  roomId: null,
  tenantName: '',
  tenantContact: '',
  tenantCompany: '',
  startDate: '',
  endDate: '',
  rentAmount: 0,
  paymentCycle: '月付',
  remark: ''
})

// ==================== 表单校验 ====================

/**
 * 手机号验证器
 */
const phoneValidator = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的11位手机号'))
  } else {
    callback()
  }
}

/**
 * 结束日期验证器（必须晚于起始日期）
 */
const endDateValidator = (rule, value, callback) => {
  if (value && form.startDate && new Date(value) <= new Date(form.startDate)) {
    callback(new Error('结束日期必须晚于起始日期'))
  } else {
    callback()
  }
}

const rules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  tenantName: [{ required: true, message: '请输入租赁方', trigger: 'blur' }],
  tenantContact: [{ validator: phoneValidator, trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择起始日期', trigger: 'change' }],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' },
    { validator: endDateValidator, trigger: 'change' }
  ],
  rentAmount: [{ required: true, message: '请输入租金', trigger: 'blur' }]
}

// ==================== 工具函数 ====================

/**
 * 获取状态标签类型
 */
function getStatusType(status) {
  const map = {
    '有效': 'success',
    '到期': 'warning',
    '终止': 'info'
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
 * 加载租赁列表数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getRentalList(query)
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
  query.tenantName = ''
  query.status = ''
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
      tenantName: row.tenantName,
      tenantContact: row.tenantContact,
      tenantCompany: row.tenantCompany,
      startDate: row.startDate,
      endDate: row.endDate,
      rentAmount: row.rentAmount,
      paymentCycle: row.paymentCycle,
      remark: row.remark
    })
  } else {
    // 新增模式
    editId.value = null
    Object.assign(form, {
      roomId: null,
      tenantName: '',
      tenantContact: '',
      tenantCompany: '',
      startDate: '',
      endDate: '',
      rentAmount: 0,
      paymentCycle: '月付',
      remark: ''
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
      await updateRental(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createRental(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

// ==================== 操作按钮 ====================

/**
 * 终止租赁
 */
async function handleTerminate(id) {
  await terminateRental(id)
  ElMessage.success('已终止')
  loadData()
}

/**
 * 查看合约（跳转到合约页面）
 */
function viewContracts(row) {
  router.push({
    path: '/rental/contract',
    query: { rentalId: row.id }
  })
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadBuildingRooms()
  loadData()
})
</script>
