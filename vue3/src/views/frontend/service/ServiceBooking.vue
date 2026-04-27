<template>
  <div class="service-booking-page">
    <!-- 页面装饰 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="booking-content" v-loading="loading">
      <div class="breadcrumb-container">
        <div class="back-button" @click="goBack">
          <el-icon><arrow-left /></el-icon>
          返回详情
        </div>
      </div>
      
      <div class="page-header">
        <h1 class="page-title">服务预约</h1>
        <p class="page-subtitle">填写信息，预约您的宠物服务</p>
      </div>
      
      <div v-if="service" class="booking-main">
        <div class="booking-grid">
          <div class="service-info">
            <h2 class="section-title">服务信息</h2>
            <div class="service-header">
              <span class="service-category">{{ categoryName }}</span>
            </div>
            <h2>{{ service.name }}</h2>
            <div class="service-meta">
              <div class="meta-item">
                <el-icon class="icon"><Calendar /></el-icon>
                <span>服务时长: {{ getDurationText(service.duration) }}</span>
              </div>
            </div>
            <div class="service-description" v-if="service.description">
              {{ service.description }}
            </div>
            <div class="service-price">
              价格: ¥{{ service.price }}
            </div>
          </div>
          
          <div class="booking-form">
            <h2>预约信息</h2>
            <el-form 
              ref="formRef" 
              :model="form" 
              :rules="rules" 
              label-width="120px" 
              status-icon
            >
              <el-form-item label="宠物名称" prop="petName">
                <el-input v-model="form.petName" placeholder="请输入宠物名称">
                  <template #prefix>
                    <el-icon><pet /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="预约时间" prop="appointmentTime">
               <el-date-picker
                  v-model="form.appointmentTime"
                  type="datetime"
                  placeholder="选择预约日期和时间"
                  :disabled-date="disabledDate"
                  format="YYYY-MM-DD HH:mm"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                >
                  <template #prefix>
                    <el-icon><calendar /></el-icon>
                  </template>
                </el-date-picker>
              </el-form-item>
              
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话">
                  <template #prefix>
                    <el-icon><phone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="特殊要求" prop="requirements">
                <el-input
                  v-model="form.requirements"
                  type="textarea"
                  placeholder="如有特殊要求请在此说明（选填）"
                  :rows="4"
                />
              </el-form-item>
              
              <el-form-item class="form-footer">
                <el-button type="primary" class="submit-btn" @click="submitForm" :loading="submitting">
                  <el-icon><check /></el-icon>
                  提交预约
                </el-button>
                <el-button class="reset-btn" @click="resetForm">
                  <el-icon><refresh /></el-icon>
                  重置表单
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <div class="notice-card">
          <h3>
            <el-icon class="icon"><Warning /></el-icon>
            预约须知
          </h3>
          <ul>
            <li>请至少提前24小时预约服务，以便我们合理安排时间。</li>
            <li>到店服务时请携带宠物的有效健康证明和疫苗接种记录。</li>
            <li>如需取消或改期，请提前12小时通知我们。</li>
            <li>为保证服务质量，请按预约时间准时到店。</li>
          </ul>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="服务不存在或已下架" class="service-empty"></el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Pet, Calendar, Clock, Phone, Check, Refresh, Warning } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const serviceId = route.params.id

// 数据定义
const loading = ref(false)
const submitting = ref(false)
const service = ref(null)
const categories = ref([])
const formRef = ref(null)

// 表单数据
const form = ref({
  petName: '',
  appointmentTime: '',
  contactPhone: '',
  requirements: ''
})

