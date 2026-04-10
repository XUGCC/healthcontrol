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
                    <el-form-item label="医院名称" prop="HospitalName">
                        <el-input v-model.trim="searchForm.HospitalName"  placeholder="请输入医院名称"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="医生姓名" prop="DoctorName">
                        <el-input v-model.trim="searchForm.DoctorName"  placeholder="请输入医生姓名"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="所在地区：如北京市海淀区" prop="Region">
                        <el-input v-model.trim="searchForm.Region"  placeholder="请输入所在地区：如北京市海淀区"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="详细地址" prop="Address">
                        <el-input v-model.trim="searchForm.Address"  placeholder="请输入详细地址"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="联系电话" prop="ContactPhone">
                        <el-input v-model.trim="searchForm.ContactPhone"  placeholder="请输入联系电话"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="创建人ID">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.CreatorId">
                        </SigleSelect>
                    </el-form-item>                
 							 <el-form-item label="科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科" class="search-input">
                 		 <el-select v-model="searchForm.DepartmentType" class="search-input" :clearable="true" placeholder="请选择科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="展示状态：0=隐藏，1=展示" class="search-input">
                 		 <el-select v-model="searchForm.ShowStatus" class="search-input" :clearable="true" placeholder="请选择展示状态：0=隐藏，1=展示" >
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
        <el-dialog :title="formData.Id ? '修改耳鼻喉医院医生' : '添加耳鼻喉医院医生'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                    <el-col :span="24">
                        <el-form-item label="医院名称" prop="HospitalName">
                            <el-input type="text" v-model="formData.HospitalName"  placeholder="请输入医院名称"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="医生姓名" prop="DoctorName">
                            <el-input type="text" v-model="formData.DoctorName"  placeholder="请输入医生姓名"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科" prop="DepartmentType">
                       		  <el-switch v-model="formData.DepartmentType"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="所在地区：如北京市海淀区" prop="Region">
                            <el-input type="text" v-model="formData.Region"  placeholder="请输入所在地区：如北京市海淀区"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
  									 <el-form-item label="详细地址" prop="Address">
                            <el-input type="text" v-model="formData.Address" placeholder="请选择详细地址" :clearable="true" :disabled="true">
                                <template #append>
                                    <el-button type="primary" size="small" @click="AddressOpenLogLatSelectToast">选择地址</el-button>
                                </template>
                            </el-input>

                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="联系电话" prop="ContactPhone">
                            <el-input type="text" v-model="formData.ContactPhone"  placeholder="请输入联系电话"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="预约链接" prop="AppointmentUrl">
                             <UploadFiles :limit="8"  v-model="formData.AppointmentUrl"></UploadFiles>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="展示状态：0=隐藏，1=展示" prop="ShowStatus">
                       		  <el-switch v-model="formData.ShowStatus"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记：0=未删除，1=已删除" prop="IsDelete">
                       		  <el-switch v-model="formData.IsDelete"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="创建人ID" prop="CreatorId">
                          <SigleSelect url="/User/List" columnName="Name" columnValue="Id"  v-model="formData.CreatorId" >
                          </SigleSelect>
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
        <PaginationTable ref="PaginationTableId" url="/TOtolaryngologyHospitalDoctor/List" :column="columnList" :where="where">
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
                <ExportButton exportUrl="/TOtolaryngologyHospitalDoctor/Export" :where="searchForm"></ExportButton>        
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
        
 			 <LogLatSelectToast ref="AddressLogLatSelectToastRef"></LogLatSelectToast>
              
              
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
    key: "HospitalName",
    title: "医院名称",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "DoctorName",
    title: "医生姓名",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "DepartmentType",
    title: "科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "Region",
    title: "所在地区：如北京市海淀区",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Address",
    title: "详细地址",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ContactPhone",
    title: "联系电话",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "AppointmentUrl",
    title: "预约链接",
    width: "140px",
    type:ColumnType.FILESLINK,
  },
  {
    key: "ShowStatus",
    title: "展示状态：0=隐藏，1=展示",
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
    title: "操作",
    width:"300px",
    key: "Operate",
    type: ColumnType.USERDEFINED,
  },
            ]);

// 表单验证规则
const editModalFormRules = reactive({
"HospitalName":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DoctorName":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DepartmentType":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Region":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Address":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ContactPhone":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
{
  validator: (rule, value, callback) => 
  {
   var reg =/^1[34578]\d{9}$/;
   if (!value || !reg.test(value)) {
       callback(new Error('请输入正确的手机号'));
    }
    else {
       callback();
      }
   }, trigger: 'blur'
 },
              ],           
"AppointmentUrl":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ShowStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"IsDelete":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"CreatorId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
 });

// 地址选择弹窗引用
const AddressLogLatSelectToastRef = ref(null);

// 打开地址选择弹窗
const AddressOpenLogLatSelectToast = () => {
    AddressLogLatSelectToastRef.value.OpenToast(formData.Latitude, formData.Longitude, formData.Address, (data) => {
        formData.AddressLatitude = data.lat;
        formData.AddressLongitude = data.lng;
        formData.Address= data.fullAddress;
        formData.AddressProviceCityArea = data.province + " " + data.city + " " + data.district;
    });
};

// 表格引用
const PaginationTableId = ref(null);

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/TOtolaryngologyHospitalDoctor/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/TOtolaryngologyHospitalDoctor/CreateOrEdit`, formData);

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
        const { Success } = await Post(`/TOtolaryngologyHospitalDoctor/Delete`, { Id: Id });
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


            const { Success } = await Post(`/TOtolaryngologyHospitalDoctor/BatchDelete`, { Ids: ids });
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