<template>
    <div class="table-box" v-if="columnList.length > 0">
        <el-card class="box-card">
            <!-- 功能区域插槽 -->

            <slot name="header"></slot>

            <!-- 表格主体 -->
            <div class="tb-body">
                <el-table :data="datalist" v-loading="listLoading" element-loading-text="加载数据中~~~" :stripe="true"
                    highlight-current-row @row-click="rowClick" @selection-change="handleSelectionChange"
                    @sort-change="handleSortChange">
                    <!-- 选择列 -->
                    <el-table-column type="selection" width="55" />

                    <!-- 动态列渲染 -->
                    <template v-for="(item, idx) in columnList" :key="idx">
                        <!-- 普通文本列 -->
                        <el-table-column v-if="item.type == 1" v-bind="item" align="center" :sortable="item.sort"
                            :prop="item.key" :label="item.title" :width="item.width || 'auto'">
                            <template #default="{ row }">
                                <slot :name="item.key" :row="row">
                                    {{ row[item.key] }}
                                </slot>
                            </template>
                        </el-table-column>

                        <!-- 日期列 -->
                        <el-table-column v-else-if="item.type == 2" v-bind="item" align="center" :prop="item.key"
                            :label="item.title" :sortable="item.sort" :width="item.width || 'auto'">
                            <template #default="{ row }">
                                <slot :name="item.key" :row="row">
                                    {{ YMDFormat(row[item.key]) }}
                                </slot>
                            </template>
                        </el-table-column>

                        <!-- 自定义操作列 -->
                        <el-table-column v-else-if="item.type == 4" v-bind="item" align="center" :width="item.width || 100"
                            :prop="item.key" :label="item.title" fixed="right">
                            <template #default="{ row }">
                                <slot :name="item.key" :row="row"></slot>
                            </template>
                        </el-table-column>

                        <!-- 短文本列(带悬浮提示) -->
                        <el-table-column v-else-if="item.type == 8" v-bind="item" align="center" :prop="item.key"
                            :sortable="item.sort" :label="item.title" :width="item.width || 'auto'">
                            <template #default="{ row }">
                                <el-popover trigger="hover" placement="top">
                                    <p>{{ row[item.key] }}</p>
                                    <template #reference>
                                        <div class="text-cut">
                                            {{ row[item.key] }}
                                        </div>
                                    </template>
                                </el-popover>
                            </template>
                        </el-table-column>

                        <!-- 标签列 -->
                        <el-table-column v-else-if="item.type == 9" v-bind="item" align="center" :prop="item.key"
                            :label="item.title" :width="item.width || 'auto'">
                            <template #default="{ row }">
                                <template v-for="(tagItem, tagIndex) in row[item.key]" :key="tagIndex">
                                    <el-tag type="primary">{{ tagItem }}</el-tag>
                                </template>
                            </template>
                        </el-table-column>

                        <!-- 带标签的悬浮提示列 -->
                        <el-table-column v-else-if="item.type == 10" v-bind="item" align="center" :prop="item.key"
                            :label="item.title" :width="item.width || 'auto'">
                            <template #default="{ row }">
                                <el-popover trigger="hover" placement="top">
                                    <p>{{ row[item.key] }}</p>
                                    <template #reference>
                                        <div class="name-wrapper">
                                            <el-tag size="small">{{ row[item.key] }}</el-tag>
                                        </div>
                                    </template>
                                </el-popover>
                            </template>
                        </el-table-column>

                        <!-- 图片组列 -->
                        <el-table-column v-else-if="item.type == 12" v-bind="item" :prop="item.key" :label="item.title"
                            :width="item.width || 'auto'" align="center">
                            <template #default="{ row }">
                                <el-image v-for="(image, idx) in row[item.key]" :key="idx" style="width: 50px; height: 40px"
                                    :src="image" fit="scale-down">
                                    <template #error>
                                        <div class="image-slot">
                                            <i class="el-icon-picture-outline"></i>
                                        </div>
                                    </template>
                                </el-image>
                            </template>
                        </el-table-column>

                        <!-- 视频列 -->
                        <el-table-column v-else-if="item.type == 13" v-bind="item" align="center"
                            :width="item.width || 'auto'" :prop="item.key" :label="item.title">
                            <template #default="{ row }">
                                <VideoButton :url="row[item.key]"></VideoButton>
                            </template>
                        </el-table-column>

                        <!-- 自定义内容列 -->
                        <el-table-column v-else-if="item.type == 14" v-bind="item" align="center"
                            :width="item.width || 'auto'" :prop="item.key" :label="item.title">
                            <template #default="{ row }">
                                <slot :name="item.key" :row="row"></slot>
                            </template>
                        </el-table-column>

                        <!-- 音频列 -->
                        <el-table-column v-else-if="item.type == 15" v-bind="item" align="center"
                            :width="item.width || 'auto'" :prop="item.key" :label="item.title">
                            <template #default="{ row }">
                                <AudioButton :url="row[item.key]"></AudioButton>
                            </template>
                        </el-table-column>

                        <!-- 超链接列 -->
                        <el-table-column v-else-if="item.type == 16" v-bind="item" align="center"
                            :width="item.width || 'auto'" :prop="item.key" :label="item.title">
                            <template #default="{ row }">
                                <a v-if="row[item.key]" target="_blank" style="color:blue;text-decoration:underline"
                                    :href="row[item.key]">超链接</a>
                            </template>
                        </el-table-column>

                        <!-- 单图片列 -->
                        <el-table-column v-else-if="item.type == 17" v-bind="item" :prop="item.key" :label="item.title"
                            :width="item.width || 'auto'" align="center">
                            <template #default="{ row }">
                                <el-image style="width: 50px; height: 40px" :src="row[item.key]"
                                    :preview-src-list="[row[item.key]]" fit="scale-down">
                                    <template #error>
                                        <div class="image-slot">
                                            <i class="el-icon-picture-outline"></i>
                                        </div>
                                    </template>
                                </el-image>
                            </template>
                        </el-table-column>

                        <!-- 富文本列 -->
                        <el-table-column v-else-if="item.type == 18" v-bind="item" :prop="item.key" :label="item.title"
                            :width="item.width || 'auto'" align="center">
                            <template #default="{ row }">
                                <RichButton :content="row[item.key]"></RichButton>
                            </template>
                        </el-table-column>

                        <!-- 文件链接列 -->
                        <el-table-column v-else-if="item.type == 19" v-bind="item" :prop="item.key" :label="item.title"
                            :width="item.width || 'auto'" align="center">
                            <template #default="{ row }">
                                <FilesLinkButton :url="row[item.key]"></FilesLinkButton>
                            </template>
                        </el-table-column>

                        <!-- 判断标签列 -->
                        <el-table-column v-else-if="item.type == 21" v-bind="item" :prop="item.key" :label="item.title"
                            :width="item.width || 'auto'" align="center">
                            <template #default="{ row }">
                                <el-tag v-if="row[item.key] == true" type="success">是</el-tag>
                                <el-tag v-else-if="row[item.key] == false" type="info">否</el-tag>
                            </template>
                        </el-table-column>

                        <!-- 保留3位小数列 -->
                        <el-table-column v-else-if="item.type == 22" v-bind="item" align="center" :prop="item.key"
                            :sortable="item.sort" :label="item.title" :width="item.width || 'auto'">
                            <template #default="{ row }">
                                <el-popover trigger="hover" placement="top">
                                    <p>{{ row[item.key] || ToFixed3 }}</p>
                                    <template #reference>
                                        <div class="text-cut">
                                            {{ ToFixed3(row[item.key]) }}
                                        </div>
                                    </template>
                                </el-popover>
                            </template>
                        </el-table-column>

                        <el-table-column v-else-if="item.type == 23" v-bind="item" :prop="item.key" :label="item.title"
                            :width="item.width || 'auto'" align="center">
                            <template #default="{ row }">
                                <div v-html="row[item.key]"></div>
                            </template>
                        </el-table-column>

                        <!-- 默认列 -->
                        <el-table-column v-else v-bind="item" align="center" :prop="item.key" :label="item.title"
                            :sortable="item.sort" :width="item.width || 'auto'">
                            <template #default="{ row }">

                                <span>{{ row[item.key] }}</span>
                            </template>
                        </el-table-column>
                    </template>
                </el-table>

                <!-- 分页组件 -->
                <div class="custom-pagination">
                    <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.limit"
                        :page-sizes="pagination.pageSizes" :total="pagination.total" background
                        layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                        @current-change="handleCurrentChange" />
                </div>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { Post } from '@/api/http';
