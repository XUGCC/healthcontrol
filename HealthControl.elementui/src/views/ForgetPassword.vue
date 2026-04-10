<template>
        <div class="forget-container">
        <div class="forget-background">
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
                        <pattern id="medical-pattern-forget" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
                            <circle cx="20" cy="20" r="2" fill="rgba(255, 255, 255, 0.1)"/>
                            <path d="M20 10 L20 30 M10 20 L30 20" stroke="rgba(255, 255, 255, 0.08)" stroke-width="1"/>
                        </pattern>
                    </defs>
                    <rect width="200" height="200" fill="url(#medical-pattern-forget)"/>
                </svg>
            </div>
        </div>
            <div class="forget-box">
            <div class="forget-header">
                <div class="logo-section">
                    <div class="logo-icon">
                        <img src="@/assets/logo.png" alt="喉部健康自查" class="logo-image" />
                    </div>
                    <h1 class="system-title">找回密码</h1>
                    <p class="system-subtitle">请填写以下信息重置您的密码</p>
                </div>
            </div>
            
            <div class="forget-form-wrapper">
                <el-form ref="loginFormRef" :model="formData" :rules="rules" class="forget-form" label-position="top">
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

                    <el-form-item prop="Email">
                        <template #label>
                            <span class="form-label">邮箱</span>
                        </template>
                        <el-input 
                            v-model="formData.Email" 
                            placeholder="请输入邮箱"
                            size="large"
                            prefix-icon="Message"
                            clearable
                        />
                            </el-form-item>

                    <el-form-item prop="PhoneNumber">
                        <template #label>
                            <span class="form-label">联系方式</span>
                        </template>
                        <el-input 
                            v-model="formData.PhoneNumber" 
                            placeholder="请输入联系方式"
                            size="large"
                            prefix-icon="Phone"
                            clearable
                        />
                            </el-form-item>

                    <el-form-item prop="Password">
                        <template #label>
                            <span class="form-label">新密码</span>
                        </template>
                        <el-input 
                            type="password" 
                            v-model="formData.Password" 
                            show-password 
                            placeholder="请输入新密码（8-16位）"
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
                                        <ValidCode ref="validCodeRef" />
                                    </div>
                                </div>
                            </el-form-item>

                            <el-form-item>
                        <el-button 
                            type="primary" 
                            class="submit-btn" 
                            @click="handleForgetPassword"
                            size="large"
                            :loading="loading"
                        >
                                    确定
                                </el-button>
                            </el-form-item>
                        </el-form>

                <div class="forget-footer">
                    <span class="footer-text">如果有账号，</span>
                    <RouterLink to="/Login" class="login-link">
                        去登录
                            </RouterLink>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { Post } from '@/api/http';
import ValidCode from '@/components/Identifyingcode/ImageCode.vue';
import { ElMessage } from 'element-plus';
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

// 路由实例
const router = useRouter()

// 表单引用
const loginFormRef = ref(null)
const validCodeRef = ref(null)
const loading = ref(false)

// 表单数据
const formData = reactive({
    UserName: '',
    Password: '',
    Email: '',
    PhoneNumber: '',
    Code: ''
})

// 表单验证规则
const rules = {
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
        { required: true, message: '该项为必填项', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const reg = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入正确邮箱'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ],
    PhoneNumber: [
        { required: true, message: '该项为必填项', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const reg = /^1[123456789]\d{9}$/
                if (!value || !reg.test(value)) {
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
}

// 处理找回密码
const handleForgetPassword = async () => {
    if (!loginFormRef.value) return

    await loginFormRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                const res = await Post('/User/ForgetPassword', formData)
                if (res.Success) {
                    ElMessage.success('修改密码成功!')
                    router.push('/Login')
                } else {
                    validCodeRef.value.refreshCode()
                }
            } catch (error) {
                console.error(error)
                validCodeRef.value.refreshCode()
            } finally {
                loading.value = false
            }
        } else {
            ElMessage.error('验证不通过')
            validCodeRef.value.refreshCode()
        }
    })
}
</script>

<style scoped lang="scss">
/* 忘记密码页面容器 */
.forget-container {
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
.forget-background {
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

/* 找回密码框 */
.forget-box {
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 520px;
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

/* 头部区域 */
.forget-header {
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
.forget-form-wrapper {
    padding: 40px;
}

/* 表单标签 */
.form-label {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
}

/* 表单样式 */
.forget-form {
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

/* 提交按钮 */
.submit-btn {
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

/* 底部区域 */
.forget-footer {
    text-align: center;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
    font-size: 14px;
    color: #606266;
}

.footer-text {
    color: #606266;
}

.login-link {
    color: #11998e;
    text-decoration: none;
    transition: all 0.3s ease;
    margin-left: 4px;

    &:hover {
        color: #38ef7d;
        text-decoration: underline;
    }
}

/* 响应式调整 */
@media (max-width: 768px) {
    .forget-container {
        padding: 10px;
    }

    .forget-box {
        max-width: 100%;
        border-radius: 16px;
    }

    .forget-header {
        padding: 30px 20px 20px;
    }

    .forget-form-wrapper {
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
    .forget-header {
        padding: 24px 16px 16px;
    }

    .forget-form-wrapper {
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

