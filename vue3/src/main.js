import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
// 导入 Element Plus
import ElementPlus from 'element-plus'
// 导入自定义主题色配置
import './styles/element-variables.scss'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 导入初始化样式
import './assets/global.css'
// 中文
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// 导入用户状态管理
import { useUserStore } from './store/user'

// 添加全局错误处理器来抑制ResizeObserver警告
const originalConsoleError = console.error
console.error = (...args) => {
  if (args[0] && args[0].includes && args[0].includes('ResizeObserver loop')) {
    return
  }
  originalConsoleError(...args)
}

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})
// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 确保在应用挂载前初始化用户状态
const initApp = async () => {
  // 初始化Pinia状态
  const userStore = useUserStore()
  
  // 尝试从localStorage恢复用户状态
  const token = localStorage.getItem('token')
  if (token) {
    try {
      // 恢复用户信息
      await userStore.getUserInfo()
    } catch (error) {
      console.warn('恢复用户状态失败:', error)
      // 如果恢复失败，清除用户状态
      userStore.clearUserInfo()
    }
  }
  
  // 挂载应用
  app.mount('#app')
}

// 启动应用
initApp()
