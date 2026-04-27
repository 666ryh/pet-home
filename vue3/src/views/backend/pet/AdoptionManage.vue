<template>
  <div class="adoption-manage-container">
    <div class="adoption-header">
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="宠物ID">
            <el-input v-model="searchForm.petId" placeholder="宠物ID" clearable />
          </el-form-item>
          <el-form-item label="用户ID">
            <el-input v-model="searchForm.userId" placeholder="用户ID" clearable />
          </el-form-item>
          <el-form-item label="申请状态">
            <el-select v-model="searchForm.status" placeholder="申请状态" clearable>
              <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="adoptionList"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="petId" label="宠物ID" width="100" />
      <el-table-column prop="applyReason" label="申请理由" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column prop="contactPhone" label="联系电话" width="120" />
      <el-table-column prop="address" label="联系地址" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="160" :formatter="formatDateTime" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button 
            v-if="scope.row.status === '已申请' || scope.row.status === '审核中'"
            type="success" 
            size="small" 
            @click="handleApprove(scope.row)"
          >
            通过
          </el-button>
          <el-button 
            v-if="scope.row.status === '已申请' || scope.row.status === '审核中'"
            type="danger" 
            size="small" 
            @click="handleReject(scope.row)"
          >
            拒绝
          </el-button>
     
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { formatDate } from '@/utils/dateUtils'
import request from '@/utils/request'

const router = useRouter()

const loading = ref(false)
const adoptionList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusOptions = ['已申请', '审核中', '已通过', '已拒绝']

// 搜索表单
const searchForm = reactive({
  petId: '',
  userId: '',
  status: ''
})

// 获取领养申请列表
const fetchAdoptions = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    // 添加筛选条件
    if (searchForm.petId) params.petId = searchForm.petId
    if (searchForm.userId) params.userId = searchForm.userId
    if (searchForm.status) params.status = searchForm.status
    
    await request.get('/adoption/page', params, {
      onSuccess: (res) => {
        adoptionList.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取领养申请列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchAdoptions()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchAdoptions()
}

// 处理分页事件
const handlePageChange = (page) => {
  currentPage.value = page
  fetchAdoptions()
}



// 通过申请
const handleApprove = (row) => {
  ElMessageBox.confirm('确定要通过该申请吗? 通过后该宠物将被标记为已领养，其他申请将被自动拒绝。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/adoption/${row.id}/review`, null, {
        params: {
          status: '已通过'
        },
        successMsg: '申请已通过',
        onSuccess: () => {
          fetchAdoptions() // 刷新列表
        }
      })
    } catch (error) {
      console.error('审核申请失败:', error)
    }
  }).catch(() => {})
}

// 拒绝申请
const handleReject = (row) => {
  ElMessageBox.confirm('确定要拒绝该申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/adoption/${row.id}/review`, null, {
        params: {
          status: '已拒绝'
        },
        successMsg: '申请已拒绝',
        onSuccess: () => {
          fetchAdoptions() // 刷新列表
        }
      })
    } catch (error) {
      console.error('审核申请失败:', error)
    }
  }).catch(() => {})
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case '已申请':
      return 'info'
    case '审核中':
      return 'warning'
    case '已通过':
      return 'success'
    case '已拒绝':
      return 'danger'
    default:
      return 'info'
  }
}

// 格式化日期时间
const formatDateTime = (row, column, cellValue) => {
  return formatDate(cellValue)
}

onMounted(() => {
  fetchAdoptions()
})
</script>

<style scoped>
.adoption-manage-container {
  padding: 20px;
}

.adoption-header {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}
</style> 