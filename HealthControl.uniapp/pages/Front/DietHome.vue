<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack diet-home-screen">
      <hc-topbar title="饮食建议" subtitle="先确定今天的饮食方向，再进入食物库和记录页" />

      <view class="hc-card-dark diet-home-hero hc-reveal-up hc-shine">
        <view class="diet-home-hero__head">
          <view class="hc-kicker hc-kicker--dark">今日饮食护理</view>
          <view class="diet-home-hero__badge">{{ heroBadge }}</view>
        </view>
        <text class="diet-home-hero__title">{{ heroTitle }}</text>
        <text class="diet-home-hero__desc">{{ summaryText }}</text>

        <view class="diet-home-hero__metrics">
          <view class="diet-home-hero__metric">
            <text class="diet-home-hero__metric-label">今日记录</text>
            <text class="diet-home-hero__metric-value">{{ todayTotal }}</text>
          </view>
          <view class="diet-home-hero__metric">
            <text class="diet-home-hero__metric-label">友好分类</text>
            <text class="diet-home-hero__metric-value">{{ friendlyCategories.length }}</text>
          </view>
          <view class="diet-home-hero__metric">
            <text class="diet-home-hero__metric-label">注意分类</text>
            <text class="diet-home-hero__metric-value">{{ avoidCategories.length }}</text>
          </view>
        </view>

        <view class="diet-home-hero__actions">
          <view class="hc-pill-button hc-interactive-pill" @click="goFoodList(0)">友好食物库</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="goRecordList">我的饮食记录</view>
        </view>
      </view>

      <view v-if="isLoading && !hasLoaded" class="hc-card-soft diet-home-state hc-reveal-up" style="--delay: 90ms">
        <text class="diet-home-state__title">正在整理今日饮食建议</text>
        <text class="diet-home-state__desc">友好食物、注意分类和饮食记录会一起加载出来。</text>
      </view>

      <view v-else-if="loadError" class="hc-card-soft diet-home-state diet-home-state--error hc-reveal-up" style="--delay: 90ms">
        <text class="diet-home-state__title">当前无法加载饮食建议</text>
        <text class="diet-home-state__desc">{{ loadError }}</text>
        <view class="diet-home-state__actions">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="loadHome">重新加载</view>
        </view>
      </view>

      <template v-else>
        <view class="diet-home-entry-grid">
          <view class="hc-card-soft diet-home-entry hc-reveal-up hc-interactive-card" style="--delay: 100ms" @click="goFoodList(0)">
            <text class="diet-home-entry__eyebrow">优先安排</text>
            <text class="diet-home-entry__title">友好食物</text>
            <text class="diet-home-entry__desc">把温和、低刺激、适合当前恢复节奏的食物放在前面看。</text>
            <view class="diet-home-entry__foot">
              <text class="diet-home-entry__meta">{{ friendlyCategories.length }} 组分类</text>
              <text class="diet-home-entry__link">进入</text>
            </view>
          </view>

          <view class="hc-card-soft diet-home-entry hc-reveal-up hc-interactive-card" style="--delay: 160ms" @click="goFoodList(1)">
            <text class="diet-home-entry__eyebrow">先避开</text>
            <text class="diet-home-entry__title">需要注意</text>
            <text class="diet-home-entry__desc">提前避开容易刺激喉部的食物，先把风险项缩小。</text>
            <view class="diet-home-entry__foot">
              <text class="diet-home-entry__meta">{{ avoidCategories.length }} 组分类</text>
              <text class="diet-home-entry__link">进入</text>
            </view>
          </view>

          <view class="hc-card-lime diet-home-entry diet-home-entry--record hc-reveal-up hc-interactive-card" style="--delay: 220ms" @click="goRecordList">
            <text class="diet-home-entry__eyebrow">恢复轨迹</text>
            <text class="diet-home-entry__title">饮食记录</text>
            <text class="diet-home-entry__desc">{{ recordSummary }}</text>
            <view class="diet-home-entry__foot">
              <text class="diet-home-entry__meta">今日 {{ todayTotal }} 次</text>
              <text class="diet-home-entry__link">查看</text>
            </view>
          </view>
        </view>

        <view class="bg-section-head hc-reveal-fade" style="--delay: 260ms">
          <text class="bg-section-head__title">今日重点分类</text>
          <text class="bg-section-head__meta">按分类快速进入</text>
        </view>

        <view class="diet-home-focus-grid">
          <view class="hc-card-soft diet-home-focus hc-reveal-up" style="--delay: 300ms">
            <view class="diet-home-focus__head">
              <view>
                <text class="diet-home-focus__title">友好方向</text>
                <text class="diet-home-focus__desc">{{ friendlyFocusText }}</text>
              </view>
              <view class="diet-home-focus__badge">推荐</view>
            </view>

            <view v-if="friendlyPreview.length" class="diet-home-focus__tags">
              <view
                v-for="item in friendlyPreview"
                :key="item.id"
                class="diet-home-focus__tag hc-interactive-pill"
                @click="goFoodListByCategory(0, item.id)"
              >
                {{ item.name }}
              </view>
            </view>
            <view v-else class="diet-home-focus__empty">当前还没有友好分类，先进入食物库查看全部内容。</view>

            <view class="diet-home-focus__action hc-pill-button-dark hc-interactive-pill" @click="goFoodList(0)">
              查看友好食物
            </view>
          </view>

          <view class="hc-card-soft diet-home-focus hc-reveal-up" style="--delay: 360ms">
            <view class="diet-home-focus__head">
              <view>
                <text class="diet-home-focus__title">需要注意</text>
                <text class="diet-home-focus__desc">{{ avoidFocusText }}</text>
              </view>
              <view class="diet-home-focus__badge diet-home-focus__badge--warn">留意</view>
            </view>

            <view v-if="avoidPreview.length" class="diet-home-focus__tags">
              <view
                v-for="item in avoidPreview"
                :key="item.id"
                class="diet-home-focus__tag diet-home-focus__tag--warn hc-interactive-pill"
                @click="goFoodListByCategory(1, item.id)"
              >
                {{ item.name }}
              </view>
            </view>
            <view v-else class="diet-home-focus__empty">当前还没有需要注意的分类，可先按清淡稳定节奏安排饮食。</view>

            <view class="diet-home-focus__action hc-pill-button hc-interactive-pill" @click="goFoodList(1)">
              查看注意食物
            </view>
          </view>
        </view>

        <view class="hc-card-soft diet-home-guidance hc-reveal-up" style="--delay: 420ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">今日安排建议</text>
            <text class="bg-section-head__meta">{{ todayDirection }}</text>
          </view>

          <view class="diet-home-guidance__grid">
            <view class="diet-home-guidance__item">
              <text class="diet-home-guidance__label">优先看</text>
              <text class="diet-home-guidance__value">{{ friendlySummaryLine }}</text>
            </view>
            <view class="diet-home-guidance__item">
              <text class="diet-home-guidance__label">重点避开</text>
              <text class="diet-home-guidance__value">{{ avoidSummaryLine }}</text>
            </view>
          </view>

          <text class="diet-home-guidance__note">{{ recordNote }}</text>
        </view>
      </template>
    </view>

    <footer-bar />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onPullDownRefresh, onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import FooterBar from "@/components/footer-bar/footer-bar.vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();

