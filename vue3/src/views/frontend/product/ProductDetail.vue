<template>
  <div class="product-detail-page" v-loading="loading">
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="product-content" v-if="product">
      <div class="breadcrumb-container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/products' }">宠物商城</el-breadcrumb-item>
          <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="product-container">
        <div class="product-gallery">
          <div class="gallery-main">
            <el-carousel 
              :interval="4000" 
              height="450px"
              indicator-position="outside"
              arrow="always"
              class="carousel-container"
              ref="carouselRef"
              :initial-index="activeIndex"
              @change="handleCarouselChange">
              <el-carousel-item v-for="(image, index) in productImages" :key="index" class="carousel-item">
                <el-image 
                  :src="getImageUrl(image)" 
                  fit="cover"
                  class="carousel-image"
                  :preview-src-list="productImageUrls"
                      :preview-teleported="true"
                  :initial-index="index">
                </el-image>
              </el-carousel-item>
            </el-carousel>
          </div>
          
          <div class="gallery-thumbnails" v-if="productImages.length > 1">
            <div 
              v-for="(image, index) in productImages" 
              :key="index" 
              class="thumbnail-item" 
              :class="{ active: index === activeIndex }"
              @click="setActiveImage(index)">
              <img :src="getImageUrl(image)" :alt="`${product.name} - 图片 ${index + 1}`">
            </div>
          </div>
        </div>
        
        <div class="product-details">
          <div class="product-header">
            <h1 class="product-name">{{ product.name }}</h1>
            <div class="product-category">
              <el-tag size="small" effect="plain" class="category-tag">{{ product.category }}</el-tag>
            </div>
          </div>
          
          <div class="product-price-container">
            <div class="price-label">价格</div>
            <div class="price-value">
              <span class="currency">¥</span>
              <span class="amount">{{ product.price }}</span>
            </div>
          </div>
          
          <div class="product-meta">
            <div class="meta-item">
              <span class="meta-label">库存:</span>
              <span class="meta-value" :class="{ 'stock-warning': product.stock < 10 }">
                {{ product.stock }} 件
                <span v-if="product.stock < 10" class="low-stock-badge">库存紧张</span>
              </span>
            </div>
            
            <div class="meta-item">
              <span class="meta-label">配送:</span>
              <span class="meta-value">全国包邮</span>
            </div>
          </div>
          
          <div class="product-divider"></div>
          
          <div class="product-action">
            <div class="quantity-selector">
              <div class="quantity-label">数量:</div>
              <el-input-number 
                v-model="quantity" 
                :min="1" 
                :max="product.stock" 
                size="large"
                :disabled="product.stock <= 0"
                class="quantity-input">
              </el-input-number>
            </div>
            
            <div class="action-buttons">
              <el-button 
                class="cart-button"
                :disabled="product.stock <= 0" 
                @click="addToCart"
                :loading="addingToCart">
                <el-icon class="button-icon"><shopping-cart /></el-icon>
                加入购物车
              </el-button>
              
              <el-button 
                class="buy-button"
                :disabled="product.stock <= 0" 
                @click="buyNow"
                :loading="buying">
                立即购买
              </el-button>
            </div>
          </div>
          
          <div class="product-features" v-if="product.features">
            <div class="features-title">产品特点</div>
            <ul class="features-list">
              <li v-for="(feature, index) in productFeatures" :key="index">
                <span class="feature-dot">•</span>
                {{ feature }}
              </li>
            </ul>
          </div>
        </div>
      </div>
      
      <div class="product-tabs">
        <el-tabs type="border-card" class="custom-tabs">
          <el-tab-pane label="商品详情">
            <div class="tab-content description-content">
              <div v-if="product.description" class="description-text">
                {{ product.description }}
              </div>
              <div v-else class="no-content">
                <el-empty description="暂无商品详情" />
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="用户评价">
            <div class="tab-content reviews-content">
              <div v-if="reviews.length > 0" class="reviews-container">
                <div class="reviews-summary">
                  <div class="summary-rating">
                    <div class="rating-average">{{ averageRating.toFixed(1) }}</div>
                    <el-rate v-model="averageRating" disabled show-score text-color="#ff9900"></el-rate>
                    <div class="rating-count">{{ reviews.length }}条评价</div>
                  </div>
                </div>
                
                <el-divider></el-divider>
                
                <div class="reviews-list">
                  <div v-for="(review, index) in reviews" :key="index" class="review-item">
                    <div class="review-header">
                      <div class="review-user">
                        <el-avatar 
                          :size="40" 
                          :src="review.isAnonymous ? 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png' : getImageUrl(review.user?.avatar || '')"
                        ></el-avatar>
                        <div class="user-info">
                          <div class="user-name">{{ review.isAnonymous ? '匿名用户' : (review.user?.username || `用户${review.userId}`) }}</div>
                          <div class="review-time">{{ formatDate(review.createTime) }}</div>
                        </div>
                      </div>
                      <div class="review-rating">
                        <el-rate v-model="review.rating" disabled></el-rate>
                      </div>
                    </div>
                    
                    <div class="review-content">{{ review.content }}</div>
                    
                    <div v-if="review.images" class="review-images">
                      <el-image 
                        v-for="(img, imgIndex) in review.images.split(',')"
                        :key="imgIndex"
                        :src="getImageUrl(img)"
                        :preview-teleported="true"
                        :preview-src-list="review.images.split(',').map(img => getImageUrl(img))"
                        fit="cover"
                        class="review-image"
                      />
                    </div>
                    
                    <div v-if="review.reply" class="merchant-reply">
                      <div class="reply-header">
                        <el-icon><ChatDotRound /></el-icon>
                        <span>商家回复</span>
                      </div>
                      <div class="reply-content">{{ review.reply }}</div>
                      <div class="reply-time">{{ formatDate(review.replyTime) }}</div>
                    </div>
                  </div>
                </div>
                
                <div class="pagination-container" v-if="totalReviews > pageSize">
                  <el-pagination
                    :current-page="currentPage"
                    :page-size="pageSize"
                    :total="totalReviews"
                    layout="prev, pager, next"
                    @current-change="handlePageChange"
                    background
                  />
                </div>
              </div>
              <div v-else class="no-content">
                <el-empty description="暂无评价" />
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="购买须知">
            <div class="tab-content">
              <div class="notice-container">
                <div class="notice-item">
                  <div class="notice-title">
                    <span class="notice-icon">📦</span>
                    物流配送
                  </div>
                  <div class="notice-content">
                    商品默认顺丰配送，偏远地区可能产生额外运费，请谅解。
                  </div>
                </div>
                <div class="notice-item">
                  <div class="notice-title">
                    <span class="notice-icon">🔄</span>
                    退换政策
                  </div>
                  <div class="notice-content">
                    自收到商品之日起7天内，如产品未使用、包装完好，可申请退换货。
                  </div>
                </div>
                <div class="notice-item">
                  <div class="notice-title">
                    <span class="notice-icon">💳</span>
                    支付方式
                  </div>
                  <div class="notice-content">
                    支持微信支付、支付宝、银联等多种支付方式。
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    
    <el-empty v-else-if="!loading" description="商品不存在或已下架" class="product-empty"></el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, ChatDotRound } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 路由
