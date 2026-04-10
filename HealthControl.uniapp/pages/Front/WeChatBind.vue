<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack wechat-screen">
      <hc-topbar title="微信绑定" subtitle="保留原有绑定与解绑接口，只统一为主页式信息结构" :show-back="true" @left="back" />

      <view class="hc-card-dark wechat-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">账号联动</view>
        <text class="wechat-hero__title">{{ isWechatBound ? "当前账号已绑定微信" : "当前账号还没有绑定微信" }}</text>
        <text class="wechat-hero__desc">绑定后可以继续沿用现有的微信登录流程；解绑时仍保持原来的二次确认逻辑。</text>
        <view class="wechat-hero__status">
          <view class="wechat-hero__badge" :class="{ 'wechat-hero__badge--off': !isWechatBound }">
            {{ bindStatus }}
          </view>
          <text v-if="maskedOpenId" class="wechat-hero__openid">{{ maskedOpenId }}</text>
        </view>
      </view>

      <view class="hc-card-soft wechat-section hc-reveal-up" style="--delay: 90ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">当前状态</text>
          <text class="bg-section-head__meta">根据 OpenId 实时显示绑定结果</text>
        </view>
        <view class="wechat-summary">
          <view class="wechat-summary__item">
            <text class="wechat-summary__label">绑定状态</text>
            <text class="wechat-summary__value">{{ bindStatus }}</text>
          </view>
          <view class="wechat-summary__item">
            <text class="wechat-summary__label">OpenId</text>
            <text class="wechat-summary__value">{{ maskedOpenId || "尚未获取" }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft wechat-section hc-reveal-up" style="--delay: 150ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">操作入口</text>
          <text class="bg-section-head__meta">只保留一个最强操作按钮</text>
        </view>
        <view
          v-if="!isWechatBound"
          class="hc-pill-button-dark hc-interactive-pill wechat-action"
          @click="BindWechatApi"
        >
          {{ isBinding ? "绑定中..." : "绑定微信账号" }}
        </view>
        <view
          v-else
          class="hc-pill-button hc-interactive-pill wechat-action wechat-action--warn"
          @click="UnbindWechatApi"
        >
          {{ isUnbinding ? "解绑中..." : "解除微信绑定" }}
        </view>
      </view>

      <view class="hc-card-lime wechat-tip hc-reveal-up" style="--delay: 210ms">
        <text class="wechat-tip__title">温馨提示</text>
        <text class="wechat-tip__desc">绑定后可使用微信快速登录；解绑后不会删除你的个人资料和检测记录，只会移除微信登录能力。</text>
      </view>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";

const commonStore = useCommonStore();
const userId = computed(() => commonStore.UserId);
const openId = computed(() => commonStore.UserInfo?.OpenId || "");

const isBinding = ref(false);
const isUnbinding = ref(false);
const dialogRef = ref(null);

const isWechatBound = computed(() => Boolean(openId.value && String(openId.value).trim()));
const bindStatus = computed(() => (isWechatBound.value ? "已绑定" : "未绑定"));
const maskedOpenId = computed(() => {
  if (!openId.value) return "";
  const value = String(openId.value);
  if (value.length <= 8) return value;
  return `${value.slice(0, 4)}****${value.slice(-4)}`;
});

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/UserCenter" });
};
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const BindWechatApi = async () => {
  isBinding.value = true;
  uni.login({
    provider: "weixin",
    onlyAuthorize: true,
    success: async (res) => {
      try {
        await Post("/User/BindWechat", { WxCode: res.code, Id: userId.value });
        await commonStore.GetInfo();
      } finally {
        isBinding.value = false;
      }
    },
    fail: () => {
      isBinding.value = false;
    },
  });
};

const UnbindWechatApi = async () => {
  const result = await showDialog({
    title: "确认解绑",
    content: "解绑后将无法继续使用微信快速登录，是否继续？",
  });
  if (!result.confirm) return;
  try {
    isUnbinding.value = true;
    await Post("/User/UnbindWechat", { Id: userId.value });
    await commonStore.GetInfo();
  } finally {
    isUnbinding.value = false;
  }
};

onLoad(async () => {
  await commonStore.GetInfo();
});
</script>

<style scoped lang="scss">
.wechat-screen {
  gap: 24upx;
}

.wechat-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.wechat-hero__title {
  display: block;
  font-size: 46upx;
  line-height: 1.12;
  font-weight: 800;
  color: #f7ffdf;
}

.wechat-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.76);
}

.wechat-hero__status {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.wechat-hero__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  align-self: flex-start;
  min-height: 58upx;
  padding: 0 22upx;
  border-radius: 999upx;
  background: rgba(210, 238, 142, 0.16);
  color: #f7ffdf;
  font-size: 22upx;
  font-weight: 800;
}

.wechat-hero__badge--off {
  background: rgba(243, 188, 171, 0.16);
}

.wechat-hero__openid {
  font-size: 22upx;
  color: rgba(241, 248, 223, 0.76);
}

.wechat-summary {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.wechat-summary__item {
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.wechat-summary__label {
  display: block;
  font-size: 20upx;
  color: #7c8e7b;
}

.wechat-summary__value {
  display: block;
  margin-top: 8upx;
  font-size: 26upx;
  font-weight: 800;
  color: #172119;
}

.wechat-action {
  margin-top: 18upx;
}

.wechat-action--warn {
  color: #5f271d;
  background: rgba(243, 188, 171, 0.86);
  border-color: rgba(220, 129, 105, 0.72);
}

.wechat-tip__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.wechat-tip__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}
</style>
