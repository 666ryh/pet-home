<template>
  <div class="appointments-page">
    <!-- 页面装饰 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="appointments-content">
      <div class="page-header">
        <h1 class="page-title">我的训练预约</h1>
        <p class="page-subtitle">查看和管理您的训练预约记录</p>
      </div>
      
      <div class="breadcrumb-container">
        <div class="back-button" @click="goToTrainingList">
          <el-icon><ArrowLeft /></el-icon>
          返回课程列表
        </div>
      </div>
      
      <el-card shadow="never" class="appointments-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <h2>预约列表</h2>
          </div>
        </template>
        
        <div class="filter-section">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="8" :md="6">
              <el-select 
                v-model="statusFilter" 
                placeholder="预约状态" 
                clearable 
                class="status-filter"
              >
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-col>
            <el-col :xs="24" :sm="8" :md="6" class="button-filter">
              <el-button type="primary" @click="fetchAppointments" class="filter-btn">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetFilters" class="filter-btn">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-col>
          </el-row>
        </div>
        
        <div class="appointments-list">
          <el-empty 
            v-if="!loading && appointments.length === 0" 
            description="暂无预约记录" 
            :image-size="200"
          >
            <template #description>
              <p>您还没有任何训练预约记录</p>
              <p>快去预约训练课程吧</p>
            </template>
            <el-button type="primary" class="action-btn" @click="goToTrainingList">
              <el-icon><Plus /></el-icon>
              预约新课程
            </el-button>
          </el-empty>
          
          <el-collapse v-else accordion>
            <el-collapse-item v-for="item in appointments" :key="item.id" class="appointment-item">
              <template #title>
                <div class="appointment-header">
                  <span class="course-name">{{ item.courseName }}</span>
                  <el-tag 
                    :type="getStatusType(item.status)" 
                    size="small" 
                    class="status-tag"
                  >
                    {{ item.status }}
                  </el-tag>
                </div>
              </template>
              
              <div class="appointment-details">
                <div class="appointment-info-card">
                  <h3 class="info-title">
                    <el-icon><Calendar /></el-icon>
                    基本信息
                  </h3>
                  <el-descriptions :column="2" border size="medium" class="info-descriptions">
                    <el-descriptions-item label="预约时间">
                      {{ formatDateTime(item.appointmentTime) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="宠物名称">{{ item.petName }}</el-descriptions-item>
                    <el-descriptions-item label="联系电话">{{ item.contactPhone }}</el-descriptions-item>
                    <el-descriptions-item label="训练进度">
                      <el-progress 
                        :percentage="item.progress || 0" 
                        :format="progressFormat"
                        :stroke-width="16"
                        :color="getProgressColor(item.progress || 0)"
                      />
                    </el-descriptions-item>
                    <el-descriptions-item v-if="item.requirements" label="特殊要求" :span="2">
                      {{ item.requirements }}
                    </el-descriptions-item>
                    <el-descriptions-item v-if="item.remark" label="备注" :span="2">
                      {{ item.remark }}
                    </el-descriptions-item>
                    <el-descriptions-item label="预约时间">{{ formatDateTime(item.createTime) }}</el-descriptions-item>
                    <el-descriptions-item label="课程类别">{{ item.category }}</el-descriptions-item>
                  </el-descriptions>
                </div>
                
                <div class="appointment-actions" v-if="item.status === '已预约' || item.status === '已确认'">
                  <el-button type="danger" @click.stop="cancelAppointment(item.id)" class="cancel-btn">
                    <el-icon><Close /></el-icon>
                    取消预约
                  </el-button>
                </div>
                
                <div class="feedback-section" v-if="item.status === '已完成' && !item.hasFeedback">
                  <h3 class="feedback-title">
                    <el-icon><ChatDotRound /></el-icon>
                    训练反馈
                  </h3>
                  <el-form :model="feedbackForm" label-position="top" class="feedback-form">
                    <el-form-item label="训练满意度">
                      <el-rate 
                        v-model="feedbackForm.rating" 
                        show-score 
                        :colors="['#FF9900', '#FF9900', '#FF9900']"
                      />
                    </el-form-item>
                    <el-form-item label="反馈内容">
                      <el-input
                        v-model="feedbackForm.content"
                        type="textarea"
                        :rows="3"
                        placeholder="请分享您对本次训练的感受和建议"
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click.stop="submitFeedback(item.id)" class="submit-btn">
                        <el-icon><Check /></el-icon>
                        提交反馈
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
                
                <div v-if="item.hasFeedback && item.feedback" class="feedback-display">
                  <h3 class="feedback-title">
                    <el-icon><Star /></el-icon>
                    我的反馈
                  </h3>
                  <div class="feedback-content">
                    <div class="rating">
                      <span>满意度: </span>
                      <el-rate 
                        v-model="item.rating" 
                        disabled 
                        show-score
                        :colors="['#FF9900', '#FF9900', '#FF9900']"
                      />
                    </div>
                    <div class="content">
                      <p>{{ item.feedback }}</p>
                    </div>
                    <div class="time">
                      <small>提交时间: {{ formatDateTime(item.feedbackTime) }}</small>
                    </div>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
          
          <div class="pagination-container" v-if="total > 0">
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              @update:page-size="val => pageSize = val"
              @update:current-page="val => currentPage = val"
              background
            />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { 
  ArrowLeft, 
  Search, 
  Refresh, 
  Plus, 
  Calendar, 
  Close, 
  ChatDotRound, 
  Check, 
  Star 
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 数据定义
const loading = ref(false)
const appointments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')

// 反馈表单
const feedbackForm = ref({
  rating: 5,
  content: ''
})

// 预约状态选项
const statusOptions = [
  { value: '已预约', label: '已预约' },
  { value: '已确认', label: '已确认' },
  { value: '进行中', label: '进行中' },
  { value: '已完成', label: '已完成' },
  { value: '已取消', label: '已取消' }
]

// 获取预约列表
const fetchAppointments = async () => {
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm(
      '查看预约记录需要先登录，是否立即前往登录？',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      })
    }).catch(() => {
      router.push('/training')
    })
    return
  }
  
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      userId: userStore.userInfo.id,
      currentPage: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value || undefined
    }
    
    await request.get('/training/appointment/user', params, {
      onSuccess: (res) => {
        appointments.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 取消预约
const cancelAppointment = (id) => {
  ElMessageBox.confirm(
    '确定要取消此次预约吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.put(`/training/appointment/${id}/cancel`, null, {
        successMsg: '预约已取消',
        onSuccess: () => {
          fetchAppointments()
        }
      })
    } catch (error) {
      console.error('取消预约失败:', error)
    }
  }).catch(() => {})
}

