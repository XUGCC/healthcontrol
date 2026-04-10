<template>
    <div class="admin-layout">
        <!-- 顶部导航栏 -->
        
        <el-header class="header">
            <div class="header-left">
                <div class="logo">
                    <div class="logo-image">
                        <img src="@/assets/logo.png" alt="系统logo">
                    </div>
                    <div class="logo-content">
                        <h1 class="system-title">喉部健康自查音频分析小程序管理端</h1>
                        <p class="system-subtitle">SpringBoot4 + Vue3</p>
                    </div>
                </div>
            </div>
            <div class="header-right">
                <el-dropdown>
                    <div class="user-info">
                        <el-avatar :size="32" :src="UserInfo.ImageUrls || defaultAvatar" />
                        <div class="user-details">
                            <span class="username">{{ UserInfo.UserName }}</span>
                            <span class="role">{{ RoleType }}</span>
                        </div>
                        <el-icon><arrow-down /></el-icon>
                    </div>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item @click="handleUserInfo">
                                <el-icon>
                                    <User />
                                </el-icon>个人信息
                            </el-dropdown-item>
                            <el-dropdown-item @click="handlePasswordEdit">
                                <el-icon>
                                    <Lock />
                                </el-icon>修改密码
                            </el-dropdown-item>
                            <el-dropdown-item @click="handleLogout">
                                <el-icon>
                                    <SwitchButton />
                                </el-icon>退出登录
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </el-header>
 			  
        <!-- 主体内容区 -->
        <el-container class="main-container">
            <!-- 侧边栏 -->
            <aside class="custom-sidebar">
                <nav class="custom-menu">
                    <!-- 控制台 -->
                    <div class="menu-item" :class="{ active: isActiveRoute('/Admin/Home') }"
                        @click="navigateTo('/Admin/Home')">
                        <div class="menu-content">
                            <span class="menu-icon">🏠</span>
                            <span class="menu-text">控制台</span>
                        </div>
                        <div class="menu-indicator"></div>
                    </div>

                    <!-- 用户管理 -->
                    <div class="menu-group">
                        <div class="menu-item submenu-header" :class="{ expanded: expandedMenus.userManagement }"
                            @click="toggleSubmenu('userManagement')">
                            <div class="menu-content">
                                <span class="menu-icon">👥</span>
                                <span class="menu-text">用户管理</span>
                                <span class="expand-icon" :class="{ rotated: expandedMenus.userManagement }">▶</span>
                            </div>
                        </div>
                        <transition name="submenu">
                            <div v-show="expandedMenus.userManagement" class="submenu-container">
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/UserList') }"
                                    @click="navigateTo('/Admin/UserList')">
                                    <span class="submenu-icon">👤</span>
                                    <span class="submenu-text">用户表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TUserPrivacySettingList') }"
                                    @click="navigateTo('/Admin/TUserPrivacySettingList')">
                                    <span class="submenu-icon">🔒</span>
                                    <span class="submenu-text">用户隐私设置表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/DataExportRequestList') }"
                                    @click="navigateTo('/Admin/DataExportRequestList')">
                                    <span class="submenu-icon">📤</span>
                                    <span class="submenu-text">数据导出申请表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/DataDeleteRequestList') }"
                                    @click="navigateTo('/Admin/DataDeleteRequestList')">
                                    <span class="submenu-icon">🗑️</span>
                                    <span class="submenu-text">数据删除申请表</span>
                                </div>
                            </div>
                        </transition>
                    </div>

                    <!-- 音频自查分析管理 -->
                    <div class="menu-group">
                        <div class="menu-item submenu-header" :class="{ expanded: expandedMenus.audioAnalysis }"
                            @click="toggleSubmenu('audioAnalysis')">
                            <div class="menu-content">
                                <span class="menu-icon">🎤</span>
                                <span class="menu-text">音频自查分析管理</span>
                                <span class="expand-icon" :class="{ rotated: expandedMenus.audioAnalysis }">▶</span>
                            </div>
                        </div>
                        <transition name="submenu">
                            <div v-show="expandedMenus.audioAnalysis" class="submenu-container">
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TAudioScreenRecordList') }"
                                    @click="navigateTo('/Admin/TAudioScreenRecordList')">
                                    <span class="submenu-icon">📊</span>
                                    <span class="submenu-text">音频自查记录表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TModelInterfaceCallLogList') }"
                                    @click="navigateTo('/Admin/TModelInterfaceCallLogList')">
                                    <span class="submenu-icon">📝</span>
                                    <span class="submenu-text">模型接口调用日志表</span>
                                </div>
                            </div>
                        </transition>
                    </div>

                    <!-- 健康档案与筛查追踪管理 -->
                    <div class="menu-group">
                        <div class="menu-item submenu-header" :class="{ expanded: expandedMenus.healthRecord }"
                            @click="toggleSubmenu('healthRecord')">
                            <div class="menu-content">
                                <span class="menu-icon">📋</span>
                                <span class="menu-text">健康档案与筛查追踪管理</span>
                                <span class="expand-icon" :class="{ rotated: expandedMenus.healthRecord }">▶</span>
                            </div>
                        </div>
                        <transition name="submenu">
                            <div v-show="expandedMenus.healthRecord" class="submenu-container">
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TPersonalLaryngealHealthRecordList') }"
                                    @click="navigateTo('/Admin/TPersonalLaryngealHealthRecordList')">
                                    <span class="submenu-icon">📁</span>
                                    <span class="submenu-text">个人喉部健康档案表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TSymptomLogList') }"
                                    @click="navigateTo('/Admin/TSymptomLogList')">
                                    <span class="submenu-icon">📈</span>
                                    <span class="submenu-text">症状日志表</span>
                                </div>
                            </div>
                        </transition>
                    </div>

                    <!-- 健康科普管理 -->
                    <div class="menu-group">
                        <div class="menu-item submenu-header" :class="{ expanded: expandedMenus.healthScience }"
                            @click="toggleSubmenu('healthScience')">
                            <div class="menu-content">
                                <span class="menu-icon">📚</span>
                                <span class="menu-text">健康科普管理</span>
                                <span class="expand-icon" :class="{ rotated: expandedMenus.healthScience }">▶</span>
                            </div>
                        </div>
                        <transition name="submenu">
                            <div v-show="expandedMenus.healthScience" class="submenu-container">
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/THealthScienceCategoryList') }"
                                    @click="navigateTo('/Admin/THealthScienceCategoryList')">
                                    <span class="submenu-icon">🏷️</span>
                                    <span class="submenu-text">健康科普分类表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/THealthScienceList') }"
                                    @click="navigateTo('/Admin/THealthScienceList')">
                                    <span class="submenu-icon">📄</span>
                                    <span class="submenu-text">健康科普表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TScienceLikeList') }"
                                    @click="navigateTo('/Admin/TScienceLikeList')">
                                    <span class="submenu-icon">👍</span>
                                    <span class="submenu-text">科普点赞表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TScienceCollectList') }"
                                    @click="navigateTo('/Admin/TScienceCollectList')">
                                    <span class="submenu-icon">⭐</span>
                                    <span class="submenu-text">科普收藏表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TScienceCommentList') }"
                                    @click="navigateTo('/Admin/TScienceCommentList')">
                                    <span class="submenu-icon">💬</span>
                                    <span class="submenu-text">科普评论表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TScienceCommentLikeList') }"
                                    @click="navigateTo('/Admin/TScienceCommentLikeList')">
                                    <span class="submenu-icon">❤️</span>
                                    <span class="submenu-text">科普评论点赞表</span>
                                </div>
                            </div>
                        </transition>
                    </div>

                    <!-- 问卷管理 -->
                    <div class="menu-group">
                        <div class="menu-item submenu-header" :class="{ expanded: expandedMenus.questionnaire }"
                            @click="toggleSubmenu('questionnaire')">
                            <div class="menu-content">
                                <span class="menu-icon">📝</span>
                                <span class="menu-text">问卷管理</span>
                                <span class="expand-icon" :class="{ rotated: expandedMenus.questionnaire }">▶</span>
                            </div>
                        </div>
                        <transition name="submenu">
                            <div v-show="expandedMenus.questionnaire" class="submenu-container">
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TRiskAssessmentQuestionnaireList') }"
                                    @click="navigateTo('/Admin/TRiskAssessmentQuestionnaireList')">
                                    <span class="submenu-icon">📋</span>
                                    <span class="submenu-text">风险评估问卷表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TRiskAssessmentQuestionList') }"
                                    @click="navigateTo('/Admin/TRiskAssessmentQuestionList')">
                                    <span class="submenu-icon">❓</span>
                                    <span class="submenu-text">风险评估题目表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TRiskAssessmentQuestionOptionList') }"
                                    @click="navigateTo('/Admin/TRiskAssessmentQuestionOptionList')">
                                    <span class="submenu-icon">🔢</span>
                                    <span class="submenu-text">风险评估题目选项表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TUserQuestionnaireAnswerList') }"
                                    @click="navigateTo('/Admin/TUserQuestionnaireAnswerList')">
                                    <span class="submenu-icon">✅</span>
                                    <span class="submenu-text">用户问卷答题表</span>
                                </div>
                            </div>
                        </transition>
                    </div>

                    <!-- 饮食管理 -->
                    <div class="menu-group">
                        <div class="menu-item submenu-header" :class="{ expanded: expandedMenus.diet }"
                            @click="toggleSubmenu('diet')">
                            <div class="menu-content">
                                <span class="menu-icon">🍎</span>
                                <span class="menu-text">饮食管理</span>
                                <span class="expand-icon" :class="{ rotated: expandedMenus.diet }">▶</span>
                            </div>
                        </div>
                        <transition name="submenu">
                            <div v-show="expandedMenus.diet" class="submenu-container">
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TLaryngealFoodCategoryList') }"
                                    @click="navigateTo('/Admin/TLaryngealFoodCategoryList')">
                                    <span class="submenu-icon">🏷️</span>
                                    <span class="submenu-text">喉部食物库分类表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TLaryngealFoodList') }"
                                    @click="navigateTo('/Admin/TLaryngealFoodList')">
                                    <span class="submenu-icon">🥗</span>
                                    <span class="submenu-text">喉部食物库表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TUserDietRecordList') }"
                                    @click="navigateTo('/Admin/TUserDietRecordList')">
                                    <span class="submenu-icon">📊</span>
                                    <span class="submenu-text">用户饮食记录表</span>
                                </div>
                            </div>
                        </transition>
                    </div>

                    <!-- 就医辅助管理 -->
                    <div class="menu-group">
                        <div class="menu-item submenu-header" :class="{ expanded: expandedMenus.medicalAssist }"
                            @click="toggleSubmenu('medicalAssist')">
                            <div class="menu-content">
                                <span class="menu-icon">🏥</span>
                                <span class="menu-text">就医辅助管理</span>
                                <span class="expand-icon" :class="{ rotated: expandedMenus.medicalAssist }">▶</span>
                            </div>
                        </div>
                        <transition name="submenu">
                            <div v-show="expandedMenus.medicalAssist" class="submenu-container">
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TOtolaryngologyHospitalDoctorList') }"
                                    @click="navigateTo('/Admin/TOtolaryngologyHospitalDoctorList')">
                                    <span class="submenu-icon">👨‍⚕️</span>
                                    <span class="submenu-text">耳鼻喉医院医生表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TUserMedicalRecommendList') }"
                                    @click="navigateTo('/Admin/TUserMedicalRecommendList')">
                                    <span class="submenu-icon">💡</span>
                                    <span class="submenu-text">用户就医推荐记录表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TUserMedicalPrepareReportList') }"
                                    @click="navigateTo('/Admin/TUserMedicalPrepareReportList')">
                                    <span class="submenu-icon">📄</span>
                                    <span class="submenu-text">用户就诊准备报告表</span>
                                </div>
                            </div>
                        </transition>
                    </div>

                    <!-- 技术扩展管理 -->
                    <div class="menu-group">
                        <div class="menu-item submenu-header" :class="{ expanded: expandedMenus.techExtension }"
                            @click="toggleSubmenu('techExtension')">
                            <div class="menu-content">
                                <span class="menu-icon">🔧</span>
                                <span class="menu-text">技术扩展管理</span>
                                <span class="expand-icon" :class="{ rotated: expandedMenus.techExtension }">▶</span>
                            </div>
                        </div>
                        <transition name="submenu">
                            <div v-show="expandedMenus.techExtension" class="submenu-container">
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TLaryngoscopePhotoList') }"
                                    @click="navigateTo('/Admin/TLaryngoscopePhotoList')">
                                    <span class="submenu-icon">📷</span>
                                    <span class="submenu-text">喉镜照片记录表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TModelOptimizeLabelList') }"
                                    @click="navigateTo('/Admin/TModelOptimizeLabelList')">
                                    <span class="submenu-icon">🏷️</span>
                                    <span class="submenu-text">模型优化标注表</span>
                                </div>
                            </div>
                        </transition>
                    </div>

                    <!-- 个性化健康服务管理 -->
                    <div class="menu-group">
                        <div class="menu-item submenu-header" :class="{ expanded: expandedMenus.personalizedHealth }"
                            @click="toggleSubmenu('personalizedHealth')">
                            <div class="menu-content">
                                <span class="menu-icon">💚</span>
                                <span class="menu-text">个性化健康服务管理</span>
                                <span class="expand-icon" :class="{ rotated: expandedMenus.personalizedHealth }">▶</span>
                            </div>
                        </div>
                        <transition name="submenu">
                            <div v-show="expandedMenus.personalizedHealth" class="submenu-container">
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TPersonalHealthRemindList') }"
                                    @click="navigateTo('/Admin/TPersonalHealthRemindList')">
                                    <span class="submenu-icon">⏰</span>
                                    <span class="submenu-text">个性化健康提醒表</span>
                                </div>
                                <div class="submenu-item" :class="{ active: isActiveRoute('/Admin/TScreenReportExportRecordList') }"
                                    @click="navigateTo('/Admin/TScreenReportExportRecordList')">
                                    <span class="submenu-icon">📤</span>
                                    <span class="submenu-text">自查报告导出记录表</span>
                                </div>
                            </div>
                        </transition>
                    </div>
                </nav>
            </aside>
				 
            <!-- 主要内容区 -->
            <el-main class="main-content">
                <!-- 面包屑导航 -->
                <el-breadcrumb separator-class="el-icon-arrow-right" class="breadcrumb">
                    <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="index" :to="item.path">
                        {{ item.title }}
                    </el-breadcrumb-item>
                    <!-- 返回链接 -->
                    <span class="back-link" @click="goBack">
                        <el-icon>
                            <back />
                        </el-icon>返回
                    </span>
                </el-breadcrumb>
                <router-view></router-view>
            </el-main>
             
        </el-container>
    </div>
