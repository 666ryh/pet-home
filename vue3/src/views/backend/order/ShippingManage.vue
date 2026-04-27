<template>
  <div class="shipping-manage-container">
    <div class="page-header">
      <h2>物流管理</h2>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable></el-input>
        </el-form-item>
        <el-form-item label="物流状态">
          <el-select v-model="searchForm.shippingStatus" placeholder="请选择状态" clearable>
            <el-option v-for="(status, index) in shippingStatusOptions" :key="index" :label="status.label" :value="status.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <el-table
      v-loading="loading"
      :data="shippingList"
      border
      style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="orderNo" label="订单号"></el-table-column>
      <el-table-column prop="shippingNo" label="物流单号" width="180"></el-table-column>
      <el-table-column prop="deliveryCompany" label="快递公司" width="120"></el-table-column>
      <el-table-column prop="trackingNo" label="快递单号" width="150"></el-table-column>
      <el-table-column prop="shippingStatus" label="物流状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.shippingStatus)">
            {{ scope.row.shippingStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deliveryTime" label="发货时间" width="160">
        <template #default="scope">
          {{ formatDate(scope.row.deliveryTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="receiptTime" label="签收时间" width="160">
        <template #default="scope">
          {{ scope.row.receiptTime ? formatDate(scope.row.receiptTime) : '未签收' }}
        </template>
      </el-table-column>
      <el-table-column prop="operator" label="操作人" width="100"></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="viewShippingDetail(scope.row)">查看详情</el-button>
          <el-button 
            v-if="scope.row.shippingStatus === '已发货'" 
            type="success" 
            size="small" 
            @click="handleUpdateStatus(scope.row, '已签收')">
            标记签收
          </el-button>
          <el-button 
            v-if="scope.row.shippingStatus === '已发货'" 
            type="warning" 
            size="small" 
            @click="handleUpdateStatus(scope.row, '已退回')">
            标记退回
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页器 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>
    </div>

    <!-- 物流详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="物流详情"
      width="700px">
      <div v-if="currentShipping" class="shipping-detail">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ currentShipping.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ currentShipping.shippingNo }}</el-descriptions-item>
          <el-descriptions-item label="快递公司">{{ currentShipping.deliveryCompany }}</el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ currentShipping.trackingNo }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">物流状态</el-divider>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物流状态">
            <el-tag :type="getStatusTagType(currentShipping.shippingStatus)">
              {{ currentShipping.shippingStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentShipping.operator || '系统' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ formatDate(currentShipping.deliveryTime) }}</el-descriptions-item>
          <el-descriptions-item label="签收时间">
            {{ currentShipping.receiptTime ? formatDate(currentShipping.receiptTime) : '未签收' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">其他信息</el-divider>
        
        <el-descriptions :column="1" border>
    
          <el-descriptions-item label="备注">{{ currentShipping.notes || '无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentShipping.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(currentShipping.updateTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">物流轨迹</el-divider>
        
        <el-timeline>
          <el-timeline-item
            v-if="currentShipping.shippingStatus === '已签收'"
            timestamp="签收"
            placement="top"
            type="success"
            :hollow="false">
            <h4>包裹已签收</h4>
            <p>{{ currentShipping.receiptTime ? formatDate(currentShipping.receiptTime) : '' }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            v-if="currentShipping.shippingStatus === '已发货' || currentShipping.shippingStatus === '已签收'"
            timestamp="运输中"
            placement="top"
            type="primary"
            :hollow="currentShipping.shippingStatus === '已签收'">
            <h4>包裹正在配送</h4>
            <p>{{ formatDate(currentShipping.deliveryTime) }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="已发货"
            placement="top"
            type="info">
            <h4>商家已发货</h4>
            <p>{{ formatDate(currentShipping.deliveryTime) }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            v-if="currentShipping.shippingStatus === '已退回'"
            timestamp="已退回"
            placement="top"
            type="danger"
            :hollow="false">
            <h4>包裹已退回</h4>
            <p>{{ currentShipping.updateTime ? formatDate(currentShipping.updateTime) : '' }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
// 数据定义
const loading = ref(false)
const shippingList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const currentShipping = ref(null)
const userStore = useUserStore()

// 物流状态选项
const shippingStatusOptions = [
  { value: '待发货', label: '待发货' },
  { value: '已发货', label: '已发货' },
  { value: '已签收', label: '已签收' },
  { value: '已退回', label: '已退回' }
]

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  shippingStatus: ''
})

// 获取物流列表
const fetchShippings = async () => {
  loading.value = true
  try {
    await request.get('/shipping/page', {
      orderNo: searchForm.orderNo,
      shippingStatus: searchForm.shippingStatus,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        shippingList.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取物流列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchShippings()
}

// 重置搜索
const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.shippingStatus = ''
  currentPage.value = 1
  fetchShippings()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 将日期对象格式化为字符串（用于发送到后端）
const formatDateString = (date) => {
  if (!date) return ''
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '待发货':
      return 'warning'
    case '已发货':
      return 'primary'
    case '已签收':
      return 'success'
    case '已退回':
      return 'danger'
    default:
      return ''
  }
}

// 查看物流详情
const viewShippingDetail = (shipping) => {
  currentShipping.value = shipping
  detailDialogVisible.value = true
}

// 更新物流状态
const handleUpdateStatus = (shipping, status) => {
  const statusText = status === '已签收' ? '签收' : '退回'
  
  ElMessageBox.confirm(
    `确定要将该物流标记为${statusText}吗？`,
    `标记${statusText}`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const updateData = {
        ...shipping,
        shippingStatus: status,
        operator: userStore.userInfo.username
      }
      
      // 如果是签收状态，设置签收时间并格式化为字符串
      if (status === '已签收') {
        updateData.receiptTime = formatDateString(new Date())
        console.log('格式化后的签收时间:', updateData.receiptTime)
      }
      
      await request.put(`/shipping/${shipping.id}`, updateData, {
        successMsg: `物流已标记为${statusText}`,
        onSuccess: () => {
          fetchShippings()
        }
      })
    } catch (error) {
      console.error('更新物流状态失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchShippings()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchShippings()
}

// 页面加载时获取物流列表
onMounted(() => {
  fetchShippings()
})
</script>

<style scoped>
.shipping-manage-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
}

.search-container {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.shipping-detail {
  padding: 10px;
}
</style> 