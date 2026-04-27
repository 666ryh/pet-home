<template>
  <div class="product-list-page">
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="product-list-content">
      <div class="page-header">
        <h1 class="page-title">宠物商城</h1>
        <p class="page-subtitle">为您心爱的宠物挑选优质商品</p>
      </div>
      
      <div class="filter-section">
        <el-form :inline="true">
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="商品分类">
            <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
              <el-option 
                v-for="category in categories" 
                :key="category.value" 
                :label="category.label" 
                :value="category.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="search-btn" @click="handleSearch">
              <el-icon><search /></el-icon>搜索
            </el-button>
            <el-button class="reset-btn" @click="resetSearch">
              <el-icon><refresh /></el-icon>重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="product-list-container" v-loading="loading">
        <transition-group name="product-fade" tag="div" class="product-grid">
          <div v-for="product in productList" :key="product.id" class="product-item">
            <div class="product-card" :class="{'out-of-stock': product.stock <= 0}">
              <div class="product-image" @click="viewProductDetail(product.id)">
                <el-image 
                  :src="getImageUrl(product.images)" 
                  fit="cover"
                  loading="lazy"
                  lazy>
                  <template #error>
                    <div class="image-error">
                      <el-icon><picture-failed /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-if="product.stock <= 0" class="stock-badge">缺货</div>
                <div v-else-if="product.stock < 10" class="stock-badge limited">库存紧张</div>
              </div>
              
              <div class="product-info">
                <div class="product-category">{{ product.category }}</div>
                <h3 class="product-name" @click="viewProductDetail(product.id)">{{ product.name }}</h3>
                <div class="product-meta">
                  <div class="product-price">
                    <span class="price">¥{{ product.price }}</span>
                    <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">¥{{ product.originalPrice }}</span>
                  </div>
                  <div v-if="product.sales" class="product-sales">已售 {{ product.sales }}+</div>
                </div>
                <div class="product-action">
                  <el-button 
                    class="cart-button" 
                    :disabled="product.stock <= 0"
                    @click="addToCart(product)">
                    <el-icon><shopping-cart /></el-icon>
                    {{ product.stock <= 0 ? '暂时缺货' : '加入购物车' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </transition-group>
        
        <el-empty 
          v-if="!loading && productList.length === 0" 
          description="暂无相关商品"
          class="product-empty">
        </el-empty>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          background
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[8, 16, 24, 32]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange">
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { PictureFailed, ShoppingCart, Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 路由
const router = useRouter()

// 数据定义
const loading = ref(false)
const productList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)

// 搜索表单
const searchForm = reactive({
  name: '',
  category: ''
})

// 商品分类
const categories = [
  { label: '主粮', value: '主粮' },
  { label: '零食', value: '零食' },
  { label: '玩具', value: '玩具' },
  { label: '日用品', value: '日用品' },
  { label: '保健品', value: '保健品' },
  { label: '服饰', value: '服饰' },
  { label: '其他', value: '其他' }
]

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    await request.get('/product/page', {
      name: searchForm.name,
      category: searchForm.category,
      currentPage: currentPage.value,
      size: pageSize.value,
      status: 1
    }, {
      onSuccess: (res) => {
        productList.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  currentPage.value = 1
  fetchProducts()
}

// 获取图片URL
const getImageUrl = (images) => {
  if (!images) return ''
  const firstImage = images.split(',')[0]
  const baseAPI = process.env.VUE_APP_BASE_API || '/api'
  return firstImage.startsWith('http') ? firstImage : `${baseAPI}${firstImage}`
}

// 查看商品详情
const viewProductDetail = (id) => {
  router.push({ name: 'ProductDetail', params: { id } })
}

// 添加到购物车
const addToCart = async (product) => {
  try {
    await request.post('/cart/add', null, {
      params: {
        productId: product.id,
        quantity: 1
      },
      successMsg: '已加入购物车'
    })
  } catch (error) {
    console.error('添加到购物车失败:', error)
  }
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchProducts()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProducts()
}

// 页面加载时获取商品列表
onMounted(() => {
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.product-list-page {
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

.product-list-content {
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
  
  :deep(.el-form-item__label) {
    color: #683e35;
    font-weight: 500;
  }
  
  :deep(.el-input__wrapper), :deep(.el-select__wrapper) {
    background-color: #f9f9f9;
    
    &:hover, &.is-focus {
      box-shadow: 0 0 0 1px #FFB6C1;
    }
  }
  
  .search-btn, .reset-btn {
    height: 40px;
    border-radius: 8px;
    transition: all 0.3s ease;
    
    i {
      margin-right: 4px;
    }
  }
  
  .search-btn {
    background-color: #FFB6C1;
    border-color: #FFB6C1;
    color: white;
    
    &:hover {
      background-color: darken(#FFB6C1, 5%);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(255, 182, 193, 0.3);
    }
  }
  
  .reset-btn {
    border-color: #dcdfe6;
    color: #666;
    
    &:hover {
      border-color: #FFB6C1;
      color: #FFB6C1;
    }
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(3, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 480px) {
    grid-template-columns: 1fr;
  }
}

.product-fade-enter-active, .product-fade-leave-active {
  transition: all 0.5s ease;
}

.product-fade-enter-from, .product-fade-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.product-item {
  display: flex;
}

.product-card {
  flex: 1;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  }
  
  &.out-of-stock {
    opacity: 0.7;
    
    &:hover {
      transform: translateY(-5px);
    }
    
    .product-image::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.1);
    }
  }
}

.product-image {
  height: 250px;
  position: relative;
  overflow: hidden;
  
  .el-image {
    width: 100%;
    height: 100%;
    transition: transform 0.6s ease;
  }
  
  &:hover .el-image {
    transform: scale(1.08);
  }
  
  .stock-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 4px 8px;
    background-color: rgba(144, 147, 153, 0.8);
    color: white;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 600;
    
    &.limited {
      background-color: rgba(230, 162, 60, 0.8);
    }
  }
  
  .image-error {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background-color: #f9f9f9;
    color: #909399;
    font-size: 30px;
  }
}

.product-info {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.product-category {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.product-name {
  margin: 0 0 15px;
  font-size: 16px;
  color: #683e35;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  height: 3em;
  cursor: pointer;
  transition: color 0.3s ease;
  
  &:hover {
    color: #FFB6C1;
  }
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex: 1;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 5px;
  
  .price {
    font-size: 20px;
    font-weight: bold;
    color: #f56c6c;
  }
  
  .original-price {
    font-size: 14px;
    color: #909399;
    text-decoration: line-through;
  }
}

.product-sales {
  font-size: 12px;
  color: #909399;
}

.product-action {
  margin-top: 10px;
  
  .cart-button {
    width: 100%;
    height: 42px;
    font-size: 14px;
    background-color: #FFA726;
    color: white;
    border: none;
    border-radius: 8px;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      margin-right: 5px;
    }
    
    &:hover:not(:disabled) {
      background-color: darken(#FFA726, 5%);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
    }
    
    &:disabled {
      background-color: #f0f0f0;
      color: #999;
    }
  }
}

.product-empty {
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

@media screen and (max-width: 768px) {
  .page-header {
    .page-title {
      font-size: 24px;
    }
    
    .page-subtitle {
      font-size: 14px;
    }
  }
  
  .product-image {
    height: 200px;
  }
  
  .product-info {
    padding: 15px;
  }
  
  .product-name {
    font-size: 14px;
    margin-bottom: 10px;
  }
  
  .product-price {
    .price {
      font-size: 18px;
    }
  }
  
  .cart-button {
    height: 38px !important;
    font-size: 13px !important;
  }
}
</style> 