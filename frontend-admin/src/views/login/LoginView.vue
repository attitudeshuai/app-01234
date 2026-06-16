<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="brand">
          <el-icon :size="48" color="#fff"><OfficeBuilding /></el-icon>
          <h1>楼宇管理系统</h1>
          <p>Building Management System</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <el-icon :size="20"><House /></el-icon>
            <span>楼宇房屋全生命周期管理</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><DataAnalysis /></el-icon>
            <span>多维度统计分析与决策支持</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><Lock /></el-icon>
            <span>分级权限管控与数据隔离</span>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-wrapper">
          <h2>账号登录</h2>
          <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  display: flex;
  width: 880px;
  min-height: 480px;
  background: $bg-white;
  border-radius: $radius-lg;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.login-left {
  width: 400px;
  background: linear-gradient(135deg, $primary-color 0%, #096dd9 100%);
  color: #fff;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .brand {
    margin-bottom: 48px;

    h1 {
      font-size: 28px;
      font-weight: 700;
      margin: 16px 0 8px;
    }

    p {
      font-size: $font-size-sm;
      opacity: 0.7;
    }
  }

  .features {
    .feature-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      padding: 12px 0;
      font-size: $font-size-base;
      opacity: 0.9;

      & + .feature-item {
        border-top: 1px solid rgba(255, 255, 255, 0.15);
      }
    }
  }
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;

  .login-form-wrapper {
    width: 100%;
    max-width: 360px;

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 32px;
    }
  }
}
</style>
