<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack dashboard-screen">
      <view v-if="showBackButton" class="page-back hc-interactive-pill" @click="goBack">
        <uni-icons type="left" size="18" color="#18211b" />
        <text>返回</text>
      </view>

      <view class="hero-card hc-reveal-up">
        <view class="hero-card__head">
          <view class="hc-kicker">今日健康</view>
          <view class="hero-card__notify-wrap hc-interactive-pill hc-reveal-fade" style="--delay: 120ms" @click="goToReminderMessageList">
            <view class="hero-card__notify-icon hc-icon-breathe">
              <uni-icons type="notification" size="16" color="#1c271f" />
            </view>
            <view class="hero-card__notify-chip">
              <text class="hero-card__notify-label">未读</text>
              <text class="hero-card__notify-value">{{ unreadCount > 99 ? "99+" : unreadCount }}</text>
            </view>
          </view>
        </view>
        <text class="hero-card__title">从一次短时发声开始，完成今天喉部自查</text>
        <view class="hero-card__actions">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="goToAudioRecord">开始自查</view>
          <view class="hero-card__action-secondary hc-pill-button-soft hc-interactive-pill" @click="goToRecordList">查看记录</view>
        </view>
      </view>

      <view class="dashboard-grid">
        <view class="hc-card-dark insight-card hc-interactive-card hc-reveal-up hc-shine" style="--delay: 140ms" @click="goToAIChat">
          <view class="hc-kicker hc-kicker--dark">智能解读</view>
          <text class="hc-card-title">结构化结果摘要</text>
          <text class="hc-card-subtitle">把筛查结果、风险阶段和建议动作压缩为可执行卡片。</text>
          <view class="insight-card__bars">
            <view v-for="(bar, index) in aiBars" :key="index" class="insight-card__bar" :style="{ width: `${bar}%` }"></view>
          </view>
        </view>

        <view class="hc-card-soft archive-card hc-interactive-card hc-reveal-up" style="--delay: 220ms" @click="goToHealthRecord">
          <text class="archive-card__eyebrow">健康档案</text>
          <text class="hc-card-title">趋势与进度</text>
          <text class="hc-card-subtitle archive-card__subtitle">查看症状变化、历史检测和护理习惯，持续追踪恢复节奏。</text>
          <view class="archive-card__chart">
            <view class="archive-card__bar" style="height: 46upx"></view>
            <view class="archive-card__bar" style="height: 62upx"></view>
            <view class="archive-card__bar" style="height: 38upx"></view>
            <view class="archive-card__bar" style="height: 78upx"></view>
            <view class="archive-card__bar" style="height: 54upx"></view>
          </view>
          <view class="archive-card__footer">
            <text class="archive-card__link">打开档案</text>
            <uni-icons type="right" size="18" color="#18211b" />
          </view>
        </view>
      </view>

      <view class="bg-section-head module-head hc-reveal-fade" style="--delay: 260ms">
        <text class="bg-section-head__title">核心模块</text>
        <text class="bg-section-head__meta">3个高优先级入口</text>
      </view>

      <view class="modules-card__grid">
        <view class="module-item module-item--dark hc-interactive-card hc-reveal-up" style="--delay: 300ms" @click="goToServiceHub">
          <text class="module-item__value">01</text>
          <text class="module-item__title">服务中心</text>
          <text class="module-item__desc">集中访问筛查、问卷、饮食和就医辅助。</text>
        </view>
        <view class="module-item hc-interactive-card hc-reveal-up" style="--delay: 360ms" @click="goToQuestionnaireHome">
          <text class="module-item__value">02</text>
          <text class="module-item__title">问卷评估</text>
          <text class="module-item__desc">阶段风险评分与趋势建议。</text>
        </view>
        <view class="module-item hc-interactive-card hc-reveal-up" style="--delay: 420ms" @click="goToUserCenter">
          <text class="module-item__value">03</text>
          <text class="module-item__title">个人中心</text>
          <text class="module-item__desc">查看账号资料与个人数据入口。</text>
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
import { Post } from "@/utils/http";
import FooterBar from "@/components/footer-bar/footer-bar.vue";

const commonStore = useCommonStore();
const UserId = computed(() => commonStore.UserId);
const unreadCount = ref(0);
const aiBars = [86, 62, 74, 48];
const showBackButton = ref(false);

const loadUnreadCount = async () => {
  if (!UserId.value) {
    unreadCount.value = 0;
    return;
  }
  try {
    const { Data } = await Post("/MessageNotice/UnreadCount", {
      UserId: UserId.value,
    });
    unreadCount.value = Data && Data.Count ? Data.Count : 0;
  } catch (error) {
    console.error("Failed to load unread count", error);
    unreadCount.value = 0;
  }
};

onShow(() => {
  updateBackState();
  loadUnreadCount();
});

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
  uni.reLaunch({ url: "/pages/Front/Welcome" });
};

const requireLoginAndGo = (url) => {
  if (!commonStore.UserId) {
    uni.showToast({
      title: "请先登录",
      icon: "none",
    });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/Front/Login" });
    }, 1200);
    return;
  }
  uni.navigateTo({ url });
};

const goToAudioRecord = () => requireLoginAndGo("/pages/Front/AudioRecord");
const goToRecordList = () => requireLoginAndGo("/pages/Front/AudioRecordList");
const goToHealthRecord = () => requireLoginAndGo("/pages/Front/HealthRecord");
const goToReminderMessageList = () => requireLoginAndGo("/pages/Front/ReminderMessageList");
const goToQuestionnaireHome = () => requireLoginAndGo("/pages/Front/QuestionnaireHome");
const goToAIChat = () => requireLoginAndGo("/pages/Front/AudioAIChat");
const goToServiceHub = () => uni.navigateTo({ url: "/pages/Front/ServiceHub" });
const goToUserCenter = () => uni.navigateTo({ url: "/pages/Front/UserCenter" });
</script>

