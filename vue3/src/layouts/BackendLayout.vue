<template>
    <div class="backend-layout">
      <!-- 侧边栏 -->
      <Sidebar />
  
      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 顶部导航栏 -->
        <Navbar @logout="handleLogout" />
  
        <!-- 页面内容 -->
        <div class="content-container">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </div>
      
      <!-- 页面装饰 -->
      <div class="page-decoration">
        <div class="decoration-bubble bubble-1"></div>
        <div class="decoration-bubble bubble-2"></div>
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { computed } from 'vue'
  import { useUserStore } from '@/store/user'
  import { useRouter } from 'vue-router'
  import Sidebar from '@/components/backend/Sidebar.vue'
  import Navbar from '@/components/backend/Navbar.vue'
  const userStore = useUserStore()
  const router = useRouter()
  
  const isAdmin = computed(() => userStore.role === 'admin')
  
  const handleLogout = () => {
    userStore.clearUserInfo()
    router.push('/login')
  }
  </script>
  
  <style lang="scss" scoped>
  .backend-layout {
    display: flex;
    height: 100vh;
    min-height: 100vh;
    background-color: #FFF9E6;
    overflow: hidden;
    position: relative;
  }
  
  .sidebar {
    width: 200px;
    background-color: #333;
    color: white;
    padding: 1rem;
  }
  
  .sidebar nav {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .sidebar a {
    color: white;
    text-decoration: none;
  }
  
  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    height: 100%;
    background-color: #FFF9E6;
    position: relative;
    z-index: 1;
  }
  
  .content-container {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    position: relative;
    border-radius: 12px 0 0 0;
    background-color: #ffffff;
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.05);
    margin: 0 0 0 10px;
    transition: all 0.3s ease;
    
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: rgba(255, 182, 193, 0.5);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
      background-color: transparent;
    }
  }
  
  .page-decoration {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 0;
    
    .decoration-bubble {
      position: absolute;
      border-radius: 50%;
      background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
      opacity: 0.05;
      
      &.bubble-1 {
        width: 300px;
        height: 300px;
        bottom: -150px;
        right: -100px;
        animation: float 15s infinite ease-in-out;
      }
      
      &.bubble-2 {
        width: 200px;
        height: 200px;
        top: 10%;
        left: -50px;
        animation: float 18s infinite ease-in-out;
      }
    }
    
    .decoration-paw {
      position: absolute;
      font-size: 40px;
      opacity: 0.03;
      
      &.paw-1 {
        bottom: 5%;
        right: 5%;
        animation: float 15s infinite ease-in-out;
      }
      
      &.paw-2 {
        top: 20%;
        right: 30%;
        animation: rotate 18s infinite linear;
      }
    }
  }
  
  /* 过渡动画 */
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.3s ease;
  }

  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }

  @keyframes float {
    0%, 100% {
      transform: translateY(0);
    }
    50% {
      transform: translateY(-15px);
    }
  }

  @keyframes rotate {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
  </style>