<template>
  <div class="banner-management">
    <div class="page-header">
      <h1 class="page-title">轮播图管理</h1>
      <p class="page-subtitle">管理网站轮播图，控制首页展示内容</p>
    </div>
    
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchBanners" :icon="Search">搜索</el-button>
          <el-button @click="resetSearch" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>轮播图列表</span>
          <el-button type="primary" @click="handleAdd" :icon="Plus">新增轮播图</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border>
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column label="图片" min-width="180">
          <template #default="scope">
            <el-image 
              :src="getImageUrl(scope.row.imageUrl)" 
              fit="cover" 
              style="width: 150px; height: 80px; border-radius: 4px;"
              :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="linkUrl" label="链接" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" min-width="80" />
        <el-table-column label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
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
    
    <!-- 新增/编辑轮播图对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增轮播图' : '编辑轮播图'" 
      v-model="dialogVisible" 
      width="650px"
    >
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="bannerFormRef" 
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <el-upload
            class="banner-upload"
            :action="`${baseAPI}/file/upload/img`"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <el-image 
              v-if="form.imageUrl" 
              :src="getImageUrl(form.imageUrl)" 
              class="banner-image" 
            />
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <div class="text">点击上传图片</div>
              <div class="tips">建议尺寸: 1920×500像素，格式: JPG/PNG</div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" placeholder="请输入跳转链接" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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
    <el-dialog title="轮播图预览" v-model="previewVisible" width="950px">
      <div class="preview-container">
        <div 
          class="preview-banner" 
          :style="{ 
            backgroundImage: `url(${getImageUrl(previewData.imageUrl)})`,
            backgroundPosition: 'center center'
          }"
        >
          <div class="preview-content" v-if="previewData.title">
            <h2 class="preview-title">{{ previewData.title }}</h2>
          </div>
        </div>
        <div class="preview-info">
          <h3>轮播图信息</h3>
          <div class="info-item">
            <span class="label">标题:</span>
            <span class="value">{{ previewData.title }}</span>
          </div>
          <div class="info-item">
            <span class="label">链接:</span>
            <span class="value">{{ previewData.linkUrl }}</span>
          </div>
          <div class="info-item">
            <span class="label">排序:</span>
            <span class="value">{{ previewData.sort }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态:</span>
            <el-tag :type="previewData.status === 1 ? 'success' : 'info'">
              {{ previewData.status === 1 ? '启用' : '禁用' }}
            </el-tag>
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
  View, 
  Picture, 
  QuestionFilled 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const baseAPI = process.env.VUE_APP_BASE_API || '/api'
const uploadHeaders = computed(() => {
  return {
    token: userStore.token
  }
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  title: '',
  status: ''
})

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchBanners()
}

// 获取轮播图列表
const fetchBanners = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      title: searchForm.title || undefined,
      status: searchForm.status ? parseInt(searchForm.status) : undefined,
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    await request.get('/banner/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取轮播图列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBanners()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBanners()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // add or edit
const bannerFormRef = ref(null)
const submitLoading = ref(false)

// 表单数据
const form = reactive({
  id: null,
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  status: 1
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ],
  imageUrl: [
    { required: true, message: '请上传图片', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '请设置排序值', trigger: 'blur' }
  ]
}

// 新增轮播图
const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑轮播图
const handleEdit = (row) => {
  dialogType.value = 'edit'
  resetForm()
  
  Object.keys(form).forEach(key => {
    if (row[key] !== undefined) {
      form[key] = row[key]
    }
  })
  
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.title = ''
  form.imageUrl = ''
  form.linkUrl = ''
  form.sort = 0
  form.status = 1
  
  if (bannerFormRef.value) {
    bannerFormRef.value.resetFields()
  }
}

// 图片上传前的处理
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  
  return true
}

// 图片上传成功的处理
const handleUploadSuccess = (res, file) => {
  if (res.code === '200') {
    form.imageUrl = res.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败: ' + res.msg)
  }
}

// 处理图片URL，添加API前缀
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http') || url.startsWith('data:') || url.startsWith('blob:')) {
    return url
  }
  return baseAPI + url
}

// 提交表单
const submitForm = () => {
  bannerFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          // 新增
          await request.post('/banner', form, {
            successMsg: '轮播图添加成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchBanners()
            }
          })
        } else {
          // 编辑
          await request.put(`/banner/${form.id}`, form, {
            successMsg: '轮播图更新成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchBanners()
            }
          })
        }
      } catch (error) {
        console.error('提交轮播图表单失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除轮播图
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/banner/${row.id}`, {
        successMsg: '轮播图已删除',
        onSuccess: () => {
          fetchBanners()
        }
      })
    } catch (error) {
      console.error('删除轮播图失败:', error)
    }
  }).catch(() => {})
}

// 切换轮播图状态
const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(`确定要${statusText}该轮播图吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/banner/${row.id}/status/${newStatus}`, null, {
        successMsg: `轮播图已${statusText}`,
        onSuccess: () => {
          fetchBanners()
        }
      })
    } catch (error) {
      console.error(`${statusText}轮播图失败:`, error)
    }
  }).catch(() => {})
}

// 预览轮播图
const previewVisible = ref(false)
const previewData = reactive({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  status: 1
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

onMounted(() => {
  fetchBanners()
})
</script>

<style lang="scss" scoped>
.banner-management {
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
  
  .banner-upload {
    .banner-image {
      width: 100%;
      height: 200px;
      border-radius: 6px;
      object-fit: cover;
    }
    
    .upload-placeholder {
      width: 100%;
      height: 200px;
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      background-color: #fafafa;
      
      &:hover {
        border-color: #FFB6C1;
        
        .text {
          color: #FFB6C1;
        }
      }
      
      .el-icon {
        font-size: 32px;
        color: #8c939d;
        margin-bottom: 10px;
      }
      
      .text {
        color: #606266;
        font-size: 14px;
        margin-bottom: 5px;
      }
      
      .tips {
        color: #999;
        font-size: 12px;
      }
    }
  }
  
  .preview-container {
    .preview-banner {
      height: 300px;
      width: 100%;
      background-size: cover;
      border-radius: 8px;
      position: relative;
      margin-bottom: 20px;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(to right, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.2) 100%);
        border-radius: 8px;
      }
    }
    
    .preview-content {
      position: absolute;
      left: 10%;
      top: 50%;
      transform: translateY(-50%);
      color: white;
      max-width: 500px;
      z-index: 1;
      text-align: left;
    }
    
    .preview-title {
      font-size: 32px;
      margin: 0 0 10px;
      font-weight: 700;
    }
    
    .preview-info {
      padding: 20px;
      background-color: #f9f9f9;
      border-radius: 8px;
      
      h3 {
        margin: 0 0 15px;
        font-size: 18px;
        color: #333;
        font-weight: 600;
      }
      
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