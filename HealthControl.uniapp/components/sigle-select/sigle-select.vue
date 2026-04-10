<template>
    <uni-data-select v-model="selectValue" :localdata="dataList" @change="PickerChange" primaryColor="var(--primary-color)"
        :clear="true" :disabled='disabled'></uni-data-select>
</template>

<script setup>
import { useCommonStore } from '@/store';
import { GetObjectValue } from "@/utils/comm.js";
import { Post } from "@/utils/http";
import { computed, onMounted, ref, watch } from 'vue';

// 获取store
const store = useCommonStore();


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
    url: {
        type: String, //默认的请求路径
        default: "",
    },
    columnName: {
        type: String, //需要绑定Name的值
        default: "",
    },
    columnValue: {
        type: String, //需要绑定Value的值
        default: "",
    },
    columnLabel: {
        type: String, //需要绑定Label的值
        default: "",
    },
    where: {
        type: Object,
        default: () => ({})
    },
    filterValue: {
        type: Array, //需要绑定Value的值
        default: () => []
    },
    disabled: {
        type: Boolean,
        default: false
    },
});

// 定义emit
const emit = defineEmits(['update:modelValue', 'SelectCallBack']);

// 定义响应式数据
const dataList = ref([]);
const where_ = ref({});
const searchWhere = ref({});
const selectValue = ref();

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
    if (newVal != null && newVal !== undefined) {
        selectValue.value = newVal.toString();
    }
}, { immediate: true });

// 初始化
onMounted(() => {
    where_.value = props.where;
    listApi();
});

// 重新加载数据
const reload = async (where) => {
    let newSearchWhere = {};
    if (where) {
        newSearchWhere = Object.assign(newSearchWhere, where);
    }
    searchWhere.value = newSearchWhere;
    await listApi(newSearchWhere);
};

// 获取数据列表
const listApi = async (customSearchWhere = {}) => {
    try {
        const { Data } = await Post(props.url, {
            Limit: 999,
            ...where_.value,
            ...customSearchWhere
        });

        const items = Data.Items;
        const newDataList = [];

        items.forEach((item) => {
            if (!props.filterValue.find(x => x == GetObjectValue(item, props.columnValue)?.toString())) {
                newDataList.push({
                    text: GetObjectValue(item, props.columnName),
                    value: GetObjectValue(item, props.columnValue).toString(),
                });
            }
        });

        if (props.filterValue.filter(x => x.toString() == selectValue.value).length > 0) {
            selectValue.value = undefined;
        }

        dataList.value = newDataList;
    } catch (error) {

    }
};

// 选择变更处理
const PickerChange = () => {
    emit('update:modelValue', selectValue.value);
    emit('SelectCallBack', selectValue.value);
};

// 对外暴露方法
defineExpose({
    reload
});
</script>

<style scoped lang="scss">
.text {
    font-size: 12px;
    color: #59665d;
    margin-top: 5px;
}

.uni-px-5 {
    padding-left: 10px;
    padding-right: 10px;
}

.uni-pb-5 {
    padding-bottom: 10px;
}

.uni-select__input-text {
    color: black;
}
</style>