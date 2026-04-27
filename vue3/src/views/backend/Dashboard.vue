<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <template #header>
        <div class="welcome-header">
          <el-avatar :size="64" :src="avatarUrl">
            {{ userInfo?.name?.charAt(0) }}
          </el-avatar>
          <div class="welcome-info">
            <h2>欢迎回来, {{ userInfo?.name || userInfo?.username }}</h2>
            <p>{{ currentTime }}</p>
          </div>
        </div>
      </template>
      <div class="role-info">
        <el-tag>{{ roleLabel }}</el-tag>
      </div>
    </el-card>

    <!-- 数据总览 -->
    <div class="data-overview">
      <el-row :gutter="20">
        <el-col :span="6" :xs="12" :sm="12" :md="6">
          <el-card shadow="hover" class="data-card">
            <div class="data-card-content">
              <div class="data-card-icon user-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="data-card-info">
                <div class="data-card-title">总用户数</div>
                <div class="data-card-value">{{ statistics.totalUserCount || 0 }}</div>
                <div class="data-card-desc">
                  <span>男: {{ statistics.maleUserCount || 0 }}</span>
                  <span>女: {{ statistics.femaleUserCount || 0 }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12" :sm="12" :md="6">
          <el-card shadow="hover" class="data-card">
            <div class="data-card-content">
              <div class="data-card-icon pet-icon">
                <el-icon><House /></el-icon>
              </div>
              <div class="data-card-info">
                <div class="data-card-title">宠物总数</div>
                <div class="data-card-value">{{ statistics.totalPetCount || 0 }}</div>
                <div class="data-card-desc">
                  <span>已领养: {{ statistics.adoptedPetCount || 0 }}</span>
                  <span>待领养: {{ statistics.availablePetCount || 0 }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12" :sm="12" :md="6">
          <el-card shadow="hover" class="data-card">
            <div class="data-card-content">
              <div class="data-card-icon service-icon">
                <el-icon><Service /></el-icon>
              </div>
              <div class="data-card-info">
                <div class="data-card-title">服务预约</div>
                <div class="data-card-value">{{ statistics.totalServiceAppointmentCount || 0 }}</div>
                <div class="data-card-desc">
                  <span>已完成: {{ statistics.completedServiceCount || 0 }}</span>
                  <span>待处理: {{ statistics.pendingServiceCount || 0 }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12" :sm="12" :md="6">
          <el-card shadow="hover" class="data-card">
            <div class="data-card-content">
              <div class="data-card-icon training-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <div class="data-card-info">
                <div class="data-card-title">训练预约</div>
                <div class="data-card-value">{{ statistics.totalTrainingCount || 0 }}</div>
                <div class="data-card-desc">
                  <span>平均评分: {{ statistics.averageRating || 0 }}/5</span>
                  <span>已完成: {{ statistics.completedTrainingCount || 0 }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表展示 -->
    <el-row :gutter="20" class="chart-container">
      <!-- 趋势图 -->
      <el-col :span="16" :xs="24" :sm="24" :md="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>数据趋势统计</span>
              <el-radio-group v-model="trendType" size="small">
                <el-radio-button label="user">用户注册</el-radio-button>
                <el-radio-button label="pet">宠物领养</el-radio-button>
                <el-radio-button label="service">服务预约</el-radio-button>
                <el-radio-button label="training">训练预约</el-radio-button>
                <el-radio-button label="boarding">寄养申请</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-body" v-loading="loading">
            <div ref="trendChartRef" class="trend-chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 饼图 -->
      <el-col :span="8" :xs="24" :sm="24" :md="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>数据分布</span>
              <el-select v-model="pieType" size="small">
                <el-option label="服务状态分布" value="serviceStatus"></el-option>
                <el-option label="训练状态分布" value="trainingStatus"></el-option>
              </el-select>
            </div>
          </template>
          <div class="chart-body" v-loading="loading">
            <div ref="pieChartRef" class="pie-chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { User, House, Service, Trophy } from '@element-plus/icons-vue'

// 修改ECharts的导入方式
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const baseAPI = process.env.VUE_APP_BASE_API || '/api'
const loading = ref(false)

// 图表实例
let trendChart = null
let pieChart = null

// 当前选择的图表类型
const trendType = ref('user')
const pieType = ref('serviceStatus')

// 统计数据
const statistics = ref({})
const serviceStatusData = ref([])
const trainingStatusData = ref([])

// 图表DOM引用
const trendChartRef = ref(null)
const pieChartRef = ref(null)

// 角色标签
const roleLabel = computed(() => {
  const roleMap = {
    'ADMIN': '系统管理员',
    'USER': '普通用户'
  }
  return roleMap[userInfo.value?.roleCode] || '未知角色'
})

const avatarUrl = computed(() => {
  return userInfo.value?.avatar ? baseAPI + userInfo.value.avatar : '';
})

// 当前时间
const currentTime = ref('')
let timeInterval = null // 保存定时器引用

const updateTime = () => {
  const now = new Date()
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric', 
    weekday: 'long',
    hour: '2-digit',
    minute: '2-digit'
  }
  currentTime.value = now.toLocaleDateString('zh-CN', options)
}

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    await request.get('/statistics/overview', {}, {
      onSuccess: (data) => {
        statistics.value = data
        // 获取数据后更新趋势图
        nextTick(() => {
          updateTrendChart()
        })
      }
    })
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取饼图数据
const fetchPieChartData = async () => {
  loading.value = true
  try {
    // 服务状态分布
    await request.get('/statistics/service/status', {}, {
      onSuccess: (data) => {
        serviceStatusData.value = data
      }
    })
    
    // 训练状态分布
    await request.get('/statistics/training/status', {}, {
      onSuccess: (data) => {
        trainingStatusData.value = data
        // 获取所有数据后更新饼图
        nextTick(() => {
          updatePieChart()
        })
      }
    })
  } catch (error) {
    console.error('获取饼图数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  // 立即更新图表数据
  updateTrendChart()
}

// 更新趋势图
const updateTrendChart = () => {
  if (!trendChart) {
    // 如果图表实例不存在，先初始化
    initTrendChart();
    return;
  }
  
  let trendData = []
  let title = ''
  
  switch (trendType.value) {
    case 'user':
      trendData = statistics.value.userTrend || []
      title = '用户注册趋势'
      break
    case 'pet':
      trendData = statistics.value.petTrend || []
      title = '宠物领养趋势'
      break
    case 'service':
      trendData = statistics.value.serviceTrend || []
      title = '服务预约趋势'
      break
    case 'training':
      trendData = statistics.value.trainingTrend || []
      title = '训练预约趋势'
      break
    case 'boarding':
      trendData = statistics.value.boardingTrend || []
      title = '寄养申请趋势'
      break
  }
  
  const months = trendData.map(item => item.month)
  const counts = trendData.map(item => item.count)
  
  const option = {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        barWidth: '60%',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  
  // 立即更新图表数据
  updatePieChart()
}

// 更新饼图
const updatePieChart = () => {
  if (!pieChart) {
    // 如果图表实例不存在，先初始化
    initPieChart();
    return;
  }
  
  let pieData = []
  let title = ''
  
  switch (pieType.value) {
    case 'serviceStatus':
      pieData = serviceStatusData.value
      title = '服务状态分布'
      break
    case 'trainingStatus':
      pieData = trainingStatusData.value
      title = '训练状态分布'
      break
  }
  
  const option = {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [
      {
        name: '数量',
        type: 'pie',
        radius: '50%',
        center: ['60%', '50%'],
        data: pieData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  pieChart.setOption(option)
}

// 监听图表类型变化
watch(trendType, () => {
  nextTick(() => {
    updateTrendChart()
  })
})

watch(pieType, () => {
  nextTick(() => {
    updatePieChart()
  })
})

// 处理窗口大小变化
const handleResize = () => {
  // 使用nextTick确保DOM已更新
  nextTick(() => {
    if (trendChart) {
      trendChart.resize()
    }
    if (pieChart) {
      pieChart.resize()
    }
  })
}

onMounted(() => {
  updateTime()
  // 每分钟更新一次时间
  timeInterval = setInterval(updateTime, 60000)
  
  // 先初始化图表容器
  nextTick(() => {
    // 初始化图表实例
    if (trendChartRef.value) {
      trendChart = echarts.init(trendChartRef.value)
    }
    
    if (pieChartRef.value) {
      pieChart = echarts.init(pieChartRef.value)
    }
    
    // 获取数据
    fetchStatistics()
    fetchPieChartData()
  })
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 清除定时器
  if (timeInterval) {
    clearInterval(timeInterval)
    timeInterval = null
  }
  
  // 销毁图表实例
  trendChart && trendChart.dispose()
  pieChart && pieChart.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-card {
    margin-bottom: 20px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .welcome-header {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .el-avatar {
        transition: transform 0.3s ease;
        
        &:hover {
          transform: scale(1.1);
        }
      }
      
      .welcome-info {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          background: linear-gradient(to right, #409eff, #67c23a);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }
        p {
          margin: 0;
          color: #666;
        }
      }
    }
    
    .role-info {
      margin-top: 16px;
    }
  }
  
  .data-overview {
    margin-bottom: 20px;
    
    .data-card {
      height: 120px;
      margin-bottom: 20px;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
      }
      
      .data-card-content {
        display: flex;
        align-items: center;
        height: 100%;
        
        .data-card-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 15px;
          
          .el-icon {
            font-size: 30px;
            color: white;
          }
          
          &.user-icon {
            background: linear-gradient(135deg, #409eff, #2e88e5);
          }
          
          &.pet-icon {
            background: linear-gradient(135deg, #67c23a, #4caf50);
          }
          
          &.service-icon {
            background: linear-gradient(135deg, #e6a23c, #f57c00);
          }
          
          &.training-icon {
            background: linear-gradient(135deg, #f56c6c, #e53935);
          }
        }
        
        .data-card-info {
          flex: 1;
          
          .data-card-title {
            font-size: 14px;
            color: #606266;
            margin-bottom: 5px;
          }
          
          .data-card-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 5px;
          }
          
          .data-card-desc {
            font-size: 12px;
            color: #909399;
            display: flex;
            justify-content: space-between;
          }
        }
      }
    }
  }
  
  .chart-container {
    .chart-card {
      margin-bottom: 20px;
      
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .chart-body {
        width: 100%;
        
        .trend-chart, .pie-chart {
          height: 400px;
          width: 100%;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .dashboard {
    .chart-container {
      .chart-card {
        .chart-header {
          flex-direction: column;
          align-items: flex-start;
          gap: 10px;
        }
        
        .chart-body {
          .trend-chart, .pie-chart {
            height: 300px;
          }
        }
      }
    }
  }
}
</style> 