<style scoped lang="scss">
.dashboard-screen {
  gap: 48upx;
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

.hero-card {
  padding: 6upx 6upx 2upx;
  display: flex;
  flex-direction: column;
}

.hero-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-card__notify-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8upx;
  transform-origin: right center;
}

.hero-card__notify-icon {
  width: 54upx;
  height: 54upx;
  border-radius: 18upx;
  background: rgba(248, 252, 239, 0.85);
  border: 1upx solid rgba(187, 211, 123, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-card__notify-chip {
  min-width: 98upx;
  height: 54upx;
  border-radius: 18upx;
  padding: 0 14upx;
  background: rgba(21, 30, 24, 0.9);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6upx;
}

.hero-card__notify-label {
  font-size: 20upx;
  color: rgba(240, 248, 220, 0.72);
}

.hero-card__notify-value {
  font-size: 30upx;
  line-height: 1;
  font-weight: 800;
  color: #f7ffdf;
  animation: notifyValuePop 2.4s ease-in-out infinite;
}

.hero-card__title {
  display: block;
  margin-top: 24upx;
  font-size: 66upx;
  line-height: 1.05;
  font-weight: 800;
  color: #152012;
  letter-spacing: -1upx;
}

.hero-card__actions {
  margin-top: 128upx;
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.hero-card__action-secondary {
  min-height: 80upx;
}

.dashboard-grid {
  margin-top: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 28upx;
}

.hc-card-title {
  margin-top: 14upx;
  display: block;
  font-size: 34upx;
  line-height: 1.25;
  font-weight: 800;
}

.hc-card-subtitle {
  margin-top: 10upx;
  display: block;
  font-size: 22upx;
  line-height: 1.6;
  color: rgba(241, 248, 223, 0.72);
}

.insight-card,
.archive-card {
  min-height: 336upx;
  display: flex;
  flex-direction: column;
}

.insight-card__bars {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.insight-card__bar {
  height: 18upx;
  border-radius: 999upx;
  background: linear-gradient(135deg, #dff88f 0%, #93d846 100%);
  transform-origin: left center;
  animation: barGrowIn 700ms cubic-bezier(0.2, 0.7, 0.2, 1) both;
}

.insight-card__bar:nth-child(1) {
  animation-delay: 220ms;
}

.insight-card__bar:nth-child(2) {
  animation-delay: 300ms;
}

.insight-card__bar:nth-child(3) {
  animation-delay: 380ms;
}

.insight-card__bar:nth-child(4) {
  animation-delay: 460ms;
}

.archive-card__eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 1upx;
  color: var(--text-color-lighter);
}

.archive-card__subtitle {
  color: #4f6150;
}

.archive-card__chart {
  margin-top: auto;
  display: flex;
  align-items: flex-end;
  gap: 10upx;
  height: 86upx;
}

.archive-card__bar {
  flex: 1;
  border-radius: 999upx;
  background: linear-gradient(180deg, #cde978 0%, #8ccf43 100%);
  transform-origin: center bottom;
  animation: chartRiseIn 780ms cubic-bezier(0.2, 0.7, 0.2, 1) both;
}

.archive-card__bar:nth-child(1) {
  animation-delay: 260ms;
}

.archive-card__bar:nth-child(2) {
  animation-delay: 320ms;
}

.archive-card__bar:nth-child(3) {
  animation-delay: 380ms;
}

.archive-card__bar:nth-child(4) {
  animation-delay: 440ms;
}

.archive-card__bar:nth-child(5) {
  animation-delay: 500ms;
}

.archive-card__footer {
  margin-top: 18upx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.archive-card__link {
  font-size: 24upx;
  font-weight: 800;
  color: #1a251d;
}

.module-head {
  margin-top: 0;
}

.modules-card__grid {
  margin-top: -24upx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  align-items: stretch;
  gap: 24upx;
}

.module-item {
  min-height: 214upx;
  border-radius: 34upx;
  padding: 22upx 20upx;
  background: rgba(248, 252, 239, 0.82);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.module-item--dark {
  background: linear-gradient(135deg, #101612 0%, #1e2820 100%);
  border-color: #101612;
}

.module-item__value {
  font-size: 46upx;
  line-height: 1;
  font-weight: 800;
  color: #172119;
}

.module-item--dark .module-item__value,
.module-item--dark .module-item__title,
.module-item--dark .module-item__desc {
  color: #f7ffdf;
}

.module-item__title {
  display: block;
  font-size: 26upx;
  font-weight: 800;
  color: #172119;
}

.module-item__desc {
  margin-top: 8upx;
  font-size: 20upx;
  line-height: 1.5;
  min-height: 60upx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: var(--text-color-light);
}

@keyframes barGrowIn {
  0% {
    opacity: 0;
    transform: scaleX(0.45);
  }
  100% {
    opacity: 1;
    transform: scaleX(1);
  }
}

@keyframes chartRiseIn {
  0% {
    opacity: 0;
    transform: scaleY(0.25);
  }
  100% {
    opacity: 1;
    transform: scaleY(1);
  }
}

@keyframes notifyValuePop {
  0%,
  100% {
    transform: scale(1);
  }
  42% {
    transform: scale(1.1);
  }
  55% {
    transform: scale(0.98);
  }
}
</style>