// 提交反馈
const submitFeedback = async (appointmentId) => {
  if (!feedbackForm.value.content) {
    ElMessage.warning('请填写反馈内容')
    return
  }
  
  try {
    const feedbackData = {
      id: appointmentId,
      userId: userStore.userInfo.id,
      rating: feedbackForm.value.rating,
      feedback: feedbackForm.value.content
    }
    
    await request.post('/training/appointment/feedback', feedbackData, {
      successMsg: '反馈已提交，感谢您的评价',
      onSuccess: () => {
        // 重置表单
        feedbackForm.value = {
          rating: 5,
          content: ''
        }
        // 刷新列表
        fetchAppointments()
      }
    })
  } catch (error) {
    console.error('提交反馈失败:', error)
  }
}

// 重置筛选条件
const resetFilters = () => {
  statusFilter.value = ''
  currentPage.value = 1
  fetchAppointments()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAppointments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAppointments()
}

// 导航到训练课程列表
const goToTrainingList = () => {
  router.push('/training')
}

// 根据状态获取标签类型
const getStatusType = (status) => {
  const statusMap = {
    '已预约': 'info',
    '已确认': 'primary',
    '进行中': 'warning',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage < 30) return '#909399'
  if (percentage < 70) return '#E6A23C'
  return '#67C23A'
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '暂无数据'
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 进度条格式化
const progressFormat = (percentage) => {
  return percentage === 100 ? '完成' : `${percentage}%`
}

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (newVal) => {
  if (newVal) {
    fetchAppointments()
  }
})

// 页面加载时获取数据
onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchAppointments()
  } else {
    ElMessageBox.confirm(
      '查看预约记录需要先登录，是否立即前往登录？',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      })
    }).catch(() => {
      router.push('/training')
    })
  }
})
</script>

