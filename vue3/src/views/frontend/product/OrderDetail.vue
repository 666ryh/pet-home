<template>
  <div class="order-detail-page">
    <!-- 页面装饰元素 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="order-detail-content">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">订单详情</h1>
        <p class="page-subtitle">查看订单的详细信息和物流状态</p>
      </div>
      
      <!-- 面包屑导航 -->
      <div class="breadcrumb-nav">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/order' }">我的订单</el-breadcrumb-item>
          <el-breadcrumb-item>订单详情</el-breadcrumb-item>
        </el-breadcrumb>
        <el-button class="back-button" @click="goBack" size="small" plain>
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
      </div>
      
      <!-- 订单详情卡片 -->
      <el-card class="order-detail-card" shadow="never" v-loading="loading">
        <div v-if="order">
          <!-- 订单进度 -->
          <div class="order-status-section">
            <el-steps :active="getStatusStep(order.status)" finish-status="success" simple align-center>
              <el-step title="待付款" :icon="Wallet"></el-step>
              <el-step title="待发货" :icon="ShoppingCart"></el-step>
              <el-step title="待收货" :icon="Van"></el-step>
              <el-step title="已完成" :icon="CircleCheck"></el-step>
            </el-steps>
            
            <div class="current-status">
              <el-tag :type="getStatusTagType(order.status)" size="large">{{ order.status }}</el-tag>
              
              <!-- 添加支付倒计时 -->
              <div v-if="order.status === '待付款'" class="payment-countdown">
                <el-tooltip
                  :content="'支付截止时间: ' + formatDate(order.paymentDeadline)"
                  placement="top"
                >
                  <span class="countdown-label">
                    <el-icon><Timer /></el-icon>
                    支付倒计时:
                  </span>
                </el-tooltip>
                <countdown-timer
                  :order-id="order.id"
                  @expired="handleOrderExpired"
                />
              </div>
              
              <div class="action-buttons">
                <template v-if="order.status === '待付款'">
                  <el-button type="danger" plain size="small" @click="cancelOrder">
                    <el-icon><Close /></el-icon>取消订单
                  </el-button>
                  <el-button type="primary" size="small" @click="payOrder">
                    <el-icon><CreditCard /></el-icon>立即付款
                  </el-button>
                </template>
                
                <template v-if="order.status === '待收货'">
                  <el-button type="success" size="small" @click="confirmReceipt">
                    <el-icon><Check /></el-icon>确认收货
                  </el-button>
                  <el-button type="info" size="small" @click="viewTracking">
                    <el-icon><Van /></el-icon>查看物流
                  </el-button>
                </template>
                
                <template v-if="order.status === '已完成' || order.status === '已取消'">
                  <el-button type="danger" plain size="small" @click="deleteOrder">
                    <el-icon><Delete /></el-icon>删除订单
                  </el-button>
                </template>
              </div>
            </div>
          </div>
          
          <!-- 订单信息 -->
          <div class="info-section">
            <div class="section-header">
              <el-icon><InfoFilled /></el-icon>
              <h3>订单信息</h3>
            </div>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="下单时间">{{ formatDate(order.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="订单状态">{{ order.status }}</el-descriptions-item>
              <el-descriptions-item label="订单备注">{{ order.remark || '无' }}</el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ order.paymentMethod || '暂未支付' }}</el-descriptions-item>
              <el-descriptions-item label="支付时间" :span="order.paymentTime ? 1 : 2">
                {{ order.paymentTime ? formatDate(order.paymentTime) : '暂未支付' }}
              </el-descriptions-item>
              <el-descriptions-item v-if="order.paymentTime" label="订单备注">{{ order.remark || '无' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 收货信息 -->
          <div class="info-section">
            <div class="section-header">
              <el-icon><Location /></el-icon>
              <h3>收货信息</h3>
            </div>
            
            <el-descriptions :column="1" border>
              <el-descriptions-item label="收货地址">{{ order.address }}</el-descriptions-item>
              <el-descriptions-item label="收货人">{{ order.contactName }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ order.contactPhone }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 物流信息 -->
          <div class="info-section" v-if="shipping">
            <div class="section-header">
              <el-icon><Van /></el-icon>
              <h3>物流信息</h3>
            </div>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="快递公司">{{ shipping.deliveryCompany }}</el-descriptions-item>
              <el-descriptions-item label="快递单号">{{ shipping.trackingNo }}</el-descriptions-item>
              <el-descriptions-item label="发货时间">{{ formatDate(shipping.deliveryTime) }}</el-descriptions-item>
              <el-descriptions-item label="签收时间">{{ shipping.receiptTime ? formatDate(shipping.receiptTime) : '未签收' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 商品信息 -->
          <div class="info-section">
            <div class="section-header">
              <el-icon><Goods /></el-icon>
              <h3>商品信息</h3>
            </div>
            
            <div class="product-card">
              <div class="product-info">
                <el-image
                  :src="getImageUrl(order.productImage)"
                  fit="cover"
                  class="product-image"
                  @click="viewProduct">
                </el-image>
                <div class="product-details">
                  <div class="product-name" @click="viewProduct">{{ order.productName }}</div>
                  <div class="product-price">单价：¥{{ order.price }}</div>
                  <div class="product-quantity">数量：{{ order.quantity }}</div>
                </div>
              </div>
              <div class="product-total">
                <div class="product-total-item">
                  <span>商品金额：</span>
                  <span>¥{{ order.price * order.quantity }}</span>
                </div>
          
                <div class="product-total-item order-amount">
                  <span>实付款：</span>
                  <span>¥{{ order.totalAmount }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 退货信息 -->
          <div class="info-section" v-if="order.isReturned">
            <div class="section-header">
              <el-icon><SwitchButton /></el-icon>
              <h3>退货信息</h3>
            </div>
            
            <el-descriptions :column="2" border v-if="returnInfo">
              <el-descriptions-item label="退货原因">{{ returnInfo.reason }}</el-descriptions-item>
              <el-descriptions-item label="退货状态">
                <el-tag :type="getReturnStatusType(returnInfo.status)">{{ returnInfo.status }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ formatDate(returnInfo.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="处理时间" v-if="returnInfo.processTime">
                {{ formatDate(returnInfo.processTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="处理备注" v-if="returnInfo.operatorNote">
                {{ returnInfo.operatorNote }}
              </el-descriptions-item>
              <el-descriptions-item label="申请前状态" v-if="returnInfo.previousOrderStatus">
                {{ returnInfo.previousOrderStatus }}
              </el-descriptions-item>
              <el-descriptions-item label="退款金额">¥{{ returnInfo.refundAmount }}</el-descriptions-item>
              <el-descriptions-item label="详细说明">{{ returnInfo.description }}</el-descriptions-item>
              <el-descriptions-item label="图片凭证" v-if="returnInfo.images" :span="2">
                <div class="return-images">
                  <el-image 
                    v-for="(img, index) in returnInfo.images.split(',')"
                    :key="index"
                    :src="getImageUrl(img)"
                    :preview-teleported="true"
                    :preview-src-list="returnInfo.images.split(',').map(img => getImageUrl(img))"
                    fit="cover"
                    class="return-image"
                  />
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 评价信息 -->
          <div class="info-section" v-if="order.isReviewed">
            <div class="section-header">
              <el-icon><ChatDotRound /></el-icon>
              <h3>评价信息</h3>
            </div>
            
            <el-descriptions :column="2" border v-if="reviewInfo">
              <el-descriptions-item label="评分">
                <el-rate v-model="reviewInfo.rating" disabled></el-rate>
              </el-descriptions-item>
              <el-descriptions-item label="评价时间">{{ formatDate(reviewInfo.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="评价内容">{{ reviewInfo.content }}</el-descriptions-item>
              <el-descriptions-item label="商家回复" v-if="reviewInfo.reply">
                {{ reviewInfo.reply }}
              </el-descriptions-item>
              <el-descriptions-item label="评价图片" v-if="reviewInfo.images" :span="2">
                <div class="review-images">
                  <el-image 
                    v-for="(img, index) in reviewInfo.images.split(',')"
                    :key="index"
                    :src="getImageUrl(img)"
                    :preview-teleported="true"
                    :preview-src-list="reviewInfo.images.split(',').map(img => getImageUrl(img))"
                    fit="cover"
                    class="review-image"
                  />
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        
        <el-empty v-else description="订单不存在或已被删除"></el-empty>
      </el-card>
    </div>
    
    <!-- 支付对话框 -->
    <el-dialog
      v-model="payDialogVisible"
      title="订单支付"
      width="500px"
      class="pay-dialog">
      <div class="pay-dialog-content">
        <div class="pay-order-info">
          <h3 class="order-title">
            <el-icon><InfoFilled /></el-icon>
            订单信息
          </h3>
          <div class="order-details">
            <p><span class="label">订单号：</span>{{ order?.orderNo }}</p>
            <p><span class="label">商品：</span>{{ order?.productName }}</p>
            <p><span class="label">数量：</span>{{ order?.quantity }}</p>
            <p class="pay-amount"><span class="label">支付金额：</span><span>¥{{ order?.totalAmount }}</span></p>
          </div>
        </div>
        
        <div class="pay-methods">
          <h3 class="method-title">
            <el-icon><Wallet /></el-icon>
            选择支付方式
          </h3>
          <div class="payment-options">
            <el-radio-group v-model="paymentMethod">
              <el-radio label="微信支付">
                <div class="payment-option">
                  <span class="payment-icon wechat">W</span>
                  微信支付
                </div>
              </el-radio>
              <el-radio label="支付宝">
                <div class="payment-option">
                  <span class="payment-icon alipay">A</span>
                  支付宝
                </div>
              </el-radio>
              <el-radio label="货到付款">
                <div class="payment-option">
                  <span class="payment-icon cod">C</span>
                  货到付款
                </div>
              </el-radio>
            </el-radio-group>
          </div>
        </div>
        
        <div class="pay-qrcode" v-if="paymentMethod === '微信支付' || paymentMethod === '支付宝'">
<!--          <p>请扫描二维码支付</p>-->
<!--          <div class="qrcode-image">-->
<!--            <el-image src="https://via.placeholder.com/200" fit="contain"></el-image>-->
<!--          </div>-->
<!--          <p class="pay-note">此处为模拟支付，实际开发中接入真实支付接口</p>-->
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="payDialogVisible = false">取消</el-button>
          <el-button type="primary" class="confirm-btn" @click="confirmPayment" :loading="paying">
            <el-icon><CreditCard /></el-icon>
            确认支付
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 物流追踪对话框 -->
    <el-dialog
      v-model="trackingDialogVisible"
      title="物流追踪"
      width="500px"
      class="tracking-dialog">
      <div v-if="shipping" class="tracking-dialog-content">
        <el-timeline>
          <el-timeline-item
            v-if="order.status === '已完成' || shipping.shippingStatus === '已签收'"
            timestamp="签收"
            placement="top"
            type="success"
            :hollow="false">
            <h4>包裹已签收</h4>
            <p>{{ formatDate(shipping.receiptTime) }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            v-if="shipping.shippingStatus === '已退回'"
            timestamp="退回"
            placement="top"
            type="danger"
            :hollow="false">
            <h4>包裹已退回</h4>
            <p>{{ formatDate(shipping.updateTime) }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="运输中"
            placement="top"
            type="primary"
            :hollow="order.status !== '待收货' || shipping.shippingStatus === '已退回'">
            <h4>包裹正在配送</h4>
            <p>{{ formatDate(shipping.deliveryTime) }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="已发货"
            placement="top"
            type="info">
            <h4>商家已发货</h4>
            <p>{{ formatDate(shipping.deliveryTime) }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="待发货"
            placement="top"
            :type="order.status === '待发货' ? 'warning' : 'info'">
            <h4>订单已支付，等待商家发货</h4>
            <p>{{ formatDate(order.paymentTime) }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="已下单"
            placement="top"
            type="info">
            <h4>订单已创建</h4>
            <p>{{ formatDate(order.createTime) }}</p>
          </el-timeline-item>
        </el-timeline>
        
        <div class="tracking-info">
          <h3 class="tracking-title">
            <el-icon><InfoFilled /></el-icon>
            快递信息
          </h3>
          <p><strong>物流公司：</strong>{{ shipping.deliveryCompany }}</p>
          <p><strong>物流单号：</strong>{{ shipping.trackingNo }}</p>
          <p class="tracking-note">您也可以到物流公司官网查询物流进度</p>
        </div>
      </div>
      
      <div v-else class="no-tracking-data">
        <el-empty description="暂无物流信息"></el-empty>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Wallet, 
  ShoppingCart, 
  Van, 
  CircleCheck, 
  Close, 
  CreditCard, 
  Check, 
  Delete, 
  InfoFilled, 
  Location, 
  Goods, 
  ArrowLeft,
  SwitchButton,
  ChatDotRound,
  Timer
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import CountdownTimer from '@/components/CountdownTimer.vue'

// 路由
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const orderId = route.params.id

// 数据定义
const loading = ref(false)
const order = ref(null)
const shipping = ref(null)
const returnInfo = ref(null)
const reviewInfo = ref(null)

// 支付相关
const payDialogVisible = ref(false)
const paymentMethod = ref('微信支付')
const paying = ref(false)

// 物流相关
const trackingDialogVisible = ref(false)

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    await request.get(`/order/${orderId}`, null, {
      onSuccess: (res) => {
        order.value = res
        // 如果订单状态是待收货或已完成，查询物流信息
        if (res.status === '待收货' || res.status === '已完成') {
          fetchShippingInfo()
        }
        // 如果订单已退货，获取退货信息
        if (res.isReturned) {
          fetchReturnInfo()
        }
        // 如果订单已评价，获取评价信息
        if (res.isReviewed) {
          fetchReviewInfo()
        }
      }
    })
  } catch (error) {
    console.error('获取订单详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取物流信息
const fetchShippingInfo = async () => {
  try {
    await request.get(`/shipping/order/${orderId}`, null, {
      onSuccess: (res) => {
        shipping.value = res
      }
    })
  } catch (error) {
    console.error('获取物流信息失败:', error)
  }
}

// 获取退货信息
const fetchReturnInfo = async () => {
  try {
    await request.get(`/order/return/order/${orderId}`, null, {
      onSuccess: (res) => {
        returnInfo.value = res
      }
    })
  } catch (error) {
    console.error('获取退货信息失败:', error)
  }
}

// 获取评价信息
const fetchReviewInfo = async () => {
  try {
    await request.get(`/order/review/order/${orderId}`, null, {
      onSuccess: (res) => {
        reviewInfo.value = res
      }
    })
  } catch (error) {
    console.error('获取评价信息失败:', error)
  }
}

// 获取图片URL
const getImageUrl = (image) => {
  if (!image) return ''
  const baseAPI = process.env.VUE_APP_BASE_API || '/api'
  return image.startsWith('http') ? image : `${baseAPI}${image}`
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取订单状态对应的步骤
const getStatusStep = (status) => {
  switch (status) {
    case '待付款': return 0
    case '待发货': return 1
    case '待收货': return 2
    case '已完成': return 3
    case '已取消': return 0 // 取消订单回到第一步，但会显示为取消状态
    default: return 0
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '待付款': return 'warning'
    case '待发货': return 'info'
    case '待收货': return 'primary'
    case '已完成': return 'success'
    case '已取消': return 'danger'
    default: return ''
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 查看商品详情
const viewProduct = () => {
  router.push({ name: 'ProductDetail', params: { id: order.value.productId } })
}

// 取消订单
const cancelOrder = () => {
  ElMessageBox.confirm(
    '确定要取消该订单吗？',
    '取消订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.put(`/order/${orderId}/cancel?userId=${userStore.userInfo.id}`, null, {
        successMsg: '订单已取消',
        onSuccess: () => {
          fetchOrderDetail()
        }
      })
    } catch (error) {
      console.error('取消订单失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 支付订单
const payOrder = () => {
  payDialogVisible.value = true
}

// 确认支付
const confirmPayment = async () => {
  paying.value = true
  try {
    await request.put(`/order/${orderId}/status`, null, {
      params: {
        status: '待发货'
      },
      successMsg: '支付成功',
      onSuccess: () => {
        payDialogVisible.value = false
        fetchOrderDetail()
      }
    })
  } catch (error) {
    console.error('支付失败:', error)
  } finally {
    paying.value = false
  }
}

// 确认收货
const confirmReceipt = () => {
  ElMessageBox.confirm(
    '确认已收到商品？',
    '确认收货',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await request.put(`/order/${orderId}/confirm?userId=${userStore.userInfo.id}`, null, {
        successMsg: '已确认收货',
        onSuccess: () => {
          fetchOrderDetail()
        }
      })
    } catch (error) {
      console.error('确认收货失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 查看物流
const viewTracking = () => {
  if (!shipping.value) {
    fetchShippingInfo()
  }
  trackingDialogVisible.value = true
}

// 删除订单
const deleteOrder = () => {
  ElMessageBox.confirm(
    '确定要删除该订单吗？删除后不可恢复',
    '删除订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.delete(`/order/${orderId}`, {
        successMsg: '订单已删除',
        onSuccess: () => {
          router.push('/order')
        }
      })
    } catch (error) {
      console.error('删除订单失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 获取退货状态类型
const getReturnStatusType = (status) => {
  switch (status) {
    case '待处理':
      return 'warning'
    case '已同意':
      return 'success'
    case '已拒绝':
      return 'danger'
    default:
      return 'info'
  }
}

// 处理订单过期
const handleOrderExpired = () => {
  ElMessage.warning('订单已超时未支付，系统将自动取消')
  
  // 调用后端API取消订单
  request.put(`/order/${orderId}/cancel?userId=${userStore.userInfo.id}`, null, {
    successMsg: '订单已自动取消',
    onSuccess: () => {
      // 重新获取订单信息
      fetchOrderDetail()
    },
    errorMsg: '订单取消失败',
    showError: true
  })
}

// 页面加载时获取订单详情
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style lang="scss" scoped>
.order-detail-page {
  position: relative;
  min-height: 100vh;
  background-color: #FFF9E6;
  padding: 30px 20px 60px;
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

.order-detail-content {
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

.breadcrumb-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      color: #666;
      font-weight: normal;
      
      &.is-link:hover {
        color: #FFA726;
      }
    }
    
    &:last-child .el-breadcrumb__inner {
      color: #683e35;
      font-weight: 600;
    }
  }
  
  .back-button {
    color: #683e35;
    border-color: #FFA726;
    
    &:hover {
      color: white;
      background-color: #FFA726;
      border-color: #FFA726;
    }
    
    .el-icon {
      margin-right: 5px;
    }
  }
}

.order-detail-card {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05) !important;
  margin-bottom: 30px;
  overflow: hidden;
  
  :deep(.el-card__body) {
    padding: 25px;
  }
}

.order-status-section {
  margin-bottom: 30px;
  
  :deep(.el-steps) {
    margin-bottom: 25px;
    
    .el-step__title {
      font-size: 14px;
      font-weight: 500;
      color: #666;
      transition: all 0.3s ease;
    }
    
    .el-step__icon {
      color: #FFA726;
      border-color: #FFA726;
    }
    
    .el-step.is-success {
      .el-step__title, .el-step__icon {
        color: #67C23A;
      }
    }
    
    .el-step.is-process {
      .el-step__title {
        color: #683e35;
        font-weight: 600;
      }
      
      .el-step__icon {
        background-color: #FFA726;
        color: white;
      }
    }
  }
}

.current-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  
  .payment-countdown {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    color: #ff6b6b;
    
    .countdown-label {
      display: flex;
      align-items: center;
      gap: 2px;
      cursor: help;
    }
  }
  
  :deep(.el-tag) {
    padding: 8px 15px;
    font-size: 14px;
    font-weight: 600;
    border-radius: 20px;
    
    &.el-tag--success {
      background-color: #f0f9eb;
      border-color: #e1f3d8;
    }
    
    &.el-tag--warning {
      background-color: #fdf6ec;
      border-color: #faecd8;
    }
    
    &.el-tag--danger {
      background-color: #fef0f0;
      border-color: #fde2e2;
    }
    
    &.el-tag--primary {
      background-color: #ecf5ff;
      border-color: #d9ecff;
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 10px;
    
    .el-button {
      transition: all 0.3s ease;
      border-radius: 20px;
      
      .el-icon {
        margin-right: 5px;
      }
      
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
    
    .el-button--primary {
      background-color: #FFA726;
      border-color: #FFA726;
      
      &:hover {
        background-color: darken(#FFA726, 5%);
        border-color: darken(#FFA726, 5%);
        box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
      }
    }
    
    .el-button--success {
      background-color: #67C23A;
      border-color: #67C23A;
      
      &:hover {
        background-color: darken(#67C23A, 5%);
        border-color: darken(#67C23A, 5%);
        box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
      }
    }
  }
}

.info-section {
  margin-bottom: 30px;
  animation: fadeIn 0.5s ease-in-out;
  
  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    
    .el-icon {
      color: #FFA726;
      font-size: 20px;
    }
    
    h3 {
      margin: 0 0 0 10px;
      font-size: 18px;
      font-weight: 600;
      color: #683e35;
    }
  }
  
  :deep(.el-descriptions) {
    --el-descriptions-item-bordered-label-background: #fafafa;
    
    .el-descriptions__body {
      background-color: #fafafa;
    }
    
    .el-descriptions__label {
      color: #666;
      font-weight: 600;
    }
    
    .el-descriptions__content {
      color: #333;
    }
  }
  
  :deep(.el-rate) {
    margin-top: 8px;
    
    .el-rate__icon {
      font-size: 24px;
      margin-right: 6px;
      
      &.hover {
        transform: scale(1.15);
      }
    }
  }
}

.product-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }
  
  .product-info {
    display: flex;
    margin-bottom: 20px;
    
    .product-image {
      width: 100px;
      height: 100px;
      border-radius: 8px;
      object-fit: cover;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: transform 0.3s ease;
      cursor: pointer;
      
      &:hover {
        transform: scale(1.05);
      }
    }
    
    .product-details {
      margin-left: 15px;
      flex: 1;
      
      .product-name {
        font-weight: 600;
        margin-bottom: 10px;
        color: #333;
        cursor: pointer;
        transition: color 0.3s ease;
        
        &:hover {
          color: #FFA726;
        }
      }
      
      .product-price,
      .product-quantity {
        color: #666;
        font-size: 14px;
        margin-bottom: 5px;
      }
    }
  }
  
  .product-total {
    border-top: 1px solid #f0f0f0;
    padding-top: 15px;
    text-align: right;
    
    .product-total-item {
      margin-bottom: 5px;
      color: #666;
      
      &.order-amount {
        font-weight: bold;
        font-size: 16px;
        color: #f56c6c;
        margin-top: 10px;
      }
    }
  }
}

.pay-dialog {
  :deep(.el-dialog__header) {
    text-align: center;
    font-weight: 600;
    padding: 20px;
    margin-right: 0;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
  }
}

.pay-dialog-content {
  padding: 0;
}

.pay-order-info {
  margin-bottom: 30px;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  
  .order-title {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #333;
    margin-top: 0;
    margin-bottom: 15px;
    
    .el-icon {
      color: #FFA726;
    }
  }
  
  .order-details {
    p {
      margin: 8px 0;
      display: flex;
      
      .label {
        width: 80px;
        color: #666;
      }
    }
    
    .pay-amount {
      font-weight: bold;
      
      span:last-child {
        color: #f56c6c;
        font-size: 20px;
      }
    }
  }
}

.pay-methods {
  margin-bottom: 30px;
  
  .method-title {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #333;
    margin-bottom: 15px;
    
    .el-icon {
      color: #FFA726;
    }
  }
  
  .payment-options {
    display: flex;
    flex-direction: column;
    gap: 10px;
    
    :deep(.el-radio__input.is-checked .el-radio__inner) {
      background-color: #FFA726;
      border-color: #FFA726;
    }
    
    :deep(.el-radio__input.is-checked + .el-radio__label) {
      color: #FFA726;
    }
  }
  
  .payment-option {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .payment-icon {
      display: inline-block;
      width: 20px;
      height: 20px;
      border-radius: 4px;
      color: white;
      font-size: 12px;
      line-height: 20px;
      text-align: center;
      
      &.wechat {
        background-color: #07C160;
      }
      
      &.alipay {
        background-color: #1677FF;
      }
      
      &.cod {
        background-color: #FF9800;
      }
    }
  }
}

.pay-qrcode {
  text-align: center;
  margin: 20px 0;
  
  .qrcode-image {
    width: 200px;
    height: 200px;
    margin: 15px auto;
    border: 1px solid #f0f0f0;
    padding: 10px;
    border-radius: 8px;
  }
  
  .pay-note {
    color: #909399;
    font-size: 12px;
    margin-top: 10px;
  }
}

.confirm-btn {
  min-width: 120px;
  background-color: #FFA726;
  border-color: #FFA726;
  
  &:hover {
    background-color: darken(#FFA726, 5%);
    border-color: darken(#FFA726, 5%);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.tracking-dialog {
  :deep(.el-dialog__header) {
    text-align: center;
    font-weight: 600;
    padding: 20px;
    margin-right: 0;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
  }
}

.tracking-dialog-content {
  padding: 0;
  
  :deep(.el-timeline) {
    padding-left: 0;
    
    .el-timeline-item__node {
      background-color: #FFA726;
    }
    
    .el-timeline-item__node--primary {
      background-color: #409EFF;
    }
    
    .el-timeline-item__node--success {
      background-color: #67C23A;
    }
    
    .el-timeline-item__tail {
      border-left: 2px solid #e8e8e8;
    }
    
    .el-timeline-item__wrapper {
      padding-left: 20px;
    }
    
    .el-timeline-item__timestamp {
      color: #909399;
    }
    
    .el-timeline-item__content {
      h4 {
        color: #333;
        margin: 0 0 5px 0;
      }
      
      p {
        color: #666;
        margin: 0;
        font-size: 14px;
      }
    }
  }
}

.tracking-info {
  margin-top: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  
  .tracking-title {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #333;
    margin-top: 0;
    margin-bottom: 15px;
    
    .el-icon {
      color: #FFA726;
    }
  }
  
  p {
    margin: 8px 0;
    color: #666;
    
    strong {
      color: #333;
      margin-right: 5px;
    }
  }
  
  .tracking-note {
    color: #909399;
    font-size: 12px;
    margin-top: 15px;
  }
}

.no-tracking-data {
  padding: 30px;
  text-align: center;
}

.return-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .return-image {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    object-fit: cover;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: scale(1.05);
    }
  }
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .review-image {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    object-fit: cover;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: scale(1.05);
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

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media screen and (max-width: 768px) {
  .page-header {
    .page-title {
      font-size: 26px;
    }
    
    .page-subtitle {
      font-size: 14px;
    }
  }
  
  .breadcrumb-nav {
    flex-direction: column;
    align-items: flex-start;
    
    .back-button {
      margin-top: 10px;
      align-self: flex-end;
    }
  }
  
  .current-status {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    
    .action-buttons {
      width: 100%;
      justify-content: flex-end;
    }
  }
  
  .product-info {
    flex-direction: column;
    
    .product-image {
      width: 120px;
      height: 120px;
      margin-bottom: 15px;
    }
    
    .product-details {
      margin-left: 0;
    }
  }
  
  .pay-dialog, .tracking-dialog {
    width: 90% !important;
    
    :deep(.el-dialog__body) {
      padding: 20px;
    }
  }
  
  :deep(.el-descriptions) {
    .el-descriptions__body {
      .el-descriptions__table {
        display: block;
        
        .el-descriptions__cell {
          display: block;
          width: 100%;
          
          &.is-bordered-label {
            width: 100%;
          }
        }
      }
    }
  }
}
</style> 