const todayStatus = ref({});
const friendlyCategories = ref([]);
const avoidCategories = ref([]);
const todayTotal = ref(0);
const isLoading = ref(false);
const hasLoaded = ref(false);
const loadError = ref("");

const normalizeCategoryList = (source = []) =>
  source
    .map((item) => ({
      id: item.CategoryId || item.Id || item.CategoryName || item.Name,
      name: item.CategoryName || item.Name || "未命名分类",
    }))
    .filter((item) => item.id && item.name);

const friendlyPreview = computed(() => normalizeCategoryList(friendlyCategories.value).slice(0, 6));
const avoidPreview = computed(() => normalizeCategoryList(avoidCategories.value).slice(0, 6));

const summaryText = computed(() => {
  const data = todayStatus.value || {};
  return (
    data.SummaryText ||
    data.StatusText ||
    data.DietSummary ||
    "先按友好食物和需要注意两组清单安排今天的饮食，再把关键饮食记进恢复轨迹。"
  );
});

const heroTitle = computed(() => {
  if (todayTotal.value > 0) {
    return "今天先延续稳定饮食，再把关键食物补充进护理记录";
  }
  return "今天先吃对，再开始记录哪些饮食更适合当前喉部状态";
});

const heroBadge = computed(() => {
  if (avoidCategories.value.length > 0) return "先避刺激";
  if (friendlyCategories.value.length > 0) return "平稳安排";
  return "清淡优先";
});

