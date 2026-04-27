<template>
  <div class="service-management-container">
    <el-card shadow="never" class="search-card">
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="服务名称">
            <el-input v-model="searchForm.name" placeholder="请输入服务名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="服务分类">
            <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
              <el-option
                v-for="item in categories"
                :key="item.id"
                :label="item.name"
                :disabled="item.status === 0"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option :value="1" label="启用"></el-option>
              <el-option :value="0" label="停用"></el-option>
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
          <span>服务列表</span>
          <el-button type="primary" @click="openDialog()">添加服务</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="name" label="服务名称" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column label="服务分类" min-width="120">
          <template #default="scope">
            {{ getCategoryName(scope.row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" align="center">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column label="时长" width="100" align="center">
          <template #default="scope">
            {{ getDurationText(scope.row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="changeStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '停用' : '启用' }}
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
    
    <!-- 服务表单对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="form.id ? '编辑服务' : '添加服务'" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px" 
        label-position="right"
        status-icon
      >
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入服务名称"></el-input>
        </el-form-item>
        
        <el-form-item label="服务分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :disabled="item.status === 0"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="服务价格" prop="price">
          <el-input-number 
            v-model="form.price" 
            :min="0" 
            :precision="2" 
            :step="10" 
            style="width: 100%"
          ></el-input-number>
        </el-form-item>
        
        <el-form-item label="服务时长" prop="duration">
          <el-input-number 
            v-model="form.duration" 
            :min="0" 
            :step="30" 
            style="width: 100%"
          ></el-input-number>
          <span class="duration-hint">分钟</span>
        </el-form-item>
        
        <el-form-item label="服务状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          ></el-switch>
        </el-form-item>
        
        <el-form-item label="服务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入服务描述"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 分类管理对话框 -->
 
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 数据定义
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const categorySubmitting = ref(false)
const tableData = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const formRef = ref(null)
const categoryFormRef = ref(null)

// 搜索表单
const searchForm = ref({
  name: '',
  categoryId: '',
  status: ''
})

// 服务表单
const form = ref({
  id: null,
  name: '',
  categoryId: '',
  price: 0,
  duration: 60,
  description: '',
  status: 1
})

// 分类表单
const categoryForm = ref({
  id: null,
  name: '',
  description: '',
  icon: '',
  sort: 0,
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入服务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择服务分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入服务价格', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入服务时长', trigger: 'blur' }
  ]
}

// 分类表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  const category = categories.value.find(item => item.id === categoryId)
  return category ? category.name : '未知分类'
}

// 处理持续时间显示
const getDurationText = (minutes) => {
  if (!minutes) return '-'
  
  if (minutes < 60) {
    return `${minutes}分钟`
  } else if (minutes === 60) {
    return '1小时'
  } else if (minutes < 1440) {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
  } else {
    const days = Math.floor(minutes / 1440)
    return `${days}天`
  }
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取服务列表
const fetchServices = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      ...searchForm.value
    }
    
    await request.get('/service/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取服务列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取服务分类
const fetchCategories = async () => {
  try {
    await request.get('/service/category/list', null, {
      onSuccess: (res) => {
        categories.value = res
      }
    })
  } catch (error) {
    console.error('获取服务分类失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchServices()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    name: '',
    categoryId: '',
    status: ''
  }
  handleSearch()
}

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchServices()
}

// 处理当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchServices()
}

// 打开对话框
const openDialog = (row) => {
  if (row) {
    form.value = JSON.parse(JSON.stringify(row))
  } else {
    form.value = {
      id: null,
      name: '',
      categoryId: '',
      price: 0,
      duration: 60,
      description: '',
      status: 1
    }
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (form.value.id) {
          // 编辑
          await request.put(`/service/${form.value.id}`, form.value, {
            successMsg: '更新成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchServices()
            }
          })
        } else {
          // 新增
          await request.post('/service', form.value, {
            successMsg: '添加成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchServices()
            }
          })
        }
      } catch (error) {
        console.error('提交服务信息失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 更改服务状态
const changeStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '停用'
  
  ElMessageBox.confirm(
    `确定要${actionText}该服务吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.put(`/service/${row.id}/status?status=${newStatus}`, null, {
        successMsg: `${actionText}成功`,
        onSuccess: () => {
          fetchServices()
        }
      })
    } catch (error) {
      console.error(`${actionText}失败:`, error)
    }
  }).catch(() => {})
}






// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchServices()
})
</script>

<style scoped>
.service-management-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.duration-hint {
  margin-left: 10px;
  color: #909399;
}

.category-list-section {
  margin-bottom: 20px;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.category-header span {
  font-size: 16px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .el-form-item {
    margin-right: 0;
    width: 100%;
  }
}
</style> 