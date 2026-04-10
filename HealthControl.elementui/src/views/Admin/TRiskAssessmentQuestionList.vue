<template>
    <div>
        <!-- 搜索表单卡片 -->
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <el-row>
                    <el-button type="primary" size="default" @click="SearchClick">
                        <el-icon>
                            <Search />
                        </el-icon>查询
                    </el-button>
                    <el-button type="warning" size="default" @click="ResetClick">
                        <el-icon>
                            <Refresh />
                        </el-icon>清空条件
                    </el-button>
                </el-row>
            </div>
            <div class="margin-top-sm">
                <el-form :inline="true" :model="searchForm" size="default">
                    <el-form-item label="问卷Id" prop="QuestionnaireId">
                        <el-input-number v-model="searchForm.QuestionnaireId" :min="1" :max="1000000" />
                    </el-form-item>
                    <el-form-item label="题干" prop="QuestionTitle">
                        <el-input v-model.trim="searchForm.QuestionTitle" placeholder="请输入题干关键字" :clearable="true" />
                    </el-form-item>
                    <el-form-item label="维度编码" prop="DimensionCode">
                        <el-input v-model.trim="searchForm.DimensionCode" placeholder="如：SMOKING/DRINKING" :clearable="true" />
                    </el-form-item>
                </el-form>
            </div>
        </el-card>

        <!-- 编辑对话框 -->
        <el-dialog :title="formData.Id ? '修改题目' : '添加题目'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData"
                label-width="140px" size="default">
                <el-row :gutter="10" class="edit-from-body">
                    <el-col :span="24">
                        <el-form-item label="问卷Id" prop="QuestionnaireId">
                            <el-input-number v-model="formData.QuestionnaireId" :min="1" :max="1000000" />
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="维度编码" prop="DimensionCode">
                            <el-input v-model="formData.DimensionCode" placeholder="如 SMOKING/DRINKING/OTHER" />
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="题干" prop="QuestionTitle">
                            <el-input type="text" v-model="formData.QuestionTitle" placeholder="请输入题目内容" :clearable="true" />
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="题目说明" prop="QuestionDesc">
                            <el-input type="textarea" v-model="formData.QuestionDesc" placeholder="可选：对题目的补充说明"
                                :rows="2" />
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="题型" prop="QuestionType">
                            <el-select v-model="formData.QuestionType" placeholder="请选择题型">
                                <el-option :value="1" label="单选" />
                                <el-option :value="2" label="多选" />
                                <el-option :value="3" label="是/否" />
                                <el-option :value="4" label="填空" />
                            </el-select>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="是否必答" prop="IsRequired">
                            <el-select v-model="formData.IsRequired">
                                <el-option :value="1" label="是" />
                                <el-option :value="0" label="否" />
                            </el-select>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="排序值" prop="SortNum">
                            <el-input-number v-model="formData.SortNum" :min="0" :max="1000000" />
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记" prop="IsDelete">
                            <el-input-number v-model="formData.IsDelete" placeholder="0=正常,1=已删除" :min="0" :max="1" />
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row type="flex" justify="end" align="bottom">
                    <el-form-item>
                        <el-button size="default" type="primary" plain @click="CreateOrEditForm()">确 定</el-button>
                        <el-button size="default" @click="editorShow = false">取 消</el-button>
                    </el-form-item>
                </el-row>
            </el-form>
        </el-dialog>

        <!-- 数据表格 -->
        <PaginationTable ref="PaginationTableId" url="/TRiskAssessmentQuestion/List" :column="columnList" :where="where">
            <template v-slot:header>
                <el-button type="primary" size="default" @click="ShowEditModal(undefined, true)">
                    <el-icon>
                        <Edit />
                    </el-icon>新 增
                </el-button>
                <el-button type="danger" size="default" @click="BatchDelete">
                    <el-icon>
                        <Delete />
                    </el-icon>批量删除
                </el-button>
                <ExportButton exportUrl="/TRiskAssessmentQuestion/Export" :where="searchForm"></ExportButton>
            </template>
            <template v-slot:Operate="scope">
                <el-button type="primary" size="default" class="margin-top-xs"
                    @click="ShowEditModal(scope.row.Id, false)">
                    <el-icon>
                        <Edit />
                    </el-icon>修 改
                </el-button>
                <el-button type="danger" size="default" class="margin-top-xs" @click="ShowDeleteModal(scope.row.Id)">
                    <el-icon>
                        <Delete />
                    </el-icon>删 除
                </el-button>
            </template>
        </PaginationTable>
    </div>
</template>

<script setup>
import { Post } from '@/api/http';
import { ColumnType } from '@/components/Tables/columnTypes';
import { useCommonStore } from "@/store";

