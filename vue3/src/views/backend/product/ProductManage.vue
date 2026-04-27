<template>
  <div class="product-manage-container">
    <div class="page-header">
      <h2>商品管理</h2>
      <div class="header-buttons">
        <el-dropdown @command="handleExport" class="export-dropdown">
          <el-button type="success">
            导出数据
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="excel">导出为Excel</el-dropdown-item>
              <el-dropdown-item command="txt">导出为TXT</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="handleAdd">添加商品</el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入商品名称"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select
            v-model="searchForm.category"
            placeholder="请选择分类"
            clearable
          >
            <el-option
              v-for="category in categories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="商品状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
          >
            <el-option label="上架" :value="1"></el-option>
            <el-option label="下架" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <el-table
      v-loading="loading"
      :data="productList"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="商品图片" width="100">
        <template #default="scope">
          <el-image
            :src="getImageUrl(scope.row.images)"
            style="width: 60px; height: 60px"
            :preview-src-list="[getImageUrl(scope.row.images)]"
            fit="cover"
                 :preview-teleported="true"
          >
          </el-image>
        </template>
      </el-table-column>
      <el-table-column
        prop="name"
        label="商品名称"
        show-overflow-tooltip
      ></el-table-column>
      <el-table-column
        prop="category"
        label="分类"
        width="100"
      ></el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">¥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? "上架" : "下架" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)"
            >编辑</el-button
          >
          <el-button
            :type="scope.row.status === 1 ? 'warning' : 'success'"
            size="small"
            @click="handleChangeStatus(scope.row)"
          >
            {{ scope.row.status === 1 ? "下架" : "上架" }}
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页器 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
      </el-pagination>
    </div>

    <!-- 编辑/添加商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '添加商品'"
      width="650px"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input
            v-model="productForm.name"
            placeholder="请输入商品名称"
          ></el-input>
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select
            v-model="productForm.category"
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number
            v-model="productForm.price"
            :precision="2"
            :min="0"
            style="width: 100%"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="商品库存" prop="stock">
          <el-input-number
            v-model="productForm.stock"
            :min="0"
            style="width: 100%"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="商品状态" prop="status">
          <el-radio-group v-model="productForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <el-upload
            class="upload-demo"
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
          <div class="upload-tip">
            提示: 请上传商品图片(JPG/PNG格式,大小不超过2MB)
          </div>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          >
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, arrowDown } from "@element-plus/icons-vue";
import request from "@/utils/request";
import * as XLSX from 'xlsx';

// 数据定义
const loading = ref(false);
const productList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const dialogVisible = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const productFormRef = ref(null);
const fileList = ref([]);
const baseAPI = process.env.VUE_APP_BASE_API || "/api";

// 商品分类
const categories = [
  { label: "主粮", value: "主粮" },
  { label: "零食", value: "零食" },
  { label: "玩具", value: "玩具" },
  { label: "日用品", value: "日用品" },
  { label: "保健品", value: "保健品" },
  { label: "服饰", value: "服饰" },
  { label: "其他", value: "其他" },
];

// 搜索表单
const searchForm = reactive({
  name: "",
  category: "",
  status: "",
});

// 商品表单
const productForm = reactive({
  id: null,
  name: "",
  category: "",
  price: 0,
  stock: 0,
  images: "",
  description: "",
  status: 1,
});

// 表单校验规则
const productRules = {
  name: [
    { required: true, message: "请输入商品名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
  ],
  category: [{ required: true, message: "请选择商品分类", trigger: "change" }],
  price: [{ required: true, message: "请输入商品价格", trigger: "blur" }],
  stock: [{ required: true, message: "请输入商品库存", trigger: "blur" }],
};

// 上传相关数据
const uploadHeaders = computed(() => {
  return {
    token: localStorage.getItem("token"),
  };
});

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true;
  try {
    await request.get(
      "/product/page",
      {
        name: searchForm.name,
        category: searchForm.category,
        status: searchForm.status === "" ? null : searchForm.status,
        currentPage: currentPage.value,
        size: pageSize.value,
      },
      {
        onSuccess: (res) => {
          productList.value = res.records;
          total.value = res.total;
        },
      }
    );
  } catch (error) {
    console.error("获取商品列表失败:", error);
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchProducts();
};

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach((key) => {
    searchForm[key] = "";
  });
  currentPage.value = 1;
  fetchProducts();
};

