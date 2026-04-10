<template>
    <view>
        <uni-file-picker :limit="limit" v-model="fileLists" :title="'最多选择' + limit + '个视频'" @select="select"
            @progress="progress" @success="success" @fail="fail" @delete='deleteFile' file-mediatype="video"
            :file-extname="fileExtname"> </uni-file-picker>

        <!-- 视频预览区域 -->
        <view class="video-preview-container" v-if="fileLists.length > 0">
            <view class="video-preview-item" v-for="(item, index) in fileLists" :key="index">
                <video :src="item.url" class="preview-video" controls></video>
                <view class="video-name">{{ item.name }}</view>
            </view>
        </view>
    </view>
</template>

<script setup>
import { useCommonStore } from '@/store';
import { FullConvertUrlArray } from "@/utils/comm";
import { Upload } from "@/utils/http";
import { computed, ref, watch } from 'vue';

// 获取store
const commonStore = useCommonStore();

const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)

// 定义props
const props = defineProps({
    modelValue: {
        type: [Number, String],
        default: ''
    },
    limit: {
        type: Number,
        default: 1
    },
    fileExtname: {
        type: Array,
        default: () => ['mp4', 'mov', 'avi', 'wmv', 'flv', 'mkv']
    }
});

// 定义emit
const emit = defineEmits(['update:modelValue']);

// 定义响应式数据
const fileLists = ref([]);

// 监听value变化
watch(() => props.modelValue, (newVal) => {

    if (newVal) {
        fileLists.value = newVal.split(",").map(x => {
            let fileInfo = FullConvertUrlArray(x);
            return {
                url: fileInfo[0].url,
                extname: fileInfo[0].type,
                name: fileInfo[0].name
            };
        });
    } else {
        fileLists.value = [];
    }
}, { immediate: true });

// 获取上传状态
const select = async (e) => {
    const { tempFilePaths, tempFiles } = e;
    if (tempFilePaths && tempFilePaths.length > 0) {
        for (let tempFilePath of tempFilePaths) {
            let { Data } = await Upload(tempFilePath);
            for (var item of Data) {
                let fileInfo = FullConvertUrlArray(item.Url);
                fileLists.value.push({
                    url: fileInfo[0].url,
                    extname: fileInfo[0].type,
                    name: fileInfo[0].name
                });
            }
        }
        covertUrl();
    }
};

// 获取上传进度
const progress = (e) => {

};

// 上传成功
const success = (e) => {

    covertUrl();
};

// 上传失败
const fail = (e) => {

};

// 删除文件
const deleteFile = (e) => {
    fileLists.value.splice(e.index, 1);
    covertUrl();
};

// 转换URL并触发input事件
const covertUrl = () => {
    let url = fileLists.value.length > 0 ? fileLists.value.map(x => x.url).join(",") : "";
    emit('update:modelValue', url)
};
</script>

<style lang="scss" scoped>
/* 视频预览容器 */
.video-preview-container {
    display: flex;
    flex-direction: column;
    margin-top: 15px;
}

/* 视频预览项 */
.video-preview-item {
    margin-bottom: 15px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 预览视频 */
.preview-video {
    width: 100%;
    height: 200px;
}

/* 视频名称 */
.video-name {
    font-size: 14px;
    color: #1d241f;
    padding: 8px;
    background-color: #f5f5f5;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
</style>
