<template>
    <el-input-tag v-model="tagValue" clearable :placeholder="placeholder" @change="handleChange" />
</template>

<script setup>
import { ref, watch } from 'vue';

// 定义props
const props = defineProps({
    modelValue: {
        type: String,
        default: ''
    },
    disabled: {
        type: Boolean,
        default: false
    },
    placeholder: {
        type: String,
        default: '请输入'
    },


})

// 定义emit
const emit = defineEmits(['update:modelValue', 'change'])

// 响应式数据
const tagValue = ref([])

// 将逗号分隔的字符串转换为数组
const stringToArray = (str) => {
    if (!str) return [];
    return str.split(',').filter(item => item.trim() !== '').map(item => item.trim());
}

// 将数组转换为逗号分隔的字符串
const arrayToString = (arr) => {
    if (!arr || arr.length === 0) return '';
    return arr.join(',');
}

// 监听modelValue变化
watch(
    () => props.modelValue,
    (newVal) => {
        // 接收字符串格式的值，转换为数组供组件内部使用
        tagValue.value = stringToArray(newVal);
    },
    { immediate: true }
)

// 标签值变化处理
const handleChange = (value) => {
    // 将数组格式的值转换为逗号分隔的字符串返回
    const stringValue = arrayToString(value);
    emit('update:modelValue', stringValue);
    emit('change', stringValue);
}
</script>

<style scoped>
/* 标签输入框样式 */
</style>
