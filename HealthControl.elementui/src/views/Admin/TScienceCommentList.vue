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
                    <el-form-item label="评论内容" prop="CommentContent">
                        <el-input v-model.trim="searchForm.CommentContent"  placeholder="请输入评论内容"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="回复内容" prop="ReplyContent">
                        <el-input v-model.trim="searchForm.ReplyContent"  placeholder="请输入回复内容"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="关联用户表主键t_user.">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.UserId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="关联科普表主键">
                        <SigleSelect url="/THealthScience/List" class="search-input" columnName="Title" :clearable="true" columnValue="Id"
                            v-model="searchForm.ScienceId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="父评论">
                        <SigleSelect url="/TScienceComment/List" class="search-input" columnName="CommentContent" :clearable="true" columnValue="Id"
                            v-model="searchForm.ParentCommentId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="回复审核时间" class="search-input">
                        <el-date-picker v-model="searchForm.UpdateTimeRange" type="datetimerange" start-placeholder="开始回复审核时间"
                            end-placeholder="结束回复审核时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
 							 <el-form-item label="审核状态" class="search-input">
                 		 <el-select v-model="searchForm.AuditStatus" class="search-input" :clearable="true" placeholder="请选择审核状态" >
                          <el-option  key="0"  label="待审"  :value="0">
                          </el-option>
                          <el-option  key="1"  label="通过"  :value="1">
                          </el-option>
                          <el-option  key="2"  label="驳回"  :value="2">
                          </el-option>
                          <el-option  key="3"  label="屏蔽"  :value="3">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="软删除标记" class="search-input">
                 		 <el-select v-model="searchForm.IsDelete" class="search-input" :clearable="true" placeholder="请选择软删除标记" >
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
        <el-dialog :title="formData.Id ? '修改科普评论' : '添加科普评论'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                    <el-col :span="24">
                        <el-form-item label="回复审核时间" prop="UpdateTime">
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
                        <el-form-item label="关联科普表主键" prop="ScienceId">
                          <SigleSelect url="/THealthScience/List" columnName="Title" columnValue="Id"  v-model="formData.ScienceId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="父评论" prop="ParentCommentId">
                          <SigleSelect url="/TScienceComment/List" columnName="CommentContent" columnValue="Id"  v-model="formData.ParentCommentId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="评论内容" prop="CommentContent">
                            <el-input type="text" v-model="formData.CommentContent"  placeholder="请输入评论内容"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="回复内容" prop="ReplyContent">
                            <el-input type="text" v-model="formData.ReplyContent"  placeholder="请输入回复内容"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="审核状态" prop="AuditStatus">
                          <el-select v-model="formData.AuditStatus" :clearable="true" placeholder="请选择审核状态">
                            <el-option key="0" label="待审" :value="0"></el-option>
                            <el-option key="1" label="通过" :value="1"></el-option>
                            <el-option key="2" label="驳回" :value="2"></el-option>
                            <el-option key="3" label="屏蔽" :value="3"></el-option>
                          </el-select>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="软删除标记" prop="IsDelete">
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
        <PaginationTable ref="PaginationTableId" url="/TScienceComment/List" :column="columnList" :where="where">
            <template v-slot:header>
                <el-button type="danger" size="default" @click="BatchDelete">
                    <el-icon>
                        <Delete />
                    </el-icon>批量删除
                </el-button>
                <ExportButton exportUrl="/TScienceComment/Export" :where="searchForm"></ExportButton>        
            </template>
            <template v-slot:Operate="scope">
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
    title: "回复审核时间",
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
    key: "ScienceId",
    hidden: true, 
  },
  {
    key: "ScienceDto.Title",
    title: "科普标题",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ParentCommentId",
    hidden: true, 
  },
  {
    key: "ParentCommentDto.CommentContent",
    title: "评论内容",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "CommentContent",
    title: "评论内容",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ReplyContent",
    title: "回复内容",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "LikeCount",
    title: "点赞数",
    width: "120px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "AuditStatus",
    title: "审核状态",
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
"ScienceId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ParentCommentId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"CommentContent":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ReplyContent":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"AuditStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"IsDelete":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
 });


// 表格引用
const PaginationTableId = ref(null);

// 评论目前仅支持删除操作，不提供在管理端新增/修改

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
        const { Success } = await Post(`/TScienceComment/Delete`, { Id: Id });
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


            const { Success } = await Post(`/TScienceComment/BatchDelete`, { Ids: ids });
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