<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <el-icon :size="28" color="#fff"><OfficeBuilding /></el-icon>
        <span v-show="!sidebarCollapsed" class="logo-text">楼宇管理系统</span>
      </div>
      <el-menu
        :default-active="currentRoute"
        :collapse="sidebarCollapsed"
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#fff"
        router
        :collapse-transition="false"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据概览</template>
        </el-menu-item>

        <el-sub-menu index="building-group">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>基础数据</span>
          </template>
          <el-menu-item index="/building">楼宇管理</el-menu-item>
          <el-menu-item index="/room">房屋管理</el-menu-item>
          <el-menu-item index="/asset">资产配置</el-menu-item>
          <el-menu-item index="/health">房屋健康</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="usage-group">
          <template #title>
            <el-icon><Stamp /></el-icon>
            <span>房屋使用</span>
          </template>
          <el-menu-item index="/approval">内部使用审批</el-menu-item>
          <el-menu-item index="/rental">对外出租</el-menu-item>
          <el-menu-item index="/rental/contract">租赁合约</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="stats-group">
          <template #title>
            <el-icon><PieChart /></el-icon>
            <span>统计分析</span>
          </template>
          <el-menu-item index="/stats/room">房屋统计</el-menu-item>
          <el-menu-item index="/stats/usage">使用统计</el-menu-item>
          <el-menu-item index="/stats/rental">租赁统计</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/system/user">
          <el-icon><UserFilled /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar" :size="20">
            <component :is="sidebarCollapsed ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="dept-tag">{{ userStore.departmentName }}</span>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.realName || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const currentRoute = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '楼宇管理系统')

function toggleSidebar() {
  appStore.toggleSidebar()
}

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.main-layout {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background-color: $bg-dark;
  overflow-y: auto;
  overflow-x: hidden;
  transition: width $transition-base;

  .logo {
    height: $header-height;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-sm;
    padding: 0 $spacing-md;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);

    .logo-text {
      color: #fff;
      font-size: $font-size-md;
      font-weight: 600;
      white-space: nowrap;
    }
  }

  :deep(.el-menu) {
    border-right: none;

    .el-menu-item.is-active {
      background-color: $primary-color !important;
    }

    .el-menu-item:hover,
    .el-sub-menu__title:hover {
      background-color: $bg-dark-light !important;
    }
  }
}

.header {
  height: $header-height;
  background: $bg-white;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-lg;
  z-index: 1;

  .header-left {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .collapse-btn {
      cursor: pointer;
      color: $text-secondary;
      transition: color $transition-fast;

      &:hover {
        color: $primary-color;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .dept-tag {
      font-size: $font-size-xs;
      color: $primary-color;
      background: lighten($primary-color, 42%);
      padding: 2px 8px;
      border-radius: $radius-base;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      cursor: pointer;
      color: $text-regular;

      .user-avatar {
        background-color: $primary-color;
        color: #fff;
        font-size: $font-size-sm;
      }

      .user-name {
        font-size: $font-size-base;
      }
    }
  }
}

.main-content {
  background-color: $bg-base;
  padding: $spacing-lg;
  overflow-y: auto;
}
</style>
