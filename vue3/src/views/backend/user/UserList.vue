<template>
  <div class="user-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select 
            v-model="searchForm.roleCode" 
            placeholder="请选择角色" 
            clearable
            style="width: 200px"
          >
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.code" />
          </el-select>
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
            <span class="title">用户列表</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="handleAdd">新增用户</el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('username')" prop="username" label="用户名" />
        <el-table-column v-if="isColumnVisible('name')" prop="name" label="姓名" />
        <el-table-column v-if="isColumnVisible('email')" prop="email" label="邮箱" />
        <el-table-column v-if="isColumnVisible('phone')" prop="phone" label="手机号" />
        <el-table-column v-if="isColumnVisible('roleCode')" prop="roleCode" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.roleCode === 'admin' ? 'danger' : 'success'">
              {{ roleList.find(role => role.code === row.roleCode)?.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('status')" prop="status" label="状态">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="(val) => handleStatusChange(row.id, val)" />
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" prop="createTime" label="创建时间">
          <template #default="{ row }">
            {{ formatTableDate(row) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination :current-page="currentPage" :page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 用户表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" placeholder="请选择角色" clearable>
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 在表格之前添加列设置抽屉 -->
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
import { formatDateTime, format } from '@/utils/dateUtils'
import * as XLSX from 'xlsx'

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

// 搜索表单
const searchForm = reactive({
  username: '',
  name: '',
  roleCode: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])
const roleList = ref([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const form = reactive({
  id: '',
  username: '',
  name: '',
  email: '',
  phone: '',
  roleCode: '',
  password: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '长度在 6 到 100 个字符', trigger: 'blur' }
  ]
}

const fetchRoleList = async () => {
  await request.get('/role/all', null, {
    onSuccess: (data) => {
      console.log("roleList data:", data)
      roleList.value = data
    }
  })
}

// 格式化日期时间
const formatTableDate = (row) => {
  if (!row.createTime) return '';
  return formatDateTime(row.createTime);
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/page', {
      currentPage: currentPage.value,
      size: pageSize.value,
      ...searchForm
    },{
      onSuccess:(res=>{
        tableData.value = res.records
        total.value = res.total
      })
    })

  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
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
  fetchUsers()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUsers()
}

// 新增用户
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
  form.id = ''
}

// 编辑用户
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该用户吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/user/delete/${row.id}`,{
      successMsg:"删除成功",
      onSuccess:(res=>{
   
    fetchUsers()
      })
    })

  } catch (error) {
    console.error('删除用户失败:', error)
  }
}

// 重置密码
const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm('确认重置该用户的密码吗？', '提示', {
      type: 'warning'
    })
    await request.get('/user/forget', {
      email: row.email,
      newPassword: '123456' // 默认密码
    })
    ElMessage.success('密码重置成功')
  } catch (error) {
    console.error('重置密码失败:', error)
  }
}

// 修改用户状态
const handleStatusChange = async (id, status) => {
  try {
    await request.put(`/user/status/${id}?status=${status}`)
  } catch (error) {
    console.error('修改状态失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.id) {
      // 编辑
      await request.put(`/user/${form.id}`, form)
      ElMessage.success('更新成功')
    } else {
      // 新增
      await request.post('/user/add', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchUsers()
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

// 添加刷新方法
const handleRefresh = () => {
  fetchUsers()
  ElMessage.success('刷新成功')
}

// 添加批量删除方法
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个用户吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/user/batch-delete', { ids })
    ElMessage.success('批量删除成功')
    fetchUsers()
  } catch (error) {
    console.error('批量删除失败:', error)
  }
}

// 修改列设置相关代码
const STORAGE_KEY = 'userListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'username', label: '用户名' },
  { prop: 'name', label: '姓名' },
  { prop: 'email', label: '邮箱' },
  { prop: 'phone', label: '手机号' },
  { prop: 'roleCode', label: '角色' },
  { prop: 'status', label: '状态' },
  { prop: 'createTime', label: '创建时间' }
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

// 修改导出方法，只导出可见列
const handleExport = () => {
  try {
    loading.value = true
    
    // 获取当前可见列的配置
    const visibleColumnConfigs = allColumns.filter(col => isColumnVisible(col.prop))
    
    // 准备导出数据
    const exportData = tableData.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'roleCode') {
          row[col.label] = roleList.value.find(role => role.code === item.roleCode)?.name || item.roleCode
        } else if (col.prop === 'status') {
          row[col.label] = item.status === 1 ? '启用' : '禁用'
        } else if (col.prop === 'createTime') {
          row[col.label] = formatTableDate(item)
        } else {
          row[col.label] = item[col.prop]
        }
      })
      return row
    })

    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '用户列表')

    // 导出文件
    XLSX.writeFile(workbook, `用户列表_${format(new Date())}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUsers()
  fetchRoleList()
})
</script>

<style scoped>
.user-list {
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

:deep(.el-select) {
  width: 200px;
}

.column-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}
</style>
