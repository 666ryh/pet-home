import { createRouter, createWebHistory } from 'vue-router'
import BackendLayout from '@/layouts/BackendLayout.vue'

// 静态路由
export const constantRoutes = [
  {
    path: '/back',
    redirect: '/back/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/back',
    name: 'back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/backend/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'service',
        name: 'ServiceManagement',
        component: () => import('@/views/backend/service/ServiceManagement.vue'),
        meta: { title: '服务管理' }
      },
      {
        path: 'appointment',
        name: 'AppointmentManagement',
        component: () => import('@/views/backend/service/AppointmentManagement.vue'),
        meta: { title: '预约管理' }
      }
    ]
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  }
]

// 动态路由
export const asyncRoutes = []

// 前台路由配置
const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontendLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/frontend/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'pet',
        name: 'PetList',
        component: () => import('@/views/frontend/pet/PetList.vue'),
        meta: { title: '宠物领养' }
      },
      {
        path: 'pet/:id',
        name: 'PetDetail',
        component: () => import('@/views/frontend/pet/PetDetail.vue'),
        meta: { title: '宠物详情' }
      },
      {
        path: 'adoption',
        name: 'MyAdoption',
        component: () => import('@/views/frontend/pet/MyAdoption.vue'),
        meta: { title: '我的领养', requiresAuth: true }
      },
      {
        path: 'boarding/apply',
        name: 'boardingApply',
        component: () => import('@/views/frontend/pet/BoardingApply.vue'),
        meta: { title: '申请寄养', requireAuth: false }
      },
      {
        path: 'my/boarding',
        name: 'myBoarding',
        component: () => import('@/views/frontend/pet/MyBoarding.vue'),
        meta: { title: '我的寄养', requireAuth: true }
      },
      // 添加产品相关路由
      {
        path: 'product',
        name: 'ProductList',
        component: () => import('@/views/frontend/product/ProductList.vue'),
        meta: { title: '宠物用品' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/frontend/product/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/frontend/product/Cart.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'order',
        name: 'MyOrders',
        component: () => import('@/views/frontend/product/MyOrders.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/frontend/product/OrderDetail.vue'),
        meta: { title: '订单详情', requiresAuth: true }
      },
      // 添加服务相关路由
      {
        path: 'service',
        name: 'ServiceList',
        component: () => import('@/views/frontend/service/ServiceList.vue'),
        meta: { title: '宠物服务' }
      },
      {
        path: 'service/:id',
        name: 'ServiceDetail',
        component: () => import('@/views/frontend/service/ServiceDetail.vue'),
        meta: { title: '服务详情' }
      },
      {
        path: 'service/booking/:id',
        name: 'ServiceBooking',
        component: () => import('@/views/frontend/service/ServiceBooking.vue'),
        meta: { title: '预约服务', requiresAuth: true }
      },
      {
        path: 'service/my-appointments',
        name: 'MyAppointments',
        component: () => import('@/views/frontend/service/MyAppointments.vue'),
        meta: { title: '我的预约', requiresAuth: true }
      },
      // 添加宠物训练相关路由
      {
        path: 'training',
        name: 'TrainingList',
        component: () => import('@/views/frontend/training/TrainingList.vue'),
        meta: { title: '宠物训练课程' }
      },
      {
        path: 'training/:id',
        name: 'TrainingDetail',
        component: () => import('@/views/frontend/training/TrainingDetail.vue'),
        meta: { title: '课程详情' }
      },
      {
        path: 'training-booking/:id',
        name: 'TrainingBooking',
        component: () => import('@/views/frontend/training/TrainingBooking.vue'),
        meta: { title: '预约训练', requiresAuth: true }
      },
      {
        path: 'training/my-appointments',
        name: 'TrainingMyAppointments',
        component: () => import('@/views/frontend/training/MyAppointments.vue'),
        meta: { title: '我的训练预约', requiresAuth: true }
      },
      // 添加搜索页面路由
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/frontend/Search.vue'),
        meta: { title: '搜索结果' }
      }
    ] 
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  }
]

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...frontendRoutes,
    ...constantRoutes
  ]
})

// 标记路由是否已初始化
let isRoutesInitialized = false;

