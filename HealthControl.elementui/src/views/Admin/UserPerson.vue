<template>
    <div>
        <el-card>
            <div slot="header" class="clearfix">
                <h2>修改账号基础信息</h2>
            </div>
            <div class="margin-top-sm">
                <el-form ref="editModalForm" v-if="editShow" :model="formData" size="default" label-width="80px"
                    :rules="rules">
                    <el-form-item label="账号" prop="UserName">
                        <el-input v-model="formData.UserName" clearable :disabled="true"></el-input>
                    </el-form-item>

                    <el-form-item label="邮箱" prop="Email">
                        <el-input v-model="formData.Email" clearable> </el-input>
                    </el-form-item>
                    <el-form-item label="名称" prop="Name">
                        <el-input v-model="formData.Name" clearable></el-input>
                    </el-form-item>
                    <el-form-item label="手机号码" prop="PhoneNumber">
                        <el-input v-model="formData.PhoneNumber" clearable></el-input>
                    </el-form-item>
                    <el-form-item label="头像" prop="ImageUrls">
                        <UploadImages v-model="formData.ImageUrls"></UploadImages>
                    </el-form-item>

                    <el-form-item label="出生年月" prop="Birth">
                        <el-date-picker type="date" value-format="YYYY-MM-DD 00:00:00" placeholder="选择日期"
                            v-model="formData.Birth" clearable></el-date-picker>
                    </el-form-item>

                    <el-form-item>
                        <el-button type="primary" size="default" @click="CreateOrEdit">确 定</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { Post } from '@/api/http';
import { useCommonStore } from "@/store";
import { ElMessage } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';

// 获取store
const commonStore = useCommonStore()

// 表单显示状态
const editShow = ref(false)

// 表单数据
const formData = reactive({})

// 表单引用
const editModalForm = ref(null)

// 表单验证规则
const rules = {
    UserName: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 3, max: 20, message: '账号长度应在3到20个字符之间', trigger: 'blur' }
    ],
    Email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                var reg = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/;
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入正确邮箱'));
                }
                else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
    Name: [
        { required: true, message: '请输入名称', trigger: 'blur' },
    ],
    PhoneNumber: [
        { required: true, message: '请输入手机号码', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                var reg = /^1[123456789]\d{9}$/;
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入正确的手机号'));
                }
                else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
    Birth: [
        { required: true, message: '请输入出生年月', trigger: 'blur' },
    ],

}

// 获取账号信息
const ShowEditModal = async () => {
    const { Data } = await Post("/User/Get", { Id: commonStore.UserId })
    Object.assign(formData, Data)
    editShow.value = true
}

// 创建或者修改
const CreateOrEdit = async () => {
    if (!editModalForm.value) return

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post("/User/CreateOrEdit", formData)
            if (Success) {
                ElMessage.success("修改成功!")
                commonStore.GetInfo()
            }
        }
    })
}

// 页面加载时获取数据
onMounted(() => {
    ShowEditModal()
})
</script>

<style scoped lang="scss"></style>