const route = useRoute()
const router = useRouter()
const productId = computed(() => route.params.id)

// 数据定义
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const addingToCart = ref(false)
const buying = ref(false)
const activeIndex = ref(0)
const carouselRef = ref(null)

// 评价相关数据
const reviews = ref([])
const averageRating = ref(5)
const totalReviews = ref(0)
const currentPage = ref(1)
const pageSize = ref(5)

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    await request.get(`/product/${productId.value}`, null, {
      onSuccess: (res) => {
        product.value = res
        // 设置默认数量为1
        quantity.value = 1
        
        // 获取评价数据
        fetchProductReviews()
      }
    })
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 获取商品评价
const fetchProductReviews = async () => {
  try {
    await request.get(`/order/review/product/${productId.value}`, null, {
      onSuccess: (res) => {
        reviews.value = res || []
        totalReviews.value = reviews.value.length
        
        // 计算平均评分
        if (reviews.value.length > 0) {
          const totalRating = reviews.value.reduce((sum, review) => sum + review.rating, 0)
          averageRating.value = totalRating / reviews.value.length
        } else {
          averageRating.value = 5
        }
      }
    })
  } catch (error) {
    console.error('获取商品评价失败:', error)
    reviews.value = []
  }
}

// 处理评价分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  // 如果后端支持分页查询，可以在这里调用分页接口
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 商品图片列表
const productImages = computed(() => {
  if (!product.value || !product.value.images) return []
  return product.value.images.split(',')
})

