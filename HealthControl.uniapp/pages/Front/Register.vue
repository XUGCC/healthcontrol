<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack auth-screen">
      <hc-topbar title="创建账号" subtitle="保留原有注册流程，只把界面统一到主页风格" :show-back="true" @left="back" />

      <view class="hc-card-dark auth-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">账号开通</view>
        <text class="auth-hero__title">把检测记录、症状变化和提醒节奏统一保存到同一个账号里</text>
        <text class="auth-hero__desc">注册成功后就能持续记录你的自查结果，也更方便后续同步问卷、饮食和就医辅助数据。</text>
        <view class="auth-hero__stats">
          <view class="auth-hero__stat">
            <text class="auth-hero__stat-label">适用场景</text>
            <text class="auth-hero__stat-value">长期追踪</text>
          </view>
          <view class="auth-hero__stat">
            <text class="auth-hero__stat-label">账号体系</text>
            <text class="auth-hero__stat-value">手机号绑定</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft auth-section hc-reveal-up" style="--delay: 90ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">注册信息</text>
          <text class="bg-section-head__meta">先补齐基础资料</text>
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
          <text class="auth-field__label">手机号</text>
          <view class="auth-input">
            <uni-icons type="phone" size="18" color="#18211b" />
            <input v-model="formData.PhoneNumber" class="auth-input__inner" type="number" placeholder="请输入手机号" />
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

        <view class="hc-pill-button-dark hc-interactive-pill auth-submit" @click="register">注册并返回登录</view>

        <view class="auth-links auth-links--single">
          <text class="auth-link auth-link--strong" @click="toLogin">已有账号，立即登录</text>
        </view>
      </view>

      <view class="hc-card-lime auth-tip hc-reveal-up" style="--delay: 150ms">
        <text class="auth-tip__title">隐私说明</text>
        <text class="auth-tip__desc">注册信息仅用于账号识别、记录同步和个性化建议，不会因为注册本身自动开启额外的数据授权。</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onLoad } from "@dcloudio/uni-app";
import { reactive, ref } from "vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import { GetLoginCode } from "@/utils/comm";
import { Post } from "@/utils/http";

const formData = reactive({
  UserName: "",
  Password: "",
  RoleType: "2",
  PhoneNumber: "",
  Name: "",
});

const roleTypeList = ref([]);

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/Login" });
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

const register = async () => {
  if (!formData.UserName) {
    uni.showToast({ title: "请输入账号", icon: "none" });
    return;
  }
  if (!formData.Password) {
    uni.showToast({ title: "请输入密码", icon: "none" });
    return;
  }
  if (!formData.PhoneNumber) {
    uni.showToast({ title: "请输入手机号", icon: "none" });
    return;
  }
  if (!/^\d{11}$/.test(formData.PhoneNumber)) {
    uni.showToast({ title: "请输入正确的手机号", icon: "none" });
    return;
  }

  formData.Name = Math.random().toString(36).slice(2, 10);

  const wxCode = await GetLoginCode();
  const { Success } = await Post("/User/Register", {
    ...formData,
    WxCode: wxCode,
  });

  if (!Success) return;

  Object.assign(formData, {
    UserName: "",
    Password: "",
    RoleType: "2",
    PhoneNumber: "",
    Name: "",
  });

  uni.redirectTo({ url: "/pages/Front/Login" });
};

const toLogin = () => {
  uni.redirectTo({ url: "/pages/Front/Login" });
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

.auth-links--single {
  justify-content: center;
}

.auth-link {
  font-size: 22upx;
  color: #556556;
}

.auth-link--strong {
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
