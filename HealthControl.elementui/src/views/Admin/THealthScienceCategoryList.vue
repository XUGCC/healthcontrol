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
                    <el-form-item label="分类名称：如声带保护/疾病科普" prop="CategoryName">
                        <el-input v-model.trim="searchForm.CategoryName"  placeholder="请输入分类名称：如声带保护/疾病科普"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="分类描述" prop="CategoryDesc">
                        <el-input v-model.trim="searchForm.CategoryDesc"  placeholder="请输入分类描述"  :clearable="true" ></el-input>
                    </el-form-item>
 							 <el-form-item label="展示状态：0=隐藏，1=展示" class="search-input">
                 		 <el-select v-model="searchForm.ShowStatus" class="search-input" :clearable="true" placeholder="请选择展示状态：0=隐藏，1=展示" >
                          <el-option  key="1"  label="展示"  :value="1">
                          </el-option>
                           <el-option  key="0"  label="隐藏"  :value="0">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="软删除标记：0=未删除，1=已删除" class="search-input">
                 		 <el-select v-model="searchForm.IsDelete" class="search-input" :clearable="true" placeholder="请选择软删除标记：0=未删除，1=已删除" >
                          <el-option  key="1"  label="已删除"  :value="1">
                          </el-option>
                           <el-option  key="0"  label="未删除"  :value="0">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
                </el-form>
            </div>
        </el-card>
<!-- 编辑对话框 -->
        <el-dialog :title="formData.Id ? '修改健康科普分类' : '添加健康科普分类'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                    <el-col :span="24">
                        <el-form-item label="分类名称：如声带保护/疾病科普" prop="CategoryName">
                            <el-input type="text" v-model="formData.CategoryName"  placeholder="请输入分类名称：如声带保护/疾病科普"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="分类描述" prop="CategoryDesc">
                            <el-input type="text" v-model="formData.CategoryDesc"  placeholder="请输入分类描述"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="排序值：升序，值越小越靠前" prop="SortNum">
                            <el-input-number  v-model="formData.SortNum"  placeholder="请输入排序值：升序，值越小越靠前"     :clearable="true"  :min="1" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="展示状态：0=隐藏，1=展示" prop="ShowStatus">
                       		  <el-switch v-model="formData.ShowStatus"  :active-value="1" :inactive-value="0">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记：0=未删除，1=已删除" prop="IsDelete">
                       		  <el-switch v-model="formData.IsDelete"  :active-value="1" :inactive-value="0">
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
        <PaginationTable ref="PaginationTableId" url="/THealthScienceCategory/List" :column="columnList" :where="where">
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
                <ExportButton exportUrl="/THealthScienceCategory/Export" :where="searchForm"></ExportButton>        
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
    key: "CategoryName",
    title: "分类名称：如声带保护/疾病科普",
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "CategoryDesc",
    title: "分类描述",
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "SortNum",
    title: "排序值：升序，值越小越靠前",
    
    type: ColumnType.SHORTTEXT, 
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
    title: "操作",
    width:"300px",
    key: "Operate",
    type: ColumnType.USERDEFINED,
  },
            ]);

// 表单验证规则
const editModalFormRules = reactive({
"CategoryName":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"CategoryDesc":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"SortNum":[
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

    const { Data } = await Post(`/THealthScienceCategory/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/THealthScienceCategory/CreateOrEdit`, formData);

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
        const { Success } = await Post(`/THealthScienceCategory/Delete`, { Id: Id });
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


            const { Success } = await Post(`/THealthScienceCategory/BatchDelete`, { Ids: ids });
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