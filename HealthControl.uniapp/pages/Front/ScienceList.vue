<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack science-list-screen">
      <hc-topbar
        title="科普列表"
        subtitle="按关键词和知识类型继续筛选内容"
        :show-back="true"
        @left="goBack"
      />

      <view class="bg-head science-list-head hc-reveal-up">
        <view class="hc-kicker">内容列表</view>
        <text class="bg-head__title">先缩小范围，再挑一篇值得读的内容</text>
        <text class="bg-head__subtitle">{{ filterSummary }}</text>
      </view>

      <view class="hc-card-soft science-list-search hc-reveal-up" style="--delay: 100ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">搜索与筛选</text>
          <text class="bg-section-head__meta">{{ typeLabel }}</text>
        </view>

        <view class="science-list-search__row">
          <view class="science-list-search__input-wrap">
            <uni-icons type="search" size="18" color="#18211b" />
            <input
              v-model="keyword"
              class="science-list-search__input"
              placeholder="搜标题 / 关键字"
              confirm-type="search"
              @confirm="onSearch"
            />
          </view>
          <view class="science-list-search__submit hc-pill-button-dark hc-interactive-pill" @click="onSearch">
            搜索
          </view>
        </view>

        <view class="science-list-filter">
          <view class="hc-official-select-trigger" @click="openTypePopup">
            <text class="hc-official-select-trigger__value">{{ typeLabel }}</text>
            <uni-icons type="down" size="18" color="#5e705d" />
          </view>
          <view class="science-list-filter__pill" @click="onClear">清空关键词</view>
          <view class="science-list-filter__pill" @click="reset">重置筛选</view>
        </view>
      </view>

      <view v-if="isLoading && !items.length" class="hc-card-soft science-list-state hc-reveal-up" style="--delay: 160ms">
        <text class="science-list-state__title">正在整理内容列表</text>
        <text class="science-list-state__desc">会优先返回推荐排序的内容，便于快速开始阅读。</text>
      </view>

      <template v-else-if="items.length">
        <view
          v-for="(item, index) in items"
          :key="item.Id"
          class="hc-card-soft science-list-item hc-interactive-card hc-reveal-up"
          :style="{ '--delay': `${180 + index * 40}ms` }"
          @click="goDetail(item.Id)"
        >
          <image class="science-list-item__cover" mode="aspectFill" :src="item.CoverUrl" />
          <view class="science-list-item__body">
            <text class="science-list-item__title">{{ item.Title }}</text>
            <text class="science-list-item__summary">{{ item.Summary || "继续打开查看正文和互动区。" }}</text>
            <view class="science-list-item__meta">
              <text>阅读 {{ item.ReadCount || 0 }}</text>
              <text>赞 {{ item.LikeCount || 0 }}</text>
              <text>评 {{ item.CommentCount || 0 }}</text>
            </view>
          </view>
        </view>
      </template>

      <view v-else class="hc-card-soft science-list-empty hc-reveal-up" style="--delay: 180ms">
        <text class="science-list-empty__title">当前条件下暂无内容</text>
        <text class="science-list-empty__desc">可以换一个关键词，或把类型切回“全部”后再试一次。</text>
        <view class="hc-pill-button-dark hc-interactive-pill" @click="reset">恢复全部内容</view>
      </view>

      <view v-if="loadStatus === 'more' && items.length" class="hc-pill-button-soft science-list-more" @click="loadMore">
        加载更多
      </view>
      <view v-if="loadStatus === 'loading' && items.length" class="science-list-footnote">正在加载更多内容...</view>
      <view v-if="loadStatus === 'noMore' && items.length" class="science-list-footnote">已经到底了</view>

      <uni-popup
        ref="typePopupRef"
        type="center"
        :safe-area="false"
        background-color="transparent"
        mask-background-color="rgba(16, 24, 19, 0.58)"
      >
        <view class="hc-official-popup-card">
          <view class="hc-official-popup-handle"></view>
          <view class="hc-official-popup-head">
            <text class="hc-official-popup-eyebrow">筛选与选择</text>
            <text class="hc-official-popup-title">选择科普内容类型</text>
            <text class="hc-official-popup-subtitle">按内容方向缩小范围，更快找到需要的资料</text>
          </view>
          <view class="hc-official-popup-summary">
            <text class="hc-official-popup-summary__label">当前</text>
            <text class="hc-official-popup-summary__value">{{ typeLabel }}</text>
          </view>
          <scroll-view scroll-y class="hc-official-popup-list">
            <view
              v-for="option in typeOptions"
              :key="String(option.value)"
              class="hc-official-popup-option"
              :class="{ active: KnowledgeType === option.value }"
              @click="onTypeChange(option)"
            >
              <text class="hc-official-popup-option__text">{{ option.text }}</text>
              <text v-if="KnowledgeType === option.value" class="hc-official-popup-option__mark">当前</text>
            </view>
          </scroll-view>
        </view>
      </uni-popup>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onPullDownRefresh, onReachBottom } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const keyword = ref("");
