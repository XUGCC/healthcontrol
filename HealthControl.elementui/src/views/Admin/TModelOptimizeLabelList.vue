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
                    <el-form-item label="医院确诊结果" prop="HospitalDiagnoseResult">
                        <el-input v-model.trim="searchForm.HospitalDiagnoseResult"  placeholder="请输入医院确诊结果"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="标注说明" prop="LabelDesc">
                        <el-input v-model.trim="searchForm.LabelDesc"  placeholder="请输入标注说明"  :clearable="true" ></el-input>
                    </el-form-item>
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
                    <el-form-item label="授权状态更新时间" class="search-input">
                        <el-date-picker v-model="searchForm.UpdateTimeRange" type="datetimerange" start-placeholder="开始授权状态更新时间"
                            end-placeholder="结束授权状态更新时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
 							 <el-form-item label="标注类型" class="search-input">
                 		 <el-select v-model="searchForm.LabelType" class="search-input" :clearable="true" placeholder="请选择标注类型" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="授权状态" class="search-input">
                 		 <el-select v-model="searchForm.AuthStatus" class="search-input" :clearable="true" placeholder="请选择授权状态" >
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
        <el-dialog :title="formData.Id ? '修改模型优化标注' : '添加模型优化标注'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                    <el-col :span="24">
                        <el-form-item label="授权状态更新时间" prop="UpdateTime">
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
                        <el-form-item label="医院确诊结果" prop="HospitalDiagnoseResult">
                            <el-input type="text" v-model="formData.HospitalDiagnoseResult"  placeholder="请输入医院确诊结果"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="标注类型" prop="LabelType">
                       		  <el-switch v-model="formData.LabelType"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="标注说明" prop="LabelDesc">
                            <el-input type="text" v-model="formData.LabelDesc"  placeholder="请输入标注说明"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>


                    <el-col :span="24">
                        <el-form-item label="授权状态" prop="AuthStatus">
                       		  <el-switch v-model="formData.AuthStatus"  :active-value="true" :inactive-value="false">
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




        <!-- 统计信息卡片 -->
        <el-card class="box-card stats-card" style="margin-top: 20px;">
            <div slot="header" class="clearfix">
                <span>标注统计</span>
            </div>
            <el-row :gutter="20" v-if="stats">
                <el-col :span="6">
                    <div class="stat-item">
                        <div class="stat-label">总标注数</div>
                        <div class="stat-value">{{ stats.total || 0 }}</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-item">
                        <div class="stat-label">已授权</div>
                        <div class="stat-value auth">{{ stats.authCount || 0 }}</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-item">
                        <div class="stat-label">未授权</div>
                        <div class="stat-value no-auth">{{ stats.noAuthCount || 0 }}</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-item">
                        <div class="stat-label">误报/确诊比例</div>
                        <div class="stat-value">{{ stats.falseAlarmRatio || '0%' }} / {{ stats.confirmedRatio || '0%' }}</div>
                    </div>
                </el-col>
            </el-row>
        </el-card>

        <!-- 数据表格 -->
        <PaginationTable ref="PaginationTableId" url="/TModelOptimizeLabel/List" :column="columnList" :where="where" @load="onTableLoad">
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
                <ExportButton exportUrl="/TModelOptimizeLabel/Export" :where="searchForm"></ExportButton>        
                <el-button type="success" size="default" @click="ExportAuthorizedOnly">
                    <el-icon>
                        <Download />
                    </el-icon>导出已授权标注
                </el-button>        
            </template>
            <template v-slot:IsReviewed="scope">
                <el-tag :type="getIsReviewed(scope.row) ? 'success' : 'info'" size="small">
                    {{ getIsReviewed(scope.row) ? '已审核' : '待审核' }}
                </el-tag>
            </template>
            <template v-slot:Operate="scope">
              <el-button type="primary" size="default" class="margin-top-xs" @click="ShowEditModal(scope.row.Id)">
                    <el-icon>
                        <Edit />
                    </el-icon>修 改
                </el-button>
                <el-button 
                    type="success" 
                    size="default" 
                    class="margin-top-xs" 
                    @click="MarkAsReviewed(scope.row)" 
                    v-if="!getIsReviewed(scope.row)"
                >
                    <el-icon>
                        <Check />
                    </el-icon>标记已审核
                </el-button>
                <el-button 
                    type="warning" 
                    size="default" 
                    class="margin-top-xs" 
                    @click="MarkAsReviewed(scope.row)" 
                    v-else
                >
                    <el-icon>
                        <Refresh />
                    </el-icon>取消审核
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

