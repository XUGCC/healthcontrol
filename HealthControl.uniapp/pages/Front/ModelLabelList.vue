<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack model-label-screen">
      <hc-topbar
        title="我的标注"
        subtitle="把确诊反馈、匿名授权和模型优化入口放到同一套主页骨架里"
        :show-back="true"
        right-text="去标注"
        @left="back"
        @right="goCreate"
      />

      <view class="hc-card-dark model-label-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">模型反馈</view>
        <text class="model-label-hero__title">{{ stats.total }} 条可见标注记录</text>
        <text class="model-label-hero__desc">长按单条记录仍可切换授权状态或删除；这里优先把状态摘要、列表和匿名授权入口收拢到一起。</text>
        <view class="model-label-hero__meta">
          <view class="model-label-hero__metric">
            <text class="model-label-hero__metric-label">已授权</text>
            <text class="model-label-hero__metric-value">{{ stats.authCount }}</text>
          </view>
          <view class="model-label-hero__metric">
            <text class="model-label-hero__metric-label">未授权</text>
            <text class="model-label-hero__metric-value">{{ stats.noAuthCount }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft model-label-privacy hc-reveal-up" style="--delay: 90ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">匿名授权开关</text>
          <text class="bg-section-head__meta">只影响后续标注的默认授权行为</text>
        </view>
        <view class="model-label-privacy__row">
          <view class="model-label-privacy__main">
            <text class="model-label-privacy__title">数据匿名授权</text>
            <text class="model-label-privacy__desc">
              {{ privacyAuthEnabled ? "已开启，后续标注可以继续选择授权用于模型优化。" : "未开启，后续标注默认仅保存在个人记录中。" }}
            </text>
          </view>
          <hc-toggle :model-value="privacyAuthEnabled" @change="onPrivacySwitchChange" />
        </view>
      </view>

      <view v-if="items.length" class="model-label-list">
        <view
          v-for="(item, index) in items"
          :key="item.Id"
          class="hc-card-soft model-label-item hc-reveal-up"
          :style="{ '--delay': `${140 + index * 30}ms` }"
          @click="viewDetail(item)"
          @longpress="showActionMenu(item)"
        >
          <view class="model-label-item__head">
            <view>
              <text class="model-label-item__time">{{ formatTime(item.DetectDto?.CreationTime) || "未知检测时间" }}</text>
              <text class="model-label-item__result">{{ item.DetectDto?.ResultText || getResultText(item.DetectDto) || "未知结果" }}</text>
            </view>
            <view class="model-label-item__tag" :class="{ 'model-label-item__tag--warn': !item.LabelType }">
              {{ item.LabelType ? "确诊" : "误报" }}
            </view>
          </view>

          <text class="model-label-item__title">{{ item.HospitalDiagnoseResult || "未填写确诊结果" }}</text>
          <text v-if="item.LabelDesc" class="model-label-item__desc">{{ item.LabelDesc }}</text>

          <view class="model-label-item__foot">
            <view class="model-label-item__auth" :class="{ 'model-label-item__auth--on': item.AuthStatus }">
              {{ item.AuthStatus ? "已授权" : "未授权" }}
            </view>
            <text class="model-label-item__update">{{ formatTime(item.CreationTime) }}</text>
          </view>
        </view>
      </view>

      <view v-else-if="loading" class="hc-card-soft model-label-state hc-reveal-up" style="--delay: 120ms">
        <text class="model-label-state__title">正在整理标注记录</text>
        <text class="model-label-state__desc">检测结果、授权状态和确诊反馈会一起加载出来。</text>
      </view>

      <view v-else class="hc-card-soft model-label-state hc-reveal-up" style="--delay: 120ms">
        <text class="model-label-state__title">还没有标注记录</text>
        <text class="model-label-state__desc">完成确诊后，可以从这里提交反馈，帮助后续模型优化和个人记录整理。</text>
      </view>

      <view v-if="items.length && hasMore" class="hc-pill-button-soft hc-interactive-pill model-label-more hc-reveal-up" style="--delay: 180ms" @click="loadMore">
        {{ loading ? "加载中..." : "加载更多" }}
      </view>
    </view>
    <hc-action-sheet ref="actionSheetRef" />
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onPullDownRefresh, onReachBottom, onShow } from "@dcloudio/uni-app";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcActionSheet from "@/components/hc-action-sheet/hc-action-sheet.vue";
import HcToggle from "@/components/hc-toggle/hc-toggle.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";

