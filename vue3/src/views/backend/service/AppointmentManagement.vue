<template>
  <div class="appointment-management-container">
    <el-card shadow="never" class="search-card">
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="服务名称">
            <el-input v-model="searchForm.serviceName" placeholder="服务名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="宠物名称">
            <el-input v-model="searchForm.petName" placeholder="宠物名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="searchForm.contactPhone" placeholder="联系电话" clearable></el-input>
          </el-form-item>
          <el-form-item label="预约状态">
            <el-select v-model="searchForm.status" placeholder="预约状态" clearable>
              <el-option value="已预约" label="待确认"></el-option>
              <el-option value="已确认" label="已确认"></el-option>
              <el-option value="已完成" label="已完成"></el-option>
              <el-option value="已取消" label="已取消"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <el-card shadow="never" v-loading="loading" class="table-card">
      <template #header>
        <div class="card-header">
          <span>服务预约管理</span>
          <div class="header-actions">
            <el-button type="success" size="small" @click="exportData">导出数据</el-button>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleDateRangeChange"
              style="width: 300px; margin-left: 10px;"
            ></el-date-picker>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="serviceName" label="服务名称" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="petName" label="宠物名称" width="120"></el-table-column>
        <el-table-column prop="userName" label="预约用户" width="120"></el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="120"></el-table-column>
        <el-table-column label="预约时间" width="170" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === '已预约'" 
              size="small" 
              type="success" 
              @click="confirmAppointment(scope.row)"
            >
              确认
            </el-button>
            <el-button 
              v-if="scope.row.status === '已确认'" 
              size="small" 
              type="primary" 
              @click="completeAppointment(scope.row)"
            >
              完成
            </el-button>
            <el-button 
              v-if="scope.row.status !== '已取消' && scope.row.status !== '已完成'" 
              size="small" 
              type="danger" 
              @click="cancelAppointment(scope.row)"
            >
              取消
            </el-button>
            <el-button 
              size="small" 
              @click="viewDetails(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 预约详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="预约详情" 
      width="600px"
    >
      <div class="appointment-details" v-if="currentAppointment">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="预约编号">{{ currentAppointment.id }}</el-descriptions-item>
          <el-descriptions-item label="服务名称">{{ currentAppointment.serviceName }}</el-descriptions-item>
          <el-descriptions-item label="预约状态">
            <el-tag :type="getStatusType(currentAppointment.status)">
              {{ currentAppointment.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ formatDateTime(currentAppointment.appointmentTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="宠物名称">{{ currentAppointment.petName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentAppointment.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="用户姓名">{{ currentAppointment.userName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentAppointment.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="特殊要求" :span="2">
            <div class="requirements-content">
              {{ currentAppointment.requirements || '无' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentAppointment && currentAppointment.status === '已预约'" 
            type="success" 
            @click="confirmAppointment(currentAppointment)"
          >
            确认预约
          </el-button>
          <el-button 
            v-if="currentAppointment && currentAppointment.status === '已确认'" 
            type="primary" 
            @click="completeAppointment(currentAppointment)"
          >
            完成服务
          </el-button>
          <el-button 
            v-if="currentAppointment && currentAppointment.status !== '已取消' && currentAppointment.status !== '已完成'" 
            type="danger" 
            @click="cancelAppointment(currentAppointment)"
          >
            取消预约
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 备注输入对话框 -->
    <el-dialog 
      v-model="remarkDialogVisible" 
      :title="remarkTitle" 
      width="400px"
    >
      <el-form>
        <el-form-item label="备注信息">
          <el-input
            v-model="remarkContent"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息（可选）"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="remarkDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStatusChange" :loading="statusChanging">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 数据定义
const loading = ref(false)
const statusChanging = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const remarkDialogVisible = ref(false)
const currentAppointment = ref(null)
const remarkTitle = ref('')
const remarkContent = ref('')
const statusAction = ref('')
const dateRange = ref([])

// 搜索表单
const searchForm = ref({
  serviceName: '',
  petName: '',
  contactPhone: '',
  status: ''
})

// 获取预约列表
const fetchAppointments = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      ...searchForm.value
    }
    
    // 添加日期范围过滤
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    await request.get('/service/appointment/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取预约详情
const fetchAppointmentDetails = async (id) => {
  try {
    await request.get(`/service/appointment/${id}`, null, {
      onSuccess: (res) => {
        currentAppointment.value = res
      }
    })
  } catch (error) {
    console.error('获取预约详情失败:', error)
  }
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态对应的类型
const getStatusType = (status) => {
  switch (status) {
    case '已预约': return 'warning'
    case '已确认': return 'primary'
    case '已完成': return 'success'
    case '已取消': return 'info'
    default: return ''
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchAppointments()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    serviceName: '',
    petName: '',
    contactPhone: '',
    status: ''
  }
  dateRange.value = []
  handleSearch()
}

// 处理日期范围变化
const handleDateRangeChange = () => {
  handleSearch()
}

// 导出数据
const exportData = () => {
  ElMessage.info('数据导出功能开发中...')
}

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchAppointments()
}

// 处理当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchAppointments()
}

// 查看详情
const viewDetails = (row) => {
  fetchAppointmentDetails(row.id)
  detailDialogVisible.value = true
}

// 确认预约
const confirmAppointment = (appointment) => {
  openRemarkDialog('确认预约', '确认预约', appointment)
}

// 完成服务
const completeAppointment = (appointment) => {
  openRemarkDialog('完成服务', '完成服务', appointment)
}

// 取消预约
const cancelAppointment = (appointment) => {
  openRemarkDialog('取消预约', '取消预约', appointment)
}

// 打开备注对话框
const openRemarkDialog = (title, action, appointment) => {
  remarkTitle.value = title
  statusAction.value = action
  currentAppointment.value = appointment
  remarkContent.value = ''
  remarkDialogVisible.value = true
}

// 提交状态变更
const submitStatusChange = async () => {
  if (!currentAppointment.value) return
  
  statusChanging.value = true
  try {
    let newStatus = ''
    switch (statusAction.value) {
      case '确认预约':
        newStatus = '已确认'
        break
      case '完成服务':
        newStatus = '已完成'
        break
      case '取消预约':
        newStatus = '已取消'
        break
      default:
        ElMessage.error('未知的操作类型')
        return
    }
    
    const params = {
      id: currentAppointment.value.id,
      status: newStatus,
      remark: remarkContent.value,
      operatorId: userStore.userInfo.id,
      operatorName: userStore.userInfo.name || userStore.userInfo.username
    }
    
    await request.put(`/service/appointment/${currentAppointment.value.id}/status`, params, {
      successMsg: `${statusAction.value}成功`,
      onSuccess: () => {
        remarkDialogVisible.value = false
        
        // 如果详情对话框已打开，刷新详情
        if (detailDialogVisible.value) {
          fetchAppointmentDetails(currentAppointment.value.id)
        }
        
        // 刷新列表
        fetchAppointments()
      }
    })
  } catch (error) {
    console.error(`${statusAction.value}失败:`, error)
  } finally {
    statusChanging.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchAppointments()
})
</script>

<style scoped>
.appointment-management-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.appointment-details {
  margin-bottom: 20px;
}

.requirements-content {
  white-space: pre-wrap;
  color: #606266;
}

@media (max-width: 768px) {
  .el-form-item {
    margin-right: 0;
    width: 100%;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style> 