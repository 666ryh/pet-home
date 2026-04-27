<template>
  <div class="home-page">
    <!-- 首页轮播图 -->
    <section class="home-banner">
      <banner-carousel 
        :bannerList="bannerList" 
        height="500px" 
        indicator-position="outside" 
        :interval="5000" 
        arrow="always"
      />
    </section>

    <!-- 公告区域 -->
    <section class="announcement-section">
      <div class="section-container">
        <announcement-list 
          title="最新公告" 
          :limit="5" 
          @view-more="navigateTo('/announcement')"
        />
      </div>
    </section>

    <!-- 核心功能区 -->
    <section class="core-features">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">为爱宠提供温馨的家</h2>
          <p class="section-subtitle">我们致力于为每一位宠物提供最好的服务</p>
        </div>
        
        <div class="feature-cards">
          <div class="feature-card" v-for="(feature, index) in coreFeatures" :key="index" @click="navigateTo(feature.link)">
            <div class="feature-icon" :style="{ backgroundColor: feature.bgColor }">
              <el-icon>
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 宠物推荐区 -->
    <section class="pet-recommend">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">等待被领养的可爱宠物</h2>
          <p class="section-subtitle">这些可爱的生命正期待一个温暖的家</p>
        </div>
        
        <div class="pets-wrapper">
          <div class="pet-list" v-loading="petsLoading">
            <div class="pet-card" v-for="(pet, index) in recommendPets" :key="index" @click="navigateTo(`/pet/${pet.id}`)">
              <div class="pet-img-wrapper">
                <img :src="pet.imageUrl" :alt="pet.name" class="pet-img">
                <div class="pet-status" :class="pet.status === '可领养' ? 'status-available' : 'status-adopted'">
                  {{ pet.status }}
                </div>
              </div>
              <div class="pet-info">
                <h3 class="pet-name">{{ pet.name }}</h3>
                <div class="pet-tags">
                  <span class="pet-tag">{{ pet.category }}</span>
                  <span class="pet-tag">{{ pet.breed }}</span>
                </div>
                <div class="pet-props">
                  <span class="pet-prop">{{ pet.age }}岁</span>
                  <span class="pet-prop">{{ pet.gender }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="more-btn-container">
            <el-button class="more-btn" @click="navigateTo('/pet')">
              查看更多宠物
              <el-icon class="el-icon--right">
                <arrow-right />
              </el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 宠物用品区 -->
    <section class="pet-products">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">精选宠物用品推荐</h2>
          <p class="section-subtitle">为您的爱宠提供高品质的生活必需品</p>
        </div>
        
        <div class="products-wrapper">
          <div class="product-list" v-loading="productsLoading">
            <div class="product-card" v-for="(product, index) in recommendProducts" :key="index" @click="navigateTo(`/product/${product.id}`)">
              <div class="product-img-wrapper">
                <img :src="product.imageUrl" :alt="product.name" class="product-img">
                <div class="product-tag" v-if="product.tag">{{ product.tag }}</div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-brief">{{ product.brief }}</p>
                <div class="product-price-row">
                  <span class="product-price">¥{{ product.price }}</span>
                  <span class="product-orig-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="more-btn-container">
            <el-button class="more-btn" @click="navigateTo('/product')">
              浏览更多用品
              <el-icon class="el-icon--right">
                <arrow-right />
              </el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 服务特色区 -->
    <section class="services-feature">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">专业宠物服务</h2>
          <p class="section-subtitle">为您的爱宠提供一站式全方位呵护</p>
        </div>
        
        <div class="services-cards">
          <div class="service-card" v-for="(service, index) in specialServices" :key="index">
            <div class="service-img-wrapper">
              <img :src="service.imageUrl" :alt="service.title" class="service-img">
              <div class="service-icon" :style="{ backgroundColor: service.iconBg }">
                <el-icon>
                  <component :is="service.icon" />
                </el-icon>
              </div>
            </div>
            <div class="service-content">
              <h3 class="service-title">{{ service.title }}</h3>
              <p class="service-desc">{{ service.description }}</p>
              <el-button class="service-btn" @click="navigateTo(service.link)">
                了解更多
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import BannerCarousel from '@/components/frontend/BannerCarousel.vue';
import AnnouncementList from '@/components/frontend/AnnouncementList.vue';
import axios from 'axios'
import { 
  HomeFilled, 
  ShoppingBag, 
  Service, 
  FirstAid, 
  Scissors, 
  House, 
  Medal,
  ArrowRight
} from '@element-plus/icons-vue';

const router = useRouter();

// 轮播图数据
const bannerList = ref([]);
const fetchBanners = async () => {
  try {
    await request.get('/banner/list', { status: 1 }, { // 只获取状态为启用的轮播图
      onSuccess: (data) => {
        // 如果有轮播图数据，直接使用；否则使用默认数据
        if (data && data.length > 0) {
          bannerList.value = data;
        } else {
          useDefaultBanners();
        }
      },
      onError: () => {
        useDefaultBanners();
        console.error('获取轮播图失败，使用默认数据');
      }
    });
  } catch (error) {
    console.error('获取轮播图失败:', error);
    useDefaultBanners();
  }
};

// 使用默认轮播图数据
const useDefaultBanners = () => {
  bannerList.value = [
    {
      title: '寻找温暖的家',
      imageUrl: 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1470&q=80',
      linkUrl: '/pet'
    },
    {
      title: '专业宠物服务',
      imageUrl: 'https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1460&q=80',
      linkUrl: '/service'
    },
    {
      title: '精选宠物用品',
      imageUrl: 'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1469&q=80',
      linkUrl: '/product'
    }
  ];
};

// 核心功能区数据
const coreFeatures = ref([
  {
    title: '宠物领养',
    description: '为流浪宠物寻找温暖的家，让爱延续',
    icon: HomeFilled,
    link: '/pet',
    bgColor: '#FFB6C1'
  },
  {
    title: '宠物商城',
    description: '精选高品质宠物用品，让爱宠生活更舒适',
    icon: ShoppingBag,
    link: '/product',
    bgColor: '#FFEE93'
  },
  {
    title: '宠物服务',
    description: '专业的医疗、美容、训练等全方位服务',
    icon: Service,
    link: '/service',
    bgColor: '#A0CFD3'
  },
  {
    title: '宠物训练',
    description: '专业的宠物训练，让爱宠健康成长',
    icon: Medal,
    link: '/training',
    bgColor: '#B5EAD7'
  }
]);

// 宠物推荐数据
const recommendPets = ref([]);
const petsLoading = ref(false);

// 宠物用品数据
const recommendProducts = ref([]);
const productsLoading = ref(false);
const baseAPI = process.env.VUE_APP_BASE_API || '/api'
// 服务特色数据
const specialServices = ref([
  {
    title: '宠物服务',
    description: '提供宠物美容、洗护等一站式专业服务',
    icon: Scissors,
    iconBg: '#FF9AA2',
    link: '/service',
    imageUrl: 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1471&q=80'
  },
  {
    title: '宠物寄养',
    description: '安全舒适的寄养环境，让您的爱宠得到精心呵护',
    icon: House,
    iconBg: '#FFDAC1',
    link: '/boarding/apply',
    imageUrl: 'https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1460&q=80'
  },
  {
    title: '宠物训练',
    description: '由专业训练师提供个性化训练课程，培养良好行为习惯',
    icon: Medal,
    iconBg: '#E2F0CB',
    link: '/training',
    imageUrl: 'https://images.unsplash.com/photo-1559190394-df5a28aab5c5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1469&q=80'
  }
]);

// 获取推荐宠物列表
const fetchRecommendPets = async () => {
  petsLoading.value = true;
  try {
    await request.get('/pet/recommend', { limit: 8 }, {
      onSuccess: (data) => {
        recommendPets.value = data.map(pet => ({
          id: pet.id,
          name: pet.name,
          category: pet.categoryName,
          breed: pet.breed,
          age: pet.age,
          gender: pet.gender,
          status: pet.adoptionStatus,
          imageUrl: getImageUrl(pet.images)
        }));
      },
      onError: () => {
        ElMessage.error('获取推荐宠物失败，请稍后再试');
        recommendPets.value = [];
      },
      showDefaultMsg: false
    });
  } catch (error) {
    console.error('获取推荐宠物失败:', error);
    recommendPets.value = [];
  } finally {
    petsLoading.value = false;
  }
};

// 获取宠物用品列表
const fetchRecommendProducts = async () => {

  productsLoading.value = true;
  try {
    const userStr = localStorage.getItem('frontUser');
    const user = userStr ? JSON.parse(userStr) : null;
    const userId = user.id
      await request.get(`/recommend/user/${userId}`, { limit: 8 }, {
      onSuccess: (data) => {
        recommendProducts.value = data.map(product => ({
          id: product.id,
          name: product.name,
          brief: product.description,
          price: product.price,
          imageUrl: getImageUrl(product.images),
          tag: product.status === 1 ? '推荐' : '新品'
        }));
      },
      onError: () => {
        ElMessage.error('获取推荐宠物用品失败，请稍后再试');
        recommendProducts.value = [];
      },
      showDefaultMsg: false
    })
    } catch (error) {
    console.error('获取推荐宠物用品失败:', error);
    recommendProducts.value = [];
    } finally {
    productsLoading.value = false;
    }
  };

// 获取图片URL
const getImageUrl = (images) => {
  if (!images) return '';
  const imageList = images.split(',');
  if (imageList.length === 0) return '';
  
  const firstImage = imageList[0].trim();
  if (firstImage.startsWith('http')) {
    return firstImage;
  }
  return `${baseAPI}${firstImage}`;
};

// 导航方法
const navigateTo = (path) => {
  router.push(path);
};

onMounted(() => {
  fetchBanners();
  fetchRecommendPets();
  fetchRecommendProducts();
});
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background-color: #FFF9E6;
}

