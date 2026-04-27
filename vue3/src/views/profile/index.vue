<template>
  <div class="profile-page">
    <!-- 页面装饰 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>

    <div class="profile-content">
      <div class="page-header">
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">管理您的个人信息和账户设置</p>
      </div>

      <el-card class="profile-card" shadow="never">
        <el-tabs v-model="activeTab">
          <!-- 基本信息 Tab -->
          <el-tab-pane label="基本信息" name="basic">
            <div class="profile-info">
              <div class="avatar-container">
                <el-avatar :size="120" :src="avatarUrl" @error="() => false" />
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :auto-upload="true"
                  :show-file-list="false"
                  :http-request="customUploadAvatar"
                  :before-upload="beforeAvatarUpload"
                >
                  <el-button size="small" type="primary" class="upload-btn">更换头像</el-button>
                </el-upload>
              </div>

              <div class="info-form">
                <el-form
                  ref="userFormRef"
                  :model="userForm"
                  :rules="rules"
                  label-width="100px"
                  status-icon
                >
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="userForm.username" disabled>
                      <template #prefix>
                        <el-icon><User /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="userForm.name">
                      <template #prefix>
                        <el-icon><UserFilled /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item label="性别" prop="sex">
                    <el-radio-group v-model="userForm.sex">
                      <el-radio label="男">男</el-radio>
                      <el-radio label="女">女</el-radio>
                    </el-radio-group>
                  </el-form-item>

                  <el-form-item label="电子邮箱" prop="email">
                    <el-input v-model="userForm.email">
                      <template #prefix>
                        <el-icon><Message /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item label="手机号码" prop="phone">
                    <el-input v-model="userForm.phone">
                      <template #prefix>
                        <el-icon><Phone /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" class="submit-btn" @click="submitUserInfo">
                      <el-icon><Check /></el-icon>
                      保存修改
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </div>
          </el-tab-pane>

          <!-- 修改密码 Tab -->
          <el-tab-pane label="修改密码" name="password">
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="120px"
              class="password-form"
              status-icon
            >
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  show-password
                  placeholder="请输入旧密码"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  show-password
                  placeholder="请输入新密码"
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  show-password
                  placeholder="请再次输入新密码"
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" class="submit-btn" @click="submitPassword">
                  <el-icon><Check /></el-icon>
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/store/user";
import request from "@/utils/request";
import { 
  User, 
  UserFilled, 
  Message, 
  Phone, 
  Lock, 
  Key, 
  Check,
  ArrowUp
} from '@element-plus/icons-vue';

const baseAPI = process.env.VUE_APP_BASE_API || "/api";
const userStore = useUserStore();
const activeTab = ref("basic");

// 表单引用
const userFormRef = ref(null);
const passwordFormRef = ref(null);

// 用户表单数据
const userForm = reactive({
  id: "",
  username: "",
  name: "",
  email: "",
  phone: "",
  sex: "",
  avatar: "",
});

// 头像地址
const avatarUrl = computed(() => {
  return baseAPI + userForm.avatar;
});

// 密码表单数据
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 表单校验规则
const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: ["blur", "change"],
    },
  ],
  phone: [
    { required: false, trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号码",
      trigger: ["blur", "change"],
    },
  ],
};

// 密码表单校验规则
const passwordRules = {
  oldPassword: [
    { required: true, message: "请输入旧密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: ["blur", "change"],
    },
  ],
};

// 获取用户信息
const getUserInfo = async () => {
  try {
    // 如果用户已登录，从 store 中获取用户信息
    if (userStore.isLoggedIn) {
      // 从后端重新获取最新的用户信息
      const response = await request.get("/user/current", null, {
        showDefaultMsg: false,
      });

      // 确保返回数据存在
      if (response) {
        // 更新store中的用户信息
        userStore.updateUserInfo(response);

        // 直接更新表单数据
        userForm.id = response.id || "";
        userForm.username = response.username || "";
        userForm.name = response.name || "";
        userForm.email = response.email || "";
        userForm.phone = response.phone || "";
        userForm.sex = response.sex || "男";
        userForm.avatar = response.avatar || "";

        console.log("用户信息加载成功:", userForm);
      }
    }
  } catch (error) {
    console.error("获取用户信息失败", error);
    ElMessage.error("获取用户信息失败");
  }
};

