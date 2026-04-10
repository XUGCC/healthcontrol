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
                    <el-form-item label="问卷标题" prop="QuestionnaireTitle">
                        <el-input v-model.trim="searchForm.QuestionnaireTitle"  placeholder="请输入问卷标题"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="问卷描述" prop="QuestionnaireDesc">
                        <el-input v-model.trim="searchForm.QuestionnaireDesc"  placeholder="请输入问卷描述"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="创建人ID">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.CreatorId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="更新时间" class="search-input">
                        <el-date-picker v-model="searchForm.UpdateTimeRange" type="datetimerange" start-placeholder="开始更新时间"
                            end-placeholder="结束更新时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
                </el-form>
            </div>
        </el-card>
<!-- 编辑对话框 -->
        <el-dialog :title="formData.Id ? '修改风险评估问卷' : '添加风险评估问卷'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                    <el-col :span="24">
                        <el-form-item label="更新时间" prop="UpdateTime">
                            <el-date-picker v-model="formData.UpdateTime" align="right" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="创建人ID" prop="CreatorId">
                          <SigleSelect url="/User/List" columnName="Name" columnValue="Id"  v-model="formData.CreatorId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="问卷标题" prop="QuestionnaireTitle">
                            <el-input type="text" v-model="formData.QuestionnaireTitle"  placeholder="请输入问卷标题"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="问卷描述" prop="QuestionnaireDesc">
                            <el-input type="text" v-model="formData.QuestionnaireDesc"  placeholder="请输入问卷描述"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="问题数量" prop="QuestionCount">
                            <el-input v-model="formData.QuestionCount" placeholder="自动统计" :disabled="true"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="展示状态" prop="ShowStatus">
                            <el-input-number v-model="formData.ShowStatus" placeholder="0=不展示,1=展示" :min="0" :max="1"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记" prop="IsDelete">
                            <el-input-number v-model="formData.IsDelete" placeholder="0=正常,1=已删除" :min="0" :max="1"></el-input-number>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row type="flex" justify="space-between" align="middle" class="margin-top-sm">
                    <el-col :span="12">
                        <el-button type="success" size="default" @click="GoQuestionManage" :disabled="!formData.Id">
                            管理该问卷题目
                        </el-button>
                        <span style="margin-left:8px;color:#999;">保存问卷后，在题目页配置题目和选项，数量自动统计。</span>
                    </el-col>
                    <el-col :span="12">
                        <el-row type="flex" justify="end">
                            <el-form-item>
                                <el-button size="default" type="primary" plain @click="CreateOrEditForm()">确 定</el-button>
                                <el-button size="default" @click="editorShow = false">取 消</el-button>
                            </el-form-item>
                        </el-row>
                    </el-col>
                </el-row>
            </el-form>
        </el-dialog>




        <!-- 数据表格 -->
        <PaginationTable ref="PaginationTableId" url="/TRiskAssessmentQuestionnaire/List" :column="columnList" :where="where">
            <template v-slot:header>
                <el-button type="primary" size="default" @click="ShowEditModal()">
                    <el-icon>
                        <Edit />
                    </el-icon>新 增
                </el-button>
                <el-button type="danger" size="default" @click="BatchDelete">
                    <el-icon>
                        <Delete />
                    </el-icon>批量删除
                </el-button>
                <ExportButton exportUrl="/TRiskAssessmentQuestionnaire/Export" :where="searchForm"></ExportButton>        
            </template>
            <template v-slot:Operate="scope">
              <el-button type="primary" size="default" class="margin-top-xs" @click="ShowEditModal(scope.row.Id)">
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
import { Post,PostSingleUpdate } from '@/api/http';
import { ColumnType } from '@/components/Tables/columnTypes';
import { useCommonStore } from "@/store";

import { Delete, Edit, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {onMounted,onBeforeMount,nextTick, watch,computed, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();
// 计算属性
const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)
  
//表格条件
const where = reactive({});
// 搜索表单数据
const searchForm = reactive({});

// 编辑表单数据
const formData = reactive({});

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
    key: "UpdateTime",
    title: "更新时间",
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "CreatorId",
    hidden: true, 
  },
  {
    key: "CreatorDto.Name",
    title: "名称",
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "QuestionnaireTitle",
    title: "问卷标题",
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "QuestionnaireDesc",
    title: "问卷描述",
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "QuestionCount",
    title: "问题数量",
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ShowStatus",
    title: "展示状态",
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "IsDelete",
    title: "软删除标记",
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    title: "操作",
    width:"300px",
    key: "Operate",
    type: ColumnType.USERDEFINED,
  },
            ]);

// 表单验证规则
const editModalFormRules = reactive({
"UpdateTime":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"CreatorId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"QuestionnaireTitle":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"QuestionnaireDesc":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
// 问题数量为自动统计字段，不再作为必填项
"ShowStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"IsDelete":[
              ],           
 });


// 表格引用
const PaginationTableId = ref(null);

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/TRiskAssessmentQuestionnaire/Get`, { Id: Id });
    
    Object.assign(formData, Data || {});

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            // 后端当前 UpdateTime 使用 LocalDateTime 反序列化，前端传 "YYYY-MM-DD HH:mm:ss" 会解析失败
            // 这里在提交前将 UpdateTime 置空，交由后端或数据库自行维护更新时间
            const payload = { ...formData };
            payload.UpdateTime = null;

            const { Success, Data } = await Post(`/TRiskAssessmentQuestionnaire/CreateOrEdit`, payload);

            if (Success) {
                if (Data) {
                    Object.assign(formData, Data);
                }
                editorShow.value = false;
                PaginationTableId.value.Reload(searchForm);
                ElMessage.success('操作成功');
            }
        }
    });
};

// 搜索点击
const SearchClick = () => {
    PaginationTableId.value.Reload(searchForm);
};

// 重置搜索条件
const ResetClick = () => {
    Object.keys(searchForm).forEach(key => {
        searchForm[key] = undefined;
    });
    PaginationTableId.value.Reload(searchForm);
};

// 显示删除确认框
const ShowDeleteModal = async (Id) => {

    try {
        await ElMessageBox.confirm('确认删除该信息吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        const { Success } = await Post(`/TRiskAssessmentQuestionnaire/Delete`, { Id: Id });
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
            await ElMessageBox.confirm('确认删除所选的行数据吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })


            const { Success } = await Post(`/TRiskAssessmentQuestionnaire/BatchDelete`, { Ids: ids });
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
        ElMessage.warning('用户放弃了操作');
    }
};

// 跳转题目管理页
const GoQuestionManage = () => {
    if (!formData.Id) {
        ElMessage.warning('请先保存问卷，再管理题目');
        return;
    }
    router.push({ path: '/Admin/TRiskAssessmentQuestionList', query: { QuestionnaireId: formData.Id } });
};
onBeforeMount(() => {
   
});
</script>

<style scoped>

</style>