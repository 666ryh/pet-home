<template>
  <div class="auth-container">
    <div class="auth-content">
      <div class="auth-left">
        <div class="auth-illustration">
          <img src="@/assets/666.png" alt="宠物之家" />
          <div class="floating-elements">
            <div class="heart heart-1">💖</div>
            <div class="heart heart-2">🐕</div>
            <div class="heart heart-3">🐱</div>
          </div>
        </div>
        <div class="auth-text">
          <h2>欢迎来到宠物之家</h2>
          <p>您的爱宠，我们的责任</p>
          <div class="feature-tags">
            <span class="tag">专业护理</span>
            <span class="tag">贴心服务</span>
            <span class="tag">安全可靠</span>
          </div>
        </div>
      </div>
      
      <div class="auth-box">
        <div class="auth-header" v-if="showHeader">
          <div class="logo">
            <div class="logo-inner">🐾</div>
            <div class="logo-glow"></div>
          </div>
          <h1 class="title">宠物之家</h1>
          <div class="subtitle">PET HOME</div>
        </div>
        
        <el-form :model="formData" :rules="rules" ref="formRef" class="auth-form">
          <slot name="form-items"></slot>
          
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit" class="auth-button">
              <span class="button-text">{{ submitText }}</span>
              <div class="button-ripple"></div>
            </el-button>
          </el-form-item>
          
          <div class="auth-links">
            <slot name="auth-links"></slot>
          </div>
        </el-form>
      </div>
    </div>
    
    <div class="auth-decorations">
      <div class="auth-bubble bubble-1"></div>
      <div class="auth-bubble bubble-2"></div>
      <div class="auth-bubble bubble-3"></div>
      <div class="auth-bubble bubble-4"></div>
      <div class="auth-bubble bubble-5"></div>
      <div class="auth-paw paw-1">🐾</div>
      <div class="auth-paw paw-2">🐾</div>
      <div class="auth-paw paw-3">🐾</div>
      <div class="auth-paw paw-4">🐕</div>
      <div class="auth-paw paw-5">🐱</div>
    </div>
    
    <!-- 新增粒子效果 -->
    <div class="particles">
      <div class="particle" v-for="i in 20" :key="i" :style="getParticleStyle(i)"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  formData: {
    type: Object,
    required: true
  },
  rules: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  submitText: {
    type: String,
    default: '提交'
  },
  showHeader: {
    type: Boolean,
    default: true
  }
})

const formRef = ref(null)

const emit = defineEmits(['submit'])

const handleSubmit = () => {
  formRef.value.validate(valid => {
    if (valid) {
      emit('submit', formRef)
    }
  })
}

// 生成粒子样式
const getParticleStyle = (index) => {
  const delay = Math.random() * 5
  const duration = 3 + Math.random() * 4
  const left = Math.random() * 100
  const size = 2 + Math.random() * 4
  
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    width: `${size}px`,
    height: `${size}px`
  }
}

defineExpose({
  formRef
})
</script>

<style lang="scss" scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFB6C1 0%, #FFC0CB 50%, #FFE4E1 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: 
      radial-gradient(circle at 20% 80%, rgba(255, 105, 180, 0.2) 0%, transparent 50%),
      radial-gradient(circle at 80% 20%, rgba(255, 182, 193, 0.3) 0%, transparent 50%),
      radial-gradient(circle at 40% 40%, rgba(255, 192, 203, 0.2) 0%, transparent 50%);
    z-index: 1;
  }
}