// 商品图片URL列表 (用于图片预览)
const productImageUrls = computed(() => {
  return productImages.value.map(image => getImageUrl(image))
})

// 商品特点列表
const productFeatures = computed(() => {
  if (!product.value || !product.value.features) return []
  return product.value.features.split(',').map(f => f.trim())
})

// 设置当前激活的图片
const setActiveImage = (index) => {
  activeIndex.value = index
  // 使用Element Plus的正确API切换轮播图
  if (carouselRef.value) {
    // 使用setActiveItem方法切换到指定索引
    carouselRef.value.setActiveItem(index)
  }
}

// 处理轮播图变化事件
const handleCarouselChange = (index) => {
  activeIndex.value = index
}

// 获取图片URL
const getImageUrl = (image) => {
  if (!image) return ''
  const baseAPI = process.env.VUE_APP_BASE_API || '/api'
  return image.startsWith('http') ? image : `${baseAPI}${image}`
}

// 添加到购物车
const addToCart = async () => {
  if (quantity.value <= 0 || quantity.value > product.value.stock) {
    ElMessage.warning('请选择正确的商品数量')
    return
  }
  
  addingToCart.value = true
  try {
    await request.post('/cart/add', null, {
      params: {
        productId: product.value.id,
        quantity: quantity.value
      },
      successMsg: '已加入购物车'
    })
  } catch (error) {
    console.error('添加到购物车失败:', error)
  } finally {
    addingToCart.value = false
  }
}

// 立即购买
const buyNow = async () => {
  if (quantity.value <= 0 || quantity.value > product.value.stock) {
    ElMessage.warning('请选择正确的商品数量')
    return
  }
  
  buying.value = true
  try {
    // 先加入购物车
    await request.post('/cart/add', null, {
      params: {
        productId: product.value.id,
        quantity: quantity.value
      }
    })
    
    // 然后跳转到购物车页面
    router.push('/cart')
  } catch (error) {
    console.error('立即购买失败:', error)
    ElMessage.error('立即购买失败')
  } finally {
    buying.value = false
  }
}

// 页面加载时获取商品详情
onMounted(() => {
  fetchProductDetail()
  
  // 确保轮播图引用正确获取
  setTimeout(() => {
    console.log('轮播图引用:', carouselRef.value)
  }, 1000)
})
</script>

<style lang="scss" scoped>
.product-detail-page {
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

.product-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.breadcrumb-container {
  margin-bottom: 20px;
  
  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      color: #683e35;
      font-weight: normal;
      
      &.is-link:hover {
        color: #FFB6C1;
      }
    }
    
    &:last-child .el-breadcrumb__inner {
      font-weight: 600;
    }
  }
}

.product-container {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
  
  @media (max-width: 992px) {
    flex-direction: column;
  }
}

.product-gallery {
  flex: 1;
  max-width: 600px;
  
  @media (max-width: 992px) {
    max-width: 100%;
  }
}

