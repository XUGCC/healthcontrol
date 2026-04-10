<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack privacy-protocol-screen">
      <hc-topbar title="隐私协议" subtitle="在提交同意前，先快速了解这份协议摘要" showBack @left="back" />

      <view class="hc-card-dark privacy-protocol-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">隐私说明</view>
        <text class="privacy-protocol-hero__title">我们如何收集、使用和响应您的数据权利请求</text>
        <text class="privacy-protocol-hero__desc">以下为简版摘要，正式协议内容以后续后台配置和最终发布版本为准。</text>
      </view>

      <view
        v-for="(section, index) in sections"
        :key="section.title"
        class="hc-card-soft privacy-protocol-section hc-reveal-up"
        :style="{ '--delay': `${100 + index * 50}ms` }"
      >
        <text class="privacy-protocol-section__title">{{ section.title }}</text>
        <text class="privacy-protocol-section__desc">{{ section.desc }}</text>
      </view>

      <view class="hc-card-lime privacy-protocol-actions hc-reveal-up" style="--delay: 360ms">
        <text class="privacy-protocol-actions__title">确认前再看一眼</text>
        <text class="privacy-protocol-actions__desc">如果暂时不想授权，可以先返回；以后仍可在隐私设置中再次查看和调整。</text>
        <view class="privacy-protocol-actions__buttons">
          <view class="hc-pill-button-soft hc-interactive-pill" @click="back">我再想想</view>
          <view class="hc-pill-button-dark hc-interactive-pill" @click="agree">我已阅读并同意</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const sections = [
  {
    title: "1. 信息收集范围",
    desc: "包括账号信息、音频自查记录、健康档案、症状日志、饮食记录、标注数据、消息通知等。",
  },
  {
    title: "2. 信息使用目的",
    desc: "用于提供喉部健康自查服务、生成报告、个性化建议与提醒；在您开启匿名授权后，用于模型优化。",
  },
  {
    title: "3. 匿名授权说明",
    desc: "开启匿名授权后，您授权的匿名标注数据可用于模型优化训练，提升检测准确率。您可随时关闭总开关。",
  },
  {
    title: "4. 数据权利",
    desc: "您可申请导出或删除个人数据，平台将在规定期限内处理并通知结果。",
  },
  {
    title: "5. 联系我们",
    desc: "如有疑问，请通过小程序内反馈渠道联系我们。",
  },
];

const back = () => {
  uni.navigateBack({ delta: 1 });
};

const agree = async () => {
  uni.showLoading({ title: "提交中..." });
  try {
    const { Success } = await Post("/Front/Privacy/AgreeProtocol", {});
    if (Success) {
      uni.showToast({ title: "已同意", icon: "success" });
      setTimeout(() => back(), 600);
    }
  } finally {
    uni.hideLoading();
  }
};
</script>

<style scoped lang="scss">
.privacy-protocol-screen {
  gap: 24upx;
}

.privacy-protocol-hero,
.privacy-protocol-section,
.privacy-protocol-actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.privacy-protocol-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.privacy-protocol-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.privacy-protocol-section__title,
.privacy-protocol-actions__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.privacy-protocol-section__desc,
.privacy-protocol-actions__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.privacy-protocol-actions__buttons {
  display: flex;
  flex-direction: column;
  gap: 12upx;
  margin-top: 8upx;
}
</style>
