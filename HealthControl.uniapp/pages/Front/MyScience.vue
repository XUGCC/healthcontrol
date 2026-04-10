<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack my-science-screen">
      <hc-topbar
        title="我的发布"
        subtitle="查看草稿、审核状态和已发布内容"
        :show-back="true"
        right-text="发布"
        @left="back"
        @right="goPublish"
      />

      <view class="bg-head my-science-head hc-reveal-up">
        <view class="hc-kicker">内容管理</view>
        <text class="bg-head__title">先看状态摘要，再决定继续编辑哪一篇</text>
        <text class="bg-head__subtitle">
          草稿适合继续完善，审核中关注进度，通过后可继续沉淀个人内容资产。
        </text>
      </view>

      <view class="hc-card-lime my-science-summary hc-reveal-up" style="--delay: 100ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">当前筛选</text>
          <text class="hc-section-head__meta">{{ statusLabel }}</text>
        </view>
        <view class="my-science-summary__metrics">
          <view class="my-science-summary__metric">
            <text class="my-science-summary__metric-label">内容数量</text>
            <text class="my-science-summary__metric-value">{{ items.length }}</text>
          </view>
          <view class="my-science-summary__metric">
            <text class="my-science-summary__metric-label">管理重点</text>
            <text class="my-science-summary__metric-value">{{ summaryHint }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft my-science-filter hc-reveal-up" style="--delay: 160ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">状态筛选</text>
          <text class="bg-section-head__meta">切换后立即刷新列表</text>
        </view>
        <view class="my-science-filter__actions">
          <view class="hc-official-select-trigger" @click="openStatusPopup">
            <text class="hc-official-select-trigger__value">{{ statusLabel || "全部" }}</text>
            <uni-icons type="down" size="18" color="#5e705d" />
          </view>
          <view class="my-science-filter__pill" @click="goPublish">新建内容</view>
        </view>
      </view>

      <view v-if="loading" class="hc-card-soft my-science-state hc-reveal-up" style="--delay: 220ms">
        <text class="my-science-state__title">正在加载发布内容</text>
        <text class="my-science-state__desc">系统会按当前状态筛选出你的内容列表。</text>
      </view>

      <template v-else-if="items.length">
        <view
          v-for="(item, index) in items"
          :key="item.Id"
          class="hc-card-soft my-science-item hc-interactive-card hc-reveal-up"
          :style="{ '--delay': `${240 + index * 40}ms` }"
          @click="edit(item.Id)"
        >
          <image class="my-science-item__cover" mode="aspectFill" :src="item.CoverUrl" />
          <view class="my-science-item__body">
            <text class="my-science-item__title">{{ item.Title }}</text>
            <view class="my-science-item__tags">
              <text class="my-science-item__tag" :class="auditClass(item.AuditStatus)">
                {{ auditText(item.AuditStatus) }}
              </text>
            </view>
            <text v-if="item.AuditStatus === 3 && item.RejectReason" class="my-science-item__reason">
              驳回原因：{{ item.RejectReason }}
            </text>
          </view>
        </view>
      </template>

      <view v-else class="hc-card-soft my-science-empty hc-reveal-up" style="--delay: 220ms">
        <text class="my-science-empty__title">当前状态下暂无内容</text>
        <text class="my-science-empty__desc">可以新建一篇内容，或切换其他状态继续查看。</text>
        <view class="hc-pill-button-dark hc-interactive-pill" @click="goPublish">去发布</view>
      </view>

      <uni-popup
        ref="statusPopupRef"
        type="center"
        :safe-area="false"
        background-color="transparent"
        mask-background-color="rgba(16, 24, 19, 0.58)"
      >
        <view class="hc-official-popup-card">
          <view class="hc-official-popup-handle"></view>
          <view class="hc-official-popup-head">
            <text class="hc-official-popup-eyebrow">筛选与选择</text>
            <text class="hc-official-popup-title">选择内容状态</text>
            <text class="hc-official-popup-subtitle">切换后立即刷新列表，方便查看不同审核阶段的内容</text>
          </view>
          <view class="hc-official-popup-summary">
            <text class="hc-official-popup-summary__label">当前</text>
            <text class="hc-official-popup-summary__value">{{ statusLabel || "全部" }}</text>
          </view>
          <scroll-view scroll-y class="hc-official-popup-list">
            <view
              v-for="option in statusOptions"
              :key="String(option.value)"
              class="hc-official-popup-option"
              :class="{ active: auditStatus === option.value }"
              @click="onStatusChange(option)"
            >
              <text class="hc-official-popup-option__text">{{ option.text }}</text>
              <text v-if="auditStatus === option.value" class="hc-official-popup-option__mark">当前</text>
            </view>
          </scroll-view>
        </view>
      </uni-popup>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();
const items = ref([]);
const loading = ref(false);
const statusPopupRef = ref(null);

const statusOptions = [
  { text: "全部", value: null },
  { text: "草稿", value: 0 },
  { text: "待审核", value: 1 },
  { text: "通过", value: 2 },
  { text: "驳回", value: 3 },
  { text: "下架/屏蔽", value: 4 },
];

const statusLabel = ref("全部");
const auditStatus = ref(null);

const summaryHint = computed(() => {
  if (statusLabel.value === "草稿") return "优先继续完善";
  if (statusLabel.value === "待审核") return "关注审核进度";
  if (statusLabel.value === "通过") return "可继续沉淀";
  if (statusLabel.value === "驳回") return "优先处理原因";
  if (statusLabel.value === "下架/屏蔽") return "检查内容状态";
  return "查看全部内容";
});

const load = async () => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录" });
    return;
  }

  loading.value = true;
  const { Data, Success, Msg } = await Post("/Front/Science/MyPublishList", {
    Page: 1,
    Limit: 50,
    AuditStatus: auditStatus.value,
  });
  loading.value = false;

  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "加载失败" });
    return;
  }

  items.value = Data?.Items || [];
};

