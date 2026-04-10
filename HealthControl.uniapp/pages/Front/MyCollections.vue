<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack my-collections-screen">
      <hc-topbar
        title="我的收藏"
        subtitle="把值得反复看的内容留在自己的阅读清单里"
        :show-back="true"
        @left="back"
      />

      <view class="bg-head my-collections-head hc-reveal-up">
        <view class="hc-kicker">收藏列表</view>
        <text class="bg-head__title">先看收藏清单，再挑一篇继续回看</text>
        <text class="bg-head__subtitle">适合反复查看护理方法、发音训练和就医提醒类内容。</text>
      </view>

      <view class="hc-card-lime my-collections-summary hc-reveal-up" style="--delay: 100ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">收藏概览</text>
          <text class="hc-section-head__meta">持续沉淀个人内容库</text>
        </view>
        <view class="my-collections-summary__metric">
          <text class="my-collections-summary__metric-label">当前收藏数量</text>
          <text class="my-collections-summary__metric-value">{{ items.length }}</text>
        </view>
      </view>

      <view v-if="loading" class="hc-card-soft my-collections-state hc-reveal-up" style="--delay: 160ms">
        <text class="my-collections-state__title">正在整理收藏清单</text>
        <text class="my-collections-state__desc">系统会优先展示你已收藏的内容，方便继续回看。</text>
      </view>

      <template v-else-if="items.length">
        <view
          v-for="(item, index) in items"
          :key="item.Id"
          class="hc-card-soft my-collections-item hc-interactive-card hc-reveal-up"
          :style="{ '--delay': `${180 + index * 40}ms` }"
          @click="goDetail(item.Id)"
        >
          <image class="my-collections-item__cover" mode="aspectFill" :src="item.CoverUrl" />
          <view class="my-collections-item__body">
            <text class="my-collections-item__title">{{ item.Title }}</text>
            <text class="my-collections-item__summary">{{ item.Summary || "继续打开查看完整正文和评论区。" }}</text>
          </view>
        </view>
      </template>

      <view v-else class="hc-card-soft my-collections-empty hc-reveal-up" style="--delay: 180ms">
        <text class="my-collections-empty__title">你还没有收藏内容</text>
        <text class="my-collections-empty__desc">看到值得反复阅读的内容时，可以先收藏，后续再回来继续看。</text>
        <view class="hc-pill-button-dark hc-interactive-pill" @click="goScienceHome">去看科普</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();
const items = ref([]);
const loading = ref(false);

const load = async () => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录" });
    return;
  }

  loading.value = true;
  const { Data, Success, Msg } = await Post("/Front/Science/MyCollectList", {
    Page: 1,
    Limit: 50,
  });
  loading.value = false;

  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "加载失败" });
    return;
  }

  items.value = Data?.Items || [];
};

const goDetail = (id) => uni.navigateTo({ url: `/pages/Front/ScienceDetail?Id=${id}` });
const goScienceHome = () => uni.navigateTo({ url: "/pages/Front/ScienceHome" });
const back = () => uni.navigateBack({ delta: 1 });

onShow(load);
</script>

<style scoped lang="scss">
.my-collections-screen {
  gap: 24upx;
}

.my-collections-head {
  gap: 10upx;
}

.my-collections-summary__metric {
  margin-top: 18upx;
  padding: 18upx 20upx;
  border-radius: 24upx;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.my-collections-summary__metric-label {
  display: block;
  font-size: 20upx;
  color: #556556;
}

.my-collections-summary__metric-value {
  display: block;
  margin-top: 8upx;
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.my-collections-state__title,
.my-collections-empty__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.my-collections-state__desc,
.my-collections-empty__desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.my-collections-item {
  display: flex;
  gap: 18upx;
}

.my-collections-item__cover {
  width: 220upx;
  height: 164upx;
  border-radius: 28upx;
  background: rgba(225, 243, 154, 0.38);
  flex-shrink: 0;
}

.my-collections-item__body {
  flex: 1;
  min-width: 0;
}

.my-collections-item__title {
  display: block;
  font-size: 30upx;
  line-height: 1.35;
  font-weight: 800;
  color: #172119;
}

.my-collections-item__summary {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.my-collections-empty {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}
</style>
