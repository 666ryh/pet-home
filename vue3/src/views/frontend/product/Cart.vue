<template>
  <div class="cart-page">
    <!-- 页面装饰 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="cart-content">
      <div class="page-header">
        <h1 class="page-title">我的购物车</h1>
        <p class="page-subtitle">管理您选择的商品，完成支付流程</p>
      </div>
      
      <el-card shadow="never" class="cart-card">
        <template #header>
          <div class="cart-header">
            <h2>购物车</h2>
            <el-button 
              v-if="cartData.items && cartData.items.length > 0" 
              type="danger" 
              plain 
              size="small" 
              @click="clearCart"
              class="clear-btn"
            >
              <el-icon><Delete /></el-icon>
              清空购物车
            </el-button>
          </div>
        </template>
        
        <div v-loading="loading">
          <!-- 购物车为空 -->
          <el-empty 
            v-if="!cartData.items || cartData.items.length === 0" 
            description="购物车为空"
            :image-size="200"
          >
            <template #description>
              <p>您的购物车还没有商品</p>
              <p>快去选购您喜爱的商品吧</p>
            </template>
            <el-button type="primary" class="action-btn" @click="goToProductList">去选购商品</el-button>
          </el-empty>
          
          <!-- 购物车有商品 -->
          <div v-else>
            <el-table :data="cartData.items" stripe style="width: 100%" class="cart-table">
              <el-table-column width="100">
                <template #default="{ row }">
                  <el-image 
                    :src="getImageUrl(row.productImage)" 
                    fit="cover" 
                    class="product-image"
                    :preview-teleported="true"
                    :preview-src-list="[getImageUrl(row.productImage)]">
                  </el-image>
                </template>
              </el-table-column>
              
              <el-table-column prop="productName" label="商品名称">
                <template #default="{ row }">
                  <div class="product-info">
                    <el-link @click="goToProductDetail(row.productId)" :underline="false" class="product-name">
                      {{ row.productName }}
                    </el-link>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column prop="price" label="单价" width="120">
                <template #default="{ row }">
                  <span class="price">¥{{ row.price }}</span>
                </template>
              </el-table-column>
              
              <el-table-column label="数量" width="150">
                <template #default="{ row }">
                  <el-input-number 
                    v-model="row.quantity" 
                    :min="1" 
                    :precision="0"
                    size="small"
                    class="quantity-input"
                    @change="(value) => updateQuantity(row.productId, value)">
                  </el-input-number>
                </template>
              </el-table-column>
              
              <el-table-column prop="totalAmount" label="小计" width="120">
                <template #default="{ row }">
                  <span class="total-price">¥{{ row.totalAmount }}</span>
                </template>
              </el-table-column>
              
              <el-table-column fixed="right" label="操作" width="120">
                <template #default="{ row }">
                  <el-button 
                    type="danger" 
                    plain 
                    size="small" 
                    class="delete-btn"
                    @click="removeFromCart(row.productId)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="cart-footer">
              <div class="cart-total">
                <span>总计：</span>
                <span class="total-price">¥{{ cartData.totalAmount }}</span>
                <span class="total-quantity">({{ cartData.totalQuantity }}件商品)</span>
              </div>
              
              <div class="cart-actions">
                <el-button @click="goToProductList" class="continue-btn">
                  <el-icon><Back /></el-icon>
                  继续购物
                </el-button>
                <el-button type="primary" @click="checkout" class="checkout-btn">
                  <el-icon><Shop /></el-icon>
                  结算
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 结算对话框 -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="填写订单信息"
      width="550px"
      class="checkout-dialog"
      destroy-on-close
    >
      <el-form :model="orderForm" label-width="100px" ref="orderFormRef" :rules="orderFormRules">
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="orderForm.address" placeholder="请输入详细收货地址">
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="orderForm.contactName" placeholder="请输入联系人姓名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="orderForm.contactPhone" placeholder="请输入联系电话">
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="orderForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%;">
            <el-option label="微信支付" value="微信支付">
              <div class="payment-option">
                <i class="payment-icon wechat">微信</i>
                <span>微信支付</span>
              </div>
            </el-option>
            <el-option label="支付宝" value="支付宝">
              <div class="payment-option">
                <i class="payment-icon alipay">支付宝</i>
                <span>支付宝</span>
              </div>
            </el-option>
            <el-option label="货到付款" value="货到付款">
              <div class="payment-option">
                <i class="payment-icon cod">货到付款</i>
                <span>货到付款</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="订单备注" prop="remark">
          <el-input 
            v-model="orderForm.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入订单备注（选填）"
          >
          </el-input>
        </el-form-item>
      </el-form>
      
      <div class="checkout-summary">
        <div class="order-items-summary">
          <div class="summary-title">
            <el-icon><ShoppingCart /></el-icon>
            <span>订单商品</span>
          </div>
          <div class="summary-content">
            <div v-for="(item, index) in cartData.items" :key="index" class="item-summary">
              <span class="item-name">{{ item.productName }}</span>
              <span class="item-quantity">x{{ item.quantity }}</span>
              <span class="item-price">¥{{ item.totalAmount }}</span>
            </div>
          </div>
        </div>
        
        <div class="checkout-total">
          <span>订单总计：</span>
          <span class="total-price">¥{{ cartData.totalAmount }}</span>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="checkoutDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmOrder" :loading="submitting" class="confirm-btn">
            <el-icon><Check /></el-icon>
            提交订单
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import { 
  Delete, 
  Back, 
  Shop, 
  Location, 
  User, 
  Phone, 
  Check, 
  ShoppingCart 
} from '@element-plus/icons-vue'