const getImageUrl = (images) => {
  // 使用数据URI作为默认图片，而不是引用外部图片文件
  const defaultImage =
    "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3QgeD0iMiIgeT0iMiIgd2lkdGg9Ijc2IiBoZWlnaHQ9Ijc2IiBzdHlsZT0iZmlsbDojZWVlO3N0cm9rZTojYWFhO3N0cm9rZS13aWR0aDoycHg7Ii8+PHRleHQgeD0iNDAiIHk9IjQwIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBzdHlsZT0iZmlsbDojOTk5O2ZvbnQtZmFtaWx5OkFyaWFsLHNhbnMtc2VyaWY7Zm9udC1zaXplOjEycHg7Ij7ml6A8L3RleHQ+PC9zdmc+";

  if (!images) return defaultImage;

  const imageList = images.split(",");
  if (imageList.length === 0) return defaultImage;

  const firstImage = imageList[0].trim();
  if (firstImage.startsWith("http")) {
    return firstImage;
  }
  return `${baseAPI}${firstImage}`;
};
// 添加商品
const handleAdd = () => {
  isEdit.value = false;
  Object.keys(productForm).forEach((key) => {
    if (key === "status") {
      productForm[key] = 1;
    } else if (key === "price" || key === "stock") {
      productForm[key] = 0;
    } else {
      productForm[key] = "";
    }
  });
  fileList.value = [];
  dialogVisible.value = true;
};

// 编辑商品
const handleEdit = (row) => {
  isEdit.value = true;
  Object.keys(productForm).forEach((key) => {
    productForm[key] = row[key];
  });

  // 设置图片列表
  fileList.value = [];
  if (row.images) {
    const imageArr = row.images.split(",");
    imageArr.forEach((img, index) => {
      fileList.value.push({
        name: `图片${index + 1}`,
        url: getImageUrl(img),
        response: { fileName: img },
      });
    });
  }

  dialogVisible.value = true;
};

// 更改商品状态
const handleChangeStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? "上架" : "下架";

    await request.put(
      `/product/${row.id}`,
      {
        ...row,
        status: newStatus,
      },
      {
        successMsg: `商品${actionText}成功`,
        onSuccess: () => {
          fetchProducts();
        },
      }
    );
  } catch (error) {
    console.error("更改商品状态失败:", error);
  }
};