</template>

<script setup>
import defaultAvatar from '@/assets/默认头像.png'; // 导入默认头像图片
import { useCommonStore } from '@/store';
import { ArrowDown, Back, Lock, SwitchButton, User } from '@element-plus/icons-vue';
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const commonStore = useCommonStore();
const router = useRouter();

// 计算属性
const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)

// 控制侧边栏折叠状态
const isCollapse = ref(false)

// 控制子菜单展开状态
const expandedMenus = ref({
    userManagement: false,
    audioAnalysis: false,
    healthRecord: false,
    healthScience: false,
    questionnaire: false,
    diet: false,
    medicalAssist: false,
    techExtension: false,
    personalizedHealth: false
})

// 切换子菜单展开状态
const toggleSubmenu = (menuKey) => {
    expandedMenus.value[menuKey] = !expandedMenus.value[menuKey]
}

// 判断路由是否激活
const isActiveRoute = (path) => {
    return route.path === path
}

// 导航到指定路由
const navigateTo = (path) => {
    if (route.path !== path) {
        router.push(path)
    }
    // 根据路由自动展开对应的菜单组
    autoExpandMenu(path)
}

// 根据路由自动展开菜单
const autoExpandMenu = (path) => {
    // 用户管理
    if (path.includes('/Admin/UserList') || path.includes('/Admin/TUserPrivacySettingList')) {
        expandedMenus.value.userManagement = true
    }
    // 音频自查分析管理
    if (path.includes('/Admin/TAudioScreenRecordList') || path.includes('/Admin/TModelInterfaceCallLogList')) {
        expandedMenus.value.audioAnalysis = true
    }
    // 健康档案与筛查追踪管理
    if (path.includes('/Admin/TPersonalLaryngealHealthRecordList') || path.includes('/Admin/TSymptomLogList')) {
        expandedMenus.value.healthRecord = true
    }
    // 健康科普管理
    if (path.includes('/Admin/THealthScience') || path.includes('/Admin/TScience')) {
        expandedMenus.value.healthScience = true
    }
    // 问卷管理
    if (path.includes('/Admin/TRiskAssessmentQuestionnaireList') || path.includes('/Admin/TUserQuestionnaireAnswerList')) {
        expandedMenus.value.questionnaire = true
    }
    // 饮食管理
    if (path.includes('/Admin/TLaryngealFood') || path.includes('/Admin/TUserDietRecordList')) {
        expandedMenus.value.diet = true
    }
    // 就医辅助管理
    if (path.includes('/Admin/TOtolaryngologyHospitalDoctorList') || path.includes('/Admin/TUserMedical')) {
        expandedMenus.value.medicalAssist = true
    }
    // 技术扩展管理
    if (path.includes('/Admin/TLaryngoscopePhotoList') || path.includes('/Admin/TModelOptimizeLabelList')) {
        expandedMenus.value.techExtension = true
    }
    // 个性化健康服务管理
    if (path.includes('/Admin/TPersonalHealthRemindList') || path.includes('/Admin/TScreenReportExportRecordList')) {
        expandedMenus.value.personalizedHealth = true
    }
}

