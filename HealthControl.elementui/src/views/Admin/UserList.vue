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
            <div class="margin-top-xs">
                <el-form :inline="true" :model="searchForm" size="default">
                    <el-form-item label="账号">
                        <el-input v-model="searchForm.UserName" class="search-input" placeholder="请输入账号"
                            clearable></el-input>
                    </el-form-item>
                    <el-form-item label="姓名">
                        <el-input v-model="searchForm.Name" class="search-input" placeholder="请输入姓名" clearable></el-input>
                    </el-form-item>
                    <el-form-item label="邮箱">
                        <el-input v-model="searchForm.Email" class="search-input" placeholder="请输入邮箱" clearable></el-input>
                    </el-form-item>
                    <el-form-item label="手机号码">
                        <el-input v-model="searchForm.PhoneNumber" class="search-input" placeholder="请输入手机号码"
                            clearable></el-input>
                    </el-form-item>
                    <el-form-item label="角色">
                        <SigleSelect v-model="searchForm.RoleType" url="/Select/RoleType" columnName="Name"
                            columnValue="Code" columnLabel="Label"></SigleSelect>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>



        <!-- 编辑对话框 -->
        <el-dialog :title="formData.Id ? '修改账号' : '添加账号'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="800px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">
                <el-row :gutter="10" class="edit-from-body">
                    <el-col :span="24">
                        <el-form-item label="账号" prop="UserName" placeholder="请输入账号">
                            <el-input v-model.trim="formData.UserName"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24" v-if="!formData.Id">
                        <el-form-item label="密码" prop="Password">
                            <el-input type="password" v-model.trim="formData.Password"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="角色" prop="RoleType">
                            <SigleSelect :filterValue="[2]" v-model="formData.RoleType" url="/Select/RoleType"
                                columnName="Name" columnValue="Code" columnLabel="Label"></SigleSelect>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="邮箱" prop="Email" placeholder="请输入邮箱">
                            <el-input v-model.trim="formData.Email"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="联系方式" prop="PhoneNumber" placeholder="请输入联系方式">
                            <el-input v-model.trim="formData.PhoneNumber"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="出生时间" prop="Birth" placeholder="请输入出生时间">
                            <el-date-picker v-model="formData.Birth" align="right" type="date" placeholder="选择出生时间"
                                value-format="YYYY-MM-DD HH:mm:ss">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="姓名" prop="Name" placeholder="请输入姓名">
                            <el-input v-model.trim="formData.Name"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="图片">
                            <UploadImages :limit="1" v-model="formData.ImageUrls"></UploadImages>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row type="flex" justify="end" align="bottom">
                    <el-form-item>
                        <el-button type="primary" size="default" plain @click="CreateOrEditForm()">确 定</el-button>
                        <el-button size="default" @click="editorShow = false">取 消</el-button>
                    </el-form-item>
                </el-row>
            </el-form>
        </el-dialog>

        <!-- 重置密码对话框 -->
        <el-dialog title="重置密码" v-model="resetPasswordShow" width="40%" :lock-scroll="true">
            <el-form v-if="resetPasswordShow" ref="resetPasswordForm" :rules="resetPasswordRules" :model="resetPasswordData"
                label-width="120px" size="default">
                <el-form-item label="新密码" prop="Password">
                    <el-input type="password" v-model.trim="resetPasswordData.Password" placeholder="请输入新密码"></el-input>
                </el-form-item>
                <el-row type="flex" justify="end" align="bottom">
                    <el-form-item>
                        <el-button type="primary" size="default" plain @click="ConfirmResetPassword()">确 定</el-button>
                        <el-button size="default" @click="resetPasswordShow = false">取 消</el-button>
                    </el-form-item>
                </el-row>
            </el-form>
        </el-dialog>


        <!-- 数据表格 -->
        <PaginationTable ref="PaginationTableId" url="/User/List" :column="columnList">
            <template v-slot:header>
                <el-button type="primary" size="default" @click="ShowEditModal()">
                    <el-icon>
                        <Edit />
                    </el-icon>新 增
                </el-button>
                <ExportButton exportUrl="/User/Export" :where="searchForm"></ExportButton>
            </template>
            <template v-slot:Operate="scope">
                <el-button type="primary" size="default" class="margin-top-xs" @click="ShowEditModal(scope.row.Id)">
                    <el-icon>
                        <Edit />
                    </el-icon>修 改
                </el-button>
                <el-button type="primary" size="default" class="margin-top-xs" @click="ResetPassword(scope.row.Id)">
                    <el-icon>
                        <Edit />
                    </el-icon>重置密码
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
import { Post } from '@/api/http';
import { ColumnType } from '@/components/Tables/columnTypes';
import { useCommonStore } from "@/store";
import { Delete, Edit, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { reactive, ref } from 'vue';

const commonStore = useCommonStore();

// 搜索表单数据
const searchForm = reactive({});

// 编辑表单数据
const formData = reactive({});

// 编辑对话框显示状态
const editorShow = ref(false);

// 表单引用
const editModalForm = ref(null);

// 重置密码表单数据
const resetPasswordData = reactive({
    Id: null,
    Password: '123456'
});

// 重置密码对话框显示状态
const resetPasswordShow = ref(false);

// 重置密码表单引用
const resetPasswordForm = ref(null);

// 表格列配置
const columnList = ref([
    {
        key: "Id",
        hidden: true,
    },
    {
        key: "RoleType",
        hidden: true,
    },
    {
        key: "ImageUrls",
        title: "头像",

        type: ColumnType.IMAGES,
    },
    {
        key: "UserName",
        title: "账号",

        type: ColumnType.SHORTTEXT,
    },
    {
        key: "Name",
        title: "姓名",

        type: ColumnType.SHORTTEXT,
        template: function (row) {
            return window.NameFormat(row.Name);
        },
    },
    {
        key: "RoleTypeFormat",
        title: "所属角色",

        type: ColumnType.SHORTTEXT,
    },
    {
        key: "PhoneNumber",
        title: "手机号",

        type: ColumnType.PHONE,
        template: function (row) {
            return window.PhoneFormat(row.PhoneNumber);
        },
    },
    {
        key: "Email",
        title: "邮箱",
        width: "150px",
        type: ColumnType.SHORTTEXT,
        template: function (row) {
            return window.EmailFormat(row.Email);
        },
    },
    {
        key: "Birth",
        title: "出生年月",
        width: "120px",
        sort: true,
        type: ColumnType.DATE,
    },
    {
        title: "操作",
        width: "300px",
        key: "Operate",
        type: ColumnType.USERDEFINED,
    },
]);

// 表单验证规则
const editModalFormRules = {
    UserName: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
        { min: 3, max: 20, message: '账号长度应在3到20个字符之间', trigger: 'blur' }
    ],
    Password: [
        {
            validator: (rule, value, callback) => {
                //如果是新增则必填
                if (!formData.Id && !value) {
                    callback(new Error('该项为必填项'));
                    return;
                }
                var reg = /^.{8,16}$/;
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入8-16位密码'));
                } else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
    Email: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                var reg = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入正确的邮箱,必须包含@和.且@和.之间必须有内容'));
                } else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
    ImageUrls: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
    ],
    Name: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
    ],
    PhoneNumber: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                var reg = /^1[123456789]\d{9}$/;
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入正确的手机号,必须以1开头且长度为11位'));
                } else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
    Birth: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
    ],
    RoleType: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
    ],
};

