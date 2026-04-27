<template>
  <div class="service-list-page">
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="service-list-content">
      <div class="page-header">
        <h1 class="page-title">宠物服务</h1>
        <p class="page-subtitle">为您的爱宠提供专业的护理服务</p>
      </div>
      
      <div class="filter-section">
        <div class="search-section">
          <el-input
            v-model="searchText"
            placeholder="搜索服务"
            class="search-input"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <div class="category-tabs">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="custom-tabs">
          <el-tab-pane label="全部服务" name="all"></el-tab-pane>
          <el-tab-pane 
            v-for="category in categories" 
            :key="category.id" 
            :label="category.name" 
            :name="String(category.id)"
          ></el-tab-pane>
        </el-tabs>
      </div>

      <div class="service-list-container" v-loading="loading">
        <transition-group name="service-fade" tag="div" class="service-grid">
          <div v-for="service in services" :key="service.id" class="service-item">
            <div class="service-card">
              <div class="service-card-content">
                <div class="service-info">
                  <div class="service-title-row">
                    <h3 class="service-name">{{ service.name }}</h3>
                    <div class="service-status" v-if="service.status === 0">暂停服务</div>
                  </div>
                  
                  <div class="service-meta">
                    <div class="service-price">¥{{ service.price }}</div>
                    <div class="service-duration">
                      <el-icon><timer /></el-icon>
                      {{ getDurationText(service.duration) }}
                    </div>
                  </div>
                  
                  <div class="service-description">{{ service.description }}</div>
                </div>
                
                <div class="service-actions">
                  <el-button 
                    type="primary" 
                    class="booking-button"
                    :disabled="service.status === 0"
                    @click.stop="bookService(service)">
                    立即预约
                  </el-button>
                  <el-button 
                    class="detail-button"
                    @click.stop="viewServiceDetail(service.id)">
                    查看详情
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </transition-group>
        
        <el-empty 
          v-if="services.length === 0 && !loading" 
          description="暂无服务"
          class="service-empty" />
      </div>

      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          background
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[8, 12, 24, 36]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Timer } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 数据定义
const loading = ref(false)
const services = ref([])
const total = ref(0)
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const searchText = ref('')
const selectedCategory = ref('')
const activeTab = ref('all')

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

// 获取服务列表
const fetchServices = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      name: searchText.value
    }
    
    // 根据分类过滤
    if (activeTab.value !== 'all') {
      params.categoryId = activeTab.value
    }
    
    await request.get('/service/page', params, {
      onSuccess: (res) => {
        services.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取服务列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查看服务详情
const viewServiceDetail = (id) => {
  router.push({ name: 'ServiceDetail', params: { id } })
}

// 预约服务
const bookService = (service) => {
  if (service.status === 0) {
    ElMessage.warning('该服务暂停预约')
    return
  }
  
  router.push({ 
    name: 'ServiceBooking', 
    params: { id: service.id },
    query: { serviceName: service.name } 
  })
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchServices()
}

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchServices()
}

// 处理当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchServices()
}

// 处理分类标签点击
const handleTabClick = () => {
  selectedCategory.value = activeTab.value === 'all' ? '' : activeTab.value
  currentPage.value = 1
  fetchServices()
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

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchServices()
})
</script>

<style lang="scss" scoped>
.service-list-page {
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

.service-list-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  
  .page-title {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 32px;
    color: #683e35;
    margin-bottom: 8px;
  }
  
  .page-subtitle {
    font-size: 16px;
    color: #666;
  }
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  
  .search-section {
    max-width: 500px;
    margin: 0 auto;
  }
  
  .search-input {
    :deep(.el-input__wrapper) {
      background-color: #f9f9f9;
      
      &:hover, &.is-focus {
        box-shadow: 0 0 0 1px #FFB6C1;
      }
    }
    
    :deep(.el-input__prefix-inner) {
      color: #FFB6C1;
    }
  }
}

.category-tabs {
  margin-bottom: 20px;
  
  .custom-tabs {
    :deep(.el-tabs__header) {
      border-bottom: none;
      margin-bottom: 20px;
    }
    
    :deep(.el-tabs__nav-wrap::after) {
      background-color: #f0f0f0;
    }
    
    :deep(.el-tabs__item) {
      color: #666;
      font-size: 15px;
      
      &.is-active {
        color: #FFB6C1;
        font-weight: 600;
      }
      
      &:hover {
        color: #FFB6C1;
      }
    }
    
    :deep(.el-tabs__active-bar) {
      background-color: #FFB6C1;
      height: 3px;
      border-radius: 3px;
    }
  }
}

.service-fade-enter-active, .service-fade-leave-active {
  transition: all 0.5s ease;
}

.service-fade-enter-from, .service-fade-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.service-item {
  display: flex;
  cursor: pointer;
}

.service-card {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  }
}

.service-card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.service-info {
  flex: 1;
}

.service-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  
  .service-name {
    margin: 0;
    font-size: 20px;
    color: #683e35;
    font-weight: 600;
  }
  
  .service-status {
    background-color: #f56c6c;
    color: white;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
  }
}

.service-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #eee;
  
  .service-price {
    font-size: 22px;
    font-weight: bold;
    color: #f56c6c;
  }
  
  .service-duration {
    display: flex;
    align-items: center;
    color: #909399;
    font-size: 14px;
    
    .el-icon {
      margin-right: 5px;
      color: #FFB6C1;
    }
  }
}

.service-description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  margin-bottom: 20px;
}

.service-actions {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
  
  .booking-button, .detail-button {
    flex: 1;
    height: 40px;
    border-radius: 8px;
    border: none;
    font-size: 14px;
    transition: all 0.3s ease;
    
    &:hover:not(:disabled) {
      transform: translateY(-2px);
    }
    
    &:disabled {
      background-color: #f0f0f0;
      color: #999;
      cursor: not-allowed;
    }
  }
  
  .booking-button {
    background-color: #FFA726;
    color: white;
    
    &:hover:not(:disabled) {
      background-color: darken(#FFA726, 5%);
      box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
    }
  }
  
  .detail-button {
    background-color: white;
    color: #666;
    border: 1px solid #dcdfe6;
    
    &:hover {
      color: #FFB6C1;
      border-color: #FFB6C1;
    }
  }
}

.service-empty {
  margin-top: 50px;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    --el-pagination-button-bg-color: white;
    --el-pagination-hover-color: #FFB6C1;
    
    .el-pagination__jump {
      color: #683e35;
    }
    
    button:not(:disabled):hover {
      color: #FFB6C1;
    }
    
    .is-active {
      background-color: #FFB6C1 !important;
      color: white !important;
    }
  }
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
  .page-header {
    .page-title {
      font-size: 24px;
    }
    
    .page-subtitle {
      font-size: 14px;
    }
  }
  
  .service-title-row {
    .service-name {
      font-size: 18px;
    }
  }
  
  .service-meta {
    .service-price {
      font-size: 20px;
    }
  }
  
  .service-card {
    padding: 20px;
  }
}
</style> 