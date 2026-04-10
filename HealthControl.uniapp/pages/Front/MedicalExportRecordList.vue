<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack export-record-screen">
      <hc-topbar title="报告导出记录" subtitle="查看报告预览与下载形成的导出轨迹" showBack @left="back" />

      <view class="hc-card-dark export-record-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">导出轨迹</view>
        <text class="export-record-hero__title">{{ items.length }} 条已加载记录</text>
        <text class="export-record-hero__desc">生成并查看或下载就诊准备报告后，这里会沉淀对应的导出记录。</text>
      </view>

      <view class="bg-section-head hc-reveal-fade" style="--delay: 100ms">
        <text class="bg-section-head__title">导出卡片</text>
        <text class="bg-section-head__meta">点击可复制链接</text>
      </view>

      <scroll-view scroll-y class="export-record-scroll" @scrolltolower="loadMore" :lower-threshold="80">
        <view v-if="items.length" class="export-record-list">
          <view
            v-for="(item, index) in items"
            :key="item.Id"
            class="hc-card-soft export-record-item hc-interactive-card hc-reveal-up"
            :style="{ '--delay': `${140 + index * 30}ms` }"
            @click="openExport(item)"
          >
            <view class="export-record-item__head">
              <text class="export-record-item__title">导出格式：{{ item.ExportFormat || "-" }}</text>
              <text class="export-record-item__tag">{{ item.ExportPurpose || "未标注" }}</text>
            </view>
            <text class="export-record-item__meta">导出时间：{{ item.ExportTime || "-" }}</text>
            <text v-if="item.ExportPurposeText" class="export-record-item__desc">用途说明：{{ item.ExportPurposeText }}</text>
          </view>
        </view>

        <view v-else-if="!loading" class="hc-card-soft export-record-empty hc-reveal-up" style="--delay: 140ms">
          <text class="export-record-empty__title">暂无导出记录</text>
          <text class="export-record-empty__desc">查看或下载报告后，这里会自动展示相关记录。</text>
        </view>

        <view class="export-record-loadmore">
          <text v-if="loading">加载中...</text>
          <text v-else-if="finished && items.length">已加载全部</text>
          <text v-else-if="items.length">继续下拉加载更多</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const query = reactive({
  Page: 1,
  Limit: 20,
});

const items = ref([]);
const loading = ref(false);
const finished = ref(false);

const back = () => {
  uni.navigateBack();
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
    const res = await Post("/Front/Medical/ExportRecordList", {
      Page: query.Page,
      Limit: query.Limit,
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

const openExport = (item) => {
  if (!item || !item.ExportFileUrl) {
    uni.showToast({ icon: "none", title: "当前记录暂无可用链接" });
    return;
  }
  uni.setClipboardData({
    data: item.ExportFileUrl,
    success: () => {
      uni.showToast({ icon: "none", title: "已复制导出链接，可在浏览器中打开" });
    },
  });
};

onLoad(() => {
  reload();
});
</script>

<style scoped lang="scss">
.export-record-screen {
  gap: 24upx;
}

.export-record-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.export-record-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.export-record-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.export-record-scroll {
  max-height: calc(100vh - 420upx);
}

.export-record-list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.export-record-item {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.export-record-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.export-record-item__title,
.export-record-empty__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.export-record-item__tag {
  padding: 8upx 14upx;
  border-radius: 999upx;
  background: rgba(225, 243, 154, 0.42);
  font-size: 20upx;
  font-weight: 800;
  color: #53683d;
}

.export-record-item__meta,
.export-record-item__desc,
.export-record-empty__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.export-record-loadmore {
  padding: 20upx 0 8upx;
  text-align: center;
  font-size: 22upx;
  color: #7f917d;
}
</style>
