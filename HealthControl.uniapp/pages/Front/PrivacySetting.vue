<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack privacy-screen">
      <hc-topbar title="隐私设置" subtitle="管理本地缓存、授权状态和数据权利" :show-back="true" @left="back" />

      <view class="hc-card-lime privacy-hero hc-reveal-up">
        <view class="hc-kicker">数据控制台</view>
        <text class="privacy-hero__title">把隐私授权、留存策略和导出删除申请放到同一套清晰面板里。</text>
        <view class="privacy-hero__stats">
          <view class="privacy-hero__stat">
            <text class="hc-stat-value">{{ formData.PrivacyAgreeStatus ? "已同意" : "未同意" }}</text>
            <text class="hc-stat-label">协议状态</text>
          </view>
          <view class="privacy-hero__stat privacy-hero__stat--dark">
            <text class="hc-stat-value hc-stat-value--dark">{{ formData.DataRetentionEnabled ? currentRetentionShort : "关闭" }}</text>
            <text class="hc-stat-label">留存策略</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft privacy-section hc-reveal-up" style="--delay: 80ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">本地与留存</text>
          <text class="hc-section-head__meta">先控制设备数据，再控制保留时长</text>
        </view>

        <view class="privacy-setting-card">
          <view class="privacy-setting-card__head">
            <view class="privacy-setting-card__main">
              <text class="privacy-setting-card__title">本地存储</text>
              <text class="privacy-setting-card__desc">
                {{ formData.LocalStorageStatus ? "已开启，允许缓存音频记录和报告以提升加载速度。" : "已关闭，不缓存本地数据，更适合精简存储空间。" }}
              </text>
            </view>
            <hc-toggle :model-value="!!formData.LocalStorageStatus" @change="onLocalStorageChange" />
          </view>
        </view>

        <view class="privacy-setting-card">
          <view class="privacy-setting-card__head">
            <view class="privacy-setting-card__main">
              <text class="privacy-setting-card__title">数据留存与自动清理</text>
              <text class="privacy-setting-card__desc">到期数据会按策略自动软删除，减少长期留存风险。</text>
            </view>
            <hc-toggle :model-value="!!formData.DataRetentionEnabled" @change="onRetentionSwitchChange" />
          </view>
          <view class="privacy-setting-card__foot">
            <text class="privacy-setting-card__label">当前策略</text>
            <view class="privacy-setting-card__picker-wrap">
              <view class="hc-official-select-trigger" @click="openRetentionPopup">
                <text class="hc-official-select-trigger__value">{{ currentRetentionLabel }}</text>
                <uni-icons type="down" size="18" color="#5e705d" />
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="hc-card-soft privacy-section hc-reveal-up" style="--delay: 140ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">授权与协议</text>
          <text class="hc-section-head__meta">重要状态集中查看</text>
        </view>

        <view class="privacy-setting-card">
          <view class="privacy-setting-card__head">
            <view class="privacy-setting-card__main">
              <text class="privacy-setting-card__title">匿名数据授权</text>
              <text class="privacy-setting-card__desc">
                {{ formData.DataAnonymousAuth ? "已开启，你授权的匿名标注数据可用于模型优化。" : "已关闭，当前数据仅用于个人记录和服务流程。" }}
              </text>
            </view>
            <hc-toggle :model-value="!!formData.DataAnonymousAuth" @change="onAnonymousAuthChange" />
          </view>
          <text class="privacy-setting-card__hint">关闭总开关不会影响你之前已授权的标注记录。</text>
          <view class="privacy-inline-actions">
            <view class="privacy-inline-action" @click="goAuthUsage">查看授权明细</view>
          </view>
        </view>

        <view class="privacy-setting-card">
          <view class="privacy-setting-card__head">
            <view class="privacy-setting-card__main">
              <text class="privacy-setting-card__title">隐私协议</text>
              <text class="privacy-setting-card__desc">建议在继续使用前确认协议内容，了解信息收集与使用范围。</text>
            </view>
            <view class="privacy-status-pill" :class="{ 'privacy-status-pill--ok': formData.PrivacyAgreeStatus }">
              {{ formData.PrivacyAgreeStatus ? "已同意" : "未同意" }}
            </view>
          </view>
          <view class="privacy-inline-actions">
            <view class="privacy-inline-action" @click="goProtocol">查看协议</view>
            <view v-if="!formData.PrivacyAgreeStatus" class="privacy-inline-action privacy-inline-action--brand" @click="agreeProtocol">
              阅读并同意
            </view>
          </view>
        </view>
      </view>

      <view class="hc-card-soft privacy-section hc-reveal-up" style="--delay: 200ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">我的数据权利</text>
          <text class="hc-section-head__meta">导出、删除和历史记录</text>
        </view>
        <view class="privacy-rights-grid">
          <view class="privacy-right-card hc-interactive-card" @click="goExport">
            <text class="privacy-right-card__title">申请导出数据</text>
            <text class="privacy-right-card__desc">导出个人数据副本，用于归档或备份。</text>
          </view>
          <view class="privacy-right-card privacy-right-card--warn hc-interactive-card" @click="goDelete">
            <text class="privacy-right-card__title">申请删除数据</text>
            <text class="privacy-right-card__desc">提交高风险操作申请，按流程确认处理。</text>
          </view>
          <view class="privacy-right-card privacy-right-card--wide hc-interactive-card" @click="goHistory">
            <text class="privacy-right-card__title">查看申请记录</text>
            <text class="privacy-right-card__desc">统一回看历史导出、删除申请与处理状态。</text>
          </view>
        </view>
      </view>

      <uni-popup
        ref="retentionPopupRef"
        type="center"
        :safe-area="false"
        background-color="transparent"
        mask-background-color="rgba(16, 24, 19, 0.58)"
      >
        <view class="hc-official-popup-card">
          <view class="hc-official-popup-handle"></view>
          <view class="hc-official-popup-head">
            <text class="hc-official-popup-eyebrow">筛选与选择</text>
            <text class="hc-official-popup-title">选择数据留存周期</text>
            <text class="hc-official-popup-subtitle">用于自动清理过期历史数据</text>
          </view>
          <view class="hc-official-popup-summary">
            <text class="hc-official-popup-summary__label">当前</text>
            <text class="hc-official-popup-summary__value">{{ currentRetentionLabel }}</text>
          </view>
          <scroll-view scroll-y class="hc-official-popup-list">
            <view
              v-for="option in retentionOptions"
              :key="String(option.value)"
              class="hc-official-popup-option"
              :class="{ active: (formData.DataRetentionMonths ?? 0) === option.value }"
              @click="onRetentionPickerChange(option)"
            >
              <text class="hc-official-popup-option__text">{{ option.text }}</text>
              <text v-if="(formData.DataRetentionMonths ?? 0) === option.value" class="hc-official-popup-option__mark">当前</text>
            </view>
          </scroll-view>
        </view>
      </uni-popup>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcToggle from "@/components/hc-toggle/hc-toggle.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const formData = ref({
  Id: null,
  UserId: null,
  LocalStorageStatus: false,
  DataAnonymousAuth: false,
  PrivacyAgreeStatus: false,
  DataRetentionEnabled: false,
  DataRetentionMonths: null,
  IsDelete: false,
});
const dialogRef = ref(null);
const retentionPopupRef = ref(null);

