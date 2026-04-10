<template>
    <div class="login-container">
        <div class="login-background">
            <div class="background-pattern"></div>
            <div class="background-circles">
                <div class="circle circle-1"></div>
                <div class="circle circle-2"></div>
                <div class="circle circle-3"></div>
                <div class="circle circle-4"></div>
            </div>
            <div class="background-shapes">
                <div class="shape shape-1"></div>
                <div class="shape shape-2"></div>
                <div class="shape shape-3"></div>
            </div>
            <div class="medical-pattern">
                <svg class="pattern-svg" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <pattern id="medical-pattern" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
                            <circle cx="20" cy="20" r="2" fill="rgba(255, 255, 255, 0.1)"/>
                            <path d="M20 10 L20 30 M10 20 L30 20" stroke="rgba(255, 255, 255, 0.08)" stroke-width="1"/>
                        </pattern>
                    </defs>
                    <rect width="200" height="200" fill="url(#medical-pattern)"/>
                </svg>
            </div>
        </div>
        <div class="login-box">
            <div class="login-header">
                <div class="logo-section">
                    <div class="logo-icon">
                        <img src="@/assets/logo.png" alt="喉部健康自查" class="logo-image" />
                    </div>
                    <h1 class="system-title">喉部健康自查</h1>
                    <p class="system-subtitle">音频分析小程序管理端</p>
                </div>
            </div>
            
            <div class="login-form-wrapper">
                <el-form ref="loginForm" :model="formData" :rules="rules" class="login-form" label-position="top">
                    <el-form-item prop="UserName">
                        <template #label>
                            <span class="form-label">账号</span>
                        </template>
                        <el-input 
                            v-model="formData.UserName" 
                            placeholder="请输入账号"
                            size="large"
                            prefix-icon="User"
                            clearable
                        />
                        </el-form-item>

                    <el-form-item prop="Password">
                        <template #label>
                            <span class="form-label">密码</span>
                        </template>
                        <el-input 
                            v-model="formData.Password" 
                            type="password" 
                            show-password 
                            placeholder="请输入密码"
                            size="large"
                            prefix-icon="Lock"
                            clearable
                        />
                        </el-form-item>

                    <el-form-item prop="Code">
                        <template #label>
                            <span class="form-label">验证码</span>
                        </template>
                            <div class="code-container">
                            <el-input 
                                v-model="formData.Code" 
                                placeholder="请输入验证码"
                                size="large"
                                prefix-icon="Key"
                                class="code-input"
                            />
                                <div class="code-image">
                                    <ImageCode ref="validCodeRef" />
                                </div>
                            </div>
                        </el-form-item>

                        <el-form-item>
                        <el-button 
                            type="primary" 
                            class="login-button" 
                            @click="loginBtn"
                            size="large"
                            :loading="loading"
                        >
                                登 录
                            </el-button>
                        </el-form-item>
                    </el-form>

                <div class="login-footer">
                    <router-link :to="{ path: '/ForgetPassword' }" class="forget-link">
                        忘记密码？
                            </router-link>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { Post } from '@/api/http'
import ImageCode from '@/components/Identifyingcode/ImageCode.vue'
import { useCommonStore } from '@/store'
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

// 路由和状态管理
const router = useRouter()
const commonStore = useCommonStore()
// 表单引用
const loginForm = ref(null)
const validCodeRef = ref(null)
const loading = ref(false)

// 表单数据
const formData = reactive({
    UserName: '',
    Password: '',
    RoleType: '1',
    Code: ''
})

// 角色选项
const roleOptions = ref([])

