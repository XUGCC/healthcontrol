<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack model-form-screen">
      <hc-topbar
        :title="isEditMode ? '编辑标注' : '确诊标注'"
        :subtitle="isEditMode ? '继续完善已有标注记录' : '把确诊结果整理成结构化反馈'"
        :show-back="true"
        @left="back"
      />

      <view class="hc-card-lime model-form-hero hc-reveal-up">
        <view class="hc-kicker">结构化反馈</view>
        <text class="model-form-hero__title">{{ selectedDetect ? "先确认检测记录，再补充医院确诊结果" : "选择一次检测记录，开始填写确诊反馈" }}</text>
        <text class="model-form-hero__desc">保持原有创建、编辑、授权和隐私校验逻辑不变，只把表单拆成更容易扫描和操作的主页化模块。</text>
      </view>

      <view class="hc-card-soft model-form-section hc-reveal-up" style="--delay: 90ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">关联检测记录</text>
          <text class="bg-section-head__meta">优先选择需要反馈的那次检测</text>
        </view>
        <view class="model-form-picker">
          <view class="hc-official-select-trigger" :class="{ 'is-placeholder': !selectedDetectLabel }" @click="openDetectPopup">
            <text class="hc-official-select-trigger__value">{{ selectedDetectLabel || "请选择检测记录" }}</text>
            <uni-icons type="down" size="18" color="#5e705d" />
          </view>
        </view>
      </view>

      <view class="hc-card-soft model-form-section hc-reveal-up" style="--delay: 140ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">医院确诊结果</text>
          <text class="bg-section-head__meta">支持快速标签和手动输入</text>
        </view>

        <view class="model-form-tags">
          <view
            v-for="item in quickResults"
            :key="item.label"
            class="model-form-tags__item hc-interactive-pill"
            @click="selectQuickResult(item.value)"
          >
            {{ item.label }}
          </view>
        </view>

        <textarea
          v-model="formData.HospitalDiagnoseResult"
          class="model-form-textarea"
          placeholder="请输入医院确诊结果，如：声带小结、声带息肉、喉癌等"
          maxlength="500"
          :show-confirm-bar="false"
          @blur="autoJudgeLabelType"
        />
        <text class="model-form-count">{{ formData.HospitalDiagnoseResult.length }}/500</text>
      </view>

      <view class="hc-card-soft model-form-section hc-reveal-up" style="--delay: 190ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">标注类型</text>
          <text class="bg-section-head__meta">根据模型判断和实际结果选择确诊或误报</text>
        </view>

        <view class="model-form-choice">
          <view
            class="model-form-choice__item hc-interactive-card"
            :class="{ active: formData.LabelType === false }"
            @click="setLabelType(false)"
          >
            <text class="model-form-choice__title">误报</text>
            <text class="model-form-choice__desc">模型判断为恶性，但实际结果不是恶性</text>
          </view>
          <view
            class="model-form-choice__item hc-interactive-card"
            :class="{ active: formData.LabelType === true }"
            @click="setLabelType(true)"
          >
            <text class="model-form-choice__title">确诊</text>
            <text class="model-form-choice__desc">模型判断与实际确诊情况保持一致</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft model-form-section hc-reveal-up" style="--delay: 240ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">补充说明</text>
          <text class="bg-section-head__meta">可填写检查方式、医生建议等上下文</text>
        </view>
        <textarea
          v-model="formData.LabelDesc"
          class="model-form-textarea"
          placeholder="可补充说明确诊过程、检查方式、医生建议等信息"
          maxlength="512"
          :show-confirm-bar="false"
        />
        <text class="model-form-count">{{ formData.LabelDesc.length }}/512</text>
      </view>

      <view class="hc-card-soft model-form-section hc-reveal-up" style="--delay: 290ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">授权用于模型优化</text>
          <text class="bg-section-head__meta">会继续沿用原来的匿名授权校验逻辑</text>
        </view>
        <view class="model-form-auth">
          <view class="model-form-auth__main">
            <text class="model-form-auth__title">{{ formData.AuthStatus ? "当前标注将参与模型优化" : "当前标注仅保留在个人记录中" }}</text>
            <text class="model-form-auth__desc">
              {{ privacyAuthEnabled ? "全局匿名授权已开启，可以继续单独控制本条标注是否授权。" : "全局匿名授权未开启，尝试授权时仍会提示你先确认匿名授权设置。" }}
            </text>
          </view>
          <hc-toggle :model-value="formData.AuthStatus" @change="onAuthStatusChange" />
        </view>
      </view>

      <view class="hc-pill-button-dark hc-interactive-pill model-form-submit hc-reveal-up" style="--delay: 340ms" @click="submitForm">
        {{ isEditMode ? "保存修改" : "提交标注" }}
      </view>
    </view>
    <uni-popup
      ref="detectPopupRef"
      type="center"
      :safe-area="false"
      background-color="transparent"
      mask-background-color="rgba(16, 24, 19, 0.58)"
    >
      <view class="hc-official-popup-card">
        <view class="hc-official-popup-handle"></view>
        <view class="hc-official-popup-head">
          <text class="hc-official-popup-eyebrow">筛选与选择</text>
          <text class="hc-official-popup-title">选择关联检测记录</text>
          <text class="hc-official-popup-subtitle">优先选择需要反馈的那次检测</text>
        </view>
        <view class="hc-official-popup-summary">
          <text class="hc-official-popup-summary__label">当前</text>
          <text class="hc-official-popup-summary__value">{{ selectedDetectLabel || "未选择" }}</text>
        </view>
        <scroll-view scroll-y class="hc-official-popup-list">
          <view
            v-for="item in detectList"
            :key="item.Id"
            class="hc-official-popup-option"
            :class="{ active: formData.DetectId === item.Id }"
            @click="onDetectChange(item)"
          >
            <text class="hc-official-popup-option__text">{{ item.displayText }}</text>
            <text v-if="formData.DetectId === item.Id" class="hc-official-popup-option__mark">当前</text>
          </view>
        </scroll-view>
      </view>
    </uni-popup>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcToggle from "@/components/hc-toggle/hc-toggle.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";

