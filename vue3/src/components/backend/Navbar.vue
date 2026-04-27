<template>
  <div class="navbar">
    <div class="left-menu">
      <el-icon class="hamburger" :size="20" @click="toggleSidebar">
        <component :is="appStore.sidebarCollapsed ? Expand : Fold" />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <div class="right-menu">
      <div class="right-menu-item" @click="toggleFullScreen">
        <el-icon :size="20">
          <component :is="isFullscreen ? Aim : FullScreen" />
        </el-icon>
      </div>
      
      <el-dropdown trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :size="32" :src="avatarUrl">
            {{ userInfo?.name?.charAt(0)?.toUpperCase() || userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
          <span class="user-name">{{ userInfo?.name || userInfo?.username || '用户' }}</span>
          <el-icon class="el-icon--right">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { ElMessageBox } from 'element-plus'
import { Expand, Fold, ArrowDown, User, SwitchButton, FullScreen, Aim } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const baseAPI = process.env.VUE_APP_BASE_API || '/api'
const userInfo = computed(() => userStore.userInfo)
const isFullscreen = ref(false)

const toggleSidebar = () => {
  appStore.toggleSidebar()
}
const avatarUrl = computed(() => {
  return userInfo.value?.avatar ? baseAPI + userInfo.value.avatar : '';
})
const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
}

// 监听全屏状态变化
const fullscreenChangeHandler = () => {
  isFullscreen.value = !!document.fullscreenElement
}

document.addEventListener('fullscreenchange', fullscreenChangeHandler)

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('fullscreenchange', fullscreenChangeHandler)
})



const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #ffffff;
  box-shadow: 0 1px 8px rgba(255, 182, 193, 0.15);
  z-index: 10;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, #FFB6C1, #FFEE93);
    opacity: 0.7;
  }

  .left-menu {
    display: flex;
    align-items: center;
    gap: 16px;

    .hamburger {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      padding: 8px;
      border-radius: 8px;
      color: #683e35;
      height: 32px;
      width: 32px;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(255, 182, 193, 0.1);
        transform: scale(1.05);
      }
    }

    :deep(.el-breadcrumb__inner) {
      color: #683e35;
      line-height: 32px;
      font-family: 'Open Sans', sans-serif;
      
      &.is-link {
        color: #6E4C1E;
        opacity: 0.8;
        
        &:hover {
          color: #FFA726;
          opacity: 1;
        }
      }
    }
  }

  .right-menu {
    display: flex;
    align-items: center;
    gap: 12px;

    .right-menu-item {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #683e35;
      border-radius: 8px;
      transition: all 0.3s;
      height: 36px;
      width: 36px;
      
      &:hover {
        background: rgba(255, 182, 193, 0.1);
        color: #FFA726;
        transform: scale(1.05);
      }
    }
    
    .avatar-wrapper {
      display: flex;
      align-items: center;
      padding: 4px 12px;
      height: 36px;
      cursor: pointer;
      border-radius: 18px;
      transition: all 0.3s;
      
      &:hover {
        background: rgba(255, 182, 193, 0.1);
      }
      
      :deep(.el-avatar) {
        background: linear-gradient(135deg, #FFB6C1, #FFEE93);
        color: #683e35;
        font-family: 'Nunito Sans', sans-serif;
        font-weight: 600;
      }
      
      .user-name {
        margin: 0 8px;
        font-size: 14px;
        color: #683e35;
        font-family: 'Open Sans', sans-serif;
        line-height: 32px;
      }

      .el-icon {
        color: #683e35;
        opacity: 0.7;
        display: flex;
        align-items: center;
      }
    }
  }

  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    height: 40px;
    color: #683e35;
    font-family: 'Open Sans', sans-serif;
    
    &:hover {
      background-color: rgba(255, 182, 193, 0.1);
      color: #FFA726;
    }
    
    .el-icon {
      margin-right: 4px;
      display: flex;
      align-items: center;
      color: inherit;
    }
  }
}
</style> 