// 表单校验规则
const rules = {
  petName: [
    { required: true, message: '请输入宠物名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  appointmentTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 计算分类名称
const categoryName = computed(() => {
  if (!service.value || !service.value.categoryId || !categories.value.length) {
    return '未分类'
  }
  
  const category = categories.value.find(c => c.id === service.value.categoryId)
  return category ? category.name : '未分类'
})

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

// 获取服务分类
const fetchCategories = async () => {
  try {
    await request.get('/service/category/list', null, {
      onSuccess: (res) => {
        categories.value = res
      }
    })
  } catch (error) {
    console.error('获取服务分类失败:', error)
  }
}

// 获取服务详情
const fetchServiceDetail = async () => {
  loading.value = true
  try {
    await request.get(`/service/${serviceId}`, null, {
      onSuccess: (res) => {
        service.value = res
        
        // 如果服务不可用，提示并返回
        if (res.status !== 1) {
          ElMessage.warning('该服务暂不可预约')
          setTimeout(() => {
            router.go(-1)
          }, 1500)
        }
      }
    })
  } catch (error) {
    console.error('获取服务详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理持续时间显示
const getDurationText = (minutes) => {
  if (!minutes) return '时长未知'
  
  if (minutes < 60) {
    return `${minutes}分钟`
  } else if (minutes === 60) {
    return '1小时'
  } else if (minutes < 1440) {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
  } else {
    const days = Math.floor(minutes / 1440)
    return `${days}天`
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!userStore.isLoggedIn) {
        ElMessageBox.confirm(
          '预约服务需要先登录，是否立即前往登录？',
          '提示',
          {
            confirmButtonText: '去登录',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          router.push({
            path: '/login',
            query: { redirect: route.fullPath }
          })
        }).catch(() => {})
        return
      }
      
      submitting.value = true
      try {
        // 日期时间已经是 yyyy-MM-dd HH:mm:ss 格式，直接使用
        const appointmentData = {
          userId: userStore.userInfo.id,
          serviceId: service.value.id,
          appointmentTime: form.value.appointmentTime,
          petName: form.value.petName,
          requirements: form.value.requirements,
          status: '已预约', // 初始状态
          contactPhone: form.value.contactPhone
        }
        
        await request.post('/service/appointment', appointmentData, {
          successMsg: '预约成功，请等待确认',
          onSuccess: () => {
            router.push('/service/my-appointments')
          }
        })
      } catch (error) {
        console.error('预约服务失败:', error)
      } finally {
        submitting.value = false
      }
    } else {
      ElMessage.warning('请完善表单信息')
      return false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchServiceDetail()
  
  // 如果已登录，则填充联系电话
  if (userStore.isLoggedIn && userStore.userInfo) {
    form.value.contactPhone = userStore.userInfo.phone || ''
  }
})
</script>

<style lang="scss" scoped>
.service-booking-page {
  min-height: 100vh;
  background-color: #FFF9E6;
  padding: 30px 20px 60px;
  position: relative;
  overflow: hidden;
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
      top: -150px;
      left: -100px;
    }
    
    &.bubble-2 {
      width: 200px;
      height: 200px;
      bottom: 10%;
      right: -50px;
    }
    
    &.bubble-3 {
      width: 150px;
      height: 150px;
      top: 40%;
      right: 10%;
    }
  }
  
  .decoration-paw {
    position: absolute;
    font-size: 30px;
    opacity: 0.1;
    
    &.paw-1 {
      top: 20%;
      left: 5%;
      animation: float 15s infinite ease-in-out;
    }
    
    &.paw-2 {
      bottom: 10%;
      right: 10%;
      animation: float 18s infinite ease-in-out reverse;
    }
  }
}

.booking-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.breadcrumb-container {
  max-width: 1200px;
  margin: 0 auto 20px;
  position: relative;
  z-index: 2;
  
  .back-button {
    display: inline-flex;
    align-items: center;
    padding: 8px 16px;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    font-weight: 500;
    color: #683e35;
    transition: all 0.3s ease;
    
    i {
      margin-right: 8px;
    }
    
    &:hover {
      transform: translateX(-3px);
      background-color: #FFF0F0;
    }
  }
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  
  .page-title {
    margin: 0;
    font-family: 'Nunito Sans', sans-serif;
    font-size: 32px;
    color: #683e35;
  }
  
  .page-subtitle {
    margin: 10px 0 0;
    color: #666;
    font-size: 16px;
  }
}

.booking-main {
  margin-top: 20px;
}

.booking-grid {
  display: flex;
  gap: 25px;
  margin-bottom: 30px;
  
  @media (max-width: 992px) {
    flex-direction: column;
  }
  
  .service-info {
    flex: 1;
    background: white;
    border-radius: 12px;
    padding: 25px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    transition: transform 0.3s ease;
    
    &:hover {
      transform: translateY(-5px);
    }
    
    .section-title {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 20px;
      color: #683e35;
      margin: 0 0 20px;
      position: relative;
      padding-left: 15px;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 4px;
        background: #FFB6C1;
        border-radius: 2px;
      }
    }
    
    .service-header {
      display: flex;
      align-items: center;
      margin-bottom: 15px;
      
      .service-category {
        display: inline-block;
        padding: 4px 12px;
        background-color: rgba(255, 182, 193, 0.2);
        color: #ff6b88;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 500;
      }
    }
    
    h2 {
      font-size: 24px;
      color: #683e35;
      margin: 0 0 15px;
    }
    
    .service-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      margin-bottom: 15px;
      
      .meta-item {
        display: flex;
        align-items: center;
        color: #666;
        font-size: 14px;
        
        .icon {
          margin-right: 6px;
          color: #683e35;
        }
      }
    }
    
    .service-description {
      line-height: 1.8;
      color: #666;
      font-size: 15px;
      margin-bottom: 20px;
    }
    
    .service-price {
      color: #f56c6c;
      font-size: 22px;
      font-weight: bold;
    }
  }
  
  .booking-form {
    flex: 1;
    background: white;
    border-radius: 12px;
    padding: 25px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    
    h2 {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 20px;
      color: #683e35;
      margin: 0 0 25px;
      position: relative;
      padding-left: 15px;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 4px;
        background: #FFB6C1;
        border-radius: 2px;
      }
    }
    
    :deep(.el-form-item__label) {
      color: #666;
      font-weight: 500;
    }
    
    .form-footer {
      margin-top: 30px;
      display: flex;
      justify-content: center;
      gap: 15px;
    }
  }
}

