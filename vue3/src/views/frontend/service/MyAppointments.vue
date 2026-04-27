<template>
  <div class="appointments-page">
    <!-- 页面装饰 -->
    <div class="page-decoration">
      <div class="bubble" v-for="i in 4" :key="`bubble-${i}`"></div>
      <div class="paw" v-for="i in 4" :key="`paw-${i}`"></div>
    </div>

    <div class="appointments-content">
      <div class="breadcrumb">
        <div class="back-button" @click="goToServiceList">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回服务列表</span>
        </div>
      </div>

      <div class="page-header">
        <h1>我的服务预约</h1>
        <p>在这里管理您的所有服务预约记录</p>
      </div>

      <el-card class="appointments-card" shadow="hover" v-loading="loading">
        <div class="filter-section">
          <el-radio-group v-model="statusFilter" @change="filterAppointments">
            <el-radio-button label="">全部预约</el-radio-button>
            <el-radio-button label="已预约">待确认</el-radio-button>
            <el-radio-button label="已确认">已确认</el-radio-button>
            <el-radio-button label="已完成">已完成</el-radio-button>
            <el-radio-button label="已取消">已取消</el-radio-button>
          </el-radio-group>

          <el-button type="primary" class="new-appointment" icon="Plus" @click="goToServiceList">预约新服务</el-button>
        </div>

        <div class="appointment-list">
          <el-empty
            v-if="filteredAppointments.length === 0"
            description="暂无预约记录"
            :image-size="200"
          >
            <template #description>
              <p>您目前没有任何预约记录</p>
            </template>
            <el-button type="primary" @click="goToServiceList">去浏览服务</el-button>
          </el-empty>

          <div v-else class="appointment-grid">
            <div
              v-for="appointment in filteredAppointments"
              :key="appointment.id"
              class="appointment-card"
            >
              <div class="appointment-badge" :class="getStatusClass(appointment.status)">
                {{ appointment.status }}
              </div>
              
              <div class="appointment-header">
                <h3>{{ appointment.serviceName }}</h3>
                <div class="appointment-time">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(appointment.appointmentTime) }}
                </div>
              </div>

              <div class="appointment-details">
                <div class="detail-item">
                  <div class="label">宠物名称</div>
                  <div class="value">{{ appointment.petName }}</div>
                </div>
                
                <div class="detail-item">
                  <div class="label">联系电话</div>
                  <div class="value">{{ appointment.contactPhone }}</div>
                </div>
                
                <div class="detail-item">
                  <div class="label">预约编号</div>
                  <div class="value appointment-id">{{ appointment.id }}</div>
                </div>
                
                <div class="detail-item" v-if="appointment.requirements">
                  <div class="label">特殊要求</div>
                  <div class="value requirements">{{ appointment.requirements }}</div>
                </div>
              </div>

              <div class="appointment-footer">
                <div class="created-at">
                  <small>创建于: {{ formatDate(appointment.createTime) }}</small>
                </div>
                
                <div class="appointment-actions">
                  <el-button
                    v-if="appointment.status === '已预约'"
                    type="danger"
                    size="small"
                    @click="cancelAppointment(appointment)"
                  >
                    取消预约
                  </el-button>
                  <el-button
                    type="primary"
                    size="small"
                    @click="viewServiceDetail(appointment.serviceId)"
                  >
                    查看服务
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { ArrowLeft, Calendar, Clock, Phone, Plus } from "@element-plus/icons-vue";
import { useUserStore } from "@/store/user";
import request from "@/utils/request";

const router = useRouter();
const userStore = useUserStore();

// 数据定义
const loading = ref(false);
const appointments = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const statusFilter = ref("");

// 获取当前用户ID
const userId = computed(() => {
  return userStore.userInfo ? userStore.userInfo.id : null;
});

// 根据状态过滤预约
const filteredAppointments = computed(() => {
  if (!statusFilter.value) {
    return appointments.value;
  }
  return appointments.value.filter(
    (item) => item.status === statusFilter.value
  );
});