const openStatusPopup = () => {
  statusPopupRef.value?.open();
};

const onStatusChange = async (item) => {
  if (!item) {
    auditStatus.value = null;
    statusLabel.value = "全部";
  } else {
    auditStatus.value = item.value;
    statusLabel.value = item.text;
  }
  statusPopupRef.value?.close();
  await load();
};

const auditText = (value) =>
  value === 0 ? "草稿" : value === 1 ? "待审核" : value === 2 ? "通过" : value === 3 ? "驳回" : "下架/屏蔽";

const auditClass = (value) => (value === 2 ? "ok" : value === 1 ? "wait" : value === 0 ? "draft" : "bad");

const goPublish = () => uni.navigateTo({ url: "/pages/Front/SciencePublish" });
const edit = (id) => uni.navigateTo({ url: `/pages/Front/SciencePublish?Id=${id}` });
const back = () => uni.navigateBack({ delta: 1 });

onShow(load);
</script>

<style scoped lang="scss">
.my-science-screen {
  gap: 24upx;
}

.my-science-head {
  gap: 10upx;
}

.my-science-summary__metrics {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.my-science-summary__metric {
  padding: 18upx 20upx;
  border-radius: 24upx;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.my-science-summary__metric-label {
  display: block;
  font-size: 20upx;
  color: #556556;
}

.my-science-summary__metric-value {
  display: block;
  margin-top: 8upx;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.my-science-filter__actions {
  margin-top: 18upx;
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.my-science-filter__pill {
  min-height: 62upx;
  padding: 0 24upx;
  border-radius: 9999upx;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(205, 224, 145, 0.88);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22upx;
  color: #556556;
}

.my-science-state__title,
.my-science-empty__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.my-science-state__desc,
.my-science-empty__desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.my-science-item {
  display: flex;
  gap: 18upx;
}

.my-science-item__cover {
  width: 220upx;
  height: 164upx;
  border-radius: 28upx;
  background: rgba(225, 243, 154, 0.38);
  flex-shrink: 0;
}

.my-science-item__body {
  flex: 1;
  min-width: 0;
}

.my-science-item__title {
  display: block;
  font-size: 30upx;
  line-height: 1.35;
  font-weight: 800;
  color: #172119;
}

.my-science-item__tags {
  margin-top: 12upx;
}

.my-science-item__tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 52upx;
  padding: 0 18upx;
  border-radius: 9999upx;
  font-size: 20upx;
  font-weight: 800;
}

.my-science-item__tag.ok {
  background: rgba(134, 189, 66, 0.16);
  color: #547e22;
}

.my-science-item__tag.wait {
  background: rgba(243, 179, 77, 0.18);
  color: #8d5b1f;
}

.my-science-item__tag.draft {
  background: rgba(129, 144, 129, 0.16);
  color: #59665d;
}

.my-science-item__tag.bad {
  background: rgba(228, 108, 73, 0.16);
  color: #944b3a;
}

.my-science-item__reason {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  line-height: 1.6;
  color: #944b3a;
}

.my-science-empty {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}
</style>
