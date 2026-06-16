<!--
  资产配置管理页面
  功能：房屋资产的增删改查
-->
<template>
  <div class="asset-list">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h2>资产配置管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增资产
      </el-button>
    </div>

    <!-- 搜索过滤栏 -->
    <div class="search-bar">
      <!-- 房间筛选 -->
      <el-select
        v-model="query.roomId"
        placeholder="选择房间"
        clearable
        filterable
        style="width: 200px"
        @change="loadData"
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

      <!-- 资产类型筛选 -->
      <el-select
        v-model="query.assetType"
        placeholder="资产类型"
        clearable
        style="width: 140px"
        @change="loadData"
      >
        <el-option v-for="t in assetTypes" :key="t" :label="t" :value="t" />
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
        <el-table-column prop="assetName" label="资产名称" min-width="150" />
        <el-table-column prop="assetType" label="类型" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="购入日期" width="110" />
        <el-table-column
          prop="remark"
          label="备注"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">
              编辑
            </el-button>
            <el-popconfirm title="确定删除该资产？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
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
      :title="editId ? '编辑资产' : '新增资产'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
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

        <!-- 资产名称 -->
        <el-form-item label="资产名称" prop="assetName">
          <el-input v-model="form.assetName" placeholder="请输入资产名称" />
        </el-form-item>

        <!-- 类型与数量 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型">
              <el-select
                v-model="form.assetType"
                placeholder="请选择资产类型"
                style="width: 100%"
              >
                <el-option v-for="t in assetTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量">
              <el-input-number
                v-model="form.quantity"
                :min="1"
                placeholder="数量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 状态与购入日期 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select
                v-model="form.status"
                placeholder="请选择状态"
                style="width: 100%"
              >
                <el-option label="正常" value="正常" />
                <el-option label="损坏" value="损坏" />
                <el-option label="报废" value="报废" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购入日期">
              <el-date-picker
                v-model="form.purchaseDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择购入日期"
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
 * 资产配置管理
 * - 展示资产列表
 * - 支持资产的增删改
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAssetList, createAsset, updateAsset, deleteAsset } from '@/api/asset'
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

/** 资产类型选项 */
const assetTypes = ['家具', '电器', '办公设备', '其他']

// ==================== 查询参数 ====================

const query = reactive({
  current: 1,
  size: 10,
  roomId: null,
  assetType: ''
})

// ==================== 表单数据 ====================

const form = reactive({
  roomId: null,
  assetName: '',
  assetType: '家具',
  quantity: 1,
  status: '正常',
  purchaseDate: '',
  remark: ''
})

// ==================== 表单校验 ====================

const rules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  assetName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

// ==================== 工具函数 ====================

/**
 * 获取状态标签类型
 */
function getStatusType(status) {
  const map = {
    '正常': 'success',
    '损坏': 'warning',
    '报废': 'danger'
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
 * 加载资产列表数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getAssetList(query)
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
  query.roomId = null
  query.assetType = ''
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
      assetName: row.assetName,
      assetType: row.assetType,
      quantity: row.quantity,
      status: row.status,
      purchaseDate: row.purchaseDate,
      remark: row.remark
    })
  } else {
    // 新增模式
    editId.value = null
    Object.assign(form, {
      roomId: null,
      assetName: '',
      assetType: '家具',
      quantity: 1,
      status: '正常',
      purchaseDate: '',
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
      await updateAsset(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createAsset(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

// ==================== 删除操作 ====================

/**
 * 删除资产
 */
async function handleDelete(id) {
  await deleteAsset(id)
  ElMessage.success('删除成功')
  loadData()
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadBuildingRooms()
  loadData()
})
</script>