.auth-content {
  display: flex;
  width: 100%;
  max-width: 1100px;
  position: relative;
  z-index: 2;
  box-shadow: 
    0 25px 50px rgba(255, 105, 180, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.auth-left {
  width: 50%;
  background: linear-gradient(135deg, #FFB6C1 0%, #FFC0CB 30%, #FFCCCB 70%, #FFE4E1 100%);
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.2) 50%, transparent 70%);
    animation: shimmer 3s ease-in-out infinite;
  }
  
  .auth-illustration {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    position: relative;
    
    img {
      max-width: 100%;
      max-height: 320px;
      object-fit: contain;
      filter: drop-shadow(0 10px 20px rgba(255, 105, 180, 0.2));
      transition: transform 0.3s ease;
      
      &:hover {
        transform: scale(1.05) rotate(2deg);
      }
    }
    
    .floating-elements {
      position: absolute;
      width: 100%;
      height: 100%;
      
      .heart {
        position: absolute;
        font-size: 24px;
        animation: float 4s ease-in-out infinite;
        
        &.heart-1 {
          top: 20%;
          left: 10%;
          animation-delay: 0s;
        }
        
        &.heart-2 {
          top: 60%;
          right: 15%;
          animation-delay: 1s;
        }
        
        &.heart-3 {
          bottom: 30%;
          left: 20%;
          animation-delay: 2s;
        }
      }
    }
  }
  
  .auth-text {
    text-align: center;
    padding: 30px 0;
    
    h2 {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 32px;
      color: #C2185B;
      margin-bottom: 15px;
      font-weight: 700;
      text-shadow: 0 2px 4px rgba(194, 24, 91, 0.1);
    }
    
    p {
      font-family: 'Open Sans', sans-serif;
      font-size: 18px;
      color: #E91E63;
      opacity: 0.9;
      margin-bottom: 20px;
    }
    
    .feature-tags {
      display: flex;
      justify-content: center;
      gap: 10px;
      flex-wrap: wrap;
      
      .tag {
        background: rgba(255, 255, 255, 0.95);
        color: #C2185B;
        padding: 6px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 500;
        box-shadow: 0 2px 8px rgba(255, 105, 180, 0.2);
        transition: all 0.3s ease;
        border: 1px solid rgba(255, 105, 180, 0.2);
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(255, 105, 180, 0.3);
          background: rgba(255, 240, 245, 0.98);
          border-color: rgba(255, 105, 180, 0.4);
        }
      }
    }
  }
}

.auth-box {
  width: 50%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(255, 182, 193, 0.1) 0%, rgba(255, 192, 203, 0.05) 100%);
    pointer-events: none;
  }
}

