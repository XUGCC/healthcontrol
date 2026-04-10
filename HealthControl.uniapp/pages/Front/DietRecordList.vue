<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack diet-record-screen">
      <hc-topbar title="我的饮食记录" subtitle="按时间、类型和食后感受回看近期护理饮食" @left="back" />

      <view class="hc-card-dark diet-record-hero hc-reveal-up hc-shine">
        <view class="diet-record-hero__head">
          <view class="hc-kicker hc-kicker--dark">饮食追踪</view>
          <view class="diet-record-hero__badge">{{ currentTypeText }}</view>
        </view>
        <text class="diet-record-hero__title">{{ total }} 条记录 · {{ currentRangeText }}</text>
        <text class="diet-record-hero__desc">{{ heroSummary }}</text>

        <view class="diet-record-hero__metrics">
          <view class="diet-record-hero__metric">
            <text class="diet-record-hero__metric-label">已加载</text>
            <text class="diet-record-hero__metric-value">{{ items.length }}</text>
          </view>
          <view class="diet-record-hero__metric">
            <text class="diet-record-hero__metric-label">友好</text>
            <text class="diet-record-hero__metric-value">{{ friendlyCount }}</text>
          </view>
          <view class="diet-record-hero__metric">
            <text class="diet-record-hero__metric-label">需要注意</text>
            <text class="diet-record-hero__metric-value">{{ avoidCount }}</text>
          </view>
        </view>
      </view>

      <view v-if="isLoading && !hasLoaded" class="hc-card-soft diet-record-state hc-reveal-up" style="--delay: 90ms">
        <text class="diet-record-state__title">正在整理饮食记录</text>
        <text class="diet-record-state__desc">日期范围、类型筛选和记录卡片会一起加载出来。</text>
      </view>

      <view v-else-if="loadError" class="hc-card-soft diet-record-state diet-record-state--error hc-reveal-up" style="--delay: 90ms">
        <text class="diet-record-state__title">当前无法加载饮食记录</text>
        <text class="diet-record-state__desc">{{ loadError }}</text>
        <view class="diet-record-state__actions">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="reload">重新加载</view>
        </view>
      </view>

      <template v-else>
        <view class="hc-card-soft diet-record-filter hc-reveal-up" style="--delay: 100ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">筛选记录</text>
            <text class="bg-section-head__meta">{{ activeFilterSummary }}</text>
          </view>

          <scroll-view scroll-x class="diet-record-filter__scroll" :show-scrollbar="false">
            <view class="hc-token-tabs diet-record-filter__tabs">
              <view class="hc-token-tabs__item" :class="{ active: quickKey === 'today' }" @click="applyQuick('today')">今天</view>
              <view class="hc-token-tabs__item" :class="{ active: quickKey === '7d' }" @click="applyQuick('7d')">近7天</view>
              <view class="hc-token-tabs__item" :class="{ active: quickKey === '30d' }" @click="applyQuick('30d')">近30天</view>
              <view class="hc-token-tabs__item" :class="{ active: quickKey === '' && !dateRange.length }" @click="clearRange">全部日期</view>
            </view>
          </scroll-view>

          <view class="hc-token-tabs">
            <view class="hc-token-tabs__item" :class="{ active: categoryType === null }" @click="pickType(null)">全部</view>
            <view class="hc-token-tabs__item" :class="{ active: categoryType === 0 }" @click="pickType(0)">友好</view>
            <view class="hc-token-tabs__item" :class="{ active: categoryType === 1 }" @click="pickType(1)">需要注意</view>
          </view>

          <view class="diet-record-filter__range">
            <text class="diet-record-filter__range-label">日期范围</text>
            <view class="diet-record-filter__picker">
              <uni-datetime-picker
                v-model="dateRange"
                type="daterange"
                return-type="string"
                :border="false"
                :clear-icon="false"
                placeholder="选择日期范围"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                range-separator="至"
                @show="onDatePickerShow"
                @maskClick="onDatePickerHide"
                @change="onDateChange"
              />
            </view>
          </view>
        </view>

        <view v-show="!isDatePickerOpen" class="bg-section-head hc-reveal-fade" style="--delay: 160ms">
          <text class="bg-section-head__title">记录时间轴</text>
          <text class="bg-section-head__meta">{{ groupedRecords.length }} 个日期分组</text>
        </view>

        <template v-if="!isDatePickerOpen && groupedRecords.length">
          <view class="diet-record-groups">
            <view
              v-for="(group, groupIndex) in groupedRecords"
              :key="group.date"
              class="diet-record-group hc-reveal-up"
              :style="{ '--delay': `${200 + groupIndex * 40}ms` }"
            >
              <view class="diet-record-group__head">
                <view>
                  <text class="diet-record-group__title">{{ group.label }}</text>
                  <text class="diet-record-group__meta">{{ group.items.length }} 条记录</text>
                </view>
                <view class="diet-record-group__line"></view>
              </view>

              <view class="diet-record-group__list">
                <view
                  v-for="item in group.items"
                  :key="item.Id"
                  class="hc-card-soft diet-record-item hc-interactive-card"
                >
                  <view class="diet-record-item__time">
                    <text class="diet-record-item__time-main">{{ extractTime(item.IntakeTime) }}</text>
                    <text class="diet-record-item__time-sub">第 {{ item.IntakeFrequency || 1 }} 次</text>
                  </view>

                  <view class="diet-record-item__body">
                    <view class="diet-record-item__head">
                      <view class="diet-record-item__main">
                        <text class="diet-record-item__name">{{ item.FoodName || "未知食物" }}</text>
                        <text class="diet-record-item__meta">{{ item.IntakeTime }}</text>
                      </view>
                      <view class="diet-record-item__badge" :class="{ 'diet-record-item__badge--warn': item.CategoryType === 1 }">
                        {{ item.CategoryType === 1 ? "需要注意" : "友好" }}
                      </view>
                    </view>

                    <view class="diet-record-item__feeling-box">
                      <text class="diet-record-item__feeling-label">食后感受</text>
                      <text class="diet-record-item__feeling">
                        {{ item.EatFeeling || "这条记录还没有填写食后感受，可补充真实体验方便后面回看。" }}
                      </text>
                    </view>

                    <view class="diet-record-item__actions">
                      <view class="diet-record-item__action hc-interactive-pill" @click="editRecord(item)">编辑感受</view>
                      <view class="diet-record-item__action diet-record-item__action--danger hc-interactive-pill" @click="deleteRecord(item.Id)">
                        删除记录
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view v-if="hasMore" class="diet-record-more hc-pill-button-soft hc-interactive-pill" @click="loadMore">
            {{ isLoadingMore ? "加载中..." : "加载更多记录" }}
          </view>
          <view v-else class="diet-record-more-text">已加载全部记录</view>
        </template>

        <view v-else-if="!isDatePickerOpen" class="hc-card-soft diet-record-empty hc-reveal-up" style="--delay: 200ms">
          <text class="diet-record-empty__title">当前筛选下还没有饮食记录</text>
          <text class="diet-record-empty__desc">可以调整日期范围或类型筛选，也可以从食物详情页开始补充新的饮食记录。</text>
        </view>
      </template>
    </view>

    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onPullDownRefresh } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const dateRange = ref([]);