// 表单校验规则
const rules = reactive({
    UserName: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 3, max: 20, message: '账号长度应在3到20个字符之间', trigger: 'blur' }
    ],
    Password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
    ],
    RoleType: [
        { required: true, message: '请选择角色', trigger: 'blur' },
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

// 组件挂载时获取角色类型
onMounted(() => {
    getRoleTypeApi()
})

// 获取角色类型
const getRoleTypeApi = async () => {
    try {
        const { Data: { Items } } = await Post('/Select/RoleType')
        roleOptions.value = Items
    } catch (error) {
        console.error('获取角色类型失败', error)
    }
}

// 登录按钮点击事件
const loginBtn = () => {
    loginForm.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                const { Success } = await commonStore.Login(formData)

                if (Success) {
                    ElMessage.success('登录成功')
                    router.push({
                        path: '/Admin'
                    })
                }
            } catch (error) {
                console.error('登录失败', error)
            } finally {
                loading.value = false
            }
        } else {
            ElMessage.error('登录验证不通过')
            return false
        }
    })
}


</script>

<style scoped lang="scss">
/* 登录页面容器 */
.login-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    padding: 20px;
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 50%, #11998e 100%);
    background-size: 200% 200%;
    animation: gradientShift 15s ease infinite;
    overflow: hidden;
}

@keyframes gradientShift {
    0% {
        background-position: 0% 50%;
    }
    50% {
        background-position: 100% 50%;
    }
    100% {
        background-position: 0% 50%;
    }
}

/* 背景装饰 */
.login-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    overflow: hidden;
    z-index: 0;
}

.background-pattern {
    position: absolute;
    width: 200%;
    height: 200%;
    background: 
        radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
        radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.12) 0%, transparent 50%),
        radial-gradient(circle at 40% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
        radial-gradient(circle at 60% 70%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
    animation: float 20s ease-in-out infinite;
}

/* 浮动圆圈 */
.background-circles {
    position: absolute;
    width: 100%;
    height: 100%;
}

.circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    animation: floatCircle 25s ease-in-out infinite;
}

.circle-1 {
    width: 300px;
    height: 300px;
    top: -100px;
    left: -100px;
    animation-delay: 0s;
}

.circle-2 {
    width: 200px;
    height: 200px;
    bottom: -50px;
    right: -50px;
    animation-delay: 5s;
}

.circle-3 {
    width: 150px;
    height: 150px;
    top: 50%;
    right: 10%;
    animation-delay: 10s;
}

.circle-4 {
    width: 180px;
    height: 180px;
    bottom: 20%;
    left: 5%;
    animation-delay: 15s;
}

@keyframes floatCircle {
    0%, 100% {
        transform: translate(0, 0) scale(1);
        opacity: 0.6;
    }
    33% {
        transform: translate(30px, -30px) scale(1.1);
        opacity: 0.8;
    }
    66% {
        transform: translate(-20px, 20px) scale(0.9);
        opacity: 0.7;
    }
}

/* 几何形状 */
.background-shapes {
    position: absolute;
    width: 100%;
    height: 100%;
}

.shape {
    position: absolute;
    background: rgba(255, 255, 255, 0.08);
    backdrop-filter: blur(5px);
    animation: rotateShape 30s linear infinite;
}

.shape-1 {
    width: 120px;
    height: 120px;
    top: 15%;
    left: 10%;
    clip-path: polygon(50% 0%, 0% 100%, 100% 100%);
    animation-delay: 0s;
}

.shape-2 {
    width: 100px;
    height: 100px;
    bottom: 25%;
    right: 15%;
    clip-path: polygon(25% 0%, 100% 0%, 75% 100%, 0% 100%);
    animation-delay: 10s;
}

.shape-3 {
    width: 80px;
    height: 80px;
    top: 60%;
    left: 20%;
    clip-path: polygon(50% 0%, 100% 50%, 50% 100%, 0% 50%);
    animation-delay: 20s;
}

@keyframes rotateShape {
    0% {
        transform: rotate(0deg) translate(0, 0);
    }
    50% {
        transform: rotate(180deg) translate(20px, -20px);
    }
    100% {
        transform: rotate(360deg) translate(0, 0);
    }
}