const commonStore = useCommonStore();
const userId = computed(() => commonStore.UserId);

const formData = ref({
  Id: null,
  UserId: null,
  DetectId: null,
  HospitalDiagnoseResult: "",
  LabelType: null,
  LabelDesc: "",
  AuthStatus: false,
});

const detectList = ref([]);
const selectedDetect = ref(null);
const selectedDetectLabel = computed(() => selectedDetect.value?.displayText || "");
const privacyAuthEnabled = ref(false);
const privacySettingId = ref(null);
const isEditMode = computed(() => !!formData.value.Id);
const dialogRef = ref(null);
const detectPopupRef = ref(null);

const quickResults = [
  { label: "良性病变", value: "良性病变" },
  { label: "恶性病变", value: "恶性病变" },
  { label: "正常 / 无异常", value: "正常" },
  { label: "其他", value: "" },
];

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/ModelLabelList" });
};
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });
const openDetectPopup = () => detectPopupRef.value?.open();

const formatTime = (timeStr) => {
  if (!timeStr) return "";
  const date = new Date(timeStr);
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day} ${hours}:${minutes}`;
};

const getResultText = (item) => {
  if (item?.PrimaryScreenResult === 0) return "良性";
  if (item?.PrimaryScreenResult === 1) return "恶性倾向";
  return "未知";
};

const loadDetectList = async () => {
  const { Data, Success } = await Post("/Front/ModelLabel/DetectList", {
    UserId: userId.value,
    Page: 1,
    Limit: 100,
  });
  if (!Success) return;

  detectList.value = (Data?.Items || []).map((item) => ({
    ...item,
    displayText: `${formatTime(item.CreationTime)} - ${getResultText(item)} (${((item.PrimaryScreenConfidence || 0) * 100).toFixed(1)}%)`,
  }));

  if (formData.value.DetectId) {
    selectedDetect.value = detectList.value.find((item) => item.Id === formData.value.DetectId) || null;
  }
};

const loadPrivacySetting = async () => {
  try {
    const { Data, Success } = await Post("/TUserPrivacySetting/Get", {
      UserId: userId.value,
    });
    if (Success && Data) {
      privacySettingId.value = Data.Id || null;
      privacyAuthEnabled.value =
        Data.DataAnonymousAuth === true ||
        Data.DataAnonymousAuth === 1 ||
        Data.DataAnonymousAuth === "true" ||
        Data.DataAnonymousAuth === "1";
      return;
    }
  } catch (error) {
    console.error("loadPrivacySetting error:", error);
  }

  privacySettingId.value = null;
  privacyAuthEnabled.value = false;
};

const applyExistingLabel = (label) => {
  formData.value = {
    ...formData.value,
    Id: label.Id || null,
    UserId: userId.value,
    DetectId: label.DetectId || formData.value.DetectId,
    HospitalDiagnoseResult: label.HospitalDiagnoseResult || "",
    LabelType: label.LabelType,
    LabelDesc: label.LabelDesc || "",
    AuthStatus: !!label.AuthStatus,
  };
};

const resetLabelFields = () => {
  formData.value = {
    ...formData.value,
    Id: null,
    HospitalDiagnoseResult: "",
    LabelType: null,
    LabelDesc: "",
    AuthStatus: false,
  };
};

const loadExistingLabelByDetectId = async (detectId, showToast = true) => {
  if (!detectId) return;
  const { Data, Success } = await Post("/Front/ModelLabel/Get", { DetectId: detectId });
  if (Success && Data?.Id) {
    applyExistingLabel(Data);
    if (showToast) {
      uni.showToast({ title: "该检测记录已有标注，已切换为编辑模式", icon: "none" });
    }
    return;
  }

  resetLabelFields();
  formData.value.DetectId = detectId;
  formData.value.UserId = userId.value;
};

const onDetectChange = (item) => {
  selectedDetect.value = item || null;
  formData.value.DetectId = item?.Id || null;
  detectPopupRef.value?.close();
  loadExistingLabelByDetectId(formData.value.DetectId);
};

const autoJudgeLabelType = () => {
  if (!selectedDetect.value || !formData.value.HospitalDiagnoseResult) return;

  const detectResult = selectedDetect.value.PrimaryScreenResult;
  const diagnoseResult = formData.value.HospitalDiagnoseResult.toLowerCase();

  const isBenignDiagnose =
    diagnoseResult.includes("良性") ||
    diagnoseResult.includes("正常") ||
    diagnoseResult.includes("无异常") ||
    diagnoseResult.includes("小结") ||
    diagnoseResult.includes("息肉");

  const isMalignantDiagnose =
    diagnoseResult.includes("恶性") ||
    diagnoseResult.includes("癌") ||
    diagnoseResult.includes("肿瘤");

  if (detectResult === 1 && isBenignDiagnose) {
    formData.value.LabelType = false;
  } else if ((detectResult === 1 && isMalignantDiagnose) || (detectResult === 0 && isBenignDiagnose)) {
    formData.value.LabelType = true;
  }
};

const selectQuickResult = (value) => {
  if (!value) return;
  formData.value.HospitalDiagnoseResult = value;
  autoJudgeLabelType();
};

const setLabelType = (value) => {
  formData.value.LabelType = value;
};

const onAuthStatusChange = async (nextValue) => {
  if (nextValue && !privacyAuthEnabled.value) {
    const result = await showDialog({
      title: "需要开启匿名授权",
      content: "授权用于模型优化前，需要先开启全局匿名授权，是否前往确认？",
    });
    if (result.confirm) {
      goToLabelList();
    } else {
      formData.value.AuthStatus = false;
    }
    return;
  }

  formData.value.AuthStatus = nextValue;
};

const goToLabelList = () => {
  uni.navigateTo({ url: "/pages/Front/ModelLabelList" });
};

const submitForm = async () => {
  if (!formData.value.DetectId) {
    uni.showToast({ title: "请选择检测记录", icon: "none" });
    return;
  }
  if (!formData.value.HospitalDiagnoseResult.trim()) {
    uni.showToast({ title: "请输入确诊结果", icon: "none" });
    return;
  }
  if (formData.value.LabelType === null) {
    uni.showToast({ title: "请选择标注类型", icon: "none" });
    return;
  }

  if (formData.value.AuthStatus && !privacyAuthEnabled.value) {
    const result = await showDialog({
      title: "提示",
      content: "授权用于模型优化需要先开启匿名授权。系统将按原逻辑继续处理，是否提交？",
    });
    if (!result.confirm) return;
  }

  uni.showLoading({ title: "提交中..." });
  try {
    const { Success, Msg } = await Post("/Front/ModelLabel/CreateOrEdit", {
      ...formData.value,
      AuthStatus: !!formData.value.AuthStatus,
    });

    if (!Success) {
      uni.showToast({ title: Msg || "提交失败", icon: "none" });
      return;
    }

    uni.showToast({ title: isEditMode.value ? "修改成功" : "提交成功", icon: "success" });
    setTimeout(() => {
      uni.navigateBack({ delta: 1 });
    }, 600);
  } finally {
    uni.hideLoading();
  }
};

onLoad(async (options) => {
  if (!userId.value) {
    uni.showToast({ title: "请先登录", icon: "none" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/Front/Login" });
    }, 1200);
    return;
  }

  formData.value.UserId = userId.value;
  if (options?.detectId) {
    formData.value.DetectId = Number(options.detectId);
  }

  await loadDetectList();
  await loadPrivacySetting();

  if (formData.value.DetectId) {
    await loadExistingLabelByDetectId(formData.value.DetectId, false);
  }
});

onShow(async () => {
  if (userId.value) {
    await loadPrivacySetting();
  }
});
</script>

<style scoped lang="scss">
.model-form-screen {
  gap: 24upx;
}

.model-form-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.model-form-hero__title {
  display: block;
  font-size: 42upx;
  line-height: 1.14;
  font-weight: 800;
  color: #172119;
}

.model-form-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.model-form-picker {
  margin-top: 18upx;
}

.model-form-picker__value,
.model-form-textarea {
  width: 100%;
  min-height: 92upx;
  padding: 22upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  box-sizing: border-box;
  font-size: 24upx;
  color: #243123;
}

.model-form-textarea {
  min-height: 190upx;
  margin-top: 18upx;
  line-height: 1.7;
}

.model-form-tags {
  margin-top: 18upx;
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.model-form-tags__item {
  min-height: 58upx;
  padding: 0 20upx;
  border-radius: 999upx;
  background: rgba(225, 243, 154, 0.52);
  border: 1upx solid rgba(183, 210, 111, 0.8);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22upx;
  font-weight: 700;
  color: #172119;
}

.model-form-count {
  display: block;
  margin-top: 8upx;
  text-align: right;
  font-size: 20upx;
  color: #7c8e7b;
}

.model-form-choice {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.model-form-choice__item {
  padding: 20upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.86);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.model-form-choice__item.active {
  background: #172119;
  border-color: #172119;
}

.model-form-choice__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.model-form-choice__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.model-form-choice__item.active .model-form-choice__title,
.model-form-choice__item.active .model-form-choice__desc {
  color: #f7ffdf;
}

.model-form-auth {
  margin-top: 18upx;
  display: flex;
  align-items: center;
  gap: 18upx;
}

.model-form-auth__main {
  flex: 1;
}

.model-form-auth__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.model-form-auth__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}
</style>
