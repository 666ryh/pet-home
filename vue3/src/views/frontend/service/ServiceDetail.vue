<template>
  <div class="service-detail-page">
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="service-detail-content" v-loading="loading">
      <div class="breadcrumb-container">
        <div class="back-button" @click="goBack">
          <el-icon><arrow-left /></el-icon>
          返回列表
        </div>
      </div>
      
      <div v-if="service" class="service-content">
        <div class="service-header">
          <div class="service-title-section">
            <h1 class="service-title">{{ service.name }}</h1>
            <el-tag 
              :type="service.status === 1 ? 'success' : 'info'"
              class="status-tag"
            >
              {{ service.status === 1 ? '可预约' : '暂停服务' }}
            </el-tag>
          </div>
        </div>
        
        <div class="service-main-info">
          <div class="service-card info-card">
            <h2 class="section-title">基本信息</h2>
            <el-descriptions :column="2" border class="info-descriptions">
              <el-descriptions-item label="服务分类">{{ categoryName }}</el-descriptions-item>
              <el-descriptions-item label="服务价格">
                <span class="price">¥{{ service.price }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="服务时长">{{ getDurationText(service.duration) }}</el-descriptions-item>
              <el-descriptions-item label="服务状态">
                <el-tag :type="service.status === 1 ? 'success' : 'info'">
                  {{ service.status === 1 ? '可预约' : '暂停服务' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <div class="service-card description-card">
            <h2 class="section-title">服务介绍</h2>
            <div class="service-description">
              <p>{{ service.description || '暂无介绍' }}</p>
            </div>
          </div>
        </div>
        
        <div class="service-card process-card">
          <h2 class="section-title">服务流程</h2>
          <div class="service-process">
            <el-steps :active="5" finish-status="success" simple>
              <el-step title="预约服务" description="选择服务和预约时间"></el-step>
              <el-step title="确认预约" description="客服确认预约信息"></el-step>
              <el-step title="到店服务" description="按预约时间到店"></el-step>
              <el-step title="服务完成" description="完成服务内容"></el-step>
              <el-step title="评价服务" description="对服务进行评价"></el-step>
            </el-steps>
          </div>
        </div>
        
        <div class="service-card notice-card">
          <h2 class="section-title">注意事项</h2>
          <div class="service-notice">
            <el-alert
              title="预约须知"
              type="warning"
              :closable="false"
              show-icon
            >
              <div class="notice-content">
                <p>1. 请至少提前24小时预约服务，以便我们合理安排时间。</p>
                <p>2. 到店服务时请携带宠物的有效健康证明和疫苗接种记录。</p>
                <p>3. 如需取消或改期，请提前12小时通知我们。</p>
                <p>4. 为保证服务质量，请按预约时间准时到店。</p>
                <p>5. 如有特殊要求，请在预约时备注说明。</p>
              </div>
            </el-alert>
          </div>
        </div>
        
        <div class="book-button-container">
          <el-button 
            type="primary" 
            size="large" 
            class="book-button"
            :disabled="service.status !== 1"
            @click="bookService"
          >
            <el-icon><calendar /></el-icon>
            立即预约
          </el-button>
          
          <div class="booking-tip" v-if="service.status !== 1">
            <el-icon><warning /></el-icon>
            <span>抱歉，该服务当前暂停预约</span>
          </div>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="服务不存在或已下架" class="service-empty"></el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Calendar, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const serviceId = route.params.id

// 数据定义
const loading = ref(false)
const service = ref(null)
const categories = ref([])

// 计算属性
const categoryName = computed(() => {
  if (!service.value || !service.value.categoryId || !categories.value.length) {
    return '未分类'
  }
  
  const category = categories.value.find(c => c.id === service.value.categoryId)
  return category ? category.name : '未分类'
})

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

// 预约服务
const bookService = () => {
  if (!service.value) return
  
  if (service.value.status !== 1) {
    ElMessage.warning('该服务暂不可预约')
    return
  }
  
  router.push({ 
    name: 'ServiceBooking', 
    params: { id: service.value.id },
    query: { serviceName: service.value.name } 
  })
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchServiceDetail()
})
</script>

<style lang="scss" scoped>
.service-detail-page {
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

.service-detail-content {
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

.service-content {
  margin-top: 20px;
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  
  .service-title-section {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .service-title {
      margin: 0;
      font-family: 'Nunito Sans', sans-serif;
      font-size: 32px;
      color: #683e35;
    }
    
    .status-tag {
      font-size: 14px;
      font-weight: 500;
      border-radius: 4px;
      padding: 2px 10px;
    }
  }
}

.service-main-info {
  display: flex;
  gap: 25px;
  margin-bottom: 30px;
  
  @media (max-width: 992px) {
    flex-direction: column;
  }
}

.service-card {
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
}

.info-card {
  flex: 1;
  
  :deep(.el-descriptions__label) {
    color: #666;
    font-weight: 500;
    background-color: #f9f9f9;
  }
  
  :deep(.el-descriptions__content) {
    padding: 12px 15px;
  }
}

.description-card {
  flex: 1;
  
  .service-description {
    line-height: 1.8;
    color: #666;
    font-size: 15px;
  }
}

.process-card, .notice-card {
  margin-bottom: 30px;
  
  .service-process {
    :deep(.el-step__title) {
      font-weight: 500;
      color: #666;
    }
    
    :deep(.el-step__title.is-success) {
      color: #67c23a;
    }
    
    :deep(.el-step__description) {
      font-size: 13px;
    }
  }
  
  .service-notice {
    :deep(.el-alert) {
      background-color: rgba(255, 238, 147, 0.2);
      border: 1px solid rgba(255, 167, 38, 0.2);
      
      .el-alert__icon {
        color: #E6A23C;
      }
      
      .el-alert__title {
        color: #683e35;
        font-weight: 600;
      }
    }
    
    .notice-content {
      padding: 10px;
      color: #666;
      
      p {
        margin: 8px 0;
        position: relative;
        padding-left: 5px;
      }
    }
  }
}

.price {
  color: #f56c6c;
  font-size: 22px;
  font-weight: bold;
}

.book-button-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 40px;
  
  .book-button {
    min-width: 250px;
    height: 50px;
    font-size: 16px;
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
  
  .booking-tip {
    margin-top: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
    color: #E6A23C;
    font-size: 14px;
    
    .el-icon {
      font-size: 16px;
    }
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

@media (max-width: 768px) {
  .service-header {
    .service-title-section {
      .service-title {
        font-size: 24px;
      }
    }
  }
  
  .service-card {
    padding: 20px;
  }
  
  .info-card {
    :deep(.el-descriptions) {
      .el-descriptions__body {
        display: block;
      }
      
      .el-descriptions__table {
        display: block;
      }
      
      .el-descriptions__cell {
        display: block;
        width: 100%;
      }
    }
  }
}
</style> 