const friendlyFocusText = computed(() => {
  if (friendlyPreview.value.length) {
    return `优先从 ${friendlyPreview.value.length} 组分类里挑选适合今天安排的食物。`;
  }
  return "先从全部食物库里找温和、低刺激的食物。";
});

const avoidFocusText = computed(() => {
  if (avoidPreview.value.length) {
    return `把容易刺激喉部的 ${avoidPreview.value.length} 组分类先标记出来。`;
  }
  return "当前没有明显的注意分类，继续保持稳定清淡即可。";
});

const friendlySummaryLine = computed(() => {
  if (!friendlyPreview.value.length) return "温和、清淡、低刺激方向";
  return friendlyPreview.value.slice(0, 3).map((item) => item.name).join("、");
});

const avoidSummaryLine = computed(() => {
  if (!avoidPreview.value.length) return "暂未发现明显高风险分类";
  return avoidPreview.value.slice(0, 3).map((item) => item.name).join("、");
});

const todayDirection = computed(() => {
  if (avoidCategories.value.length > 0 && friendlyCategories.value.length > 0) {
    return "先避开刺激，再安排友好食物";
  }
  if (friendlyCategories.value.length > 0) {
    return "以温和饮食为主";
  }
  return "保持清淡稳定";
});

const recordSummary = computed(() => {
  if (todayTotal.value > 0) {
    return `今天已记录 ${todayTotal.value} 次饮食，可继续补充食后感受。`;
  }
  return "今天还没有饮食记录，建议从食物详情页开始记第一条。";
});

const recordNote = computed(() => {
  if (todayTotal.value > 0) {
    return "继续把食后感受和当天频次记录下来，后面回看时会更容易判断哪些饮食更适合当前恢复节奏。";
  }
  return "建议先从今天真正吃过的关键食物开始记录，优先补充容易引起不适或明显舒缓的那几项。";
});

const loadHome = async () => {
  isLoading.value = true;
  loadError.value = "";
  try {
    const res = await Post("/Front/Diet/Home", {});
    if (!res.Success) {
      loadError.value = res.Msg || "加载失败，请稍后重试";
      uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
      return;
    }

    const data = res.Data || {};
    todayStatus.value = data.TodayStatus || data.TodayDietStatus || data.todayStatus || {};
    friendlyCategories.value = data.FriendlyCategories || data.FriendlyCategoryList || [];
    avoidCategories.value = data.AvoidCategories || data.AvoidCategoryList || [];
    todayTotal.value = data.TodayTotal || data.RecordCount || 0;
    hasLoaded.value = true;
  } catch (error) {
    console.error("loadHome error:", error);
    loadError.value = "饮食建议加载失败，请稍后重试";
    uni.showToast({ icon: "none", title: "加载失败" });
  } finally {
    isLoading.value = false;
  }
};

onShow(() => {
  loadHome();
});

onPullDownRefresh(async () => {
  await loadHome();
  uni.stopPullDownRefresh();
});

const goFoodList = (categoryType = 0) => {
  uni.navigateTo({
    url: `/pages/Front/DietFoodList?categoryType=${categoryType}`,
  });
};

const goFoodListByCategory = (categoryType, categoryId) => {
  const query = [`categoryType=${categoryType}`];
  if (categoryId) {
    query.push(`categoryId=${categoryId}`);
  }
  uni.navigateTo({
    url: `/pages/Front/DietFoodList?${query.join("&")}`,
  });
};