import { Delete, Edit, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { onBeforeMount, computed, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();
const Token = computed(() => commonStore.Token)

// 表格条件
const where = reactive({});
// 搜索表单数据
const searchForm = reactive({});

// 编辑表单数据
const formData = reactive({
    IsDelete: 0,
    SortNum: 0,
    IsRequired: 1,
});

// 编辑对话框显示状态
const editorShow = ref(false);

// 表单引用
const editModalForm = ref(null);

// 表格列配置
const columnList = ref([
    {
        key: "Id",
        hidden: true,
    },
    {
        key: "QuestionnaireId",
        title: "问卷Id",
        type: ColumnType.SHORTTEXT,
    },
    {
        key: "DimensionCode",
        title: "维度编码",
        type: ColumnType.SHORTTEXT,
    },
    {
        key: "QuestionTitle",
        title: "题干",
        type: ColumnType.SHORTTEXT,
    },
    {
        key: "QuestionType",
        title: "题型(1单选/2多选/3是非/4填空)",
        type: ColumnType.SHORTTEXT,
    },
    {
        key: "IsRequired",
        title: "是否必答",
        type: ColumnType.SHORTTEXT,
    },
    {
        key: "SortNum",
        title: "排序值",
        type: ColumnType.SHORTTEXT,
    },
    {
        key: "IsDelete",
        title: "软删除标记",
        type: ColumnType.SHORTTEXT,
    },
    {
        title: "操作",
        width: "260px",
        key: "Operate",
        type: ColumnType.USERDEFINED,
    },
]);

// 表单验证规则
const editModalFormRules = reactive({
    "QuestionnaireId": [
        { required: true, message: '该项为必填项', trigger: 'blur' },
    ],
    "QuestionTitle": [
        { required: true, message: '该项为必填项', trigger: 'blur' },
    ],
    "QuestionType": [
        { required: true, message: '请选择题型', trigger: 'change' },
    ],
});

// 表格引用
const PaginationTableId = ref(null);

// 显示编辑对话框
const ShowEditModal = async (Id, isCreate) => {
    if (isCreate) {
        Object.keys(formData).forEach(k => delete formData[k]);
        formData.IsDelete = 0;
        formData.SortNum = 0;
        formData.IsRequired = 1;
        const qid = route.query.QuestionnaireId;
        if (qid) {
            formData.QuestionnaireId = Number(qid);
        }
    } else {
        const { Data } = await Post(`/TRiskAssessmentQuestion/Get`, { Id: Id });
        Object.assign(formData, Data || {});
    }

    editorShow.value = true;
};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const payload = { ...formData };
            const { Success } = await Post(`/TRiskAssessmentQuestion/CreateOrEdit`, payload);

            if (Success) {
                editorShow.value = false;
                PaginationTableId.value.Reload(searchForm);
                ElMessage.success('操作成功');
            }
        }
    });
};

// 搜索点击
const SearchClick = () => {
    Object.assign(where, searchForm);
    PaginationTableId.value.Reload(searchForm);
};

// 重置搜索条件
const ResetClick = () => {
    Object.keys(searchForm).forEach(key => {
        searchForm[key] = undefined;
    });
    Object.assign(where, searchForm);
    PaginationTableId.value.Reload(searchForm);
};

// 显示删除确认框
const ShowDeleteModal = async (Id) => {

    try {
        await ElMessageBox.confirm('确认删除该题目吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        const { Success } = await Post(`/TRiskAssessmentQuestion/Delete`, { Id: Id });
        if (Success) {
            PaginationTableId.value.Reload(searchForm);
            ElMessage.success('删除成功');
        }
    }
    catch (error) {
        ElMessage.warning('用户放弃了操作');
    }
};

// 批量删除
const BatchDelete = async () => {
    const ids = PaginationTableId.value.GetSelectionRow().map(x => x.Id);
    if (ids.length > 0) {
        try {
            await ElMessageBox.confirm('确认删除所选的题目吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })


            const { Success } = await Post(`/TRiskAssessmentQuestion/BatchDelete`, { Ids: ids });
            if (Success) {
                PaginationTableId.value.Reload(searchForm);
                ElMessage.success('删除成功');
            }
        }
        catch (error) {
            ElMessage.warning('用户放弃了操作');
        }
    }
    else {
        ElMessage.warning('请先选择要删除的行');
    }
};

onBeforeMount(() => {
    const qid = route.query.QuestionnaireId;
    if (qid) {
        searchForm.QuestionnaireId = Number(qid);
        where.QuestionnaireId = Number(qid);
    }
});
</script>

<style scoped>

</style>

