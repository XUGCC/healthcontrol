<template>
  <view class="hc-mobile-shell welcome-page">
    <view class="hc-screen">
      <view class="welcome-page__inner">
        <view v-if="showBackButton" class="page-back hc-interactive-pill" @click="goBack">
          <uni-icons type="left" size="18" color="#18211b" />
          <text>返回</text>
        </view>

        <view class="welcome-page__hero hc-card-lime">
          <view class="hc-kicker">{{ '\u5589\u90e8\u5065\u5eb7\u7cfb\u7edf' }}</view>
          <view class="welcome-page__art">
            <view class="art-orb art-orb--main"></view>
            <view class="art-orb art-orb--dark"></view>
            <view class="art-card art-card--dark">
              <text class="art-card__title">78%</text>
              <text class="art-card__meta">{{ '\u672c\u5468\u62a4\u7406\u6267\u884c' }}</text>
            </view>
            <view class="art-card art-card--soft">
              <text class="art-card__title art-card__title--soft">09:00</text>
              <text class="art-card__meta art-card__meta--soft">{{ '\u5b9a\u671f\u81ea\u67e5\u63d0\u9192' }}</text>
            </view>
            <view class="art-wave">
              <view class="art-wave__bar" v-for="bar in waveform" :key="bar" :style="{ height: `${bar}upx` }"></view>
            </view>
          </view>
          <text class="welcome-page__title">{{ '\u4e3a\u5589\u90e8\u5065\u5eb7\u6253\u9020\u4e00\u5957\u5e74\u8f7b\u5316\u7684\u7eff\u8272\u79fb\u52a8\u63a7\u5236\u53f0' }}</text>
          <text class="welcome-page__desc">
            {{ '\u7b5b\u67e5\uff0c\u75c7\u72b6\u8bb0\u5f55\uff0c\u63d0\u9192\u548c\u98ce\u9669\u95ee\u5377\u73b0\u5728\u88ab\u7edf\u4e00\u6536\u53e3\u5230\u4e00\u5957\u9ec4\u7eff\u4e3b\u5bfc\u7684\u9ad8\u8bc6\u522b\u7cfb\u7edf\u91cc\u3002' }}
          </text>
        </view>

        <view class="welcome-page__grid hc-grid-2">
          <view class="hc-card-soft">
            <text class="hc-stat-value">4</text>
            <text class="hc-stat-label">{{ '\u6838\u5fc3\u5065\u5eb7\u6d41\u7a0b' }}</text>
          </view>
          <view class="hc-card-dark">
            <text class="hc-stat-value hc-stat-value--dark">24h</text>
            <text class="hc-stat-label">{{ '\u968f\u65f6\u8fdb\u5165\u63d0\u9192\u4e0e\u8bb0\u5f55' }}</text>
          </view>
        </view>

        <view class="hc-card-soft welcome-page__feature">
          <view class="hc-section-head">
            <text class="hc-section-head__title">{{ '\u4ea7\u54c1\u4e3b\u7ebf' }}</text>
            <text class="hc-section-head__meta">{{ '\u9ad8\u4fdd\u771f\u4f46\u53ef\u5b9e\u73b0' }}</text>
          </view>
          <view class="welcome-page__feature-list">
            <view class="feature-row">
              <view class="feature-row__dot"></view>
              <text class="feature-row__text">{{ '\u9996\u9875\u7edf\u4e00\u6536\u53e3\u7b5b\u67e5\uff0c\u63d0\u9192\uff0c\u8d8b\u52bf\u5361\u7247\u4e0e\u667a\u80fd\u89e3\u8bfb\u3002' }}</text>
            </view>
            <view class="feature-row">
              <view class="feature-row__dot"></view>
              <text class="feature-row__text">{{ '\u75c7\u72b6\u9875\u4e0d\u518d\u662f\u666e\u901a\u5217\u8868\uff0c\u800c\u662f\u7c7b\u4e60\u60ef\u8ffd\u8e2a\u7684\u5361\u7247\u754c\u9762\u3002' }}</text>
            </view>
            <view class="feature-row">
              <view class="feature-row__dot"></view>
              <text class="feature-row__text">{{ '\u95ee\u5377\u548c\u6863\u6848\u9875\u627f\u62c5\u9636\u6bb5\u76ee\u6807\u4e0e\u7edf\u8ba1\u603b\u7ed3\u7684\u53d9\u4e8b\u4efb\u52a1\u3002' }}</text>
            </view>
          </view>
        </view>

        <view class="welcome-page__actions">
          <view class="hc-pill-button-dark" @click="goPrimary">{{ '\u8fdb\u5165\u5065\u5eb7\u63a7\u5236\u53f0' }}</view>
          <view class="hc-pill-button-soft" @click="goPreview">{{ '\u5148\u770b\u9996\u9875\u8bbe\u8ba1' }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useCommonStore } from "@/store";

