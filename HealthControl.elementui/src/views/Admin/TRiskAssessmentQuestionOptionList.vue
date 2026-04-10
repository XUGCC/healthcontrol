<template>
    <div>
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
                    <el-form-item label="题目Id" prop="QuestionId">
                        <el-input-number v-model="searchForm.QuestionId" :min="1" :max="1000000" />
                    </el-form-item>
                    <el-form-item label="选项文本" prop="OptionText">
                        <el-input v-model.trim="searchForm.OptionText" placeholder="请输入选项内容" :clearable="true" />
                    </el-form-item>
                </el-form>
            </div>
        </el-card>

        <el-dialog :title="formData.Id ? '修改选项' : '添加选项'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData"
                label-width="140px" size="default">
                <el-row :gutter="10" class="edit-from-body">
                    <el-col :span="24">
                        <el-form-item label="题目Id" prop="QuestionId">
                            <el-input-number v-model="formData.QuestionId" :min="1" :max="1000000" />
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="选项文本" prop="OptionText">
                            <el-input type="text" v-model="formData.OptionText" placeholder="请输入选项内容"
                                :clearable="true" />
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="选项值" prop="OptionValue">
                            <el-input type="text" v-model="formData.OptionValue" placeholder="提交用值，建议唯一"
                                :clearable="true" />
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="得分" prop="ScoreValue">
                            <el-input-number v-model="formData.ScoreValue" :min="0" :max="1000" />
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

        <PaginationTable ref="PaginationTableId" url="/TRiskAssessmentQuestionOption/List" :column="columnList"
            :where="where">
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
                <ExportButton exportUrl="/TRiskAssessmentQuestionOption/Export" :where="searchForm"></ExportButton>
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

const where = reactive({});
const searchForm = reactive({});

const formData = reactive({
    IsDelete: 0,
    SortNum: 0,
    ScoreValue: 0,
});

const editorShow = ref(false);
const editModalForm = ref(null);

const columnList = ref([
    {
        key: "Id",
        hidden: true,
    },
    {
        key: "QuestionId",
        title: "题目Id",
        type: ColumnType.SHORTTEXT,
    },
    {
        key: "OptionText",
        title: "选项文本",
        type: ColumnType.SHORTTEXT,
    },
    {
        key: "OptionValue",
        title: "选项值",
        type: ColumnType.SHORTTEXT,
    },
    {
        key: "ScoreValue",
        title: "得分",
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

const editModalFormRules = reactive({
    "QuestionId": [
        { required: true, message: '该项为必填项', trigger: 'blur' },
    ],
    "OptionText": [
        { required: true, message: '该项为必填项', trigger: 'blur' },
    ],
});

const PaginationTableId = ref(null);

const ShowEditModal = async (Id, isCreate) => {
    if (isCreate) {
        Object.keys(formData).forEach(k => delete formData[k]);
        formData.IsDelete = 0;
        formData.SortNum = 0;
        formData.ScoreValue = 0;
        const qid = route.query.QuestionId;
        if (qid) {
            formData.QuestionId = Number(qid);
        }
    } else {
        const { Data } = await Post(`/TRiskAssessmentQuestionOption/Get`, { Id: Id });
        Object.assign(formData, Data || {});
    }
    editorShow.value = true;
};

const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const payload = { ...formData };
            const { Success } = await Post(`/TRiskAssessmentQuestionOption/CreateOrEdit`, payload);

            if (Success) {
                editorShow.value = false;
                PaginationTableId.value.Reload(searchForm);
                ElMessage.success('操作成功');
            }
        }
    });
};

const SearchClick = () => {
    Object.assign(where, searchForm);
    PaginationTableId.value.Reload(searchForm);
};

const ResetClick = () => {
    Object.keys(searchForm).forEach(key => {
        searchForm[key] = undefined;
    });
    Object.assign(where, searchForm);
    PaginationTableId.value.Reload(searchForm);
};

const ShowDeleteModal = async (Id) => {
    try {
        await ElMessageBox.confirm('确认删除该选项吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        const { Success } = await Post(`/TRiskAssessmentQuestionOption/Delete`, { Id: Id });
        if (Success) {
            PaginationTableId.value.Reload(searchForm);
            ElMessage.success('删除成功');
        }
    }
    catch (error) {
        ElMessage.warning('用户放弃了操作');
    }
};

const BatchDelete = async () => {
    const ids = PaginationTableId.value.GetSelectionRow().map(x => x.Id);
    if (ids.length > 0) {
        try {
            await ElMessageBox.confirm('确认删除所选的选项吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })


            const { Success } = await Post(`/TRiskAssessmentQuestionOption/BatchDelete`, { Ids: ids });
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
    const qid = route.query.QuestionId;
    if (qid) {
        searchForm.QuestionId = Number(qid);
        where.QuestionId = Number(qid);
    }
});
</script>

<style scoped>

</style>

