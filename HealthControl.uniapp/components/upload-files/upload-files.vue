<template>
    <view class="file-upload-container">
        <!-- 当没有文件时显示上传框 -->
        <view v-if="!fileList || fileList.length === 0" class="upload-box" @click="chooseFiles">
            <view class="upload-icon">
                <uni-icons type="upload" size="30" color="var(--primary-color)"></uni-icons>
            </view>
            <view class="upload-text">点击上传附件</view>
            <view class="upload-tips">{{ tips || '支持PDF、Word、图片等格式' }}</view>
        </view>
        <!-- 已上传文件列表 -->
        <view v-else class="files-list">
            <view v-for="(fileUrl, index) in fileList" :key="index" class="file-item">
                <view class="file-info">
                    <uni-icons type="file" size="24" color="var(--primary-color)"></uni-icons>
                    <text class="file-name">{{ getFileName(fileUrl) }}</text>
                </view>
                <view class="file-actions">
                    <view class="action-btn preview-btn" @click="previewFile(fileUrl)">
                        <uni-icons type="eye" size="18"></uni-icons>
                        <text>预览</text>
                    </view>
                    <view class="action-btn delete-btn" @click="removeFile(index)">
                        <uni-icons type="trash" size="18"></uni-icons>
                        <text>删除</text>
                    </view>
                </view>
            </view>
            <!-- 添加更多按钮 -->
            <view v-if="fileList.length < maxCount" class="add-more-btn" @click="chooseFiles">
                <uni-icons type="plusempty" size="24" color="var(--primary-color)"></uni-icons>
                <text>添加更多附件</text>
            </view>
        </view>
    </view>
</template>

<script setup>
import { useCommonStore } from '@/store';
import { Upload } from "@/utils/http";
import { computed } from 'vue';

// 获取store
const store = useCommonStore();

// 定义props
const props = defineProps({
    // 文件URL字符串，多个URL用逗号分隔
    modelValue: {
        type: String,
        default: ''
    },
    // 最大上传数量
    maxCount: {
        type: Number,
        default: 8
    },
    // 文件类型扩展名
    extensions: {
        type: Array,
        default: () => ['.pdf', '.doc', '.docx', '.jpg', '.jpeg', '.png', '.zip', '.rar']
    },
    // 提示文字
    tips: {
        type: String,
        default: ''
    }
});

// 定义emit
const emit = defineEmits(['update:modelValue']);

// 计算属性：将逗号分隔的URL字符串转为数组
const fileList = computed(() => {
    if (!props.modelValue) return [];
    return props.modelValue.split(',').filter(url => url);
});

/**
 * 选择多个文件上传
 */
const chooseFiles = async () => {
    // 计算还可以上传的文件数量
    const currentCount = fileList.value.length;
    const remainingCount = props.maxCount - currentCount;

    if (remainingCount <= 0) {
        uni.showToast({
            title: `最多只能上传${props.maxCount}个附件`,
            icon: 'none'
        });
        return;
    }

    // 根据平台选择合适的文件选择API
    if (uni.getSystemInfoSync().platform === 'devtools' || uni.getSystemInfoSync().platform === 'mp-weixin') {
        // 小程序环境使用 chooseMessageFile
        uni.chooseMessageFile({
            count: remainingCount,
            type: 'all',
            extension: props.extensions,
            success: async (res) => {
                handleFileUpload(res.tempFiles);
            }
        });
    } else {
        // 其他环境尝试使用 plus.io 或 chooseImage
        // #ifdef APP-PLUS || H5
        uni.chooseFile({
            count: remainingCount,
            type: 'all',
            extension: props.extensions,
            success: async (res) => {
                if (res.tempFilePaths && res.tempFilePaths.length > 0) {
                    handleFileUpload(res.tempFilePaths.map(path => ({ path })));
                }
            }
        });
        // #endif

        // #ifdef MP-ALIPAY || MP-BAIDU || MP-TOUTIAO || MP-QQ
        uni.chooseImage({
            count: remainingCount,
            success: async (res) => {
                if (res.tempFilePaths && res.tempFilePaths.length > 0) {
                    handleFileUpload(res.tempFilePaths.map(path => ({ path })));
                }
            }
        });
        // #endif
    }
};

/**
 * 处理文件上传
 */
const handleFileUpload = async (files) => {
    if (!files || files.length === 0) return;

    uni.showLoading({
        title: '上传中...'
    });

    try {
        // 逐个上传文件
        for (let i = 0; i < files.length; i++) {
            const filePath = files[i].path || files[i].tempFilePath;
            const uploadResult = await Upload(filePath);

            if (uploadResult && uploadResult.Success) {
                // 将新上传的文件URL添加到现有的value中
                const newUrl = uploadResult.Data[0].Url;
                let newValue = props.modelValue;

                if (newValue) {
                    newValue += ',' + newUrl;
                } else {
                    newValue = newUrl;
                }

                // 通过事件更新父组件的值
                emit('update:modelValue', newValue);
            } else {
                uni.showToast({
                    title: '部分文件上传失败',
                    icon: 'none'
                });
            }
        }

        uni.showToast({
            title: '上传成功',
            icon: 'success'
        });
    } catch (error) {
      
        uni.showToast({
            title: '上传过程中出错',
            icon: 'none'
        });
    } finally {
        uni.hideLoading();
    }
};