const commonStore = useCommonStore();
const waveform = [34, 56, 76, 48, 66, 90, 52, 72];
const isLogin = computed(() => !!commonStore.UserId);
const showBackButton = ref(false);

const goPrimary = () => {
  uni.reLaunch({ url: isLogin.value ? "/pages/Front/Index" : "/pages/Front/Login" });
};

const goPreview = () => {
  uni.reLaunch({ url: "/pages/Front/Index" });
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
.welcome-page__inner {
  display: flex;
  flex-direction: column;
  gap: 22upx;
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

.welcome-page__hero {
  padding-top: 34upx;
}

.welcome-page__art {
  position: relative;
  height: 430upx;
  margin-top: 20upx;
  margin-bottom: 20upx;
}

.art-orb {
  position: absolute;
  border-radius: 9999upx;
  animation: hcFloat 4.8s ease-in-out infinite;
}

.art-orb--main {
  left: 36upx;
  top: 34upx;
  width: 250upx;
  height: 250upx;
  background: radial-gradient(circle at 35% 35%, #ecffb9 0%, #c9ef73 52%, #8ab944 100%);
}

.art-orb--dark {
  right: 20upx;
  top: 20upx;
  width: 180upx;
  height: 180upx;
  background: linear-gradient(135deg, #111713 0%, #263229 100%);
  animation-duration: 5.5s;
}

.art-card {
  position: absolute;
  border-radius: 36upx;
  padding: 24upx 26upx;
}

.art-card--dark {
  left: 196upx;
  top: 164upx;
  min-width: 214upx;
  background: linear-gradient(135deg, #111713 0%, #1d271f 100%);
  box-shadow: 0 18upx 40upx rgba(17, 23, 18, 0.22);
}

.art-card--soft {
  left: 18upx;
  bottom: 18upx;
  min-width: 244upx;
  background: rgba(248, 252, 239, 0.95);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.art-card__title {
  display: block;
  font-size: 52upx;
  line-height: 1;
  font-weight: 800;
  color: #ffffff;
}

.art-card__title--soft {
  color: #18211b;
}

.art-card__meta {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  color: rgba(255, 255, 255, 0.7);
}

.art-card__meta--soft {
  color: var(--text-color-light);
}

.art-wave {
  position: absolute;
  right: 0;
  bottom: 10upx;
  width: 260upx;
  height: 180upx;
  padding: 26upx 24upx;
  border-radius: 38upx;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(205, 224, 145, 0.86);
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.art-wave__bar {
  width: 18upx;
  border-radius: 999upx;
  background: linear-gradient(180deg, #d4f883 0%, #7ab13f 100%);
}

.welcome-page__title {
  display: block;
  font-size: var(--type-display);
  line-height: 1.05;
  font-weight: 800;
  letter-spacing: -1upx;
  color: #152012;
}

.welcome-page__desc {
  display: block;
  margin-top: 16upx;
  font-size: 24upx;
  line-height: 1.7;
  color: #33412f;
}

.welcome-page__feature-list {
  margin-top: 20upx;
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.feature-row {
  display: flex;
  align-items: flex-start;
  gap: 12upx;
}

.feature-row__dot {
  width: 18upx;
  height: 18upx;
  border-radius: 50%;
  margin-top: 6upx;
  background: #9cd94d;
}

.feature-row__text {
  flex: 1;
  font-size: 24upx;
  line-height: 1.65;
  color: var(--text-color);
}

.welcome-page__actions {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}
</style>