.auth-header {
  text-align: center;
  margin-bottom: 40px;

  .logo {
    position: relative;
    display: inline-block;
    margin-bottom: 20px;
    
    .logo-inner {
      font-size: 48px;
      background: linear-gradient(135deg, #FF69B4 0%, #FF1493 100%);
      width: 90px;
      height: 90px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto;
      box-shadow: 
        0 8px 32px rgba(255, 105, 180, 0.5),
        inset 0 2px 0 rgba(255, 255, 255, 0.3);
      animation: logoFloat 3s ease-in-out infinite;
      position: relative;
      z-index: 2;
      border: 2px solid rgba(255, 255, 255, 0.3);
    }
    
    .logo-glow {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 100px;
      height: 100px;
      background: radial-gradient(circle, rgba(255, 105, 180, 0.4) 0%, transparent 70%);
      border-radius: 50%;
      animation: pulse 2s ease-in-out infinite;
      z-index: 1;
    }
  }

  .title {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 32px;
    color: #C2185B;
    margin: 0 0 8px;
    font-weight: 700;
    background: linear-gradient(135deg, #FF69B4 0%, #C2185B 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .subtitle {
    font-family: 'Open Sans', sans-serif;
    font-size: 14px;
    color: #E91E63;
    letter-spacing: 3px;
    opacity: 0.8;
    font-weight: 300;
  }
}

.auth-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
  
  :deep(.el-input) {
    --el-input-hover-border-color: #FF69B4;
    --el-input-focus-border-color: #FF69B4;
    
    .el-input__wrapper {
      border-radius: 12px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background: rgba(255, 255, 255, 0.9);
      border: 2px solid transparent;
      box-shadow: 0 2px 8px rgba(255, 105, 180, 0.1);
      height: 50px;
      
      &.is-focus {
        border-color: #FF69B4;
        background: #fff;
        box-shadow: 
          0 0 0 3px rgba(255, 105, 180, 0.15),
          0 4px 12px rgba(255, 105, 180, 0.2);
        transform: translateY(-1px);
      }

      &:hover {
        background: #fff;
        box-shadow: 0 4px 12px rgba(255, 105, 180, 0.15);
        transform: translateY(-1px);
        border-color: rgba(255, 105, 180, 0.3);
      }
      
      .el-input__inner {
        font-size: 16px;
        color: #C2185B;
        
        &::placeholder {
          color: #F8BBD9;
        }
      }
      
      .el-input__prefix-inner {
        color: #E91E63;
        font-size: 18px;
      }
    }
  }
  
  .auth-button {
    width: 100%;
    height: 56px;
    border-radius: 12px;
    background: linear-gradient(135deg, #FF69B4 0%, #FF1493 100%);
    border: none;
    font-size: 18px;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
    box-shadow: 0 4px 15px rgba(255, 105, 180, 0.5);
    
    .button-text {
      position: relative;
      z-index: 2;
      color: #fff;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    }
    
    .button-ripple {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, transparent 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
      z-index: 1;
    }
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 25px rgba(255, 105, 180, 0.6);
      background: linear-gradient(135deg, #FF1493 0%, #DC143C 100%);
      
      .button-ripple {
        opacity: 1;
      }
    }
    
    &:active {
      transform: translateY(-1px);
    }
    
    &.is-loading {
      .button-ripple {
        opacity: 0.5;
      }
    }
  }
}

.auth-links {
  margin-top: 30px;
  text-align: center;
  font-family: 'Open Sans', sans-serif;
  
  a {
    color: #FF69B4;
    text-decoration: none;
    margin: 0 8px;
    transition: all 0.3s ease;
    font-weight: 500;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 0;
      height: 2px;
      background: linear-gradient(135deg, #FF69B4 0%, #FF1493 100%);
      transition: width 0.3s ease;
    }
    
    &:hover {
      color: #FF1493;
      
      &::after {
        width: 100%;
      }
    }
  }
}

.auth-decorations {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1;
  overflow: hidden;
  pointer-events: none;
}

.auth-bubble {
  position: absolute;
  border-radius: 50%;
  opacity: 0.2;
  background: linear-gradient(135deg, #FFB6C1 0%, #FF69B4 50%, #FF1493 100%);
  
  &.bubble-1 {
    width: 180px;
    height: 180px;
    top: 8%;
    left: 12%;
    animation: float 10s ease-in-out infinite;
  }
  
  &.bubble-2 {
    width: 100px;
    height: 100px;
    top: 65%;
    left: 3%;
    animation: float 12s ease-in-out infinite 1s;
  }
  
  &.bubble-3 {
    width: 120px;
    height: 120px;
    top: 25%;
    right: 8%;
    animation: float 14s ease-in-out infinite 0.5s;
  }
  
  &.bubble-4 {
    width: 80px;
    height: 80px;
    bottom: 15%;
    right: 18%;
    animation: float 9s ease-in-out infinite 1.5s;
  }
  
  &.bubble-5 {
    width: 60px;
    height: 60px;
    top: 50%;
    left: 8%;
    animation: float 11s ease-in-out infinite 2s;
  }
}

.auth-paw {
  position: absolute;
  font-size: 20px;
  opacity: 0.3;
  
  &.paw-1 {
    top: 15%;
    right: 25%;
    animation: rotate 20s linear infinite;
  }
  
  &.paw-2 {
    bottom: 10%;
    left: 35%;
    animation: rotate 18s linear infinite reverse;
  }
  
  &.paw-3 {
    top: 45%;
    left: 10%;
    animation: rotate 25s linear infinite;
  }
  
  &.paw-4 {
    top: 70%;
    right: 5%;
    animation: float 8s ease-in-out infinite;
  }
  
  &.paw-5 {
    bottom: 40%;
    right: 30%;
    animation: float 6s ease-in-out infinite 1s;
  }
}

.particles {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1;
  pointer-events: none;
  
  .particle {
    position: absolute;
    background: rgba(255, 182, 193, 0.7);
    border-radius: 50%;
    animation: particleFloat linear infinite;
  }
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-8px) rotate(5deg);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.4;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.7;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-15px) rotate(10deg);
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

@keyframes particleFloat {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(360deg);
    opacity: 0;
  }
}

@media (max-width: 768px) {
  .auth-container {
    padding: 10px;
  }
  
  .auth-content {
    flex-direction: column;
    max-width: 400px;
  }
  
  .auth-left,
  .auth-box {
    width: 100%;
  }
  
  .auth-left {
    padding: 30px 20px;
    
    .auth-text {
      h2 {
        font-size: 24px;
      }
      
      p {
        font-size: 16px;
      }
    }
  }
  
  .auth-box {
    padding: 30px 20px;
  }
  
  .auth-header {
    .logo .logo-inner {
      width: 70px;
      height: 70px;
      font-size: 36px;
    }
    
    .title {
      font-size: 24px;
    }
  }
}
</style>