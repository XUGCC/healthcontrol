<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack reminder-message-screen">
      <hc-topbar :title="'\u5065\u5eb7\u63d0\u9192'" :subtitle="'\u6309\u65f6\u95f4\u7ebf\u67e5\u770b\u672a\u8bfb\u63d0\u9192\u4e0e\u62a4\u7406\u4efb\u52a1'" :show-back="true" @left="goBack" />

      <view class="hc-card-dark reminder-message-hero hc-reveal-up">
        <view class="hc-kicker hc-kicker--dark">提醒中心</view>
        <text class="reminder-message-hero__title">把未读提醒、重点消息和最近 30 天通知集中到一个入口里。</text>
        <view class="reminder-message-hero__stats">
          <view class="reminder-message-hero__stat">
            <text class="hc-stat-value hc-stat-value--dark">{{ stats?.Unread || 0 }}</text>
            <text class="hc-stat-label">未读提醒</text>
          </view>
          <view class="reminder-message-hero__stat reminder-message-hero__stat--soft">
            <text class="hc-stat-value">{{ stats?.Total || items.length || 0 }}</text>
            <text class="hc-stat-label">近 30 天消息</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft reminder-toolbar hc-reveal-up" style="--delay: 80ms">
        <view class="reminder-toolbar__search">
          <uni-icons type="search" size="18" color="#758473" />
          <input
            v-model="searchKeyword"
            class="reminder-toolbar__input"
            :placeholder="'\u641c\u7d22\u6807\u9898\u6216\u5185\u5bb9'"
            confirm-type="search"
            @confirm="onSearch"
            @input="onSearchInput"
          />
        </view>
        <view class="reminder-toolbar__filter hc-interactive-pill" @click="showDateFilter">
          <text>{{ dateFilterLabel }}</text>
          <uni-icons type="bottom" size="14" color="#41513e" />
        </view>
      </view>

      <scroll-view class="reminder-tabs-scroll hc-reveal-fade" scroll-x style="--delay: 120ms">
        <view class="hc-token-tabs reminder-tabs">
          <view
            v-for="tab in tabList"
            :key="tab.value"
            class="hc-token-tabs__item"
            :class="{ active: currentTab === tab.value }"
            @click="changeTab(tab.value)"
          >
            {{ tab.label }}
          </view>
        </view>
      </scroll-view>

      <view v-if="loading && !items.length" class="hc-card-soft reminder-loading hc-reveal-up" style="--delay: 160ms">
        <view class="reminder-loading__spinner"></view>
        <text class="reminder-loading__text">正在加载提醒列表...</text>
      </view>

      <template v-else>
        <view v-for="group in groupedSections" :key="group.key" class="reminder-group hc-reveal-up">
          <view v-if="group.list.length" class="bg-section-head">
            <text class="bg-section-head__title">{{ group.title }}</text>
            <text class="bg-section-head__meta">{{ group.list.length }} 条</text>
          </view>
          <view v-if="group.list.length" class="reminder-group__list">
            <view
              v-for="item in group.list"
              :key="item.Id"
              class="hc-card-soft reminder-card hc-interactive-card"
              :class="{ 'reminder-card--pinned': item.IsPinned, 'reminder-card--unread': !item.IsRead }"
              @click="handleClick(item)"
              @longpress="handleLongPress(item)"
            >
              <view class="reminder-card__head">
                <view class="reminder-card__chips">
                  <view v-if="item.Priority === 1" class="reminder-chip reminder-chip--warn">重要</view>
                  <view class="reminder-chip" :class="typeClass(item.Type)">{{ typeLabel(item.Type) }}</view>
                  <view v-if="item.IsPinned" class="reminder-chip reminder-chip--pin">置顶</view>
                </view>
                <view v-if="!item.IsRead" class="reminder-card__dot"></view>
              </view>
              <text class="reminder-card__title">{{ item.Title || "系统提醒" }}</text>
              <text class="reminder-card__desc">{{ item.Content || "暂无内容" }}</text>
              <view class="reminder-card__foot">
                <text class="reminder-card__time">{{ formatTime(item.ActualSendTime || item.PlanSendTime || item.CreationTime) }}</text>
                <uni-icons type="right" size="16" color="#738272" />
              </view>
            </view>
          </view>
        </view>

        <view v-if="!items.length" class="hc-card-soft reminder-empty hc-reveal-up" style="--delay: 160ms">
          <view class="reminder-empty__icon">
            <uni-icons type="notification" size="32" color="#1b261d" />
          </view>
          <text class="reminder-empty__title">暂时还没有健康提醒</text>
          <text class="reminder-empty__desc">当系统生成自查、护嗓或复查提醒后，这里会按时间线自动汇总。</text>
        </view>

        <view v-if="loading && items.length" class="reminder-loading-more">正在加载更多...</view>
      </template>

      <view v-if="hasMore && !loading && items.length" class="hc-pill-button-soft reminder-more" @click="loadMore">加载更多</view>
    </view>

    <view v-if="(hasUnread || hasRead) && items.length && !isPopupOpen" class="hc-fab reminder-actions">
      <view v-if="hasUnread" class="hc-pill-button-dark" @click="markAllRead">全部标记已读</view>
      <view v-if="hasRead" class="hc-pill-button-soft" @click="cleanRead">清理已读</view>
    </view>
    <hc-action-sheet ref="actionSheetRef" @popup-change="onPopupChange" />
    <hc-dialog ref="dialogRef" @popup-change="onPopupChange" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onReachBottom, onShow } from "@dcloudio/uni-app";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcActionSheet from "@/components/hc-action-sheet/hc-action-sheet.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const commonStore = useCommonStore();
