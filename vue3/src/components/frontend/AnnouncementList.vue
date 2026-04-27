<template>
  <div class="announcement-list">
    <div class="announcement-header">
      <div class="title-wrapper">
        <i class="announcement-icon"></i>
        <h3 class="announcement-title">{{ title }}</h3>
      </div>
      <div class="more-link" v-if="showMoreLink" @click="onViewMore">
        更多公告
        <el-icon><arrow-right /></el-icon>
      </div>
    </div>
    
    <div class="announcement-content" v-loading="loading">
      <template v-if="announcements.length > 0">
        <div 
          class="announcement-item" 
          v-for="item in announcements" 
          :key="item.id"
          @click="onAnnouncementClick(item)"
        >
          <div class="announcement-badge" :class="getBadgeClass(item.type)">{{ getTypeShortName(item.type) }}</div>
          <div class="announcement-info">
            <div class="announcement-item-title">{{ item.title }}</div>
            <div class="announcement-date">{{ formatDate(item.createdTime) }}</div>
          </div>
        </div>
      </template>
      <div class="empty-tip" v-else>
        <el-empty description="暂无公告" :image-size="60" />
      </div>
    </div>
    
    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentAnnouncement.title"
      width="600px"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  title: {
    type: String,
    default: '公告'
  },
  limit: {
    type: Number,
    default: 5
  },
  showMoreLink: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['view-more', 'item-click'])

const announcements = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentAnnouncement = reactive({
  id: null,
  title: '',
  type: '',
  content: '',
  createdTime: ''
})

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    await request.get('/announcement/latest', { limit: props.limit }, {
      onSuccess: (data) => {
        announcements.value = data || []
      },
      showDefaultMsg: false
    })
  } catch (error) {
    console.error('获取公告列表失败:', error)
  } finally {
    loading.value = false
  }
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

// 获取公告类型短名称
const getTypeShortName = (type) => {
  switch (type) {
    case 'NOTICE':
      return '通'
    case 'ACTIVITY':
      return '活'
    case 'PROMOTION':
      return '促'
    default:
      return '其'
  }
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

// 获取公告类型徽章样式
const getBadgeClass = (type) => {
  switch (type) {
    case 'NOTICE':
      return 'badge-notice'
    case 'ACTIVITY':
      return 'badge-activity'
    case 'PROMOTION':
      return 'badge-promotion'
    default:
      return 'badge-other'
  }
}

// 查看更多公告
const onViewMore = () => {
  emit('view-more')
}

// 点击公告项
const onAnnouncementClick = (announcement) => {
  // 设置当前公告信息
  Object.keys(currentAnnouncement).forEach(key => {
    if (announcement[key] !== undefined) {
      currentAnnouncement[key] = announcement[key]
    }
  })
  
  dialogVisible.value = true
  emit('item-click', announcement)
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style lang="scss" scoped>
.announcement-list {
  width: 100%;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
  
  .announcement-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #f0f0f0;
    
    .title-wrapper {
      display: flex;
      align-items: center;
      
      .announcement-icon {
        width: 16px;
        height: 16px;
        background-color: #FFB6C1;
        border-radius: 50%;
        margin-right: 8px;
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 8px;
          height: 8px;
          background-color: white;
          border-radius: 50%;
        }
      }
      
      .announcement-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin: 0;
      }
    }
    
    .more-link {
      display: flex;
      align-items: center;
      color: #87CEEB;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s ease;
      
      .el-icon {
        margin-left: 4px;
        font-size: 12px;
        transition: transform 0.3s ease;
      }
      
      &:hover {
        color: #FFB6C1;
        
        .el-icon {
          transform: translateX(3px);
        }
      }
    }
  }
  
  .announcement-content {
    padding: 10px 0;
    min-height: 100px;
    
    .announcement-item {
      display: flex;
      align-items: center;
      padding: 10px 20px;
      cursor: pointer;
      transition: background-color 0.3s ease;
      
      &:hover {
        background-color: #f9f9f9;
      }
      
      .announcement-badge {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        color: white;
        margin-right: 12px;
        flex-shrink: 0;
      }
      
      .badge-notice {
        background-color: #87CEEB;
      }
      
      .badge-activity {
        background-color: #67C23A;
      }
      
      .badge-promotion {
        background-color: #FFA07A;
      }
      
      .badge-other {
        background-color: #909399;
      }
      
      .announcement-info {
        flex: 1;
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .announcement-item-title {
          font-size: 14px;
          color: #333;
          flex: 1;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          margin-right: 15px;
        }
        
        .announcement-date {
          font-size: 12px;
          color: #999;
          flex-shrink: 0;
        }
      }
    }
    
    .empty-tip {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100px;
    }
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
    padding: 15px;
    background-color: #f9f9f9;
    border-radius: 4px;
    line-height: 1.6;
    min-height: 100px;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style> 