// 面包屑数据
const breadcrumbList = ref([])
const route = useRoute()

// 监听路由变化，更新面包屑和自动展开菜单
watch(
    () => route.path,
    (newPath) => {
        // 自动展开对应的菜单组
        autoExpandMenu(newPath)
        
        // 更新面包屑
        const matched = route.matched
        if (!isHome(matched[0])) {
            breadcrumbList.value = [
                { title: '控制台', path: '/Admin/Home' },
                ...matched.map(item => ({
                    title: item.meta.title || item.name,
                    path: item.path
                }))
            ]
        } else {
            breadcrumbList.value = matched.map(item => ({
                title: item.meta.title || item.name,
                path: item.path
            }))
        }
    },
    { immediate: true }
)

function isHome(route) {
    return route.path === "/Admin";
}
function handleLogout() {
    commonStore.Logout();
    router.push("/Login");
}
function goBack() {
    router.back();
}
function handleUserInfo() {
    router.push("/Admin/UserPerson");
}
function handlePasswordEdit() {
    router.push("/Admin/PasswordEdit");
}
</script>

<style scoped lang="scss">
/* 整体布局样式 */
.admin-layout {
    height: 100vh;
    display: flex;
    flex-direction: column;
}

/* 顶部导航栏样式 */
.header {
    background-color: #fff;
    border-bottom: 1px solid #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    height: 60px;
    box-shadow: 0 1px 4px rgba(0, 21, 41, .08);
}

