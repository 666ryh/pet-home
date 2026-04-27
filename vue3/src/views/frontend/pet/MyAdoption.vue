<template>
  <div class="adoption-page">
    <!-- 页面装饰元素 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="adoption-content">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">我的领养申请</h1>
        <p class="page-subtitle">查看和管理您的宠物领养申请记录</p>
      </div>
      
      <el-card shadow="never" class="adoption-card">
        <template #header>
          <div class="card-header">
            <h2>申请记录</h2>
            <div class="card-actions">
              <el-radio-group v-model="currentStatus" @change="handleStatusChange" size="small">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button label="已申请">已申请</el-radio-button>
                <el-radio-button label="审核中">审核中</el-radio-button>
                <el-radio-button label="已通过">已通过</el-radio-button>
                <el-radio-button label="已拒绝">已拒绝</el-radio-button>
              </el-radio-group>
              <el-button type="primary" size="small" @click="handleRefresh" :icon="Refresh">刷新</el-button>
            </div>
          </div>
        </template>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        
        <!-- 错误状态 -->
        <div v-else-if="loadError" class="error-container">
          <el-result
            icon="error"
            title="数据加载失败"
            sub-title="无法获取领养申请记录，请检查网络连接或稍后重试"
          >
            <template #extra>
              <el-button type="primary" @click="handleRefresh">重新加载</el-button>
            </template>
          </el-result>
        </div>
        
        <!-- 空数据状态 -->
        <div v-else-if="adoptionList.length === 0" class="empty-container">
          <el-empty description="暂无申请记录" :image-size="200">
            <template #description>
              <p>您还没有提交过领养申请</p>
              <p class="empty-hint">您可以浏览可领养的宠物，找到心仪的宠物后提交申请</p>
            </template>
            <el-button type="primary" class="action-btn" @click="goToAdoption">
              <el-icon><Pet /></el-icon>
              去看看可爱的宠物
            </el-button>
          </el-empty>
        </div>
        
        <!-- 数据列表 -->
        <div v-else class="adoption-list">
          <el-timeline>
            <el-timeline-item
              v-for="item in adoptionList"
              :key="item.id"
              :timestamp="formatDate(item.createTime)"
              :type="getStatusType(item.status)"
            >
              <el-card class="adoption-item">
                <div class="adoption-header">
                  <div class="adoption-info">
                    <h3>申请ID: {{ item.id }}</h3>
                    <el-tag :type="getStatusType(item.status)" effect="light">{{ item.status }}</el-tag>
                  </div>
                  <div class="adoption-actions">
                    <el-button 
                      v-if="item.status === '已申请' || item.status === '审核中'"
                      type="danger" 
                      plain
                      size="small" 
                      @click="handleCancel(item)"
                    >
                      <el-icon><Close /></el-icon>
                      取消申请
                    </el-button>
                    <el-button type="primary" size="small" @click="viewPet(item.petId)">
                      <el-icon><View /></el-icon>
                      查看宠物
                    </el-button>
                  </div>
                </div>
                
                <div class="adoption-details">
                  <div class="detail-item">
                    <span class="detail-label"><el-icon><Document /></el-icon> 申请理由:</span>
                    <span class="detail-value">{{ item.applyReason || '无' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label"><el-icon><Phone /></el-icon> 联系电话:</span>
                    <span class="detail-value">{{ item.contactPhone || '无' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label"><el-icon><Location /></el-icon> 联系地址:</span>
                    <span class="detail-value">{{ item.address || '无' }}</span>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
        
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            background
            layout="prev, pager, next, jumper"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'
import { useUserStore } from '@/store/user'
import { Refresh, Pet, Close, View, Document, Phone, Location } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 格式化日期函数
const formatDate = (date) => {
  if (!date) return '';
  try {
    return DateUtils.formatDateTime(date);
  } catch (error) {
    console.error('日期格式化错误:', error, date);
    return date;
  }
}

const loading = ref(false)
const loadError = ref(false)
const adoptionList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const currentStatus = ref('')

// 申请状态保持不变，仍然有4种状态
const statusOptions = ['已申请', '审核中', '已通过', '已拒绝']

// 获取领养申请列表
const fetchAdoptions = async () => {
  // 确保初始化为空数组而不是undefined
  adoptionList.value = []
  
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('您需要登录后才能查看领养申请记录，是否前往登录？', '提示', {
      confirmButtonText: '前往登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      });
    }).catch(() => {
      router.push('/');
    });
    return;
  }
  
  loading.value = true
  loadError.value = false
  try {
    await request.get('/adoption/user', {
      status: currentStatus.value,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        // 确保响应数据存在且包含记录
        if (res && res.records) {
          adoptionList.value = res.records
          total.value = res.total || 0
        } else {
          // 如果没有数据，设置为空数组
          adoptionList.value = []
          total.value = 0
        }
      }
    })
  } catch (error) {
    console.error('获取领养申请列表失败:', error)
    // 确保在出错时也将列表设置为空数组
    adoptionList.value = []
    total.value = 0
    loadError.value = true
    ElMessage.error('获取领养申请记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const handleRefresh = () => {
  fetchAdoptions()
}

// 状态筛选变更
const handleStatusChange = () => {
  currentPage.value = 1
  fetchAdoptions()
}

// 处理分页事件
const handlePageChange = (page) => {
  currentPage.value = page
  fetchAdoptions()
}

// 查看宠物
const viewPet = (petId) => {
  router.push(`/pet/${petId}`)
}

// 前往领养页面
const goToAdoption = () => {
  router.push('/pet');
};

// 取消申请
const handleCancel = (item) => {
  ElMessageBox.confirm('确定要取消该领养申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/adoption/${item.id}`, {
        successMsg: '申请已取消',
        onSuccess: () => {
          fetchAdoptions() // 刷新列表
        }
      })
    } catch (error) {
      console.error('取消申请失败:', error)
    }
  }).catch(() => {})
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case '已申请':
      return 'info'
    case '审核中':
      return 'warning'
    case '已通过':
      return 'success'
    case '已拒绝':
      return 'danger'
    default:
      return 'info'
  }
}

