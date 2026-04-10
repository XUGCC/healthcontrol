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
 							 <el-form-item label="本地存储开启状态：0=否，1=是" class="search-input">
                 		 <el-select v-model="searchForm.LocalStorageStatus" class="search-input" :clearable="true" placeholder="请选择本地存储开启状态：0=否，1=是" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="数据匿名授权状态：0=否，1=是" class="search-input">
                 		 <el-select v-model="searchForm.DataAnonymousAuth" class="search-input" :clearable="true" placeholder="请选择数据匿名授权状态：0=否，1=是" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="隐私协议同意状态：0=否，1=是" class="search-input">
                 		 <el-select v-model="searchForm.PrivacyAgreeStatus" class="search-input" :clearable="true" placeholder="请选择隐私协议同意状态：0=否，1=是" >
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
        <el-dialog :title="formData.Id ? '修改用户隐私设置' : '添加用户隐私设置'" v-model="editorShow" width="50%" :lock-scroll="true"
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
                        <el-form-item label="本地存储开启状态：0=否，1=是" prop="LocalStorageStatus">
                       		  <el-switch v-model="formData.LocalStorageStatus"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="数据匿名授权状态：0=否，1=是" prop="DataAnonymousAuth">
                       		  <el-switch v-model="formData.DataAnonymousAuth"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="隐私协议同意状态：0=否，1=是" prop="PrivacyAgreeStatus">
                       		  <el-switch v-model="formData.PrivacyAgreeStatus"  :active-value="true" :inactive-value="false">
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




        <!-- 统计信息卡片（增强功能：总用户数、匿名授权数、隐私协议同意数等） -->
        <el-card class="box-card" style="margin-top: 20px;" v-if="stats">
            <div slot="header" class="clearfix">
                <span>隐私设置统计</span>
            </div>
            <el-row :gutter="20">
                <el-col :span="4">
                    <div class="stat-item">
                        <div class="stat-label">总用户数</div>
                        <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
                    </div>
                </el-col>
                <el-col :span="4">
                    <div class="stat-item">
                        <div class="stat-label">已同意隐私协议</div>
                        <div class="stat-value ok">{{ stats.privacyAgreed || 0 }}</div>
                    </div>
                </el-col>
                <el-col :span="4">
                    <div class="stat-item">
                        <div class="stat-label">已开启匿名授权</div>
                        <div class="stat-value ok">{{ stats.anonymousOn || 0 }}</div>
                    </div>
                </el-col>
                <el-col :span="4">
                    <div class="stat-item">
                        <div class="stat-label">开启自动清理</div>
                        <div class="stat-value warn">{{ stats.retentionOn || 0 }}</div>
                    </div>
                </el-col>
                <el-col :span="4">
                    <div class="stat-item">
                        <div class="stat-label">开启本地存储</div>
                        <div class="stat-value">{{ stats.localStorageOn || 0 }}</div>
                    </div>
                </el-col>
            </el-row>
        </el-card>

        <!-- 数据表格 -->
        <PaginationTable ref="PaginationTableId" url="/TUserPrivacySetting/List" :column="columnList" :where="where">
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
                <ExportButton exportUrl="/TUserPrivacySetting/Export" :where="searchForm"></ExportButton>        
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
    
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "LocalStorageStatus",
    title: "本地存储开启状态：0=否，1=是",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "DataAnonymousAuth",
    title: "数据匿名授权状态：0=否，1=是",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "PrivacyAgreeStatus",
    title: "隐私协议同意状态：0=否，1=是",
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
"LocalStorageStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"DataAnonymousAuth":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"PrivacyAgreeStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"IsDelete":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
 });


// 表格引用
const PaginationTableId = ref(null);

// 统计信息
const stats = ref(null);

// 加载统计信息（基于当前筛选条件的总体情况）
const LoadStats = async () => {
    try {
        // 这里不做分页限制，尽量获取全部满足条件的数据用于统计
        const { Data, Success } = await Post('/TUserPrivacySetting/List', {
            Page: 1,
            Limit: 10000,
            UserId: searchForm.UserId,
            LocalStorageStatus: searchForm.LocalStorageStatus,
            DataAnonymousAuth: searchForm.DataAnonymousAuth,
            PrivacyAgreeStatus: searchForm.PrivacyAgreeStatus,
            IsDelete: searchForm.IsDelete
        });

        if (Success && Data && Data.Items) {
            const items = Data.Items;
            const totalUsers = Data.TotalCount || items.length;

            let anonymousOn = 0;
            let privacyAgreed = 0;
            let retentionOn = 0;
            let localStorageOn = 0;

            items.forEach(it => {
                if (it.DataAnonymousAuth === true || it.DataAnonymousAuth === 1 || it.DataAnonymousAuth === 'true' || it.DataAnonymousAuth === '1') {
                    anonymousOn++;
                }
                if (it.PrivacyAgreeStatus === true || it.PrivacyAgreeStatus === 1 || it.PrivacyAgreeStatus === 'true' || it.PrivacyAgreeStatus === '1') {
                    privacyAgreed++;
                }
                if (it.DataRetentionEnabled === true || it.DataRetentionEnabled === 1 || it.DataRetentionEnabled === 'true' || it.DataRetentionEnabled === '1') {
                    retentionOn++;
                }
                if (it.LocalStorageStatus === true || it.LocalStorageStatus === 1 || it.LocalStorageStatus === 'true' || it.LocalStorageStatus === '1') {
                    localStorageOn++;
                }
            });

            stats.value = {
                totalUsers,
                anonymousOn,
                privacyAgreed,
                retentionOn,
                localStorageOn
            };
        } else {
            stats.value = {
                totalUsers: 0,
                anonymousOn: 0,
                privacyAgreed: 0,
                retentionOn: 0,
                localStorageOn: 0
            };
        }
    } catch (error) {
        console.error('加载隐私设置统计信息失败:', error);
        stats.value = {
            totalUsers: 0,
            anonymousOn: 0,
            privacyAgreed: 0,
            retentionOn: 0,
            localStorageOn: 0
        };
    }
};

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/TUserPrivacySetting/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/TUserPrivacySetting/CreateOrEdit`, formData);

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
    LoadStats();
};

// 重置搜索条件
const ResetClick = () => {
    Object.keys(searchForm).forEach(key => {
        searchForm[key] = undefined;
    });
    PaginationTableId.value.Reload(searchForm);
    LoadStats();
};

// 显示删除确认框
const ShowDeleteModal = async (Id) => {

    try {
        await ElMessageBox.confirm('确认删除该信息吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        const { Success } = await Post(`/TUserPrivacySetting/Delete`, { Id: Id });
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


            const { Success } = await Post(`/TUserPrivacySetting/BatchDelete`, { Ids: ids });
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
   LoadStats();
});
</script>

<style scoped>
.stat-item {
  padding: 12px 8px;
}
.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}
.stat-value.ok {
  color: #16a34a;
}
.stat-value.warn {
  color: #f97316;
}
</style>