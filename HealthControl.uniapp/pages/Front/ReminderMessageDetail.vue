<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack reminder-detail-screen">
      <hc-topbar
        :title="'\u901a\u77e5\u8be6\u60c5'"
        :subtitle="'\u67e5\u770b\u63d0\u9192\u5185\u5bb9\u4e0e\u4e0b\u4e00\u6b65\u52a8\u4f5c'"
        :show-back="true"
        :right-text="'列表'"
        @left="goBack"
        @right="goBack"
      />

      <view v-if="notice" class="hc-card-dark reminder-detail-hero hc-reveal-up">
        <view class="reminder-detail-hero__top">
          <view class="reminder-detail-hero__chips">
            <view v-if="notice.Priority === 1" class="hc-kicker hc-kicker--dark">重要提醒</view>
            <view class="reminder-detail-chip" :class="typeClass(notice.Type)">{{ typeLabel(notice.Type) }}</view>
            <view v-if="notice.IsPinned" class="reminder-detail-chip reminder-detail-chip--pin">置顶</view>
          </view>
          <text class="reminder-detail-hero__time">{{ formatTime(notice.ActualSendTime || notice.PlanSendTime || notice.CreationTime) }}</text>
        </view>
        <text class="reminder-detail-hero__title">{{ notice.Title || "系统提醒" }}</text>
        <text class="reminder-detail-hero__desc">{{ notice.IsRead ? "这条提醒已读，可继续处理相关动作。" : "这条提醒已自动标记为已读，建议尽快处理相关动作。" }}</text>
      </view>

      <view v-if="notice" class="hc-card-soft reminder-detail-body hc-reveal-up" style="--delay: 100ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">提醒内容</text>
          <text class="bg-section-head__meta">{{ notice.ActionType ? "\u5e26\u52a8\u4f5c\u5165\u53e3" : "\u666e\u901a\u901a\u77e5" }}</text>
        </view>
        <text class="reminder-detail-body__content">{{ notice.Content || "暂无内容" }}</text>
      </view>

      <view v-if="notice" class="hc-card-soft reminder-detail-actions hc-reveal-up" style="--delay: 160ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">快捷操作</text>
          <text class="bg-section-head__meta">按当前状态即时调整</text>
        </view>
        <view class="reminder-detail-actions__grid">
          <view class="reminder-action-card hc-interactive-card" @click="togglePriority">
            <text class="reminder-action-card__title">{{ notice.Priority === 1 ? "取消重要" : "标记重要" }}</text>
            <text class="reminder-action-card__desc">调整这条提醒的优先级</text>
          </view>
          <view class="reminder-action-card hc-interactive-card" @click="togglePin">
            <text class="reminder-action-card__title">{{ notice.IsPinned ? "取消置顶" : "置顶提醒" }}</text>
            <text class="reminder-action-card__desc">让这条提醒保留在前面</text>
          </view>
        </view>
        <view v-if="notice.ActionType" class="hc-pill-button-dark" @click="handleAction">前往相关页面</view>
        <view class="hc-pill-button-soft reminder-detail-actions__danger" @click="deleteNotice">删除提醒</view>
      </view>

      <view v-else class="hc-card-soft reminder-detail-loading">
        <view class="reminder-detail-loading__spinner"></view>
        <text class="reminder-detail-loading__text">正在加载提醒详情...</text>
      </view>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const commonStore = useCommonStore();
const UserId = computed(() => commonStore.UserId);
const notice = ref(null);
const noticeId = ref(null);
const dialogRef = ref(null);

const goBack = () => uni.navigateBack();
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const typeLabel = (type) => {
  if (!type) return "其他";
  const value = type.toUpperCase();
  if (value === "CHECKUP") return "自查提醒";
  if (value === "VOICE_CARE") return "护嗓提醒";
  if (value === "FOLLOWUP") return "复查提醒";
  return type;
};

const typeClass = (type) => {
  if (!type) return "reminder-detail-chip--other";
  const value = type.toUpperCase();
  if (value === "CHECKUP") return "reminder-detail-chip--checkup";
  if (value === "VOICE_CARE") return "reminder-detail-chip--voice";
  if (value === "FOLLOWUP") return "reminder-detail-chip--followup";
  return "reminder-detail-chip--other";
};

const formatTime = (value) => {
  if (!value) return "";
  const str = String(value).replace("T", " ");
  return str.length >= 19 ? str.substring(0, 19) : str;
};

const loadDetail = async () => {
  if (!noticeId.value || !UserId.value) return;
  try {
    const { Data, Success } = await Post("/MessageNotice/Get", { Id: noticeId.value, UserId: UserId.value });
    if (Success && Data) {
      notice.value = Data;
      if (!Data.IsRead) {
        await Post("/MessageNotice/MarkRead", { Id: noticeId.value, UserId: UserId.value });
        notice.value.IsRead = true;
      }
    }
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "加载失败", icon: "error" });
  }
};

const togglePriority = async () => {
  if (!notice.value) return;
  try {
    const newPriority = notice.value.Priority === 1 ? 0 : 1;
    await Post("/MessageNotice/CreateOrEdit", { Id: notice.value.Id, Priority: newPriority });
    notice.value.Priority = newPriority;
    uni.showToast({ title: newPriority === 1 ? "\u5df2\u6807\u8bb0\u4e3a\u91cd\u8981" : "\u5df2\u53d6\u6d88\u91cd\u8981", icon: "success" });
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "操作失败", icon: "error" });
  }
};