const quickKey = ref("");
const categoryType = ref(null);
const page = ref(1);
const limit = ref(10);
const items = ref([]);
const total = ref(0);
const hasMore = ref(true);
const dialogRef = ref(null);
const isLoading = ref(false);
const isLoadingMore = ref(false);
const hasLoaded = ref(false);
const loadError = ref("");
const isDatePickerOpen = ref(false);

const friendlyCount = computed(() => items.value.filter((item) => item.CategoryType !== 1).length);
const avoidCount = computed(() => items.value.filter((item) => item.CategoryType === 1).length);

const currentTypeText = computed(() => {
  if (categoryType.value === 0) return "仅看友好";
  if (categoryType.value === 1) return "仅看需要注意";
  return "查看全部";
});

const currentRangeText = computed(() => {
  if (dateRange.value.length === 2) {
    return `${dateRange.value[0]} 至 ${dateRange.value[1]}`;
  }
  return "全部日期";
});

const activeFilterSummary = computed(() => {
  if (dateRange.value.length === 2 && categoryType.value !== null) {
    return `${currentRangeText.value} · ${currentTypeText.value}`;
  }
  if (dateRange.value.length === 2) {
    return currentRangeText.value;
  }
  return currentTypeText.value;
});

const heroSummary = computed(() => {
  if (!total.value) {
    return "当前筛选下还没有饮食记录，可以调整日期和类型筛选，或从食物详情页补充新的记录。";
  }
  return `当前按 ${activeFilterSummary.value} 查看记录，已经加载 ${items.value.length} 条，可继续回看食后感受和当天频次。`;
});

const formatDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const formatWeekday = (dateText) => {
  if (!dateText) return "";
  const date = new Date(`${dateText}T00:00:00`);
  const weekdays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
  return weekdays[date.getDay()] || "";
};

const formatGroupLabel = (dateText) => {
  const todayText = formatDate(new Date());
  const yesterdayDate = new Date();
  yesterdayDate.setDate(yesterdayDate.getDate() - 1);
  const yesterdayText = formatDate(yesterdayDate);

  if (dateText === todayText) return `今天 · ${dateText}`;
  if (dateText === yesterdayText) return `昨天 · ${dateText}`;
  return `${dateText} · ${formatWeekday(dateText)}`;
};

const groupedRecords = computed(() => {
  const groups = [];
  const groupMap = new Map();

  items.value.forEach((item) => {
    const dateText = String(item.IntakeTime || "").slice(0, 10) || "未知日期";
    if (!groupMap.has(dateText)) {
      const group = {
        date: dateText,
        label: formatGroupLabel(dateText),
        items: [],
      };
      groupMap.set(dateText, group);
      groups.push(group);
    }
    groupMap.get(dateText).items.push(item);
  });

  return groups;
});

const buildQuery = () => {
  let start = null;
  let end = null;
  if (dateRange.value.length === 2) {
    start = dateRange.value[0];
    end = dateRange.value[1];
  }

  return {
    Page: page.value,
    Limit: limit.value,
    DateStart: start,
    DateEnd: end,
    CategoryType: categoryType.value,
  };
};

const load = async ({ reset = false } = {}) => {
  const currentPage = page.value;

  if (reset) {
    if (isLoading.value) return;
    page.value = 1;
    items.value = [];
    hasMore.value = true;
    loadError.value = "";
    isLoading.value = true;
  } else {
    if (isLoadingMore.value || !hasMore.value) return;
    isLoadingMore.value = true;
  }

  try {
    const res = await Post("/Front/Diet/RecordList", buildQuery());
    if (!res.Success) {
      if (reset || !hasLoaded.value) {
        loadError.value = res.Msg || "加载失败，请稍后重试";
      } else {
        page.value = currentPage - 1;
      }
      uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
      return;
    }

    const data = res.Data || {};
    const list = data.Items || [];
    total.value = data.TotalCount || 0;
    items.value = reset ? list : items.value.concat(list);
    hasMore.value = items.value.length < total.value;
    hasLoaded.value = true;
  } catch (error) {
    console.error("load diet records error:", error);
    if (reset || !hasLoaded.value) {
      loadError.value = "饮食记录加载失败，请稍后重试";
    } else {
      page.value = currentPage - 1;
    }
    uni.showToast({ icon: "none", title: "加载失败" });
  } finally {
    isLoading.value = false;
    isLoadingMore.value = false;
  }
};

const reload = async () => {
  await load({ reset: true });
};

const onDateChange = async () => {
  isDatePickerOpen.value = false;
  quickKey.value = "";
  await reload();
};

const onDatePickerShow = () => {
  isDatePickerOpen.value = true;
};

const onDatePickerHide = () => {
  isDatePickerOpen.value = false;
};

const loadMore = async () => {
  if (!hasMore.value) return;
  page.value += 1;
  await load();
};

const pickType = async (type) => {
  if (categoryType.value === type) return;
  categoryType.value = type;
  await reload();
};