.notice-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
  border-left: 4px solid #E6A23C;
  background-color: rgba(255, 248, 225, 0.5);
  
  h3 {
    color: #F57C00;
    margin: 0 0 15px;
    font-size: 18px;
    display: flex;
    align-items: center;
    
    .icon {
      margin-right: 8px;
    }
  }
  
  ul {
    padding-left: 20px;
    margin: 0;
    
    li {
      margin-bottom: 8px;
      color: #555;
      line-height: 1.5;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.submit-btn {
  min-width: 120px;
  height: 40px;
  background-color: #FFA726;
  color: white;
  border: none;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
  &:hover:not(:disabled) {
    transform: translateY(-3px);
    background-color: darken(#FFA726, 5%);
    box-shadow: 0 4px 15px rgba(255, 167, 38, 0.3);
  }
  
  &:disabled {
    background-color: #f0f0f0;
    color: #999;
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.reset-btn {
  min-width: 120px;
  height: 40px;
  color: #666;
  border-color: #dcdfe6;
  border-radius: 8px;
  transition: all 0.3s ease;
  
  &:hover, &:focus {
    color: #683e35;
    border-color: #683e35;
  }
}

.service-empty {
  margin-top: 100px;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

// 表单样式覆盖
:deep(.el-input__wrapper), :deep(.el-textarea__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  
  &:hover {
    box-shadow: 0 0 0 1px #FFA726 inset;
  }
  
  &.is-focus {
    box-shadow: 0 0 0 1px #FFA726 inset;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .service-booking-page {
    padding: 20px 16px;
  }
  
  .page-header {
    .page-title {
      font-size: 24px;
    }
  }
  
  .booking-grid {
    .service-info, .booking-form {
      padding: 20px;
    }
  }
}
</style> 