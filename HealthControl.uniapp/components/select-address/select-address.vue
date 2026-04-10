<template>
    <uni-easyinput v-model="addressValue" placeholder="请输入地址" primaryColor="var(--primary-color)">
        <template #right>
            <view class="select-btn" @click="openAddressSelector">选择地址</view>
        </template>
    </uni-easyinput>
</template>

<script setup>
import { useCommonStore } from '@/store';
import { ref, watch } from 'vue';

// 获取store
const store = useCommonStore();

// 定义props
const props = defineProps({
    modelValue: {
        type: String,
        default: ''
    },
    disabled: {
        type: Boolean,
        default: false
    }
});

// 定义emit
const emit = defineEmits(['update:modelValue', 'selected']);

// 地址值
const addressValue = ref('');

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
    if (newVal !== undefined) {
        addressValue.value = newVal;
    }
}, { immediate: true });

// 监听addressValue变化
watch(() => addressValue.value, (newVal) => {
    emit('update:modelValue', newVal);
});

// 打开地址选择器
const openAddressSelector = () => {
    if (props.disabled) return;

    // 调用uniapp的选择位置API
    uni.chooseLocation({
        success: (res) => {
            // 成功选择地址后处理
            const { name, address, latitude, longitude } = res;
            const fullAddress = name ? `${name}(${address})` : address;

            // 更新地址值
            addressValue.value = fullAddress;

            // 触发回调，返回完整的地址信息
            emit('selected', {
                name,
                address,
                fullAddress,
                latitude,
                longitude
            });
        },
        fail: (err) => {
            // 选择失败或取消
            uni.showToast({
                title: '地址选择失败',
                icon: 'none'
            });
            console.error('地址选择失败:', err);
        }
    });
};

// 对外暴露方法
defineExpose({
    // 清空地址
    clearAddress: () => {
        addressValue.value = '';
    },
    // 设置地址
    setAddress: (address) => {
        addressValue.value = address;
    }
});
</script>

<style scoped lang="scss">
.select-btn {
    // 选择按钮样式
    color: var(--primary-color);
    font-size: 14px;
    padding: 0 10px;
    display: flex;
    align-items: center;
    height: 100%;
    cursor: pointer;
}
</style>