// 重置密码表单验证规则
const resetPasswordRules = {
    Password: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应在6到20个字符之间', trigger: 'blur' }
    ],
};

// 表格引用
const PaginationTableId = ref(null);

// 显示重置密码对话框
const ResetPassword = (Id) => {
    resetPasswordData.Id = Id;
    resetPasswordData.Password = '123456';
    resetPasswordShow.value = true;
    // 清除表单验证状态
    if (resetPasswordForm.value) {
        resetPasswordForm.value.clearValidate();
    }
};

// 确认重置密码
const ConfirmResetPassword = async () => {
    if (!resetPasswordForm.value) return;

    await resetPasswordForm.value.validate(async valid => {
        if (valid) {
            try {
                const { Success } = await Post(`/User/ResetPassword`, resetPasswordData);
                if (Success) {
                    resetPasswordShow.value = false;
                    ElMessage.success('重置密码成功');
                }
            } catch (error) {
                ElMessage.error('重置密码失败');
            }
        }
    });
};

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/User/Get`, { Id: Id });
    Object.assign(formData, Data);

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/User/CreateOrEdit`, formData);

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
        const result = await ElMessageBox.confirm('确认删除该账号吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })

        const { Success } = await Post(`/User/Delete`, { Id: Id });
        if (Success) {
            PaginationTableId.value.Reload(searchForm);
            ElMessage.success('删除成功');

        }
    }
    catch (error) {
        ElMessage.warning('用户放弃了操作');
    }
};
</script>

<style scoped lang="scss">
/* 搜索表单样式 */
.box-card {
    margin-bottom: 20px;
}
</style>