.gallery-main {
  margin-bottom: 20px;
  
  .carousel-container {
    :deep(.el-carousel__arrow) {
      background-color: rgba(255, 182, 193, 0.8);
      border-radius: 50%;
      
      &:hover {
        background-color: #FFB6C1;
      }
    }
    
    :deep(.el-carousel__indicators) {
      .el-carousel__button {
        background-color: #FFEE93;
      }
    }
  }
  
  .carousel-item {
    border-radius: 12px;
    overflow: hidden;
  }
  
  .carousel-image {
    width: 100%;
    height: 100%;
    border-radius: 12px;
  }
}

.gallery-thumbnails {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 10px;
  
  &::-webkit-scrollbar {
    height: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: #FFEE93;
    border-radius: 2px;
  }
  
  &::-webkit-scrollbar-track {
    background-color: #f5f5f5;
    border-radius: 2px;
  }
  
  .thumbnail-item {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.3s ease;
    flex-shrink: 0;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    }
    
    &.active {
      border-color: #FFB6C1;
    }
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.product-details {
  flex: 1;
  background-color: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.product-header {
  margin-bottom: 20px;
  
  .product-name {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 28px;
    color: #683e35;
    margin: 0 0 10px;
  }
  
  .category-tag {
    background-color: rgba(255, 238, 147, 0.3);
    color: #b29860;
    border-color: rgba(178, 152, 96, 0.2);
  }
}

.product-price-container {
  display: flex;
  flex-direction: column;
  margin-bottom: 15px;
  
  .price-label {
    font-size: 14px;
    color: #888;
    margin-bottom: 5px;
  }
  
  .price-value {
    display: flex;
    align-items: baseline;
    
    .currency {
      font-size: 20px;
      color: #f56c6c;
      margin-right: 2px;
    }
    
    .amount {
      font-size: 32px;
      font-weight: bold;
      color: #f56c6c;
      font-family: 'Roboto', sans-serif;
    }
  }
}

.product-meta {
  margin-bottom: 20px;
  
  .meta-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
    
    .meta-label {
      min-width: 60px;
      font-size: 14px;
      color: #666;
    }
    
    .meta-value {
      font-size: 14px;
      color: #333;
    }
    
    .stock-warning {
      color: #e6a23c;
    }
    
    .low-stock-badge {
      display: inline-block;
      margin-left: 8px;
      padding: 2px 6px;
      background-color: #fff3e0;
      color: #e6a23c;
      font-size: 12px;
      border-radius: 4px;
      border: 1px solid #ffd6a0;
    }
  }
}

.product-divider {
  height: 1px;
  background: linear-gradient(to right, transparent, #e0e0e0, transparent);
  margin: 20px 0;
}

.product-action {
  margin-bottom: 30px;
  
  .quantity-selector {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    .quantity-label {
      min-width: 50px;
      font-size: 16px;
      color: #333;
      margin-right: 10px;
    }
    
    .quantity-input {
      width: 140px;
      
      :deep(.el-input-number__decrease),
      :deep(.el-input-number__increase) {
        background-color: #f9f9f9;
        color: #666;
        
        &:hover {
          color: #FFB6C1;
        }
      }
      
      :deep(.el-input__wrapper) {
        &.is-focus {
          box-shadow: 0 0 0 1px #FFB6C1;
        }
      }
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 20px;
    
    @media (max-width: 576px) {
      flex-direction: column;
      gap: 10px;
    }
    
    .cart-button, .buy-button {
      flex: 1;
      height: 46px;
      font-size: 16px;
      border-radius: 8px;
      transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
      
      &:hover {
        transform: translateY(-2px);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
    
    .cart-button {
      background-color: #fff3e0;
      color: #e6a23c;
      border-color: #e6a23c;
      
      .button-icon {
        margin-right: 5px;
      }
      
      &:hover {
        background-color: #ffefd5;
        box-shadow: 0 4px 12px rgba(230, 162, 60, 0.2);
      }
    }
    
    .buy-button {
      background-color: #FFA726;
      color: white;
      border: none;
      
      &:hover {
        background-color: #ff9800;
        box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
      }
    }
  }
}

.product-features {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  
  .features-title {
    font-size: 16px;
    font-weight: 600;
    color: #683e35;
    margin-bottom: 10px;
  }
  
  .features-list {
    list-style-type: none;
    padding: 0;
    margin: 0;
    
    li {
      display: flex;
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
      
      .feature-dot {
        color: #FFB6C1;
        margin-right: 8px;
        font-size: 18px;
      }
    }
  }
}

.product-tabs {
  margin-bottom: 40px;
  
  .custom-tabs {
    :deep(.el-tabs__nav) {
      background-color: #f9f9f9;
    }
    
    :deep(.el-tabs__item) {
      color: #666;
      
      &.is-active {
        color: #FFB6C1;
      }
      
      &:hover {
        color: #FFB6C1;
      }
    }
    
    :deep(.el-tabs__active-bar) {
      background-color: #FFB6C1;
    }
  }
  
  .tab-content {
    padding: 30px 20px;
    min-height: 200px;
  }
  
  .description-text {
    font-size: 15px;
    line-height: 1.8;
    color: #333;
    white-space: pre-wrap;
  }
  
  .notice-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
    
    .notice-item {
      background-color: #f9f9f9;
      border-radius: 8px;
      padding: 16px;
      
      .notice-title {
        font-weight: 600;
        font-size: 16px;
        color: #683e35;
        margin-bottom: 10px;
        display: flex;
        align-items: center;
        
        .notice-icon {
          margin-right: 8px;
          font-size: 18px;
        }
      }
      
      .notice-content {
        font-size: 14px;
        color: #666;
        line-height: 1.6;
      }
    }
  }
}

.product-empty {
  margin-top: 100px;
}

.no-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

/* 评价样式 */
.reviews-content {
  padding: 20px 0;
}

.reviews-summary {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  
  .summary-rating {
    display: flex;
    align-items: center;
    
    .rating-average {
      font-size: 36px;
      font-weight: bold;
      color: #ff9900;
      margin-right: 15px;
    }
    
    .rating-count {
      margin-left: 15px;
      color: #666;
      font-size: 14px;
    }
  }
}

.reviews-list {
  .review-item {
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      
      .review-user {
        display: flex;
        align-items: center;
        
        .user-info {
          margin-left: 10px;
          
          .user-name {
            font-weight: 500;
            color: #333;
            margin-bottom: 5px;
          }
          
          .review-time {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }
    
    .review-content {
      font-size: 14px;
      line-height: 1.6;
      color: #333;
      margin-bottom: 15px;
      white-space: pre-wrap;
    }
    
    .review-images {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-bottom: 15px;
      
      .review-image {
        width: 100px;
        height: 100px;
        border-radius: 8px;
        object-fit: cover;
        cursor: pointer;
        transition: transform 0.3s ease;
        
        &:hover {
          transform: scale(1.05);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
      }
    }
    
    .merchant-reply {
      background-color: #f9f9f9;
      border-radius: 8px;
      padding: 15px;
      margin-top: 10px;
      
      .reply-header {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        font-weight: 500;
        color: #666;
        
        .el-icon {
          color: #FFB6C1;
          margin-right: 5px;
        }
      }
      
      .reply-content {
        font-size: 14px;
        color: #333;
        margin-bottom: 5px;
      }
      
      .reply-time {
        font-size: 12px;
        color: #999;
        text-align: right;
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
    --el-pagination-hover-color: #FFB6C1;
    
    .el-pager li.is-active {
      background-color: #FFB6C1;
      color: white;
    }
    
    .el-pager li:hover {
      color: #FFB6C1;
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
  .product-details {
    padding: 20px;
  }
  
  .product-header .product-name {
    font-size: 22px;
  }
  
  .price-value .amount {
    font-size: 26px;
  }
  
  .gallery-thumbnails .thumbnail-item {
    width: 60px;
    height: 60px;
  }
}
</style> 