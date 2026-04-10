<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack password-screen">
      <hc-topbar title="修改密码" subtitle="更新账号安全设置并继续保护你的健康数据" :show-back="true" @left="back" />

      <view class="hc-card-dark password-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">账号安全</view>
        <text class="password-hero__title">密码更新后会立即生效，建议定期更换并避免与其他平台重复</text>
        <text class="password-hero__desc">确认修改成功后，系统会按原逻辑退出当前登录状态，方便你使用新密码重新进入控制台。</text>
      </view>

      <view class="hc-card-soft password-form hc-reveal-up" style="--delay: 90ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">密码表单</text>
          <text class="bg-section-head__meta">先验证旧密码，再输入两次新密码</text>
        </view>

        <uni-forms ref="editModalForm" :model="formData" :rules="rules" label-position="top">
          <uni-forms-item label="原始密码" required name="OrginPassword">
            <uni-easyinput v-model="formData.OrginPassword" type="password" :password-icon="true" :inputBorder="false" placeholder="请输入原始密码" />
          </uni-forms-item>
          <uni-forms-item label="新密码" required name="NewPassword">
            <uni-easyinput v-model="formData.NewPassword" type="password" :password-icon="true" :inputBorder="false" placeholder="请输入新密码" />
          </uni-forms-item>
          <uni-forms-item label="确认新密码" required name="TwoPassword">
            <uni-easyinput v-model="formData.TwoPassword" type="password" :password-icon="true" :inputBorder="false" placeholder="请再次输入新密码" />
          </uni-forms-item>
        </uni-forms>

        <view class="hc-pill-button-dark hc-interactive-pill password-submit" @click="createOrEdit">确认修改密码</view>
      </view>

      <view class="hc-card-lime password-tip hc-reveal-up" style="--delay: 150ms">
        <text class="password-tip__title">修改后会发生什么</text>
        <text class="password-tip__desc">系统会保留你的资料和记录，但会退出当前登录状态；重新登录后即可继续查看数据。</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";

const commonStore = useCommonStore();
const editModalForm = ref(null);
const userId = computed(() => commonStore.UserId);

const formData = reactive({
  OrginPassword: "",
  NewPassword: "",
  TwoPassword: "",
});

const rules = {
  OrginPassword: {
    rules: [{ required: true, errorMessage: "请输入原始密码" }],
  },
  NewPassword: {
    rules: [{ required: true, errorMessage: "请输入新密码" }],
  },
  TwoPassword: {
    rules: [{ required: true, errorMessage: "请再次输入新密码" }],
  },
};

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/UserCenter" });
};

const createOrEdit = async () => {
  try {
    await editModalForm.value.validate();
  } catch (error) {
    return;
  }

  if (formData.NewPassword !== formData.TwoPassword) {
    uni.showToast({ title: "两次输入的新密码不一致", icon: "none" });
    return;
  }

  const { Success } = await Post("/User/ChangePassword", {
    Id: userId.value,
    OrginPassword: formData.OrginPassword,
    Password: formData.TwoPassword,
  });

  if (!Success) return;
  await commonStore.Logout();
};
</script>

<style scoped lang="scss">
.password-screen {
  gap: 24upx;
}

.password-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.password-hero__title {
  display: block;
  font-size: 46upx;
  line-height: 1.12;
  font-weight: 800;
  color: #f7ffdf;
}

.password-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.76);
}

.password-submit {
  margin-top: 10upx;
}

.password-tip__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.password-tip__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}
</style>
