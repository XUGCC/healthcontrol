<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack recommend-list-screen">
      <hc-topbar title="我的就医建议" subtitle="按查看状态回看系统生成的建议记录" showBack @left="back" />

      <view class="hc-card-dark recommend-list-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">建议记录</view>
        <text class="recommend-list-hero__title">{{ currentViewStatusText }} · {{ items.length }} 条已加载</text>
        <text class="recommend-list-hero__desc">当检测结果或风险等级达到一定阈值后，系统会在这里沉淀就医建议。</text>
      </view>

      <view class="hc-card-soft recommend-list-filter hc-reveal-up" style="--delay: 100ms">
        <view class="hc-token-tabs">
          <view class="hc-token-tabs__item" :class="{ active: query.ViewStatus === '' }" @click="pickViewStatus('')">全部</view>
          <view class="hc-token-tabs__item" :class="{ active: query.ViewStatus === 0 }" @click="pickViewStatus(0)">未查看</view>
          <view class="hc-token-tabs__item" :class="{ active: query.ViewStatus === 1 }" @click="pickViewStatus(1)">已查看</view>
        </view>
      </view>

      <view class="bg-section-head hc-reveal-fade" style="--delay: 160ms">
        <text class="bg-section-head__title">建议卡片</text>
        <text class="bg-section-head__meta">{{ currentViewStatusText }}</text>
      </view>

      <scroll-view scroll-y class="recommend-list-scroll" @scrolltolower="loadMore" :lower-threshold="80">
        <view v-if="items.length" class="recommend-list-cards">
          <view
            v-for="(item, index) in items"
            :key="item.Id"
            class="hc-card-soft recommend-card hc-interactive-card hc-reveal-up"
            :style="{ '--delay': `${200 + index * 30}ms` }"
            @click="goDetail(item.Id)"
          >
            <view class="recommend-card__head">
              <text class="recommend-card__risk" :class="`level-${item.RiskLevel ?? -1}`">{{ formatRiskLevel(item.RiskLevel) }}</text>
              <text class="recommend-card__status" :class="{ active: !item.ViewStatus }">{{ item.ViewStatus ? "已查看" : "未查看" }}</text>
            </view>
            <text class="recommend-card__title">{{ item.Title || "就医建议" }}</text>
            <text class="recommend-card__desc">
              {{ item.RecommendReason || "基于近期检测结果和症状，系统建议关注线下就诊评估。" }}
            </text>
            <text class="recommend-card__meta">推荐时间：{{ item.RecommendTime || "-" }}</text>
          </view>
        </view>

        <view v-else-if="!loading" class="hc-card-soft recommend-list-empty hc-reveal-up" style="--delay: 200ms">
          <text class="recommend-list-empty__title">暂无就医建议记录</text>
          <text class="recommend-list-empty__desc">当检测结果或风险等级达到一定阈值后，这里会生成建议记录。</text>
        </view>

        <view class="recommend-list-loadmore">
          <text v-if="loading">加载中...</text>
          <text v-else-if="finished && items.length">已加载全部</text>
          <text v-else-if="items.length">继续下拉加载更多</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const query = reactive({
  Page: 1,
  Limit: 10,
  ViewStatus: "",
});

const items = ref([]);
const loading = ref(false);
const finished = ref(false);

const currentViewStatusText = computed(() => {
  if (query.ViewStatus === 0) return "未查看";
  if (query.ViewStatus === 1) return "已查看";
  return "全部";
});

const back = () => {
  uni.navigateBack();
};

const formatRiskLevel = (value) => {
  if (value === 2) return "高风险";
  if (value === 1) return "中风险";
  if (value === 0) return "低风险";
  return "未知风险";
};

const pickViewStatus = (value) => {
  if (query.ViewStatus === value) return;
  query.ViewStatus = value;
  reload();
};

const reload = () => {
  query.Page = 1;
  items.value = [];
  finished.value = false;
  load();
};

const load = async () => {
  if (loading.value || finished.value) return;
  loading.value = true;
  try {
    const res = await Post("/Front/Medical/RecommendList", {
      Page: query.Page,
      Limit: query.Limit,
      ViewStatus: query.ViewStatus === "" ? null : Number(query.ViewStatus),
    });
    if (!res.Success) {
      uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
      return;
    }
    const data = res.Data || {};
    const list = data.Items || [];
    items.value = query.Page === 1 ? list : items.value.concat(list);
    const total = data.TotalCount || 0;
    if (items.value.length >= total || list.length < query.Limit) {
      finished.value = true;
    } else {
      query.Page += 1;
    }
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  load();
};

const goDetail = (id) => {
  if (!id) return;
  uni.navigateTo({ url: `/pages/Front/MedicalRecommendDetail?recommendId=${id}` });
};

onLoad(() => {
  reload();
});
</script>

<style scoped lang="scss">
.recommend-list-screen {
  gap: 24upx;
}

.recommend-list-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.recommend-list-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.recommend-list-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.recommend-list-scroll {
  max-height: calc(100vh - 470upx);
}

.recommend-list-cards {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.recommend-card {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.recommend-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.recommend-card__risk,
.recommend-card__status {
  padding: 8upx 14upx;
  border-radius: 999upx;
  font-size: 20upx;
  font-weight: 800;
}

.recommend-card__risk {
  background: rgba(225, 243, 154, 0.42);
  color: #53683d;
}

.recommend-card__risk.level-1 {
  background: rgba(239, 179, 77, 0.16);
  color: #91692a;
}

.recommend-card__risk.level-2 {
  background: rgba(228, 108, 73, 0.12);
  color: #a14d34;
}

.recommend-card__status {
  background: rgba(225, 243, 154, 0.38);
  color: #53683d;
}

.recommend-card__status.active {
  background: rgba(228, 108, 73, 0.12);
  color: #a14d34;
}

.recommend-card__title,
.recommend-list-empty__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.recommend-card__desc,
.recommend-list-empty__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.recommend-card__meta {
  font-size: 20upx;
  color: #7f917d;
}

.recommend-list-loadmore {
  padding: 20upx 0 8upx;
  text-align: center;
  font-size: 22upx;
  color: #7f917d;
}
</style>
