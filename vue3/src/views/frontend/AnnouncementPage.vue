<template>
  <div class="announcement-page">
    <div class="page-header">
      <div class="section-container">
        <h1 class="page-title">公告中心</h1>
        <p class="page-subtitle">查看宠物之家的最新公告和活动信息</p>
      </div>
    </div>
    
    <div class="section-container">
      <div class="filter-section">
        <el-form :inline="true" :model="searchForm" class="filter-form">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
              <el-option label="全部" value="" />
              <el-option label="通知" value="NOTICE" />
              <el-option label="活动" value="ACTIVITY" />
              <el-option label="促销" value="PROMOTION" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchAnnouncements" :icon="Search">搜索</el-button>
            <el-button @click="resetSearch" :icon="Refresh">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="announcement-list-container" v-loading="loading">
        <template v-if="announcements.length > 0">
          <div class="announcement-card" v-for="item in announcements" :key="item.id" @click="handleAnnouncementClick(item)">
            <div class="announcement-card-header">
              <div class="announcement-title-wrapper">
                <el-tag :type="getTypeTagType(item.type)" size="small" class="announcement-tag">
                  {{ getTypeLabel(item.type) }}
                </el-tag>
                <h3 class="announcement-title">{{ item.title }}</h3>
              </div>
              <div class="announcement-date">{{ formatDate(item.createdTime) }}</div>
            </div>
            <div class="announcement-card-content">
              <p class="announcement-preview">{{ getContentPreview(item.content) }}</p>
            </div>
            <div class="announcement-card-footer">
              <el-button text type="primary" @click.stop="handleAnnouncementClick(item)">
                查看详情
                <el-icon><arrow-right /></el-icon>
              </el-button>
            </div>
          </div>
        </template>
        <div class="empty-tip" v-else>
          <el-empty description="暂无公告" />
        </div>
      </div>
      
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          @update:current-page="val => currentPage = val"
          @update:page-size="val => pageSize = val"
          background
        />
      </div>
    </div>
    
    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentAnnouncement.title"
      width="700px"
      destroy-on-close
    >
      <div class="announcement-detail">
        <div class="announcement-meta">
          <el-tag :type="getTypeTagType(currentAnnouncement.type)" size="small">
            {{ getTypeLabel(currentAnnouncement.type) }}
          </el-tag>
          <span class="announcement-time">发布时间：{{ formatDateTime(currentAnnouncement.createdTime) }}</span>
        </div>
        <div class="announcement-detail-content">
          {{ currentAnnouncement.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 搜索表单
const searchForm = reactive({
  title: '',
  type: ''
})

// 表格数据
const announcements = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框相关
const dialogVisible = ref(false)
const currentAnnouncement = reactive({
  id: null,
  title: '',
  type: '',
  content: '',
  createdTime: ''
})

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.type = ''
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
      status: 1, // 只查询启用的公告
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    await request.get('/announcement/page', params, {
      onSuccess: (res) => {
        announcements.value = res.records || []
        total.value = res.total || 0
      },
      showDefaultMsg: false
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

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取公告类型标签样式
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

// 获取公告类型标签文本
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

// 获取内容预览
const getContentPreview = (content) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

// 点击公告
const handleAnnouncementClick = (announcement) => {
  // 设置当前公告信息
  Object.keys(currentAnnouncement).forEach(key => {
    if (announcement[key] !== undefined) {
      currentAnnouncement[key] = announcement[key]
    }
  })
  
  dialogVisible.value = true
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style lang="scss" scoped>
.announcement-page {
  min-height: 100vh;
  background-color: #FFF9E6;
  padding-bottom: 40px;
  
  .section-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
  
  .page-header {
    background-color: #FFB6C1;
    padding: 40px 0;
    margin-bottom: 30px;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 15px;
      background: linear-gradient(135deg, #FFF9E6 25%, transparent 25%) -10px 0,
                  linear-gradient(225deg, #FFF9E6 25%, transparent 25%) -10px 0,
                  linear-gradient(315deg, #FFF9E6 25%, transparent 25%),
                  linear-gradient(45deg, #FFF9E6 25%, transparent 25%);
      background-size: 20px 20px;
      background-color: #FFB6C1;
    }
    
    .page-title {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 32px;
      color: white;
      margin: 0 0 10px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
    
    .page-subtitle {
      font-size: 16px;
      color: rgba(255, 255, 255, 0.9);
      margin: 0;
    }
  }
  
  .filter-section {
    background-color: white;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }
  
  .announcement-list-container {
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    gap: 20px;
    margin-bottom: 30px;
  }
  
  .announcement-card {
    background-color: white;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    }
    
    .announcement-card-header {
      padding: 20px;
      border-bottom: 1px solid #f0f0f0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .announcement-title-wrapper {
        display: flex;
        align-items: center;
        
        .announcement-tag {
          margin-right: 10px;
        }
        
        .announcement-title {
          font-size: 18px;
          margin: 0;
          font-weight: 600;
          color: #333;
        }
      }
      
      .announcement-date {
        font-size: 14px;
        color: #999;
      }
    }
    
    .announcement-card-content {
      padding: 20px;
      
      .announcement-preview {
        margin: 0;
        color: #666;
        line-height: 1.6;
      }
    }
    
    .announcement-card-footer {
      padding: 10px 20px 20px;
      text-align: right;
      
      .el-button {
        .el-icon {
          margin-left: 5px;
          transition: transform 0.3s ease;
        }
        
        &:hover .el-icon {
          transform: translateX(3px);
        }
      }
    }
  }
  
  .empty-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 30px;
  }
}

.announcement-detail {
  .announcement-meta {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    
    .announcement-time {
      margin-left: 10px;
      font-size: 14px;
      color: #999;
    }
  }
  
  .announcement-detail-content {
    padding: 20px;
    background-color: #f9f9f9;
    border-radius: 8px;
    line-height: 1.8;
    min-height: 150px;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style> 