const retentionOptions = [
  { value: 0, text: "永久保留" },
  { value: 3, text: "保留最近 3 个月" },
  { value: 6, text: "保留最近 6 个月" },
  { value: 12, text: "保留最近 12 个月" },
];

const currentRetentionLabel = computed(() => {
  const found = retentionOptions.find((item) => item.value === formData.value.DataRetentionMonths);
  return found ? found.text : "永久保留";
});

const currentRetentionShort = computed(() => {
  const months = formData.value.DataRetentionMonths;
  return months ? `${months}个月` : "长期";
});

const back = () => uni.navigateBack({ delta: 1 });
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const loadSetting = async () => {
  const { Data, Success } = await Post("/Front/Privacy/GetSetting", {});
  if (Success && Data) formData.value = { ...formData.value, ...Data };
};

onShow(loadSetting);

const saveSetting = async (patch) => {
  const payload = { ...formData.value, ...patch };
  const { Data, Success } = await Post("/Front/Privacy/UpdateSetting", payload);
  if (Success && Data) {
    formData.value = { ...formData.value, ...Data };
    uni.showToast({ title: "\u5df2\u4fdd\u5b58", icon: "success" });
    return true;
  }
  return false;
};

const onLocalStorageChange = async (newValue) => {
  if (!newValue && formData.value.LocalStorageStatus) {
    const result = await showDialog({
      title: "关闭本地存储",
      content: "关闭后将不再缓存数据，是否确认关闭？",
    });
    if (!result.confirm) return;
    await saveSetting({ LocalStorageStatus: false });
    return;
  }
  await saveSetting({ LocalStorageStatus: newValue });
};

const onAnonymousAuthChange = async (newValue) => {
  if (newValue && !formData.value.DataAnonymousAuth) {
    const result = await showDialog({
      title: "开启匿名授权",
      content: "开启后，你授权的匿名标注数据可用于模型优化，是否确认开启？",
    });
    if (!result.confirm) return;
    await saveSetting({ DataAnonymousAuth: true });
    return;
  }
  if (!newValue && formData.value.DataAnonymousAuth) {
    const result = await showDialog({
      title: "关闭匿名授权",
      content: "关闭后数据仅用于个人记录和当前服务，不影响既有授权记录，是否确认关闭？",
    });
    if (!result.confirm) return;
    await saveSetting({ DataAnonymousAuth: false });
    return;
  }
  await saveSetting({ DataAnonymousAuth: newValue });
};

const goProtocol = () => uni.navigateTo({ url: "/pages/Front/PrivacyProtocol" });
const goExport = () => uni.navigateTo({ url: "/pages/Front/DataExportRequest" });
const goDelete = () => uni.navigateTo({ url: "/pages/Front/DataDeleteRequest" });
const goHistory = () => uni.navigateTo({ url: "/pages/Front/DataRightsHistory" });
const goAuthUsage = () => uni.navigateTo({ url: "/pages/Front/AuthUsageDetail" });
const openRetentionPopup = () => retentionPopupRef.value?.open();

