<template>
  <div class="training-detail-page">
    <!-- 页面装饰 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="training-content" v-loading="loading">
      <div class="breadcrumb-container">
        <div class="back-button" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </div>
      </div>
      
      <div v-if="!loading && course" class="course-detail-container">
        <!-- 课程头部信息 -->
        <div class="course-header-section">
          <div class="course-header">
            <h1 class="course-name">{{ course.name }}</h1>
            <el-tag class="status-tag" :type="course.status === 1 ? 'success' : 'danger'">
              {{ course.status === 1 ? '可预约' : '停用' }}
            </el-tag>
          </div>
          
          <div class="course-category-badge">
            <span>{{ course.category }}</span>
          </div>
        </div>
          
        <!-- 主内容区域 -->
        <div class="course-content-wrapper">
          <!-- 左侧：课程属性和特点 -->
          <div class="course-left-panel">
            <!-- 课程属性 -->
            <el-descriptions :column="2" border class="course-attributes">
              <el-descriptions-item label="课程时长">
                <div class="attribute-value">
                  <el-icon><Clock /></el-icon>
                  <span>{{ course.duration }}分钟</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="课程价格" class="price">
                <div class="attribute-value price">
                  <span>¥{{ course.price }}</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="课程状态">
                <div class="attribute-value">
                  <el-tag :type="course.status === 1 ? 'success' : 'danger'" size="small">
                    {{ course.status === 1 ? '可预约' : '停用' }}
                  </el-tag>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="最大参与人数">
                <div class="attribute-value">
                  <el-icon><User /></el-icon>
                  <span>{{ course.maxParticipants }}人</span>
                </div>
              </el-descriptions-item>
            </el-descriptions>
            
            <!-- 课程特点 -->
            <div class="section-block features-section">
              <h3 class="section-title">
                <div class="title-icon"><el-icon><Star /></el-icon></div>
                <span>课程特点</span>
              </h3>
              <div class="features-grid">
                <div class="feature-card">
                  <el-icon size="32" color="#FFA726"><Trophy /></el-icon>
                  <h4>专业训练师</h4>
                  <p>多年训练经验，为您的爱宠提供专业指导</p>
                </div>
                <div class="feature-card">
                  <el-icon size="32" color="#67C23A"><DataAnalysis /></el-icon>
                  <h4>进度追踪</h4>
                  <p>实时记录宠物训练进度，查看训练效果</p>
                </div>
                <div class="feature-card">
                  <el-icon size="32" color="#F56C6C"><Guide /></el-icon>
                  <h4>一对一指导</h4>
                  <p>根据宠物特点定制训练计划，提供个性化辅导</p>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 右侧：课程介绍和流程 -->
          <div class="course-right-panel">
            <!-- 课程介绍 -->
            <div class="section-block description-section">
              <h3 class="section-title">
                <div class="title-icon"><el-icon><InfoFilled /></el-icon></div>
                <span>课程介绍</span>
              </h3>
              <div class="description-content">
                <p>{{ course.description || '暂无详细介绍' }}</p>
              </div>
            </div>
            
            <!-- 服务流程 -->
            <div class="section-block process-section">
              <h3 class="section-title">
                <div class="title-icon"><el-icon><Connection /></el-icon></div>
                <span>服务流程</span>
              </h3>
              <div class="process-steps">
                <el-steps :active="5" finish-status="success" class="service-steps">
                  <el-step title="在线预约" icon="Calendar" />
                  <el-step title="确认预约" icon="Check" />
                  <el-step title="宠物训练" icon="Connection" />
                  <el-step title="训练反馈" icon="Comment" />
                  <el-step title="训练完成" icon="Medal" />
                </el-steps>
              </div>
            </div>
            
            <!-- 预约按钮 -->
            <div class="booking-action" v-if="course.status === 1">
              <el-button type="primary" class="booking-btn" @click="bookCourse" :disabled="course.status !== 1">
                <el-icon><Calendar /></el-icon>
                立即预约
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="课程不存在或已下架"
        :image-size="200">
        <template #description>
          <p>抱歉，该课程当前不可用</p>
          <p>您可以浏览其他训练课程</p>
        </template>
        <el-button type="primary" @click="goBack">返回课程列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Calendar, 
  Check, 
  Connection, 
  Comment, 
  Medal,
  Trophy,
  DataAnalysis,
  Guide,
  Clock,
  InfoFilled,
  Star,
  User
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const courseId = route.params.id

