<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack profile-edit-screen">
      <hc-topbar title="个人信息" subtitle="补齐基础资料，让记录、提醒和就医辅助更贴合当前状态" :show-back="true" @left="back" />

      <view class="hc-card-lime profile-edit-hero hc-reveal-up">
        <view class="hc-kicker">资料维护</view>
        <text class="profile-edit-hero__title">头像、昵称、联系方式和出生时间都会影响你的使用体验</text>
        <text class="profile-edit-hero__desc">这里只做界面主页化改造，提交逻辑仍保持原有 `/User/CreateOrEdit` 流程不变。</text>
      </view>

      <view class="hc-card-soft profile-edit-form hc-reveal-up" style="--delay: 90ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">资料表单</text>
          <text class="bg-section-head__meta">编辑后会同步刷新个人中心信息</text>
        </view>

        <uni-forms ref="editModalForm" :model="formData" :rules="editModalFormRules" label-position="top">
          <uni-forms-item label="名称" required name="Name">
            <uni-easyinput v-model="formData.Name" :inputBorder="false" placeholder="请输入名称" />
          </uni-forms-item>
          <uni-forms-item label="邮箱" required name="Email">
            <uni-easyinput v-model="formData.Email" :inputBorder="false" placeholder="请输入邮箱" />
          </uni-forms-item>
          <uni-forms-item label="手机号" required name="PhoneNumber">
            <uni-easyinput v-model="formData.PhoneNumber" :inputBorder="false" placeholder="请输入手机号" />
          </uni-forms-item>
          <uni-forms-item label="出生日期" required name="Birth">
            <picker mode="date" :value="birthPickerValue" @change="onBirthChange">
              <view class="hc-official-select-trigger profile-edit-birth-trigger" :class="{ 'is-placeholder': !formData.Birth }">
                <text class="hc-official-select-trigger__value">{{ formData.Birth || "请选择出生日期" }}</text>
                <uni-icons type="calendar" size="18" color="#5e705d" />
              </view>
            </picker>
          </uni-forms-item>
          <uni-forms-item label="头像" name="ImageUrls">
            <upload-images v-model="formData.ImageUrls" :limit="1" />
          </uni-forms-item>
        </uni-forms>

        <view class="hc-pill-button-dark hc-interactive-pill profile-edit-submit" @click="createOrEditAsync">保存个人信息</view>
      </view>

      <view class="hc-card-soft profile-edit-tip hc-reveal-up" style="--delay: 150ms">
        <text class="profile-edit-tip__title">提交后会自动刷新</text>
        <text class="profile-edit-tip__desc">保存成功后会重新拉取用户资料，并返回上一页，保持你当前的使用节奏不被打断。</text>
      </view>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { onLoad } from "@dcloudio/uni-app";
import { computed, nextTick, reactive, ref } from "vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import UploadImages from "@/components/upload-images/upload-images.vue";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";

const commonStore = useCommonStore();
const editModalForm = ref(null);
const dialogRef = ref(null);
const userId = computed(() => commonStore.UserId);
const userInfo = computed(() => commonStore.UserInfo || {});

const formData = reactive({
  Name: "",
  Email: "",
  PhoneNumber: "",
  Birth: "",
  ImageUrls: "",
});

const editModalFormRules = {
  Name: { rules: [{ required: true, errorMessage: "该项为必填项" }] },
  Email: { rules: [{ required: true, errorMessage: "该项为必填项" }] },
  PhoneNumber: { rules: [{ required: true, errorMessage: "该项为必填项" }] },
  Birth: { rules: [{ required: true, errorMessage: "该项为必填项" }] },
  ImageUrls: { rules: [{ required: true, errorMessage: "该项为必填项" }] },
};
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });
const birthPickerValue = computed(() => formData.Birth || getLocalDateString());

const getLocalDateString = (input = new Date()) => {
  const date = input instanceof Date ? input : new Date(input);
  if (Number.isNaN(date.getTime())) return "";
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const normalizeBirthDate = (value) => {
  if (!value) return "";
  if (typeof value === "string") {
    const matched = value.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (matched) return matched[0];
  }
  return getLocalDateString(value);
};

const formatBirthForSubmit = (value) => {
  const normalized = normalizeBirthDate(value);
  return normalized ? `${normalized}T00:00:00` : "";
};

const onBirthChange = (event) => {
  formData.Birth = normalizeBirthDate(event?.detail?.value);
};

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/UserCenter" });
};

const createOrEditAsync = async () => {
  try {
    await editModalForm.value.validate();
  } catch (error) {
    return;
  }

  const result = await showDialog({
    title: "提示",
    content: "你确定要保存这些资料吗？",
  });
  if (!result.confirm) return;

  let Success = false;
  try {
    ({ Success } = await Post("/User/CreateOrEdit", {
      Id: userId.value,
      UserName: userInfo.value.UserName,
      Name: formData.Name,
      Email: formData.Email,
      PhoneNumber: formData.PhoneNumber,
      Birth: formatBirthForSubmit(formData.Birth),
      ImageUrls: formData.ImageUrls,
    }));
  } catch (error) {
    console.error("保存个人信息失败", error);
    uni.showToast({ title: "保存失败，请稍后重试", icon: "none" });
    return;
  }

  if (!Success) {
    uni.showToast({ title: "保存失败，请稍后重试", icon: "none" });
    return;
  }

  uni.showToast({ title: "操作成功", icon: "success" });
  await commonStore.GetInfo();
  uni.navigateBack({ delta: 1 });
};

onLoad(async () => {
  const { Data } = await commonStore.GetInfo();
  Object.assign(formData, {
    Name: Data?.Name || "",
    Email: Data?.Email || "",
    PhoneNumber: Data?.PhoneNumber || "",
    Birth: normalizeBirthDate(Data?.Birth || ""),
    ImageUrls: Data?.ImageUrls || "",
  });
  await nextTick();
  editModalForm.value?.setRules(editModalFormRules);
});
</script>

<style scoped lang="scss">
.profile-edit-screen {
  gap: 24upx;
}

.profile-edit-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.profile-edit-hero__title {
  display: block;
  font-size: 42upx;
  line-height: 1.14;
  font-weight: 800;
  color: #172119;
}

.profile-edit-hero__desc,
.profile-edit-tip__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.profile-edit-submit {
  margin-top: 10upx;
}

.profile-edit-birth-trigger {
  min-height: 92upx;
  padding: 0 22upx;
}

.profile-edit-tip__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}
</style>
