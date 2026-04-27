<template>
  <div class="pet-list-page">
    <div class="page-banner">
      <h1>寻找你的伴侣</h1>
      <p>每一只宠物都在等待一个温暖的家</p>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>

    <div class="pet-list-content">
      <div class="filter-section">
        <div class="filter-card">
          <div class="filter-header">
            <h3>寻找宠物</h3>
            <div class="filter-icon">🔍</div>
          </div>
          
          <el-form :model="searchForm" label-width="70px" class="search-form">
            <el-form-item label="名称">
              <el-input 
                v-model="searchForm.name" 
                placeholder="请输入宠物名称" 
                clearable 
                @keyup.enter="handleSearch" 
                class="custom-input" />
            </el-form-item>
            
            <el-form-item label="分类">
              <el-cascader 
                v-model="searchForm.categoryId" 
                :options="categoriesTree"
                :props="{
                  checkStrictly: true,
                  value: 'id',
                  label: 'name',
                  emitPath: false
                }"
                placeholder="请选择宠物分类" 
                clearable 
                filterable
                class="custom-select" />
            </el-form-item>
            
            <el-form-item label="品种">
              <el-input 
                v-model="searchForm.breed" 
                placeholder="请输入宠物品种" 
                clearable 
                @keyup.enter="handleSearch"
                class="custom-input" />
            </el-form-item>
            
            <el-form-item label="状态">
              <el-select 
                v-model="searchForm.adoptionStatus" 
                placeholder="请选择领养状态" 
                clearable 
                class="custom-select">
                <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            
            <div class="filter-actions">
              <el-button class="search-btn" @click="handleSearch">查询</el-button>
              <el-button class="reset-btn" @click="resetSearch">重置</el-button>
            </div>
          </el-form>
          
          <div class="filter-decoration">
            <div class="decoration-bubble bubble-1"></div>
            <div class="decoration-bubble bubble-2"></div>
          </div>
        </div>

        <div class="filter-tip">
          <div class="tip-icon">💡</div>
          <div class="tip-text">我们的宠物全部接受过健康检查和疫苗接种</div>
        </div>
        
        <div class="category-tags" v-if="categoriesList.length > 0">
          <h4 class="tags-title">热门分类</h4>
          <div class="tags-container">
            <el-tag 
              v-for="category in categoriesList.slice(0, 8)" 
              :key="category.id"
              :type="searchForm.categoryId === category.id ? 'warning' : 'info'"
              effect="light"
              class="category-tag"
              @click="selectCategory(category.id)"
            >
              {{ category.name }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <div class="list-section">
        <h2 class="section-title">
          <span class="title-icon">🐾</span>
          宠物列表
          <span class="title-count">(共 {{ total }} 只)</span>
        </h2>
        
        <div class="pet-grid" v-loading="loading">
          <div v-for="pet in petList" :key="pet.id" class="pet-card-wrapper">
            <div class="pet-card" @click="goToDetail(pet.id)">
              <div class="pet-image-container">
                <img :src="getImageUrl(pet.images)" :alt="pet.name" class="pet-image">
                <div class="pet-status" :class="getStatusClass(pet.adoptionStatus)">
                  {{ pet.adoptionStatus }}
                </div>
                <div class="pet-overlay">
                  <div class="pet-quick-info">
                    <div class="quick-info-item">
                      <span class="quick-icon">🔍</span>
                      <span>{{ pet.age }}岁</span>
                    </div>
                    <div class="quick-info-item">
                      <span class="quick-icon">{{ pet.gender === '公' ? '♂️' : '♀️' }}</span>
                      <span>{{ pet.gender }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="pet-info">
                <h3 class="pet-name">{{ pet.name }}</h3>
                <div class="pet-tags">
                  <span class="pet-tag tag-type">{{ pet.categoryName || '未分类' }}</span>
                  <span class="pet-tag tag-breed">{{ pet.breed }}</span>
                </div>
                <div class="pet-meta">
                  <div class="meta-item">
                    <span class="meta-icon">🔍</span>
                    <span>{{ pet.age }}岁</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-icon">{{ pet.gender === '公' ? '♂️' : '♀️' }}</span>
                    <span>{{ pet.gender }}</span>
                  </div>
                </div>
                <div class="pet-button">
                  <span>了解更多</span>
                  <span class="button-icon">→</span>
                </div>
              </div>
            </div>
          </div>
          
          <el-empty v-if="petList.length === 0 && !loading" description="暂无符合条件的宠物" />
        </div>
        
        <div class="pagination-container">
          <el-pagination
            background
            layout="prev, pager, next, jumper"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const baseAPI = process.env.VUE_APP_BASE_API || '/api'

const petList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const loading = ref(false)

const statusOptions = ['可领养', '已领养']
const categoriesList = ref([])
const categoriesTree = ref([])

const searchForm = reactive({
  name: '',
  breed: '',
  adoptionStatus: '',
  categoryId: ''
})

// 获取宠物列表
const fetchPets = async () => {
  loading.value = true
  try {
    await request.get('/pet/page', {
      name: searchForm.name,
      breed: searchForm.breed,
      adoptionStatus: searchForm.adoptionStatus,
      categoryId: searchForm.categoryId || null,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        petList.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取宠物列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取分类列表和树形结构
const fetchCategories = async () => {
  try {
    await request.get('/pet-category/tree', {}, {
      onSuccess: (res) => {
          if (res) {
        categoriesTree.value = res.map(item => ({
          ...item,
            disabled: item.status === 0
          }))
            flattenCategories(res)
        }else{
          categoriesTree.value = []
          flattenCategories([])
        }
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 将树形结构扁平化
const flattenCategories = (tree) => {
  categoriesList.value = []
  const flatten = (items) => {
    items.forEach(item => {
      categoriesList.value.push({
        id: item.id,
        name: item.name
      })
      if (item.children && item.children.length > 0) {
        flatten(item.children)
      }
    })
  }
  flatten(tree)
}

// 选择分类
const selectCategory = (categoryId) => {
  if (searchForm.categoryId === categoryId) {
    searchForm.categoryId = ''; // 如果点击的是已选中的分类，取消选择
  } else {
    searchForm.categoryId = categoryId; // 否则选择该分类
  }
  handleSearch();
}

// 处理搜索事件
const handleSearch = () => {
  currentPage.value = 1
  fetchPets()
}

// 重置搜索条件
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchPets()
}

// 处理分页事件
const handlePageChange = (page) => {
  currentPage.value = page
  fetchPets()
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/pet/${id}`)
}

// 获取宠物图片
const getImageUrl = (images) => {
  // 使用数据URI作为默认图片，而不是引用外部图片文件
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

// 获取状态样式类
const getStatusClass = (status) => {
  if (status === '可领养') return 'status-available'
  if (status === '已领养') return 'status-adopted'
  return ''
}

// 页面加载时获取数据
onMounted(() => {
  fetchPets()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.pet-list-page {
  min-height: 100vh;
  background-color: #FFF9E6;
  padding-bottom: 50px;
}

.page-banner {
  background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
  padding: 60px 40px;
  text-align: center;
  position: relative;
  margin-bottom: 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  
  h1 {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 36px;
    color: #683e35;
    margin-bottom: 10px;
    animation: fadeInDown 0.8s ease;
  }
  
  p {
    font-family: 'Open Sans', sans-serif;
    font-size: 18px;
    color: #683e35;
    opacity: 0.8;
    animation: fadeInUp 0.8s ease;
  }
  
  .banner-decoration {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    overflow: hidden;
    pointer-events: none;
  }
  
  .decoration-paw {
    position: absolute;
    font-size: 28px;
    opacity: 0.2;
    
    &.paw-1 {
      top: 15%;
      right: 20%;
      animation: rotate 25s linear infinite;
    }
    
    &.paw-2 {
      bottom: 25%;
      left: 15%;
      animation: rotate 20s linear infinite reverse;
      font-size: 22px;
    }
    
    &.paw-3 {
      top: 60%;
      right: 30%;
      animation: rotate 30s linear infinite;
      font-size: 20px;
    }
  }
}

.pet-list-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 25px;
  padding: 0 20px;
}

.filter-section {
  width: 280px;
  flex-shrink: 0;
}

.filter-card {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
  
  .filter-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    
    h3 {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 20px;
      color: #683e35;
      margin: 0;
    }
    
    .filter-icon {
      font-size: 22px;
      width: 40px;
      height: 40px;
      background: #FFEE93;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: pulse 2s infinite;
    }
  }
  
  .search-form {
    :deep(.el-form-item__label) {
      color: #683e35;
      font-weight: 500;
    }
    
    .custom-input {
      :deep(.el-input__wrapper) {
        border-radius: 8px;
        transition: all 0.3s ease;
        
        &.is-focus {
          box-shadow: 0 0 0 1px #FFB6C1;
        }
      }
    }
    
    .custom-select {
      width: 100%;
      
      :deep(.el-input__wrapper) {
        border-radius: 8px;
        transition: all 0.3s ease;
        
        &.is-focus {
          box-shadow: 0 0 0 1px #FFB6C1;
        }
      }
    }
    
    .option-icon {
      margin-right: 5px;
    }
  }
  
  .filter-actions {
    display: flex;
    gap: 10px;
    margin-top: 10px;
    
    .search-btn, .reset-btn {
      flex: 1;
      height: 40px;
      border-radius: 8px;
      font-weight: 500;
    }
    
    .search-btn {
      background: #FFA726;
      border: none;
      color: white;
      
      &:hover {
        background: #ff9800;
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(255, 167, 38, 0.3);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
    
    .reset-btn {
      background: transparent;
      border: 1px solid #d9d9d9;
      color: #606266;
      
      &:hover {
        border-color: #FFB6C1;
        color: #FFB6C1;
      }
    }
  }
  
  .filter-decoration {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    pointer-events: none;
    overflow: hidden;
    
    .decoration-bubble {
      position: absolute;
      border-radius: 50%;
      background: #FFB6C1;
      opacity: 0.1;
      
      &.bubble-1 {
        width: 100px;
        height: 100px;
        bottom: -30px;
        right: -30px;
      }
      
      &.bubble-2 {
        width: 60px;
        height: 60px;
        top: 20px;
        left: -20px;
      }
    }
  }
}

.filter-tip {
  margin-top: 15px;
  background: rgba(255, 238, 147, 0.4);
  border-left: 3px solid #FFEE93;
  padding: 12px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  
  .tip-icon {
    font-size: 20px;
    margin-right: 10px;
  }
  
  .tip-text {
    font-size: 14px;
    color: #683e35;
    line-height: 1.5;
  }
}

.list-section {
  flex-grow: 1;
}

.section-title {
  display: flex;
  align-items: center;
  font-family: 'Nunito Sans', sans-serif;
  font-size: 24px;
  color: #683e35;
  margin-bottom: 25px;
  
  .title-icon {
    margin-right: 10px;
    font-size: 28px;
  }
  
  .title-count {
    margin-left: 10px;
    font-size: 16px;
    opacity: 0.7;
  }
}

.pet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 25px;
  margin-bottom: 30px;
}

.pet-card-wrapper {
  transition: all 0.4s ease;
  
  &:hover {
    transform: translateY(-5px);
  }
}

.pet-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    
    .pet-overlay {
      opacity: 1;
    }
    
    .pet-button {
      background: #FFA726;
      color: white;
      
      .button-icon {
        transform: translateX(5px);
      }
    }
  }
}

.pet-image-container {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.pet-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
  
  &:hover {
    transform: scale(1.05);
  }
}

.pet-status {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  z-index: 2;
}

.status-available {
  background: #67c23a;
  box-shadow: 0 3px 6px rgba(103, 194, 58, 0.2);
}

.status-adopted {
  background: #909399;
  box-shadow: 0 3px 6px rgba(144, 147, 153, 0.2);
}

.pet-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.2);
  opacity: 0;
  transition: all 0.3s ease;
  display: flex;
  align-items: flex-end;
  padding: 15px;
}

.pet-quick-info {
  display: flex;
  gap: 15px;
}

.quick-info-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 5px 10px;
  font-size: 12px;
  display: flex;
  align-items: center;
  
  .quick-icon {
    margin-right: 5px;
  }
}

.pet-info {
  padding: 20px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.pet-name {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 10px;
  color: #683e35;
}

.pet-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.pet-tag {
  background: #FFF9E6;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  
  &.tag-type {
    background: rgba(255, 182, 193, 0.2);
    color: #d85a77;
  }
  
  &.tag-breed {
    background: rgba(255, 238, 147, 0.3);
    color: #b29860;
  }
}

.pet-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
  flex-grow: 1;
  
  .meta-item {
    display: flex;
    align-items: center;
    font-size: 14px;
    color: #666;
    
    .meta-icon {
      margin-right: 4px;
      font-size: 16px;
    }
  }
}

.pet-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
  margin-top: auto;
  
  .button-icon {
    margin-left: 5px;
    transition: transform 0.3s ease;
  }
}

.pagination-container {
  margin-top: 40px;
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

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 响应式布局 */
@media (max-width: 992px) {
  .pet-list-content {
    flex-direction: column;
  }
  
  .filter-section {
    width: 100%;
  }
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .pet-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .pet-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .page-banner {
    padding: 40px 20px;
    
    h1 {
      font-size: 28px;
    }
    
    p {
      font-size: 16px;
    }
  }
  
  .pet-grid {
    grid-template-columns: repeat(1, 1fr);
    gap: 15px;
  }
  
  .section-title {
    font-size: 20px;
  }
}

.category-tags {
  margin-top: 20px;
  background-color: white;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  
  .tags-title {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 16px;
    color: #683e35;
    margin: 0 0 12px;
    font-weight: 600;
  }
  
  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .category-tag {
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
    }
  }
}
</style> 