// 数据定义
const loading = ref(false)
const course = ref(null)

// 获取课程详情
const fetchCourseDetail = async () => {
  loading.value = true
  try {
    await request.get(`/training/course/${courseId}`, null, {
      onSuccess: (res) => {
        course.value = res
      }
    })
  } catch (error) {
    console.error('获取课程详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 预约课程
const bookCourse = () => {
  router.push({ name: 'TrainingBooking', params: { id: courseId } })
}

// 返回上一页
const goBack = () => {
  router.push({ name: 'TrainingList' })
}

// 页面加载时获取数据
onMounted(() => {
  fetchCourseDetail()
})
</script>

<style lang="scss" scoped>
.training-detail-page {
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

.training-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

.breadcrumb-container {
  display: flex;
  align-items: center;
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

.course-detail-container {
  background-color: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.course-header-section {
  margin-bottom: 25px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 20px;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .course-name {
    margin: 0;
    font-family: 'Nunito Sans', sans-serif;
    font-size: 28px;
    color: #683e35;
  }
  
  .status-tag {
    font-size: 14px;
    font-weight: 600;
    border-radius: 20px;
    padding: 4px 15px;
  }
}

.course-category-badge {
  display: inline-block;
  margin-top: 12px;
  padding: 4px 12px;
  background-color: rgba(255, 182, 193, 0.2);
  color: #ff6b88;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.course-content-wrapper {
  display: grid;
  grid-template-columns: minmax(300px, 40%) 1fr;
  gap: 30px;
}

.course-attributes {
  margin-bottom: 30px;
  
  :deep(.el-descriptions__label) {
    width: 120px;
    color: #666;
  }
  
  :deep(.el-descriptions__content) {
    color: #333;
  }
  
  .attribute-value {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      color: #FFA726;
    }
    
    &.price {
      color: #f56c6c;
      font-weight: bold;
      font-size: 20px;
    }
  }
}

.section-block {
  margin-bottom: 30px;
  
  .section-title {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 20px;
    color: #683e35;
    margin: 0 0 20px;
    display: flex;
    align-items: center;
    
    .title-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      background-color: rgba(255, 167, 38, 0.1);
      border-radius: 50%;
      margin-right: 12px;
      
      .el-icon {
        color: #FFA726;
        font-size: 20px;
      }
    }
  }
}

.description-content {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  line-height: 1.8;
  color: #666;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.feature-card {
  background-color: #f8f9fa;
  padding: 20px;
  text-align: center;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  }
  
  h4 {
    margin: 15px 0 10px;
    font-size: 18px;
    color: #683e35;
  }
  
  p {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    margin: 0;
  }
}

.process-steps {
  margin-top: 30px;
  
  :deep(.el-step__title) {
    font-size: 14px;
    color: #666;
    
    &.is-success {
      color: #FFA726;
    }
  }
  
  :deep(.el-step__head.is-success) {
    color: #FFA726;
    border-color: #FFA726;
  }
  
  :deep(.el-step__line) {
    background-color: #dcdfe6;
  }
}

.booking-action {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  
  .booking-btn {
    min-width: 200px;
    height: 50px;
    background-color: #FFA726;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    
    &:hover:not(:disabled) {
      transform: translateY(-5px);
      background-color: darken(#FFA726, 5%);
      box-shadow: 0 8px 25px rgba(255, 167, 38, 0.3);
    }
    
    &:disabled {
      background-color: #f0f0f0;
      color: #999;
    }
    
    .el-icon {
      margin-right: 8px;
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

// 响应式调整
@media (max-width: 992px) {
  .course-content-wrapper {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .training-detail-page {
    padding: 20px 16px 40px;
  }
  
  .course-detail-container {
    padding: 20px;
  }
  
  .course-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    
    .course-name {
      font-size: 22px;
    }
    
    .status-tag {
      align-self: flex-start;
    }
  }
  
  .feature-card {
    padding: 15px;
  }
  
  .booking-action {
    .booking-btn {
      width: 100%;
    }
  }
}
</style> 