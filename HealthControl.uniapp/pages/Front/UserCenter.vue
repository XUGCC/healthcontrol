<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack profile-screen">
      <view v-if="showBackButton" class="page-back hc-interactive-pill" @click="goBack">
        <uni-icons type="left" size="18" color="#18211b" />
        <text>返回</text>
      </view>

      <view class="profile-hero hc-reveal-up">
        <view class="profile-hero__top">
          <view class="profile-hero__identity">
            <image class="profile-hero__avatar" mode="aspectFill" :src="UserInfo.ImageUrls || defaultHeadImage"></image>
            <view class="profile-hero__main">
              <text class="profile-hero__name">{{ UserInfo.Name || "未命名用户" }}</text>
              <text class="profile-hero__time">{{ headerTimeText }}</text>
            </view>
          </view>
          <view class="profile-hero__edit hc-interactive-pill hc-icon-ping" @click.stop="navigateTo('/pages/Front/UserInfoEdit')">
            <uni-icons type="compose" size="22" color="#0f1612" />
          </view>
        </view>
        <view class="profile-hero__display hc-interactive-pill" :class="{ 'profile-hero__display--burst': profileWordBurst }" @click="triggerProfileWordFx">
          <view class="profile-hero__display-orbit profile-hero__display-orbit--1"></view>
          <view class="profile-hero__display-orbit profile-hero__display-orbit--2"></view>
          <view class="profile-hero__display-orbit profile-hero__display-orbit--3"></view>
          <text class="profile-hero__display-line profile-hero__display-line--primary">PROFILE</text>
          <text class="profile-hero__display-line profile-hero__display-line--secondary">ME</text>
        </view>
      </view>

      <view class="summary-grid hc-reveal-up" style="--delay: 120ms">
        <view class="summary-card hc-card-soft hc-interactive-card" @click="navigateTo('/pages/Front/QuestionnaireHistory')">
          <text class="summary-card__eyebrow">最近风险测评</text>
          <template v-if="latestResult">
            <text class="summary-card__time">{{ latestResult.AnswerTime }}</text>
            <view class="summary-card__row">
              <text class="summary-card__tag" :class="levelClass(latestResult.RiskAssessmentLevel)">
                {{ levelText(latestResult.RiskAssessmentLevel) }}
              </text>
              <text class="summary-card__score">{{ latestResult.Score }}</text>
            </view>
          </template>
          <template v-else>
            <text class="summary-card__time">暂无测评记录</text>
          </template>
        </view>

        <view class="summary-card hc-card-lime hc-interactive-card" @click="navigateTo('/pages/Front/ReminderMessageList')">
          <text class="summary-card__eyebrow">提醒消息</text>
          <text class="summary-card__value">{{ unreadCount > 99 ? "99+" : unreadCount }}</text>
          <text class="summary-card__desc">待处理提醒与系统通知</text>
        </view>
      </view>

      <view class="profile-group hc-reveal-up" style="--delay: 220ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">个人操作</text>
          <text class="bg-section-head__meta">记录与常用入口</text>
        </view>
        <view class="group-list">
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/AudioRecordList')">
            <view class="group-row__icon"><uni-icons type="list" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">检测记录</text>
              <text class="group-row__desc">历史结果与检测详情</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/DataRightsHistory')">
            <view class="group-row__icon"><uni-icons type="calendar" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">数据权利历史</text>
              <text class="group-row__desc">导出、删除等处理记录</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
        </view>
      </view>

      <view class="profile-group hc-reveal-up" style="--delay: 300ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">我的内容</text>
          <text class="bg-section-head__meta">发布与收藏</text>
        </view>
        <view class="group-list">
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/MyScience')">
            <view class="group-row__icon"><uni-icons type="compose" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">我的发布</text>
              <text class="group-row__desc">管理已发布科普内容</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/MyCollections')">
            <view class="group-row__icon"><uni-icons type="star" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">我的收藏</text>
              <text class="group-row__desc">查看已收藏内容</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
        </view>
      </view>

      <view class="profile-group hc-reveal-up" style="--delay: 360ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">数据与隐私</text>
          <text class="bg-section-head__meta">授权与账号</text>
        </view>
        <view class="group-list">
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/ModelLabelList')">
            <view class="group-row__icon"><uni-icons type="flag" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">确诊标注记录</text>
              <text class="group-row__desc">模型优化反馈数据</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/LaryngoscopePhotoList')">
            <view class="group-row__icon"><uni-icons type="image" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">喉镜影像档案</text>
              <text class="group-row__desc">线下检查资料管理</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/PrivacySetting')">
            <view class="group-row__icon"><uni-icons type="locked" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">隐私设置与授权</text>
              <text class="group-row__desc">管理个人授权偏好</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/UserInfoEdit')">
            <view class="group-row__icon"><uni-icons type="person" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">个人信息</text>
              <text class="group-row__desc">编辑账号资料</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/PasswordEdit')">
            <view class="group-row__icon"><uni-icons type="locked" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">修改密码</text>
              <text class="group-row__desc">提升账号安全性</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
          <view class="group-row hc-interactive-card" @click="navigateTo('/pages/Front/WeChatBind')">
            <view class="group-row__icon"><uni-icons type="chat" size="18" color="#1a251d" /></view>
            <view class="group-row__main">
              <text class="group-row__title">微信绑定</text>
              <text class="group-row__desc">绑定或切换微信账号</text>
            </view>
            <uni-icons type="right" size="18" color="#5f6f5f" />
          </view>
        </view>
      </view>

      <view class="hc-pill-button-dark logout-btn hc-interactive-pill hc-reveal-up" style="--delay: 420ms" @click="logout">退出登录</view>
    </view>

    <footer-bar />
  </view>
