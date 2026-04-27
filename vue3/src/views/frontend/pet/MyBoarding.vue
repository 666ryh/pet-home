<template>
  <div class="boarding-page">
    <!-- 页面装饰元素 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="boarding-content">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">我的寄养记录</h1>
        <p class="page-subtitle">查看和管理您的宠物寄养服务记录</p>
      </div>

      <el-card class="boarding-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2>寄养记录</h2>
            <div class="card-actions">
              <el-form :inline="true" :model="searchForm" class="filter-form">
                <el-form-item label="寄养状态">
                  <el-select v-model="searchForm.status" placeholder="寄养状态" clearable>
                    <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">
                    <el-icon><Search /></el-icon>查询
                  </el-button>
                  <el-button @click="resetSearch">
                    <el-icon><RefreshRight /></el-icon>重置
                  </el-button>
                  <el-button type="success" @click="handleRefresh" :loading="refreshLoading">
                    <el-icon><Refresh /></el-icon>刷新
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </template>

        <!-- 数据加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- 错误状态 -->
        <div v-else-if="loadError" class="error-container">
          <el-result
            icon="error"
            title="数据加载失败"
            sub-title="无法获取寄养记录，请检查网络连接或稍后重试"
          >
            <template #extra>
              <el-button type="primary" @click="handleRefresh">重新加载</el-button>
            </template>
          </el-result>
        </div>

        <!-- 空数据状态 -->
        <div v-else-if="boardingList.length === 0" class="empty-container">
          <el-empty 
            description="暂无寄养记录" 
            :image-size="200"
          >
            <template #description>
              <p>您还没有任何寄养记录</p>
              <p class="empty-hint">您可以立即预约寄养服务，为您的宠物提供专业照顾</p>
            </template>
            <el-button type="primary" class="action-btn" @click="goToBooking">
              <el-icon><HomeFilled /></el-icon>
              立即预约
            </el-button>
          </el-empty>
        </div>

        <!-- 数据展示 -->
        <el-table
          v-else
          :data="boardingList"
          border
          style="width: 100%"
          class="boarding-table"
          :header-cell-style="{background: '#f5f7fa', color: '#606266'}"
        >
          <el-table-column label="宠物名称" prop="petName" min-width="120">
            <template #default="scope">
              <span class="pet-name">{{ scope.row.petName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="宠物类型" prop="petType" min-width="100" />
          <el-table-column label="开始时间" min-width="160">
            <template #default="scope">
              <div class="date-info">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(scope.row.startTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="结束时间" min-width="160">
            <template #default="scope">
              <div class="date-info">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(scope.row.endTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格" prop="price" min-width="100">
            <template #default="scope">
              <span class="price">¥{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="特殊要求" prop="requirements" min-width="200" :show-overflow-tooltip="true">
            <template #default="scope">
              <div class="requirements">
                <el-icon><InfoFilled /></el-icon>
                <span v-if="scope.row.requirements">{{ scope.row.requirements }}</span>
                <span v-else class="no-data">无特殊要求</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" min-width="160">
            <template #default="scope">
              <div class="date-info">
                <el-icon><Clock /></el-icon>
                {{ formatDate(scope.row.createTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="180">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleDetail(scope.row)"
              >
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button 
                v-if="scope.row.status === '已申请'"
                type="danger" 
                plain
                size="small" 
                @click="handleCancel(scope.row)"
              >
                <el-icon><Close /></el-icon>
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="寄养详情" width="500px" class="boarding-dialog">
      <div v-if="currentBoarding" class="boarding-detail">
        <div class="detail-section">
          <div class="section-header">
            <el-icon><Pet /></el-icon>
            <h3>宠物信息</h3>
          </div>
          <div class="detail-item">
            <span class="detail-label">宠物名称：</span>
            <span class="detail-value">{{ currentBoarding.petName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">宠物类型：</span>
            <span class="detail-value">{{ currentBoarding.petType }}</span>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="section-header">
            <el-icon><Calendar /></el-icon>
            <h3>时间信息</h3>
          </div>
          <div class="detail-item">
            <span class="detail-label">开始时间：</span>
            <span class="detail-value">{{ formatDate(currentBoarding.startTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">结束时间：</span>
            <span class="detail-value">{{ formatDate(currentBoarding.endTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">申请时间：</span>
            <span class="detail-value">{{ formatDate(currentBoarding.createTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">更新时间：</span>
            <span class="detail-value">{{ formatDate(currentBoarding.updateTime) }}</span>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="section-header">
            <el-icon><Wallet /></el-icon>
            <h3>费用与状态</h3>
          </div>
          <div class="detail-item">
            <span class="detail-label">价格：</span>
            <span class="detail-value price">¥{{ currentBoarding.price }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态：</span>
            <el-tag :type="getStatusType(currentBoarding.status)">{{ currentBoarding.status }}</el-tag>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="section-header">
            <el-icon><InfoFilled /></el-icon>
            <h3>特殊要求</h3>
          </div>
          <div class="detail-content">
            {{ currentBoarding.requirements || '无特殊要求' }}
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentBoarding && currentBoarding.status === '已申请'"
            type="danger" 
            @click="handleCancel(currentBoarding)"
          >
            取消申请
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DateUtils from '@/utils/dateUtils'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import { 
  Refresh, 
  RefreshRight, 
  Search, 
  Calendar, 
  Clock, 
  View, 
  Close, 
  Pet, 
  HomeFilled, 
  InfoFilled,
  Wallet
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

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

// 数据列表
const boardingList = ref([])
const loading = ref(false)
const refreshLoading = ref(false)
const loadError = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusOptions = ['已申请', '已接受', '进行中', '已完成', '已取消']

// 搜索表单
const searchForm = reactive({
  status: ''
})

// 重置搜索条件
const resetSearch = () => {
  searchForm.status = ''
  handleSearch()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchMyBoardingList()
}

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page
  fetchMyBoardingList()
}

// 手动刷新数据
const handleRefresh = () => {
  refreshLoading.value = true
  fetchMyBoardingList().finally(() => {
    refreshLoading.value = false
  })
}

// 前往预约寄养页面
const goToBooking = () => {
  router.push('/boarding');
};

// 获取我的寄养列表
const fetchMyBoardingList = async () => {
  // 确保初始化为空数组而不是undefined
  boardingList.value = []
  
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('您需要登录后才能查看寄养记录，是否前往登录？', '提示', {
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

  loading.value = true;
  loadError.value = false;
  try {
    await request.get('/boarding/my', {
      userId: userStore.userInfo.id,
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        // 确保响应数据存在且包含记录
        if (res && res.records) {
          console.log('寄养记录数据:', res.records);
          // 确保日期格式正确 - DateUtils类已内置LocalDateTime处理
          boardingList.value = res.records;
          total.value = res.total || 0;
        } else {
          // 如果没有数据，设置为空数组
          boardingList.value = [];
          total.value = 0;
        }
      }
    });
  } catch (error) {
    console.error('获取寄养列表失败:', error);
    // 确保在出错时也将列表设置为空数组
    boardingList.value = [];
    total.value = 0;
    loadError.value = true;
    ElMessage.error('获取寄养记录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch(status) {
    case '已申请': return 'info'
    case '已接受': return 'success'
    case '进行中': return 'warning'
    case '已完成': return 'success'
    case '已取消': return 'danger'
    default: return 'info'
  }
}

// 详情相关变量和方法
const detailDialogVisible = ref(false)
const currentBoarding = ref(null)

// 查看详情
const handleDetail = (row) => {
  currentBoarding.value = row
  detailDialogVisible.value = true
}

// 取消申请
const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该寄养申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/boarding/${row.id}/cancel`, null, {
        successMsg: '寄养申请已取消',
        onSuccess: () => {
          detailDialogVisible.value = false
          fetchMyBoardingList()
        }
      })
    } catch (error) {
      console.error('取消寄养申请失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchMyBoardingList()
})
</script>

<style lang="scss" scoped>
.boarding-page {
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

.boarding-content {
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

.boarding-card {
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
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 15px;
  
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #683e35;
  }
  
  .card-actions {
    flex: 1;
    display: flex;
    justify-content: flex-end;
  }
  
  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
    
    :deep(.el-select) {
      width: 140px;
    }
    
    :deep(.el-button) {
      border-radius: 20px;
      transition: all 0.3s ease;
      
      .el-icon {
        margin-right: 5px;
      }
      
      &:hover {
        transform: translateY(-3px);
      }
    }
    
    :deep(.el-button--primary) {
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

.loading-container,
.empty-container,
.error-container {
  padding: 60px 0;
  text-align: center;
  background: #f9f9f9;
  border-radius: 8px;
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

.no-data {
  color: #909399;
  font-style: italic;
}

.boarding-table {
  border-radius: 8px;
  overflow: hidden;
  margin-top: 20px;
  
  :deep(.el-table__row) {
    transition: all 0.3s ease;
    
    &:hover {
      background-color: #fff9e6;
      transform: translateY(-2px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    }
  }
  
  :deep(.el-button) {
    border-radius: 20px;
    transition: all 0.3s ease;
    
    .el-icon {
      margin-right: 5px;
    }
    
    &:hover {
      transform: translateY(-3px);
    }
  }
  
  :deep(.el-button--primary) {
    background-color: #FFA726;
    border-color: #FFA726;
    
    &:hover {
      background-color: darken(#FFA726, 5%);
      border-color: darken(#FFA726, 5%);
      box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
    }
  }
  
  .pet-name {
    font-weight: 600;
    color: #333;
  }
  
  .date-info {
    display: flex;
    align-items: center;
    gap: 5px;
    
    .el-icon {
      color: #FFA726;
    }
  }
  
  .price {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .requirements {
    display: flex;
    align-items: flex-start;
    gap: 5px;
    
    .el-icon {
      margin-top: 3px;
      color: #FFA726;
    }
  }
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  
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

.boarding-dialog {
  :deep(.el-dialog__header) {
    text-align: center;
    font-weight: 600;
    padding: 20px;
    margin-right: 0;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
  }
}

.boarding-detail {
  .detail-section {
    margin-bottom: 25px;
    
    .section-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 1px solid #f0f0f0;
      
      .el-icon {
        color: #FFA726;
        font-size: 18px;
      }
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #683e35;
      }
    }
    
    .detail-item {
      margin: 10px 0;
      display: flex;
      
      .detail-label {
        width: 90px;
        color: #666;
        font-weight: 500;
      }
      
      .detail-value {
        flex: 1;
        color: #333;
        
        &.price {
          color: #f56c6c;
          font-weight: 600;
        }
      }
    }
    
    .detail-content {
      padding: 10px;
      background-color: #f9f9f9;
      border-radius: 8px;
      color: #666;
      line-height: 1.6;
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
    align-items: flex-start;
    
    .card-actions {
      width: 100%;
    }
    
    .filter-form {
      width: 100%;
      justify-content: space-between;
      
      .el-form-item {
        margin-bottom: 10px;
      }
    }
  }
  
  .boarding-table {
    :deep(.el-table__body) {
      width: 100% !important;
    }
  }
  
  .boarding-dialog {
    width: 90% !important;
    
    :deep(.el-dialog__body) {
      padding: 20px;
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