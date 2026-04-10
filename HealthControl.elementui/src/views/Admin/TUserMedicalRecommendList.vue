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
                    <el-form-item label="关联用户表主键t_user.">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.UserId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="关联音频自查记录表主键">
                        <SigleSelect url="/TAudioScreenRecord/List" class="search-input" columnName="DensenetModelVersion" :clearable="true" columnValue="Id"
                            v-model="searchForm.DetectId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="关联医院医生表主键">
                        <SigleSelect url="/TOtolaryngologyHospitalDoctor/List" class="search-input" columnName="HospitalName" :clearable="true" columnValue="Id"
                            v-model="searchForm.RecommendHospitalId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="查看状态更新时间" class="search-input">
                        <el-date-picker v-model="searchForm.UpdateTimeRange" type="datetimerange" start-placeholder="开始查看状态更新时间"
                            end-placeholder="结束查看状态更新时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
                    <el-form-item label="系统推荐时间" class="search-input">
                        <el-date-picker v-model="searchForm.RecommendTimeRange" type="datetimerange" start-placeholder="开始系统推荐时间"
                            end-placeholder="结束系统推荐时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
 							 <el-form-item label="推荐依据风险等级" class="search-input">
                 		 <el-select v-model="searchForm.RiskLevel" class="search-input" :clearable="true" placeholder="请选择推荐依据风险等级" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="查看状态" class="search-input">
                 		 <el-select v-model="searchForm.ViewStatus" class="search-input" :clearable="true" placeholder="请选择查看状态" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="软删除标记" class="search-input">
                 		 <el-select v-model="searchForm.IsDelete" class="search-input" :clearable="true" placeholder="请选择软删除标记" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
                </el-form>
            </div>
        </el-card>
<!-- 编辑对话框 -->
        <el-dialog :title="formData.Id ? '修改用户就医推荐记录' : '添加用户就医推荐记录'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                    <el-col :span="24">
                        <el-form-item label="查看状态更新时间" prop="UpdateTime">
                            <el-date-picker v-model="formData.UpdateTime" align="right" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="关联用户表主键t_user." prop="UserId">
                          <SigleSelect url="/User/List" columnName="Name" columnValue="Id"  v-model="formData.UserId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="关联音频自查记录表主键" prop="DetectId">
                          <SigleSelect url="/TAudioScreenRecord/List" columnName="AudioUrl" columnValue="Id"  v-model="formData.DetectId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="推荐依据风险等级" prop="RiskLevel">
                       		  <el-switch v-model="formData.RiskLevel"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="关联医院医生表主键" prop="RecommendHospitalId">
                          <SigleSelect url="/TOtolaryngologyHospitalDoctor/List" columnName="HospitalName" columnValue="Id"  v-model="formData.RecommendHospitalId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="系统推荐时间" prop="RecommendTime">
                            <el-date-picker v-model="formData.RecommendTime" align="right" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="查看状态" prop="ViewStatus">
                       		  <el-switch v-model="formData.ViewStatus"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记" prop="IsDelete">
                       		  <el-switch v-model="formData.IsDelete"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
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
        <PaginationTable ref="PaginationTableId" url="/TUserMedicalRecommend/List" :column="columnList" :where="where">
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
                <ExportButton exportUrl="/TUserMedicalRecommend/Export" :where="searchForm"></ExportButton>        
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
    title: "查看状态更新时间",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "UserId",
    hidden: true, 
  },
  {
    key: "UserDto.Name",
    title: "名称",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "DetectId",
    hidden: true, 
  },
  {
    key: "DetectDto.AudioUrl",
    title: "音频文件URL",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "RiskLevel",
    title: "推荐依据风险等级",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "RecommendHospitalId",
    hidden: true, 
  },
  {
    key: "RecommendHospitalDto.HospitalName",
    title: "医院名称",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "RecommendTime",
    title: "系统推荐时间",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ViewStatus",
    title: "查看状态",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "IsDelete",
    title: "软删除标记",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
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
"UserId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DetectId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"RiskLevel":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"RecommendHospitalId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"RecommendTime":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ViewStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"IsDelete":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
 });


// 表格引用
const PaginationTableId = ref(null);

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/TUserMedicalRecommend/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/TUserMedicalRecommend/CreateOrEdit`, formData);

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
        const { Success } = await Post(`/TUserMedicalRecommend/Delete`, { Id: Id });
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


            const { Success } = await Post(`/TUserMedicalRecommend/BatchDelete`, { Ids: ids });
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