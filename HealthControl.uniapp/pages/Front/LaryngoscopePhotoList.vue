<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack laryngoscope-list-screen">
      <hc-topbar
        title="喉镜影像档案"
        subtitle="按检查批次整理你的喉镜资料，便于后续回看和补充"
        :show-back="true"
        right-text="上传"
        @left="back"
        @right="goCreate"
      />

      <view class="hc-card-dark laryngoscope-list-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">线下资料</view>
        <text class="laryngoscope-list-hero__title">{{ groupedItems.length }} 组检查记录</text>
        <text class="laryngoscope-list-hero__desc">原有分组、编辑、预览和删除逻辑保持不变，这里只把列表换成主页化的分组卡结构。</text>
        <view class="laryngoscope-list-hero__meta">
          <view class="laryngoscope-list-hero__metric">
            <text class="laryngoscope-list-hero__metric-label">照片总数</text>
            <text class="laryngoscope-list-hero__metric-value">{{ items.length }}</text>
          </view>
          <view class="laryngoscope-list-hero__metric">
            <text class="laryngoscope-list-hero__metric-label">当前状态</text>
            <text class="laryngoscope-list-hero__metric-value">{{ loading ? "加载中" : "可查看" }}</text>
          </view>
        </view>
      </view>

      <view v-if="!items.length && !loading" class="hc-card-soft laryngoscope-empty hc-reveal-up" style="--delay: 90ms">
        <text class="laryngoscope-empty__title">还没有喉镜照片记录</text>
        <text class="laryngoscope-empty__desc">完成喉镜检查后，可以在这里或检测结果页上传线下检查图片，后续会自动归档到同一列表中。</text>
        <view class="hc-pill-button-dark hc-interactive-pill" @click="goCreate">立即上传</view>
      </view>

      <view v-else class="laryngoscope-group-list">
        <view
          v-for="(group, index) in groupedItems"
          :key="group.key"
          class="hc-card-soft laryngoscope-group hc-reveal-up"
          :style="{ '--delay': `${100 + index * 35}ms` }"
        >
          <view class="laryngoscope-group__head">
            <view>
              <text class="laryngoscope-group__title">{{ buildGroupTitle(group) }}</text>
              <text class="laryngoscope-group__meta">检查时间：{{ group.checkTime ? formatTime(group.checkTime) : "未填写" }}</text>
            </view>
            <view class="laryngoscope-group__badge">{{ group.items.length }} 张</view>
          </view>

          <view class="laryngoscope-group__photos">
            <view v-for="item in group.items" :key="item.Id" class="laryngoscope-card">
              <view class="laryngoscope-card__thumb" @click="preview(item.LaryngoscopePhotoUrl)">
                <image :src="item.LaryngoscopePhotoUrl" mode="aspectFill" class="laryngoscope-card__image" />
              </view>
              <text class="laryngoscope-card__title">{{ buildTitle(item) }}</text>
              <text class="laryngoscope-card__time">{{ item.UploadTime ? `上传：${formatTime(item.UploadTime)}` : "上传时间未知" }}</text>
              <text v-if="item.PhotoDesc" class="laryngoscope-card__desc">{{ item.PhotoDesc }}</text>
              <view class="laryngoscope-card__actions">
                <view class="hc-pill-button-soft hc-interactive-pill" @click="edit(item)">编辑</view>
                <view class="hc-pill-button hc-interactive-pill laryngoscope-card__delete" @click="remove(item)">删除</view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="items.length && hasMore" class="hc-pill-button-soft hc-interactive-pill laryngoscope-more hc-reveal-up" style="--delay: 180ms" @click="loadMore">
        {{ loading ? "加载中..." : "加载更多" }}
      </view>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onPullDownRefresh, onReachBottom, onShow } from "@dcloudio/uni-app";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import { Post } from "@/utils/http";

const page = ref(1);
const limit = ref(10);
const loading = ref(false);
const hasMore = ref(true);
const items = ref([]);
const dialogRef = ref(null);

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/UserCenter" });
};
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const goCreate = () => {
  uni.navigateTo({ url: "/pages/Front/LaryngoscopePhotoEdit" });
};

const formatTime = (val) => {
  if (!val) return "";
  return String(val).replace("T", " ");
};

const buildTitle = (item) => {
  if (item.PhotoDesc) return String(item.PhotoDesc).split("|")[0].trim();
  return "喉镜检查记录";
};

