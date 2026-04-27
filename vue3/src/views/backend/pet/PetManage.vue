<template>
  <div class="pet-manage-container">
    <div class="pet-header">
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="名称">
            <el-input v-model="searchForm.name" placeholder="宠物名称" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-cascader
              v-model="searchForm.categoryId"
              :options="categoriesTree"
              :props="{
                checkStrictly: true,
                value: 'id',
                
                label: 'name',
                emitPath: false
              }"
              placeholder="请选择宠物分类"
              clearable
            />
          </el-form-item>
          <el-form-item label="领养状态">
            <el-select v-model="searchForm.adoptionStatus" placeholder="领养状态" clearable>
              <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="action-buttons">
        <el-button type="primary" @click="handleAdd">添加宠物</el-button>
        <el-button type="success" @click="handleRefresh" :loading="refreshLoading">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <!-- 数据加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 错误状态 -->
    <div v-else-if="loadError" class="error-container">
      <el-result
        icon="error"
        title="数据加载失败"
        sub-title="无法获取宠物信息，请检查网络连接或稍后重试"
      >
        <template #extra>
          <el-button type="primary" @click="handleRefresh">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <!-- 空数据状态 -->
    <div v-else-if="petList.length === 0" class="empty-container">
      <el-empty 
        description="暂无宠物信息" 
        :image-size="200"
      >
        <template #description>
          <p>系统中尚未添加任何宠物信息</p>
          <p class="empty-hint">您可以点击"添加宠物"按钮创建新的宠物记录</p>
        </template>
        <el-button type="primary" @click="handleAdd">添加宠物</el-button>
      </el-empty>
    </div>

    <!-- 数据展示 -->
    <el-table
      v-else
      :data="petList"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="120">
        <template #default="scope">
          <el-image 
            :src="getImageUrl(scope.row.images)" 
            :preview-src-list="[getImageUrl(scope.row.images)]"
            style="width: 80px; height: 80px"
            fit="cover"
            :preview-teleported="true"
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" width="120" />
      <el-table-column prop="categoryName" label="分类" width="120">
        <template #default="scope">
          <span>{{ scope.row.categoryName || '未分类' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="breed" label="品种" width="120" />
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.adoptionStatus)">
            {{ scope.row.adoptionStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="healthStatus" label="健康状况" width="150" />
      <el-table-column prop="description" label="描述" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="操作" width="400" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          <el-button type="info" size="small" @click="handleApplications(scope.row)">查看申请</el-button>
          <el-button type="success" size="small" @click="handleHealthRecord(scope.row)">健康记录</el-button>
          <el-button type="warning" size="small" @click="handleVaccination(scope.row)">疫苗记录</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" v-if="petList.length > 0">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 添加/编辑宠物对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑宠物' : '添加宠物'" 
      width="700px"
      :before-close="handleDialogClose"
    >
      <el-form :model="petForm" label-width="100px" :rules="petRules" ref="petFormRef">
        <el-form-item label="名称" prop="name">
          <el-input v-model="petForm.name" placeholder="请输入宠物名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-cascader
            v-model="petForm.categoryId"
            :options="categoriesTree"
            :props="{
              checkStrictly: true,
              value: 'id',
              label: 'name',
              emitPath: false
            }"
            placeholder="请选择宠物分类"
            filterable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model="petForm.breed" placeholder="请输入宠物品种" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="petForm.age" :min="0" :max="30" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="petForm.gender">
            <el-radio label="公">公</el-radio>
            <el-radio label="母">母</el-radio>
            <el-radio label="未知">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="领养状态" prop="adoptionStatus">
          <el-select v-model="petForm.adoptionStatus" placeholder="请选择领养状态" style="width: 100%">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="健康状况" prop="healthStatus">
          <el-input v-model="petForm.healthStatus" placeholder="请输入健康状况" />
        </el-form-item>
        <el-form-item label="图片" prop="images">
          <el-upload
            action="#"
            list-type="picture-card"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :on-error="handleUploadError"
            :on-change="handleChange"
            :file-list="fileList"
            :http-request="customUploadImage"
            :before-upload="beforeImageUpload"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="petForm.description" type="textarea" rows="4" placeholder="请输入宠物描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPet" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看申请对话框 -->
    <el-dialog v-model="applicationsDialogVisible" title="领养申请列表" width="900px">
      <el-table
        v-loading="applicationsLoading"
        :data="applicationsList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="申请用户" width="120" />
        <el-table-column prop="applyReason" label="申请理由" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="address" label="联系地址" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getApplicationStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" :formatter="formatDateTime" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === '已申请' || scope.row.status === '审核中'"
              type="success" 
              size="small" 
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.status === '已申请' || scope.row.status === '审核中'"
              type="danger" 
              size="small" 
              @click="handleReject(scope.row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 健康记录对话框 -->
    <el-dialog 
      v-model="healthRecordDialogVisible" 
      :title="`${currentPet?.name || ''} - 健康记录管理`" 
      width="800px"
    >
      <div class="dialog-toolbar">
        <el-button type="primary" @click="handleAddHealthRecord">添加记录</el-button>
      </div>
      
      <el-table
        v-loading="healthRecordLoading"
        :data="healthRecordList"
        border
        style="width: 100%"
      >
        <el-table-column prop="recordDate" label="记录日期" width="160" :formatter="formatDateTime" />
        <el-table-column prop="recordType" label="记录类型" width="100" />
        <el-table-column prop="hospital" label="医院" width="150" />
        <el-table-column prop="doctor" label="医生" width="100" />
        <el-table-column prop="weight" label="体重(kg)" width="100" />
        <el-table-column prop="temperature" label="体温(℃)" width="100" />
        <el-table-column prop="diagnosisResults" label="诊断结果" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditHealthRecord(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteHealthRecord(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container" v-if="healthRecordList.length > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="healthRecordTotal"
          :page-size="healthRecordPageSize"
          :current-page="healthRecordCurrentPage"
          @current-change="handleHealthRecordPageChange"
        />
      </div>
    </el-dialog>

    <!-- 添加/编辑健康记录对话框 -->
    <el-dialog 
      v-model="healthRecordFormDialogVisible" 
      :title="isEditHealthRecord ? '编辑健康记录' : '添加健康记录'" 
      width="850px"
    >
      <el-form :model="healthRecordForm" label-width="120px" :rules="healthRecordRules" ref="healthRecordFormRef">
        <el-form-item label="记录类型" prop="recordType">
          <el-select v-model="healthRecordForm.recordType" placeholder="请选择记录类型" style="width: 100%">
            <el-option label="体检" value="体检" />
            <el-option label="就诊" value="就诊" />
            <el-option label="手术" value="手术" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker
            v-model="healthRecordForm.recordDate"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="医院" prop="hospital">
          <el-input v-model="healthRecordForm.hospital" placeholder="请输入医院名称" />
        </el-form-item>
        <el-form-item label="医生" prop="doctor">
          <el-input v-model="healthRecordForm.doctor" placeholder="请输入医生姓名" />
        </el-form-item>
        
        <el-divider content-position="center">体检数据</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number  v-model="healthRecordForm.weight" :precision="2" :step="0.1" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="体温(℃)" prop="temperature">
              <el-input-number v-model="healthRecordForm.temperature" :precision="1" :step="0.1" :min="35" :max="43" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="心率(/分)" prop="heartRate">
              <el-input-number v-model="healthRecordForm.heartRate" :min="0" :max="300" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="血压" prop="bloodPressure">
              <el-input v-model="healthRecordForm.bloodPressure" placeholder="请输入血压" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="呼吸频率" prop="respiratoryRate">
              <el-input-number v-model="healthRecordForm.respiratoryRate" :min="0" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="center">身体状况</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="皮肤状况" prop="skinCondition">
              <el-input v-model="healthRecordForm.skinCondition" placeholder="请输入皮肤状况" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="毛发状况" prop="furCondition">
              <el-input v-model="healthRecordForm.furCondition" placeholder="请输入毛发状况" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="眼部状况" prop="eyeCondition">
              <el-input v-model="healthRecordForm.eyeCondition" placeholder="请输入眼部状况" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="耳部状况" prop="earCondition">
              <el-input v-model="healthRecordForm.earCondition" placeholder="请输入耳部状况" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="口腔状况" prop="oralCondition">
              <el-input v-model="healthRecordForm.oralCondition" placeholder="请输入口腔状况" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="肢体状况" prop="limbCondition">
              <el-input v-model="healthRecordForm.limbCondition" placeholder="请输入肢体状况" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="诊断结果" prop="diagnosisResults">
              <el-input v-model="healthRecordForm.diagnosisResults" placeholder="请输入诊断结果" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健康建议" prop="healthSuggestions">
              <el-input v-model="healthRecordForm.healthSuggestions" placeholder="请输入健康建议" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="备注" prop="notes">
              <el-input v-model="healthRecordForm.notes" type="textarea" rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="healthRecordFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHealthRecord" :loading="healthRecordSubmitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 疫苗记录对话框 -->
    <el-dialog 
      v-model="vaccinationDialogVisible" 
      :title="`${currentPet?.name || ''} - 疫苗接种记录`" 
      width="800px"
    >
      <div class="dialog-toolbar">
        <el-button type="primary" @click="handleAddVaccination">添加记录</el-button>
      </div>
      
      <el-table
        v-loading="vaccinationLoading"
        :data="vaccinationList"
        border
        style="width: 100%"
      >
        <el-table-column prop="vaccinationDate" label="接种日期" width="160" :formatter="formatDateTime" />
        <el-table-column prop="vaccineName" label="疫苗名称" width="160" />
        <el-table-column prop="hospital" label="接种医院" width="160" />
        <el-table-column prop="nextDate" label="下次接种日期" width="160" :formatter="formatDateTime" />
        <el-table-column prop="batchNumber" label="疫苗批号" width="120" />
        <el-table-column prop="notes" label="备注" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditVaccination(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteVaccination(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container" v-if="vaccinationList.length > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="vaccinationTotal"
          :page-size="vaccinationPageSize"
          :current-page="vaccinationCurrentPage"
          @current-change="handleVaccinationPageChange"
        />
      </div>
    </el-dialog>

    <!-- 添加/编辑疫苗记录对话框 -->
    <el-dialog 
      v-model="vaccinationFormDialogVisible" 
      :title="isEditVaccination ? '编辑疫苗记录' : '添加疫苗记录'" 
      width="650px"
    >
      <el-form :model="vaccinationForm" label-width="100px" :rules="vaccinationRules" ref="vaccinationFormRef">
        <el-form-item label="疫苗名称" prop="vaccineName">
          <el-input v-model="vaccinationForm.vaccineName" placeholder="请输入疫苗名称" />
        </el-form-item>
        <el-form-item label="接种日期" prop="vaccinationDate">
          <el-date-picker
            v-model="vaccinationForm.vaccinationDate"
            type="datetime"
            placeholder="选择接种日期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="下次接种" prop="nextDate">
          <el-date-picker
            v-model="vaccinationForm.nextDate"
            type="datetime"
            placeholder="选择下次接种日期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="接种医院" prop="hospital">
          <el-input v-model="vaccinationForm.hospital" placeholder="请输入接种医院" />
        </el-form-item>
        <el-form-item label="疫苗批号" prop="batchNumber">
          <el-input v-model="vaccinationForm.batchNumber" placeholder="请输入疫苗批号" />
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input v-model="vaccinationForm.notes" type="textarea" rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="vaccinationFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitVaccination" :loading="vaccinationSubmitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate, formatDateTime as formatDateTimeUtil } from '@/utils/dateUtils'
import request from '@/utils/request'
import { Plus, Refresh } from '@element-plus/icons-vue'

const baseAPI = process.env.VUE_APP_BASE_API || '/api'

// 宠物列表数据
const petList = ref([])
const loading = ref(false)
const loadError = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusOptions = ['可领养', '已领养']

// 分类列表和树形结构
const categoriesList = ref([])
const categoriesTree = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: '',
  adoptionStatus: ''
})

// 宠物表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const petFormRef = ref(null)
const fileList = ref([])
const uploadHeaders = computed(() => {
  return {
    token: localStorage.getItem('token')
  }
})

const petForm = reactive({
  id: null,
  name: '',
  categoryId: null,
  breed: '',
  age: 0,
  gender: '',
  healthStatus: '',
  adoptionStatus: '可领养',
  description: '',
  images: ''
})

// 宠物表单验证规则
const petRules = {
  name: [
    { required: true, message: '请输入宠物名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择宠物分类', trigger: 'change' }
  ],
  breed: [
    { required: true, message: '请输入宠物品种', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择宠物性别', trigger: 'change' }
  ],
  adoptionStatus: [
    { required: true, message: '请选择领养状态', trigger: 'change' }
  ]
}

// 申请列表数据
const applicationsDialogVisible = ref(false)
const applicationsLoading = ref(false)
const applicationsList = ref([])
const currentPetId = ref(null)

// 添加刷新功能
const refreshLoading = ref(false)

// 当前选中的宠物
const currentPet = ref(null)

// 健康记录相关
const healthRecordDialogVisible = ref(false)
const healthRecordLoading = ref(false)
const healthRecordList = ref([])
const healthRecordTotal = ref(0)
const healthRecordCurrentPage = ref(1)
const healthRecordPageSize = ref(10)

// 健康记录表单
const healthRecordFormDialogVisible = ref(false)
const isEditHealthRecord = ref(false)
const healthRecordFormRef = ref(null)
const healthRecordSubmitLoading = ref(false)

const healthRecordForm = reactive({
  id: null,
  petId: null,
  userId: null,
  recordType: '体检',
  recordDate: new Date(),
  hospital: '',
  doctor: '',
  weight: null,
  temperature: null,
  heartRate: null,
  bloodPressure: '',
  respiratoryRate: null,
  skinCondition: '',
  furCondition: '',
  eyeCondition: '',
  earCondition: '',
  oralCondition: '',
  limbCondition: '',
  diagnosisResults: '',
  healthSuggestions: '',
  notes: ''
})

// 健康记录表单验证规则
const healthRecordRules = {
  recordType: [
    { required: true, message: '请选择记录类型', trigger: 'change' }
  ],
  recordDate: [
    { required: true, message: '请选择记录日期', trigger: 'change' }
  ],
  hospital: [
    { required: true, message: '请输入医院名称', trigger: 'blur' }
  ],
  doctor: [
    { required: true, message: '请输入医生姓名', trigger: 'blur' }
  ]
}

// 疫苗记录相关
const vaccinationDialogVisible = ref(false)
const vaccinationLoading = ref(false)
const vaccinationList = ref([])
const vaccinationTotal = ref(0)
const vaccinationCurrentPage = ref(1)
const vaccinationPageSize = ref(10)

// 疫苗记录表单
const vaccinationFormDialogVisible = ref(false)
const isEditVaccination = ref(false)
const vaccinationFormRef = ref(null)
const vaccinationSubmitLoading = ref(false)

const vaccinationForm = reactive({
  id: null,
  petId: null,
  vaccineName: '',
  vaccinationDate: new Date(),
  nextDate: null,
  hospital: '',
  batchNumber: '',
  notes: ''
})

// 疫苗记录表单验证规则
const vaccinationRules = {
  vaccineName: [
    { required: true, message: '请输入疫苗名称', trigger: 'blur' }
  ],
  vaccinationDate: [
    { required: true, message: '请选择接种日期', trigger: 'change' }
  ],
  hospital: [
    { required: true, message: '请输入接种医院', trigger: 'blur' }
  ]
}

// 获取宠物列表
const fetchPets = async () => {
  loading.value = true
  loadError.value = false
  // 确保初始化为空数组而不是undefined
  petList.value = []
  
  try {
    await request.get('/pet/page', {
      name: searchForm.name,
      categoryId: searchForm.categoryId !== '' ? searchForm.categoryId : null,
      adoptionStatus: searchForm.adoptionStatus,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        // 确保响应数据存在且包含记录
        if (res && res.records) {
          petList.value = res.records
          total.value = res.total || 0
        } else {
          // 如果没有数据，设置为空数组
          petList.value = []
          total.value = 0
        }
      }
    })
  } catch (error) {
    console.error('获取宠物列表失败:', error)
    // 确保在出错时也将列表设置为空数组
    petList.value = []
    total.value = 0
    loadError.value = true
    ElMessage.error('获取宠物列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取分类列表和树形结构
const fetchCategories = async () => {
  try {
    await request.get('/pet-category/tree', {}, {
      onSuccess: (res) => {
        if (res) {
        categoriesTree.value = res.map(item => ({
          ...item,
            disabled: item.status === 0
          }))
            flattenCategories(res)
        }else{
          categoriesTree.value = []
          flattenCategories([])
        }
        
        // 同时保留平铺的分类列表，用于其他地方可能需要
      
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 将树形结构扁平化
const flattenCategories = (tree) => {
  categoriesList.value = []
  const flatten = (items) => {
    items.forEach(item => {
      categoriesList.value.push({
        id: item.id,
        name: item.name,
        // status: item.status,
        disabled: item.status === 0
      })
      if (item.children && item.children.length > 0) {
        flatten(item.children)
      }
    })
  }
  flatten(tree)
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPets()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchPets()
}

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page
  fetchPets()
}

// 添加宠物
const handleAdd = () => {
  isEdit.value = false
  resetPetForm()
  fileList.value = []
  dialogVisible.value = true
}

// 编辑宠物
const handleEdit = (row) => {
  isEdit.value = true
  resetPetForm()
  
  // 复制数据到表单
  Object.keys(petForm).forEach(key => {
    if (key in row) {
      petForm[key] = row[key]
    }
  })
  
  // 处理图片
  fileList.value = []
  if (row.images) {
    const images = row.images.split(',')
    fileList.value = images.map((imgUrl, index) => {
      return {
        name: `图片${index + 1}`,
        url: getImageUrl(imgUrl),
        response: { fileName: imgUrl }
      }
    })
  }
  
  dialogVisible.value = true
}

// 删除宠物
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该宠物吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/pet/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchPets()
        }
      })
    } catch (error) {
      console.error('删除宠物失败:', error)
    }
  }).catch(() => {})
}

// 查看申请
const handleApplications = async (row) => {
  applicationsLoading.value = true
  currentPetId.value = row.id
  
  try {
    await request.get('/adoption/page', {
      petId: row.id,
      currentPage: 1,
      size: 100
    }, {
      onSuccess: (res) => {
        applicationsList.value = res.records
      }
    })
  } catch (error) {
    console.error('获取申请列表失败:', error)
  } finally {
    applicationsLoading.value = false
    applicationsDialogVisible.value = true
  }
}

// 通过申请
const handleApprove = (row) => {
  ElMessageBox.confirm('确定要通过该申请吗? 通过后该宠物将被标记为已领养，其他申请将被自动拒绝。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/adoption/${row.id}/review`, null, {
        params: {
          status: '已通过'
        },
        successMsg: '申请已通过',
        onSuccess: () => {
          handleApplications({ id: currentPetId.value })
          fetchPets() // 同时刷新宠物列表，因为宠物状态会改变
        }
      })
    } catch (error) {
      console.error('审核申请失败:', error)
    }
  }).catch(() => {})
}