const UserId = computed(() => commonStore.UserId);
const items = ref([]);
const page = ref(1);
const pageSize = 20;
const hasMore = ref(true);
const loading = ref(false);
const currentTab = ref("ALL");
const stats = ref(null);
const searchKeyword = ref("");
const dateFilter = ref("30");
const actionSheetRef = ref(null);
const dialogRef = ref(null);
const activePopupCount = ref(0);

const tabList = [
  { value: "ALL", label: "\u5168\u90e8" },
  { value: "UNREAD", label: "\u672a\u8bfb" },
  { value: "CHECKUP", label: "\u81ea\u67e5" },
  { value: "VOICE_CARE", label: "\u62a4\u55d3" },
  { value: "FOLLOWUP", label: "\u590d\u67e5" },
];

const dateFilterLabel = computed(() => {
  if (dateFilter.value === "all") return "\u5168\u90e8";
  if (dateFilter.value === "7") return "\u8fd1 7 \u5929";
  if (dateFilter.value === "30") return "\u8fd1 30 \u5929";
  return "\u81ea\u5b9a\u4e49";
});
const showActionSheet = (options) =>
  actionSheetRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true, tapIndex: -1, action: null });
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });
const onPopupChange = (show) => {
  activePopupCount.value = Math.max(0, activePopupCount.value + (show ? 1 : -1));
};
const isPopupOpen = computed(() => activePopupCount.value > 0);

const hasUnread = computed(() => items.value.some((item) => !item.IsRead));
const hasRead = computed(() => items.value.some((item) => item.IsRead));

const normalizeDate = (value) => {
  const date = new Date(value);
  date.setHours(0, 0, 0, 0);
  return date.getTime();
};

const todayItems = computed(() => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const todayTime = today.getTime();
  return items.value.filter((item) => normalizeDate(item.CreationTime || item.PlanSendTime) === todayTime);
});

const weekItems = computed(() => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const todayTime = today.getTime();
  const weekAgo = todayTime - 7 * 24 * 60 * 60 * 1000;
  return items.value.filter((item) => {
    const itemTime = normalizeDate(item.CreationTime || item.PlanSendTime);
    return itemTime < todayTime && itemTime >= weekAgo;
  });
});

const olderItems = computed(() => {
  const boundary = new Date();
  boundary.setHours(0, 0, 0, 0);
  const weekAgo = boundary.getTime() - 7 * 24 * 60 * 60 * 1000;
  return items.value.filter((item) => normalizeDate(item.CreationTime || item.PlanSendTime) < weekAgo);
});

const groupedSections = computed(() => [
  { key: "today", title: "\u4eca\u5929", list: todayItems.value },
  { key: "week", title: "\u8fd1 7 \u5929", list: weekItems.value },
  { key: "older", title: "\u66f4\u65e9", list: olderItems.value },
]);

