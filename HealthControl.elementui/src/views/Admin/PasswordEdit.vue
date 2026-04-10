<template>
    <div>

        <el-card class="margin-top-sm">
            <template #header>
                <div class="clearfix">
                    <h2>修改密码</h2>
                </div>
            </template>
            <div>
                <el-form ref="editModalForm" :model="formData" label-width="80px" size="default" :rules="rules">
                    <el-form-item label="原始密码" prop="OrginPassword">
                        <el-input type="password" v-model.trim="formData.OrginPassword" placeholder="请输入原始密码"
                            show-password></el-input>
                    </el-form-item>

                    <el-form-item label="新密码" prop="NewPassword">
                        <el-input type="password" v-model.trim="formData.NewPassword" placeholder="请输入新密码"
                            show-password></el-input>
                    </el-form-item>

                    <el-form-item label="确认密码" prop="Password">
                        <el-input type="password" v-model.trim="formData.Password" placeholder="请再次输入新密码"
                            show-password></el-input>
                    </el-form-item>
                </el-form>
                <div class="button-container">
                    <el-button type="primary" size="default" @click="CreateOrEdit">确 定</el-button>
                </div>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { Post } from '@/api/http'
import { useCommonStore } from "@/store"
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

// 获取store和router实例
const commonStore = useCommonStore()
const router = useRouter()

// 表单数据
const formData = reactive({})
formData.Id = commonStore.UserId

// 表单校验规则
const rules = {
    OrginPassword: [
        { required: true, message: '该项为必填项', trigger: 'blur' }
    ],
    NewPassword: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {

                var reg = /^.{8,16}$/;
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入8-16位密码'));
                } else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
    Password: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
         {
            validator: (rule, value, callback) => {

                var reg = /^.{8,16}$/;
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入8-16位密码'));
                } else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ]
}

// 表单引用
const editModalForm = ref(null)

// 创建或修改方法
const CreateOrEdit = async () => {
    if (!editModalForm.value) return

    await editModalForm.value.validate(async (valid) => {
        if (valid) {
            // 判断新密码和确认密码是否一致
            if (formData.NewPassword !== formData.Password) {
                ElMessage.error("新密码和确认密码不一致")
                return false
            }
            const { Success } = await Post("/User/ChangePassword", formData)
            if (Success) {
                ElMessage.success("修改成功!")
                await commonStore.Logout()
                router.push("/Login")
            }
        } else {

            return false
        }
    })
}
</script>

<style scoped lang="scss">
/* 卡片样式 */
.margin-top-sm {
    margin-top: 20px;
}

/* 清除浮动 */
.clearfix::after {
    content: '';
    display: table;
    clear: both;
}

/* 按钮容器样式 */
.button-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
}
</style>