// 删除商品
const handleDelete = (row) => {
  ElMessageBox.confirm("确定要删除该商品吗？删除后不可恢复", "删除商品", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await request.delete(`/product/${row.id}`, {
          successMsg: "商品删除成功",
          onSuccess: () => {
            fetchProducts();
          },
        });
      } catch (error) {
        console.error("删除商品失败:", error);
      }
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 处理图片上传
const handleImageChange = (file, uploadFileList) => {
  console.log("图片变化回调:", file);
};

// 监听文件列表变化
const handleChange = (file, uploadFileList) => {
  console.log("文件列表变化:", file, uploadFileList);
};

// 处理上传成功
const handleUploadSuccess = (response, file, fileList) => {
  console.log("上传成功回调:", response, file);
  ElMessage.success("上传成功");
};

// 处理上传失败
const handleUploadError = (error, file, fileList) => {
  console.error("上传失败:", error, file);
  ElMessage.error("上传失败");
};

// 处理移除上传文件
const handleUploadRemove = (file, uploadFileList) => {
  console.log("移除文件:", file, "当前文件列表:", uploadFileList);
  // 更新fileList引用
  fileList.value = uploadFileList;
};

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
    console.log("开始上传文件:", file.name);

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

        console.log("服务器返回的文件数据:", data);

        if (data) {
          // 构造文件对象，确保响应格式与el-upload期望的一致
          const fileObj = {
            name: file.name,
            url: `${baseAPI}${data}`,
            // 关键：保存原始的服务器返回的文件路径，而不是blob URL
            response: { fileName: data },
          };

          console.log("构造的文件对象:", fileObj);

          // 通知上传组件上传成功，传递服务器返回的文件路径
          onSuccess({ fileName: data });

          // 由于el-upload组件在使用自定义上传时可能不会自动更新fileList
          // 我们确保文件被添加到列表中
          const found = fileList.value.some(
            (f) => f.response && f.response.fileName === data
          );

          if (!found) {
            fileList.value.push(fileObj);
            console.log("已手动添加文件到列表，当前列表:", fileList.value);
          }
        } else {
          onError(new Error("服务器返回的文件数据无效"));
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

// 提交表单
const submitForm = async () => {
  if (!productFormRef.value) return;

  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        console.log("提交前的文件列表:", fileList.value);

        // 处理图片：从response中获取服务器返回的文件路径，而不是使用blob URL
        if (fileList.value.length > 0) {
          // 严格筛选具有response.fileName属性的文件，确保只使用服务器路径
          const fileNames = fileList.value
            .filter((file) => file.response && file.response.fileName)
            .map((file) => file.response.fileName);

          console.log("过滤后的文件名列表 (服务器路径):", fileNames);

          if (fileNames.length > 0) {
            productForm.images = fileNames.join(",");
            console.log(
              "最终设置的images字段 (服务器路径):",
              productForm.images
            );
          } else {
            // 如果没有找到有效的文件名，保留原始图片（编辑模式下）
            if (!isEdit.value) {
              productForm.images = "";
            }
            console.log(
              "未找到有效的服务器路径，images字段:",
              productForm.images
            );
          }
        } else {
          productForm.images = "";
          console.log("文件列表为空，images字段被清空");
        }

        // 输出完整的表单数据
        console.log("提交的商品表单数据:", JSON.stringify(productForm));

        if (isEdit.value) {
          // 编辑模式
          await request.put(`/product/${productForm.id}`, productForm, {
            successMsg: "商品更新成功",
            onSuccess: (res) => {
              console.log("更新商品成功，响应:", res);
              dialogVisible.value = false;
              fetchProducts();
            },
          });
        } else {
          // 添加模式
          await request.post("/product", productForm, {
            successMsg: "商品添加成功",
            onSuccess: (res) => {
              console.log("添加商品成功，响应:", res);
              dialogVisible.value = false;
              fetchProducts();
            },
          });
        }
      } catch (error) {
        console.error("提交商品表单失败:", error);
        ElMessage.error("保存商品失败");
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchProducts();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchProducts();
};

// 页面加载时获取商品列表
onMounted(() => {
  fetchProducts();
});

// 导出处理函数
const handleExport = (command) => {
  if (command === 'excel') {
    exportToExcel();
  } else if (command === 'txt') {
    exportToTxt();
  }
};

// 导出为Excel
const exportToExcel = () => {
  // 准备导出数据
  const exportData = productList.value.map(item => ({
    'ID': item.id,
    '商品名称': item.name,
    '分类': item.category,
    '价格': item.price,
    '库存': item.stock,
    '状态': item.status === 1 ? '上架' : '下架',
    '描述': item.description
  }));

  // 创建工作簿
  const wb = XLSX.utils.book_new();
  const ws = XLSX.utils.json_to_sheet(exportData);

  // 设置列宽
  const colWidth = [
    { wch: 8 },  // ID
    { wch: 20 }, // 商品名称
    { wch: 10 }, // 分类
    { wch: 10 }, // 价格
    { wch: 10 }, // 库存
    { wch: 8 },  // 状态
    { wch: 30 }, // 描述
  ];
  ws['!cols'] = colWidth;

  // 添加工作表到工作簿
  XLSX.utils.book_append_sheet(wb, ws, '商品列表');

  // 生成文件并下载
  const now = new Date();
  const timestamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`;
  XLSX.writeFile(wb, `商品列表_${timestamp}.xlsx`);
  
  ElMessage.success('Excel文件导出成功');
};

// 导出为TXT
const exportToTxt = () => {
  // 准备导出数据
  let content = '商品列表\n\n';
  content += '序号\t商品名称\t分类\t价格\t库存\t状态\t描述\n';
  content += '================================================\n';
  
  productList.value.forEach((item, index) => {
    content += `${index + 1}\t${item.name}\t${item.category}\t${item.price}\t${item.stock}\t${item.status === 1 ? '上架' : '下架'}\t${item.description || ''}\n`;
  });

  // 创建Blob对象
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
  
  // 创建下载链接
  const now = new Date();
  const timestamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`;
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `商品列表_${timestamp}.txt`;
  
  // 触发下载
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  
  ElMessage.success('TXT文件导出成功');
};
</script>

<style scoped>
.product-manage-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
}

.search-container {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.header-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.export-dropdown {
  margin-right: 10px;
}
</style> 