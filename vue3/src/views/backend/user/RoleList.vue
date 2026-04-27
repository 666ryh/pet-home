<template>
  <div class="role-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色编码">
          <el-input v-model="searchForm.code" placeholder="请输入角色编码" clearable />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.name" placeholder="请输入角色名称" clearable />
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
            <span class="title">角色列表</span>
            <el-button :icon="Refresh" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button :icon="Setting" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="handleAdd">新增角色</el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('code')" prop="code" label="角色编码" />
        <el-table-column v-if="isColumnVisible('name')" prop="name" label="角色名称" />
        <el-table-column v-if="isColumnVisible('description')" prop="description" label="角色描述" show-overflow-tooltip />
        <el-table-column v-if="isColumnVisible('createdTime')" prop="createdTime" label="创建时间">
          <template #default="{ row }">
            {{ formatDate(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" :disabled="row.code === 'USER'||row.code === 'ADMIN'" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" :disabled="row.code === 'USER'" link @click="handleAssignMenus(row)">分配菜单</el-button>
            <el-button type="danger" :disabled="row.code === 'ADMIN'||row.code === 'USER'" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          :model-value="currentPage"
          @update:model-value="currentPage = $event"
          :page-size="pageSize"
          @update:page-size="pageSize = $event"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 角色表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分配菜单对话框 -->
    <el-dialog
      title="分配菜单"
      v-model="menuDialogVisible"
      width="400px"
      :close-on-click-modal="false"
      @close="resetMenuDialog"
    >
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'name' }"
        :default-checked-keys="selectedMenuIds"
        check-strictly
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="menuDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitMenuAssignment">确定</el-button>
        </span>
      </template>
    </el-dialog>

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
  code: '',
  name: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])
const menuTree = ref([])
const selectedMenuIds = ref([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)
const form = reactive({
  id: '',
  code: '',
  name: '',
  description: ''
})

// 菜单对话框
const menuDialogVisible = ref(false)
const menuTreeRef = ref(null)
const currentRole = ref(null)

// 表单验证规则
const rules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date) => {
  return dateUtils.format(date, 'YYYY-MM-DD HH:mm:ss')
}

// 获取角色列表
const fetchRoles = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      code: searchForm.code,
      name: searchForm.name
    }
    const res = await request.get('/role/page', params)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 获取菜单树
const fetchMenuTree = async () => {
  try {
    const res = await request.get('/menu')
    menuTree.value = res
  } catch (error) {
    console.error('获取菜单树失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRoles()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRoles()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRoles()
}

// 新增角色
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
  form.id = ''
}

// 编辑角色
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除角色
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该角色吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/role/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchRoles()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除角色失败:', error)
    }
  }
}

// 分配菜单
const handleAssignMenus = async (row) => {
  currentRole.value = row
  menuDialogVisible.value = true
  selectedMenuIds.value = [] // 先清空之前的选择
  try {
    const res = await request.get(`/role/${row.id}/menuIds`, {
      showDefaultMsg: false
    })
    selectedMenuIds.value = res
  } catch (error) {
    console.error('获取角色菜单失败:', error)
  }
}

// 提交菜单分配
const submitMenuAssignment = async () => {
  if (!currentRole.value || !menuTreeRef.value) return
  try {
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
    // 将半选中的节点也算入选中节点中
    const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]
    await request.post(`/role/${currentRole.value.id}/menus`, allCheckedKeys, {
      successMsg: '菜单分配成功'
    })
    menuDialogVisible.value = false
  } catch (error) {
    console.error('分配菜单失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/role/${form.id}`, form, {
        successMsg: '更新成功'
      })
    } else {
      await request.post('/role', form, {
        successMsg: '创建成功'
      })
    }
    dialogVisible.value = false
    fetchRoles()
  } catch (error) {
    console.error('提交表单失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.keys(form).forEach(key => {
    form[key] = ''
  })
}

// 刷新
const handleRefresh = () => {
  fetchRoles()
  ElMessage.success('刷新成功')
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个角色吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/role/batch-delete', { ids }, {
      successMsg: '批量删除成功'
    })
    fetchRoles()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 导出
const handleExport = () => {
  try {
    loading.value = true
    
    const visibleColumnConfigs = allColumns.filter(col => isColumnVisible(col.prop))
    
    const exportData = tableData.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'createdTime') {
          row[col.label] = formatDate(item.createdTime)
        } else {
          row[col.label] = item[col.prop]
        }
      })
      return row
    })

    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '角色列表')
    XLSX.writeFile(workbook, `角色列表_${dateUtils.format(new Date(), 'YYYY-MM-DD')}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 列设置
const STORAGE_KEY = 'roleListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'code', label: '角色编码' },
  { prop: 'name', label: '角色名称' },
  { prop: 'description', label: '角色描述' },
  { prop: 'createdTime', label: '创建时间' }
]

const visibleColumns = ref(
  JSON.parse(localStorage.getItem(STORAGE_KEY)) || allColumns.map(col => col.prop)
)

watch(visibleColumns, (newVal) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal))
}, { deep: true })

const isColumnVisible = (prop) => {
  return visibleColumns.value.includes(prop)
}

// 重置菜单分配对话框
const resetMenuDialog = () => {
  currentRole.value = null
  selectedMenuIds.value = []
  if (menuTreeRef.value) {
    // 手动清空树选择状态
    menuTreeRef.value.setCheckedKeys([])
  }
}

onMounted(() => {
  fetchRoles()
  fetchMenuTree()
})
</script>

<style scoped>
.role-list {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

.column-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}
</style> 