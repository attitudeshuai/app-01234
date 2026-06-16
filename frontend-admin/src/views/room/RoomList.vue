<template>
  <div class="room-list">
    <div class="page-header">
      <h2>房屋管理</h2>
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>房屋登记</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select v-model="query.departmentId" placeholder="所属单位" clearable style="width: 160px" @change="loadData">
        <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-select v-model="query.buildingId" placeholder="所属楼宇" clearable style="width: 160px" @change="loadData">
        <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
      </el-select>
      <el-input v-model="query.roomNumber" placeholder="房间号" clearable style="width: 140px" />
      <el-select v-model="query.purpose" placeholder="用途" clearable style="width: 120px" @change="loadData">
        <el-option v-for="p in purposes" :key="p" :label="p" :value="p" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
        <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
      </el-select>
      <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="page-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="roomNumber" label="房间号" width="120" />
        <el-table-column prop="buildingName" label="所属楼宇" width="140" />
        <el-table-column prop="departmentName" label="所属单位" width="120" />
        <el-table-column prop="floor" label="楼层" width="70" align="center" />
        <el-table-column prop="area" label="面积(㎡)" width="100" align="right" />
        <el-table-column label="用途" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.purpose || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-actions" style="justify-content: center;">
              <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
              <el-dropdown trigger="click" @command="(cmd) => handleStatusChange(row, cmd)">
                <el-button link type="warning" size="small">变更状态</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="s in statuses" :key="s" :command="s" :disabled="s === row.status">{{ s }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-popconfirm title="确定要删除该房屋吗？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.current" v-model:page-size="query.size"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData" @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑房屋' : '房屋登记'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属楼宇" prop="buildingId">
              <el-select v-model="form.buildingId" placeholder="请选择" style="width: 100%">
                <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属单位" prop="departmentId">
              <el-select v-model="form.departmentId" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="房间号" prop="roomNumber">
              <el-input v-model="form.roomNumber" placeholder="如 101" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="楼层">
              <el-input-number v-model="form.floor" :min="-5" :max="200" placeholder="楼层" class="full-width-input-number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积(㎡)">
              <el-input-number v-model="form.area" :min="0" :precision="2" placeholder="面积" class="full-width-input-number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用途">
              <el-select v-model="form.purpose" placeholder="请选择" style="width: 100%">
                <el-option v-for="p in purposes" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" placeholder="请选择" style="width: 100%">
                <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 房屋管理页面
 *
 * 功能说明：
 * - 房屋列表的分页查询、多条件筛选
 * - 房屋登记（新增）/编辑
 * - 变更房屋状态（空闲/使用中/出租/封存）
 * - 删除房屋
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoomList, createRoom, updateRoom, updateRoomStatus, deleteRoom } from '@/api/room'
import { getAllBuildings, getDepartments } from '@/api/building'

// ==================== 响应式状态 ====================
const loading = ref(false)           // 表格加载状态
const submitLoading = ref(false)     // 表单提交加载状态
const tableData = ref([])            // 表格数据
const total = ref(0)                 // 数据总条数
const departments = ref([])          // 单位下拉选项
const buildings = ref([])            // 楼宇下拉选项
const dialogVisible = ref(false)     // 弹窗显示状态
const editId = ref(null)             // 编辑时的房屋ID，新增时为null
const formRef = ref()                // 表单引用，用于校验

// 用途和状态选项
const purposes = ['办公', '宿舍', '公寓', '教学楼', '病房', '其他']
const statuses = ['空闲', '使用中', '出租', '封存']

// 查询参数
const query = reactive({ current: 1, size: 10, buildingId: null, departmentId: null, roomNumber: '', purpose: '', status: '' })

// 表单数据
const form = reactive({ buildingId: null, departmentId: null, roomNumber: '', floor: 1, area: 0, purpose: '', status: '空闲', remark: '' })

// 表单校验规则
const rules = {
  buildingId: [{ required: true, message: '请选择楼宇', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择单位', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }]
}

// ==================== 工具方法 ====================

/** 根据状态返回对应的标签类型 */
function statusType(status) {
  const map = { '空闲': 'success', '使用中': 'primary', '出租': 'warning', '封存': 'info' }
  return map[status] || 'info'
}

// ==================== 数据加载方法 ====================

/** 加载房屋列表数据 */
async function loadData() {
  loading.value = true
  try {
    const res = await getRoomList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

/** 重置查询条件并刷新列表 */
function resetQuery() {
  Object.assign(query, { current: 1, buildingId: null, departmentId: null, roomNumber: '', purpose: '', status: '' })
  loadData()
}

// ==================== 弹窗与表单操作 ====================

/**
 * 打开新增/编辑弹窗
 * @param row 编辑时传入行数据，新增时不传
 */
function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    // 编辑模式：填充表单数据
    editId.value = row.id
    Object.assign(form, { buildingId: row.buildingId, departmentId: row.departmentId, roomNumber: row.roomNumber, floor: row.floor, area: row.area, purpose: row.purpose, status: row.status, remark: row.remark })
  } else {
    // 新增模式：重置表单
    editId.value = null
    Object.assign(form, { buildingId: null, departmentId: null, roomNumber: '', floor: 1, area: 0, purpose: '', status: '空闲', remark: '' })
  }
}

/** 提交表单（新增或修改） */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (editId.value) { await updateRoom(editId.value, form); ElMessage.success('修改成功') }
    else { await createRoom(form); ElMessage.success('登记成功') }
    dialogVisible.value = false; loadData()
  } finally { submitLoading.value = false }
}

// ==================== 状态与删除操作 ====================

/** 变更房屋状态 */
async function handleStatusChange(row, status) {
  await updateRoomStatus(row.id, status)
  ElMessage.success(`状态已变更为${status}`)
  loadData()
}

/** 删除房屋 */
async function handleDelete(id) {
  await deleteRoom(id); ElMessage.success('删除成功'); loadData()
}

// ==================== 生命周期 ====================

onMounted(async () => {
  // 并行加载单位和楼宇下拉数据
  const [deptRes, bldRes] = await Promise.all([getDepartments(), getAllBuildings()])
  departments.value = deptRes.data; buildings.value = bldRes.data
  loadData()  // 加载房屋列表
})
</script>

<style scoped>
.full-width-input-number {
  width: 100% !important;
  min-width: 120px;
}
.full-width-input-number :deep(.el-input__inner) {
  text-align: left;
  min-width: 3em;
}
</style>
