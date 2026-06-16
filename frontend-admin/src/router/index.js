import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '数据概览', icon: 'DataAnalysis' }
      },
      {
        path: 'building',
        name: 'Building',
        component: () => import('@/views/building/BuildingList.vue'),
        meta: { title: '楼宇管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'room',
        name: 'Room',
        component: () => import('@/views/room/RoomList.vue'),
        meta: { title: '房屋管理', icon: 'House' }
      },
      {
        path: 'asset',
        name: 'Asset',
        component: () => import('@/views/asset/AssetList.vue'),
        meta: { title: '资产配置', icon: 'Box' }
      },
      {
        path: 'health',
        name: 'Health',
        component: () => import('@/views/health/HealthList.vue'),
        meta: { title: '房屋健康', icon: 'FirstAidKit' }
      },
      {
        path: 'approval',
        name: 'Approval',
        component: () => import('@/views/approval/ApprovalList.vue'),
        meta: { title: '使用审批', icon: 'Stamp' }
      },
      {
        path: 'rental',
        name: 'Rental',
        component: () => import('@/views/rental/RentalList.vue'),
        meta: { title: '对外出租', icon: 'Money' }
      },
      {
        path: 'rental/contract',
        name: 'Contract',
        component: () => import('@/views/rental/ContractList.vue'),
        meta: { title: '租赁合约', icon: 'Document' }
      },
      {
        path: 'stats/room',
        name: 'RoomStats',
        component: () => import('@/views/statistics/RoomStats.vue'),
        meta: { title: '房屋统计', icon: 'PieChart' }
      },
      {
        path: 'stats/usage',
        name: 'UsageStats',
        component: () => import('@/views/statistics/UsageStats.vue'),
        meta: { title: '使用统计', icon: 'TrendCharts' }
      },
      {
        path: 'stats/rental',
        name: 'RentalStats',
        component: () => import('@/views/statistics/RentalStats.vue'),
        meta: { title: '租赁统计', icon: 'Histogram' }
      },
      {
        path: 'system/user',
        name: 'UserManage',
        component: () => import('@/views/system/UserList.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '楼宇管理系统'} - 楼宇管理系统`
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
