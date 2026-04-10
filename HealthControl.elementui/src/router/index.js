import { useCommonStore } from "@/store";
import AdminLayout from "@/views/Admin/Layout/index.vue";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import { createRouter, createWebHashHistory } from "vue-router";

// 配置 NProgress
NProgress.configure({
  showSpinner: false, // 不显示加载圈
  speed: 1000,
  trickle: false,
});

// 路由配置
const routes = [
  {
    path: "/",
    redirect: "/Admin",
  },
  {
    path: "/Login",
    name: "Login",
    component: () => import("../views/Login.vue"),
  },
  {
    path: "/Register",
    name: "Register",
    component: () => import("../views/Register.vue"),
  },
  {
    path: "/ForgetPassword",
    name: "ForgetPassword",
    component: () => import("../views/ForgetPassword.vue"),
  },
  // 404页面
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("../views/NotFound.vue"),
  },
];



const adminRoutes = [
  {
    path: "/Admin",
    name: "Admin",
    redirect: "/Admin/Home",
    component: AdminLayout,
    meta: {
      title: "控制台",
      isAdmin: true,
    },
    children: [
      {
        path: "/Admin/Home",
        name: "AdminHome",
        meta: {
          title: "控制台",
          isAdmin: true,
        },
        component: () => import("../views/Admin/Home.vue"),
      },
      {
        path: "/Admin/UserPerson",
        name: "AdminUserPerson",
        meta: {
          title: "账号信息",
          isAdmin: true,
        },
        component: () => import("../views/Admin/UserPerson.vue"),
      },
      {
        path: "/Admin/PasswordEdit",
        name: "PasswordEdit",
        meta: {
          title: "修改密码",
          isAdmin: true,
        },
        component: () => import("../views/Admin/PasswordEdit.vue"),
      },
      {
        path: "/Admin/UserList",
        name: "AdminUserList",
        meta: {
          title: "账号列表",
          isAdmin: true,
        },
        component: () => import("../views/Admin/UserList.vue"),
      },
      {
        path: "/Admin/ModelInterfaceStats",
        meta: {
          title: "模型接口统计",
          isAdmin: true,
        },
        component: () => import("../views/Admin/ModelInterfaceStats.vue"),
      },
      	 {
            path: "/Admin/TAudioScreenRecordList",
            meta: {
                title: "音频自查记录",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TAudioScreenRecordList.vue")
        },	
 
      	 {
            path: "/Admin/THealthScienceList",
            meta: {
                title: "健康科普",
                isAdmin: true,
            },
            component: () => import("../views/Admin/THealthScienceList.vue")
        },	
 
      	 {
            path: "/Admin/THealthScienceCategoryList",
            meta: {
                title: "健康科普分类",
                isAdmin: true,
            },
            component: () => import("../views/Admin/THealthScienceCategoryList.vue")
        },	
 
      	 {
            path: "/Admin/TLaryngealFoodList",
            meta: {
                title: "喉部食物库",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TLaryngealFoodList.vue")
        },	
 
      	 {
            path: "/Admin/TLaryngealFoodCategoryList",
            meta: {
                title: "喉部食物库分类",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TLaryngealFoodCategoryList.vue")
        },	
 
      	 {
            path: "/Admin/TLaryngoscopePhotoList",
            meta: {
                title: "喉镜照片记录",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TLaryngoscopePhotoList.vue")
        },	
 
      	 {
            path: "/Admin/TModelInterfaceCallLogList",
            meta: {
                title: "模型接口调用日志",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TModelInterfaceCallLogList.vue")
        },	
 
      	 {
            path: "/Admin/TModelOptimizeLabelList",
            meta: {
                title: "模型优化标注",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TModelOptimizeLabelList.vue")
        },	
 
      	 {
            path: "/Admin/TOtolaryngologyHospitalDoctorList",
            meta: {
                title: "耳鼻喉医院医生",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TOtolaryngologyHospitalDoctorList.vue")
        },	
 
      	 {
            path: "/Admin/TPersonalHealthRemindList",
            meta: {
                title: "个性化健康提醒",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TPersonalHealthRemindList.vue")
        },	
 
      	 {
            path: "/Admin/TPersonalLaryngealHealthRecordList",
            meta: {
                title: "个人喉部健康档案",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TPersonalLaryngealHealthRecordList.vue")
        },	
 
      	 {
            path: "/Admin/TRiskAssessmentQuestionnaireList",
            meta: {
                title: "风险评估问卷",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TRiskAssessmentQuestionnaireList.vue")
        },	

      	 {
            path: "/Admin/TRiskAssessmentQuestionList",
            meta: {
                title: "风险评估题目",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TRiskAssessmentQuestionList.vue")
        },	

      	 {
            path: "/Admin/TRiskAssessmentQuestionOptionList",
            meta: {
                title: "风险评估题目选项",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TRiskAssessmentQuestionOptionList.vue")
        },	
 
      	 {
            path: "/Admin/TScienceCollectList",
            meta: {
                title: "科普收藏",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TScienceCollectList.vue")
        },	
 
      	 {
            path: "/Admin/TScienceCommentList",
            meta: {
                title: "科普评论",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TScienceCommentList.vue")
        },	
 
      	 {
            path: "/Admin/TScienceCommentLikeList",
            meta: {
                title: "科普评论点赞",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TScienceCommentLikeList.vue")
        },	
 
      	 {
            path: "/Admin/TScienceLikeList",
            meta: {
                title: "科普点赞",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TScienceLikeList.vue")
        },	
 
      	 {
            path: "/Admin/TScreenReportExportRecordList",
            meta: {
                title: "自查报告导出记录",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TScreenReportExportRecordList.vue")
        },	
 
      	 {
            path: "/Admin/DataExportRequestList",
            meta: {
                title: "数据导出申请",
                isAdmin: true,
            },
            component: () => import("../views/Admin/DataExportRequestList.vue")
        },	
 
      	 {
            path: "/Admin/DataDeleteRequestList",
            meta: {
                title: "数据删除申请",
                isAdmin: true,
            },
            component: () => import("../views/Admin/DataDeleteRequestList.vue")
        },	
 
      	 {
            path: "/Admin/TSymptomLogList",
            meta: {
                title: "症状日志",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TSymptomLogList.vue")
        },	
 
      	 {
            path: "/Admin/TUserDietRecordList",
            meta: {
                title: "用户饮食记录",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TUserDietRecordList.vue")
        },	
 
      	 {
            path: "/Admin/TUserMedicalPrepareReportList",
            meta: {
                title: "用户就诊准备报告",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TUserMedicalPrepareReportList.vue")
        },	
 
      	 {
            path: "/Admin/TUserMedicalRecommendList",
            meta: {
                title: "用户就医推荐记录",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TUserMedicalRecommendList.vue")
        },	
 
      	 {
            path: "/Admin/TUserPrivacySettingList",
            meta: {
                title: "用户隐私设置",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TUserPrivacySettingList.vue")
        },	
 
      	 {
            path: "/Admin/TUserQuestionnaireAnswerList",
            meta: {
                title: "用户问卷答题",
                isAdmin: true,
            },
            component: () => import("../views/Admin/TUserQuestionnaireAnswerList.vue")
        },	
 
    ],
  },
];

// 创建路由实例
const router = createRouter({
  history: createWebHashHistory(),
  routes: [...routes, ...adminRoutes],
});

// 路由前置守卫
router.beforeEach(async (to, from, next) => {
  // 开启进度条
  NProgress.start();

  // 获取 store 实例
  const commonStore = useCommonStore();

  // 可以在这里处理路由权限等逻辑
  if (commonStore.Token) {
    //判断是否有用户信息
    if (commonStore.HasUserInfo == false) {
      //获取账号信息
      await commonStore.GetInfo();
      //获取的账号id为空或者为0
      if (commonStore.UserId == null || commonStore.UserId == 0) {
        commonStore.Logout();
      }
    }
    //如果去往的页面是后台
    if (to.meta && to.meta.isAdmin) {
      if (commonStore.RoleType == "管理员") {
        next();
      } else {
        next({ path: "/" });
      }
    } else {
      if (commonStore.RoleType == "管理员") {
        next({ path: "/Admin" });
      } else {
        next();
      }
    }
  } else {
    if (to.meta && to.meta.isAdmin) {
      next({ path: "/Login" });
    } else {
      next();
    }
  }
});

// 路由后置守卫
router.afterEach(() => {
  window.scrollTo({ top: 0 });
  // 关闭进度条
  NProgress.done();
});

export default router;