const CategoryId = ref(null);
const KnowledgeType = ref(null);
const Page = ref(1);
const Limit = ref(10);
const TotalCount = ref(0);
const items = ref([]);
const loadStatus = ref("more");
const typePopupRef = ref(null);

const typeOptions = [
  { text: "全部", value: null },
  { text: "疾病科普", value: 1 },
  { text: "护喉贴士", value: 2 },
  { text: "发音训练", value: 3 },
  { text: "就医指南/随访", value: 4 },
  { text: "其他", value: 9 },
];

const typeLabel = ref("全部");
const isLoading = computed(() => loadStatus.value === "loading");
const filterSummary = computed(() => {
  const pieces = [];
  if (CategoryId.value) pieces.push("已带入分类筛选");
  if (keyword.value.trim()) pieces.push(`关键词：${keyword.value.trim()}`);
  if (KnowledgeType.value !== null) pieces.push(`类型：${typeLabel.value}`);
  if (!pieces.length) return "当前按推荐排序展示全部内容，可继续按类型或关键词筛选。";
  return `${pieces.join("，")}，共已加载 ${items.value.length} 条内容。`;
});

const fetchList = async (resetList = false) => {
  if (resetList) {
    Page.value = 1;
    items.value = [];
    loadStatus.value = "more";
  }
  if (loadStatus.value === "noMore") return;

  loadStatus.value = "loading";
  const { Data, Success, Msg } = await Post("/Front/Science/List", {
    Page: Page.value,
    Limit: Limit.value,
    CategoryId: CategoryId.value,
    Keyword: keyword.value ? keyword.value.trim() : "",
    KnowledgeType: KnowledgeType.value,
    Sort: "RECOMMEND",
  });

  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "加载失败" });
    loadStatus.value = "more";
    return;
  }

  const newItems = Data?.Items || [];
  TotalCount.value = Data?.TotalCount || 0;
  items.value = items.value.concat(newItems);
  if (items.value.length >= TotalCount.value || newItems.length === 0) {
    loadStatus.value = "noMore";
  } else {
    loadStatus.value = "more";
  }
};

const onSearch = async () => {
  await fetchList(true);
};

const onClear = async () => {
  keyword.value = "";
  await fetchList(true);
};

const openTypePopup = () => {
  typePopupRef.value?.open();
};

const reset = async () => {
  keyword.value = "";
  KnowledgeType.value = null;
  typeLabel.value = "全部";
  await fetchList(true);
};

const onTypeChange = async (item) => {
  if (item) {
    KnowledgeType.value = item.value;
    typeLabel.value = item.text;
  } else {
    KnowledgeType.value = null;
    typeLabel.value = "全部";
  }
  typePopupRef.value?.close();
  await fetchList(true);
};

const loadMore = async () => {
  if (loadStatus.value !== "more") return;
  Page.value += 1;
  await fetchList(false);
};

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/Front/ScienceDetail?Id=${id}` });
};

const goBack = () => {
  const pages = getCurrentPages();
  if (pages && pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/ScienceHome" });
};

onLoad(async (query) => {
  if (query?.CategoryId) CategoryId.value = Number(query.CategoryId);
  await fetchList(true);
});

onReachBottom(async () => {
  await loadMore();
});

onPullDownRefresh(async () => {
  await fetchList(true);
  uni.stopPullDownRefresh();
});
</script>

<style scoped lang="scss">
.science-list-screen {
  gap: 24upx;
}

.science-list-head {
  gap: 10upx;
}

.science-list-search__row {
  margin-top: 18upx;
  display: flex;
  gap: 12upx;
}

.science-list-search__input-wrap {
  flex: 1;
  min-width: 0;
  min-height: 86upx;
  padding: 0 22upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  align-items: center;
  gap: 12upx;
}

.science-list-search__input {
  flex: 1;
  min-height: 72upx;
  border: none;
  background: transparent;
  font-size: 24upx;
}

.science-list-search__submit {
  min-width: 160upx;
}

.science-list-filter {
  margin-top: 16upx;
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.science-list-filter__pill {
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

.science-list-state__title,
.science-list-empty__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.science-list-state__desc,
.science-list-empty__desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.science-list-item {
  display: flex;
  gap: 18upx;
}

.science-list-item__cover {
  width: 220upx;
  height: 164upx;
  border-radius: 28upx;
  background: rgba(225, 243, 154, 0.38);
  flex-shrink: 0;
}

.science-list-item__body {
  flex: 1;
  min-width: 0;
}

.science-list-item__title {
  display: block;
  font-size: 30upx;
  line-height: 1.35;
  font-weight: 800;
  color: #172119;
}

.science-list-item__summary {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.science-list-item__meta {
  margin-top: 12upx;
  display: flex;
  flex-wrap: wrap;
  gap: 16upx;
  font-size: 20upx;
  color: #7f917d;
}

.science-list-empty {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.science-list-more {
  margin-top: 4upx;
}

.science-list-footnote {
  text-align: center;
  font-size: 20upx;
  color: #7f917d;
  padding-bottom: 12upx;
}
</style>