// 初始化路由权限 - 修改为接收userStore作为参数
const initializeRoutes = (userStore) => {
  if (isRoutesInitialized) return;
  
  // 检查localStorage中是否有token和role信息
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  
  // 如果是管理员用户，添加后台路由
  if ((userStore && userStore.isLoggedIn && !userStore.isUser) || 
      (token && role && role !== 'USER')) {
    setBackRoutes();
    isRoutesInitialized = true;
  }
}

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 宠物之家`
  }
  
  // 在这里安全地导入并使用store
  const { useUserStore } = require('@/store/user')
  const userStore = useUserStore()
  
  // 每次路由导航前初始化路由，传入userStore
  initializeRoutes(userStore);

  // 检查是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 已登录用户的路由控制
  if (userStore.isLoggedIn) {
    // 处理登录页面访问
    if (to.path === '/login') {
      next(userStore.isUser ? '/' : '/back/dashboard')
      return
    }

    if (!userStore.isUser) {
      // 后台路由处理
      if (to.path.startsWith('/back')) {
        // 确保后台路由已添加
        if (!isRoutesInitialized) {
          setBackRoutes();
          isRoutesInitialized = true;
          // 设置路由后，需要重新导航到目标路由
          next({ ...to, replace: true });
          return;
        }
        next();
      } else {
        next('/back/dashboard');
      }
      return;
    } else {
      // 普通用户只能访问前台路由
      if (to.path.startsWith('/back')) {
        next('/')
      } else {
        next()
      }
      return
    }
  } else {
    // 未登录用户
    if (to.path.startsWith('/back')) {
      next('/login')
      return
    }
  }

  next()
})

// 动态加载组件
export function loadComponent(component) {
  try {
    // 如果是布局组件
    if (component === 'Layout') {
      return BackendLayout
    }
    // 处理路径中的斜杠，转换为实际的文件路径
    const path = component.replace(/^\//, '').replace(/\//g, '/')
    // 其他组件动态导入
    return () => import(`@/views/backend/${path}.vue`).catch(error => {
      console.error(`Failed to load component: @/views/backend/${path}.vue`, error)
      // 返回一个简单的错误提示组件
      return {
        template: `
          <div class="error-component">
            <h3>组件加载失败</h3>
            <p>路径: ${path}</p>
          </div>
        `
      }
    })
  } catch (error) {
    console.error('Component loading error:', error)
    // 返回一个空组件而不是直接抛出错误
    return {
      template: '<div>组件加载失败</div>'
    }
  }
}

// 生成动态路由
export function generateAsyncRoutes(menuItems) {
  const menus = menuItems || JSON.parse(localStorage.getItem('menus') || '[]')
  if (!menus || !menus.length) {
    console.warn('No menus provided for route generation')
    return []
  }

  const routes = []
  
  menus.forEach(menu => {
    console.log("current menu", menu)
    if (!menu.path) {
      console.warn('Invalid menu item: missing path')
      return
    }

    // 只有有组件的菜单才会生成实际路由
    if (menu.component) {
      const route = {
        path: menu.path.startsWith('/') ? menu.path.substring(1) : menu.path,
        name: menu.name || menu.path.replace(/\//g, '-').slice(1),
        component: loadComponent(menu.component),
        meta: {
          title: menu.title || menu.name,
          icon: menu.icon,
          hidden: menu.hidden
        }
      }

      // 处理子菜单
      if (menu.children && menu.children.length > 0) {
        route.children = generateAsyncRoutes(menu.children)
      }

      routes.push(route)
    } else if (menu.children && menu.children.length > 0) {
      // 如果菜单没有组件但有子菜单，只处理其子菜单并将其添加到routes中
      const childRoutes = generateAsyncRoutes(menu.children)
      routes.push(...childRoutes)
    }
  })

  return routes
}

export function setBackRoutes() {
  // 从localStorage获取菜单数据
  const menus = JSON.parse(localStorage.getItem('menus') || '[]')
  const routes = generateAsyncRoutes(menus)
  
  // 防止无路由情况下移除默认路由
  try {
    router.removeRoute('back')
  } catch (error) {
    console.warn('Error removing back route:', error)
  }
  
  router.addRoute({
    path: '/back',
    name: 'back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/backend/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'service',
        name: 'ServiceManagement',
        component: () => import('@/views/backend/service/ServiceManagement.vue'),
        meta: { title: '服务管理' }
      },
      {
        path: 'appointment',
        name: 'AppointmentManagement',
        component: () => import('@/views/backend/service/AppointmentManagement.vue'),
        meta: { title: '预约管理' }
      },
      {
        path: 'training/course',
        name: 'TrainingCourseManagement',
        component: () => import('@/views/backend/training/TrainingCourseManagement.vue'),
        meta: { title: '训练课程管理' }
      },
      {
        path: 'training/category',
        name: 'TrainingCategoryManagement',
        component: () => import('@/views/backend/training/TrainingCategoryManagement.vue'),
        meta: { title: '训练分类管理' }
      },
      {
        path: 'training/appointment',
        name: 'TrainingAppointmentManagement',
        component: () => import('@/views/backend/training/TrainingAppointmentManagement.vue'),
        meta: { title: '训练预约管理' }
      },
      ...routes
    ]
  })

  
  // 添加404路由
  router.addRoute({
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  })
}

// 在创建路由后立即初始化路由
// 这样在页面刷新时也能保持路由状态
const token = localStorage.getItem('token');
const role = localStorage.getItem('role');
if (token && role && role !== 'USER') {
  setBackRoutes();
  isRoutesInitialized = true;
}

export default router
