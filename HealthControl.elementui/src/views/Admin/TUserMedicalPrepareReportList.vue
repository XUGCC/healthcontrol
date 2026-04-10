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
                    <el-form-item label="关联检测" prop="DetectIdList">
                        <el-input v-model.trim="searchForm.DetectIdList"  placeholder="请输入关联检测"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="文档内容范围：如筛查记录+症状日志+饮食记录" prop="ContentScope">
                        <el-input v-model.trim="searchForm.ContentScope"  placeholder="请输入文档内容范围：如筛查记录+症状日志+饮食记录"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="生成失败原因" prop="GenerateFailReason">
                        <el-input v-model.trim="searchForm.GenerateFailReason"  placeholder="请输入生成失败原因"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="关联用户表主键t_user.">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.UserId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="关联健康档案表主键t_personal_laryngeal_health_record.">
                        <SigleSelect url="/TPersonalLaryngealHealthRecord/List" class="search-input" columnName="SymptomDuration" :clearable="true" columnValue="Id"
                            v-model="searchForm.HealthRecordId">
                        </SigleSelect>
                    </el-form-item>                
 							 <el-form-item label="文档模板类型：0=简版，1=详版" class="search-input">
                 		 <el-select v-model="searchForm.TemplateType" class="search-input" :clearable="true" placeholder="请选择文档模板类型：0=简版，1=详版" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="生成状态：0=待生成，1=生成中，2=已完成，3=生成失败" class="search-input">
                 		 <el-select v-model="searchForm.GenerateStatus" class="search-input" :clearable="true" placeholder="请选择生成状态：0=待生成，1=生成中，2=已完成，3=生成失败" >
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
        <el-dialog :title="formData.Id ? '修改用户就诊准备报告' : '添加用户就诊准备报告'" v-model="editorShow" width="50%" :lock-scroll="true"
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
                        <el-form-item label="关联健康档案表主键t_personal_laryngeal_health_record." prop="HealthRecordId">
                          <SigleSelect url="/TPersonalLaryngealHealthRecord/List" columnName="RecentSymptom" columnValue="Id"  v-model="formData.HealthRecordId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="关联检测" prop="DetectIdList">
                            <el-input type="text" v-model="formData.DetectIdList"  placeholder="请输入关联检测"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="文档模板类型：0=简版，1=详版" prop="TemplateType">
                       		  <el-switch v-model="formData.TemplateType"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="文档内容范围：如筛查记录+症状日志+饮食记录" prop="ContentScope">
                            <el-input type="text" v-model="formData.ContentScope"  placeholder="请输入文档内容范围：如筛查记录+症状日志+饮食记录"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="标准化文档URL" prop="StandardDocUrl">
                             <UploadFiles :limit="8"  v-model="formData.StandardDocUrl"></UploadFiles>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="生成状态：0=待生成，1=生成中，2=已完成，3=生成失败" prop="GenerateStatus">
                       		  <el-switch v-model="formData.GenerateStatus"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="生成失败原因" prop="GenerateFailReason">
                            <el-input type="text" v-model="formData.GenerateFailReason"  placeholder="请输入生成失败原因"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="累计查看次数" prop="ViewCount">
                            <el-input-number  v-model="formData.ViewCount"  placeholder="请输入累计查看次数"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="累计下载次数" prop="DownloadCount">
                            <el-input-number  v-model="formData.DownloadCount"  placeholder="请输入累计下载次数"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="累计分享次数" prop="ShareCount">
                            <el-input-number  v-model="formData.ShareCount"  placeholder="请输入累计分享次数"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
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
        <PaginationTable ref="PaginationTableId" url="/TUserMedicalPrepareReport/List" :column="columnList" :where="where">
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
                <ExportButton exportUrl="/TUserMedicalPrepareReport/Export" :where="searchForm"></ExportButton>        
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
    key: "HealthRecordId",
    hidden: true, 
  },
  {
    key: "HealthRecordDto.RecentSymptom",
    title: "近期主要不适症状",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "DetectIdList",
    title: "关联检测",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "TemplateType",
    title: "文档模板类型：0=简版，1=详版",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "ContentScope",
    title: "文档内容范围：如筛查记录+症状日志+饮食记录",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "StandardDocUrl",
    title: "标准化文档URL",
    width: "140px",
    type:ColumnType.FILESLINK,
  },
  {
    key: "GenerateStatus",
    title: "生成状态：0=待生成，1=生成中，2=已完成，3=生成失败",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "GenerateFailReason",
    title: "生成失败原因",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ViewCount",
    title: "累计查看次数",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "DownloadCount",
    title: "累计下载次数",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ShareCount",
    title: "累计分享次数",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
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
"HealthRecordId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DetectIdList":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"TemplateType":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ContentScope":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"StandardDocUrl":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"GenerateStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"GenerateFailReason":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ViewCount":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DownloadCount":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ShareCount":[
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

    const { Data } = await Post(`/TUserMedicalPrepareReport/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/TUserMedicalPrepareReport/CreateOrEdit`, formData);

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
        const { Success } = await Post(`/TUserMedicalPrepareReport/Delete`, { Id: Id });
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


            const { Success } = await Post(`/TUserMedicalPrepareReport/BatchDelete`, { Ids: ids });
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