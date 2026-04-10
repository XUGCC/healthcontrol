<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack science-home-screen">
      <hc-topbar title="健康科普" subtitle="把知识、护理建议和个人内容放进同一套内容首页" />

      <view class="hc-card-dark science-home-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">内容社区</view>
        <text class="science-home-hero__title">先看值得读的内容，再按主题继续深入</text>
        <text class="science-home-hero__desc">
          这里会汇总喉部健康科普、护嗓贴士和发音训练内容，方便持续补充日常护理知识。
        </text>
        <view class="science-home-hero__meta">
          <view class="science-home-hero__metric">
            <text class="science-home-hero__metric-label">一级分类</text>
            <text class="science-home-hero__metric-value">{{ rootCategories.length }}</text>
          </view>
          <view class="science-home-hero__metric">
            <text class="science-home-hero__metric-label">推荐内容</text>
            <text class="science-home-hero__metric-value">{{ list.length }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft science-home-search hc-reveal-up hc-interactive-card" style="--delay: 100ms" @click="goSearch">
        <view class="science-home-search__icon">
          <uni-icons type="search" size="18" color="#18211b" />
        </view>
        <view class="science-home-search__content">
          <text class="science-home-search__title">搜索科普内容</text>
          <text class="science-home-search__desc">按标题、关键字或主题继续查找</text>
        </view>
        <uni-icons type="right" size="18" color="#18211b" />
      </view>

      <view class="hc-card-soft science-home-categories hc-reveal-up" style="--delay: 160ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">主题分类</text>
          <text class="bg-section-head__meta">{{ rootCategories.length }} 个一级主题</text>
        </view>

        <scroll-view class="science-home-tabs" scroll-x :show-scrollbar="false">
          <view class="hc-token-tabs science-home-tabs__inner">
            <view
              v-for="(category, index) in rootCategories"
              :key="category.Id"
              class="hc-token-tabs__item"
              :class="{ active: index === activeRootIndex }"
              @click="selectRoot(index)"
            >
              {{ category.CategoryName }}
            </view>
          </view>
        </scroll-view>

        <view v-if="subCategories.length" class="science-home-subgrid">
          <view
            v-for="subcategory in subCategories"
            :key="subcategory.Id"
            class="science-home-subgrid__item hc-interactive-card"
            @click="goListByCategory(subcategory.Id)"
          >
            <text class="science-home-subgrid__title">{{ subcategory.CategoryName }}</text>
            <text class="science-home-subgrid__desc">查看该主题下的内容</text>
          </view>
        </view>
      </view>

      <view class="science-home-list-head bg-section-head hc-reveal-fade" style="--delay: 220ms">
        <text class="bg-section-head__title">推荐内容</text>
        <text class="bg-section-head__meta">{{ activeRootName }}</text>
      </view>

      <template v-if="list.length">
        <view
          v-for="(item, index) in list"
          :key="item.Id"
          class="hc-card-soft science-home-item hc-interactive-card hc-reveal-up"
          :style="{ '--delay': `${260 + index * 40}ms` }"
          @click="goDetail(item.Id)"
        >
          <image class="science-home-item__cover" mode="aspectFill" :src="item.CoverUrl" />
          <view class="science-home-item__body">
            <text class="science-home-item__title">{{ item.Title }}</text>
            <text v-if="item.AuthorName" class="science-home-item__author">作者：{{ item.AuthorName }}</text>
            <text class="science-home-item__summary">{{ item.Summary || "继续打开查看完整内容与互动区。" }}</text>
            <view class="science-home-item__meta">
              <text>阅读 {{ item.ReadCount || 0 }}</text>
              <text>赞 {{ item.LikeCount || 0 }}</text>
              <text>藏 {{ item.CollectCount || 0 }}</text>
            </view>
          </view>
        </view>
      </template>

      <view v-else class="hc-card-soft science-home-empty hc-reveal-up" style="--delay: 260ms">
        <text class="science-home-empty__title">当前分类还没有推荐内容</text>
        <text class="science-home-empty__desc">可以先切换分类，或者直接去列表页查看更多内容。</text>
        <view class="hc-pill-button-dark hc-interactive-pill" @click="goSearch">去内容列表</view>
      </view>
    </view>

    <footer-bar />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import FooterBar from "@/components/footer-bar/footer-bar.vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const categories = ref([]);
const activeRootIndex = ref(0);
const list = ref([]);

const rootCategories = computed(() => categories.value.filter((item) => !item.ParentId || item.ParentId === 0));
const activeRootName = computed(() => rootCategories.value[activeRootIndex.value]?.CategoryName || "推荐内容");
const subCategories = computed(() => {
  const currentRoot = rootCategories.value[activeRootIndex.value];
  if (!currentRoot) return [];
  return categories.value.filter((item) => item.ParentId === currentRoot.Id);
});

const loadCategories = async () => {
  const { Data, Success, Msg } = await Post("/Front/Science/CategoryList", { IncludeEmpty: true });
  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "加载分类失败" });
    return;
  }
  categories.value = (Data || []).flatMap((root) => [root, ...(root.Children || [])]);
  if (activeRootIndex.value >= rootCategories.value.length) {
    activeRootIndex.value = 0;
  }
};

