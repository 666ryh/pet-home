<template>
  <div class="menu-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="菜单名称">
          <el-input v-model="searchForm.name" placeholder="请输入菜单名称" clearable />
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
            <span class="title">菜单列表</span>
            <el-button :icon="Refresh" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button type="primary" @click="handleAdd">新增菜单</el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" row-key="id" border default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="菜单名称" min-width="180" />
        <el-table-column prop="path" label="路由路径" min-width="180" />
        <el-table-column prop="component" label="组件路径" min-width="180" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.icon">
              <component :is="row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="sortNum" label="排序" width="80" />
        <el-table-column prop="hidden" label="显示" width="80">
          <template #default="{ row }">
            <el-tag :type="row.hidden ? 'info' : 'success'">
              {{ row.hidden ? '隐藏' : '显示' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.level < 2 && !row.component" 
              type="primary" 
              link 
              @click="handleAdd(row)"
            >新增子菜单</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 菜单表单对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select 
            v-model="form.pid" 
            :data="menuTree" 
            :props="{ 
              label: 'name', 
              value: 'id'
            }" 
            placeholder="请选择上级菜单" 
            clearable 
            check-strictly 
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="菜单图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请选择图标" readonly @click="openIconSelector">
            <template #prepend>
              <el-icon v-if="form.icon">
                <component :is="form.icon" />
              </el-icon>
            </template>
            <template #append>
              <el-button @click="openIconSelector">选择图标</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="排序号" prop="sortNum">
          <el-input-number v-model="form.sortNum" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="菜单描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入菜单描述" />
        </el-form-item>
        <el-form-item label="是否隐藏">
          <el-switch v-model="form.hidden" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加图标选择对话框 -->
    <el-dialog v-model="iconDialogVisible" title="选择图标" width="800px">
      <el-input
        v-model="iconSearch"
        placeholder="搜索图标"
        clearable
        style="margin-bottom: 16px;"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div class="icon-list-container">
        <div class="icon-list">
          <div
            v-for="icon in filteredIconList"
            :key="icon"
            class="icon-item"
            :class="{ active: form.icon === icon }"
            @click="selectIcon(icon)"
          >
            <el-icon>
              <component :is="icon" />
            </el-icon>
            <span>{{ icon }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as ElementPlusIcons from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  name: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const menuTree = ref([])

// 表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref(null)
const form = reactive({
  id: '',
  pid: null,
  name: '',
  path: '',
  component: '',
  icon: '',
  description: '',
  sortNum: 1,
  hidden: false
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请输入路由路径', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (form.pid === null && !value.startsWith('/')) {
          callback(new Error('顶级菜单路径必须以/开头'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],

  sortNum: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    { type: 'number', min: 0, max: 999, message: '排序号必须在0-999之间', trigger: 'blur' }
  ]
}

// 获取菜单树
const fetchMenuTree = async () => {
  loading.value = true
  try {
    const res = await request.get('/menu', {
      showDefaultMsg: false
    })
    // 处理菜单树数据，添加层级标识
    const processMenuTree = (menus, level = 0) => {
      return menus
        .filter(menu => !menu.component) // 只返回没有组件路径的菜单
        .map(menu => ({
          ...menu,
          level,
          children: menu.children && level < 1 ? processMenuTree(menu.children, level + 1) : [] // 只处理一级以下的子菜单
        }))
    }
    const processedData = processMenuTree(res)
    tableData.value = res.map(menu => ({  // 表格数据保持原样
      ...menu,
      level: 0,
      children: menu.children ? menu.children.map(child => ({
        ...child,
        level: 1,
        children: child.children ? child.children.map(grandChild => ({
          ...grandChild,
          level: 2
        })) : []
      })) : []
    }))
    
    // 构建菜单选择树（只包含可作为父级的菜单）
    menuTree.value = [{
      id: null,
      name: '顶级菜单',
      children: processedData,
      level: -1
    }]
  } catch (error) {
    console.error('获取菜单树失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  fetchMenuTree()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  fetchMenuTree()
  ElMessage.success('刷新成功')
}

// 新增菜单
const handleAdd = (row = null) => {
  dialogTitle.value = row ? '新增子菜单' : '新增菜单'
  form.pid = row ? row.id : null
  dialogVisible.value = true
}

// 编辑菜单
const handleEdit = (row) => {
  dialogTitle.value = '编辑菜单'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除菜单
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该菜单吗？删除后将无法恢复！', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await request.delete(`/menu/${row.id}`, {
      successMsg: '删除成功'
    })
    fetchMenuTree()
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('删除菜单失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 检查菜单层级
    if (form.pid) {
      const parentMenu = findMenuById(form.pid)
      if (parentMenu && parentMenu.level >= 2) {
        ElMessage.error('不能在二级菜单下创建子菜单')
        return
      }
    }

    if (form.id) {
      await request.put(`/menu/${form.id}`, form, {
        successMsg: '更新成功'
      })
    } else {
      await request.post('/menu', form, {
        successMsg: '创建成功'
      })
    }
    dialogVisible.value = false
    fetchMenuTree()
  } catch (error) {
    console.error('提交表单失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: '',
    pid: null,
    name: '',
    path: '',
    component: '',
    icon: '',
    description: '',
    sortNum: 1,
    hidden: false
  })
}

// 添加查找菜单的辅助函数
const findMenuById = (id) => {
  const find = (menus) => {
    for (const menu of menus) {
      if (menu.id === id) return menu
      if (menu.children) {
        const found = find(menu.children)
        if (found) return found
      }
    }
    return null
  }
  return find(tableData.value)
}

// 图标选择相关
const iconDialogVisible = ref(false)
const iconList = ref(Object.keys(ElementPlusIcons))
const iconSearch = ref('')
const filteredIconList = computed(() => {
  return iconList.value.filter(icon => 
    icon.toLowerCase().includes(iconSearch.value.toLowerCase())
  )
})

const openIconSelector = () => {
  iconDialogVisible.value = true
}

const selectIcon = (icon) => {
  form.icon = icon
  iconDialogVisible.value = false
}

onMounted(() => {
  fetchMenuTree()
})
</script>

<style scoped>
.menu-list {
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

:deep(.el-tree-select) {
  width: 100%;
}

.icon-list-container {
  height: 400px;
  overflow-y: auto;
}

.icon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  padding: 8px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.icon-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.icon-item.active {
  background-color: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.icon-item .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.icon-item span {
  font-size: 12px;
  text-align: center;
  word-break: break-all;
}

.icon-list-container::-webkit-scrollbar {
  width: 6px;
}

.icon-list-container::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.icon-list-container::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style> 