/**
 * 预览文件
 */
const previewFile = (fileUrl) => {
    if (!fileUrl) return;

    // 获取文件扩展名
    const fileExt = getFileExtension(fileUrl).toLowerCase();

    // 显示加载提示
    uni.showLoading({
        title: '加载中...'
    });

    // 图片类型直接预览
    if (['.jpg', '.jpeg', '.png', '.gif'].includes(fileExt)) {
        uni.previewImage({
            urls: [fileUrl],
            current: fileUrl,
            complete: () => {
                uni.hideLoading();
            }
        });
        return;
    }

    // #ifdef H5
    // H5环境下使用新窗口打开
    window.open(fileUrl, '_blank');
    uni.hideLoading();
    // #endif

    // #ifdef MP || APP-PLUS
    // 小程序或APP环境下载并预览文件
    uni.downloadFile({
        url: fileUrl,
        success: (res) => {
            if (res.statusCode === 200) {
                // 打开文档预览
                uni.openDocument({
                    filePath: res.tempFilePath,
                    showMenu: true,
                    success: () => {
                  
                    },
                    fail: () => {
                        uni.showToast({
                            title: '无法预览该文件',
                            icon: 'none'
                        });
                    }
                });
            } else {
                uni.showToast({
                    title: '文件加载失败',
                    icon: 'none'
                });
            }
        },
        fail: () => {
            uni.showToast({
                title: '文件加载失败',
                icon: 'none'
            });
        },
        complete: () => {
            uni.hideLoading();
        }
    });
    // #endif
};

/**
 * 删除已上传的文件
 */
const removeFile = async (index) => {

    uni.showModal({
        title: '提示',
        content: '确定要删除该附件吗?',
        success: (res) => {
            if (res.confirm) {
                // 从数组中移除指定索引的文件
                const fileArray = [...fileList.value];
                fileArray.splice(index, 1);

                // 更新value
                emit('update:modelValue', fileArray.join(','));

                uni.showToast({
                    title: '已删除文件',
                    icon: 'success'
                });
            }
        }
    });
};

/**
 * 获取文件名
 */
const getFileName = (url) => {
    if (!url) return '';
    const parts = url.split('/');
    return parts[parts.length - 1];
};

/**
 * 获取文件扩展名
 */
const getFileExtension = (filename) => {
    if (!filename) return '';
    return filename.substring(filename.lastIndexOf('.'));
};
</script>

<style lang="scss" scoped>
/* 文件上传区域样式 */
.file-upload-container {
    width: 100%;
    margin-bottom: 20upx;
}

/* 上传框样式 */
.upload-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 240upx;
    border: 2upx dashed #d7e0c8;
    border-radius: 8upx;
    background-color: #f4f8ee;
    cursor: pointer;
}

.upload-box:active {
    background-color: #f0f0f0;
}

.upload-icon {
    margin-bottom: 20upx;
}

.upload-text {
    font-size: 28upx;
    color: #1d241f;
    margin-bottom: 10upx;
}

.upload-tips {
    font-size: 24upx;
    color: #999;
}

/* 文件列表样式 */
.files-list {
    width: 100%;
}

.file-item {
    padding: 20upx;
    border-radius: 8upx;
    background-color: #f4f8ee;
    border: 1upx solid #ebeef5;
    margin-bottom: 20upx;
}

.file-info {
    display: flex;
    align-items: center;
    margin-bottom: 20upx;
}

.file-name {
    margin-left: 16upx;
    font-size: 28upx;
    color: #1d241f;
    word-break: break-all;
}

.file-actions {
    display: flex;
    justify-content: flex-end;
}

.action-btn {
    display: flex;
    align-items: center;
    padding: 10upx 20upx;
    border-radius: 6upx;
    margin-left: 20upx;
    font-size: 24upx;
}

.action-btn text {
    margin-left: 8upx;
}

.preview-btn {
    background-color: #ecf5ff;
    color: #6d8d45;
}

.delete-btn {
    background-color: #fef0f0;
    color: #f56c6c;
}

/* 添加更多按钮 */
.add-more-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20upx;
    border: 1upx dashed #d7e0c8;
    border-radius: 8upx;
    background-color: #f4f8ee;
    cursor: pointer;
}

.add-more-btn text {
    margin-left: 10upx;
    font-size: 28upx;
    color: #6d8d45;
}
</style>