import { Delete, Edit, Refresh, Search, Download, Check } from '@element-plus/icons-vue';
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
    title: "授权状态更新时间",
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
    key: "HospitalDiagnoseResult",
    title: "医院确诊结果",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "LabelType",
    title: "标注类型",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "LabelDesc",
    title: "标注说明",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "AuthStatus",
    title: "授权状态",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "IsReviewed",
    title: "审核状态",
    width: "120px",
    type: ColumnType.USERDEFINED,
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
"HospitalDiagnoseResult":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"LabelType":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"LabelDesc":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"AuthStatus":[
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

// 计算统计信息
const calculateStats = async () => {
    try {
        // 获取所有未删除的标注数据（用于统计）
        // 注意：IsDelete需要传递布尔值false，而不是字符串
        const { Data, Success } = await Post('/TModelOptimizeLabel/List', {
            Page: 1,
            Limit: 10000, // 获取所有数据用于统计
            IsDelete: false  // 布尔值false，表示未删除
        });
        
        if (Success && Data && Data.Items) {
            const items = Data.Items;
            // 使用TotalCount而不是items.length，因为可能分页
            const total = Data.TotalCount || items.length;
            
            // 统计已授权和未授权数量（兼容多种数据类型）
            let authCount = 0;
            let noAuthCount = 0;
            let falseAlarmCount = 0;
            let confirmedCount = 0;
            
            items.forEach(item => {
                // 统计授权状态
                const auth = item.AuthStatus;
                if (auth === true || auth === 1 || auth === 'true' || auth === '1') {
                    authCount++;
                } else {
                    noAuthCount++;
                }
                
                // 统计标注类型
                const labelType = item.LabelType;
                if (labelType === false || labelType === 0 || labelType === 'false' || labelType === '0') {
                    falseAlarmCount++;
                } else if (labelType === true || labelType === 1 || labelType === 'true' || labelType === '1') {
                    confirmedCount++;
                }
            });
            
            stats.value = {
                total: total,
                authCount: authCount,
                noAuthCount: noAuthCount,
                falseAlarmRatio: total > 0 ? ((falseAlarmCount / total) * 100).toFixed(1) + '%' : '0%',
                confirmedRatio: total > 0 ? ((confirmedCount / total) * 100).toFixed(1) + '%' : '0%'
            };
        } else {
            // 如果没有数据，重置统计
            stats.value = {
                total: 0,
                authCount: 0,
                noAuthCount: 0,
                falseAlarmRatio: '0%',
                confirmedRatio: '0%'
            };
        }
    } catch (error) {
        console.error('计算统计信息失败:', error);
        // 出错时重置统计
        stats.value = {
            total: 0,
            authCount: 0,
            noAuthCount: 0,
            falseAlarmRatio: '0%',
            confirmedRatio: '0%'
        };
    }
};

// 表格加载完成事件
const onTableLoad = () => {
    calculateStats();
};

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/TModelOptimizeLabel/Get`, { Id: Id });
    
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/TModelOptimizeLabel/CreateOrEdit`, formData);

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
    calculateStats();
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
        const { Success } = await Post(`/TModelOptimizeLabel/Delete`, { Id: Id });
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


            const { Success } = await Post(`/TModelOptimizeLabel/BatchDelete`, { Ids: ids });
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

// 导出已授权标注
const ExportAuthorizedOnly = async () => {
    try {
        const exportForm = { ...searchForm, AuthStatus: true, IsDelete: false };
        const queryString = JSON.stringify(exportForm);
        const url = `/TModelOptimizeLabel/Export?query=${encodeURIComponent(queryString)}`;
        window.open(url, '_blank');
        ElMessage.success('正在导出已授权标注数据...');
    } catch (error) {
        ElMessage.error('导出失败：' + error.message);
    }
};

// 判断是否已审核（简化版本：基于UpdateTime判断，如果UpdateTime存在且不等于CreationTime，视为已审核）
const getIsReviewed = (row) => {
    if (!row.UpdateTime || !row.CreationTime) return false;
    // 如果UpdateTime和CreationTime不同，视为已审核（简化判断）
    return row.UpdateTime !== row.CreationTime;
};

// 标记为已审核/取消审核（简化版本：通过更新UpdateTime来标记）
const MarkAsReviewed = async (row) => {
    try {
        const isReviewed = getIsReviewed(row);
        const newStatus = !isReviewed;
        
        // 先获取完整的标注记录数据，确保所有字段都存在
        const { Data: fullData, Success: getSuccess } = await Post(`/TModelOptimizeLabel/Get`, { Id: row.Id });
        
        if (!getSuccess || !fullData) {
            ElMessage.error('获取标注记录失败，请刷新页面重试');
            return;
        }
        
        // 更新标注记录，通过修改UpdateTime来标记审核状态
        const updateData = {
            Id: fullData.Id,
            UserId: fullData.UserId,
            DetectId: fullData.DetectId,
            HospitalDiagnoseResult: fullData.HospitalDiagnoseResult || '',
            LabelType: fullData.LabelType !== null && fullData.LabelType !== undefined ? fullData.LabelType : false,
            LabelDesc: fullData.LabelDesc || '',
            AuthStatus: fullData.AuthStatus !== null && fullData.AuthStatus !== undefined ? fullData.AuthStatus : false,
            IsDelete: fullData.IsDelete !== null && fullData.IsDelete !== undefined ? fullData.IsDelete : false,
            UpdateTime: newStatus ? new Date().toISOString().replace('T', ' ').substring(0, 19) : (fullData.CreationTime || new Date().toISOString().replace('T', ' ').substring(0, 19))
        };
        
        console.log('审核更新数据:', updateData);
        
        const { Success, Msg } = await Post(`/TModelOptimizeLabel/CreateOrEdit`, updateData);
        if (Success) {
            ElMessage.success(newStatus ? '已标记为已审核' : '已取消审核标记');
            PaginationTableId.value.Reload(searchForm);
            calculateStats();
        } else {
            ElMessage.error(Msg || '操作失败');
        }
    } catch (error) {
        console.error('审核操作失败:', error);
        ElMessage.error('操作失败：' + (error.message || error.toString() || '未知错误'));
    }
};

onBeforeMount(() => {
    calculateStats();
});
</script>

<style scoped>
.stats-card {
    margin-bottom: 20px;
}

.stat-item {
    text-align: center;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
}

.stat-label {
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
}

.stat-value {
    font-size: 24px;
    font-weight: 600;
    color: #333;
}

.stat-value.auth {
    color: #52c41a;
}

.stat-value.no-auth {
    color: #999;
}
</style>