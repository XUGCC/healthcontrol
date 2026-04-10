<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack service-screen">
      <view v-if="showBackButton" class="page-back hc-interactive-pill" @click="goBack">
        <uni-icons type="left" size="18" color="#18211b" />
        <text>返回</text>
      </view>

      <view class="hc-card-dark service-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">服务矩阵</view>
        <text class="service-hero__title">把筛查、护理、问卷、隐私和就医辅助集中到一个模块化入口</text>
        <view class="service-hero__stats">
          <view class="service-hero__stat">
            <text class="hc-stat-value hc-stat-value--dark">3</text>
            <text class="hc-stat-label">主要路径</text>
          </view>
          <view class="service-hero__stat">
            <text class="hc-stat-value hc-stat-value--dark">10+</text>
            <text class="hc-stat-label">功能入口</text>
          </view>
        </view>
      </view>

      <view
        v-for="(section, sectionIndex) in sections"
        :key="section.key"
        class="service-section hc-reveal-up"
        :style="{ '--delay': `${140 + sectionIndex * 120}ms` }"
      >
        <view class="bg-section-head">
          <text class="bg-section-head__title">{{ section.title }}</text>
          <text class="bg-section-head__meta">{{ section.subtitle }}</text>
        </view>

        <view v-if="section.items[0]" class="service-section__lead hc-interactive-card" @click="go(section.items[0])">
          <view class="service-section__lead-index">{{ section.order }}</view>
          <view class="service-section__lead-main">
            <text class="service-section__lead-title">{{ section.items[0].title }}</text>
            <text class="service-section__lead-desc">{{ section.items[0].desc }}</text>
          </view>
          <uni-icons type="right" size="18" color="#18211b" />
        </view>

        <view class="service-section__grid">
          <view v-for="item in section.items.slice(1)" :key="item.path" class="service-tile hc-interactive-card" @click="go(item)">
            <view class="service-tile__icon">
              <uni-icons :type="item.icon" size="19" color="#18211b" />
            </view>
            <text class="service-tile__title">{{ item.title }}</text>
            <text class="service-tile__desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <footer-bar />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useCommonStore } from "@/store";
import FooterBar from "@/components/footer-bar/footer-bar.vue";

const commonStore = useCommonStore();
const isLogin = computed(() => !!commonStore.UserId);
const showBackButton = ref(false);

const sections = [
  {
    key: "health",
    order: "01",
    title: "日常健康管理",
    subtitle: "记录，提醒，档案",
    items: [
      { title: "检测记录", desc: "回看所有历史自查。", path: "/pages/Front/AudioRecordList", icon: "list", needLogin: true },
      { title: "健康档案", desc: "风险趋势与护理习惯总览。", path: "/pages/Front/HealthRecord", icon: "person", needLogin: true },
      { title: "症状记录", desc: "跟踪不适变化。", path: "/pages/Front/SymptomLogList", icon: "compose", needLogin: true },
      { title: "提醒设置", desc: "管理日常护理节奏。", path: "/pages/Front/HealthReminder", icon: "notification", needLogin: true },
      { title: "喉镜影像", desc: "存档线下检查资料。", path: "/pages/Front/LaryngoscopePhotoList", icon: "image", needLogin: true },
    ],
  },
  {
    key: "tool",
    order: "02",
    title: "知识与工具",
    subtitle: "问卷，饮食，AI，科普",
    items: [
      { title: "健康科普", desc: "阅读喉部健康内容。", path: "/pages/Front/ScienceHome", icon: "paperplane", needLogin: false },
      { title: "风险问卷", desc: "生成阶段评分。", path: "/pages/Front/QuestionnaireHome", icon: "help", needLogin: true },
      { title: "饮食建议", desc: "查看适宜食物与记录。", path: "/pages/Front/DietHome", icon: "heart", needLogin: false },
      { title: "智能咨询", desc: "围绕结果继续提问。", path: "/pages/Front/AudioAIChat", icon: "chat", needLogin: true },
      { title: "确诊标注", desc: "补充模型优化数据。", path: "/pages/Front/ModelLabelList", icon: "flag", needLogin: true },
    ],
  },
  {
    key: "privacy",
    order: "03",
    title: "就医与隐私",
    subtitle: "支持，授权，数据",
    items: [
      { title: "就医辅助", desc: "整理报告与就诊准备。", path: "/pages/Front/MedicalHome", icon: "medal", needLogin: false },
      { title: "隐私设置", desc: "查看授权偏好。", path: "/pages/Front/PrivacySetting", icon: "locked", needLogin: true },
      { title: "数据导出", desc: "申请导出个人数据。", path: "/pages/Front/DataExportRequest", icon: "download", needLogin: true },
      { title: "数据删除", desc: "提交删除申请。", path: "/pages/Front/DataDeleteRequest", icon: "trash", needLogin: true },
      { title: "权利历史", desc: "查看历史处理记录。", path: "/pages/Front/DataRightsHistory", icon: "calendar", needLogin: true },
    ],
  },
];