const applyQuick = async (key) => {
  quickKey.value = key;
  const now = new Date();

  if (key === "today") {
    const dateText = formatDate(now);
    dateRange.value = [dateText, dateText];
  } else if (key === "7d") {
    const start = new Date(now.getTime() - 6 * 24 * 60 * 60 * 1000);
    dateRange.value = [formatDate(start), formatDate(now)];
  } else if (key === "30d") {
    const start = new Date(now.getTime() - 29 * 24 * 60 * 60 * 1000);
    dateRange.value = [formatDate(start), formatDate(now)];
  }

  await reload();
};

const clearRange = async () => {
  quickKey.value = "";
  dateRange.value = [];
  await reload();
};

const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const extractTime = (timeText) => {
  const text = String(timeText || "");
  if (text.length >= 16) return text.slice(11, 16);
  return "--:--";
};

const editRecord = async (item) => {
  const result = await showDialog({
    title: "修改食后感受",
    subtitle: item.FoodName || "饮食记录",
    editable: true,
    placeholderText: "请输入新的食后感受",
    content: item.EatFeeling || "",
    confirmText: "保存",
  });
  if (!result.confirm) return;

  const response = await Post("/Front/Diet/RecordCreateOrEdit", {
    Id: item.Id,
    FoodId: item.FoodId,
    IntakeTime: item.IntakeTime,
    IntakeFrequency: item.IntakeFrequency,
    EatFeeling: result.content || "",
  });

  if (!response.Success) {
    uni.showToast({ icon: "none", title: response.Msg || "保存失败" });
    return;
  }

  uni.showToast({ icon: "success", title: "已保存" });
  await reload();
};

const deleteRecord = async (id) => {
  const result = await showDialog({
    title: "删除记录",
    content: "确定删除这条饮食记录吗？删除后将无法恢复。",
    confirmText: "删除",
  });
  if (!result.confirm) return;

  const response = await Post("/Front/Diet/RecordDelete", { Id: id });
  if (!response.Success) {
    uni.showToast({ icon: "none", title: response.Msg || "删除失败" });
    return;
  }

  uni.showToast({ icon: "success", title: "已删除" });
  await reload();
};

const back = () => {
  const pages = getCurrentPages();
  if (pages && pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/DietHome" });
};

onLoad(() => {
  applyQuick("7d");
});

onPullDownRefresh(async () => {
  await reload();
  uni.stopPullDownRefresh();
});
</script>

<style scoped lang="scss">
.diet-record-screen {
  gap: 26upx;
}

.diet-record-hero {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.diet-record-hero__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16upx;
}

.diet-record-hero__badge {
  min-height: 54upx;
  padding: 0 18upx;
  border-radius: 999upx;
  background: rgba(225, 243, 154, 0.16);
  border: 1upx solid rgba(183, 232, 99, 0.24);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
  color: #d7f494;
  flex-shrink: 0;
}

.diet-record-hero__title {
  display: block;
  font-size: 50upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.diet-record-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.68;
  color: rgba(241, 248, 223, 0.74);
}

.diet-record-hero__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12upx;
}

.diet-record-hero__metric {
  min-height: 132upx;
  padding: 18upx 18upx 20upx;
  border-radius: 26upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.diet-record-hero__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.7);
}

.diet-record-hero__metric-value {
  display: block;
  margin-top: 10upx;
  font-size: 34upx;
  line-height: 1;
  font-weight: 800;
  color: #f7ffdf;
}