// 拒绝申请
const handleReject = (row) => {
  ElMessageBox.confirm('确定要拒绝该申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/adoption/${row.id}/review`, null, {
        params: {
          status: '已拒绝'
        },
        successMsg: '申请已拒绝',
        onSuccess: () => {
          handleApplications({ id: currentPetId.value })
          fetchPets() // 同时刷新宠物列表，申请都被拒绝后，宠物状态可能会改变
        }
      })
    } catch (error) {
      console.error('审核申请失败:', error)
    }
  }).catch(() => {})
}

// 重置宠物表单
const resetPetForm = () => {
  petForm.id = null
  petForm.name = ''
  petForm.categoryId = null
  petForm.breed = ''
  petForm.age = 0
  petForm.gender = ''
  petForm.healthStatus = ''
  petForm.adoptionStatus = '可领养'
  petForm.description = ''
  petForm.images = ''
}

// 提交宠物表单
const submitPet = () => {
  petFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    
    try {
      // 处理图片
      console.log('提交前的文件列表:', fileList.value);
      
      if (fileList.value.length > 0) {
        // 确保每个文件对象都有response.fileName属性
        const fileNames = fileList.value
          .filter(file => file.response && file.response.fileName)
          .map(file => file.response.fileName);
        
        console.log('过滤后的文件名列表:', fileNames);
        
        if (fileNames.length > 0) {
          petForm.images = fileNames.join(',');
          console.log('最终设置的images字段:', petForm.images);
        } else {
          // 如果没有找到有效的文件名，保留原始图片（编辑模式下）
          if (!isEdit.value) {
            petForm.images = '';
          }
          console.log('未找到有效文件名，images字段:', petForm.images);
        }
      } else {
        petForm.images = '';
        console.log('文件列表为空，images字段被清空');
      }
      
      // 输出完整的表单数据，用于调试
      console.log('提交的宠物表单数据:', JSON.stringify(petForm));
      
      if (isEdit.value) {
        // 编辑
        await request.put('/pet', petForm, {
          successMsg: '更新成功',
          onSuccess: (res) => {
            console.log('更新宠物成功，响应:', res);
            dialogVisible.value = false
            fetchPets()
          }
        })
      } else {
        // 添加
        await request.post('/pet', petForm, {
          successMsg: '添加成功',
          onSuccess: (res) => {
            console.log('添加宠物成功，响应:', res);
            dialogVisible.value = false
            fetchPets()
          }
        })
      }
    } catch (error) {
      console.error('保存宠物失败:', error)
      ElMessage.error('保存宠物失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 处理上传成功
const handleUploadSuccess = (response, file, fileList) => {
  console.log('上传成功回调:', response, file);
  ElMessage.success('上传成功');
}

// 处理上传失败
const handleUploadError = (error, file, fileList) => {
  console.error('上传失败:', error, file);
  ElMessage.error('上传失败');
}

// 处理移除上传文件
const handleUploadRemove = (file, uploadFileList) => {
  console.log('移除文件:', file, '当前文件列表:', uploadFileList);
  // 更新fileList引用
  fileList.value = uploadFileList;
}

// 监听文件列表变化
const handleChange = (file, uploadFileList) => {
  console.log('文件列表变化:', file, uploadFileList);
}

// 上传图片前的校验
const beforeImageUpload = (file) => {
  const isJPG = file.type === "image/jpeg";
  const isPNG = file.type === "image/png";
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    ElMessage.error("图片只能是 JPG 或 PNG 格式!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB!");
    return false;
  }
  return true;
};

// 自定义图片上传方法
const customUploadImage = async (options) => {
  const { file, onProgress, onSuccess, onError } = options;
  
  try {
    console.log('开始上传文件:', file.name);
    
    // 创建 FormData 对象
    const formData = new FormData();
    formData.append("file", file);

    // 模拟上传进度
    let percent = 0;
    const interval = setInterval(() => {
      percent += 10;
      if (percent <= 90) {
        onProgress({ percent });
      } else {
        clearInterval(interval);
      }
    }, 100);

    // 设置自定义上传选项
    const uploadOptions = {
      headers: {
        token: localStorage.getItem("token") || "",
      },
      // 不进行JSON处理
      transformRequest: [(data) => data],
      // 成功回调
      onSuccess: (data) => {
        clearInterval(interval);
        onProgress({ percent: 100 });
        
        console.log('服务器返回的文件数据:', data);
        
        if (data) {
          // 构造文件对象，确保响应格式与el-upload期望的一致
          const fileObj = {
            name: file.name,
            url: `${baseAPI}${data}`,
            response: { fileName: data }
          };
          
          console.log('构造的文件对象:', fileObj);
          
          // 通知上传组件上传成功
          onSuccess({ fileName: data });
          
          // 由于el-upload组件在使用自定义上传时可能不会自动更新fileList
          // 我们确保文件被添加到列表中
          const found = fileList.value.some(f => 
            f.response && f.response.fileName === data);
          
          if (!found) {
            fileList.value.push(fileObj);
            console.log('已手动添加文件到列表，当前列表:', fileList.value);
          }
        } else {
          onError(new Error('服务器返回的文件数据无效'));
        }
      },
      // 错误回调
      onError: (error) => {
        clearInterval(interval);
        console.error("图片上传错误:", error);
        onError(new Error(error.message || "上传失败"));
      },
    };

    // 发送上传请求
    await request.post("/file/upload/img", formData, uploadOptions);
  } catch (error) {
    onError(error);
    console.error("图片上传过程发生错误:", error);
  }
};

// 处理关闭对话框
const handleDialogClose = (done) => {
  petFormRef.value.resetFields()
  done()
}

// 获取图片URL
const getImageUrl = (images) => {
  // 使用数据URI作为默认图片，而不是引用外部图片文件
  const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3QgeD0iMiIgeT0iMiIgd2lkdGg9Ijc2IiBoZWlnaHQ9Ijc2IiBzdHlsZT0iZmlsbDojZWVlO3N0cm9rZTojYWFhO3N0cm9rZS13aWR0aDoycHg7Ii8+PHRleHQgeD0iNDAiIHk9IjQwIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBzdHlsZT0iZmlsbDojOTk5O2ZvbnQtZmFtaWx5OkFyaWFsLHNhbnMtc2VyaWY7Zm9udC1zaXplOjEycHg7Ij7ml6A8L3RleHQ+PC9zdmc+'
  
  if (!images) return defaultImage
  
  const imageList = images.split(',')
  if (imageList.length === 0) return defaultImage
  
  const firstImage = imageList[0].trim()
  if (firstImage.startsWith('http')) {
    return firstImage
  }
  return `${baseAPI}${firstImage}`
}

// 获取状态类型
const getStatusType = (status) => {
  if (status === '可领养') return 'success'
  if (status === '已领养') return 'info'
  return ''
}

// 获取申请状态类型
const getApplicationStatusType = (status) => {
  switch (status) {
    case '已申请':
      return 'info'
    case '审核中':
      return 'warning'
    case '已通过':
      return 'success'
    case '已拒绝':
      return 'danger'
    default:
      return 'info'
  }
}

// 格式化日期时间
const formatDateTime = (row, column, cellValue) => {
  if (!cellValue) return '';
  return formatDateTimeUtil(cellValue);
}

// 手动刷新数据
const handleRefresh = () => {
  refreshLoading.value = true
  fetchPets().finally(() => {
    refreshLoading.value = false
  })
}

// 处理健康记录
const handleHealthRecord = (row) => {
  currentPet.value = row
  healthRecordCurrentPage.value = 1
  fetchHealthRecords()
  healthRecordDialogVisible.value = true
}

// 获取健康记录列表
const fetchHealthRecords = async () => {
  if (!currentPet.value) return
  
  healthRecordLoading.value = true
  try {
    await request.get('/pet-health-record/page', {
      petId: currentPet.value.id,
      currentPage: healthRecordCurrentPage.value,
      size: healthRecordPageSize.value
    }, {
      onSuccess: (res) => {
        healthRecordList.value = res.records
        healthRecordTotal.value = res.total
      }
    })
  } catch (error) {
    console.error('获取健康记录失败:', error)
    ElMessage.error('获取健康记录失败')
  } finally {
    healthRecordLoading.value = false
  }
}

// 处理健康记录分页
const handleHealthRecordPageChange = (page) => {
  healthRecordCurrentPage.value = page
  fetchHealthRecords()
}

// 添加健康记录
const handleAddHealthRecord = () => {
  isEditHealthRecord.value = false
  resetHealthRecordForm()
  
  // 设置默认值
  healthRecordForm.petId = currentPet.value.id
  healthRecordForm.userId = JSON.parse(localStorage.getItem('userInfo')).id
  healthRecordForm.recordDate = new Date()
  
  healthRecordFormDialogVisible.value = true
}

// 编辑健康记录
const handleEditHealthRecord = (row) => {
  isEditHealthRecord.value = true
  resetHealthRecordForm()
  
  // 复制数据到表单
  Object.keys(healthRecordForm).forEach(key => {
    if (key in row) {
      healthRecordForm[key] = row[key]
    }
  })
  
  healthRecordFormDialogVisible.value = true
}

// 删除健康记录
const handleDeleteHealthRecord = (row) => {
  ElMessageBox.confirm('确定要删除该健康记录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/pet-health-record/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchHealthRecords()
        }
      })
    } catch (error) {
      console.error('删除健康记录失败:', error)
    }
  }).catch(() => {})
}

