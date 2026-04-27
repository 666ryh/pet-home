<template>
  <div class="view-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="电影名称">
          <el-input v-model="searchForm.movieTitle" placeholder="请输入电影名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <span class="title">浏览历史</span>
            <el-button :icon="Refresh" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button :icon="Setting" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('username')" prop="username" label="用户名" min-width="120" />
        <el-table-column v-if="isColumnVisible('movieTitle')" prop="movieTitle" label="电影名称" min-width="180" />
        <el-table-column v-if="isColumnVisible('coverImage')" prop="coverImage" label="电影封面" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="getImageUrl(row.coverImage)"
              :preview-src-list="[getImageUrl(row.coverImage)]"
              fit="cover"
              class="movie-cover"
            />
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('browseTime')" prop="browseTime" label="浏览时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.browseTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 列设置抽屉 -->
    <el-drawer
      v-model="columnSettingVisible"
      title="列设置"
      direction="rtl"
      size="300px"
    >
      <el-checkbox-group v-model="visibleColumns" class="column-list">
        <el-checkbox v-for="col in allColumns" :key="col.prop" :label="col.prop">
          {{ col.label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Setting } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dateUtils from '@/utils/dateUtils'
import * as XLSX from 'xlsx'

// 搜索表单
const searchForm = reactive({
  username: '',
  movieTitle: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])

// 列设置
const STORAGE_KEY = 'movieViewListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'username', label: '用户名' },
  { prop: 'movieTitle', label: '电影名称' },
  { prop: 'coverImage', label: '电影封面' },
  { prop: 'browseTime', label: '浏览时间' }
]

// 从localStorage获取保存的列设置，如果没有则使用默认值
const visibleColumns = ref(
  JSON.parse(localStorage.getItem(STORAGE_KEY)) || allColumns.map(col => col.prop)
)

// 监听列设置变化并保存到localStorage
watch(visibleColumns, (newVal) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal))
}, { deep: true })

const isColumnVisible = (prop) => {
  return visibleColumns.value.includes(prop)
}

// 格式化日期
const formatDate = (date) => {
  return dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss')
}

// 获取图片URL
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  const baseUrl = (process.env.VUE_APP_BASE_API || '/api').replace(/\/$/, '')
  return baseUrl + (path.startsWith('/') ? path : '/' + path)
}

// 获取浏览历史列表
const fetchViewList = async () => {
  loading.value = true
  try {
    const res = await request.get('/movie/history/page', {
      ...searchForm,
      currentPage: currentPage.value,
      pageSize: pageSize.value
    }, {
      showDefaultMsg: false
    })
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('获取浏览历史列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchViewList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  fetchViewList()
  ElMessage.success('刷新成功')
}

// 删除浏览记录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该浏览记录吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/movie/history/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchViewList()
  } catch (error) {
    if (error.type !== 'business') {
      console.error('删除浏览记录失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 条记录吗？`, '提示', {
      type: 'warning'
    })
    await request.delete(`/movie/history/batch?ids=${selectedRows.value.map(row => row.id).join(',')}`, {
      successMsg: '批量删除成功'
    })
    fetchViewList()
  } catch (error) {
    if (error.type !== 'business') {
      console.error('批量删除失败:', error)
    }
  }
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchViewList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchViewList()
}

// 导出功能
const handleExport = () => {
  try {
    loading.value = true
    
    // 获取当前可见列的配置
    const visibleColumnConfigs = allColumns.filter(col => isColumnVisible(col.prop))
    
    // 准备导出数据
    const exportData = tableData.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'browseTime') {
          row[col.label] = formatDate(item.browseTime)
        } else if (col.prop !== 'coverImage') {
          row[col.label] = item[col.prop]
        }
      })
      return row
    })

    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '浏览历史')

    // 导出文件
    XLSX.writeFile(workbook, `浏览历史_${dateUtils.format(new Date(), 'YYYY-MM-DD')}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchViewList()
})
</script>

<style scoped>
.view-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header .right {
  display: flex;
  gap: 10px;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.column-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}

.movie-cover {
  width: 90px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
  object-fit: cover;
}
</style> 