const togglePin = async () => {
  if (!notice.value) return;
  try {
    const newPin = !notice.value.IsPinned;
    await Post("/MessageNotice/CreateOrEdit", { Id: notice.value.Id, IsPinned: newPin });
    notice.value.IsPinned = newPin;
    uni.showToast({ title: newPin ? "已置顶" : "已取消置顶", icon: "success" });
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "操作失败", icon: "error" });
  }
};

const deleteNotice = async () => {
  if (!notice.value) return;
  const result = await showDialog({
    title: "提示",
    content: "确认删除这条提醒吗？",
  });
  if (!result.confirm) return;
  try {
    await Post("/MessageNotice/Delete", { Id: notice.value.Id });
    uni.showToast({ title: "\u5df2\u5220\u9664", icon: "success" });
    setTimeout(() => uni.navigateBack(), 1200);
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "删除失败", icon: "error" });
  }
};

const handleAction = () => {
  if (!notice.value?.ActionType) return;
  const actionType = notice.value.ActionType.toUpperCase();
  const actionParam = notice.value.ActionParam;
  switch (actionType) {
    case "GOTO_AUDIO_DETECT":
      uni.navigateTo({ url: "/pages/Front/AudioRecord" });
      break;
    case "GOTO_HEALTH_RECORD":
      uni.navigateTo({ url: "/pages/Front/HealthRecord" });
      break;
    case "GOTO_SYMPTOM_FORM":
      uni.navigateTo({ url: "/pages/Front/SymptomLogForm" });
      break;
    case "GOTO_DIET_HOME":
      uni.navigateTo({ url: "/pages/Front/DietHome" });
      break;
    case "GOTO_MEDICAL_HOME":
      uni.navigateTo({ url: "/pages/Front/MedicalHome" });
      break;
    case "GOTO_MEDICAL_RECOMMEND_DETAIL":
      if (actionParam) uni.navigateTo({ url: `/pages/Front/MedicalRecommendDetail?id=${actionParam}` });
      break;
    case "GOTO_AUDIO_RESULT":
      if (actionParam) uni.navigateTo({ url: `/pages/Front/AudioResult?id=${actionParam}` });
      break;
    default:
      break;
  }
};

onLoad((options) => {
  if (!options.id) {
    uni.showToast({ title: "参数错误", icon: "error" });
    setTimeout(() => uni.navigateBack(), 1200);
    return;
  }
  noticeId.value = Number(options.id);
  if (commonStore.CheckIsLogin()) loadDetail();
});
</script>

<style scoped lang="scss">
.reminder-detail-screen { padding-bottom: calc(env(safe-area-inset-bottom) + 120upx); }
.reminder-detail-hero__top { display: flex; align-items: flex-start; justify-content: space-between; gap: 16upx; }
.reminder-detail-hero__chips { display: flex; flex-wrap: wrap; gap: 10upx; }
.reminder-detail-chip { min-height: 46upx; padding: 0 16upx; border-radius: 999upx; display: inline-flex; align-items: center; justify-content: center; font-size: 20upx; font-weight: 800; }
.reminder-detail-chip--checkup { background: rgba(142, 201, 79, 0.18); color: #d7f494; }
.reminder-detail-chip--voice { background: rgba(207, 242, 143, 0.14); color: #f0f8dc; }
.reminder-detail-chip--followup { background: rgba(234, 117, 81, 0.16); color: #ffc8bb; }
.reminder-detail-chip--pin,.reminder-detail-chip--other { background: rgba(248, 252, 239, 0.16); color: #f7ffdf; }
.reminder-detail-hero__time { font-size: 20upx; color: rgba(247, 255, 223, 0.7); }
.reminder-detail-hero__title { display: block; margin-top: 18upx; font-size: 44upx; line-height: 1.18; font-weight: 800; color: #f7ffdf; }
.reminder-detail-hero__desc { display: block; margin-top: 12upx; font-size: 22upx; line-height: 1.65; color: rgba(247, 255, 223, 0.72); }
.reminder-detail-body__content { margin-top: 20upx; display: block; font-size: 26upx; line-height: 1.85; color: var(--text-color); white-space: pre-wrap; word-break: break-word; }
.reminder-detail-actions { display: flex; flex-direction: column; gap: 20upx; }
.reminder-detail-actions__grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12upx; }
.reminder-action-card { min-height: 180upx; padding: 24upx; border-radius: 32upx; background: rgba(248, 252, 239, 0.82); border: 1upx solid rgba(205, 224, 145, 0.88); }
.reminder-action-card__title { display: block; font-size: 28upx; font-weight: 800; color: var(--text-color); }
.reminder-action-card__desc { display: block; margin-top: 10upx; font-size: 22upx; line-height: 1.6; color: var(--text-color-light); }
.reminder-detail-actions__danger { color: #8f4b42; }
.reminder-detail-loading { text-align: center; padding: 52upx 30upx; }
.reminder-detail-loading__spinner { width: 64upx; height: 64upx; margin: 0 auto; border-radius: 999upx; border: 5upx solid rgba(29, 39, 31, 0.14); border-top-color: #7ead49; animation: reminderDetailSpin 0.9s linear infinite; }
.reminder-detail-loading__text { margin-top: 16upx; font-size: 24upx; color: var(--text-color-light); }
@keyframes reminderDetailSpin { to { transform: rotate(360deg); } }
</style>
