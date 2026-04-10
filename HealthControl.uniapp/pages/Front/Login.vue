<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack auth-screen">
      <hc-topbar title="账号登录" subtitle="回到你的喉部健康控制台" :show-back="true" @left="back" />

      <view class="hc-card-dark auth-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">健康控制台</view>
        <text class="auth-hero__title">登录后继续查看自查结果、历史记录和提醒进度</text>
        <text class="auth-hero__desc">账号会同步你的检测记录、问卷结论和个人护理节奏，方便持续追踪恢复变化。</text>
        <view class="auth-hero__stats">
          <view class="auth-hero__stat">
            <text class="auth-hero__stat-label">主链路</text>
            <text class="auth-hero__stat-value">记录同步</text>
          </view>
          <view class="auth-hero__stat">
            <text class="auth-hero__stat-label">登录方式</text>
            <text class="auth-hero__stat-value">账号 / 微信</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft auth-section hc-reveal-up" style="--delay: 90ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">登录信息</text>
          <text class="bg-section-head__meta">先确认账号与角色</text>
        </view>

        <view class="auth-field">
          <text class="auth-field__label">账号</text>
          <view class="auth-input">
            <uni-icons type="person" size="18" color="#18211b" />
            <input v-model="formData.UserName" class="auth-input__inner" type="text" placeholder="请输入账号" />
          </view>
        </view>

        <view class="auth-field">
          <text class="auth-field__label">密码</text>
          <view class="auth-input">
            <uni-icons type="locked" size="18" color="#18211b" />
            <input v-model="formData.Password" class="auth-input__inner" type="password" :password="true" placeholder="请输入密码" />
          </view>
        </view>

        <view class="auth-field">
          <text class="auth-field__label">角色</text>
          <view class="auth-choice">
            <view class="hc-token-tabs">
              <view
                v-for="item in roleTypeList"
                :key="item.value"
                class="hc-token-tabs__item"
                :class="{ active: formData.RoleType === item.value }"
                @click="formData.RoleType = item.value"
              >
                {{ item.text }}
              </view>
            </view>
          </view>
        </view>

        <view class="hc-pill-button-dark hc-interactive-pill auth-submit" @click="login">登录并进入首页</view>

        <view class="auth-links">
          <text class="auth-link" @click="toForgetPassword">忘记密码</text>
          <text class="auth-link auth-link--strong" @click="toRegister">立即注册</text>
        </view>
      </view>

      <view class="hc-card-soft auth-section hc-reveal-up" style="--delay: 150ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">快捷登录</text>
          <text class="bg-section-head__meta">保留原有微信授权流程</text>
        </view>
        <view class="auth-wechat hc-interactive-card" @click="otherLogin(0)">
          <view class="auth-wechat__icon">
            <image class="auth-wechat__icon-image" src="/assets/wx.png" mode="aspectFit"></image>
          </view>
          <view class="auth-wechat__main">
            <text class="auth-wechat__title">微信登录</text>
            <text class="auth-wechat__desc">快速绑定当前账号并进入控制台</text>
          </view>
          <uni-icons type="right" size="18" color="#5c6d59" />
        </view>
      </view>

      <view class="hc-card-lime auth-tip hc-reveal-up" style="--delay: 210ms">
        <text class="auth-tip__title">使用说明</text>
        <text class="auth-tip__desc">检测结果仅供居家自查参考，不替代临床诊断；如有持续不适，请及时就医。</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onLoad } from "@dcloudio/uni-app";
import { ref } from "vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import { useCommonStore } from "@/store";
import { GetLoginCode } from "@/utils/comm";
import { Post } from "@/utils/http";

const commonStore = useCommonStore();

const formData = ref({
  UserName: "",
  Password: "",
  RoleType: "2",
});

const roleTypeList = ref([]);

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/Welcome" });
};

