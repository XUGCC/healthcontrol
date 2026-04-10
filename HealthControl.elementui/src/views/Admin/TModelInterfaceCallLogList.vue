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
                    <el-form-item label="调用参数" prop="InputParams">
                        <el-input v-model.trim="searchForm.InputParams"  placeholder="请输入调用参数"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="模型版本" prop="ModelVersion">
                        <el-input v-model.trim="searchForm.ModelVersion"  placeholder="请输入模型版本号"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="服务名称" prop="ServiceName">
                        <el-input v-model.trim="searchForm.ServiceName"  placeholder="请输入服务名称/标识"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="返回结果摘要" prop="ResultSummary">
                        <el-input v-model.trim="searchForm.ResultSummary"  placeholder="请输入返回结果摘要"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="失败错误码" prop="FailErrorCode">
                        <el-input v-model.trim="searchForm.FailErrorCode"  placeholder="请输入失败错误码"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="失败详细信息" prop="FailDetailInfo">
                        <el-input v-model.trim="searchForm.FailDetailInfo"  placeholder="请输入失败详细信息"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="关联音频自查记录表主键">
                        <SigleSelect url="/TAudioScreenRecord/List" class="search-input" columnName="DensenetModelVersion" :clearable="true" columnValue="Id"
                            v-model="searchForm.DetectId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="结果更新时间" class="search-input">
                        <el-date-picker v-model="searchForm.UpdateTimeRange" type="datetimerange" start-placeholder="开始结果更新时间"
                            end-placeholder="结束结果更新时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
 							 <el-form-item label="调用环节" class="search-input">
                 		 <el-select v-model="searchForm.CallLink" class="search-input" :clearable="true" placeholder="请选择调用环节" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="模型接口类型" class="search-input">
                 		 <el-select v-model="searchForm.ModelInterfaceType" class="search-input" :clearable="true" placeholder="请选择模型接口类型" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="调用状态：0=失败，1=成功" class="search-input">
                 		 <el-select v-model="searchForm.CallStatus" class="search-input" :clearable="true" placeholder="请选择调用状态：0=失败，1=成功" >
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
        <el-dialog :title="formData.Id ? '修改模型接口调用日志' : '添加模型接口调用日志'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                    <el-col :span="24">
                        <el-form-item label="结果更新时间" prop="UpdateTime">
                            <el-date-picker v-model="formData.UpdateTime" align="right" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="关联音频自查记录表主键" prop="DetectId">
                          <SigleSelect url="/TAudioScreenRecord/List" columnName="AudioUrl" columnValue="Id"  v-model="formData.DetectId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="调用环节" prop="CallLink">
                       		  <el-switch v-model="formData.CallLink"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="模型接口类型" prop="ModelInterfaceType">
                       		  <el-switch v-model="formData.ModelInterfaceType"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="输入图谱URL" prop="InputSpectrumUrl">
                             <UploadFiles :limit="8"  v-model="formData.InputSpectrumUrl"></UploadFiles>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="调用参数" prop="InputParams">
                            <el-input type="text" v-model="formData.InputParams"  placeholder="请输入调用参数"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="调用耗时" prop="CallCost">
                            <el-input-number  v-model="formData.CallCost"  placeholder="请输入调用耗时"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="返回结果摘要" prop="ResultSummary">
                            <el-input type="text" v-model="formData.ResultSummary"  placeholder="请输入返回结果摘要"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="返回完整结果" prop="ResultComplete">
                            <el-input type="textarea" :rows="5" v-model.trim="formData.ResultComplete"  placeholder="请输入返回完整结果"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="调用状态：0=失败，1=成功" prop="CallStatus">
                       		  <el-switch v-model="formData.CallStatus"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="失败错误码" prop="FailErrorCode">
                            <el-input type="text" v-model="formData.FailErrorCode"  placeholder="请输入失败错误码"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="失败详细信息" prop="FailDetailInfo">
                            <el-input type="text" v-model="formData.FailDetailInfo"  placeholder="请输入失败详细信息"     :clearable="true"></el-input>
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
        <PaginationTable ref="PaginationTableId" url="/TModelInterfaceCallLog/List" :column="columnList" :where="where">
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
                <ExportButton exportUrl="/TModelInterfaceCallLog/Export" :where="searchForm"></ExportButton>        
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
    title: "结果更新时间",
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
    key: "CallLink",
    title: "调用环节",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "ModelInterfaceType",
    title: "模型接口类型",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "ModelVersion",
    title: "模型版本",
    width: "140px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "ServiceName",
    title: "服务名称",
    width: "160px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "InputSpectrumUrl",
    title: "输入图谱URL",
    width: "140px",
    type:ColumnType.FILESLINK,
  },
  {
    key: "InputParams",
    title: "调用参数",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "CallCost",
    title: "调用耗时",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ResultSummary",
    title: "返回结果摘要",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ResultComplete",
    title: "返回完整结果",
    width: "140px",
    type:ColumnType.RICHTEXT,
  },
  {
    key: "CallStatus",
    title: "调用状态：0=失败，1=成功",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "FailErrorCode",
    title: "失败错误码",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "FailDetailInfo",
    title: "失败详细信息",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
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
"DetectId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"CallLink":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ModelInterfaceType":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"InputSpectrumUrl":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"InputParams":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"CallCost":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ResultSummary":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ResultComplete":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"CallStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"FailErrorCode":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"FailDetailInfo":[
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

    const { Data } = await Post(`/TModelInterfaceCallLog/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/TModelInterfaceCallLog/CreateOrEdit`, formData);

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
        const { Success } = await Post(`/TModelInterfaceCallLog/Delete`, { Id: Id });
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


            const { Success } = await Post(`/TModelInterfaceCallLog/BatchDelete`, { Ids: ids });
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