// 重置健康记录表单
const resetHealthRecordForm = () => {
  if (healthRecordFormRef.value) {
    healthRecordFormRef.value.resetFields()
  }
  
  healthRecordForm.id = null
  healthRecordForm.petId = null
  healthRecordForm.userId = null
  healthRecordForm.recordType = '体检'
  healthRecordForm.recordDate = new Date().toISOString()
  healthRecordForm.hospital = ''
  healthRecordForm.doctor = ''
  healthRecordForm.weight = null
  healthRecordForm.temperature = null
  healthRecordForm.heartRate = null
  healthRecordForm.bloodPressure = ''
  healthRecordForm.respiratoryRate = null
  healthRecordForm.skinCondition = ''
  healthRecordForm.furCondition = ''
  healthRecordForm.eyeCondition = ''
  healthRecordForm.earCondition = ''
  healthRecordForm.oralCondition = ''
  healthRecordForm.limbCondition = ''
  healthRecordForm.diagnosisResults = ''
  healthRecordForm.healthSuggestions = ''
  healthRecordForm.notes = ''
}

// 提交健康记录
const submitHealthRecord = () => {
  healthRecordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    healthRecordSubmitLoading.value = true
    try {
      healthRecordForm.createTime = null
      healthRecordForm.updateTime = null
      
      if (isEditHealthRecord.value) {
        // 编辑
        await request.put('/pet-health-record', healthRecordForm, {
          successMsg: '更新成功',
          onSuccess: () => {
            healthRecordFormDialogVisible.value = false
            fetchHealthRecords()
          }
        })
      } else {
        // 添加
        await request.post('/pet-health-record', healthRecordForm, {
          successMsg: '添加成功',
          onSuccess: () => {
            healthRecordFormDialogVisible.value = false
            fetchHealthRecords()
          }
        })
      }
    } catch (error) {
      console.error('保存健康记录失败:', error)
    } finally {
      healthRecordSubmitLoading.value = false
    }
  })
}

