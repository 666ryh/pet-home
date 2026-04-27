<template>
  <div class="announcement-management">
    <div class="page-header">
      <h1 class="page-title">公告管理</h1>
      <p class="page-subtitle">管理系统公告，发布重要通知</p>
    </div>
    
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
            <el-option label="通知" value="NOTICE" />
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="促销" value="PROMOTION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchAnnouncements" :icon="Search">搜索</el-button>
          <el-button @click="resetSearch" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>公告列表</span>
          <el-button type="primary" @click="handleAdd" :icon="Plus">新增公告</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border>
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" min-width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ formatDateTime(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              link 
              @click="handlePreview(scope.row)"
              :icon="View"
            >
              预览
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              link 
              @click="handleEdit(scope.row)" 
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'danger' : 'success'" 
              link 
              @click="handleToggleStatus(scope.row)" 
              :icon="scope.row.status === 1 ? 'el-icon-close' : 'el-icon-check'"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              link 
              @click="handleDelete(scope.row)" 
              :icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          @update:current-page="val => currentPage = val"
          @update:page-size="val => pageSize = val"
          background
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑公告对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增公告' : '编辑公告'" 
      v-model="dialogVisible" 
      width="650px"
      @closed="handleDialogClosed"
    >
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="announcementFormRef" 
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="通知" value="NOTICE" />
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="促销" value="PROMOTION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入公告内容" 
          />
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
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 预览对话框 -->
    <el-dialog 
      title="公告预览" 
      v-model="previewVisible" 
      width="650px"
      @closed="handlePreviewClosed"
    >
      <div class="preview-container">
        <div class="preview-header">
          <h2 class="preview-title">{{ previewData.title }}</h2>
          <div class="preview-meta">
            <el-tag :type="getTypeTagType(previewData.type)" size="small">
              {{ getTypeLabel(previewData.type) }}
            </el-tag>
            <span class="preview-time">发布时间：{{ formatDateTime(previewData.createdTime) }}</span>
          </div>
        </div>
        <div class="preview-content">
          <p class="content-text">{{ previewData.content }}</p>
        </div>
        <div class="preview-info">
          <div class="info-item">
            <span class="label">状态:</span>
            <el-tag :type="previewData.status === 1 ? 'success' : 'info'">
              {{ previewData.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">更新时间:</span>
            <span class="value">{{ formatDateTime(previewData.updatedTime) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { 
  Search, 
  Refresh, 
  Plus, 
  Edit, 
  Delete, 
  View
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 表格数据
const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  title: '',
  type: '',
  status: ''
})

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.type = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchAnnouncements()
}

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      title: searchForm.title || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status ? parseInt(searchForm.status) : undefined,
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    await request.get('/announcement/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取公告列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAnnouncements()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAnnouncements()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取类型标签样式
const getTypeTagType = (type) => {
  switch (type) {
    case 'NOTICE':
      return 'primary'
    case 'ACTIVITY':
      return 'success'
    case 'PROMOTION':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取类型标签文本
const getTypeLabel = (type) => {
  switch (type) {
    case 'NOTICE':
      return '通知'
    case 'ACTIVITY':
      return '活动'
    case 'PROMOTION':
      return '促销'
    default:
      return '其他'
  }
}

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // add or edit
const announcementFormRef = ref(null)
const submitLoading = ref(false)

// 表单数据
const form = reactive({
  id: null,
  title: '',
  type: 'NOTICE',
  content: '',
  status: 1
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
}

// 新增公告
const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑公告
const handleEdit = (row) => {
  dialogType.value = 'edit'
  
  // 先重置表单，然后设置新数据
  resetForm()
  
  // 复制行数据到表单
  Object.keys(form).forEach(key => {
    if (row[key] !== undefined) {
      form[key] = row[key]
    }
  })
  
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  // 完全重置表单数据
  form.id = null
  form.title = ''
  form.type = 'NOTICE'
  form.content = ''
  form.status = 1
  
  // 重置表单验证状态
  if (announcementFormRef.value) {
    announcementFormRef.value.resetFields()
  }
}

// 处理对话框关闭
const handleDialogClosed = () => {
  // 确保对话框关闭时完全重置表单
  form.id = null
  form.title = ''
  form.type = 'NOTICE'
  form.content = ''
  form.status = 1
  
  // 重置对话框类型
  dialogType.value = 'add'
  
  // 延迟重置表单验证状态，确保在DOM更新后执行
  setTimeout(() => {
    if (announcementFormRef.value) {
      announcementFormRef.value.resetFields()
    }
  }, 0)
}

// 提交表单
const submitForm = () => {
  announcementFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          // 新增
          await request.post('/announcement', form, {
            successMsg: '公告添加成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchAnnouncements()
            }
          })
        } else {
          // 编辑
          await request.put(`/announcement/${form.id}`, form, {
            successMsg: '公告更新成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchAnnouncements()
            }
          })
        }
      } catch (error) {
        console.error('提交公告表单失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除公告
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/announcement/${row.id}`, {
        successMsg: '公告已删除',
        onSuccess: () => {
          fetchAnnouncements()
        }
      })
    } catch (error) {
      console.error('删除公告失败:', error)
    }
  }).catch(() => {})
}

// 切换公告状态
const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(`确定要${statusText}该公告吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/announcement/${row.id}/status/${newStatus}`, null, {
        successMsg: `公告已${statusText}`,
        onSuccess: () => {
          fetchAnnouncements()
        }
      })
    } catch (error) {
      console.error(`${statusText}公告失败:`, error)
    }
  }).catch(() => {})
}

// 预览公告
const previewVisible = ref(false)
const previewData = reactive({
  title: '',
  type: '',
  content: '',
  status: 1,
  createdTime: '',
  updatedTime: ''
})

const handlePreview = (row) => {
  Object.keys(previewData).forEach(key => {
    if (row[key] !== undefined) {
      previewData[key] = row[key]
    } else {
      previewData[key] = ''
    }
  })
  
  previewVisible.value = true
}

// 处理预览对话框关闭
const handlePreviewClosed = () => {
  // 重置预览数据
  Object.keys(previewData).forEach(key => {
    previewData[key] = ''
  })
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style lang="scss" scoped>
.announcement-management {
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin: 0 0 8px;
    }
    
    .page-subtitle {
      color: #666;
      font-size: 14px;
      margin: 0;
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
    
    .filter-form {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
    }
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;
      font-size: 16px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .preview-container {
    .preview-header {
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #eee;
      
      .preview-title {
        font-size: 22px;
        color: #333;
        margin: 0 0 10px;
      }
      
      .preview-meta {
        display: flex;
        align-items: center;
        gap: 15px;
        
        .preview-time {
          color: #999;
          font-size: 14px;
        }
      }
    }
    
    .preview-content {
      padding: 15px;
      background-color: #f9f9f9;
      border-radius: 8px;
      margin-bottom: 20px;
      min-height: 150px;
      
      .content-text {
        margin: 0;
        white-space: pre-wrap;
        line-height: 1.6;
      }
    }
    
    .preview-info {
      .info-item {
        margin-bottom: 10px;
        display: flex;
        
        .label {
          width: 80px;
          font-weight: 600;
          color: #666;
        }
        
        .value {
          color: #333;
          flex: 1;
        }
      }
    }
  }
}
</style> 