import { useCommonStore } from '@/store';
import { ElMessage } from 'element-plus';
import { computed, onMounted, ref } from 'vue';

const commonStore = useCommonStore();

const YMDFormat = window.YMDFormat;

// 定义props
const props = defineProps({
    url: {
        type: String,
        default: ''
    },
    column: {
        type: Array,
        default: () => []
    },
    where: {
        type: Object,
        default: () => ({})
    }
});

// 定义emit
const emit = defineEmits(['fetchDataSuccess', 'rowClick', 'rowSelectionChange']);

// 响应式数据
const selectRow = ref([]);
const datalist = ref([]);
const listLoading = ref(true);
const where_ = ref({});
const searchWhere = ref({});

// 分页配置
const pagination = ref({
    limit: 10,
    page: 1,
    total: 0,
    pageSizes: [10, 20, 50, 100, 200, 500, 1000]
});

// 计算属性：过滤隐藏列
const columnList = computed(() => {
    return props.column.filter(x => !x.hidden);
});

// 分页事件
const handleCurrentChange = (val) => {
    pagination.value.page = val;
    fetchDataList(searchWhere.value);
};

// 分页大小事件
const handleSizeChange = (val) => {
    pagination.value.limit = val;
    pagination.value.page = 1;
    fetchDataList(searchWhere.value);
};