const commonStore = useCommonStore();
const userId = computed(() => commonStore.UserId);

const items = ref([]);
const page = ref(1);
const pageSize = 20;
const hasMore = ref(true);
const loading = ref(false);
const stats = ref({ total: 0, authCount: 0, noAuthCount: 0 });
const privacyAuthEnabled = ref(false);
const privacySettingId = ref(null);
const actionSheetRef = ref(null);
const dialogRef = ref(null);

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/UserCenter" });
};
const showActionSheet = (options) =>
  actionSheetRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true, tapIndex: -1, action: null });
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

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
  if (!item) return "";
  if (item.PrimaryScreenResult === 0) return "良性";
  if (item.PrimaryScreenResult === 1) return "恶性倾向";
  return "未知";
};

const refreshStats = (source) => {
  const list = source || [];
  const authCount = list.filter((item) => !!item.AuthStatus).length;
  const noAuthCount = list.filter((item) => !item.AuthStatus).length;
  stats.value = {
    total: stats.value.total,
    authCount,
    noAuthCount,
  };
};

const goCreate = () => {
  uni.navigateTo({ url: "/pages/Front/ModelLabelForm" });
};

const loadList = async (reset = false) => {
  if (loading.value) return;
  if (reset) {
    page.value = 1;
    hasMore.value = true;
    items.value = [];
  }
  if (!hasMore.value) return;

  loading.value = true;
  try {
    const { Data, Success, Msg } = await Post("/Front/ModelLabel/List", {
      UserId: userId.value,
      Page: page.value,
      Limit: pageSize,
    });

    if (!Success) {
      uni.showToast({ title: Msg || "加载失败", icon: "none" });
      return;
    }

    const list = Data?.Items || [];
    if (reset) {
      items.value = list;
    } else {
      items.value = items.value.concat(list);
    }
    stats.value.total = Data?.TotalCount || items.value.length;
    refreshStats(items.value);

    hasMore.value = list.length >= pageSize;
    if (hasMore.value) {
      page.value += 1;
    }
  } catch (error) {
    console.error("loadList error:", error);
    uni.showToast({ title: "加载失败，请稍后重试", icon: "none" });
  } finally {
    loading.value = false;
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

const savePrivacyAuthorization = async (enabled) => {
  const { Data, Success, Msg } = await Post("/TUserPrivacySetting/CreateOrEdit", {
    Id: privacySettingId.value,
    UserId: userId.value,
    LocalStorageStatus: false,
    DataAnonymousAuth: enabled,
    PrivacyAgreeStatus: true,
    IsDelete: false,
  });

  if (!Success) {
    uni.showToast({ title: Msg || "保存匿名授权失败", icon: "none" });
    return false;
  }

  privacySettingId.value = Data?.Id || privacySettingId.value;
  privacyAuthEnabled.value = enabled;
  return true;
};

const onPrivacySwitchChange = async (targetValue) => {
  const title = targetValue ? "开启匿名授权" : "关闭匿名授权";
  const content = targetValue
    ? "开启后，后续授权的匿名标注数据可用于模型优化，是否确认开启？"
    : "关闭后，仅影响后续标注时的默认授权能力，不会修改历史标注状态，是否继续？";

  const result = await showDialog({
    title,
    content,
  });
  if (!result.confirm) return;

  uni.showLoading({ title: "处理中..." });
  try {
    const success = await savePrivacyAuthorization(targetValue);
    if (success) {
      uni.showToast({ title: "设置成功", icon: "success" });
    }
  } finally {
    uni.hideLoading();
  }
};

const viewDetail = (item) => {
  uni.navigateTo({ url: `/pages/Front/ModelLabelDetail?id=${item.Id}` });
};

const updateAuthStatus = async (item, authStatus) => {
  uni.showLoading({ title: "处理中..." });
  try {
    const { Success } = await Post("/Front/ModelLabel/UpdateAuth", {
      Id: item.Id,
      UserId: userId.value,
      AuthStatus: authStatus,
    });
    if (!Success) return;

    item.AuthStatus = authStatus;
    refreshStats(items.value);
    uni.showToast({ title: "操作成功", icon: "success" });
  } finally {
    uni.hideLoading();
  }
};

const deleteLabel = async (item) => {
  const result = await showDialog({
    title: "确认删除",
    content: "确定要删除这条标注记录吗？",
  });
  if (!result.confirm) return;
  uni.showLoading({ title: "删除中..." });
  try {
    const { Success } = await Post("/Front/ModelLabel/Delete", {
      Id: item.Id,
      UserId: userId.value,
    });
    if (!Success) return;

    items.value = items.value.filter((current) => current.Id !== item.Id);
    stats.value.total = Math.max(0, stats.value.total - 1);
    refreshStats(items.value);
    uni.showToast({ title: "删除成功", icon: "success" });
  } finally {
    uni.hideLoading();
  }
};

const showActionMenu = (item) => {
  showActionSheet({
    title: "选择标注操作",
    subtitle: "可快速切换授权状态或删除当前记录",
    actions: [
      { label: item.AuthStatus ? "取消授权" : "开启授权" },
      { label: "删除", danger: true },
    ],
  }).then(async (result) => {
    if (!result.confirm || result.tapIndex < 0) return;
    if (result.tapIndex === 0) {
      await updateAuthStatus(item, !item.AuthStatus);
      return;
    }
    if (result.tapIndex === 1) {
      await deleteLabel(item);
    }
  });
};

const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadList(false);
  }
};

