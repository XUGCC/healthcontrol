<template>
    <div class="register-container">
        <div class="register-box">
            <div class="register-left">
                <img class="register-image" src="@/assets/loginbg.png" alt="注册背景图">
            </div>
            <div class="register-right">
                <div class="register-form-container">
                    <h2 class="register-title">注册信息填写</h2>
                    <el-form ref="registerForm" :model="formData" label-width="90px" label-position="top" :rules="rules"
                        class="register-form">
                        <el-form-item label="账号" prop="UserName">
                            <el-input v-model="formData.UserName" placeholder="请输入账号" />
                        </el-form-item>

                        <el-form-item label="密码" prop="Password">
                            <el-input v-model="formData.Password" type="password" show-password placeholder="请输入密码" />
                        </el-form-item>

                        <el-form-item label="邮箱" prop="Email">
                            <el-input v-model="formData.Email" placeholder="请输入邮箱" />
                        </el-form-item>

                        <el-form-item label="联系方式" prop="PhoneNumber">
                            <el-input v-model="formData.PhoneNumber" placeholder="请输入联系方式" />
                        </el-form-item>

                        <el-form-item label="姓名" prop="Name">
                            <el-input v-model="formData.Name" placeholder="请输入姓名" />
                        </el-form-item>

                        <el-form-item label="验证码" prop="Code">
                            <div class="code-container">
                                <el-input v-model="formData.Code" placeholder="请输入验证码" />
                                <div class="code-image">
                                    <ValidCode ref="validCodeRef" />
                                </div>
                            </div>
                        </el-form-item>

                        <el-form-item>
                            <el-button type="primary" class="register-button" @click="registerBtn">
                                注 册
                            </el-button>
                        </el-form-item>
                    </el-form>

                    <div class="register-options">
                        <div class="login-link">
                            <span>如果有账号</span>
                            <router-link :to="{ path: '/Login' }">
                                <span class="link-text">去登录</span>
                            </router-link>
                        </div>
                        <div class="forget-link">
                            <router-link :to="{ path: '/ForgetPassword' }">
                                <span class="link-text">忘记密码</span>
                            </router-link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { Post } from '@/api/http'
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import ValidCode from '../components/Identifyingcode/ImageCode.vue'

// 路由
const router = useRouter()

// 表单引用
const registerForm = ref(null)
const validCodeRef = ref(null)

// 表单数据
const formData = reactive({
    UserName: '',
    Password: '',
    PhoneNumber: '',
    RoleType: '2',
    Email: '',
    Name: '',
    Code: ''
})

// 表单校验规则
const rules = reactive({
    UserName: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 3, max: 20, message: '账号长度应在3到20个字符之间', trigger: 'blur' }
    ],
    Password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
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
    Email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        {
            type: 'email',
            message: '请输入正确的邮箱格式',
            trigger: 'blur'
        },
        {
            validator: (rule, value, callback) => {
                const emailRegex = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
                if (!emailRegex.test(value)) {
                    callback(new Error('邮箱格式不正确，请输入有效的邮箱地址'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],
    Name: [
        { required: true, message: '请输入姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '姓名长度应在2到20个字符之间', trigger: 'blur' }
    ],
    PhoneNumber: [
        { required: true, message: '请输入联系方式', trigger: 'blur' },
        { min: 11, max: 11, message: '联系方式长度应为11个字符', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const phoneRegex = /^1[0-9]\d{9}$/
                if (!phoneRegex.test(value)) {
                    callback(new Error('请输入正确的手机号'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],
    Code: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const identifyCode = validCodeRef.value.getCode()
                if (value !== identifyCode) {
                    callback(new Error('请输入正确的验证码'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ]
})

// 注册按钮点击事件
const registerBtn = () => {
    registerForm.value.validate(async (valid) => {
        if (valid) {
            try {
                const { Success } = await Post('/User/Register', formData)
                if (Success) {
                    ElMessage.success('注册成功!')
                    router.push({
                        path: '/Login'
                    })
                }
            } catch (error) {
                console.error('注册失败', error)
                validCodeRef.value.refreshCode()
            }
        } else {
            ElMessage.error('注册验证不通过')
            validCodeRef.value.refreshCode()
            return false
        }
    })
}
</script>

<style scoped lang="scss">
/* 注册页面容器 */
.register-container {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 注册框 */
.register-box {
    display: flex;
    width: 1200px;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

/* 左侧图片区域 */
.register-left {
    flex: 1.8;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    overflow: hidden;
    position: relative;
}

.register-image {
    width: 100%;
    height: 100%;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}

/* 右侧表单区域 */
.register-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 40px;
}

.register-form-container {
    width: 100%;
}

/* 注册标题 */
.register-title {
    font-size: 24px;
    color: #303133;
    margin-bottom: 30px;
    text-align: center;
    font-weight: 600;
}

/* 注册表单 */
.register-form {
    margin-top: 20px;
}

/* 验证码容器 */
.code-container {
    display: flex;
    align-items: center;
    gap: 10px;
}

.code-image {
    flex-shrink: 0;
}

/* 注册按钮 */
.register-button {
    width: 100%;
    height: 40px;
    font-size: 16px;
    margin-top: 10px;
}

/* 注册选项区域 */
.register-options {
    display: flex;
    justify-content: space-between;
    margin-top: 20px;
    font-size: 14px;
    color: #606266;
}

/* 链接文本 */
.link-text {
    color: #409EFF;
    margin-left: 5px;
    cursor: pointer;
    transition: color 0.3s;

    &:hover {
        color: #66b1ff;
    }
}

/* 响应式调整 */
@media (max-width: 992px) {
    .register-box {
        width: 90%;
        height: auto;
        flex-direction: column;
    }

    .register-left {
        display: none;
    }

    .register-right {
        padding: 20px;
    }
}
</style>