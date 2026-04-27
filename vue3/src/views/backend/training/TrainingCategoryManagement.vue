<template>
  <div class="category-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>训练分类管理</span>
          <el-button type="primary" @click="openAddDialog" plain>
            新增分类
          </el-button>
        </div>
      </template>
      
      <div class="filter-container">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="分类名称">
            <el-input v-model="filterForm.name" placeholder="分类名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="分类状态" clearable>
              <el-option :value="1" label="启用" />
              <el-option :value="0" label="停用" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchCategories">查询</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table
        v-loading="loading"
        :data="categories"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="分类描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="scope">
            <el-icon v-if="scope.row.icon" :size="20">
              <component :is="scope.row.icon" />
            </el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="openEditDialog(scope.row)"
              plain
            >
              编辑
            </el-button>
            <el-button 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              size="small" 
              @click="toggleStatus(scope.row)"
              plain
            >
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-popconfirm
              confirm-button-text="确定"
              cancel-button-text="取消"
              title="确定删除该分类吗？"
              @confirm="deleteCategory(scope.row.id)"
            >
              <template #reference>
                <el-button 
                  type="danger" 
                  size="small" 
                  plain
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 分类表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="rules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        
        <el-form-item label="分类图标" prop="icon">
          <el-select v-model="categoryForm.icon" placeholder="选择图标" clearable filterable>
            <el-option
              v-for="icon in iconOptions"
              :key="icon.value"
              :label="icon.label"
              :value="icon.value"
            >
              <div class="icon-option">
                <el-icon><component :is="icon.value" /></el-icon>
                <span>{{ icon.label }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number
            v-model="categoryForm.sortOrder"
            :min="0"
            :max="999"
            controls-position="right"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as ElementPlusIcons from '@element-plus/icons-vue'
import request from '@/utils/request'

// 数据定义
const loading = ref(false)
const submitting = ref(false)
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const categoryFormRef = ref(null)

// 对话框标题
const dialogTitle = computed(() => {
  return categoryForm.id ? '编辑分类' : '新增分类'
})

// 过滤表单
const filterForm = reactive({
  name: '',
  status: ''
})

// 分类表单
const categoryForm = reactive({
  id: '',
  name: '',
  description: '',
  icon: '',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序顺序', trigger: 'blur' }
  ]
}

// 图标选项
const iconOptions = ref([])

// 初始化图标选项
const initIconOptions = () => {
  const icons = []
  for (const key in ElementPlusIcons) {
    if (key !== 'default') {
      icons.push({
        value: key,
        label: key
      })
    }
  }
  iconOptions.value = icons
}

// 加载分类列表
const loadCategories = async () => {
  loading.value = true
  try {
    const params = {
      name: filterForm.name || undefined,
      status: filterForm.status === '' ? undefined : filterForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }

    
    await request.get('/training/category/page',  params , {
      onSuccess: (res) => {
        categories.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开添加对话框
const openAddDialog = () => {
  resetCategoryForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (row) => {
  resetCategoryForm()
  Object.assign(categoryForm, row)
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!categoryFormRef.value) return
  
  await categoryFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const isEdit = !!categoryForm.id
        const api = isEdit ? `/training/category/${categoryForm.id}` : '/training/category'
        const method = isEdit ? 'put' : 'post'
        
        categoryForm.createTime = null
        categoryForm.updateTime = null
        await request[method](api, categoryForm, {
          successMsg: isEdit ? '分类更新成功' : '分类添加成功',
          onSuccess: () => {
            dialogVisible.value = false
            loadCategories()
          }
        })
      } catch (error) {
        console.error('保存分类失败:', error)
      } finally {
        submitting.value = false
      }
    } else {
      return false
    }
  })
}

// 切换分类状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '停用'
  
  try {
    await request.put(`/training/category/${row.id}/status?status=${newStatus}`, null, {
      successMsg: `分类已${statusText}`,
      onSuccess: () => {
        row.status = newStatus
      }
    })
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 删除分类
const deleteCategory = async (id) => {
  try {
    await request.delete(`/training/category/${id}`, {
      successMsg: '分类已删除',
      onSuccess: () => {
        loadCategories()
      }
    })
  } catch (error) {
    console.error('删除分类失败:', error)
  }
}

// 搜索分类
const searchCategories = () => {
  currentPage.value = 1
  loadCategories()
}

// 重置表单
const resetForm = () => {
  filterForm.name = ''
  filterForm.status = ''
  currentPage.value = 1
  loadCategories()
}

// 重置分类表单
const resetCategoryForm = () => {
  Object.assign(categoryForm, {
    id: '',
    name: '',
    description: '',
    icon: '',
    sortOrder: 0,
    status: 1
  })
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadCategories()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadCategories()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 页面加载时获取数据
onMounted(() => {
  initIconOptions()
  loadCategories()
})
</script>

<style scoped>
.category-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.icon-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-table .cell) {
  white-space: nowrap;
}
</style> 