const goBack = () => uni.navigateBack();
const onSearch = () => resetAndLoad();
const onSearchInput = () => {};

const showDateFilter = () => {
  showActionSheet({
    title: "选择时间范围",
    subtitle: "按时间筛选提醒记录",
    actions: [
      { label: "\u5168\u90e8" },
      { label: "\u8fd1 7 \u5929" },
      { label: "\u8fd1 30 \u5929" },
      { label: "\u81ea\u5b9a\u4e49", desc: "当前版本仍在开发中" },
    ],
  }).then((res) => {
    if (!res.confirm) return;
    if (res.tapIndex === 0) dateFilter.value = "all";
    else if (res.tapIndex === 1) dateFilter.value = "7";
    else if (res.tapIndex === 2) dateFilter.value = "30";
    else {
      uni.showToast({ title: "自定义日期功能开发中", icon: "none" });
      return;
    }
    resetAndLoad();
  });
};

const typeLabel = (type) => {
  if (!type) return "其他";
  const value = type.toUpperCase();
  if (value === "CHECKUP") return "自查提醒";
  if (value === "VOICE_CARE") return "护嗓提醒";
  if (value === "FOLLOWUP") return "复查提醒";
  return type;
};

const typeClass = (type) => {
  if (!type) return "reminder-chip--other";
  const value = type.toUpperCase();
  if (value === "CHECKUP") return "reminder-chip--checkup";
  if (value === "VOICE_CARE") return "reminder-chip--voice";
  if (value === "FOLLOWUP") return "reminder-chip--followup";
  return "reminder-chip--other";
};

const formatTime = (value) => {
  if (!value) return "";
  const str = String(value).replace("T", " ");
  return str.length >= 16 ? str.substring(5, 16) : str;
};

const formatDateTime = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hour = String(date.getHours()).padStart(2, "0");
  const minute = String(date.getMinutes()).padStart(2, "0");
  const second = String(date.getSeconds()).padStart(2, "0");
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
};

const buildFilter = () => {
  const filter = { UserId: UserId.value, Page: page.value, Limit: pageSize };
  if (currentTab.value === "UNREAD") filter.IsRead = false;
  else if (currentTab.value !== "ALL") filter.Type = currentTab.value;
  if (searchKeyword.value) filter.KeyWord = searchKeyword.value;

  const now = new Date();
  if (dateFilter.value === "7") {
    filter.StartTime = formatDateTime(new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000));
    filter.EndTime = formatDateTime(now);
  } else if (dateFilter.value === "30") {
    filter.StartTime = formatDateTime(new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000));
    filter.EndTime = formatDateTime(now);
  }
  return filter;
};

const loadList = async () => {
  if (!hasMore.value || loading.value || !UserId.value) return;
  loading.value = true;
  try {
    const { Data } = await Post("/MessageNotice/List", buildFilter());
    const list = Data?.Items || [];
    if (page.value === 1) {
      items.value = list;
      stats.value = Data?.Stats || null;
    } else {
      items.value = items.value.concat(list);
    }
    hasMore.value = list.length === pageSize;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const resetAndLoad = () => {
  page.value = 1;
  items.value = [];
  hasMore.value = true;
  loadList();
};

const loadMore = () => {
  if (!hasMore.value) return;
  page.value += 1;
  loadList();
};

const changeTab = (value) => {
  if (currentTab.value === value) return;
  currentTab.value = value;
  resetAndLoad();
};

const togglePriority = async (item) => {
  try {
    const newPriority = item.Priority === 1 ? 0 : 1;
    await Post("/MessageNotice/CreateOrEdit", { Id: item.Id, Priority: newPriority });
    item.Priority = newPriority;
        uni.showToast({ title: newPriority === 1 ? "\u5df2\u6807\u8bb0\u4e3a\u91cd\u8981" : "\u5df2\u53d6\u6d88\u91cd\u8981", icon: "success" });
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "操作失败", icon: "error" });
  }
};

const togglePin = async (item) => {
  try {
    const pinnedCount = items.value.filter((record) => record.IsPinned).length;
    if (!item.IsPinned && pinnedCount >= 5) {
      uni.showToast({ title: "最多只能置顶 5 条，请先取消部分置顶", icon: "none" });
      return;
    }
    const newPin = !item.IsPinned;
    await Post("/MessageNotice/CreateOrEdit", { Id: item.Id, IsPinned: newPin });
    item.IsPinned = newPin;
    uni.showToast({ title: newPin ? "已置顶" : "已取消置顶", icon: "success" });
    resetAndLoad();
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "操作失败", icon: "error" });
  }
};