// 上传头像前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === "image/jpeg";
  const isPNG = file.type === "image/png";
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    ElMessage.error("头像只能是 JPG 或 PNG 格式!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("头像大小不能超过 2MB!");
    return false;
  }
  return true;
};

// 自定义头像上传方法
const customUploadAvatar = async (options) => {
  try {
    const { file } = options;

    // 创建 FormData 对象
    const formData = new FormData();
    formData.append("file", file);

    // 设置自定义上传选项
    const uploadOptions = {
      headers: {
        token: localStorage.getItem("token") || "",
      },
      // 不进行JSON处理
      transformRequest: [(data) => data],
      // 自定义成功消息
      successMsg: "头像上传成功",
      // 自定义错误消息
      errorMsg: "头像上传失败",
      // 成功回调
      onSuccess: async (data) => {
        // 更新用户头像
        userForm.avatar = data;

        // 保存到后端
        await updateUserAvatar(data);

        // 通知上传成功
        options.onSuccess({ data });
      },
      // 错误回调
      onError: (error) => {
        console.error("头像上传错误:", error);
        options.onError(new Error(error.message || "上传失败"));
      },
    };

    // 发送上传请求
    await request.post("/file/upload/img", formData, uploadOptions);
  } catch (error) {
    options.onError(error);
    console.error("头像上传过程发生错误:", error);
  }
};

// 更新用户头像信息
const updateUserAvatar = async (avatarPath) => {
  try {
    await request.put(
      `/user/${userForm.id}`,
      { avatar: avatarPath },
      {
        showDefaultMsg: false,
        onSuccess: (data) => {
          // 更新本地用户信息
          const updatedUserInfo = { ...userStore.userInfo, avatar: avatarPath };
          userStore.updateUserInfo(updatedUserInfo);
        },
        onError: (error) => {
          console.error("头像信息保存失败", error);
          ElMessage.error("头像信息保存失败");
        },
      }
    );
  } catch (error) {
    console.error("头像信息保存失败", error);
    ElMessage.error("头像信息保存失败");
    throw error;
  }
};

// 提交用户信息更新
const submitUserInfo = async () => {
  if (!userFormRef.value) return;

  try {
    // 表单验证
    await userFormRef.value.validate();

    await request.put(
      `/user/${userForm.id}`,
      {
        name: userForm.name,
        email: userForm.email,
        phone: userForm.phone,
        sex: userForm.sex,
      },
      {
        showDefaultMsg: false,
        successMsg: "个人信息更新成功!",
        onSuccess: (data) => {
          // 更新本地用户信息
          const updatedUserInfo = {
            ...userStore.userInfo,
            name: userForm.name,
            email: userForm.email,
            phone: userForm.phone,
            sex: userForm.sex,
          };
          userStore.updateUserInfo(updatedUserInfo);
        },
        onError: (error) => {
          console.error("用户信息更新失败", error);
          ElMessage.error("用户信息更新失败");
        },
      }
    );

  } catch (error) {
    console.error("保存个人信息失败", error);
    ElMessage.error("保存个人信息失败");
  }
};

// 提交密码修改
const submitPassword = async () => {
  if (!passwordFormRef.value) return;

  try {
    // 表单验证
    await passwordFormRef.value.validate();

    await request.put(
      `/user/password/${userForm.id}`,
      {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      },
      {
        showDefaultMsg: false,

        onSuccess: (data) => {
          // 清空表单
          passwordForm.oldPassword = "";
          passwordForm.newPassword = "";
          passwordForm.confirmPassword = "";

          // 提示用户重新登录
          ElMessageBox.confirm("密码已修改，需要重新登录", "提示", {
            confirmButtonText: "重新登录",
            cancelButtonText: "取消",
            type: "warning",
          }).then(() => {
            // 清除用户信息并跳转到登录页
            userStore.clearUserInfo();
            window.location.href = "/login";
          });
        },
        onError: (error) => {
          console.error("密码信息保存失败", error);
          ElMessage.error("密码信息保存失败");
        },
      }
    );
  } catch (error) {
    console.error("密码修改失败", error);
    ElMessage.error(error.message || "密码修改失败");
  }
};

