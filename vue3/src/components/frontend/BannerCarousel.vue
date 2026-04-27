<template>
  <div class="banner-carousel">
    <el-carousel 
      :height="height" 
      :indicator-position="indicatorPosition" 
      :interval="interval" 
      :arrow="arrow"
      :autoplay="autoplay"
      :trigger="trigger"
    >
      <el-carousel-item v-for="(item, index) in bannerList" :key="index">
        <div 
          class="banner-item" 
          :style="{ 
            backgroundImage: `url(${getImageUrl(item.imageUrl)})`,
            backgroundPosition: 'center center'
          }"
          @click="handleClick(item)"
        >
          <div class="banner-content" v-if="showContent && item.title">
            <h2 class="banner-title">{{ item.title }}</h2>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 没有轮播图时的占位 -->
    <div class="empty-banner" v-if="!bannerList || bannerList.length === 0">
      <el-empty description="暂无轮播图" :image-size="200">
        <template #description>
          <p>暂未配置轮播图</p>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 组件接收的属性
const props = defineProps({
  // 轮播图数据
  bannerList: {
    type: Array,
    default: () => []
  },
  // 轮播图高度
  height: {
    type: String,
    default: '500px'
  },
  // 指示器位置
  indicatorPosition: {
    type: String,
    default: 'outside'
  },
  // 轮播间隔时间，单位毫秒
  interval: {
    type: Number,
    default: 5000
  },
  // 箭头显示时机：always/hover/never
  arrow: {
    type: String,
    default: 'hover'
  },
  // 是否自动播放
  autoplay: {
    type: Boolean,
    default: true
  },
  // 指示器触发方式：hover/click
  trigger: {
    type: String,
    default: 'click'
  },
  // 是否显示内容
  showContent: {
    type: Boolean,
    default: true
  },
  // API前缀
  baseAPI: {
    type: String,
    default: process.env.VUE_APP_BASE_API || '/api'
  }
});

// 方法: 处理点击事件
const handleClick = (banner) => {
  if (banner.linkUrl) {
    // 判断是否是外部链接
    if (banner.linkUrl.startsWith('http') || banner.linkUrl.startsWith('https')) {
      window.open(banner.linkUrl, '_blank');
    } else {
      // 内部链接，路由跳转
      router.push(banner.linkUrl);
    }
  }
};

// 处理图片URL，添加API前缀
const getImageUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http') || url.startsWith('data:') || url.startsWith('blob:')) {
    return url;
  }
  return props.baseAPI + url;
};
</script>

<style lang="scss" scoped>
.banner-carousel {
  position: relative;
  margin-bottom: 40px;
  
  :deep(.el-carousel__container) {
    border-radius: 0 0 20px 20px;
    overflow: hidden;
  }
  
  :deep(.el-carousel__arrow) {
    background-color: rgba(255, 182, 193, 0.7);
    
    &:hover {
      background-color: rgba(255, 182, 193, 0.9);
    }
  }
  
  :deep(.el-carousel__indicators) {
    bottom: 20px;
    
    .el-carousel__button {
      background-color: #FFB6C1;
      width: 12px;
      height: 12px;
      border-radius: 6px;
    }
  }
}

.banner-item {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  cursor: pointer;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(to right, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.2) 100%);
  }
}

.banner-content {
  position: absolute;
  left: 10%;
  top: 50%;
  transform: translateY(-50%);
  color: white;
  max-width: 500px;
  text-align: left;
  z-index: 2;
}

.banner-title {
  font-family: 'Nunito Sans', sans-serif;
  font-size: 42px;
  margin-bottom: 16px;
  font-weight: 700;
}

.empty-banner {
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .banner-title {
    font-size: 36px;
  }
}

@media (max-width: 768px) {
  .banner-content {
    left: 5%;
    max-width: 80%;
  }
  
  .banner-title {
    font-size: 30px;
  }
}

@media (max-width: 576px) {
  .banner-title {
    font-size: 26px;
  }
}
</style> 