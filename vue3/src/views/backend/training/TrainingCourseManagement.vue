<template>
  <div class="training-course-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>训练课程管理</span>
          <el-button type="primary" @click="openAddDialog" plain>
            新增课程
          </el-button>
        </div>
      </template>
      
      <div class="filter-container">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="课程名称">
            <el-input v-model="filterForm.name" placeholder="课程名称" clearable />
          </el-form-item>
          <el-form-item label="课程分类">
            <el-select v-model="filterForm.categoryId" placeholder="选择分类" clearable>
              <el-option 
                v-for="category in categories" 
                :key="category.id" 
                :disabled="category.status === 0"
                :label="category.name" 
                :value="category.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="课程状态" clearable>
              <el-option :value="1" label="启用" />
              <el-option :value="0" label="停用" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchCourses">查询</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table
        v-loading="loading"
        :data="courses"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="课程名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="price" label="价格(¥)" width="100">
          <template #default="scope">
            {{ formatPrice(scope.row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="maxParticipants" label="最大人数" width="100" />
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
              title="确定删除该课程吗？"
              @confirm="deleteCourse(scope.row.id)"
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
    
    <!-- 课程表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="650px"
      destroy-on-close
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="rules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        
        <el-form-item label="课程分类" prop="categoryId">
          <el-select v-model="courseForm.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :label="category.name" 
              :disabled="category.status === 0"
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程价格" prop="price">
              <el-input-number
                v-model="courseForm.price"
                :min="0"
                :precision="2"
                :step="10"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程时长" prop="duration">
              <el-input-number
                v-model="courseForm.duration"
                :min="1"
                :precision="0"
                :step="15"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="最大参与人数" prop="maxParticipants">
          <el-input-number
            v-model="courseForm.maxParticipants"
            :min="1"
            :max="100"
            :precision="0"
            :step="1"
            style="width: 40%"
          />
        </el-form-item>
        
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="courseForm.status">
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
    
    <!-- 分类管理对话框 -->
    <el-dialog
      title="训练课程分类管理"
      v-model="categoryDialogVisible"
      width="500px"
    >
      <div class="category-container">
        <div class="category-form">
          <el-form :inline="true" :model="categoryForm" class="demo-form-inline">
            <el-form-item label="分类名称" prop="name">
              <el-input v-model="categoryForm.name" placeholder="分类名称" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitCategory">
                {{ categoryForm.id ? '更新' : '添加' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <el-divider />
        
        <div class="category-list">
          <el-table :data="categories" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="分类名称" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="editCategory(scope.row)"
                  plain
                >
                  编辑
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
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 数据定义
const loading = ref(false)
const submitting = ref(false)
const courses = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const courseFormRef = ref(null)
const categories = ref([])

// 对话框标题
const dialogTitle = computed(() => {
  return courseForm.id ? '编辑课程' : '新增课程'
})

// 过滤表单
const filterForm = reactive({
  name: '',
  categoryId: '',
  status: ''
})

// 课程表单
const courseForm = reactive({
  id: '',
  name: '',
  categoryId: '',
  price: 0,
  duration: 60,
  maxParticipants: 1,
  description: '',
  status: 1
})

// 分类表单
const categoryForm = reactive({
  id: '',
  name: '',
  status: 1 // 默认启用
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择课程分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入课程价格', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'blur' }
  ]
}

// 加载课程列表
const loadCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      ...filterForm
    }
    
    await request.get('/training/course/page', params, {
      onSuccess: (res) => {
        courses.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    await request.get('/training/category/list', null, {
      onSuccess: (res) => {
        categories.value = res || []
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 打开添加对话框
const openAddDialog = () => {
  resetCourseForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (row) => {
  Object.assign(courseForm, row)
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!courseFormRef.value) return
  
  await courseFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const isEdit = !!courseForm.id
        const api = isEdit ? `/training/course/${courseForm.id}` : '/training/course'
        const method = isEdit ? 'put' : 'post'
        courseForm.createTime = null
        courseForm.updateTime = null
        

        await request[method](api, courseForm, {
          successMsg: isEdit ? '课程更新成功' : '课程添加成功',
          onSuccess: () => {
            dialogVisible.value = false
            loadCourses()
          }
        })
      } catch (error) {
        console.error('保存课程失败:', error)
      } finally {
        submitting.value = false
      }
    } else {
      return false
    }
  })
}

// 切换课程状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '停用'
  
  try {
    await request.put(`/training/course/${row.id}/status?status=${newStatus}`, null, {
      successMsg: `课程已${statusText}`,
      onSuccess: () => {
        row.status = newStatus
      }
    })
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 删除课程
const deleteCourse = async (id) => {
  try {
    await request.delete(`/training/course/${id}`, {
      successMsg: '课程已删除',
      onSuccess: () => {
        loadCourses()
      }
    })
  } catch (error) {
    console.error('删除课程失败:', error)
  }
}

// 搜索课程
const searchCourses = () => {
  currentPage.value = 1
  loadCourses()
}

// 重置表单
const resetForm = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key] = ''
  })
  currentPage.value = 1
  loadCourses()
}

// 重置课程表单
const resetCourseForm = () => {
  Object.assign(courseForm, {
    id: '',
    name: '',
    categoryId: '',
    price: 0,
    duration: 60,
    maxParticipants: 1,
    description: '',
    status: 1
  })
}

// 打开分类管理对话框
const openCategoryDialog = () => {
  resetCategoryForm()
  categoryDialogVisible.value = true
}

// 编辑分类
const editCategory = (row) => {
  Object.assign(categoryForm, row)
}

// 提交分类
const submitCategory = async () => {
  if (!categoryForm.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  
  try {
    const isEdit = !!categoryForm.id
    const api = isEdit ? `/training/category/${categoryForm.id}` : '/training/category'
    const method = isEdit ? 'put' : 'post'
    
    await request[method](api, categoryForm, {
      successMsg: isEdit ? '分类更新成功' : '分类添加成功',
      onSuccess: () => {
        resetCategoryForm()
        loadCategories()
      }
    })
  } catch (error) {
    console.error('保存分类失败:', error)
  }
}

// 删除分类
const deleteCategory = async (id) => {
  try {
    await request.delete(`/training/category/${id}`, null, {
      successMsg: '分类已删除',
      onSuccess: () => {
        loadCategories()
      }
    })
  } catch (error) {
    console.error('删除分类失败:', error)
  }
}

// 重置分类表单
const resetCategoryForm = () => {
  Object.assign(categoryForm, {
    id: '',
    name: '',
    status: 1 // 默认启用
  })
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadCourses()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadCourses()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 格式化价格
const formatPrice = (price) => {
  return price.toFixed(2)
}

// 页面加载时获取数据
onMounted(() => {
  loadCourses()
  loadCategories()
})
</script>

<style scoped>
.training-course-management {
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

.category-container {
  padding: 10px;
}

.category-form {
  margin-bottom: 20px;
}

.category-list {
  margin-top: 20px;
}

:deep(.el-table .cell) {
  white-space: nowrap;
}
</style> 