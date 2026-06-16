<!--
  租赁合约管理页面
  功能：查看和新增租赁合约
-->
<template>
  <div class="contract-list">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h2>租赁合约管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增合约
      </el-button>
    </div>

    <!-- 搜索过滤栏 -->
    <div class="search-bar">
      <el-select
        v-model="selectedRentalId"
        placeholder="选择租赁记录"
        clearable
        filterable
        style="width: 300px"
        @change="loadContracts"
      >
        <el-option
          v-for="r in rentals"
          :key="r.id"
          :label="`#${r.id} ${r.tenantName} - ${r.roomNumber || ''}`"
          :value="r.id"
        />
      </el-select>
    </div>

    <!-- 数据表格 -->
    <div class="page-card">
      <el-table :data="contracts" v-loading="loading" stripe border>
        <el-table-column prop="contractNumber" label="合同编号" width="140" />
        <el-table-column prop="tenantName" label="租赁方" width="100" />
        <el-table-column prop="roomNumber" label="房间号" width="90" />
        <el-table-column prop="signDate" label="签约日期" width="110" />
        <el-table-column prop="startDate" label="合同起始" width="110" />
        <el-table-column prop="endDate" label="合同终止" width="110" />
        <el-table-column label="合同总额(元)" width="140" align="right">
          <template #default="{ row }">
            {{ Number(row.totalAmount).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="remark"
          label="备注"
          min-width="150"
          show-overflow-tooltip
        />
      </el-table>
    </div>

    <!-- 新增合约对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增合约"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 租赁记录选择 -->
        <el-form-item label="租赁记录" prop="rentalId">
          <el-select
            v-model="form.rentalId"
            placeholder="选择租赁记录"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="r in rentals"
              :key="r.id"
              :label="`#${r.id} ${r.tenantName}`"
              :value="r.id"
            />
          </el-select>
        </el-form-item>

        <!-- 合同编号 -->
        <el-form-item label="合同编号" prop="contractNumber">
          <el-input v-model="form.contractNumber" placeholder="如 CT-2024-001" />
        </el-form-item>

        <!-- 签约日期与合同总额 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="签约日期" prop="signDate">
              <el-date-picker
                v-model="form.signDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择签约日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同总额" prop="totalAmount">
              <el-input-number
                v-model="form.totalAmount"
                :min="0"
                :precision="2"
                placeholder="合同总额"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 合同日期范围 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="合同起始" prop="startDate">
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
            <el-form-item label="合同终止" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择终止日期"
                style="width: 100%"
              />
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
 * 租赁合约管理
 * - 展示合约列表（可按租赁记录筛选）
 * - 支持新增合约
 */
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRentalList, getContracts, getAllContracts, createContract } from '@/api/rental'

// ==================== 路由 ====================
const route = useRoute()

// ==================== 状态定义 ====================

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitLoading = ref(false)
/** 合约列表 */
const contracts = ref([])
/** 租赁记录列表（用于下拉选择） */
const rentals = ref([])
/** 当前选中的租赁记录ID */
const selectedRentalId = ref(null)

/** 对话框显示状态 */
const dialogVisible = ref(false)
/** 表单引用 */
const formRef = ref()

// ==================== 表单数据 ====================

const form = reactive({
  rentalId: null,
  contractNumber: '',
  signDate: '',
  startDate: '',
  endDate: '',
  totalAmount: 0,
  remark: ''
})

// ==================== 表单校验 ====================

/**
 * 合同终止日期验证器（必须晚于起始日期）
 */
const endDateValidator = (rule, value, callback) => {
  if (value && form.startDate && new Date(value) <= new Date(form.startDate)) {
    callback(new Error('合同终止日期必须晚于起始日期'))
  } else {
    callback()
  }
}

const rules = {
  rentalId: [{ required: true, message: '请选择租赁记录', trigger: 'change' }],
  contractNumber: [{ required: true, message: '请输入合同编号', trigger: 'blur' }],
  signDate: [{ required: true, message: '请选择签约日期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择起始日期', trigger: 'change' }],
  endDate: [
    { required: true, message: '请选择终止日期', trigger: 'change' },
    { validator: endDateValidator, trigger: 'change' }
  ],
  totalAmount: [{ required: true, message: '请输入合同总额', trigger: 'blur' }]
}

// ==================== 工具函数 ====================

/**
 * 获取状态标签类型
 */
function getStatusType(status) {
  const map = {
    '生效': 'success',
    '到期': 'warning',
    '终止': 'info'
  }
  return map[status] || 'info'
}

// ==================== 数据加载 ====================

/**
 * 加载租赁记录列表
 */
async function loadRentals() {
  const res = await getRentalList({ current: 1, size: 100 })
  rentals.value = res.data.records
}

/**
 * 加载合约列表
 * - 有选中租赁记录时：加载该记录的合约
 * - 无选中时：加载全部合约
 */
async function loadContracts() {
  loading.value = true
  try {
    if (selectedRentalId.value) {
      // 按租赁记录过滤
      const res = await getContracts(selectedRentalId.value)
      contracts.value = res.data
    } else {
      // 查询全量数据
      const res = await getAllContracts()
      contracts.value = res.data
    }
  } finally {
    loading.value = false
  }
}

// ==================== 对话框操作 ====================

/**
 * 打开新增对话框
 */
function openDialog() {
  dialogVisible.value = true
  Object.assign(form, {
    rentalId: selectedRentalId.value,
    contractNumber: '',
    signDate: '',
    startDate: '',
    endDate: '',
    totalAmount: 0,
    remark: ''
  })
}

/**
 * 提交表单
 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    await createContract(form.rentalId, form)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    loadContracts()
  } finally {
    submitLoading.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(async () => {
  // 加载租赁记录
  await loadRentals()

  // 从路由参数获取预选的租赁ID
  if (route.query.rentalId) {
    selectedRentalId.value = Number(route.query.rentalId)
  }

  // 加载合约数据
  loadContracts()
})
</script>
