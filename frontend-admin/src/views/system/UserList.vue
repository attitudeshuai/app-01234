<!--
  用户管理页面
  功能：系统用户的增删改查、密码重置
-->
<template>
  <div class="user-list">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <!-- 搜索过滤栏 -->
    <div class="search-bar">
      <el-input
        v-model="query.keyword"
        placeholder="用户名/姓名"
        clearable
        style="width: 200px"
        @keyup.enter="loadData"
      />
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleSearchReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="page-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'ADMIN' ? '集团管理员' : '单位管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="所属单位" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column
          prop="email"
          label="邮箱"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">
              编辑
            </el-button>
            <el-popconfirm
              title="确定重置密码为 123456？"
              @confirm="handleResetPassword(row.id)"
            >
              <template #reference>
                <el-button link type="warning" size="small">重置密码</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm
              v-if="row.username !== 'admin'"
              title="确定删除该用户？"
              @confirm="handleDelete(row.id)"
            >
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
      :title="editId ? '编辑用户' : '新增用户'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            :disabled="!!editId"
            placeholder="请输入用户名"
          />
        </el-form-item>

        <!-- 密码（仅新增时显示） -->
        <el-form-item v-if="!editId" label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="默认 123456"
            show-password
          />
        </el-form-item>

        <!-- 真实姓名与角色 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select
                v-model="form.role"
                placeholder="请选择角色"
                style="width: 100%"
              >
                <el-option label="集团管理员" value="ADMIN" />
                <el-option label="单位管理员" value="MANAGER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 所属单位 -->
        <el-form-item label="所属单位" prop="departmentId">
          <el-select v-model="form.departmentId" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="d in departments"
              :key="d.id"
              :label="d.name"
              :value="d.id"
            />
          </el-select>
        </el-form-item>

        <!-- 手机号与状态 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch
                v-model="form.status"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
 * 用户管理
 * - 展示用户列表
 * - 支持用户的增删改
 * - 支持密码重置
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, resetPassword } from '@/api/user'
import { getDepartments } from '@/api/building'

// ==================== 状态定义 ====================

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitLoading = ref(false)
/** 表格数据 */
const tableData = ref([])
/** 数据总数 */
const total = ref(0)

/** 单位列表 */
const departments = ref([])
/** 对话框显示状态 */
const dialogVisible = ref(false)
/** 当前编辑的记录ID（新增时为null） */
const editId = ref(null)
/** 表单引用 */
const formRef = ref()

// ==================== 查询参数 ====================

const query = reactive({
  current: 1,
  size: 10,
  keyword: ''
})

// ==================== 表单数据 ====================

const form = reactive({
  username: '',
  password: '',
  realName: '',
  role: 'MANAGER',
  departmentId: null,
  phone: '',
  email: '',
  status: 1
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
 * 邮箱验证器
 */
const emailValidator = (rule, value, callback) => {
  if (value && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择单位', trigger: 'change' }],
  phone: [{ validator: phoneValidator, trigger: 'blur' }],
  email: [{ validator: emailValidator, trigger: 'blur' }]
}

// ==================== 数据加载 ====================

/**
 * 加载用户列表数据
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getUserList(query)
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
function handleSearchReset() {
  query.keyword = ''
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
      username: row.username,
      realName: row.realName,
      role: row.role,
      departmentId: row.departmentId,
      phone: row.phone,
      email: row.email,
      status: row.status
    })
  } else {
    // 新增模式
    editId.value = null
    Object.assign(form, {
      username: '',
      password: '',
      realName: '',
      role: 'MANAGER',
      departmentId: null,
      phone: '',
      email: '',
      status: 1
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
      await updateUser(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createUser(form)
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
 * 重置用户密码
 */
async function handleResetPassword(id) {
  await resetPassword(id)
  ElMessage.success('密码已重置为 123456')
}

/**
 * 删除用户
 */
async function handleDelete(id) {
  await deleteUser(id)
  ElMessage.success('删除成功')
  loadData()
}

// ==================== 生命周期 ====================

onMounted(async () => {
  // 加载单位列表
  const res = await getDepartments()
  departments.value = res.data

  // 加载用户列表
  loadData()
})
</script>