// 获取预约列表
const fetchAppointments = async () => {
  if (!userId.value) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }

  loading.value = true;
  try {
    const params = {
      userId: userId.value,
      currentPage: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value ? statusFilter.value : null,
    };


    await request.get("/service/appointment/user", params, {
      onSuccess: (res) => {
        appointments.value = res.records||[];
        total.value = res.total||0;
      },
    });
  } catch (error) {
    console.error("获取预约列表失败:", error);
  } finally {
    loading.value = false;
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return "未知时间";

  try {
    // 处理可能的格式问题，确保日期字符串可以被正确解析
    let date;
    if (dateString.includes('T') && dateString.includes('Z')) {
      // 处理ISO格式的日期字符串
      date = new Date(dateString);
    } else if (dateString.includes('T')) {
      // 处理不带时区的ISO格式
      date = new Date(dateString);
    } else {
      // 处理普通格式的日期字符串
      date = new Date(dateString.replace(' ', 'T'));
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hour = String(date.getHours()).padStart(2, "0");
    const minute = String(date.getMinutes()).padStart(2, "0");

    return `${year}-${month}-${day} ${hour}:${minute}`;
  } catch (error) {
    console.error("日期格式化错误:", error);
    return dateString || "未知时间";
  }
};

// 获取状态对应的样式类型
const getStatusType = (status) => {
  switch (status) {
    case "已预约":
      return "warning";
    case "已确认":
      return "primary";
    case "已完成":
      return "success";
    case "已取消":
      return "info";
    default:
      return "";
  }
};

// 获取状态对应的CSS类
const getStatusClass = (status) => {
  switch (status) {
    case "已预约":
      return "status-pending";
    case "已确认":
      return "status-confirmed";
    case "已完成":
      return "status-completed";
    case "已取消":
      return "status-cancelled";
    default:
      return "";
  }
};

// 处理状态过滤
const filterAppointments = () => {
  currentPage.value = 1;
  fetchAppointments();
};

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size;
  fetchAppointments();
};

// 处理当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchAppointments();
};

// 取消预约
const cancelAppointment = (appointment) => {
  ElMessageBox.confirm("确定要取消该预约吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await request.put(
          `/service/appointment/${appointment.id}/cancel`,
          {
            userId: userId.value,
          },
          {
            successMsg: "预约已取消",
            onSuccess: () => {
              fetchAppointments();
            },
          }
        );
      } catch (error) {
        console.error("取消预约失败:", error);
      }
    })
    .catch(() => {});
};

// 查看服务详情
const viewServiceDetail = (serviceId) => {
  router.push({ name: "ServiceDetail", params: { id: serviceId } });
};

// 前往服务列表
const goToServiceList = () => {
  router.push({ name: "ServiceList" });
};

// 页面加载时检查登录状态并获取数据
onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录");
    router.push({
      path: "/login",
      query: { redirect: router.currentRoute.value.fullPath },
    });
    return;
  }

  fetchAppointments();
});
</script>