const initialize = async () => {
  if (!userId.value) {
    uni.showToast({ title: "请先登录", icon: "none" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/Front/Login" });
    }, 1200);
    return;
  }
  await loadPrivacySetting();
  await loadList(true);
};

onLoad(() => {
  initialize();
});

onShow(() => {
  if (userId.value) {
    initialize();
  }
});

onPullDownRefresh(async () => {
  await initialize();
  uni.stopPullDownRefresh();
});

onReachBottom(() => {
  loadMore();
});
</script>

<style scoped lang="scss">
.model-label-screen {
  gap: 24upx;
}

.model-label-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.model-label-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.1;
  font-weight: 800;
  color: #f7ffdf;
}

.model-label-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.76);
}

.model-label-hero__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.model-label-hero__metric {
  padding: 18upx 20upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.model-label-hero__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.model-label-hero__metric-value {
  display: block;
  margin-top: 8upx;
  font-size: 30upx;
  font-weight: 800;
  color: #f7ffdf;
}

.model-label-privacy__row {
  margin-top: 18upx;
  display: flex;
  align-items: center;
  gap: 18upx;
}

.model-label-privacy__main {
  flex: 1;
}

.model-label-privacy__title,
.model-label-state__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.model-label-privacy__desc,
.model-label-state__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.model-label-list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.model-label-item {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.model-label-item__head,
.model-label-item__foot {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12upx;
}

.model-label-item__time,
.model-label-item__update {
  display: block;
  font-size: 20upx;
  color: #7c8e7b;
}

.model-label-item__result,
.model-label-item__title {
  display: block;
  margin-top: 8upx;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.model-label-item__title {
  margin-top: 0;
  font-size: 30upx;
}

.model-label-item__tag {
  min-height: 56upx;
  padding: 0 18upx;
  border-radius: 999upx;
  background: rgba(140, 207, 67, 0.14);
  color: #56773a;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
}

.model-label-item__tag--warn {
  background: rgba(243, 188, 171, 0.18);
  color: #a14d34;
}

.model-label-item__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.model-label-item__auth {
  min-height: 52upx;
  padding: 0 18upx;
  border-radius: 999upx;
  background: rgba(127, 145, 125, 0.16);
  color: #667964;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
}

.model-label-item__auth--on {
  background: rgba(140, 207, 67, 0.14);
  color: #56773a;
}
</style>
