<template>
  <Auth 
    :formData="loginForm" 
    :rules="rules" 
    :loading="loading"
    submitText="登录"
    @submit="handleSubmit"
  >
    <template #form-items>
      <div class="welcome-text">欢迎回来！</div>
      <p class="form-desc">登录您的账号，开启宠物之家的奇妙旅程</p>
      
      <el-form-item prop="username">
        <el-input 
          v-model="loginForm.username"
          :prefix-icon="User"
          placeholder="请输入用户名">
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input 
          v-model="loginForm.password"
          :prefix-icon="Lock"
          type="password"
          placeholder="请输入密码"
          @keyup.enter="handleSubmit">
        </el-input>
      </el-form-item>
    </template>

    <template #auth-links>
      <div class="links-container">
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </template>
  </Auth>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { User, Lock } from '@element-plus/icons-vue'
import { setBackRoutes } from '@/router'
import Auth from './Auth.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleSubmit = (form) => {
  formRef.value = form.value
  loginFormRef.value = form.value
  handleLogin()
}

const loginFormRef = ref(null)

const handleLogin = () => {
  loginFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        // 统一使用用户登录接口
        const res = await request.post("/user/login", loginForm, {
          successMsg: "登录成功",
          showDefaultMsg: true,
          onSuccess: async (data) => {
            // 先设置用户信息
            userStore.setUserInfo(data)
            localStorage.setItem('frontUser', JSON.stringify(data))
            // 根据返回的角色决定跳转路径
            if (data.roleCode !== 'USER') {
              // 管理员登录，设置菜单和路由
              userStore.setMenus(data.menuList || [])
              setBackRoutes()
              await router.isReady()
              
              // 使用 nextTick 确保路由变更已完成
              setTimeout(() => {
                router.push(route.query.redirect || '/back/dashboard')
              }, 100)
            } else {
              // 普通用户登录，直接跳转到前台
              const redirect = route.query.redirect || '/'
              router.push(redirect)
            }
          },
          onError: (error) => {
            console.error('登录失败:', error)
          }
        })
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.welcome-text {
  text-align: center;
  font-family: 'Nunito Sans', sans-serif;
  font-size: 26px;
  color: #683e35;
  margin-bottom: 10px;
  font-weight: 600;
}

.form-desc {
  text-align: center;
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  color: #683e35;
  opacity: 0.7;
  margin-bottom: 30px;
}

.links-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;

  .divider {
    color: #ddd;
    font-size: 14px;
  }

  a {
    font-size: 14px;
    
    &.forget-link {
      color: #683e35;
      opacity: 0.8;
      
      &:hover {
        opacity: 1;
      }
    }
    
    &.register-link {
      color: #FFA726;
      font-weight: 500;
      
      &:hover {
        color: #FFB6C1;
      }
    }
  }
}
</style> 