/* 左侧区域样式 */
.header-left {
    display: flex;
    align-items: center;
}

/* Logo样式 */
.logo {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 5px 0;
}

.logo-image {
    height: 40px;
    display: flex;
    align-items: center;
}

.logo-image img {
    height: 100%;
    width: auto;
    object-fit: contain;
}

.logo-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.system-title {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0;
    line-height: 1.2;
}

.system-subtitle {
    font-size: 12px;
    color: #666;
    margin: 0;
    line-height: 1.2;
}

/* 右侧用户信息样式 */
.header-right {
    display: flex;
    align-items: center;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 4px;
    transition: background-color 0.3s;
}

.user-info:hover {
    background-color: #f5f7fa;
}

.user-details {
    display: flex;
    flex-direction: column;
    line-height: 1.2;
}

.username {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
}

.role {
    font-size: 12px;
    color: #909399;
}

/* 下拉菜单项样式 */
:deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
}

:deep(.el-dropdown-menu__item .el-icon) {
    margin-right: 4px;
}

/* 主体容器样式 */
.main-container {
    flex: 1;
    overflow: hidden;
}

/* 自定义侧边栏样式 */
.custom-sidebar {
    width: 240px;
    min-width: 240px;
    max-width: 240px;
    background: #ffffff;
    height: 100%;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
    border-right: 1px solid #f0f0f0;
    position: relative;
    overflow: hidden;
    flex-shrink: 0;
}