const goRecordList = () => {
  if (!commonStore.UserId) {
    uni.showToast({ title: "请先登录", icon: "none" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/Front/Login" });
    }, 1200);
    return;
  }
  uni.navigateTo({
    url: "/pages/Front/DietRecordList",
  });
};
</script>

<style scoped lang="scss">
.diet-home-screen {
  gap: 30upx;
}

.diet-home-hero {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.diet-home-hero__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16upx;
}

.diet-home-hero__badge {
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

.diet-home-hero__title {
  display: block;
  font-size: 54upx;
  line-height: 1.06;
  font-weight: 800;
  color: #f7ffdf;
}

.diet-home-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.68;
  color: rgba(241, 248, 223, 0.76);
}

.diet-home-hero__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12upx;
}

.diet-home-hero__metric {
  min-height: 132upx;
  padding: 18upx 18upx 20upx;
  border-radius: 26upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.diet-home-hero__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.7);
}

.diet-home-hero__metric-value {
  display: block;
  margin-top: 10upx;
  font-size: 34upx;
  line-height: 1;
  font-weight: 800;
  color: #f7ffdf;
}

.diet-home-hero__actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14upx;
}

.diet-home-state {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.diet-home-state--error {
  border-color: rgba(228, 108, 73, 0.34);
  background: rgba(255, 243, 238, 0.92);
}

.diet-home-state__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.diet-home-state__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.diet-home-state__actions {
  margin-top: 6upx;
}

.diet-home-entry-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18upx;
}

.diet-home-entry {
  min-height: 250upx;
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.diet-home-entry--record {
  grid-column: 1 / -1;
  min-height: auto;
}

.diet-home-entry__eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 1upx;
  color: #7a8a75;
}

.diet-home-entry__title {
  display: block;
  font-size: 34upx;
  line-height: 1.18;
  font-weight: 800;
  color: #172119;
}

.diet-home-entry__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.62;
  color: #556556;
}

.diet-home-entry__foot {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14upx;
}

.diet-home-entry__meta {
  font-size: 20upx;
  color: #7f917d;
}

.diet-home-entry__link {
  font-size: 22upx;
  font-weight: 800;
  color: #172119;
}

.diet-home-focus-grid {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.diet-home-focus {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.diet-home-focus__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16upx;
}

.diet-home-focus__title {
  display: block;
  font-size: 34upx;
  line-height: 1.18;
  font-weight: 800;
  color: #172119;
}

.diet-home-focus__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.62;
  color: #556556;
}

.diet-home-focus__badge {
  min-height: 52upx;
  padding: 0 18upx;
  border-radius: 999upx;
  background: #172119;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
  color: #f7ffdf;
  flex-shrink: 0;
}

.diet-home-focus__badge--warn {
  background: #efb34d;
  color: #35240b;
}

.diet-home-focus__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.diet-home-focus__tag {
  min-height: 62upx;
  padding: 0 22upx;
  border-radius: 999upx;
  background: rgba(225, 243, 154, 0.52);
  border: 1upx solid rgba(183, 210, 111, 0.8);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 24upx;
  font-weight: 700;
  color: #172119;
}

.diet-home-focus__tag--warn {
  background: rgba(245, 216, 154, 0.42);
  border-color: rgba(239, 179, 77, 0.7);
}

.diet-home-focus__empty {
  font-size: 22upx;
  line-height: 1.62;
  color: #7c8e7b;
}

.diet-home-focus__action {
  margin-top: 2upx;
}

.diet-home-guidance {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.diet-home-guidance__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16upx;
}

.diet-home-guidance__item {
  min-height: 138upx;
  padding: 20upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.82);
  border: 1upx solid rgba(205, 224, 145, 0.84);
}

.diet-home-guidance__label {
  display: block;
  font-size: 20upx;
  color: #7a8a75;
}

.diet-home-guidance__value {
  display: block;
  margin-top: 10upx;
  font-size: 28upx;
  line-height: 1.36;
  font-weight: 800;
  color: #172119;
}

.diet-home-guidance__note {
  font-size: 22upx;
  line-height: 1.7;
  color: #556556;
}
</style>