onMounted(() => {
  fetchAdoptions()
})
</script>

<style lang="scss" scoped>
.adoption-page {
  position: relative;
  min-height: 100vh;
  background-color: #FFF9E6;
  padding: 30px 20px 60px;
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
      animation: float 15s infinite ease-in-out;
    }
    
    &.bubble-2 {
      width: 200px;
      height: 200px;
      bottom: 10%;
      right: -50px;
      animation: float 18s infinite ease-in-out;
    }
    
    &.bubble-3 {
      width: 150px;
      height: 150px;
      top: 40%;
      right: 10%;
      animation: float 12s infinite ease-in-out;
    }
  }
  
  .decoration-paw {
    position: absolute;
    font-size: 40px;
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

.adoption-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
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

.adoption-card {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05) !important;
  margin-bottom: 30px;
  
  :deep(.el-card__header) {
    padding: 20px 25px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #683e35;
  }
  
  .card-actions {
    display: flex;
    gap: 10px;
    align-items: center;
    
    :deep(.el-radio-button__inner) {
      padding: 8px 15px;
    }
    
    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      background-color: #FFA726;
      border-color: #FFA726;
      box-shadow: -1px 0 0 0 #FFA726;
    }
  }
}

.loading-container,
.empty-container,
.error-container {
  padding: 40px 0;
  text-align: center;
  border-radius: 8px;
  background-color: #f9f9f9;
  margin: 20px 0;
}

.error-container {
  background-color: #fff0f0;
}

.empty-hint {
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
  margin-bottom: 20px;
}

.action-btn {
  background-color: #FFA726;
  border-color: #FFA726;
  transition: all 0.3s ease;
  
  &:hover {
    background-color: darken(#FFA726, 5%);
    border-color: darken(#FFA726, 5%);
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.adoption-list {
  :deep(.el-timeline) {
    padding-left: 0;
    
    .el-timeline-item__node {
      background-color: #FFA726;
    }
    
    .el-timeline-item__node--primary {
      background-color: #409EFF;
    }
    
    .el-timeline-item__node--success {
      background-color: #67C23A;
    }
    
    .el-timeline-item__node--warning {
      background-color: #E6A23C;
    }
    
    .el-timeline-item__node--danger {
      background-color: #F56C6C;
    }
    
    .el-timeline-item__tail {
      border-left: 2px solid #e8e8e8;
    }
    
    .el-timeline-item__wrapper {
      padding-left: 20px;
    }
    
    .el-timeline-item__timestamp {
      color: #909399;
      font-size: 13px;
    }
  }
}

.adoption-item {
  margin-bottom: 10px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }
}

.adoption-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  
  .adoption-info {
    display: flex;
    align-items: center;
    gap: 15px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .adoption-actions {
    display: flex;
    gap: 10px;
    
    .el-button {
      border-radius: 20px;
      transition: all 0.3s ease;
      
      .el-icon {
        margin-right: 5px;
      }
      
      &:hover {
        transform: translateY(-3px);
      }
    }
    
    .el-button--primary {
      background-color: #FFA726;
      border-color: #FFA726;
      
      &:hover {
        background-color: darken(#FFA726, 5%);
        border-color: darken(#FFA726, 5%);
        box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
      }
    }
  }
}

.adoption-details {
  margin-top: 15px;
  
  .detail-item {
    margin: 10px 0;
    display: flex;
    align-items: flex-start;
    line-height: 1.5;
    
    .detail-label {
      width: 90px;
      color: #666;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 5px;
      
      .el-icon {
        color: #FFA726;
      }
    }
    
    .detail-value {
      flex: 1;
      color: #333;
    }
  }
}

.pagination-container {
  margin-top: 30px;
  text-align: center;
  
  :deep(.el-pagination) {
    --el-pagination-button-bg-color: #fff;
    --el-pagination-button-color: #683e35;
    --el-pagination-button-disabled-bg-color: #f4f4f5;
    --el-pagination-button-disabled-color: #a8abb2;
    --el-pagination-hover-color: #FFA726;
    
    .el-pagination__jump,
    .el-pagination__total {
      color: #666;
    }
    
    .el-pager li.is-active {
      background-color: #FFA726;
      color: white;
    }
    
    .el-pager li:hover {
      color: #FFA726;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

@media screen and (max-width: 768px) {
  .page-header {
    .page-title {
      font-size: 26px;
    }
    
    .page-subtitle {
      font-size: 14px;
    }
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    
    .card-actions {
      width: 100%;
      justify-content: space-between;
    }
  }
  
  .adoption-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    
    .adoption-actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
  
  .detail-item {
    flex-direction: column;
    
    .detail-label {
      width: 100%;
      margin-bottom: 5px;
    }
  }
}
</style> 