.custom-sidebar::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #11998e, #38ef7d, #11998e);
    z-index: 1;
}

/* 自定义菜单容器 */
.custom-menu {
    padding: 15px 0;
    height: 100%;
    overflow-y: auto;
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.1) transparent;
}

.custom-menu::-webkit-scrollbar {
    width: 4px;
}

.custom-menu::-webkit-scrollbar-track {
    background: transparent;
}

.custom-menu::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 2px;
}

/* 菜单组样式 */
.menu-group {
    margin-bottom: 2px;
}

/* 菜单项基础样式 */
.menu-item {
    position: relative;
    margin: 0 8px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
    width: calc(100% - 16px);
    box-sizing: border-box;
}

.menu-item:hover {
    background: #f8f9fa;
    transform: translateX(4px);
}

.menu-item.active {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    box-shadow: 0 4px 12px rgba(17, 153, 142, 0.2);
}

.menu-item.active .menu-indicator {
    opacity: 1;
}

/* 菜单内容布局 */
.menu-content {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    color: #606266;
    position: relative;
    z-index: 2;
    width: 100%;
    box-sizing: border-box;
}

.menu-item.active .menu-content {
    color: #ffffff;
}

/* 菜单图标样式 */
.menu-icon {
    font-size: 18px;
    margin-right: 12px;
    display: inline-block;
    width: 24px;
    text-align: center;
    transition: transform 0.3s ease;
}