const groupedItems = computed(() => {
  const map = {};
  for (const item of items.value || []) {
    const datePart = item.CheckTime ? String(item.CheckTime).split("T")[0] : "";
    const hospital = item.HospitalName || "";
    const key = `${datePart}|${hospital}` || `id_${item.Id}`;
    if (!map[key]) {
      map[key] = {
        key,
        checkTime: item.CheckTime,
        hospitalName: hospital,
        items: [],
      };
    }
    map[key].items.push(item);
  }
  return Object.values(map);
});

const buildGroupTitle = (group) => {
  const datePart = group.checkTime ? String(group.checkTime).split("T")[0] : "";
  if (datePart && group.hospitalName) return `${datePart} ${group.hospitalName} 喉镜检查`;
  if (group.hospitalName) return `${group.hospitalName} 喉镜检查`;
  if (datePart) return `${datePart} 喉镜检查`;
  return "喉镜检查记录";
};

const preview = (url) => {
  if (!url) return;
  uni.previewImage({
    urls: [url],
    current: url,
  });
};

const fetchList = async (reset = false) => {
  if (loading.value || (!hasMore.value && !reset)) return;

  loading.value = true;
  if (reset) {
    page.value = 1;
    hasMore.value = true;
    items.value = [];
  }

  try {
    const { Data, Success, Msg } = await Post("/Front/Laryngoscope/List", {
      Page: page.value,
      Limit: limit.value,
    });

    if (!Success) {
      uni.showToast({ title: Msg || "加载失败", icon: "none" });
      return;
    }

    const list = Data?.Items || Data?.items || [];
    items.value = reset ? list : items.value.concat(list);
    hasMore.value = list.length >= limit.value;
    if (hasMore.value) {
      page.value += 1;
    }
  } catch (error) {
    console.error("fetchList error:", error);
    uni.showToast({ title: "网络异常，请稍后重试", icon: "none" });
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  fetchList(false);
};

const edit = (item) => {
  const detectId = item.DetectId || "";
  uni.navigateTo({
    url: `/pages/Front/LaryngoscopePhotoEdit?id=${item.Id}&detectId=${detectId}`,
  });
};

const remove = async (item) => {
  const result = await showDialog({
    title: "确认删除",
    content: "删除后这张喉镜照片将不再出现在你的档案中，是否继续？",
  });
  if (!result.confirm) return;
  const { Success } = await Post("/Front/Laryngoscope/Delete", { Id: item.Id });
  if (!Success) return;

  uni.showToast({ title: "已删除", icon: "success" });
  fetchList(true);
};

onShow(() => {
  fetchList(true);
});

onPullDownRefresh(async () => {
  await fetchList(true);
  uni.stopPullDownRefresh();
});

onReachBottom(() => {
  loadMore();
});
</script>

<style scoped lang="scss">
.laryngoscope-list-screen {
  gap: 24upx;
}

.laryngoscope-list-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.laryngoscope-list-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.1;
  font-weight: 800;
  color: #f7ffdf;
}

.laryngoscope-list-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.76);
}

.laryngoscope-list-hero__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.laryngoscope-list-hero__metric {
  padding: 18upx 20upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.laryngoscope-list-hero__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.laryngoscope-list-hero__metric-value {
  display: block;
  margin-top: 8upx;
  font-size: 30upx;
  font-weight: 800;
  color: #f7ffdf;
}

.laryngoscope-empty__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-empty__desc {
  display: block;
  margin: 8upx 0 18upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.laryngoscope-group-list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.laryngoscope-group__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12upx;
}

.laryngoscope-group__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-group__meta {
  display: block;
  margin-top: 8upx;
  font-size: 20upx;
  color: #7c8e7b;
}

.laryngoscope-group__badge {
  min-height: 54upx;
  padding: 0 18upx;
  border-radius: 999upx;
  background: #172119;
  color: #f7ffdf;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
}

.laryngoscope-group__photos {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14upx;
}

.laryngoscope-card {
  padding: 14upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.laryngoscope-card__thumb {
  width: 100%;
  height: 220upx;
}

.laryngoscope-card__image {
  width: 100%;
  height: 100%;
  border-radius: 22upx;
  background: #dfe7d2;
}

.laryngoscope-card__title {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-card__time,
.laryngoscope-card__desc {
  display: block;
  margin-top: 6upx;
  font-size: 20upx;
  line-height: 1.6;
  color: #556556;
}

.laryngoscope-card__actions {
  margin-top: 12upx;
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.laryngoscope-card__delete {
  background: rgba(243, 188, 171, 0.86);
  border-color: rgba(220, 129, 105, 0.72);
  color: #5f271d;
}
</style>
