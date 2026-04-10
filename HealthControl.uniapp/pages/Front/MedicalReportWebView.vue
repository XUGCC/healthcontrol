<template>
  <view class="hc-mobile-shell medical-webview-shell">
    <view class="hc-screen hc-stack medical-webview-screen">
      <hc-topbar title="报告预览" subtitle="在小程序内查看就诊准备报告" showBack @left="back" />

      <view v-if="!src" class="hc-card-soft medical-webview-empty">
        <text class="medical-webview-empty__title">缺少预览链接</text>
        <text class="medical-webview-empty__desc">请返回报告列表重新打开，或稍后再试。</text>
      </view>
    </view>

    <view v-if="src" class="medical-webview-wrap">
      <web-view :src="src" />
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const src = ref("");

const back = () => {
  uni.navigateBack();
};

onLoad((options) => {
  src.value = options?.url ? decodeURIComponent(options.url) : "";
});
</script>

<style scoped lang="scss">
.medical-webview-shell {
  min-height: 100vh;
}

.medical-webview-screen {
  gap: 24upx;
}

.medical-webview-wrap {
  height: calc(100vh - 140upx);
}

.medical-webview-empty {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.medical-webview-empty__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.medical-webview-empty__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}
</style>