/* 轮播图样式 */
.home-banner {
  margin-bottom: 60px;
}

/* 公告区域样式 */
.announcement-section {
  padding: 20px 0;
  margin-bottom: 40px;
  
  .section-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
}

/* 核心功能区样式 */
.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  font-family: 'Nunito Sans', sans-serif;
  font-size: 32px;
  color: #683e35;
  margin-bottom: 12px;
  position: relative;
  display: inline-block;
  
  &::after {
    content: '';
    position: absolute;
    bottom: -8px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(to right, #FFB6C1, #FFEE93);
    border-radius: 3px;
  }
}

.section-subtitle {
  color: #6E4C1E;
  font-size: 16px;
  opacity: 0.8;
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 60px;
}

.feature-card {
  background-color: white;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-10px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  }
}

.feature-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  
  i {
    font-size: 28px;
    color: white;
  }
}

.feature-title {
  font-family: 'Nunito Sans', sans-serif;
  font-size: 20px;
  color: #683e35;
  margin-bottom: 12px;
}

.feature-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

/* 宠物推荐区样式 */
.pet-recommend {
  padding: 50px 0;
  background-color: white;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 15px;
    background: linear-gradient(135deg, #FFB6C1 25%, transparent 25%) -10px 0,
                linear-gradient(225deg, #FFB6C1 25%, transparent 25%) -10px 0,
                linear-gradient(315deg, #FFB6C1 25%, transparent 25%),
                linear-gradient(45deg, #FFB6C1 25%, transparent 25%);
    background-size: 20px 20px;
    background-color: #FFEE93;
  }
  
  .pet-list {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 30px;
  }
  
  .pet-card {
    background-color: #FFF9E6;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
      
      .pet-img {
        transform: scale(1.1);
      }
    }
  }
  
  .pet-img-wrapper {
    height: 200px;
    position: relative;
    overflow: hidden;
  }
  
  .pet-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
  
  .pet-status {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 5px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    color: white;
  }
  
  .status-available {
    background-color: #67c23a;
  }
  
  .status-adopted {
    background-color: #909399;
  }
  
  .pet-info {
    padding: 15px;
  }
  
  .pet-name {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 18px;
    color: #683e35;
    margin: 0 0 8px;
  }
  
  .pet-tags {
    display: flex;
    gap: 5px;
    margin-bottom: 8px;
    
    .pet-tag {
      background-color: #FFB6C1;
      color: white;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
    }
  }
  
  .pet-props {
    display: flex;
    gap: 15px;
    
    .pet-prop {
      font-size: 14px;
      color: #666;
    }
  }
  
  .more-btn-container {
    text-align: center;
  }
  
  .more-btn {
    background-color: transparent;
    color: #FFA726;
    border: 1px solid #FFA726;
    padding: 10px 20px;
    border-radius: 30px;
    transition: all 0.3s ease;
    
    &:hover {
      background-color: #FFA726;
      color: white;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(255, 167, 38, 0.15);
    }
    
    i {
      margin-left: 5px;
      transition: transform 0.3s ease;
    }
    
    &:hover i {
      transform: translateX(3px);
    }
  }
}

/* 宠物用品区样式 */
.pet-products {
  padding: 70px 0;
  background-color: #FFF9E6;
  position: relative;
  
  .products-wrapper {
    width: 100%;
  }
  
  .product-list {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 30px;
  }
  
  .product-card {
    background-color: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
      
      .product-img {
        transform: scale(1.1);
      }
    }
  }
  
  .product-img-wrapper {
    height: 200px;
    position: relative;
    overflow: hidden;
  }
  
  .product-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
  
  .product-tag {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 5px 10px;
    border-radius: 20px;
    font-size: 12px;
    background-color: #FFA726;
    color: white;
  }
  
  .product-info {
    padding: 15px;
  }
  
  .product-name {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 18px;
    color: #683e35;
    margin: 0 0 8px;
  }
  
  .product-brief {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
  }
  
  .product-price-row {
    display: flex;
    align-items: center;
    margin-top: 10px;
  }
  
  .product-price {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 16px;
    color: #683e35;
    font-weight: 600;
  }
  
  .product-orig-price {
    font-size: 14px;
    color: #999;
    text-decoration: line-through;
    margin-left: 10px;
  }
  
  .more-btn-container {
    text-align: center;
  }
  
  .more-btn {
    background-color: transparent;
    color: #FFA726;
    border: 1px solid #FFA726;
    padding: 10px 20px;
    border-radius: 30px;
    transition: all 0.3s ease;
    
    &:hover {
      background-color: #FFA726;
      color: white;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(255, 167, 38, 0.15);
    }
    
    i {
      margin-left: 5px;
      transition: transform 0.3s ease;
    }
    
    &:hover i {
      transform: translateX(3px);
    }
  }
}

/* 服务特色区样式 */
.services-feature {
  padding: 70px 0;
  background-color: #FFF9E6;
  position: relative;
  
  .services-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 30px;
  }
  
  .service-card {
    background-color: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
      
      .service-img {
        transform: scale(1.1);
      }
    }
  }
  
  .service-img-wrapper {
    height: 180px;
    position: relative;
    overflow: hidden;
  }
  
  .service-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
  
  .service-icon {
    position: absolute;
    bottom: -20px;
    left: 20px;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    
    i {
      font-size: 20px;
      color: white;
    }
  }
  
  .service-content {
    padding: 25px 20px 20px;
  }
  
  .service-title {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 20px;
    color: #683e35;
    margin: 0 0 10px;
  }
  
  .service-desc {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 20px;
    height: 66px;
    overflow: hidden;
  }
  
  .service-btn {
    color: #FFA726;
    background-color: transparent;
    border: none;
    padding: 0;
    font-weight: 600;
    transition: all 0.3s ease;
    
    &:hover {
      color: #FF9800;
      text-decoration: underline;
    }
  }
}

/* 响应式设计 */
@media screen and (max-width: 992px) {
  .feature-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .pet-list {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .product-list {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .services-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media screen and (max-width: 768px) {
  .section-title {
    font-size: 28px;
  }
  
  .section-subtitle {
    font-size: 14px;
  }
  
  .pet-list {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .product-list {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .feature-title {
    font-size: 18px;
  }
  
  .feature-desc {
    font-size: 13px;
  }
  
  .service-title {
    font-size: 18px;
  }
  
  .service-desc {
    font-size: 13px;
  }
}

@media screen and (max-width: 576px) {
  .feature-cards {
    grid-template-columns: 1fr;
  }
  
  .pet-list {
    grid-template-columns: 1fr;
  }
  
  .product-list {
    grid-template-columns: 1fr;
  }
  
  .services-cards {
    grid-template-columns: 1fr;
  }
  
  .section-title {
    font-size: 24px;
  }
}
</style>