// 处理疫苗记录
const handleVaccination = (row) => {
  currentPet.value = row
  vaccinationCurrentPage.value = 1
  fetchVaccinations()
  vaccinationDialogVisible.value = true
}

// 获取疫苗记录列表
const fetchVaccinations = async () => {
  if (!currentPet.value) return
  
  vaccinationLoading.value = true
  try {
    await request.get('/pet-vaccination/page', {
      petId: currentPet.value.id,
      currentPage: vaccinationCurrentPage.value,
      size: vaccinationPageSize.value
    }, {
      onSuccess: (res) => {
        vaccinationList.value = res.records
        vaccinationTotal.value = res.total
      }
    })
  } catch (error) {
    console.error('获取疫苗记录失败:', error)
    ElMessage.error('获取疫苗记录失败')
  } finally {
    vaccinationLoading.value = false
  }
}

// 处理疫苗记录分页
const handleVaccinationPageChange = (page) => {
  vaccinationCurrentPage.value = page
  fetchVaccinations()
}

// 添加疫苗记录
const handleAddVaccination = () => {
  isEditVaccination.value = false
  resetVaccinationForm()
  
  // 设置默认值
  vaccinationForm.petId = currentPet.value.id
  vaccinationForm.vaccinationDate = new Date()
  
  vaccinationFormDialogVisible.value = true
}