const deleteNotice = async (item) => {
  const result = await showDialog({
    title: "提示",
    content: "确认删除这条提醒吗？",
  });
  if (!result.confirm) return;
  try {
    await Post("/MessageNotice/Delete", { Id: item.Id });
    uni.showToast({ title: "\u5df2\u5220\u9664", icon: "success" });
    resetAndLoad();
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "删除失败", icon: "error" });
  }
};

const handleLongPress = (item) => {
  const actions = [item.Priority === 1 ? "取消重要" : "标记重要", item.IsPinned ? "取消置顶" : "置顶", "删除"];
  showActionSheet({
    title: "选择提醒操作",
    subtitle: "可快速调整重要程度、置顶或删除",
    actions: actions.map((label) => ({
      label,
      danger: label === "删除",
    })),
  }).then(async (res) => {
    if (!res.confirm || res.tapIndex < 0) return;
    const action = actions[res.tapIndex];
    if (action.includes("重要")) await togglePriority(item);
    else if (action.includes("置顶")) await togglePin(item);
    else await deleteNotice(item);
  });
};

const handleClick = async (item) => {
  if (!item.IsRead) {
    try {
      await Post("/MessageNotice/MarkRead", { Id: item.Id, UserId: UserId.value });
      item.IsRead = true;
      if (stats.value) {
        stats.value.Read = (stats.value.Read || 0) + 1;
        stats.value.Unread = Math.max(0, (stats.value.Unread || 0) - 1);
      }
    } catch (error) {
      console.error(error);
    }
  }

  const actionType = (item.ActionType || "").toUpperCase();
  const actionParam = item.ActionParam;
  if (!actionType) {
    uni.navigateTo({ url: `/pages/Front/ReminderMessageDetail?id=${item.Id}` });
    return;
  }

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
      uni.navigateTo({ url: `/pages/Front/ReminderMessageDetail?id=${item.Id}` });
  }
};

const markAllRead = async () => {
  const result = await showDialog({
    title: "提示",
    content: "确认将全部提醒标记为已读吗？",
  });
  if (!result.confirm) return;
  try {
    const { Success } = await Post("/MessageNotice/MarkAllRead", { UserId: UserId.value });
    if (Success) {
      uni.showToast({ title: "\u5df2\u5168\u90e8\u6807\u8bb0\u5df2\u8bfb", icon: "success" });
      resetAndLoad();
    }
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "操作失败", icon: "error" });
  }
};

const cleanRead = async () => {
  const result = await showDialog({
    title: "提示",
    content: "\u786e\u8ba4\u6e05\u7406\u6240\u6709\u5df2\u8bfb\u63d0\u9192\u5417\uff1f",
  });
  if (!result.confirm) return;
  try {
    const { Success } = await Post("/MessageNotice/BatchSoftDelete", { UserId: UserId.value, IsRead: true });
    if (Success) {
      uni.showToast({ title: "\u5df2\u6e05\u7406", icon: "success" });
      resetAndLoad();
    }
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "清理失败", icon: "error" });
  }
};

onReachBottom(loadMore);
onLoad(() => {
  if (commonStore.CheckIsLogin()) resetAndLoad();
});
onShow(() => {
  if (commonStore.CheckIsLogin() && UserId.value && (currentTab.value === "ALL" || currentTab.value === "UNREAD")) {
    resetAndLoad();
  }
});
</script>