<style lang="scss" scoped>
.appointments-page {
  position: relative;
  min-height: calc(100vh - 60px);
  padding: 40px 20px;
  background-color: #fdf7f2;
  overflow: hidden;

  .page-decoration {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    z-index: 0;
    pointer-events: none;

    .bubble {
      position: absolute;
      border-radius: 50%;
      background-color: rgba(255, 230, 204, 0.6);
      animation: float 10s infinite ease-in-out;

      &:nth-child(1) {
        width: 120px;
        height: 120px;
        top: 10%;
        left: 5%;
        animation-delay: 0s;
      }

      &:nth-child(2) {
        width: 80px;
        height: 80px;
        top: 30%;
        right: 10%;
        animation-delay: 1s;
      }

      &:nth-child(3) {
        width: 150px;
        height: 150px;
        bottom: 15%;
        right: 5%;
        animation-delay: 2s;
      }

      &:nth-child(4) {
        width: 100px;
        height: 100px;
        bottom: 20%;
        left: 15%;
        animation-delay: 3s;
      }
    }

    .paw {
      position: absolute;
      width: 30px;
      height: 30px;
      background-image: url('@/assets/宠物手掌.svg');
      background-size: contain;
      background-repeat: no-repeat;
      opacity: 0.3;
      transform: rotate(var(--rotation));

      &:nth-child(5) {
        top: 20%;
        left: 20%;
        --rotation: 30deg;
      }

      &:nth-child(6) {
        top: 40%;
        right: 25%;
        --rotation: -20deg;
      }

      &:nth-child(7) {
        bottom: 30%;
        right: 35%;
        --rotation: 45deg;
      }

      &:nth-child(8) {
        bottom: 10%;
        left: 40%;
        --rotation: -45deg;
      }
    }
  }

  .appointments-content {
    position: relative;
    z-index: 1;
    max-width: 1200px;
    margin: 0 auto;

    .breadcrumb {
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      font-size: 14px;
      color: #666;

      .back-button {
        display: inline-flex;
        align-items: center;
        margin-right: 10px;
        color: #FFA500;
        cursor: pointer;
        transition: color 0.3s;

        &:hover {
          color: #FF8C00;
        }

        i {
          margin-right: 5px;
        }
      }
    }

    .page-header {
      text-align: center;
      margin-bottom: 30px;

      h1 {
        font-size: 32px;
        color: #FF8C00;
        margin-bottom: 10px;
        font-weight: 600;
      }

      p {
        font-size: 16px;
        color: #666;
      }
    }

    .appointments-card {
      background-color: white;
      border-radius: 12px;
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      padding: 24px;
      margin-bottom: 30px;

      .filter-section {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        flex-wrap: wrap;
        gap: 15px;

        .el-radio-group {
          .el-radio-button {
            margin-bottom: 5px;
          }
        }

        .new-appointment {
          background-color: #FF8C00;
          border-color: #FF8C00;
          
          &:hover, &:focus {
            background-color: #FF6B00;
            border-color: #FF6B00;
          }
        }
      }

      .appointment-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 20px;
        margin-bottom: 20px;
      }

      .appointment-card {
        position: relative;
        background-color: white;
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        padding: 20px;
        transition: transform 0.3s, box-shadow 0.3s;
        overflow: hidden;

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
        }

        .appointment-badge {
          position: absolute;
          top: 0;
          right: 0;
          padding: 6px 12px;
          font-size: 12px;
          font-weight: 500;
          color: white;
          border-radius: 0 12px 0 12px;

          &.status-pending {
            background-color: #E6A23C;
          }

          &.status-confirmed {
            background-color: #409EFF;
          }

          &.status-completed {
            background-color: #67C23A;
          }

          &.status-cancelled {
            background-color: #909399;
          }
        }

        .appointment-header {
          margin-bottom: 15px;
          padding-right: 70px; // Space for badge

          h3 {
            font-size: 18px;
            color: #333;
            margin: 0 0 8px 0;
            font-weight: 600;
            line-height: 1.4;
          }

          .appointment-time {
            display: flex;
            align-items: center;
            font-size: 14px;
            color: #FF8C00;

            .el-icon {
              margin-right: 5px;
            }
          }
        }

        .appointment-details {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 12px;
          margin-bottom: 20px;

          .detail-item {
            .label {
              font-size: 12px;
              color: #909399;
              margin-bottom: 4px;
            }

            .value {
              font-size: 14px;
              color: #333;
              word-break: break-word;
            }

            &:nth-child(3), &:nth-child(4) {
              grid-column: 1 / -1;
            }
          }
        }

        .appointment-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          border-top: 1px dashed #eee;
          padding-top: 15px;
          margin-top: 5px;

          .created-at {
            color: #909399;
            font-size: 12px;
          }

          .appointment-actions {
            display: flex;
            gap: 8px;
          }
        }
      }
    }

    .pagination-container {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }
}

// 动画效果
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

// 响应式调整
@media (max-width: 768px) {
  .appointments-page {
    padding: 20px 16px;

    .appointments-content {
      .page-header {
        h1 {
          font-size: 26px;
        }
      }

      .appointments-card {
        padding: 16px;

        .filter-section {
          flex-direction: column;
          align-items: flex-start;

          .el-radio-group {
            width: 100%;
            overflow-x: auto;
            white-space: nowrap;
            padding-bottom: 10px;
          }

          .new-appointment {
            align-self: flex-end;
          }
        }

        .appointment-grid {
          grid-template-columns: 1fr;
        }
      }
    }
  }
}

// 清空数据样式
:deep(.el-empty) {
  padding: 40px 0;
  
  .el-empty__image {
    width: 120px;
    height: 120px;
  }
  
  .el-empty__description {
    margin-top: 20px;
    
    p {
      color: #909399;
      margin-bottom: 20px;
    }
  }
  
  .el-button {
    background-color: #FF8C00;
    border-color: #FF8C00;
    
    &:hover, &:focus {
      background-color: #FF6B00;
      border-color: #FF6B00;
    }
  }
}

// 按钮样式
:deep(.el-button--primary) {
  &:not(.new-appointment) {
    --el-button-bg-color: #FF8C00;
    --el-button-border-color: #FF8C00;
    --el-button-hover-bg-color: #FF6B00;
    --el-button-hover-border-color: #FF6B00;
    --el-button-active-bg-color: #E07800;
    --el-button-active-border-color: #E07800;
  }
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #FF8C00;
  border-color: #FF8C00;
  box-shadow: -1px 0 0 0 #FF8C00;
}

:deep(.el-radio-button__inner:hover) {
  color: #FF8C00;
}

:deep(.el-pagination) {
  .el-pagination__sizes, 
  .btn-prev, 
  .btn-next, 
  .el-pager li {
    &.active, &:hover {
      color: #FF8C00;
    }
  }
  
  .el-pager li.is-active {
    background-color: #FF8C00;
    color: white;
  }
}
</style> 