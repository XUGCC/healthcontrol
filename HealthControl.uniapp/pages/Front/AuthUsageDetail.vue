<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack auth-usage-screen">
      <hc-topbar title="模型优化授权明细" subtitle="查看授权状态、时间范围和检测结果分布" showBack @left="back" />

      <view class="hc-card-dark auth-usage-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">授权明细</view>
        <text class="auth-usage-hero__title">{{ displayItems.length }} 条当前可见记录</text>
        <text class="auth-usage-hero__desc">默认展示已授权记录，您可以继续按授权状态、时间范围和检测结果筛选。</text>
        <view class="auth-usage-hero__meta">
          <view class="auth-usage-hero__metric">
            <text class="auth-usage-hero__metric-label">总记录</text>
            <text class="auth-usage-hero__metric-value">{{ items.length }}</text>
          </view>
          <view class="auth-usage-hero__metric">
            <text class="auth-usage-hero__metric-label">已授权</text>
            <text class="auth-usage-hero__metric-value">{{ authorizedCount }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft auth-usage-filter hc-reveal-up" style="--delay: 100ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">授权状态</text>
          <text class="bg-section-head__meta">{{ authFilterText }}</text>
        </view>
        <view class="hc-token-tabs">
          <view class="hc-token-tabs__item" :class="{ active: authFilter === 'all' }" @click="setAuth('all')">全部</view>
          <view class="hc-token-tabs__item" :class="{ active: authFilter === 'yes' }" @click="setAuth('yes')">已授权</view>
          <view class="hc-token-tabs__item" :class="{ active: authFilter === 'no' }" @click="setAuth('no')">未授权</view>
        </view>
      </view>

      <view class="hc-card-soft auth-usage-filter hc-reveal-up" style="--delay: 160ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">时间范围</text>
          <text class="bg-section-head__meta">{{ timeFilterText }}</text>
        </view>
        <view class="hc-token-tabs auth-usage-filter__wrap">
          <view class="hc-token-tabs__item" :class="{ active: timeFilter === 'all' }" @click="setTimeFilter('all')">全部</view>
          <view class="hc-token-tabs__item" :class="{ active: timeFilter === '7' }" @click="setTimeFilter('7')">近7天</view>
          <view class="hc-token-tabs__item" :class="{ active: timeFilter === '30' }" @click="setTimeFilter('30')">近30天</view>
          <view class="hc-token-tabs__item" :class="{ active: timeFilter === 'custom' }" @click="setTimeFilter('custom')">自定义</view>
        </view>
        <view v-if="timeFilter === 'custom'" class="auth-usage-range">
          <view class="auth-usage-range__picker">
            <uni-datetime-picker
              v-model="customStart"
              type="date"
              return-type="string"
              :border="false"
              :clear-icon="false"
              placeholder="开始日期"
            />
          </view>
          <text class="auth-usage-range__sep">至</text>
          <view class="auth-usage-range__picker">
            <uni-datetime-picker
              v-model="customEnd"
              type="date"
              return-type="string"
              :border="false"
              :clear-icon="false"
              placeholder="结束日期"
            />
          </view>
        </view>
      </view>

      <view class="hc-card-soft auth-usage-filter hc-reveal-up" style="--delay: 220ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">检测结果</text>
          <text class="bg-section-head__meta">{{ resultFilterText }}</text>
        </view>
        <view class="hc-token-tabs auth-usage-filter__wrap">
          <view class="hc-token-tabs__item" :class="{ active: resultFilter === 'all' }" @click="setResultFilter('all')">全部</view>
          <view class="hc-token-tabs__item" :class="{ active: resultFilter === 'good' }" @click="setResultFilter('good')">良性</view>
          <view class="hc-token-tabs__item" :class="{ active: resultFilter === 'bad' }" @click="setResultFilter('bad')">恶性</view>
          <view class="hc-token-tabs__item" :class="{ active: resultFilter === 'other' }" @click="setResultFilter('other')">其他</view>
        </view>
      </view>

      <view v-if="displayItems.length" class="auth-usage-list">
        <view
          v-for="(item, index) in displayItems"
          :key="item.Id"
          class="hc-card-soft auth-usage-item hc-reveal-up"
          :style="{ '--delay': `${280 + index * 30}ms` }"
        >
          <view class="auth-usage-item__head">
            <text class="auth-usage-item__title">{{ item.HospitalDiagnoseResult || "标注记录" }}</text>
            <text class="auth-usage-item__status" :class="{ inactive: !item.AuthStatus }">
              {{ item.AuthStatus ? "已授权" : "未授权" }}
            </text>
          </view>
          <text class="auth-usage-item__desc">检测时间：{{ item.DetectDto?.CreationTime || "-" }}</text>
          <text class="auth-usage-item__desc">初筛结果：{{ item.DetectDto?.ScreenResult || "-" }}</text>
          <text v-if="item.LabelDesc" class="auth-usage-item__desc">标注说明：{{ item.LabelDesc }}</text>

          <view v-if="item.AuthStatus" class="auth-usage-item__actions">
            <view class="auth-usage-item__action" @click="cancelAuth(item)">取消授权</view>
          </view>
        </view>
      </view>

      <view v-else class="hc-card-soft auth-usage-empty hc-reveal-up" style="--delay: 280ms">
        <text class="auth-usage-empty__title">暂无标注记录</text>
        <text class="auth-usage-empty__desc">可以调整筛选条件，或先完成更多标注后再回来看授权明细。</text>
      </view>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const commonStore = useCommonStore();
const UserId = computed(() => commonStore.UserId);

const authFilter = ref("yes");
const timeFilter = ref("all");
const customStart = ref("");
const customEnd = ref("");
const resultFilter = ref("all");
const items = ref([]);
const dialogRef = ref(null);

const authorizedCount = computed(() => items.value.filter((item) => item.AuthStatus).length);
const authFilterText = computed(() => {
  if (authFilter.value === "yes") return "已授权";
  if (authFilter.value === "no") return "未授权";
  return "全部";
});
const timeFilterText = computed(() => {
  if (timeFilter.value === "7") return "近7天";
  if (timeFilter.value === "30") return "近30天";
  if (timeFilter.value === "custom") return `${customStart.value || "开始日期"} 至 ${customEnd.value || "结束日期"}`;
  return "全部";
});
const resultFilterText = computed(() => {
  if (resultFilter.value === "good") return "良性";
  if (resultFilter.value === "bad") return "恶性";
  if (resultFilter.value === "other") return "其他";
  return "全部";
});

const back = () => {
  uni.navigateBack({ delta: 1 });
};
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const setAuth = (value) => {
  authFilter.value = value;
  reload();
};

const setTimeFilter = (value) => {
  timeFilter.value = value;
};

const setResultFilter = (value) => {
  resultFilter.value = value;
};

const reload = async () => {
  const input = {
    Page: 1,
    Limit: 50,
  };
  if (UserId.value) {
    input.UserId = UserId.value;
  }
  if (authFilter.value === "yes") {
    input.AuthStatus = true;
  } else if (authFilter.value === "no") {
    input.AuthStatus = false;
  }
  const { Data, Success } = await Post("/Front/ModelLabel/List", input);
  items.value = Success && Data?.Items ? Data.Items : [];
};

onShow(() => {
  reload();
});

const parseDetectTime = (item) => {
  const raw = item.DetectDto?.CreationTime;
  if (!raw) return null;
  try {
    return new Date(raw.replace(" ", "T"));
  } catch (error) {
    console.error("parseDetectTime error:", error);
    return null;
  }
};

const getResultText = (item) => {
  if (item.HospitalDiagnoseResult) return item.HospitalDiagnoseResult;
  if (item.DetectDto?.ScreenResult) return item.DetectDto.ScreenResult;
  return "";
};

const displayItems = computed(() => {
  const now = new Date();
  return items.value.filter((item) => {
    const time = parseDetectTime(item);
    if (!time && timeFilter.value !== "all") return false;
    if (timeFilter.value === "7") {
      const threshold = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      if (time < threshold) return false;
    } else if (timeFilter.value === "30") {
      const threshold = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
      if (time < threshold) return false;
    } else if (timeFilter.value === "custom") {
      if (customStart.value) {
        const start = new Date(`${customStart.value}T00:00:00`);
        if (time < start) return false;
      }
      if (customEnd.value) {
        const end = new Date(`${customEnd.value}T23:59:59`);
        if (time > end) return false;
      }
    }

    const resultText = getResultText(item);
    if (resultFilter.value === "good" && !resultText.includes("良性")) return false;
    if (resultFilter.value === "bad" && !resultText.includes("恶性")) return false;
    if (resultFilter.value === "other" && (!resultText || resultText.includes("良性") || resultText.includes("恶性"))) return false;
    return true;
  });
});

const cancelAuth = async (item) => {
  const result = await showDialog({
    title: "取消授权",
    content: "取消后，该条标注记录将不再用于后续模型优化训练，已经进入历史模型的部分难以从模型中彻底移除。是否继续？",
  });
  if (!result.confirm) return;
  const payload = {
    Id: item.Id,
    AuthStatus: false,
  };
  if (UserId.value) {
    payload.UserId = UserId.value;
  }
  const { Success } = await Post("/Front/ModelLabel/UpdateAuth", payload);
  if (Success) {
    uni.showToast({ title: "已取消授权", icon: "success" });
    reload();
  }
};
</script>

<style scoped lang="scss">
.auth-usage-screen {
  gap: 24upx;
}

.auth-usage-hero,
.auth-usage-filter,
.auth-usage-empty {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.auth-usage-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.auth-usage-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.auth-usage-hero__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.auth-usage-hero__metric {
  padding: 18upx 20upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.auth-usage-hero__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.auth-usage-hero__metric-value {
  display: block;
  margin-top: 8upx;
  font-size: 32upx;
  font-weight: 800;
  color: #f7ffdf;
}

.auth-usage-filter__wrap {
  flex-wrap: wrap;
}

.auth-usage-range {
  display: flex;
  align-items: center;
  gap: 12upx;
  flex-wrap: wrap;
}

.auth-usage-range__picker {
  flex: 1 1 280upx;
  min-width: 0;
}

.auth-usage-range__picker :deep(.uni-date) {
  width: 100%;
}

.auth-usage-range__picker :deep(.uni-date-editor) {
  width: 100%;
}

.auth-usage-range__picker :deep(.uni-date-editor--x) {
  min-height: 72upx;
  padding: 0 20upx;
  border-radius: 999upx;
  border: 1upx solid rgba(205, 224, 145, 0.9) !important;
  background: rgba(225, 243, 154, 0.32) !important;
  box-shadow: none !important;
}

.auth-usage-range__picker :deep(.uni-date-x) {
  min-height: 60upx;
}

.auth-usage-range__picker :deep(.uni-date__x-input) {
  font-size: 22upx;
  font-weight: 700;
  color: #172119 !important;
}

.auth-usage-range__picker :deep(.icon-calendar) {
  color: #6c7d69 !important;
}

.auth-usage-range__pill {
  min-height: 68upx;
  padding: 0 20upx;
  border-radius: 999upx;
  background: rgba(225, 243, 154, 0.32);
  display: inline-flex;
  align-items: center;
  font-size: 22upx;
  font-weight: 700;
  color: #172119;
}

.auth-usage-range__sep {
  font-size: 22upx;
  color: #7f917d;
}

.auth-usage-list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.auth-usage-item {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.auth-usage-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.auth-usage-item__title,
.auth-usage-empty__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.auth-usage-item__status {
  padding: 8upx 14upx;
  border-radius: 999upx;
  background: rgba(140, 207, 67, 0.14);
  font-size: 20upx;
  font-weight: 800;
  color: #56773a;
}

.auth-usage-item__status.inactive {
  background: rgba(127, 145, 125, 0.16);
  color: #667964;
}

.auth-usage-item__desc,
.auth-usage-empty__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.auth-usage-item__actions {
  margin-top: 6upx;
}

.auth-usage-item__action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 76upx;
  padding: 0 24upx;
  border-radius: 999upx;
  background: rgba(228, 108, 73, 0.12);
  font-size: 22upx;
  font-weight: 700;
  color: #a14d34;
}
</style>
