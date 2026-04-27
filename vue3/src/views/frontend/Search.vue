<template>
  <div class="search-page">
    <div class="search-header">
      <h1 class="search-title">搜索结果：{{ keyword }}</h1>
      <div class="search-options">
        <el-checkbox v-model="searchPets" @change="fetchSearchResults">宠物</el-checkbox>
        <el-checkbox v-model="searchServices" @change="fetchSearchResults">服务</el-checkbox>
        <el-checkbox v-model="searchTrainingCourses" @change="fetchSearchResults">训练课程</el-checkbox>
      </div>
    </div>

    <div class="search-content">
      <!-- 宠物搜索结果 -->
      <template v-if="searchPets && petResults.records && petResults.records.length > 0">
        <div class="result-section">
          <h2 class="section-title">宠物 ({{ petResults.total || 0 }})</h2>
          <div class="pet-results">
            <el-row :gutter="20">
              <el-col v-for="pet in petResults.records" :key="pet.id" :xs="24" :sm="12" :md="8" :lg="6">
                <router-link :to="`/pet/${pet.id}`">
                  <el-card class="pet-card" shadow="hover">
                    <div class="pet-image">
                      <el-image 
                        :src="getImageUrl(pet.images)" 
                        fit="cover"
                        :preview-src-list="getImageUrlList(pet.images)">
                        <template #error>
                          <div class="image-placeholder">
                            <el-icon><Picture /></el-icon>
                          </div>
                        </template>
                      </el-image>
                    </div>
                    <div class="pet-info">
                      <h3 class="pet-name">{{ pet.name }}</h3>
                      <p class="pet-breed">{{ pet.breed }}</p>
                      <div class="pet-details">
                        <span class="pet-age">{{ pet.age }}岁</span>
                        <span class="pet-gender">{{ pet.gender }}</span>
                      </div>
                    </div>
                  </el-card>
                </router-link>
              </el-col>
            </el-row>
            <div class="pagination-container" v-if="petResults.total > 10">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="petResults.total"
                :page-size="10"
                :current-page="currentPetPage"
                @current-change="handlePetPageChange"
              />
            </div>
          </div>
        </div>
      </template>
      <template v-else-if="searchPets && (!petResults.records || petResults.records.length === 0)">
        <div class="empty-result">
          <el-empty description="没有找到相关宠物" />
        </div>
      </template>

      <!-- 服务搜索结果 -->
      <template v-if="searchServices && serviceResults.records && serviceResults.records.length > 0">
        <div class="result-section">
          <h2 class="section-title">服务 ({{ serviceResults.total || 0 }})</h2>
          <div class="service-results">
            <el-row :gutter="20">
              <el-col v-for="service in serviceResults.records" :key="service.id" :xs="24" :sm="12" :md="8">
                <router-link :to="`/service/${service.id}`">
                  <el-card class="service-card" shadow="hover">
                    <div class="service-info">
                      <h3 class="service-name">{{ service.name }}</h3>
                      <div class="service-price">¥{{ service.price }}</div>
                      <div class="service-duration">
                        <el-icon><Timer /></el-icon>
                        <span>{{ service.duration }}分钟</span>
                      </div>
                      <p class="service-description">{{ service.description }}</p>
                    </div>
                  </el-card>
                </router-link>
              </el-col>
            </el-row>
            <div class="pagination-container" v-if="serviceResults.total > 10">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="serviceResults.total"
                :page-size="10"
                :current-page="currentServicePage"
                @current-change="handleServicePageChange"
              />
            </div>
          </div>
        </div>
      </template>
      <template v-else-if="searchServices && (!serviceResults.records || serviceResults.records.length === 0)">
        <div class="empty-result">
          <el-empty description="没有找到相关服务" />
        </div>
      </template>

      <!-- 训练课程搜索结果 -->
      <template v-if="searchTrainingCourses && trainingResults.records && trainingResults.records.length > 0">
        <div class="result-section">
          <h2 class="section-title">训练课程 ({{ trainingResults.total || 0 }})</h2>
          <div class="training-results">
            <el-row :gutter="20">
              <el-col v-for="course in trainingResults.records" :key="course.id" :xs="24" :sm="12" :md="8">
                <router-link :to="`/training/${course.id}`">
                  <el-card class="training-card" shadow="hover">
                    <div class="training-info">
                      <h3 class="training-name">{{ course.name }}</h3>
                      <div class="training-category">
                        <el-tag size="small" type="warning">{{ course.category }}</el-tag>
                      </div>
                      <div class="training-price">¥{{ course.price }}</div>
                      <div class="training-duration">
                        <el-icon><Timer /></el-icon>
                        <span>{{ course.duration }}分钟</span>
                      </div>
                      <p class="training-description">{{ course.description }}</p>
                    </div>
                  </el-card>
                </router-link>
              </el-col>
            </el-row>
            <div class="pagination-container" v-if="trainingResults.total > 10">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="trainingResults.total"
                :page-size="10"
                :current-page="currentTrainingPage"
                @current-change="handleTrainingPageChange"
              />
            </div>
          </div>
        </div>
      </template>
      <template v-else-if="searchTrainingCourses && (!trainingResults.records || trainingResults.records.length === 0)">
        <div class="empty-result">
          <el-empty description="没有找到相关训练课程" />
        </div>
      </template>

      <!-- 没有结果时显示 -->
      <template v-if="noResults">
        <el-empty description="没有找到相关内容" :image-size="200">
          <template #description>
            <p>没有找到与"{{ keyword }}"相关的内容，请尝试其他关键词</p>
          </template>
          <el-button type="primary" @click="goHome">返回首页</el-button>
        </el-empty>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Picture, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const baseAPI = process.env.VUE_APP_BASE_API || '/api'

