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
                    <el-form-item label="近期主要不适症状" prop="RecentSymptom">
                        <el-input v-model.trim="searchForm.RecentSymptom"  placeholder="请输入近期主要不适症状"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="症状持续时间：如1周/1月" prop="SymptomDuration">
                        <el-input v-model.trim="searchForm.SymptomDuration"  placeholder="请输入症状持续时间：如1周/1月"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="日常护嗓习惯" prop="DailyVoiceCare">
                        <el-input v-model.trim="searchForm.DailyVoiceCare"  placeholder="请输入日常护嗓习惯"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="健康趋势标签：如好转/恶化/稳定" prop="HealthTrendTag">
                        <el-input v-model.trim="searchForm.HealthTrendTag"  placeholder="请输入健康趋势标签：如好转/恶化/稳定"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="关联用户表主键t_user.">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.UserId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="关联最新检测">
                        <SigleSelect url="/TAudioScreenRecord/List" class="search-input" columnName="DensenetModelVersion" :clearable="true" columnValue="Id"
                            v-model="searchForm.LatestDetectId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="末次自查时间" class="search-input">
                        <el-date-picker v-model="searchForm.LastScreenTimeRange" type="datetimerange" start-placeholder="开始末次自查时间"
                            end-placeholder="结束末次自查时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
 							 <el-form-item label="风险评估等级：0=低，1=中，2=高" class="search-input">
                 		 <el-select v-model="searchForm.RiskAssessmentLevel" class="search-input" :clearable="true" placeholder="请选择风险评估等级：0=低，1=中，2=高" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="软删除标记：0=未删除，1=已删除" class="search-input">
                 		 <el-select v-model="searchForm.IsDelete" class="search-input" :clearable="true" placeholder="请选择软删除标记：0=未删除，1=已删除" >
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
        <el-dialog :title="formData.Id ? '修改个人喉部健康档案' : '添加个人喉部健康档案'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                    <el-col :span="24">
                        <el-form-item label="关联用户表主键t_user." prop="UserId">
                          <SigleSelect url="/User/List" columnName="Name" columnValue="Id"  v-model="formData.UserId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="近期主要不适症状" prop="RecentSymptom">
                            <el-input type="text" v-model="formData.RecentSymptom"  placeholder="请输入近期主要不适症状"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="症状持续时间：如1周/1月" prop="SymptomDuration">
                            <el-input type="text" v-model="formData.SymptomDuration"  placeholder="请输入症状持续时间：如1周/1月"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="日常护嗓习惯" prop="DailyVoiceCare">
                            <el-input type="text" v-model="formData.DailyVoiceCare"  placeholder="请输入日常护嗓习惯"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="末次自查时间" prop="LastScreenTime">
                            <el-date-picker v-model="formData.LastScreenTime" align="right" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="健康趋势标签：如好转/恶化/稳定" prop="HealthTrendTag">
                            <el-input type="text" v-model="formData.HealthTrendTag"  placeholder="请输入健康趋势标签：如好转/恶化/稳定"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="关联最新检测" prop="LatestDetectId">
                          <SigleSelect url="/TAudioScreenRecord/List" columnName="AudioUrl" columnValue="Id"  v-model="formData.LatestDetectId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="风险评估等级：0=低，1=中，2=高" prop="RiskAssessmentLevel">
                       		  <el-switch v-model="formData.RiskAssessmentLevel"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记：0=未删除，1=已删除" prop="IsDelete">
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
        <PaginationTable ref="PaginationTableId" url="/TPersonalLaryngealHealthRecord/List" :column="columnList" :where="where">
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
                <ExportButton exportUrl="/TPersonalLaryngealHealthRecord/Export" :where="searchForm"></ExportButton>        
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
    key: "RecentSymptom",
    title: "近期主要不适症状",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "SymptomDuration",
    title: "症状持续时间：如1周/1月",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "DailyVoiceCare",
    title: "日常护嗓习惯",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "LastScreenTime",
    title: "末次自查时间",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "HealthTrendTag",
    title: "健康趋势标签：如好转/恶化/稳定",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "LatestDetectId",
    hidden: true, 
  },
  {
    key: "LatestDetectDto.AudioUrl",
    title: "音频文件URL",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "RiskAssessmentLevel",
    title: "风险评估等级：0=低，1=中，2=高",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "IsDelete",
    title: "软删除标记：0=未删除，1=已删除",
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
"UserId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"RecentSymptom":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"SymptomDuration":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DailyVoiceCare":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"LastScreenTime":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"HealthTrendTag":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"LatestDetectId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"RiskAssessmentLevel":[
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

    const { Data } = await Post(`/TPersonalLaryngealHealthRecord/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/TPersonalLaryngealHealthRecord/CreateOrEdit`, formData);

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
        const { Success } = await Post(`/TPersonalLaryngealHealthRecord/Delete`, { Id: Id });
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


            const { Success } = await Post(`/TPersonalLaryngealHealthRecord/BatchDelete`, { Ids: ids });
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