.menu-item:hover .menu-icon {
    transform: scale(1.1);
}

/* 菜单文字样式 */
.menu-text {
    font-size: 14px;
    font-weight: 500;
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    min-width: 0;
}

/* 激活指示器 */
.menu-indicator {
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 20px;
    background: #11998e;
    border-radius: 0 2px 2px 0;
    opacity: 0;
    transition: opacity 0.3s ease;
}

/* 展开图标样式 */
.expand-icon {
    font-size: 12px;
    margin-left: auto;
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    color: #909399;
}

.expand-icon.rotated {
    transform: rotate(90deg);
    color: #606266;
}

.menu-item.active .expand-icon {
    color: #ffffff;
}

.menu-item.active .expand-icon.rotated {
    color: #ffffff;
}

/* 子菜单头部样式 */
.submenu-header {
    position: relative;
}

.submenu-header.expanded {
    background: #f5f7fa;
}

.submenu-header.expanded::after {
    content: '';
    position: absolute;
    left: 8px;
    right: 8px;
    bottom: 0;
    height: 1px;
    background: #e4e7ed;
}

/* 子菜单容器样式 */
.submenu-container {
    margin-left: 16px;
    border-left: 2px solid #e4e7ed;
    position: relative;
    width: calc(100% - 16px);
    box-sizing: border-box;
}

/* 子菜单项样式 */
.submenu-item {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    margin: 2px 8px 2px 0;
    border-radius: 6px;
    color: #909399;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    width: calc(100% - 8px);
    box-sizing: border-box;
}

.submenu-item::before {
    content: '';
    position: absolute;
    left: -18px;
    top: 50%;
    transform: translateY(-50%);
    width: 6px;
    height: 6px;
    background: #c0c4cc;
    border-radius: 50%;
    transition: all 0.3s ease;
}

.submenu-item:hover {
    background: #f0f2f5;
    color: #606266;
    transform: translateX(4px);
}

.submenu-item:hover::before {
    background: #11998e;
    transform: translateY(-50%) scale(1.2);
}

.submenu-item.active {
    background: linear-gradient(135deg, rgba(17, 153, 142, 0.1) 0%, rgba(56, 239, 125, 0.1) 100%);
    color: #11998e;
    font-weight: 600;
}

.submenu-item.active::before {
    background: #11998e;
    transform: translateY(-50%) scale(1.3);
    box-shadow: 0 0 8px rgba(17, 153, 142, 0.4);
}

/* 子菜单图标样式 */
.submenu-icon {
    font-size: 14px;
    margin-right: 10px;
    display: inline-block;
    width: 18px;
    text-align: center;
    transition: transform 0.3s ease;
}

.submenu-item:hover .submenu-icon {
    transform: scale(1.1);
}

/* 子菜单文字样式 */
.submenu-text {
    font-size: 13px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
    min-width: 0;
}

/* 子菜单展开收起动画 */
.submenu-enter-active,
.submenu-leave-active {
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    transform-origin: top;
}

.submenu-enter-from,
.submenu-leave-to {
    opacity: 0;
    transform: scaleY(0.8) translateY(-10px);
    max-height: 0;
}

.submenu-enter-to,
.submenu-leave-from {
    opacity: 1;
    transform: scaleY(1) translateY(0);
    max-height: 500px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .custom-sidebar {
        width: 200px;
    }

    .menu-text,
    .submenu-text {
        font-size: 12px;
    }

    .menu-icon {
        font-size: 16px;
        width: 20px;
    }

    .submenu-icon {
        font-size: 12px;
        width: 16px;
    }
}

/* 主内容区样式 */
.main-content {
    background-color: #f0f2f5;
    padding: 20px;
    height: calc(100vh - 60px);
    overflow-y: scroll;
    box-sizing: border-box;
}

/* 面包屑样式 */
.breadcrumb {
    margin-bottom: 20px;
    padding: 10px;
    align-items: center;
    display: flex;
    background-color: #fff;
    border-radius: 4px;
}

/* 返回链接样式 */
.back-link {
    margin-left: auto;
    cursor: pointer;
    color: #11998e;
    display: flex;
    align-items: center;
    gap: 4px;
    transition: color 0.3s ease;
}

.back-link:hover {
    color: #38ef7d;
}
</style>