/* 医疗图案 */
.medical-pattern {
    position: absolute;
    width: 100%;
    height: 100%;
    opacity: 0.3;
    animation: patternMove 40s linear infinite;
}

.pattern-svg {
    width: 100%;
    height: 100%;
}

@keyframes patternMove {
    0% {
        transform: translate(0, 0);
    }
    100% {
        transform: translate(40px, 40px);
    }
}

@keyframes float {
    0%, 100% {
        transform: translate(0, 0) rotate(0deg);
    }
    50% {
        transform: translate(-50px, -50px) rotate(180deg);
    }
}

/* 登录框 */
.login-box {
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 440px;
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    overflow: hidden;
    animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 登录头部 */
.login-header {
    padding: 40px 40px 30px;
    text-align: center;
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    color: white;
    position: relative;
    overflow: hidden;

    &::before {
        content: '';
        position: absolute;
        top: -50%;
        left: -50%;
        width: 200%;
        height: 200%;
        background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
        animation: headerShine 8s ease-in-out infinite;
    }
}

@keyframes headerShine {
    0%, 100% {
        transform: translate(0, 0);
    }
    50% {
        transform: translate(50%, 50%);
    }
}

.logo-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
}

.logo-icon {
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(10px);
    padding: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    .logo-image {
    width: 100%;
        height: 100%;
        object-fit: contain;
        border-radius: 8px;
    }
}

.system-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
    letter-spacing: 0.5px;
}

.system-subtitle {
    font-size: 14px;
    margin: 0;
    opacity: 0.9;
    font-weight: 300;
}

/* 表单包装器 */
.login-form-wrapper {
    padding: 40px;
}

/* 表单标签 */
.form-label {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
}

/* 登录表单 */
.login-form {
    :deep(.el-form-item) {
        margin-bottom: 24px;
    }

    :deep(.el-form-item__label) {
        padding-bottom: 8px;
        line-height: 1.5;
    }

    :deep(.el-input__wrapper) {
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;

        &:hover {
            box-shadow: 0 4px 12px rgba(17, 153, 142, 0.15);
        }

        &.is-focus {
            box-shadow: 0 4px 12px rgba(17, 153, 142, 0.25);
        }
    }

    :deep(.el-input__inner) {
        font-size: 14px;
    }
}

/* 验证码容器 */
.code-container {
    display: flex;
    align-items: flex-start;
    gap: 12px;
}

.code-input {
    flex: 1;
}

.code-image {
    flex-shrink: 0;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 登录按钮 */
.login-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 500;
    border-radius: 8px;
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    border: none;
    margin-top: 8px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(17, 153, 142, 0.3);
    position: relative;
    overflow: hidden;

    &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
        transition: left 0.5s;
    }

    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(17, 153, 142, 0.4);

        &::before {
            left: 100%;
        }
    }

    &:active {
        transform: translateY(0);
    }
}

/* 登录底部 */
.login-footer {
    text-align: center;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
}

.forget-link {
    font-size: 14px;
    color: #11998e;
    text-decoration: none;
    transition: all 0.3s ease;
    display: inline-block;

    &:hover {
        color: #38ef7d;
        text-decoration: underline;
    }
}

/* 响应式调整 */
@media (max-width: 768px) {
    .login-container {
        padding: 10px;
    }

    .login-box {
        max-width: 100%;
        border-radius: 16px;
    }

    .login-header {
        padding: 30px 20px 20px;
    }

    .login-form-wrapper {
        padding: 30px 20px;
    }

    .system-title {
        font-size: 20px;
    }

    .system-subtitle {
        font-size: 12px;
    }
}

@media (max-width: 480px) {
    .login-header {
        padding: 24px 16px 16px;
    }

    .login-form-wrapper {
        padding: 24px 16px;
    }

    .logo-icon {
        width: 64px;
        height: 64px;
        padding: 6px;

        .logo-image {
            width: 100%;
            height: 100%;
        }
    }
}
</style>
