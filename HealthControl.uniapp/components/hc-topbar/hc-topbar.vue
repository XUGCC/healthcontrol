<template>
  <view class="hc-topbar" :class="{ 'hc-topbar--overlay': overlay }">
    <view class="hc-topbar__row">
      <view class="hc-topbar__action hc-topbar__action--left" @click="handleLeft">
        <uni-icons v-if="showBack" type="left" size="18" color="#18211b" />
        <text v-if="leftText" class="hc-topbar__action-text">{{ leftText }}</text>
      </view>
      <view class="hc-topbar__main">
        <text class="hc-topbar__title">{{ title }}</text>
        <text v-if="subtitle" class="hc-topbar__subtitle">{{ subtitle }}</text>
      </view>
      <view class="hc-topbar__action hc-topbar__action--right" @click="handleRight">
        <slot name="right">
          <text v-if="rightText" class="hc-topbar__action-text hc-topbar__action-text--strong">{{ rightText }}</text>
        </slot>
      </view>
    </view>
  </view>
</template>

<script setup>
import { getCurrentInstance } from "vue";

const props = defineProps({
  title: {
    type: String,
    default: "",
  },
  subtitle: {
    type: String,
    default: "",
  },
  leftText: {
    type: String,
    default: "",
  },
  rightText: {
    type: String,
    default: "",
  },
  showBack: {
    type: Boolean,
    default: true,
  },
  overlay: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["left", "right"]);
const instance = getCurrentInstance();

const hasLeftListener = () => Boolean(instance?.vnode?.props?.onLeft);

const handleLeft = () => {
  if (!props.showBack && !props.leftText) return;
  if (hasLeftListener()) {
    emit("left");
    return;
  }
  if (props.showBack) {
    const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
    if (pages.length > 1) {
      uni.navigateBack({ delta: 1 });
      return;
    }
    uni.reLaunch({ url: "/pages/Front/Index" });
    return;
  }
  emit("left");
};

const handleRight = () => {
  if (!props.rightText) {
    emit("right");
    return;
  }
  emit("right");
};
</script>

<style scoped lang="scss">
.hc-topbar {
  position: relative;
  z-index: 3;
  padding-bottom: 12upx;
}

.hc-topbar--overlay {
  padding-bottom: 0;
}

.hc-topbar__row {
  display: flex;
  align-items: center;
  gap: 12upx;
}

.hc-topbar__action {
  min-width: 84upx;
  min-height: 62upx;
  padding: 0 12upx;
  display: flex;
  align-items: center;
}

.hc-topbar__action--left {
  justify-content: flex-start;
}

.hc-topbar__action--right {
  justify-content: flex-end;
}

.hc-topbar__main {
  flex: 1;
  min-width: 0;
}

.hc-topbar__title {
  display: block;
  font-size: 36upx;
  line-height: 1.2;
  font-weight: 800;
  color: var(--text-color);
}

.hc-topbar__subtitle {
  display: block;
  margin-top: 6upx;
  font-size: 20upx;
  line-height: 1.4;
  color: var(--text-color-lighter);
}

.hc-topbar__action-text {
  font-size: 24upx;
  color: var(--text-color-light);
  font-weight: 700;
}

.hc-topbar__action-text--strong {
  color: var(--text-color);
}
</style>