const loadRoleTypeList = async () => {
  try {
    const { Data, Success } = await Post("/Select/RoleType");
    const list = Success && Data?.Items ? Data.Items : [];
    if (!list.length) {
      roleTypeList.value = [{ text: "普通用户", value: "2" }];
      return;
    }
    roleTypeList.value = list
      .filter((item) => item.Code !== 1)
      .map((item) => ({
        text: item.Name,
        value: String(item.Code),
      }));
  } catch (error) {
    console.error("loadRoleTypeList error:", error);
    roleTypeList.value = [{ text: "普通用户", value: "2" }];
  }
};

const login = async () => {
  if (!formData.value.UserName) {
    uni.showToast({ title: "请输入账号", icon: "none" });
    return;
  }
  if (!formData.value.Password) {
    uni.showToast({ title: "请输入密码", icon: "none" });
    return;
  }

  const { Success } = await commonStore.Login(formData.value);
  if (!Success) return;

  await commonStore.GetInfo();
  uni.reLaunch({ url: "/pages/Front/Index" });
};

const toForgetPassword = () => {
  uni.redirectTo({ url: "/pages/Front/ForgetPassword" });
};

const toRegister = () => {
  uni.redirectTo({ url: "/pages/Front/Register" });
};

const otherLogin = async (type) => {
  if (type !== 0) {
    uni.showToast({ title: "该方式暂未开放", icon: "none" });
    return;
  }

  // #ifdef H5
  uni.showToast({ title: "H5 暂不支持微信登录", icon: "none" });
  return;
  // #endif

  uni.showLoading({ title: "正在登录..." });
  try {
    const wxCode = await GetLoginCode();
    const { Success } = await commonStore.Login({ WxCode: wxCode });
    if (!Success) return;
    await commonStore.GetInfo();
    uni.reLaunch({ url: "/pages/Front/Index" });
  } finally {
    uni.hideLoading();
  }
};

onLoad(() => {
  loadRoleTypeList();
});
</script>

<style scoped lang="scss">
.auth-screen {
  gap: 24upx;
}

.auth-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.auth-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.12;
  font-weight: 800;
  color: #f7ffdf;
}

.auth-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.76);
}

.auth-hero__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.auth-hero__stat {
  padding: 18upx 20upx;
  border-radius: 26upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.auth-hero__stat-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.auth-hero__stat-value {
  display: block;
  margin-top: 8upx;
  font-size: 28upx;
  font-weight: 800;
  color: #f7ffdf;
}

.auth-section {
  display: flex;
  flex-direction: column;
}

.auth-field {
  margin-top: 18upx;
}

.auth-field__label {
  display: block;
  margin-bottom: 10upx;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.auth-input,
.auth-choice {
  min-height: 88upx;
  padding: 0 22upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  align-items: center;
  gap: 14upx;
}

.auth-choice {
  padding-top: 18upx;
  padding-bottom: 18upx;
  min-height: 0;
}

.auth-input__inner {
  flex: 1;
  height: 88upx;
  font-size: 24upx;
  color: #243123;
}

.auth-submit {
  margin-top: 24upx;
}

.auth-links {
  margin-top: 18upx;
  display: flex;
  justify-content: space-between;
  gap: 16upx;
}

.auth-link {
  font-size: 22upx;
  color: #556556;
}

.auth-link--strong {
  color: #172119;
  font-weight: 800;
}

.auth-wechat {
  margin-top: 18upx;
  padding: 18upx 20upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.86);
  border: 1upx solid rgba(205, 224, 145, 0.92);
  display: flex;
  align-items: center;
  gap: 16upx;
}

.auth-wechat__icon {
  width: 72upx;
  height: 72upx;
  border-radius: 22upx;
  background: rgba(164, 215, 88, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-wechat__icon-image {
  width: 40upx;
  height: 40upx;
}

.auth-wechat__main {
  flex: 1;
  min-width: 0;
}

.auth-wechat__title,
.auth-tip__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.auth-wechat__desc,
.auth-tip__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}
</style>
