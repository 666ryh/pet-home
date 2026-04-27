<template>
  <div class="service-category-management-container">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <span>服务分类管理</span>
          <el-button type="primary" @click="handleAdd">添加分类</el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="分类名称">
            <el-input v-model="searchForm.name" placeholder="请输入分类名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option :value="1" label="启用"></el-option>
              <el-option :value="0" label="禁用"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 分类列表 -->
      <el-table
        v-loading="loading"
        :data="categories"
        border
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="150"></el-table-column>
        <el-table-column prop="description" label="分类描述" min-width="200">
          <template #default="scope">
            {{ scope.row.description || '暂无描述' }}
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="100" align="center">
          <template #default="scope">
            <el-icon v-if="scope.row.icon"><component :is="scope.row.icon"></component></el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center"></el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑分类对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="form.id ? '编辑分类' : '添加分类'" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px" 
        status-icon
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        
        <el-form-item label="分类描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入分类描述"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称"></el-input>
          <div class="icon-hint">Element Plus 图标名称，例如: "Pet", "CoffeeCup"</div>
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right"></el-input-number>
          <div class="sort-hint">数字越小排序越靠前</div>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 数据定义
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref(null)

// 搜索表单
const searchForm = ref({
  name: '',
  status: null
})

// 添加/编辑表单
const form = ref({
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
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '最多不超过 200 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    await request.get('/service/category/list', null, {
      onSuccess: (res) => {
        // 根据搜索条件筛选
        let filteredData = [...res]
        
        if (searchForm.value.name) {
          filteredData = filteredData.filter(item => 
            item.name.toLowerCase().includes(searchForm.value.name.toLowerCase())
          )
        }
        
        if (searchForm.value.status !== null) {
          filteredData = filteredData.filter(item => item.status === searchForm.value.status)
        }
        
        // 手动分页
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value
        
        total.value = filteredData.length
        categories.value = filteredData.slice(start, end)
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 添加分类
const handleAdd = () => {
  form.value = {
    id: null,
    name: '',
    description: '',
    icon: '',
    sort: 0,
    status: 1
  }
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  form.value = {
    id: row.id,
    name: row.name,
    description: row.description || '',
    icon: row.icon || '',
    sort: row.sort || 0,
    status: row.status
  }
  dialogVisible.value = true
}

// 修改状态
const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(
    `确定要${statusText}该分类吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const updatedCategory = {
        ...row,
        status: newStatus
      }
      
      await request.put(`/service/category/${row.id}`, updatedCategory, {
        successMsg: `${statusText}成功`,
        onSuccess: () => {
          fetchCategories()
        }
      })
    } catch (error) {
      console.error(`${statusText}失败:`, error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 删除分类
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该分类吗？删除后不可恢复',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.delete(`/service/category/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchCategories()
        }
      })
    } catch (error) {
      console.error('删除分类失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (form.value.id) {
          // 编辑模式
          await request.put(`/service/category/${form.value.id}`, form.value, {
            successMsg: '更新成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchCategories()
            }
          })
        } else {
          // 添加模式
          await request.post('/service/category', form.value, {
            successMsg: '添加成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchCategories()
            }
          })
        }
      } catch (error) {
        console.error('保存分类失败:', error)
      } finally {
        submitting.value = false
      }
    } else {
      ElMessage.warning('请完善表单信息')
      return false
    }
  })
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCategories()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    name: '',
    status: null
  }
  currentPage.value = 1
  fetchCategories()
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchCategories()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCategories()
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.service-category-management-container {
  padding: 20px;
}

.main-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 18px;
  font-weight: bold;
}

.search-section {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.icon-hint,
.sort-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 