<style scoped lang="scss">
.reminder-message-screen { padding-bottom: calc(env(safe-area-inset-bottom) + 260upx); }
.reminder-message-hero__title { display: block; margin-top: 16upx; font-size: 46upx; line-height: 1.18; font-weight: 800; color: #f7ffdf; }
.reminder-message-hero__stats { margin-top: 24upx; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12upx; }
.reminder-message-hero__stat { padding: 22upx; border-radius: 30upx; background: rgba(225, 243, 154, 0.14); border: 1upx solid rgba(183, 232, 99, 0.2); }
.reminder-message-hero__stat--soft { background: rgba(248, 252, 239, 0.9); border-color: rgba(205, 224, 145, 0.88); }
.reminder-toolbar { display: flex; align-items: center; gap: 14upx; }
.reminder-toolbar__search { flex: 1; min-height: 86upx; padding: 0 22upx; border-radius: 999upx; background: rgba(248, 252, 239, 0.82); border: 1upx solid rgba(205, 224, 145, 0.9); display: flex; align-items: center; gap: 12upx; }
.reminder-toolbar__input { flex: 1; height: 100%; border: 0; background: transparent; font-size: 24upx; color: var(--text-color); }
.reminder-toolbar__filter { min-width: 176upx; min-height: 86upx; padding: 0 22upx; border-radius: 999upx; background: rgba(223, 242, 162, 0.86); border: 1upx solid rgba(177, 212, 106, 0.72); display: flex; align-items: center; justify-content: space-between; gap: 12upx; font-size: 24upx; font-weight: 800; color: #31422f; }
.reminder-tabs-scroll { white-space: nowrap; }
.reminder-tabs { flex-wrap: nowrap; padding-right: 20upx; }
.reminder-group { display: flex; flex-direction: column; gap: 16upx; }
.reminder-group__list { display: flex; flex-direction: column; gap: 14upx; }
.reminder-card { padding: 24upx; }
.reminder-card--pinned { border-color: rgba(143, 184, 82, 0.96); }
.reminder-card--unread { box-shadow: 0 20upx 44upx rgba(44, 58, 35, 0.14); }
.reminder-card__head { display: flex; align-items: center; justify-content: space-between; gap: 12upx; }
.reminder-card__chips { display: flex; flex-wrap: wrap; gap: 10upx; }
.reminder-chip { min-height: 46upx; padding: 0 16upx; border-radius: 999upx; display: inline-flex; align-items: center; justify-content: center; font-size: 20upx; font-weight: 800; color: #253123; background: rgba(24, 33, 27, 0.08); }
.reminder-chip--warn { background: rgba(243, 182, 65, 0.18); color: #8a5b12; }
.reminder-chip--checkup { background: rgba(142, 201, 79, 0.18); }
.reminder-chip--voice { background: rgba(129, 168, 82, 0.16); }
.reminder-chip--followup { background: rgba(234, 117, 81, 0.14); color: #8f4d3a; }
.reminder-chip--pin { background: rgba(23, 33, 25, 0.9); color: #f7ffdf; }
.reminder-chip--other { background: rgba(128, 144, 129, 0.16); color: #556355; }
.reminder-card__dot { width: 18upx; height: 18upx; border-radius: 50%; background: #ea7551; box-shadow: 0 0 0 10upx rgba(234, 117, 81, 0.12); flex-shrink: 0; }
.reminder-card__title { margin-top: 18upx; display: block; font-size: 30upx; line-height: 1.35; font-weight: 800; color: var(--text-color); }
.reminder-card__desc { margin-top: 10upx; display: block; font-size: 22upx; line-height: 1.65; color: var(--text-color-light); }
.reminder-card__foot { margin-top: 18upx; display: flex; align-items: center; justify-content: space-between; gap: 12upx; }
.reminder-card__time { font-size: 20upx; color: var(--text-color-lighter); }
.reminder-loading,.reminder-empty { text-align: center; padding: 48upx 30upx; }
.reminder-loading__spinner { width: 64upx; height: 64upx; margin: 0 auto; border-radius: 999upx; border: 5upx solid rgba(29, 39, 31, 0.12); border-top-color: #7ead49; animation: reminderSpin 0.9s linear infinite; }
.reminder-loading__text,.reminder-empty__desc,.reminder-loading-more { margin-top: 16upx; font-size: 24upx; color: var(--text-color-light); }
.reminder-empty__icon { width: 84upx; height: 84upx; margin: 0 auto; border-radius: 28upx; background: #ddf29c; display: flex; align-items: center; justify-content: center; }
.reminder-empty__title { margin-top: 18upx; display: block; font-size: 32upx; font-weight: 800; color: var(--text-color); }
.reminder-loading-more { text-align: center; }
.reminder-more { margin-bottom: 6upx; }
.reminder-actions { display: flex; flex-direction: column; gap: 12upx; }
@keyframes reminderSpin { to { transform: rotate(360deg); } }
</style>