<style lang="scss" scoped>
.appointments-page {
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

.appointments-content {
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

.breadcrumb-container {
  margin-bottom: 20px;
  
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
    
    .el-icon {
      margin-right: 8px;
    }
    
    &:hover {
      transform: translateX(-3px);
      background-color: #FFF0F0;
    }
  }
}

.appointments-card {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05) !important;
  margin-bottom: 30px;
  
  :deep(.el-card__header) {
    padding: 20px 25px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.card-header {
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #683e35;
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

.filter-section {
  margin-bottom: 25px;
  
  .status-filter {
    width: 100%;
    
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #dcdfe6 inset;
      
      &:hover, &.is-focus {
        box-shadow: 0 0 0 1px #FFA726 inset;
      }
    }
  }
  
  .filter-btn {
    border-radius: 20px;
    transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    margin-right: 10px;
    
    &:first-child {
      background-color: #FFA726;
      border-color: #FFA726;
      
      &:hover {
        background-color: darken(#FFA726, 5%);
        border-color: darken(#FFA726, 5%);
        transform: translateY(-3px);
        box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
      }
    }
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .el-icon {
      margin-right: 5px;
    }
  }
  
  .button-filter {
    margin-top: 10px;
    
    @media (min-width: 768px) {
      margin-top: 0;
    }
  }
}

.appointments-list {
  .appointment-item {
    margin-bottom: 15px;
    border-radius: 8px;
    overflow: hidden;
    
    :deep(.el-collapse-item__header) {
      background-color: #f8f9fa;
      
      &.is-active {
        background-color: #f0f7ff;
      }
    }
    
    :deep(.el-collapse-item__wrap) {
      border-color: #f0f0f0;
    }
  }
}

.appointment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  
  .course-name {
    font-weight: 600;
    flex-grow: 1;
    color: #333;
  }
  
  .status-tag {
    margin-left: 10px;
  }
}

.appointment-details {
  padding: 15px 0;
}

.appointment-info-card {
  margin-bottom: 20px;
  
  .info-title {
    display: flex;
    align-items: center;
    margin: 0 0 15px;
    color: #333;
    font-size: 18px;
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
    
    .el-icon {
      margin-right: 8px;
      color: #FFA726;
    }
  }
  
  .info-descriptions {
    :deep(.el-descriptions__label) {
      color: #666;
      font-weight: 500;
      background-color: #f9f9f9;
    }
    
    :deep(.el-descriptions__content) {
      padding: 12px 15px;
    }
  }
}

.appointment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  
  .cancel-btn {
    color: #f56c6c;
    border-color: #f56c6c;
    border-radius: 20px;
    transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    
    &:hover {
      color: white;
      background-color: #f56c6c;
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(245, 108, 108, 0.15);
    }
    
    .el-icon {
      margin-right: 5px;
    }
  }
}

.feedback-section {
  margin-top: 25px;
  border-top: 1px dashed #ebeef5;
  padding-top: 20px;
  
  .feedback-title {
    display: flex;
    align-items: center;
    margin: 0 0 15px;
    color: #333;
    font-size: 18px;
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
    
    .el-icon {
      margin-right: 8px;
      color: #FFA726;
    }
  }
  
  .feedback-form {
    background-color: #f8f9fa;
    border-radius: 8px;
    padding: 20px;
    
    :deep(.el-form-item__label) {
      color: #666;
      font-weight: 500;
    }
    
    :deep(.el-textarea__inner) {
      box-shadow: 0 0 0 1px #dcdfe6 inset;
      
      &:hover, &:focus {
        box-shadow: 0 0 0 1px #FFA726 inset;
      }
    }
    
    .submit-btn {
      background-color: #FFA726;
      border-color: #FFA726;
      border-radius: 20px;
      transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
      
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
  }
}

.feedback-display {
  margin-top: 25px;
  border-top: 1px dashed #ebeef5;
  padding-top: 20px;
  
  .feedback-title {
    display: flex;
    align-items: center;
    margin: 0 0 15px;
    color: #333;
    font-size: 18px;
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
    
    .el-icon {
      margin-right: 8px;
      color: #FF9900;
    }
  }
  
  .feedback-content {
    background-color: #f8f9fa;
    padding: 15px;
    border-radius: 8px;
    
    .rating {
      margin-bottom: 10px;
    }
    
    .content {
      padding: 10px 0;
      border-top: 1px solid #ebeef5;
      border-bottom: 1px solid #ebeef5;
      color: #666;
      line-height: 1.6;
      
      p {
        margin: 0;
      }
    }
    
    .time {
      margin-top: 10px;
      text-align: right;
      color: #909399;
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

.action-btn {
  background-color: #FFA726;
  border-color: #FFA726;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
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
  
  .appointment-info-card {
    .info-descriptions {
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
}
</style> 