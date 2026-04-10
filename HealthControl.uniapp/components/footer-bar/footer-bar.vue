<template>
  <view v-if="showBar" class="footer-shell hc-reveal-up" style="--delay: 160ms" :style="barStyle">
    <view
      v-for="(item, index) in footerBarList"
      :key="index"
      class="footer-item hc-interactive-pill"
      :class="{ 'footer-item--active': currentIndex === index }"
      @click="skip(item.url)"
    >
      <view class="footer-item__core">
        <uni-icons
          v-if="item.iconType"
          :type="item.iconType"
          size="21"
          :color="currentIndex === index ? '#122018' : '#4e6254'"
        />
        <image
          v-else
          :src="item.icon"
          class="footer-item__icon"
          :class="{ 'footer-item__icon--active': currentIndex === index }"
        />
      </view>
      <text class="footer-item__label">{{ item.label }}</text>
    </view>
  </view>
</template>

<script>
import { useCommonStore } from "@/store";
import { computed, onMounted, ref } from "vue";

export default {
  name: "FooterBar",
  options: {
    virtualHost: true,
    pureDataPattern: /^_/,
  },
  setup() {
    const store = useCommonStore();
    const currentIndex = ref(0);
    const showBar = ref(false);
    const tabbarHeight = ref(64);
    const safeAreaBottom = ref(0);

    const footerBarList = computed(() => store.FooterBarList);
    const barStyle = computed(() => ({
      height: `${tabbarHeight.value}px`,
      bottom: `${16 + safeAreaBottom.value}px`,
    }));

    onMounted(() => {
      const pages = getCurrentPages();
      const currentPageRoute = pages[pages.length - 1].route;
      const mapFooterBarList = footerBarList.value.map((item) => item.url.replace("/", ""));
      const position = mapFooterBarList.indexOf(currentPageRoute);
      currentIndex.value = position;
      showBar.value = position > -1;
      getSystemInfo();
    });

    const getSystemInfo = () => {
      uni.getSystemInfo({
        success: (res) => {
          const isIOS = res.platform === "ios";
          if (res.safeArea) {
            safeAreaBottom.value = res.screenHeight - res.safeArea.bottom;
          }
          tabbarHeight.value = 64;
          if (!isIOS) {
            safeAreaBottom.value = Math.max(safeAreaBottom.value, 8);
          }
        },
      });
    };

    const skip = (url) => {
      if (!url) return;
      if (footerBarList.value[currentIndex.value] && footerBarList.value[currentIndex.value].url === url) {
        return;
      }
      if (typeof uni.vibrateShort === "function") {
        uni.vibrateShort();
      }
      uni.redirectTo({ url });
    };

    return {
      currentIndex,
      showBar,
      footerBarList,
      barStyle,
      skip,
    };
  },
};
</script>

<style scoped lang="scss">
.footer-shell {
  position: fixed;
  left: 24upx;
  right: 24upx;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 8upx 10upx;
  border-radius: 9999upx;
  border: 1upx solid rgba(255, 255, 255, 0.46);
  background: linear-gradient(180deg, rgba(242, 248, 234, 0.72) 0%, rgba(226, 236, 212, 0.56) 100%);
  box-shadow:
    0 14upx 34upx rgba(58, 76, 53, 0.2),
    0 1upx 0 rgba(255, 255, 255, 0.4) inset,
    0 -1upx 0 rgba(150, 175, 130, 0.28) inset;
  backdrop-filter: blur(20px) saturate(118%);
  -webkit-backdrop-filter: blur(20px) saturate(118%);
}

.footer-item {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6upx;
  transition: var(--transition-base);
  position: relative;
  z-index: 1;
}

.footer-item:active {
  transform: scale(0.97);
}

.footer-item__core {
  min-width: 86upx;
  height: 48upx;
  padding: 0 18upx;
  border-radius: 9999upx;
  border: 1upx solid rgba(255, 255, 255, 0.34);
  background: rgba(241, 248, 231, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-item__label {
  font-size: 18upx;
  letter-spacing: 0.6upx;
  color: rgba(58, 74, 62, 0.88);
}

.footer-item--active .footer-item__core {
  background: linear-gradient(135deg, rgba(207, 233, 149, 0.96) 0%, rgba(184, 222, 117, 0.94) 100%);
  border: 1upx solid rgba(235, 249, 208, 0.88);
  box-shadow:
    0 8upx 18upx rgba(106, 141, 67, 0.24),
    0 1upx 0 rgba(255, 255, 255, 0.42) inset;
  animation: footerCorePop 320ms ease;
}

.footer-item--active .footer-item__label {
  color: #1f2d22;
  font-weight: 800;
}

.footer-item__icon {
  width: 42upx;
  height: 42upx;
  opacity: 0.8;
}

.footer-item__icon--active {
  opacity: 1;
}

@keyframes footerCorePop {
  0% {
    transform: scale(0.85);
  }
  70% {
    transform: scale(1.06);
  }
  100% {
    transform: scale(1);
  }
}
</style>
