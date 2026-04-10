<template>
	<view>
		<uni-file-picker :limit="limit" v-model="fileLists" :title="'最多选择' + limit + '张图片'" @select="select"
			@progress="progress" @success="success" @fail="fail" @delete='deleteFile'> </uni-file-picker>
	</view>
</template>

<script setup>
import {
	useCommonStore
} from '@/store';
import {
	FullConvertUrlArray
} from "@/utils/comm";
import {
	Upload
} from "@/utils/http";
import {
	computed,
	ref,
	watch
} from 'vue';

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
	}
}, {
	immediate: true
});

// 获取上传状态
const select = async (e) => {
	const {
		tempFilePaths,
		tempFiles
	} = e;
	if (tempFilePaths && tempFilePaths.length > 0) {
		for (let tempFilePath of tempFilePaths) {
			let {
				Data
			} = await Upload(tempFilePath);
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
/* 示例容器 */
.example-body {
	padding: 10px;
	padding-top: 0;
}

/* 自定义图片盒子 */
.custom-image-box {
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
}

/* 文本样式 */
.text {
	font-size: 14px;
	color: #1d241f;
}
</style>