<template>
    <el-select 
        style="width: 100%" 
        :disabled="disabled" 
        v-model="selectValue" 
        multiple 
        allow-create 
        placeholder="请选择"
        :filterable="true" 
        :loading="loading" 
        :clearable="true" 
        :remote="true" 
        @change="handleChange"
    >
        <el-option 
            v-for="item in options" 
            :key="item.name" 
            :label="item.name" 
            :value="item.name"
        >
            <span style="float: left">{{ item.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ item.label }}</span>
        </el-option>
    </el-select>
</template>

<script setup>
import { Post } from '@/api/http';
import { onMounted, ref, watch } from 'vue';

// 定义props
const props = defineProps({
    modelValue: {
        type: [Number, String],
        default: ''
    },
    url: {
        type: String, //默认的请求路径
        default: ""
    },
    columnName: { //默认的列属性
        type: String, //默认的请求路径
        default: ""
    },
    where: {
        type: Object,
        default: () => ({})
    },
    disabled: {
        type: Boolean,
        default: false
    }
})

// 定义emit
const emit = defineEmits(['update:modelValue'])

// 响应式数据
const loading = ref(false)
const options = ref([])
const selectValue = ref([])
const where_ = ref({})

// 监听modelValue变化
watch(
    () => props.modelValue,
    (newVal) => {
        if (newVal?.toString()?.length > 0) {
            selectValue.value = newVal.toString().split(",")
        } else {
            selectValue.value = []
        }
    },
    { immediate: true }
)

// 获取数据列表
const fetchDataList = async () => {
    if (props.url) {
        loading.value = true
        try {
            const { Data: { Items } } = await Post(props.url, {
                ...where_.value
            })
            
            const dataList = Items.map(item => ({
                name: item[props.columnName]
            }))
            
            options.value = dataList
        } catch (error) {
            console.error('Failed to fetch data:', error)
        } finally {
            loading.value = false
        }
    }
}

// 初始化数据
const initData = () => {
    where_.value = {}
}

// 重新加载数据
const reload = async (where) => {
    if (where) {
        where_.value = { ...where }
    }
    await fetchDataList()
}

// 选择值变化处理
const handleChange = (value) => {
    if (Array.isArray(value) && value.length > 0) {
        emit('update:modelValue', value.join(","))
    } else {
        emit('update:modelValue', '')
    }
}

// 组件挂载时初始化
onMounted(() => {
    where_.value = props.where
    fetchDataList()
})

// 暴露方法给父组件
defineExpose({
    initData,
    reload,
    fetchDataList
})
</script>

<style scoped lang="scss">
</style>
