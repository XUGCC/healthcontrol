<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack auth-screen">
      <hc-topbar title="重置密码" subtitle="通过账号与手机号找回访问权限" :show-back="true" @left="back" />

      <view class="hc-card-dark auth-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">账号安全</view>
        <text class="auth-hero__title">先验证账号和手机号，再设置新的登录密码</text>
        <text class="auth-hero__desc">密码重置后会立即应用到当前账号，建议使用包含字母和数字的强密码。</text>
      </view>

      <view class="hc-card-soft auth-section hc-reveal-up" style="--delay: 90ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">验证信息</text>
          <text class="bg-section-head__meta">填写后即可提交重置</text>
        </view>

        <view class="auth-field">
          <text class="auth-field__label">账号</text>
          <view class="auth-input">
            <uni-icons type="person" size="18" color="#18211b" />
            <input v-model="formData.UserName" class="auth-input__inner" type="text" placeholder="请输入账号" />
          </view>
        </view>

        <view class="auth-field">
          <text class="auth-field__label">手机号</text>
          <view class="auth-input">
            <uni-icons type="phone" size="18" color="#18211b" />
            <input v-model="formData.PhoneNumber" class="auth-input__inner" type="number" placeholder="请输入手机号" />
          </view>
        </view>

        <view class="auth-field">
          <text class="auth-field__label">新密码</text>
          <view class="auth-input">
            <uni-icons type="locked" size="18" color="#18211b" />
            <input v-model="formData.Password" class="auth-input__inner" type="password" :password="true" placeholder="请输入新密码" />
          </view>
        </view>

        <view class="hc-pill-button-dark hc-interactive-pill auth-submit" @click="resetPassword">确认重置密码</view>

        <view class="auth-links auth-links--single">
          <text class="auth-link auth-link--strong" @click="toLogin">返回登录</text>
        </view>
      </view>

      <view class="hc-card-lime auth-tip hc-reveal-up" style="--delay: 150ms">
        <text class="auth-tip__title">安全建议</text>
        <text class="auth-tip__desc">请避免与其他平台共用密码，重置完成后建议重新登录并检查账号绑定状态。</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import { Post } from "@/utils/http";

const formData = ref({
  UserName: "",
  Password: "",
  PhoneNumber: "",
});

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/Login" });
};

const resetPassword = async () => {
  if (!formData.value.UserName) {
    uni.showToast({ title: "请输入账号", icon: "none" });
    return;
  }
  if (!formData.value.PhoneNumber) {
    uni.showToast({ title: "请输入手机号", icon: "none" });
    return;
  }
  if (!formData.value.Password) {
    uni.showToast({ title: "请输入新密码", icon: "none" });
    return;
  }

  const { Success } = await Post("/User/ForgetPassword", formData.value);
  if (!Success) return;

  uni.showToast({ title: "密码重置成功", icon: "success" });
  setTimeout(() => {
    uni.redirectTo({ url: "/pages/Front/Login" });
  }, 500);
};

const toLogin = () => {
  uni.redirectTo({ url: "/pages/Front/Login" });
};
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
  font-size: 46upx;
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

.auth-input {
  min-height: 88upx;
  padding: 0 22upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  align-items: center;
  gap: 14upx;
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
  justify-content: center;
}

.auth-link {
  font-size: 22upx;
  color: #172119;
  font-weight: 800;
}

.auth-tip__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.auth-tip__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}
</style>