// 搜索关键词
const keyword = ref('')

// 搜索选项
const searchPets = ref(true)
const searchServices = ref(true)
const searchTrainingCourses = ref(true)

// 搜索结果
const petResults = ref({ records: [], total: 0 })
const serviceResults = ref({ records: [], total: 0 })
const trainingResults = ref({ records: [], total: 0 })

// 分页
const currentPetPage = ref(1)
const currentServicePage = ref(1)
const currentTrainingPage = ref(1)

// 加载状态
const loading = ref(false)

// 计算是否没有搜索结果
const noResults = computed(() => {
  return (
    (!petResults.value.records || petResults.value.records.length === 0 || !searchPets.value) &&
    (!serviceResults.value.records || serviceResults.value.records.length === 0 || !searchServices.value) &&
    (!trainingResults.value.records || trainingResults.value.records.length === 0 || !searchTrainingCourses.value)
  )
})

// 监听路由参数变化
watch(() => route.query.q, (newKeyword) => {
  if (newKeyword) {
    keyword.value = newKeyword
    resetPagination()
    fetchSearchResults()
  }
})

// 重置分页
const resetPagination = () => {
  currentPetPage.value = 1
  currentServicePage.value = 1
  currentTrainingPage.value = 1
}

// 获取搜索结果
const fetchSearchResults = async () => {
  if (!keyword.value) return
  
  loading.value = true
  
  try {
    const params = {
      keyword: keyword.value,
      searchPets: searchPets.value,
      searchServices: searchServices.value,
      searchTrainingCourses: searchTrainingCourses.value,
      currentPage: 1,
      size: 10
    }
    
    // 使用request工具直接发送请求
    const response = await request.get('/search', params, {
      onSuccess: (data) => {
        if (data.pets) {
          petResults.value = data.pets
        }
        
        if (data.services) {
          serviceResults.value = data.services
        }
        
        if (data.trainingCourses) {
          trainingResults.value = data.trainingCourses
        }
      },
      onError: (error) => {
        console.error('搜索失败', error)
        ElMessage.error('搜索失败，请稍后再试')
      }
    })
  } catch (error) {
    console.error('搜索失败', error)
    ElMessage.error('搜索失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 宠物分页变化
const handlePetPageChange = async (page) => {
  currentPetPage.value = page
  
  try {
    const params = {
      keyword: keyword.value,
      searchPets: true,
      searchServices: false,
      searchTrainingCourses: false,
      currentPage: page,
      size: 10
    }
    
    // 使用request工具直接发送请求
    await request.get('/search', params, {
      onSuccess: (data) => {
        if (data.pets) {
          petResults.value = data.pets
        }
      },
      onError: (error) => {
        console.error('加载宠物分页失败', error)
        ElMessage.error('加载失败，请稍后再试')
      }
    })
  } catch (error) {
    console.error('加载宠物分页失败', error)
    ElMessage.error('加载失败，请稍后再试')
  }
}

// 服务分页变化
const handleServicePageChange = async (page) => {
  currentServicePage.value = page
  
  try {
    const params = {
      keyword: keyword.value,
      searchPets: false,
      searchServices: true,
      searchTrainingCourses: false,
      currentPage: page,
      size: 10
    }
    
    // 使用request工具直接发送请求
    await request.get('/search', params, {
      onSuccess: (data) => {
        if (data.services) {
          serviceResults.value = data.services
        }
      },
      onError: (error) => {
        console.error('加载服务分页失败', error)
        ElMessage.error('加载失败，请稍后再试')
      }
    })
  } catch (error) {
    console.error('加载服务分页失败', error)
    ElMessage.error('加载失败，请稍后再试')
  }
}

// 训练课程分页变化
const handleTrainingPageChange = async (page) => {
  currentTrainingPage.value = page
  
  try {
    const params = {
      keyword: keyword.value,
      searchPets: false,
      searchServices: false,
      searchTrainingCourses: true,
      currentPage: page,
      size: 10
    }
    
    // 使用request工具直接发送请求
    await request.get('/search', params, {
      onSuccess: (data) => {
        if (data.trainingCourses) {
          trainingResults.value = data.trainingCourses
        }
      },
      onError: (error) => {
        console.error('加载训练课程分页失败', error)
        ElMessage.error('加载失败，请稍后再试')
      }
    })
  } catch (error) {
    console.error('加载训练课程分页失败', error)
    ElMessage.error('加载失败，请稍后再试')
  }
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 页面加载时获取搜索关键词并执行搜索
onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q
    fetchSearchResults()
  }
})

// 获取宠物图片URL
const getImageUrl = (images) => {
  // 使用数据URI作为默认图片
  const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTgwIiBoZWlnaHQ9IjE4MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB4PSIyIiB5PSIyIiB3aWR0aD0iMTc2IiBoZWlnaHQ9IjE3NiIgc3R5bGU9ImZpbGw6I2VlZTtzdHJva2U6I2FhYTtzdHJva2Utd2lkdGg6MnB4OyIvPjx0ZXh0IHg9IjkwIiB5PSI5MCIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgc3R5bGU9ImZpbGw6Izk5OTtmb250LWZhbWlseTpBcmlhbCxzYW5zLXNlcmlmO2ZvbnQtc2l6ZToxOHB4OyI+无图片</dGV4dD48L3N2Zz4='
  
  if (!images) return defaultImage
  const imageList = images.split(',')
  if (imageList.length === 0) return defaultImage
  
  const firstImage = imageList[0].trim()
  if (firstImage.startsWith('http')) {
    return firstImage
  }
  return `${baseAPI}${firstImage}`
}

// 获取宠物图片列表（用于预览）
const getImageUrlList = (images) => {
  if (!images) return []
  
  return images.split(',').map(img => {
    const trimmedImg = img.trim()
    if (trimmedImg.startsWith('http')) {
      return trimmedImg
    }
    return `${baseAPI}${trimmedImg}`
  })
}
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.search-header {
  margin-bottom: 2rem;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 1rem;
}

.search-title {
  font-size: 1.8rem;
  color: #6E4C1E;
  margin-bottom: 1rem;
}

.search-options {
  display: flex;
  gap: 1.5rem;
}

.result-section {
  margin-bottom: 3rem;
}

.section-title {
  font-size: 1.4rem;
  color: #6E4C1E;
  margin-bottom: 1.5rem;
  border-left: 4px solid #FF9800;
  padding-left: 1rem;
}

.pet-card, .service-card, .training-card {
  transition: transform 0.3s, box-shadow 0.3s;
  height: 100%;
  margin-bottom: 20px;
}

.pet-card:hover, .service-card:hover, .training-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.pet-image {
  height: 200px;
  overflow: hidden;
}

.pet-image .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #909399;
}

.image-placeholder .el-icon {
  font-size: 2rem;
}

.pet-info, .service-info, .training-info {
  padding: 1rem;
}

.pet-name, .service-name, .training-name {
  font-size: 1.2rem;
  color: #303133;
  margin-bottom: 0.5rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pet-breed {
  color: #606266;
  margin-bottom: 0.5rem;
}

.pet-details {
  display: flex;
  gap: 1rem;
  color: #909399;
}

.service-price, .training-price {
  font-size: 1.2rem;
  color: #ff6b00;
  margin: 0.5rem 0;
}

.service-duration, .training-duration {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #909399;
  margin-bottom: 0.5rem;
}

.service-description, .training-description {
  color: #606266;
  margin-top: 0.5rem;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.training-category {
  margin: 0.5rem 0;
}

.pagination-container {
  margin-top: 1rem;
  display: flex;
  justify-content: center;
}

.empty-result {
  padding: 2rem 0;
}

a {
  text-decoration: none;
  color: inherit;
}

@media (max-width: 768px) {
  .search-title {
    font-size: 1.5rem;
  }
  
  .search-options {
    flex-wrap: wrap;
  }
  
  .section-title {
    font-size: 1.2rem;
  }
}
</style> 