</template>

<script setup>
import userInfoIcon from "@/assets/默认头像.png";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";
import { computed, onUnmounted, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";

const commonStore = useCommonStore();
const defaultHeadImage = ref(userInfoIcon);
const UserInfo = computed(() => commonStore.UserInfo || {});
const UserId = computed(() => commonStore.UserId);
const showBackButton = ref(false);

const latestResult = ref(null);
const unreadCount = ref(0);
const profileWordBurst = ref(false);
let profileWordBurstTimer = null;
const headerTimeText = computed(() => {
  const raw = latestResult.value && latestResult.value.AnswerTime;
  if (!raw) {
    const now = new Date();
    return `${now.getMonth() + 1}月${now.getDate()}日`;
  }
  const normalized = String(raw).replace("T", " ");
  const m = normalized.match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (m) {
    return `${Number(m[2])}月${Number(m[3])}日`;
  }
  return normalized.slice(0, 10);
});

const loadUnreadCount = async () => {
  if (!UserId.value) return;
  try {
    const { Data } = await Post("/MessageNotice/UnreadCount", {
      UserId: UserId.value,
    });
    unreadCount.value = Data && Data.Count ? Data.Count : 0;
  } catch (e) {
    console.error("获取未读数量失败", e);
    unreadCount.value = 0;
  }
};

onLoad(() => {
  updateBackState();
  if (!commonStore.Token) {
    commonStore.CheckIsLogin();
    return;
  }
  setTimeout(() => {
    getUserInfo();
    loadLatestResult();
    loadUnreadCount();
  }, 80);
});

onShow(() => {
  updateBackState();
  if (!commonStore.Token) {
    commonStore.CheckIsLogin();
    return;
  }
  if (UserId.value) {
    loadLatestResult();
    loadUnreadCount();
  }
});

const getUserInfo = async () => {
  await commonStore.GetInfo();
};

const loadLatestResult = async () => {
  try {
    const { Data, Success } = await Post("/Front/Questionnaire/LatestResult", {});
    latestResult.value = Success ? Data || null : null;
  } catch (e) {
    latestResult.value = null;
  }
};

const levelText = (v) => (v === 2 ? "高风险" : v === 1 ? "中风险" : "低风险");
const levelClass = (v) => (v === 2 ? "level2" : v === 1 ? "level1" : "level0");

const logout = () => {
  commonStore.Logout();
};

const navigateTo = (url) => {
  uni.navigateTo({ url });
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

const triggerProfileWordFx = () => {
  if (profileWordBurstTimer) {
    clearTimeout(profileWordBurstTimer);
    profileWordBurstTimer = null;
  }
  profileWordBurst.value = false;
  setTimeout(() => {
    profileWordBurst.value = true;
  }, 16);
  profileWordBurstTimer = setTimeout(() => {
    profileWordBurst.value = false;
  }, 900);
  if (typeof uni.vibrateShort === "function") {
    uni.vibrateShort();
  }
};

onUnmounted(() => {
  if (profileWordBurstTimer) {
    clearTimeout(profileWordBurstTimer);
    profileWordBurstTimer = null;
  }
});
</script>

<style scoped lang="scss">
.profile-screen {
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

.profile-hero {
  padding: 8upx 6upx 4upx;
  border-radius: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.profile-hero__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.profile-hero__identity {
  display: flex;
  align-items: center;
  gap: 12upx;
  flex: 1;
  min-width: 0;
}

.profile-hero__avatar {
  width: 92upx;
  height: 92upx;
  border-radius: 26upx;
  border: none;
}

.profile-hero__main {
  display: flex;
  flex-direction: column;
  gap: 4upx;
  min-width: 0;
}

.profile-hero__name {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 36upx;
  line-height: 1;
  font-weight: 800;
  color: #0f1511;
  letter-spacing: -0.4upx;
}

.profile-hero__time {
  font-size: 24upx;
  color: #5f6f5f;
}

.profile-hero__edit {
  width: 92upx;
  height: 92upx;
  border-radius: 26upx;
  padding: 0;
  background: rgba(216, 244, 137, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.profile-hero__display {
  padding-top: 10upx;
  padding-bottom: 6upx;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 0;
  overflow: hidden;
}

.profile-hero__display-line {
  position: relative;
  z-index: 2;
  display: block;
  font-size: 84upx;
  line-height: 0.95;
  font-weight: 900;
  color: #111712;
  letter-spacing: -1.2upx;
  transition: transform 0.28s ease, text-shadow 0.28s ease;
}

.profile-hero__display-line--primary {
  font-size: 96upx;
  animation: wordIdle 3.8s ease-in-out infinite;
}

.profile-hero__display-line--secondary {
  font-size: 96upx;
  color: transparent;
  -webkit-text-stroke: 2upx #111712;
  animation: wordIdle 3.8s ease-in-out infinite 120ms;
}

.profile-hero__display-orbit {
  position: absolute;
  z-index: 1;
  width: 18upx;
  height: 18upx;
  border-radius: 999upx;
  background: linear-gradient(135deg, #d9f889 0%, #9bdc4a 100%);
  opacity: 0;
  transform: scale(0.4);
  box-shadow: 0 0 0 4upx rgba(178, 228, 96, 0.2);
}

.profile-hero__display-orbit--1 {
  top: 12upx;
  left: 120upx;
}

.profile-hero__display-orbit--2 {
  right: 130upx;
  top: 62upx;
}

.profile-hero__display-orbit--3 {
  left: 50%;
  bottom: 10upx;
}

.profile-hero__display--burst {
  animation: displayKick 560ms cubic-bezier(0.2, 0.7, 0.2, 1);
}

.profile-hero__display--burst .profile-hero__display-line--primary {
  animation: wordBurstPrimary 640ms cubic-bezier(0.2, 0.7, 0.2, 1);
}

.profile-hero__display--burst .profile-hero__display-line--secondary {
  animation: wordBurstSecondary 640ms cubic-bezier(0.2, 0.7, 0.2, 1);
}

.profile-hero__display--burst .profile-hero__display-orbit--1 {
  animation: orbitBurst1 720ms ease-out;
}

.profile-hero__display--burst .profile-hero__display-orbit--2 {
  animation: orbitBurst2 760ms ease-out;
}

.profile-hero__display--burst .profile-hero__display-orbit--3 {
  animation: orbitBurst3 700ms ease-out;
}

.summary-grid {
  margin-top: 8upx;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 16upx;
}

.summary-card {
  min-height: 166upx;
  border-radius: 34upx;
  padding: 20upx;
  display: flex;
  flex-direction: column;
}

.summary-card__eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 1upx;
  color: var(--text-color-lighter);
}

.summary-card__time {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  color: var(--text-color-light);
}

.summary-card__row {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10upx;
}

.summary-card__tag {
  padding: 6upx 14upx;
  border-radius: 999upx;
  font-size: 20upx;
  color: #fff;
}

.summary-card__tag.level0 {
  background: #93b85b;
}

.summary-card__tag.level1 {
  background: #c19446;
}

.summary-card__tag.level2 {
  background: #cc6354;
}

.summary-card__score {
  font-size: 46upx;
  line-height: 1;
  font-weight: 800;
  color: #2f4230;
}

.summary-card__value {
  margin-top: auto;
  font-size: 64upx;
  line-height: 1;
  font-weight: 800;
  color: #152012;
}

.summary-card__desc {
  margin-top: 10upx;
  font-size: 22upx;
  color: #4f6150;
}

.summary-grid .hc-card-lime {
  background: linear-gradient(145deg, #d6ee95 0%, #cae97e 50%, #e5f4b8 100%);
  border-color: rgba(171, 207, 89, 0.52);
  box-shadow: 0 12upx 26upx rgba(78, 108, 51, 0.14);
}

.profile-group {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.profile-group + .profile-group {
  margin-top: 14upx;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.group-row {
  min-height: 96upx;
  padding: 16upx 18upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.88);
  border: 1upx solid rgba(205, 224, 145, 0.88);
  display: flex;
  align-items: center;
  gap: 12upx;
}

.group-row__icon {
  width: 52upx;
  height: 52upx;
  border-radius: 16upx;
  background: #def39a;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.22s ease;
}

.group-row__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.group-row__title {
  font-size: 28upx;
  font-weight: 800;
  color: #1a251d;
}

.group-row__desc {
  margin-top: 4upx;
  font-size: 20upx;
  color: #617161;
}

.group-row:active .group-row__icon {
  transform: scale(0.9) rotate(-7deg);
}

@keyframes wordIdle {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-2upx);
  }
}

@keyframes displayKick {
  0% {
    transform: scale(1);
  }
  40% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes wordBurstPrimary {
  0% {
    transform: translateX(0) rotate(0deg);
    text-shadow: none;
  }
  25% {
    transform: translateX(-4upx) rotate(-1.2deg);
    text-shadow: 4upx 0 0 rgba(154, 214, 71, 0.35), -3upx 0 0 rgba(94, 132, 48, 0.22);
  }
  60% {
    transform: translateX(3upx) rotate(0.9deg);
    text-shadow: -3upx 0 0 rgba(154, 214, 71, 0.3);
  }
  100% {
    transform: translateX(0) rotate(0deg);
    text-shadow: none;
  }
}

@keyframes wordBurstSecondary {
  0% {
    transform: translateY(0) scale(1);
    color: transparent;
  }
  30% {
    transform: translateY(-4upx) scale(1.04);
    color: rgba(150, 214, 70, 0.2);
  }
  100% {
    transform: translateY(0) scale(1);
    color: transparent;
  }
}

@keyframes orbitBurst1 {
  0% {
    opacity: 0;
    transform: translate3d(0, 0, 0) scale(0.3);
  }
  35% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translate3d(-64upx, -24upx, 0) scale(1);
  }
}

@keyframes orbitBurst2 {
  0% {
    opacity: 0;
    transform: translate3d(0, 0, 0) scale(0.3);
  }
  40% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translate3d(68upx, -14upx, 0) scale(1.1);
  }
}

@keyframes orbitBurst3 {
  0% {
    opacity: 0;
    transform: translate3d(0, 0, 0) scale(0.3);
  }
  30% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translate3d(0, 28upx, 0) scale(1);
  }
}

.logout-btn {
  margin-top: 18upx;
}
</style>