// 监听用户表单数据变化
watch(
  userForm,
  (newVal) => {
    console.log("用户表单数据变化:", newVal);
  },
  { deep: true }
);

// 组件挂载时获取用户信息
onMounted(() => {
  getUserInfo();
});
</script>

<style lang="scss" scoped>
.profile-page {
  position: relative;
  min-height: 100vh;
  background-color: #FFF9E6;
  padding: 30px 20px 60px;
  overflow: hidden;
}

.page-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  
  .decoration-bubble {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
    opacity: 0.05;
    
    &.bubble-1 {
      width: 300px;
      height: 300px;
      top: -150px;
      left: -100px;
      animation: float 15s infinite ease-in-out;
    }
    
    &.bubble-2 {
      width: 200px;
      height: 200px;
      bottom: 10%;
      right: -50px;
      animation: float 18s infinite ease-in-out;
    }
    
    &.bubble-3 {
      width: 150px;
      height: 150px;
      top: 40%;
      right: 10%;
      animation: float 12s infinite ease-in-out;
    }
  }
  
  .decoration-paw {
    position: absolute;
    font-size: 40px;
    opacity: 0.1;
    
    &.paw-1 {
      top: 20%;
      left: 5%;
      animation: float 15s infinite ease-in-out;
    }
    
    &.paw-2 {
      bottom: 10%;
      right: 10%;
      animation: float 18s infinite ease-in-out reverse;
    }
  }
}

.profile-content {
  position: relative;
  z-index: 1;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  
  .page-title {
    margin: 0;
    font-family: 'Nunito Sans', sans-serif;
    font-size: 32px;
    color: #683e35;
  }
  
  .page-subtitle {
    margin: 10px 0 0;
    color: #666;
    font-size: 16px;
  }
}

.profile-card {
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05) !important;
  
  :deep(.el-card__header) {
    padding: 20px 25px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    background-color: #f0f0f0;
  }
  
  :deep(.el-tabs__item) {
    color: #666;
    font-size: 16px;
    
    &.is-active {
      color: #FFA726;
    }
  }
  
  :deep(.el-tabs__active-bar) {
    background-color: #FFA726;
  }
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 40px;
  padding: 20px 0;
  
  @media (min-width: 768px) {
    flex-direction: row;
    align-items: flex-start;
  }
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 12px;
  
  .el-avatar {
    border: 4px solid white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.avatar-uploader {
  margin-top: 10px;
  
  .upload-btn {
    background-color: #FFA726;
    border-color: #FFA726;
    
    &:hover {
      background-color: darken(#FFA726, 5%);
      border-color: darken(#FFA726, 5%);
    }
  }
}

.info-form {
  flex: 1;
  
  :deep(.el-form-item__label) {
    color: #666;
    font-weight: 500;
  }
  
  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    
    &:hover {
      box-shadow: 0 0 0 1px #FFA726 inset;
    }
    
    &.is-focus {
      box-shadow: 0 0 0 1px #FFA726 inset;
    }
  }
  
  :deep(.el-radio__input.is-checked .el-radio__inner) {
    background-color: #FFA726;
    border-color: #FFA726;
  }
  
  :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: #FFA726;
  }
}

.password-form {
  max-width: 500px;
  margin: 30px auto;
  
  :deep(.el-form-item__label) {
    color: #666;
    font-weight: 500;
  }
  
  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    
    &:hover {
      box-shadow: 0 0 0 1px #FFA726 inset;
    }
    
    &.is-focus {
      box-shadow: 0 0 0 1px #FFA726 inset;
    }
  }
}

.submit-btn {
  min-width: 120px;
  background-color: #FFA726;
  border-color: #FFA726;
  transition: all 0.3s ease;
  
  &:hover {
    background-color: darken(#FFA726, 5%);
    border-color: darken(#FFA726, 5%);
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}
</style> 