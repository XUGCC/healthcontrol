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
                    <el-form-item label="科普标题" prop="Title">
                        <el-input v-model.trim="searchForm.Title"  placeholder="请输入科普标题"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="创建人ID">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.CreatorId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="分类ID">
                        <SigleSelect url="/THealthScienceCategory/List" class="search-input" columnName="CategoryName" :clearable="true" columnValue="Id"
                            v-model="searchForm.CategoryId">
                        </SigleSelect>
                    </el-form-item>   
                    <el-form-item label="审核状态">
                        <el-select v-model="searchForm.AuditStatus" placeholder="全部">
                            <el-option :value="undefined" label="全部" />
                            <el-option v-for="item in auditStatusOptions" :key="item.value" :value="item.value" :label="item.label" />
                        </el-select>
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
        <el-dialog :title="formData.Id ? '修改健康科普' : '添加健康科普'" v-model="editorShow" width="50%" :lock-scroll="true"
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
                        <el-form-item label="分类ID" prop="CategoryId">
                          <SigleSelect url="/THealthScienceCategory/List" columnName="CategoryName" columnValue="Id"  v-model="formData.CategoryId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="科普标题" prop="Title">
                            <el-input type="text" v-model="formData.Title"  placeholder="请输入科普标题"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="封面图URL" prop="CoverUrl">
                           <UploadImages :limit="1" v-model="formData.CoverUrl"></UploadImages>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="科普内容" prop="ScienceContent">
                            <el-input type="textarea" :rows="5" v-model.trim="formData.ScienceContent"  placeholder="请输入科普内容"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="阅读量" prop="ReadCount">
                            <el-input-number  v-model="formData.ReadCount"  placeholder="请输入阅读量" :min="0" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="知识类型" prop="KnowledgeType">
                            <el-input-number  v-model="formData.KnowledgeType"  placeholder="请输入知识类型" :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="展示状态" prop="ShowStatus">
                            <el-input-number  v-model="formData.ShowStatus"  placeholder="0=隐藏，1=展示" :min="0" :max="1"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记" prop="IsDelete">
                            <el-input-number  v-model="formData.IsDelete"  placeholder="0=未删除，1=已删除" :min="0" :max="1"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="审核状态" prop="AuditStatus">
                            <el-select v-model="formData.AuditStatus" placeholder="请选择审核状态">
                                <el-option
                                    v-for="item in auditStatusOptions"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value"
                                />
                            </el-select>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24" v-if="formData.AuditStatus === 3">
                        <el-form-item label="驳回原因" prop="RejectReason">
                            <el-input
                                type="textarea"
                                :rows="3"
                                v-model.trim="formData.RejectReason"
                                placeholder="请输入驳回原因（将展示给作者）"
                            />
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
        <PaginationTable ref="PaginationTableId" url="/THealthScience/List" :column="columnList" :where="where">
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
                <ExportButton exportUrl="/THealthScience/Export" :where="searchForm"></ExportButton>        
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
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "CreatorId",
    hidden: true, 
  },
  {
    key: "CreatorDto.Name",
    title: "名称",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "CategoryId",
    hidden: true, 
  },
  {
    key: "CategoryDto.CategoryName",
    title: "分类名称：如声带保护/疾病科普",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Title",
    title: "科普标题",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "CoverUrl",
    title: "封面图URL",
    type:ColumnType.IMAGES,
  },
  {
    key: "ScienceContent",
    title: "科普内容",
    width: "140px",
    type:ColumnType.RICHTEXT,
  },
  {
    key: "ReadCount",
    title: "阅读量",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "KnowledgeType",
    title: "知识类型",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ShowStatus",
    title: "展示状态",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "AuditStatus",
    title: "审核状态",
    width: "120px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "RejectReason",
    title: "驳回原因",
    width: "200px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "IsDelete",
    title: "软删除标记",
    width: "160px",
        
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
"CategoryId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Title":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"CoverUrl":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ScienceContent":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"KnowledgeType":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ShowStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
 "AuditStatus":[
 { required: true, message: '请选择审核状态', trigger: 'change' },
               ],           
 });


// 表格引用
const PaginationTableId = ref(null);

// 审核状态选项：0=草稿，1=待审核，2=通过，3=驳回，4=下架/屏蔽
const auditStatusOptions = [
  { label: "草稿", value: 0 },
  { label: "待审核", value: 1 },
  { label: "通过", value: 2 },
  { label: "驳回", value: 3 },
  { label: "下架/屏蔽", value: 4 },
];

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/THealthScience/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/THealthScience/CreateOrEdit`, formData);

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
        const { Success } = await Post(`/THealthScience/Delete`, { Id: Id });
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


            const { Success } = await Post(`/THealthScience/BatchDelete`, { Ids: ids });
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
onBeforeMount(() => {
   
});
</script>

<style scoped>

</style>