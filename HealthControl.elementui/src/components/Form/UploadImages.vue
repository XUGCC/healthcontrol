<template>
    <div class="upload-files-wrap">
        <!-- 图片上传组件 -->
        <el-upload :action="uploadUrl" list-type="picture-card" :show-file-list="true" :on-success="handleUploadSuccess"
            :on-remove="handleRemove" :file-list="fileList" accept=".jpg,.png,.jpeg,.jfif,.webp" :limit="limit" :multiple="true">
            <el-icon>
                <Plus />
            </el-icon>
        </el-upload>
    </div>
</template>

<script setup>
import { GetFileNameByPath } from "@/utils/comm.js";
import { Plus } from '@element-plus/icons-vue';
import { ref, watch } from 'vue';

// 定义props
const props = defineProps({
    modelValue: {
        type: [Number, String],
        default: ''
    },
    limit: {
        type: Number,
        default: 1,
    },
})

// 定义emit
const emit = defineEmits(['update:modelValue'])

// 响应式数据
const uploadUrl = import.meta.env.VITE_API_BASE_URL + "/File/BatchUpload"
const fileList = ref([])

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
    if (newVal) {
        fileList.value = newVal.split(",").map(x => {
            return {
                url: x,
                name: GetFileNameByPath(x),
                status: "success"
            }
        })
    }
}, { immediate: true })

// 文件列表转换方法
const FileListConvert = (files) => {
    let list = []
    if (Array.isArray(files)) {
        files.filter(x => x.status == "success").forEach((item) => {
            if (item.response != null) {
                list = [...list, { name: "", url: item.response.Data[0].Url }]
            } else {
                list = [...list, item]
            }
        })
    }
    return list
}

// 文件上传成功处理
const handleUploadSuccess = (response, file, fileList) => {
    let fs = FileListConvert(fileList)
    let url = fs.length > 0 ? fs.map(x => x.url).join(",") : ""
    emit('update:modelValue', url)
}

// 文件移除处理
const handleRemove = (file, fileList) => {
    let fs = FileListConvert(fileList)
    let url = fs.length > 0 ? fs.map(x => x.url).join(",") : ""
    emit('update:modelValue', url)
}
</script>

<style scoped>
/* 上传组件容器样式 */
.upload-files-wrap {
    width: 100%;
}

/* 上传按钮样式 */
:deep(.el-upload--picture-card) {
    background-color: transparent !important;
}

/* 上传图标样式 */
:deep(.el-upload--picture-card .el-icon) {
    font-size: 28px;
    color: #8c939d;
}
</style>