.diet-record-state {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.diet-record-state--error {
  border-color: rgba(228, 108, 73, 0.34);
  background: rgba(255, 243, 238, 0.92);
}

.diet-record-state__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.diet-record-state__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.diet-record-state__actions {
  margin-top: 6upx;
}

.diet-record-filter {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.diet-record-filter__scroll {
  white-space: nowrap;
}

.diet-record-filter__tabs {
  display: inline-flex;
  flex-wrap: nowrap;
}

.diet-record-filter__range {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.diet-record-filter__range-label {
  font-size: 22upx;
  font-weight: 800;
  color: #172119;
}

.diet-record-filter__picker {
  padding: 8upx 0 2upx;
}

.diet-record-filter__picker :deep(.uni-date) {
  width: 100%;
}

.diet-record-filter__picker :deep(.uni-date-editor) {
  width: 100%;
}

.diet-record-filter__picker :deep(.uni-date-editor--x) {
  min-height: 98upx;
  padding: 0 22upx;
  border-radius: 30upx;
  border: 1upx solid rgba(205, 224, 145, 0.9) !important;
  background: rgba(248, 252, 239, 0.9) !important;
  box-shadow: none !important;
}

.diet-record-filter__picker :deep(.uni-date-x) {
  min-height: 82upx;
}

.diet-record-filter__picker :deep(.uni-date__x-input) {
  font-size: 24upx;
  font-weight: 700;
  color: #172119 !important;
}

.diet-record-filter__picker :deep(.range-separator) {
  font-size: 24upx;
  font-weight: 800;
  color: #687866 !important;
}

.diet-record-filter__picker :deep(.icon-calendar) {
  color: #6c7d69 !important;
}

.diet-record-groups {
  display: flex;
  flex-direction: column;
  gap: 22upx;
}

.diet-record-group {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.diet-record-group__head {
  display: flex;
  align-items: center;
  gap: 18upx;
}

.diet-record-group__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.diet-record-group__meta {
  display: block;
  margin-top: 6upx;
  font-size: 20upx;
  color: #7f917d;
}

.diet-record-group__line {
  flex: 1;
  height: 2upx;
  background: linear-gradient(90deg, rgba(142, 201, 79, 0.36) 0%, rgba(142, 201, 79, 0.02) 100%);
}

.diet-record-group__list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.diet-record-item {
  display: flex;
  gap: 18upx;
  align-items: stretch;
}

.diet-record-item__time {
  width: 128upx;
  flex-shrink: 0;
  padding: 18upx 14upx;
  border-radius: 28upx;
  background: rgba(225, 243, 154, 0.42);
  border: 1upx solid rgba(183, 210, 111, 0.68);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8upx;
}

.diet-record-item__time-main {
  display: block;
  font-size: 30upx;
  line-height: 1;
  font-weight: 800;
  color: #172119;
}

.diet-record-item__time-sub {
  display: block;
  font-size: 18upx;
  line-height: 1.4;
  color: #5a6a56;
}

.diet-record-item__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.diet-record-item__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14upx;
}

.diet-record-item__main {
  flex: 1;
  min-width: 0;
}

.diet-record-item__name {
  display: block;
  font-size: 30upx;
  line-height: 1.32;
  font-weight: 800;
  color: #172119;
}

.diet-record-item__meta {
  display: block;
  margin-top: 8upx;
  font-size: 20upx;
  color: #7f917d;
}

.diet-record-item__badge {
  min-height: 52upx;
  padding: 0 16upx;
  border-radius: 999upx;
  background: rgba(31, 40, 32, 0.08);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
  color: #172119;
  flex-shrink: 0;
}

.diet-record-item__badge--warn {
  background: rgba(239, 179, 77, 0.16);
  color: #8a5b10;
}

.diet-record-item__feeling-box {
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.76);
  border: 1upx solid rgba(205, 224, 145, 0.78);
}

.diet-record-item__feeling-label {
  display: block;
  font-size: 18upx;
  font-weight: 800;
  color: #7a8a75;
}

.diet-record-item__feeling {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.diet-record-item__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.diet-record-item__action {
  min-height: 58upx;
  padding: 0 20upx;
  border-radius: 999upx;
  background: rgba(225, 243, 154, 0.42);
  border: 1upx solid rgba(183, 210, 111, 0.68);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22upx;
  font-weight: 700;
  color: #172119;
}

.diet-record-item__action--danger {
  background: rgba(228, 108, 73, 0.12);
  border-color: rgba(228, 108, 73, 0.22);
  color: #a14d34;
}

.diet-record-more {
  margin-top: 6upx;
}

.diet-record-more-text {
  padding: 8upx 0 6upx;
  text-align: center;
  font-size: 22upx;
  color: #7f917d;
}

.diet-record-empty {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.diet-record-empty__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.diet-record-empty__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}
</style>