// 路由
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 数据定义
const loading = ref(false)
const cartData = ref({
  items: [],
  totalQuantity: 0,
  totalAmount: 0
})

// 结算相关
const checkoutDialogVisible = ref(false)
const submitting = ref(false)
const orderFormRef = ref(null)

// 订单表单
const orderForm = reactive({
  address: '',
  contactName: '',
  contactPhone: '',
  paymentMethod: '',
  remark: ''
})

// 表单验证规则
const orderFormRules = {
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

// 获取购物车数据
const fetchCart = async () => {
  loading.value = true
  try {
    await request.get('/cart', null, {
      onSuccess: (res) => {
        cartData.value = res
      }
    })
  } catch (error) {
    console.error('获取购物车失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取图片URL
const getImageUrl = (image) => {
  if (!image) return ''
  const baseAPI = process.env.VUE_APP_BASE_API || '/api'
  return image.startsWith('http') ? image : `${baseAPI}${image}`
}

// 更新购物车商品数量
const updateQuantity = async (productId, quantity) => {
  try {
    await request.put('/cart/update', null, {
      params: {
        productId,
        quantity
      },
      showDefaultMsg: false,
      onSuccess: (res) => {
        cartData.value = res
      }
    })
  } catch (error) {
    console.error('更新购物车失败:', error)
    fetchCart() // 更新失败，重新获取购物车数据
  }
}

// 从购物车移除商品
const removeFromCart = async (productId) => {
  try {
    await request.delete('/cart/remove', {
      params: {
        productId
      },
      successMsg: '商品已从购物车移除',
      onSuccess: (res) => {
        cartData.value = res
      }
    })
  } catch (error) {
    console.error('移除商品失败:', error)
  }
}

// 清空购物车
const clearCart = async () => {
  ElMessageBox.confirm(
    '确定要清空购物车吗？',
    '清空购物车',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.delete('/cart/clear', {
        successMsg: '购物车已清空',
        onSuccess: (res) => {
          cartData.value = res
        }
      })
    } catch (error) {
      console.error('清空购物车失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 跳转到商品列表页
const goToProductList = () => {
  router.push('/product')
}

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  router.push({ name: 'ProductDetail', params: { id: productId } })
}

// 结算
const checkout = () => {
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm(
      '请先登录后再进行结算',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push({ path: '/login', query: { redirect: '/cart' } })
    }).catch(() => {
      // 用户取消操作
    })
    return
  }
  
  // 打开结算对话框
  checkoutDialogVisible.value = true
  
  // 初始化表单数据
  if (userStore.userInfo) {
    orderForm.contactName = userStore.userInfo.name || ''
    orderForm.contactPhone = userStore.userInfo.phone || ''
  }
}

// 提交订单
const confirmOrder = async () => {
  if (!orderFormRef.value) return
  
  orderFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 构造订单数据
        const orderData = {
          items: cartData.value.items,
          address: orderForm.address,
          contactName: orderForm.contactName,
          contactPhone: orderForm.contactPhone,
          paymentMethod: orderForm.paymentMethod,
          remark: orderForm.remark
        }
        
        // 提交订单
        await request.post('/order?userId=' + userStore.userInfo.id, orderData, {
          successMsg: '订单提交成功',
          onSuccess: (res) => {
            checkoutDialogVisible.value = false
            
            // 清空购物车
            fetchCart()
            
            // 跳转到订单详情页
            router.push({ name: 'OrderDetail', params: { id: res.id } })
          }
        })
      } catch (error) {
        console.error('提交订单失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 页面加载时获取购物车数据
onMounted(() => {
  fetchCart()
})
</script>

<style lang="scss" scoped>
.cart-page {
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

.cart-content {
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

.cart-card {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05) !important;
  margin-bottom: 30px;
  
  :deep(.el-card__header) {
    padding: 20px 25px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #683e35;
    position: relative;
    padding-left: 15px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: #FFB6C1;
      border-radius: 2px;
    }
  }
  
  .clear-btn {
    color: #f56c6c;
    border-color: #f56c6c;
    border-radius: 20px;
    transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    
    &:hover {
      background-color: #fef0f0;
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(245, 108, 108, 0.15);
    }
    
    .el-icon {
      margin-right: 5px;
    }
  }
}

.cart-table {
  margin-bottom: 20px;
  
  :deep(.el-table__header) {
    th {
      background-color: #f8f9fa;
      color: #666;
      font-weight: 600;
    }
  }
  
  :deep(.el-table__row) {
    transition: all 0.3s ease;
    
    &:hover {
      background-color: #fff9e6;
      transform: translateY(-2px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    }
  }
  
  .product-image {
    width: 70px;
    height: 70px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    
    &:hover {
      transform: scale(1.05);
    }
  }
  
  .product-name {
    color: #333;
    font-weight: 500;
    
    &:hover {
      color: #FFA726;
    }
  }
  
  .price {
    color: #666;
  }
  
  .total-price {
    color: #f56c6c;
    font-weight: bold;
  }
  
  .quantity-input {
    :deep(.el-input-number__decrease), 
    :deep(.el-input-number__increase) {
      background-color: #f8f9fa;
      color: #666;
    }
    
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #dcdfe6 inset;
      
      &:hover, &.is-focus {
        box-shadow: 0 0 0 1px #FFA726 inset;
      }
    }
  }
  
  .delete-btn {
    border-radius: 20px;
    transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(245, 108, 108, 0.15);
    }
    
    .el-icon {
      margin-right: 5px;
    }
  }
}

.cart-footer {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  
  .cart-total {
    font-size: 16px;
    
    .total-price {
      color: #f56c6c;
      font-weight: bold;
      font-size: 24px;
      margin: 0 5px;
    }
    
    .total-quantity {
      margin-left: 5px;
      font-size: 14px;
      color: #909399;
    }
  }
  
  .cart-actions {
    display: flex;
    gap: 15px;
    
    .continue-btn {
      border-radius: 20px;
      transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
      
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      .el-icon {
        margin-right: 5px;
      }
    }
    
    .checkout-btn {
      min-width: 120px;
      background-color: #FFA726;
      border-color: #FFA726;
      border-radius: 20px;
      transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
      
      &:hover {
        background-color: darken(#FFA726, 5%);
        border-color: darken(#FFA726, 5%);
        transform: translateY(-3px);
        box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
      }
      
      .el-icon {
        margin-right: 5px;
      }
    }
  }
}

.checkout-dialog {
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
  
  :deep(.el-form-item__label) {
    color: #666;
    font-weight: 500;
  }
  
  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    
    &:hover, &.is-focus {
      box-shadow: 0 0 0 1px #FFA726 inset;
    }
  }
  
  :deep(.el-textarea__inner) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    
    &:hover, &:focus {
      box-shadow: 0 0 0 1px #FFA726 inset;
    }
  }
  
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
  
  .payment-icon {
    display: inline-block;
    width: 20px;
    height: 20px;
    border-radius: 4px;
    color: white;
    font-size: 12px;
    line-height: 20px;
    text-align: center;
    margin-right: 8px;
    
    &.wechat {
      background-color: #07c160;
    }
    
    &.alipay {
      background-color: #1677ff;
    }
    
    &.cod {
      background-color: #ff9800;
    }
  }
}

.checkout-summary {
  margin-top: 30px;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.order-items-summary {
  margin-bottom: 20px;
  
  .summary-title {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    color: #666;
    font-weight: 600;
    position: relative;
    padding-left: 15px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: #FFB6C1;
      border-radius: 2px;
    }
    
    .el-icon {
      margin-right: 8px;
      color: #FFA726;
    }
  }
  
  .summary-content {
    max-height: 150px;
    overflow-y: auto;
    padding-right: 5px;
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 3px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #ddd;
      border-radius: 3px;
    }
    
    &::-webkit-scrollbar-thumb:hover {
      background: #ccc;
    }
  }
  
  .item-summary {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    padding-bottom: 8px;
    border-bottom: 1px dashed #eee;
    
    &:last-child {
      border-bottom: none;
    }
    
    .item-name {
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-right: 10px;
    }
    
    .item-quantity {
      color: #909399;
      margin-right: 15px;
    }
    
    .item-price {
      color: #f56c6c;
      font-weight: 500;
    }
  }
}

.checkout-total {
  text-align: right;
  font-size: 16px;
  
  .total-price {
    color: #f56c6c;
    font-weight: bold;
    font-size: 24px;
    margin-left: 5px;
  }
}

.confirm-btn {
  min-width: 120px;
  background-color: #FFA726;
  border-color: #FFA726;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
  &:hover {
    background-color: darken(#FFA726, 5%);
    border-color: darken(#FFA726, 5%);
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.action-btn {
  background-color: #FFA726;
  border-color: #FFA726;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
  &:hover {
    background-color: darken(#FFA726, 5%);
    border-color: darken(#FFA726, 5%);
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
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

@media screen and (max-width: 768px) {
  .page-header {
    .page-title {
      font-size: 26px;
    }
    
    .page-subtitle {
      font-size: 14px;
    }
  }
  
  .cart-footer {
    flex-direction: column;
    gap: 20px;
    
    .cart-total {
      text-align: center;
    }
    
    .cart-actions {
      width: 100%;
      justify-content: center;
    }
  }
  
  .checkout-dialog {
    :deep(.el-dialog__body) {
      padding: 20px;
    }
  }
}
</style> 