const agreeProtocol = async () => {
  uni.showLoading({ title: "提交中..." });
  try {
    const { Success } = await Post("/Front/Privacy/AgreeProtocol", {});
    if (Success) {
      await loadSetting();
      uni.showToast({ title: "\u5df2\u540c\u610f", icon: "success" });
    }
  } finally {
    uni.hideLoading();
  }
};

const onRetentionSwitchChange = async (enabled) => {
  if (enabled && !formData.value.DataRetentionEnabled) {
    const targetMonths = formData.value.DataRetentionMonths || 6;
    const result = await showDialog({
      title: "开启自动清理",
      content: "开启后，超出保留时长的历史数据会自动软删除，是否确认开启？",
    });
    if (!result.confirm) return;
    await saveSetting({ DataRetentionEnabled: true, DataRetentionMonths: targetMonths });
  } else if (formData.value.DataRetentionEnabled) {
    const result = await showDialog({
      title: "关闭自动清理",
      content: "关闭后将不再自动清理历史数据，是否确认关闭？",
    });
    if (!result.confirm) return;
    await saveSetting({ DataRetentionEnabled: false });
  }
};

const onRetentionPickerChange = async (option) => {
  const target = option || retentionOptions[0];
  retentionPopupRef.value?.close();
  await saveSetting({ DataRetentionMonths: target.value });
};
</script>

<style scoped lang="scss">
.privacy-screen { padding-bottom: calc(env(safe-area-inset-bottom) + 100upx); }
.privacy-hero__title { display: block; margin-top: 16upx; font-size: 44upx; line-height: 1.18; font-weight: 800; color: #152012; }
.privacy-hero__stats { margin-top: 24upx; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12upx; }
.privacy-hero__stat { padding: 22upx; border-radius: 30upx; background: rgba(248, 252, 239, 0.82); border: 1upx solid rgba(205, 224, 145, 0.88); }
.privacy-hero__stat--dark { background: linear-gradient(135deg, #101612 0%, #1d271f 100%); border-color: #101612; }
.privacy-section { display: flex; flex-direction: column; gap: 18upx; }
.privacy-setting-card { padding: 24upx; border-radius: 34upx; background: rgba(248, 252, 239, 0.84); border: 1upx solid rgba(205, 224, 145, 0.88); }
.privacy-setting-card__head { display: flex; align-items: flex-start; justify-content: space-between; gap: 16upx; }
.privacy-setting-card__main { flex: 1; }
.privacy-setting-card__title { display: block; font-size: 30upx; font-weight: 800; color: var(--text-color); }
.privacy-setting-card__desc { display: block; margin-top: 10upx; font-size: 22upx; line-height: 1.65; color: var(--text-color-light); }
.privacy-setting-card__hint { display: block; margin-top: 12upx; font-size: 20upx; line-height: 1.6; color: var(--text-color-lighter); }
.privacy-setting-card__foot { margin-top: 18upx; display: flex; align-items: center; justify-content: space-between; gap: 12upx; }
.privacy-setting-card__label { font-size: 22upx; color: var(--text-color-light); }
.privacy-setting-card__picker-wrap { width: 300upx; max-width: 100%; }
.privacy-inline-actions { margin-top: 18upx; display: flex; flex-wrap: wrap; gap: 12upx; }
.privacy-inline-action { min-height: 62upx; padding: 0 22upx; border-radius: 999upx; display: inline-flex; align-items: center; justify-content: center; background: rgba(24, 33, 27, 0.08); border: 1upx solid rgba(205, 224, 145, 0.88); font-size: 22upx; font-weight: 800; color: var(--text-color); }
.privacy-inline-action--brand { background: linear-gradient(135deg, #101612 0%, #1d271f 100%); color: #f7ffdf; border-color: #101612; }
.privacy-status-pill { min-height: 54upx; padding: 0 18upx; border-radius: 999upx; display: inline-flex; align-items: center; justify-content: center; background: rgba(90, 102, 93, 0.12); color: #546355; font-size: 20upx; font-weight: 800; }
.privacy-status-pill--ok { background: rgba(142, 201, 79, 0.18); color: #456133; }
.privacy-rights-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12upx; }
.privacy-right-card { min-height: 208upx; padding: 24upx; border-radius: 32upx; background: rgba(248, 252, 239, 0.84); border: 1upx solid rgba(205, 224, 145, 0.88); }
.privacy-right-card--warn { background: rgba(250, 235, 228, 0.92); border-color: rgba(234, 117, 81, 0.22); }
.privacy-right-card--wide { grid-column: 1 / -1; }
.privacy-right-card__title { display: block; font-size: 28upx; font-weight: 800; color: var(--text-color); }
.privacy-right-card__desc { display: block; margin-top: 10upx; font-size: 22upx; line-height: 1.65; color: var(--text-color-light); }
</style>
