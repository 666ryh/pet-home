<template>
  <div class="training-list-page">
    <!-- 页面装饰 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="training-content">
      <div class="page-header">
        <h1 class="page-title">宠物训练课程</h1>
        <p class="page-subtitle">专业宠物训练课程，助您的爱宠养成良好习惯</p>
      </div>
      
      <div class="filter-container">
        <div class="filter-section">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程"
            @input="handleSearch"
            clearable
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select
            v-model="categoryFilter"
            placeholder="课程分类"
            clearable
            @change="handleCategoryChange"
            class="category-select"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            >
              <div class="category-option">
                <el-icon v-if="category.icon"><component :is="category.icon" /></el-icon>
                <span>{{ category.name }}</span>
              </div>
            </el-option>
          </el-select>
        </div>
      </div>

      <div class="course-list" v-loading="loading">
        <div v-if="!loading && courses.length > 0" class="course-grid">
          <div
            v-for="course in courses"
            :key="course.id"
            class="course-card"
            @click="viewCourseDetail(course.id)"
          >
            <div class="course-category-tag">{{ course.category }}</div>
            <div class="course-content">
              <h3 class="course-name">{{ course.name }}</h3>
              <div class="course-info">
                <div class="course-meta">
                  <div class="meta-item">
                    <el-icon class="icon"><Clock /></el-icon>
                    <span>{{ course.duration }}分钟</span>
                  </div>
                  <div class="meta-item">
                    <el-icon class="icon"><User /></el-icon>
                    <span>最多{{ course.maxParticipants || 5 }}人</span>
                  </div>
                </div>
                <div class="course-description">{{ formatDescription(course.description) }}</div>
                <div class="course-price">¥{{ course.price }}</div>
              </div>
              <div class="course-action">
                <el-button type="primary" class="detail-btn" @click.stop="viewCourseDetail(course.id)">
                  <el-icon><InfoFilled /></el-icon>
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <el-empty
          v-else-if="!loading && courses.length === 0"
          description="暂无可用的训练课程"
          :image-size="200"
        >
          <template #description>
            <p>目前没有匹配的训练课程</p>
            <p>请调整筛选条件或稍后再试</p>
          </template>
        </el-empty>
      </div>

      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[8, 16, 24, 32]"
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Clock, User, InfoFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 数据定义
const loading = ref(false)
const courses = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)
const searchKeyword = ref('')
const categoryFilter = ref('')
const categories = ref([])

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true
  try {
    const params = {
      name: searchKeyword.value,
      categoryId: categoryFilter.value,
      currentPage: currentPage.value,
      size: pageSize.value
    }

    await request.get('/training/course/page', params, {
      onSuccess: (res) => {
        courses.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取所有课程分类
const fetchCategories = async () => {
  try {
    await request.get('/training/category/enabled', null, {
      onSuccess: (res) => {
        categories.value = res || []
      }
    })
  } catch (error) {
    console.error('获取课程分类失败:', error)
  }
}

// 格式化课程描述
const formatDescription = (description) => {
  if (!description) return '暂无描述'
  return description.length > 50 ? description.substring(0, 50) + '...' : description
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCourses()
}

// 处理分类筛选
const handleCategoryChange = () => {
  currentPage.value = 1
  fetchCourses()
}

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchCourses()
}

// 处理当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchCourses()
}

// 查看课程详情
const viewCourseDetail = (id) => {
  router.push({ name: 'TrainingDetail', params: { id } })
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchCourses()
})
</script>

<style lang="scss" scoped>
.training-list-page {
  position: relative;
  padding: 30px 20px 60px;
  min-height: 100vh;
  background-color: #FFF9E6;
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

.filter-container {
  margin-bottom: 30px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.filter-section {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  justify-content: center;
  
  .search-input {
    width: 280px;
    
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #dcdfe6 inset;
      border-radius: 8px;
      
      &:hover {
        box-shadow: 0 0 0 1px #FFA726 inset;
      }
      
      &.is-focus {
        box-shadow: 0 0 0 1px #FFA726 inset;
      }
    }
  }
  
  .category-select {
    width: 180px;
    
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #dcdfe6 inset;
      border-radius: 8px;
      
      &:hover {
        box-shadow: 0 0 0 1px #FFA726 inset;
      }
      
      &.is-focus {
        box-shadow: 0 0 0 1px #FFA726 inset;
      }
    }
  }
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.course-list {
  margin-bottom: 30px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
}

.course-card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  position: relative;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  }
  
  .course-category-tag {
    position: absolute;
    top: 15px;
    right: 15px;
    background-color: #FFA726;
    color: white;
    padding: 3px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    z-index: 2;
  }
  
  .course-content {
    padding: 25px;
  }
  
  .course-name {
    margin-top: 5px;
    margin-bottom: 15px;
    font-size: 18px;
    font-weight: 600;
    color: #683e35;
    line-height: 1.4;
  }
  
  .course-info {
    margin-bottom: 15px;
  }
  
  .course-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    margin-bottom: 15px;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #666;
      font-size: 14px;
      
      .icon {
        color: #FFA726;
      }
    }
  }
  
  .course-description {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 15px;
    min-height: 65px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .course-price {
    color: #f56c6c;
    font-weight: bold;
    font-size: 20px;
    margin-top: 10px;
    
    &::before {
      content: '¥';
      font-size: 16px;
    }
  }
  
  .course-action {
    display: flex;
    justify-content: flex-end;
    
    .detail-btn {
      background-color: #FFA726;
      color: white;
      border: none;
      border-radius: 8px;
      padding: 8px 16px;
      transition: all 0.3s ease;
      
      &:hover {
        background-color: darken(#FFA726, 5%);
        transform: translateY(-3px);
      }
      
      .el-icon {
        margin-right: 5px;
      }
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

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

// 响应式调整
@media (max-width: 768px) {
  .training-list-page {
    padding: 20px 16px 40px;
  }
  
  .page-header {
    .page-title {
      font-size: 26px;
    }
    
    .page-subtitle {
      font-size: 14px;
    }
  }
  
  .filter-section {
    flex-direction: column;
    width: 100%;
    gap: 10px;
    
    .search-input,
    .category-select {
      width: 100%;
    }
  }
  
  .course-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 20px;
  }
  
  .course-card {
    .course-content {
      padding: 20px;
    }
    
    .course-name {
      font-size: 16px;
    }
    
    .course-description {
      min-height: 50px;
    }
  }
}
</style> 