const loadList = async () => {
  const currentRoot = rootCategories.value[activeRootIndex.value];
  const CategoryId = currentRoot ? currentRoot.Id : null;
  const { Data, Success, Msg } = await Post("/Front/Science/List", {
    Page: 1,
    Limit: 10,
    CategoryId,
    Sort: "RECOMMEND",
  });
  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "加载失败" });
    return;
  }
  list.value = Data?.Items || [];
};

const selectRoot = async (index) => {
  activeRootIndex.value = index;
  await loadList();
};

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/Front/ScienceDetail?Id=${id}` });
};

const goSearch = () => {
  uni.navigateTo({ url: "/pages/Front/ScienceList" });
};

const goListByCategory = (id) => {
  uni.navigateTo({ url: `/pages/Front/ScienceList?CategoryId=${id}` });
};

onShow(async () => {
  await loadCategories();
  await loadList();
});
</script>

<style scoped lang="scss">
.science-home-screen {
  gap: 24upx;
}

.science-home-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.science-home-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.science-home-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.science-home-hero__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
  margin-top: 6upx;
}

.science-home-hero__metric {
  padding: 18upx 20upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.science-home-hero__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.science-home-hero__metric-value {
  display: block;
  margin-top: 8upx;
  font-size: 32upx;
  font-weight: 800;
  color: #f7ffdf;
}

.science-home-search {
  display: flex;
  align-items: center;
  gap: 16upx;
}

.science-home-search__icon {
  width: 62upx;
  height: 62upx;
  border-radius: 20upx;
  background: rgba(225, 243, 154, 0.52);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.science-home-search__content {
  flex: 1;
  min-width: 0;
}

.science-home-search__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.science-home-search__desc {
  display: block;
  margin-top: 6upx;
  font-size: 22upx;
  color: #556556;
}

.science-home-tabs {
  margin-top: 18upx;
  white-space: nowrap;
}

.science-home-tabs__inner {
  display: inline-flex;
  flex-wrap: nowrap;
}

.science-home-subgrid {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.science-home-subgrid__item {
  padding: 18upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.82);
  border: 1upx solid rgba(205, 224, 145, 0.88);
}

.science-home-subgrid__title {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.science-home-subgrid__desc {
  display: block;
  margin-top: 8upx;
  font-size: 20upx;
  line-height: 1.5;
  color: #556556;
}

.science-home-item {
  display: flex;
  gap: 18upx;
}

.science-home-item__cover {
  width: 220upx;
  height: 164upx;
  border-radius: 28upx;
  background: rgba(225, 243, 154, 0.38);
  flex-shrink: 0;
}

.science-home-item__body {
  flex: 1;
  min-width: 0;
}

.science-home-item__title {
  display: block;
  font-size: 30upx;
  line-height: 1.35;
  font-weight: 800;
  color: #172119;
}

.science-home-item__author {
  display: block;
  margin-top: 8upx;
  font-size: 20upx;
  color: #7f917d;
}

.science-home-item__summary {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.6;
  color: #556556;
}

.science-home-item__meta {
  margin-top: 12upx;
  display: flex;
  flex-wrap: wrap;
  gap: 16upx;
  font-size: 20upx;
  color: #7f917d;
}

.science-home-empty {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.science-home-empty__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.science-home-empty__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}
</style>
