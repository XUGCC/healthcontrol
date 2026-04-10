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
                    <el-form-item label="食物名称" prop="FoodName">
                        <el-input v-model.trim="searchForm.FoodName"  placeholder="请输入食物名称"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="食物别名" prop="FoodAlias">
                        <el-input v-model.trim="searchForm.FoodAlias"  placeholder="请输入食物别名"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="功效危害说明" prop="EffectHarmDesc">
                        <el-input v-model.trim="searchForm.EffectHarmDesc"  placeholder="请输入功效危害说明"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="忌口建议" prop="SuggestContent">
                        <el-input v-model.trim="searchForm.SuggestContent"  placeholder="请输入忌口建议"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="创建人ID">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.CreatorId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="分类ID">
                        <SigleSelect url="/TLaryngealFoodCategory/List" class="search-input" columnName="CategoryName" :clearable="true" columnValue="Id"
                            v-model="searchForm.CategoryId">
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
        <el-dialog :title="formData.Id ? '修改喉部食物库' : '添加喉部食物库'" v-model="editorShow" width="50%" :lock-scroll="true"
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
                          <SigleSelect url="/TLaryngealFoodCategory/List" columnName="CategoryName" columnValue="Id"  v-model="formData.CategoryId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="食物名称" prop="FoodName">
                            <el-input type="text" v-model="formData.FoodName"  placeholder="请输入食物名称"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="食物别名" prop="FoodAlias">
                            <el-input type="text" v-model="formData.FoodAlias"  placeholder="请输入食物别名"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="功效危害说明" prop="EffectHarmDesc">
                            <el-input type="text" v-model="formData.EffectHarmDesc"  placeholder="请输入功效危害说明"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="忌口建议" prop="SuggestContent">
                            <el-input type="text" v-model="formData.SuggestContent"  placeholder="请输入忌口建议"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="食物配图URL" prop="PicUrl">
                             <UploadFiles :limit="8"  v-model="formData.PicUrl"></UploadFiles>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="展示状态" prop="ShowStatus">
                            <el-switch
                                v-model="formData.ShowStatus"
                                :active-value="1"
                                :inactive-value="0"
                                active-text="展示"
                                inactive-text="隐藏"
                            />
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记" prop="IsDelete">
                            <el-switch
                                v-model="formData.IsDelete"
                                :active-value="1"
                                :inactive-value="0"
                                active-text="已删除"
                                inactive-text="未删除"
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
        <PaginationTable ref="PaginationTableId" url="/TLaryngealFood/List" :column="columnList" :where="where">
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
                <ExportButton exportUrl="/TLaryngealFood/Export" :where="searchForm"></ExportButton>        
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
    title: "分类名称：如润喉类/辛辣类",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "FoodName",
    title: "食物名称",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "FoodAlias",
    title: "食物别名",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "EffectHarmDesc",
    title: "功效危害说明",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "SuggestContent",
    title: "忌口建议",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "PicUrl",
    title: "食物配图URL",
    width: "140px",
    type:ColumnType.FILESLINK,
  },
  {
    key: "ShowStatus",
    title: "展示状态",
    width: "160px",
        
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
"FoodName":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"FoodAlias":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"EffectHarmDesc":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"SuggestContent":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"PicUrl":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ShowStatus":[
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

    const { Data } = await Post(`/TLaryngealFood/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/TLaryngealFood/CreateOrEdit`, formData);

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
        const { Success } = await Post(`/TLaryngealFood/Delete`, { Id: Id });
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


            const { Success } = await Post(`/TLaryngealFood/BatchDelete`, { Ids: ids });
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