const go = (item) => {
  if (!item || !item.path) return;
  if (item.needLogin && !isLogin.value) {
    uni.showToast({ title: "请先登录", icon: "none" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/Front/Login" });
    }, 1200);
    return;
  }
  uni.navigateTo({ url: item.path });
};

const updateBackState = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  showBackButton.value = pages.length > 1;
};

const goBack = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/Index" });
};

onShow(() => {
  updateBackState();
});
</script>

<style scoped lang="scss">
.service-screen {
  gap: 32upx;
}

.page-back {
  align-self: flex-start;
  min-height: 64upx;
  padding: 0 20upx;
  border-radius: 999upx;
  background: rgba(248, 252, 239, 0.9);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: inline-flex;
  align-items: center;
  gap: 8upx;
  font-size: 22upx;
  font-weight: 800;
  color: #18211b;
}

.service-hero {
  animation-delay: 40ms;
}

.service-hero__title {
  display: block;
  margin-top: 16upx;
  font-size: 54upx;
  line-height: 1.08;
  font-weight: 800;
  color: #ffffff;
}

.service-hero__stats {
  margin-top: 20upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.service-hero__stat {
  border-radius: 30upx;
  padding: 22upx;
  background: rgba(225, 243, 154, 0.12);
  border: 1upx solid rgba(183, 232, 99, 0.2);
}

.service-section {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.service-section + .service-section {
  margin-top: 16upx;
}

.service-section__lead {
  padding: 18upx;
  border-radius: 34upx;
  background: linear-gradient(135deg, #e0f59b 0%, #cdef75 100%);
  border: 1upx solid rgba(171, 208, 89, 0.4);
  display: flex;
  align-items: center;
  gap: 16upx;
}

.service-section__lead-index,
.service-tile__icon {
  transition: transform 0.22s ease;
}

.service-section__lead:active .service-section__lead-index {
  transform: scale(0.92);
}

.service-section__lead-index {
  width: 72upx;
  height: 72upx;
  border-radius: 24upx;
  background: #111713;
  color: #f7ffdf;
  font-size: 28upx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-section__lead-main {
  flex: 1;
  min-width: 0;
}

.service-section__lead-title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #152012;
}

.service-section__lead-desc {
  display: block;
  margin-top: 6upx;
  font-size: 22upx;
  line-height: 1.55;
  color: #455442;
}

.service-section__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14upx;
}

.service-tile {
  min-height: 180upx;
  padding: 20upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.86);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  flex-direction: column;
}

.service-tile:active .service-tile__icon {
  transform: scale(0.9) rotate(-8deg);
}

.service-tile__icon {
  width: 56upx;
  height: 56upx;
  border-radius: 20upx;
  background: #dff39c;
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-tile__title {
  display: block;
  margin-top: 20upx;
  font-size: 26upx;
  font-weight: 800;
  color: #172119;
}

.service-tile__desc {
  display: block;
  margin-top: 8upx;
  font-size: 20upx;
  line-height: 1.55;
  color: var(--text-color-light);
}
</style>
