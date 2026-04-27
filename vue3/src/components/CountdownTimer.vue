<template>
  <div class="countdown-timer" :class="{ 'expiring': isExpiring, 'expired': isExpired }">
    <template v-if="loading">
      <el-skeleton style="width: 100px" animated>
        <template #template>
          <el-skeleton-item variant="text" style="width: 100%" />
        </template>
      </el-skeleton>
    </template>
    <template v-else-if="isExpired">
      <span class="expired-text">已过期</span>
    </template>
    <template v-else>
      <span class="time-part">{{ formatTime(hours) }}</span>:
      <span class="time-part">{{ formatTime(minutes) }}</span>:
      <span class="time-part">{{ formatTime(seconds) }}</span>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import request from '@/utils/request'

const props = defineProps({
  orderId: {
    type: [Number, String],
    required: true
  },
  // 倒计时警告阈值（秒）
  warningThreshold: {
    type: Number,
    default: 300 // 5分钟
  }
})

const emit = defineEmits(['expired'])

// 状态变量
const loading = ref(true)
const remainingSeconds = ref(0)
const isExpired = ref(false)
const timer = ref(null)

// 计算属性
const hours = computed(() => Math.floor(remainingSeconds.value / 3600))
const minutes = computed(() => Math.floor((remainingSeconds.value % 3600) / 60))
const seconds = computed(() => remainingSeconds.value % 60)
const isExpiring = computed(() => remainingSeconds.value <= props.warningThreshold && remainingSeconds.value > 0)

// 格式化时间，保持两位数
const formatTime = (time) => {
  return time.toString().padStart(2, '0')
}

// 获取订单支付剩余时间
const fetchRemainingTime = async () => {
  try {
    await request.get(`/order/${props.orderId}/payment-time`, null, {
      onSuccess: (res) => {
        remainingSeconds.value = res.remainingSeconds
        isExpired.value = res.isExpired
        
        if (isExpired.value) {
          emit('expired')
          clearInterval(timer.value)
        }
      }
    })
  } catch (error) {
    console.error('获取订单支付剩余时间失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新倒计时
const updateCountdown = () => {
  if (remainingSeconds.value > 0) {
    remainingSeconds.value--
  } else {
    isExpired.value = true
    emit('expired')
    clearInterval(timer.value)
  }
}

onMounted(async () => {
  await fetchRemainingTime()
  
  if (!isExpired.value) {
    timer.value = setInterval(updateCountdown, 1000)
  }
})

onBeforeUnmount(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})
</script>

<style lang="scss" scoped>
.countdown-timer {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  
  .time-part {
    display: inline-block;
    min-width: 20px;
    text-align: center;
  }
  
  &.expiring {
    color: #e6a23c;
    animation: pulse 1s infinite;
  }
  
  &.expired {
    color: #f56c6c;
  }
  
  .expired-text {
    color: #f56c6c;
  }
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}
</style> 