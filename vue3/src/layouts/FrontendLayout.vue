<template>
    <div class="frontend-layout">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header-container">
          <div class="logo">
            <router-link to="/">
              <img src="@/assets/宠物手掌.svg" alt="宠物之家" class="logo-image" />
              <span class="logo-text">宠物之家</span>
            </router-link>
          </div>
          
          <el-menu 
            :default-active="activeIndex"
            class="main-menu"
            mode="horizontal"
            :ellipsis="false"
            :router="true"
            background-color="#FFFAF0"
            text-color="#6E4C1E"
            active-text-color="#FF9800"
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            
            <el-menu-item index="/pet">
              <el-icon><Promotion /></el-icon>
              <span>宠物领养</span>
            </el-menu-item>
            
            <el-menu-item index="/product">
              <el-icon><ShoppingBag /></el-icon>
              <span>宠物用品</span>
            </el-menu-item>
            
            <el-menu-item index="/service">
              <el-icon><Service /></el-icon>
              <span>宠物服务</span>
            </el-menu-item>

            <el-menu-item index="/boarding/apply">
              <el-icon><House /></el-icon>
              <span>申请寄养</span>
            </el-menu-item>
            
            <el-menu-item index="/training">
              <el-icon><Trophy /></el-icon>
              <span>宠物训练</span>
            </el-menu-item>
            
            <div class="flex-grow"></div>
            
            <div class="search-box">
              <el-input 
                v-model="searchQuery"
                placeholder="搜索宠物、商品或服务..." 
                class="rounded-search"
                size="small"
                @keyup.enter="handleSearch"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            
            <!-- 未登录状态 -->
            <template v-if="!isLoggedIn">
              <el-menu-item index="/login" class="login-item">
                <el-button type="text" size="small">
                  <el-icon><Key /></el-icon>
                  <span>登录</span>
                </el-button>
              </el-menu-item>
              <el-menu-item index="/register" class="register-item">
                <el-button type="primary" size="small" plain>
                  <span>注册</span>
                </el-button>
              </el-menu-item>
            </template>
            
            <!-- 已登录状态 -->
            <template v-else>
              <el-sub-menu index="5" popper-class="user-dropdown">
                <template #title>
                  <div class="user-avatar">
                    <el-avatar :size="32" :src="'/api'+userStore.userInfo.avatar" />
                  </div>
                </template>
                <el-menu-item index="/profile">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-menu-item>
                <el-menu-item index="/cart">
                  <el-icon><ShoppingCart /></el-icon>
                  <span>我的购物车</span>
                  <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge" />
                </el-menu-item>
                <el-menu-item index="/order">
                  <el-icon><Document /></el-icon>
                  <span>我的订单</span>
                </el-menu-item>
                <el-menu-item index="/adoption">
                  <el-icon><House /></el-icon>
                  <span>我的领养</span>
                </el-menu-item>
                <el-menu-item index="/my/boarding">
                  <el-icon><House /></el-icon>
                  <span>我的寄养</span>
                </el-menu-item>
                <el-menu-item index="/service/my-appointments">
                  <el-icon><Calendar /></el-icon>
                  <span>我的预约</span>
                </el-menu-item>
                <el-menu-item index="/training/my-appointments">
                  <el-icon><Trophy /></el-icon>
                  <span>我的训练</span>
                </el-menu-item>
                <el-menu-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-menu>
          
          <!-- 移动端菜单按钮 -->
          <div class="mobile-toggle-btn" @click="toggleMobileMenu">
            <el-icon><Menu /></el-icon>
          </div>
        </div>
      </header>
  
      <!-- 移动端菜单 -->
      <el-drawer
        v-model="showMobileMenu"
        title="宠物之家"
        direction="ltr"
        size="80%"
        :with-header="true"
      >
        <div class="mobile-search-box">
          <el-input 
            v-model="searchQuery"
            placeholder="搜索宠物、商品或服务..." 
            clearable
            @keyup.enter="handleMobileSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      
        <el-menu 
          :default-active="activeIndex"
          class="mobile-menu"
          :router="true"
          @select="closeMobileMenu"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          
          <el-menu-item index="/pet">
            <el-icon><Promotion /></el-icon>
            <span>宠物领养</span>
          </el-menu-item>
          
          <el-menu-item index="/product">
            <el-icon><ShoppingBag /></el-icon>
            <span>宠物用品</span>
          </el-menu-item>
          
          <el-menu-item index="/service">
            <el-icon><Service /></el-icon>
            <span>宠物服务</span>
          </el-menu-item>
          
          <el-menu-item index="/boarding/apply">
            <el-icon><House /></el-icon>
            <span>申请寄养</span>
          </el-menu-item>
          
          <el-menu-item index="/training">
            <el-icon><Trophy /></el-icon>
            <span>宠物训练</span>
          </el-menu-item>
          
          <el-menu-item index="/wiki">
            <el-icon><FirstAid /></el-icon>
            <span>宠物百科</span>
          </el-menu-item>
          
          <el-menu-item index="/guide">
            <el-icon><FirstAid /></el-icon>
            <span>饲养指南</span>
          </el-menu-item>
          
          <el-menu-item index="/adoption" v-if="isLoggedIn">
            <el-icon><House /></el-icon>
            <span>我的领养</span>
          </el-menu-item>
          
          <el-menu-item index="/order" v-if="isLoggedIn">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          
          <el-menu-item index="/cart" v-if="isLoggedIn">
            <el-icon><ShoppingCart /></el-icon>
            <span>我的购物车</span>
            <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge" />
          </el-menu-item>
          
          <el-menu-item index="/my/boarding" v-if="isLoggedIn">
            <el-icon><House /></el-icon>
            <span>我的寄养</span>
          </el-menu-item>
          
          <el-menu-item index="/service/my-appointments" v-if="isLoggedIn">
            <el-icon><Calendar /></el-icon>
            <span>我的预约</span>
          </el-menu-item>
          
          <el-menu-item index="/training/my-appointments" v-if="isLoggedIn">
            <el-icon><Trophy /></el-icon>
            <span>我的训练</span>
          </el-menu-item>
          
          <el-menu-item index="/health-records" v-if="isLoggedIn">
            <el-icon><FirstAid /></el-icon>
            <span>健康档案</span>
          </el-menu-item>
        </el-menu>
        
        <div class="mobile-user-actions">
          <template v-if="isLoggedIn">
            <el-button @click="goToProfile" type="info" plain icon="User" block>个人中心</el-button>
            <el-button @click="handleLogoutMobile" type="warning" plain icon="SwitchButton" block class="logout-btn">
              退出登录
            </el-button>
          </template>
          <template v-else>
            <el-button @click="goToLogin" type="primary" plain block>登录</el-button>
            <el-button @click="goToRegister" type="warning" block>注册</el-button>
          </template>
        </div>
      </el-drawer>
  
      <!-- 主要内容区域 -->
      <main class="main-content">
        <router-view />
      </main>
  
      <!-- 页脚 -->
      <footer class="footer">
        
        <div class="footer-bottom">
          <p>&copy; 2025 宠物之家 - 为宠物提供温暖的家</p>
 
        </div>
      </footer>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, reactive, onMounted } from 'vue'
  import { useUserStore } from '@/store/user'
  import { useRouter, useRoute } from 'vue-router'
  import { 
    ShoppingCart, 
    User, 
    Key, 
    Edit, 
    SwitchButton, 
    Menu, 
    Search, 
    ArrowDown, 
    ArrowRight,
    Location,
    Phone,
    Message,
    Star,
    Link,
    HomeFilled,
    ShoppingBag,
    Service,
    Document,
    Pet,
    House,
    Calendar,
    Trophy,
    Promotion,
    FirstAid
  } from '@element-plus/icons-vue'
  
  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
  const searchQuery = ref('')
  const showMobileMenu = ref(false)
  const cartCount = ref(0) // 这里应该从购物车store中获取
  const activeIndex = ref('/')
  
  const isLoggedIn = computed(() => !!userStore.token)
  
  onMounted(() => {
    // 设置当前激活的菜单项
    activeIndex.value = route.path
  })
  
  // 处理搜索
  const handleSearch = () => {
    if (searchQuery.value.trim()) {
      router.push({ 
        path: '/search', 
        query: { q: searchQuery.value.trim() } 
      })
      searchQuery.value = ''
      closeMobileMenu() // 如果是移动菜单展开状态，关闭菜单
    }
  }
  
  const handleMobileSearch = () => {
    handleSearch()
  }
  
  // 处理退出登录
  const handleLogout = () => {
    userStore.clearUserInfo()
    router.push('/login')
  }
  
  const handleLogoutMobile = () => {
    handleLogout()
    closeMobileMenu()
  }
  
  // 移动端菜单控制
  const toggleMobileMenu = () => {
    showMobileMenu.value = !showMobileMenu.value
  }
  
  const closeMobileMenu = () => {
    showMobileMenu.value = false
  }
  
  // 移动端导航
  const goToProfile = () => {
    router.push('/profile')
    closeMobileMenu()
  }
  
  const goToLogin = () => {
    router.push('/login')
    closeMobileMenu()
  }
  
  const goToRegister = () => {
    router.push('/register')
    closeMobileMenu()
  }
  </script>
  
  <style scoped>
  .frontend-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    font-family: 'Open Sans', 'Nunito Sans', Arial, sans-serif;
    background-color: #FFFDF7;
  }
  
  /* 顶部导航栏 - 暖黄色调 */
  .header {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    position: sticky;
    top: 0;
    z-index: 1000;
    background-color: #FFFAF0;
  }
  
  .header-container {
    display: flex;
    align-items: center;
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 1rem;
    height: 56px; /* 减小导航栏高度 */
  }
  
  .logo {
    margin-right: 15px; /* 减小右侧间距 */
  }
  
  .logo a {
    display: flex;
    align-items: center;
    text-decoration: none;
    color: #6E4C1E;
  }
  
  .logo-image {
    height: 32px; /* 减小logo大小 */
    margin-right: 8px;
    transition: transform 0.3s;
  }
  
  .logo:hover .logo-image {
    transform: scale(1.1);
  }
  
  .logo-text {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 18px; /* 减小标题字体 */
    font-weight: bold;
    color: #6E4C1E;
  }
  
  /* Element Plus Menu 样式覆盖 */
  .main-menu {
    flex: 1;
    border-bottom: none !important;
    background-color: transparent !important;
  }
  
  .main-menu :deep(.el-menu-item) {
    font-size: 14px; /* 减小菜单项字体 */
    border-bottom: none !important;
    padding: 0 12px; /* 减小内边距使菜单项更紧凑 */
    height: 56px; /* 匹配导航栏高度 */
  }
  
  .main-menu :deep(.el-menu-item.is-active) {
    font-weight: bold;
  }
  
  .main-menu :deep(.el-sub-menu__title) {
    font-size: 14px; /* 减小子菜单标题字体 */
    border-bottom: none !important;
    padding: 0 12px; /* 减小内边距 */
    height: 56px; /* 匹配导航栏高度 */
    display: flex !important;
    align-items: center !important;
    justify-content: flex-start !important;
  }
  
  .main-menu :deep(.el-sub-menu__icon-arrow) {
    position: static !important;
    margin-left: 4px !important;
    margin-top: 0 !important;
    margin-right: 0 !important;
    order: 1 !important;
  }
  
  .main-menu :deep(.el-sub-menu__title .el-icon) {
    order: 0 !important;
    margin-right: 4px !important;
  }
  
  .main-menu :deep(.el-sub-menu__title span) {
    order: 0 !important;
  }
  
  .main-menu :deep(.el-menu--horizontal > .el-menu-item:not(.is-disabled):focus, 
                   .el-menu--horizontal > .el-menu-item:not(.is-disabled):hover) {
    background-color: #FFF2D6 !important;
  }
  
  .main-menu :deep(.el-menu--horizontal > .el-sub-menu:focus .el-sub-menu__title,
                   .el-menu--horizontal > .el-sub-menu:hover .el-sub-menu__title) {
    background-color: #FFF2D6 !important;
  }
  
  /* 为菜单添加图标和文字之间的间距 */
  .main-menu :deep(.el-icon), 
  .mobile-menu :deep(.el-icon) {
    margin-right: 4px; /* 减小图标与文字的间距 */
    width: 16px; /* 减小图标大小 */
    height: 16px; /* 减小图标大小 */
  }
  
  /* 用户头像 */
  .user-avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
  
  .user-avatar :deep(.el-avatar) {
    margin-right: 0;
    border: 2px solid #FFEED6;
    transition: all 0.3s;
    width: 28px; /* 减小头像大小 */
    height: 28px; /* 减小头像大小 */
  }
  
  .user-avatar:hover :deep(.el-avatar) {
    transform: scale(1.1);
    border-color: #FFCD70;
  }
  
  /* 购物车徽章 */
  .cart-badge :deep(.el-badge__content) {
    background-color: #FF9800;
    border: none;
  }
  
  /* 弹性增长空间，用于推动搜索框和用户操作到右侧 */
  .flex-grow {
    flex-grow: 1;
  }
  
  /* 搜索框样式 */
  .search-box {
    margin: 0 12px; /* 减小搜索框左右间距 */
    width: 200px; /* 减小搜索框宽度 */
    display: flex;
    align-items: center;
    height: 100%;
  }
  
  .rounded-search {
    width: 100%;
  }
  
  .rounded-search :deep(.el-input__wrapper) {
    border-radius: 20px;
    background-color: #FFF5E0;
    box-shadow: 0 0 0 1px #FFEBC8;
    transition: all 0.3s;
  }
  
  .rounded-search :deep(.el-input__wrapper:hover) {
    background-color: white;
    box-shadow: 0 0 0 1px #FFD691;
  }
  
  .rounded-search :deep(.el-input__wrapper.is-focus) {
    background-color: white;
    box-shadow: 0 0 0 1px #FFCC80;
  }
  
  /* 登录注册按钮样式 */
  .login-item :deep(.el-button) {
    color: #6E4C1E;
  }
  
  .login-item :deep(.el-button:hover) {
    color: #FF9800;
  }
  
  .register-item :deep(.el-button) {
    color: #FF9800;
    border-color: #FF9800;
  }
  
  .register-item :deep(.el-button:hover) {
    color: white;
    background-color: #FF9800;
  }
  
  /* 移动端菜单按钮 */
  .mobile-toggle-btn {
    display: none;
    font-size: 24px;
    cursor: pointer;
    color: #6E4C1E;
  }
  
  /* 移动端搜索框 */
  .mobile-search-box {
    padding: 16px;
    border-bottom: 1px solid #EAEAEA;
  }
  
  /* 移动端菜单样式 */
  .mobile-menu {
    border-right: none;
  }
  
  .mobile-user-actions {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    border-top: 1px solid #EAEAEA;
    margin-top: auto;
  }
  
  .logout-btn {
    margin-top: 8px;
  }
  
  /* 主要内容区域 */
  .main-content {
    flex: 1;
    padding: 2rem;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
  }
  
  /* 页脚样式 */
  .footer {
    background: linear-gradient(135deg, #F7E2B2, #E6C674);
    color: #6E4C1E;
    margin-top: auto;
  }
  
  .footer-container {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    max-width: 1200px;
    margin: 0 auto;
    padding: 3rem 2rem;
  }
  
  .footer-section {
    flex: 1;
    min-width: 200px;
    margin-bottom: 2rem;
  }
  
  .footer-title {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 18px;
    margin-bottom: 1rem;
    position: relative;
    padding-bottom: 10px;
    color: #6E4C1E;
  }
  
  .footer-title::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 40px;
    height: 3px;
    background: linear-gradient(to right, #FFBB54, #FF9800);
    border-radius: 3px;
  }
  
  .footer-links {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  
  .footer-links li {
    margin-bottom: 0.5rem;
  }
  
  .footer-links a {
    color: #6E4C1E;
    text-decoration: none;
    transition: color 0.3s;
    display: inline-block;
  }
  
  .footer-links a:hover {
    color: #FF9800;
    transform: translateX(5px);
  }
  
  .social-links {
    display: flex;
    gap: 1rem;
  }
  
  .social-link {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    background-color: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    color: #6E4C1E;
    transition: all 0.3s;
  }
  
  .social-link:hover {
    background-color: #FF9800;
    color: white;
    transform: scale(1.1);
  }
  
  .contact-info p {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 0.75rem;
    color: #6E4C1E;
  }
  
  .footer-bottom {
    background-color: rgba(0, 0, 0, 0.05);
    padding: 1.5rem 2rem;
    text-align: center;
    color: #6E4C1E;
  }
  
  .footer-bottom-links {
    display: flex;
    justify-content: center;
    gap: 1.5rem;
    margin-top: 0.5rem;
  }
  
  .footer-bottom-links a {
    color: #6E4C1E;
    text-decoration: none;
    font-size: 14px;
    transition: color 0.3s;
  }
  
  .footer-bottom-links a:hover {
    color: #FF9800;
  }
  
  /* 响应式设计 */
  @media (max-width: 992px) {
    .main-menu {
      display: none !important;
    }
    
    .mobile-toggle-btn {
      display: block;
      margin-left: auto;
    }
    
    .header-container {
      height: 50px; /* 在移动端进一步减小导航栏高度 */
    }
    
    .logo-text {
      font-size: 16px;
    }
    
    .logo-image {
      height: 28px;
    }
  }
  
  @media (max-width: 768px) {
    .footer-container {
      flex-direction: column;
      padding: 2rem 1rem;
    }
    
    .footer-section {
      margin-bottom: 1.5rem;
    }
    
    .main-content {
      padding: 1.5rem 1rem;
    }
  }
  </style>