// 行点击事件
const rowClick = (row) => {
    emit('rowClick', { selectRow: row });
};

// 行选择事件
const handleSelectionChange = (rows) => {
    selectRow.value = rows;
    emit('rowSelectionChange', { selectRow: rows });
};

// 排序事件
const handleSortChange = (column) => {

    if (column.order == null) {
        where_.value.SortItem = null;
    } else {
        where_.value.SortItem = {
            Sort: column.prop,
            IsAsc: column.order == 'ascending'
        };
    }
    fetchDataList();
};

// 获取对象中指定key的值
const GetObjectValue = (obj, name) => {
    if (!name || !obj) return undefined;
    if (name.indexOf('.') !== -1) {
        const array = name.split('.');
        let value = obj;
        for (const item of array) {
            if (value == null) return undefined;
            value = value[item];
        }
        return value;
    }
    return obj[name];
};

// 保留3位小数
const ToFixed3 = (value) => {
    if (!value) return '';
    return Number(value).toFixed(3);
};

// 获取数据列表
const fetchDataList = async (searchWhere = {}) => {
    listLoading.value = true;
    try {
        const { Data: { Items, TotalCount } } = await Post(props.url, {
            Limit: pagination.value.limit,
            Page: pagination.value.page,
            ...where_.value,
            ...searchWhere
        });

        const dataList = Items.map((item, index) => {
            const dataItem = {};
            props.column.forEach((element) => {
                if (element.template) {
                    dataItem[element.key] = element.template(item, index);
                } else {
                    dataItem[element.key] = GetObjectValue(item, element.key);
                }

                if (element.type == 12) { // IMAGES类型
                    if (element.template) {
                        dataItem[element.key] = dataItem[element.key]?.split(',');
                    } else {
                        dataItem[element.key] = GetObjectValue(item, element.key)?.split(',');
                    }
                }
            });
            dataItem.OrginValue = item;
            return dataItem;
        });

        datalist.value = dataList;
        selectRow.value = [];
        pagination.value.total = parseInt(TotalCount);
        emit('fetchDataSuccess', { items: Items, totalCount: TotalCount });
    } catch (error) {
        ElMessage.error('获取数据失败');
    } finally {
        listLoading.value = false;
    }
};

// 初始化
onMounted(() => {
    where_.value = props.where;
    fetchDataList();
});

// 暴露方法给父组件
defineExpose({
    Reload: (where) => {
        pagination.value.page = 1;
        pagination.value.total = 0;
        const newSearchWhere = where ? { ...where } : {};
        searchWhere.value = newSearchWhere;
        fetchDataList(newSearchWhere);
    },
    GetSelectionRow: () => selectRow.value
});
</script>

<style scoped>
/* 分页样式 */
.custom-pagination {
    margin-top: 20px;
    display: flex;
    flex-direction: row-reverse;
}

.tb-body {
    padding-top: 20px;
}

/* 表格容器样式 */
.table-box {
    background-color: white;
    margin-top: 20px;
}

/* 文本截断样式 */
.text-cut {
    white-space: nowrap;
    overflow: hidden;
    text-align: center;
    text-overflow: ellipsis;
}

/* 图片错误占位样式 */
.image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background: #f5f7fa;
    color: #909399;
}
</style>