// 编辑疫苗记录
const handleEditVaccination = (row) => {
  isEditVaccination.value = true
  resetVaccinationForm()
  
  // 复制数据到表单
  Object.keys(vaccinationForm).forEach(key => {
    if (key in row) {
      vaccinationForm[key] = row[key]
    }
  })
  
  vaccinationFormDialogVisible.value = true
}

// 删除疫苗记录
const handleDeleteVaccination = (row) => {
  ElMessageBox.confirm('确定要删除该疫苗接种记录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/pet-vaccination/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchVaccinations()
        }
      })
    } catch (error) {
      console.error('删除疫苗记录失败:', error)
    }
  }).catch(() => {})
}

// 重置疫苗记录表单
const resetVaccinationForm = () => {
  if (vaccinationFormRef.value) {
    vaccinationFormRef.value.resetFields()
  }
  
  vaccinationForm.id = null
  vaccinationForm.petId = null
  vaccinationForm.vaccineName = ''
  vaccinationForm.vaccinationDate = new Date().toISOString()
  vaccinationForm.nextDate = null
  vaccinationForm.hospital = ''
  vaccinationForm.batchNumber = ''
  vaccinationForm.notes = ''
}

// 提交疫苗记录
const submitVaccination = () => {
  vaccinationFormRef.value.validate(async (valid) => {
    vaccinationForm.createTime = null
    vaccinationForm.updateTime = null
    if (!valid) return
    
    vaccinationSubmitLoading.value = true
    try {
      if (isEditVaccination.value) {
        // 编辑
        await request.put('/pet-vaccination', vaccinationForm, {
          successMsg: '更新成功',
          onSuccess: () => {
            vaccinationFormDialogVisible.value = false
            fetchVaccinations()
          }
        })
      } else {
        // 添加
        await request.post('/pet-vaccination', vaccinationForm, {
          successMsg: '添加成功',
          onSuccess: () => {
            vaccinationFormDialogVisible.value = false
            fetchVaccinations()
          }
        })
      }
    } catch (error) {
      console.error('保存疫苗记录失败:', error)
    } finally {
      vaccinationSubmitLoading.value = false
    }
  })
}

onMounted(() => {
  fetchPets()
  fetchCategories()
})
</script>

<style scoped>
.pet-manage-container {
  padding: 20px;
}

.pet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.loading-container,
.error-container,
.empty-container {
  padding: 50px 0;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 20px;
}

.error-container {
  background-color: #fff0f0;
}

.empty-hint {
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
}

.no-data {
  color: #909399;
  font-style: italic;
}

.dialog-toolbar {
  margin-bottom: 15px;
  display: flex;
  justify-content: flex-end;
}

.el-divider {
  margin: 15px 0;
}
</style> 