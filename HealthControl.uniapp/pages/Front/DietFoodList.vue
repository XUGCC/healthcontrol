<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack diet-food-list-screen">
      <hc-topbar title="饮食食物库" :subtitle="`按 ${currentTypeText} 筛选和搜索适合当前喉部状态的食物`" @left="back" />

      <view class="hc-card-dark diet-food-list-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">食物筛选</view>
        <text class="diet-food-list-hero__title">{{ currentTypeText }} · {{ selectedCategoryName }}</text>
        <text class="diet-food-list-hero__desc">先按食物类型和分类快速聚焦，再根据名称或别名继续查找。</text>
        <view class="diet-food-list-hero__meta">
          <view class="diet-food-list-hero__metric">
            <text class="diet-food-list-hero__metric-label">当前结果</text>
            <text class="diet-food-list-hero__metric-value">{{ total }}</text>
          </view>
          <view class="diet-food-list-hero__metric">
            <text class="diet-food-list-hero__metric-label">分类数</text>
            <text class="diet-food-list-hero__metric-value">{{ categories.length }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft diet-food-list-filter hc-reveal-up" style="--delay: 100ms">
        <view class="hc-token-tabs">
          <view class="hc-token-tabs__item" :class="{ active: categoryType === 0 }" @click="switchType(0)">友好食物</view>
          <view class="hc-token-tabs__item" :class="{ active: categoryType === 1 }" @click="switchType(1)">需要注意</view>
        </view>

        <view class="diet-food-list-filter__search">
          <uni-easyinput
            v-model="keyword"
            placeholder="搜索食物名称或别名"
            clearable
            @confirm="reload"
          />
          <view class="hc-pill-button-dark hc-interactive-pill" @click="reload">搜索</view>
        </view>

        <scroll-view scroll-x class="diet-food-list-filter__scroll" :show-scrollbar="false">
          <view class="hc-token-tabs diet-food-list-filter__tabs">
            <view class="hc-token-tabs__item" :class="{ active: selectedCategoryId === 0 }" @click="pickCategory(0)">全部</view>
            <view
              v-for="item in categories"
              :key="item.CategoryId"
              class="hc-token-tabs__item"
              :class="{ active: selectedCategoryId === item.CategoryId }"
              @click="pickCategory(item.CategoryId)"
            >
              {{ item.CategoryName }}
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="bg-section-head hc-reveal-fade" style="--delay: 160ms">
        <text class="bg-section-head__title">食物卡片</text>
        <text class="bg-section-head__meta">{{ total }} 条结果</text>
      </view>

      <scroll-view scroll-y class="diet-food-list-scroll" @scrolltolower="loadMore">
        <view v-if="items.length" class="diet-food-list-grid">
          <view
            v-for="(item, index) in items"
            :key="item.FoodId"
            class="hc-card-soft diet-food-item hc-interactive-card hc-reveal-up"
            :style="{ '--delay': `${200 + index * 30}ms` }"
            @click="goDetail(item.FoodId)"
          >
            <image class="diet-food-item__thumb" :src="item.PicUrl || defaultImg" mode="aspectFill" />
            <view class="diet-food-item__body">
              <view class="diet-food-item__head">
                <text class="diet-food-item__name">{{ item.FoodName }}</text>
                <text class="diet-food-item__badge" :class="{ warn: item.CategoryType === 1 }">
                  {{ item.CategoryType === 1 ? "少吃" : "推荐" }}
                </text>
              </view>
              <text v-if="item.FoodAlias" class="diet-food-item__alias">别名：{{ item.FoodAlias }}</text>
              <text class="diet-food-item__summary">{{ item.Summary || "继续打开查看对喉部的影响和食用建议。" }}</text>
            </view>
          </view>
        </view>

        <view v-else class="hc-card-soft diet-food-list-empty hc-reveal-up" style="--delay: 200ms">
          <text class="diet-food-list-empty__title">当前筛选下还没有内容</text>
          <text class="diet-food-list-empty__desc">可以切换食物类型、分类，或者换个关键词试试。</text>
        </view>

        <view class="diet-food-list-loadmore">
          <text v-if="hasMore && items.length">继续下拉加载更多</text>
          <text v-else-if="items.length">已加载全部内容</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const defaultImg = "/static/food-placeholder.png";
const categoryType = ref(0);
const keyword = ref("");
const page = ref(1);
const limit = ref(10);
const items = ref([]);
const total = ref(0);
const hasMore = ref(true);
const categories = ref([]);
const selectedCategoryId = ref(0);

const categoryId = computed(() => (selectedCategoryId.value === 0 ? null : selectedCategoryId.value));
const currentTypeText = computed(() => (categoryType.value === 1 ? "需要注意" : "友好食物"));
const selectedCategoryName = computed(() => {
  if (selectedCategoryId.value === 0) return "全部分类";
  return categories.value.find((item) => item.CategoryId === selectedCategoryId.value)?.CategoryName || "指定分类";
});

onLoad((options) => {
  if (options && options.categoryType != null) {
    const type = Number.parseInt(options.categoryType, 10);
    if (type === 0 || type === 1) {
      categoryType.value = type;
    }
  }
  if (options && options.categoryId) {
    selectedCategoryId.value = Number.parseInt(options.categoryId, 10) || 0;
  }
  init();
});

const back = () => {
  const pages = getCurrentPages();
  if (pages && pages.length > 1) {
    uni.navigateBack({ delta: 1 });
  } else {
    uni.reLaunch({ url: "/pages/Front/DietHome" });
  }
};

const reload = async () => {
  page.value = 1;
  items.value = [];
  await load();
};

const init = async () => {
  await loadCategories();
  await reload();
};

const loadCategories = async () => {
  const res = await Post("/Front/Diet/CategoryList", {
    CategoryType: categoryType.value,
  });
  if (!res.Success) {
    categories.value = [];
    return;
  }
  categories.value = res.Data || [];
  if (selectedCategoryId.value && !categories.value.some((item) => item.CategoryId === selectedCategoryId.value)) {
    selectedCategoryId.value = 0;
  }
};

const load = async () => {
  const res = await Post("/Front/Diet/FoodList", {
    Page: page.value,
    Limit: limit.value,
    CategoryType: categoryType.value,
    CategoryId: categoryId.value,
    Keyword: keyword.value || null,
  });
  if (!res.Success) {
    uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
    return;
  }
  const data = res.Data || {};
  const list = data.Items || [];
  total.value = data.TotalCount || 0;
  items.value = items.value.concat(list);
  hasMore.value = items.value.length < total.value;
};

const loadMore = async () => {
  if (!hasMore.value) return;
  page.value += 1;
  await load();
};

const switchType = async (type) => {
  if (categoryType.value === type) return;
  categoryType.value = type;
  selectedCategoryId.value = 0;
  await init();
};

const pickCategory = async (id) => {
  if (selectedCategoryId.value === id) return;
  selectedCategoryId.value = id;
  await reload();
};

const goDetail = (foodId) => {
  uni.navigateTo({
    url: `/pages/Front/DietFoodDetail?foodId=${foodId}`,
  });
};
</script>

<style scoped lang="scss">
.diet-food-list-screen {
  gap: 24upx;
}

.diet-food-list-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.diet-food-list-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.diet-food-list-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.diet-food-list-hero__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.diet-food-list-hero__metric {
  padding: 18upx 20upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.diet-food-list-hero__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.diet-food-list-hero__metric-value {
  display: block;
  margin-top: 8upx;
  font-size: 32upx;
  font-weight: 800;
  color: #f7ffdf;
}

.diet-food-list-filter {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.diet-food-list-filter__search {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.diet-food-list-filter__scroll {
  white-space: nowrap;
}

.diet-food-list-filter__tabs {
  display: inline-flex;
  flex-wrap: nowrap;
}

.diet-food-list-scroll {
  max-height: calc(100vh - 520upx);
}

.diet-food-list-grid {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.diet-food-item {
  display: flex;
  gap: 18upx;
}

.diet-food-item__thumb {
  width: 200upx;
  height: 160upx;
  border-radius: 28upx;
  background: rgba(225, 243, 154, 0.34);
  flex-shrink: 0;
}

.diet-food-item__body {
  flex: 1;
  min-width: 0;
}

.diet-food-item__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12upx;
}

.diet-food-item__name {
  display: block;
  font-size: 30upx;
  line-height: 1.35;
  font-weight: 800;
  color: #172119;
}

.diet-food-item__badge {
  padding: 8upx 14upx;
  border-radius: 999upx;
  background: #172119;
  font-size: 20upx;
  font-weight: 800;
  color: #f7ffdf;
  flex-shrink: 0;
}

.diet-food-item__badge.warn {
  background: #efb34d;
  color: #35240b;
}

.diet-food-item__alias {
  display: block;
  margin-top: 8upx;
  font-size: 20upx;
  color: #7f917d;
}

.diet-food-item__summary {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.6;
  color: #556556;
}

.diet-food-list-empty {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.diet-food-list-empty__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.diet-food-list-empty__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.diet-food-list-loadmore {
  padding: 20upx 0 8upx;
  text-align: center;
  font-size: 22upx;
  color: #7f917d;
}
</style>
