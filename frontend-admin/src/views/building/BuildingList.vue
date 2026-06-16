<template>
  <div class="building-list">
    <div class="page-header">
      <h2>楼宇管理</h2>
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增楼宇</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="query.name" placeholder="楼宇名称" clearable style="width: 200px" @clear="loadData" />
      <el-select v-model="query.departmentId" placeholder="所属单位" clearable style="width: 180px" @change="loadData">
        <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
        <el-option label="正常" :value="1" />
        <el-option label="封存" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="page-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="code" label="楼宇编号" width="140" />
        <el-table-column prop="name" label="楼宇名称" min-width="150" />
        <el-table-column prop="departmentName" label="所属单位" width="120" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="floors" label="楼层数" width="80" align="center" />
        <el-table-column prop="totalArea" label="总面积(㎡)" width="110" align="right" />
        <el-table-column prop="structureType" label="结构类型" width="100" />
        <el-table-column prop="buildYear" label="建造年份" width="90" align="center" />
        <el-table-column prop="roomCount" label="房屋数" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '封存' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '封存' : '启用' }}
            </el-button>
            <el-popconfirm title="确定要删除该楼宇吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑楼宇' : '新增楼宇'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="楼宇名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入楼宇名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="楼宇编号" prop="code">
              <el-input v-model="form.code" placeholder="如 BLD-001" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属单位" prop="departmentId">
              <el-select v-model="form.departmentId" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结构类型">
              <el-select v-model="form.structureType" placeholder="请选择" style="width: 100%">
                <el-option label="钢筋混凝土" value="钢筋混凝土" />
                <el-option label="框架结构" value="框架结构" />
                <el-option label="钢结构" value="钢结构" />
                <el-option label="砖混结构" value="砖混结构" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="楼层数">
              <el-input-number v-model="form.floors" :min="1" :max="200" placeholder="楼层数" class="full-width-input-number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总面积(㎡)">
              <el-input-number v-model="form.totalArea" :min="0" :precision="2" placeholder="总面积" class="full-width-input-number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="建造年份">
              <el-input-number v-model="form.buildYear" :min="1900" :max="2030" placeholder="建造年份" class="full-width-input-number" />
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
 * 楼宇管理页面
 *
 * 功能说明：
 * - 楼宇列表的分页查询、搜索过滤
 * - 新增/编辑楼宇信息
 * - 变更楼宇状态（正常/封存）
 * - 删除楼宇（需确认，且楼宇下无房屋时才可删除）
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBuildingList, getDepartments, createBuilding, updateBuilding, updateBuildingStatus, deleteBuilding } from '@/api/building'

// ==================== 响应式状态 ====================
const loading = ref(false)           // 表格加载状态
const submitLoading = ref(false)     // 表单提交加载状态
const tableData = ref([])            // 表格数据
const total = ref(0)                 // 数据总条数
const departments = ref([])          // 单位下拉选项
const dialogVisible = ref(false)     // 弹窗显示状态
const editId = ref(null)             // 编辑时的楼宇ID，新增时为null
const formRef = ref()                // 表单引用，用于校验

// 查询参数
const query = reactive({ current: 1, size: 10, name: '', departmentId: null, status: null })

// 表单数据
const form = reactive({
  name: '', code: '', departmentId: null, address: '', floors: 1,
  totalArea: 0, structureType: '', buildYear: 2024, remark: ''
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入楼宇名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入楼宇编号', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择所属单位', trigger: 'change' }]
}

// ==================== 数据加载方法 ====================

/** 加载楼宇列表数据 */
async function loadData() {
  loading.value = true
  try {
    const res = await getBuildingList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

/** 加载单位下拉选项 */
async function loadDepartments() {
  const res = await getDepartments()
  departments.value = res.data
}

/** 重置查询条件并刷新列表 */
function resetQuery() {
  query.name = ''; query.departmentId = null; query.status = null; query.current = 1
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
    Object.assign(form, { name: row.name, code: row.code, departmentId: row.departmentId, address: row.address, floors: row.floors, totalArea: row.totalArea, structureType: row.structureType, buildYear: row.buildYear, remark: row.remark })
  } else {
    // 新增模式：重置表单
    editId.value = null
    Object.assign(form, { name: '', code: '', departmentId: null, address: '', floors: 1, totalArea: 0, structureType: '', buildYear: 2024, remark: '' })
  }
}

/** 提交表单（新增或修改） */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (editId.value) {
      await updateBuilding(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createBuilding(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

// ==================== 状态与删除操作 ====================

/** 切换楼宇状态（正常↔封存） */
async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateBuildingStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已启用' : '已封存')
  loadData()
}

/** 删除楼宇 */
async function handleDelete(id) {
  await deleteBuilding(id)
  ElMessage.success('删除成功')
  loadData()
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadDepartments()  // 加载单位下拉数据
  loadData()         // 加载楼宇列表
})
</script>

<style scoped>
/* 保证 input-number 有足够宽度，数字内容可完整展示 */
.full-width-input-number {
  width: 100% !important;
  min-width: 120px;
}
.full-width-input-number :deep(.el-input__inner) {
  text-align: left;
  min-width: 3em;
}
</style>
