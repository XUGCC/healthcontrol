<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack rights-history-screen">
      <hc-topbar title="数据权利申请记录" subtitle="回看导出与删除申请的处理进度" showBack @left="back" />

      <view class="hc-card-dark rights-history-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">权利记录</view>
        <text class="rights-history-hero__title">{{ list.length }} 条可见记录</text>
        <text class="rights-history-hero__desc">导出和删除申请会合并展示，方便统一追踪审核、处理中和完成状态。</text>
        <view class="rights-history-hero__meta">
          <view class="rights-history-hero__metric">
            <text class="rights-history-hero__metric-label">导出申请</text>
            <text class="rights-history-hero__metric-value">{{ exportItems.length }}</text>
          </view>
          <view class="rights-history-hero__metric">
            <text class="rights-history-hero__metric-label">删除申请</text>
            <text class="rights-history-hero__metric-value">{{ deleteItems.length }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft rights-history-filter hc-reveal-up" style="--delay: 100ms">
        <view class="hc-token-tabs">
          <view class="hc-token-tabs__item" :class="{ active: tab === 'all' }" @click="tab = 'all'">全部</view>
          <view class="hc-token-tabs__item" :class="{ active: tab === 'export' }" @click="tab = 'export'">导出申请</view>
          <view class="hc-token-tabs__item" :class="{ active: tab === 'delete' }" @click="tab = 'delete'">删除申请</view>
        </view>
      </view>

      <view v-if="list.length" class="rights-history-list">
        <view
          v-for="(item, index) in list"
          :key="item._key"
          class="hc-card-soft rights-history-item hc-reveal-up"
          :style="{ '--delay': `${160 + index * 30}ms` }"
        >
          <view class="rights-history-item__head">
            <text class="rights-history-item__title">{{ item.TypeText }}</text>
            <text class="rights-history-item__status" :class="statusClass(item.Status)">{{ statusText(item.Status) }}</text>
          </view>
          <text class="rights-history-item__meta">申请时间：{{ item.CreationTime || "-" }}</text>
          <text class="rights-history-item__desc">内容：{{ item.DataTypeText || "-" }}</text>
          <text v-if="item.Type === 'export' && item.ExportFormat" class="rights-history-item__desc">格式：{{ item.ExportFormat }}</text>
          <text v-if="item.Status === 'Rejected' && item.ErrorMessage" class="rights-history-item__desc rights-history-item__desc--danger">
            拒绝原因：{{ item.ErrorMessage }}
          </text>

          <view v-if="item.Type === 'export' && (item.Status === 'Approved' || item.Status === 'Completed')" class="rights-history-item__actions">
            <view class="rights-history-item__action" @click="download(item.Id, item.ExportFormat)">
              {{ item.Status === "Approved" ? "生成并下载" : "下载" }}
            </view>
          </view>
        </view>
      </view>

      <view v-else class="hc-card-soft rights-history-empty hc-reveal-up" style="--delay: 160ms">
        <text class="rights-history-empty__title">暂无申请记录</text>
        <text class="rights-history-empty__desc">可以先发起导出或删除申请，记录会在这里统一汇总。</text>
      </view>

      <view class="hc-card-lime rights-history-actions hc-reveal-up" style="--delay: 220ms">
        <text class="rights-history-actions__title">继续操作</text>
        <view class="rights-history-actions__buttons">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="goExport">申请导出</view>
          <view class="hc-pill-button hc-interactive-pill" @click="goDelete">申请删除</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="reload">刷新</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onPullDownRefresh, onReachBottom, onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { API_BASE_URL } from "@/utils/config";
import { GetLoginToken } from "@/utils/cache";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const tab = ref("all");
const exportItems = ref([]);
const deleteItems = ref([]);
const pageSize = 20;
const exportPage = ref(1);
const deletePage = ref(1);
const exportHasMore = ref(true);
const deleteHasMore = ref(true);
const loadingMore = ref(false);

const back = () => {
  uni.navigateBack({ delta: 1 });
};

const parseDataTypeText = (raw) => {
  if (!raw) return "";
  try {
    const array = JSON.parse(raw);
    if (Array.isArray(array)) return array.join("、");
  } catch (error) {
    console.error("parseDataTypeText error:", error);
  }
  return String(raw);
};

const list = computed(() => {
  const merged = [];
  for (const item of exportItems.value) {
    merged.push({
      _key: `export_${item.Id}`,
      Type: "export",
      TypeText: "导出申请",
      ...item,
      DataTypeText: parseDataTypeText(item.DataType),
    });
  }
  for (const item of deleteItems.value) {
    merged.push({
      _key: `delete_${item.Id}`,
      Type: "delete",
      TypeText: "删除申请",
      ...item,
      DataTypeText: parseDataTypeText(item.DataType),
    });
  }
  merged.sort((a, b) => (b.CreationTime || "").localeCompare(a.CreationTime || ""));
  if (tab.value === "export") return merged.filter((item) => item.Type === "export");
  if (tab.value === "delete") return merged.filter((item) => item.Type === "delete");
  return merged;
});

const statusText = (status) => {
  if (status === "PendingAudit") return "待审核";
  if (status === "Approved") return "已通过（处理中）";
  if (status === "Rejected") return "已拒绝";
  if (status === "Pending") return "待处理";
  if (status === "Processing") return "处理中";
  if (status === "Completed") return "已完成";
  if (status === "Failed") return "失败";
  return status || "未知";
};

const statusClass = (status) => {
  if (status === "Completed") return "ok";
  if (status === "Processing" || status === "Approved") return "ing";
  if (status === "Rejected" || status === "Failed") return "bad";
  return "pending";
};

const loadFirstPage = async () => {
  exportPage.value = 1;
  deletePage.value = 1;
  exportHasMore.value = true;
  deleteHasMore.value = true;
  loadingMore.value = false;

  const { Data: exportRes, Success: exportOk } = await Post("/Front/Privacy/ExportRequestList", {
    Page: exportPage.value,
    Limit: pageSize,
  });
  const exportList = exportOk && exportRes?.Items ? exportRes.Items : [];
  exportItems.value = exportList;
  exportHasMore.value = exportList.length >= pageSize;

  const { Data: deleteRes, Success: deleteOk } = await Post("/Front/Privacy/DeleteRequestList", {
    Page: deletePage.value,
    Limit: pageSize,
  });
  const deleteList = deleteOk && deleteRes?.Items ? deleteRes.Items : [];
  deleteItems.value = deleteList;
  deleteHasMore.value = deleteList.length >= pageSize;
};

const reload = async () => {
  await loadFirstPage();
};

const goExport = () => {
  uni.navigateTo({ url: "/pages/Front/DataExportRequest" });
};

const goDelete = () => {
  uni.navigateTo({ url: "/pages/Front/DataDeleteRequest" });
};

onShow(() => {
  reload();
});

onPullDownRefresh(async () => {
  await reload();
  uni.stopPullDownRefresh();
});

onReachBottom(async () => {
  if (loadingMore.value || (!exportHasMore.value && !deleteHasMore.value)) return;
  loadingMore.value = true;
  try {
    if (exportHasMore.value) {
      const nextPage = exportPage.value + 1;
      const { Data, Success } = await Post("/Front/Privacy/ExportRequestList", {
        Page: nextPage,
        Limit: pageSize,
      });
      const list = Success && Data?.Items ? Data.Items : [];
      if (list.length) {
        exportItems.value = exportItems.value.concat(list);
        exportPage.value = nextPage;
      }
      if (list.length < pageSize) {
        exportHasMore.value = false;
      }
    }

    if (deleteHasMore.value) {
      const nextPage = deletePage.value + 1;
      const { Data, Success } = await Post("/Front/Privacy/DeleteRequestList", {
        Page: nextPage,
        Limit: pageSize,
      });
      const list = Success && Data?.Items ? Data.Items : [];
      if (list.length) {
        deleteItems.value = deleteItems.value.concat(list);
        deletePage.value = nextPage;
      }
      if (list.length < pageSize) {
        deleteHasMore.value = false;
      }
    }
  } finally {
    loadingMore.value = false;
  }
});

const download = (requestId, fmt) => {
  const token = GetLoginToken();
  const url = `${API_BASE_URL}/Front/Privacy/DownloadExport?RequestId=${requestId}`;

  uni.showLoading({ title: "下载中..." });
  uni.downloadFile({
    url,
    header: token ? { Authorization: `Bearer ${token}` } : {},
    success: (res) => {
      if (res.statusCode === 200) {
        // #ifdef H5
        const link = document.createElement("a");
        const format = (fmt || "JSON").toUpperCase();
        const ext = format === "EXCEL" ? ".xls" : format === "PDF" ? ".pdf" : ".json";
        link.href = res.tempFilePath || url;
        link.download = `export_${requestId}${ext}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        uni.showToast({ title: "已开始下载", icon: "success" });
        // #endif

        // #ifndef H5
        uni.saveFile({
          tempFilePath: res.tempFilePath,
          success: () => uni.showToast({ title: "已保存到本地", icon: "success" }),
          fail: () => uni.showToast({ title: "保存失败", icon: "none" }),
        });
        // #endif
      } else {
        uni.showToast({ title: "下载失败", icon: "none" });
      }
    },
    fail: () => uni.showToast({ title: "下载失败", icon: "none" }),
    complete: () => uni.hideLoading(),
  });
};
</script>

<style scoped lang="scss">
.rights-history-screen {
  gap: 24upx;
}

.rights-history-hero,
.rights-history-filter,
.rights-history-empty,
.rights-history-actions {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.rights-history-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.rights-history-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.rights-history-hero__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.rights-history-hero__metric {
  padding: 18upx 20upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.rights-history-hero__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.rights-history-hero__metric-value {
  display: block;
  margin-top: 8upx;
  font-size: 32upx;
  font-weight: 800;
  color: #f7ffdf;
}

.rights-history-list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.rights-history-item {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.rights-history-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.rights-history-item__title,
.rights-history-empty__title,
.rights-history-actions__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.rights-history-item__status {
  padding: 8upx 14upx;
  border-radius: 999upx;
  font-size: 20upx;
  font-weight: 800;
}

.rights-history-item__status.ok {
  background: rgba(140, 207, 67, 0.14);
  color: #56773a;
}

.rights-history-item__status.ing {
  background: rgba(78, 134, 255, 0.12);
  color: #3e67bf;
}

.rights-history-item__status.pending {
  background: rgba(127, 145, 125, 0.16);
  color: #667964;
}

.rights-history-item__status.bad {
  background: rgba(228, 108, 73, 0.12);
  color: #a14d34;
}

.rights-history-item__meta,
.rights-history-item__desc,
.rights-history-empty__desc,
.rights-history-actions__buttons {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.rights-history-item__desc--danger {
  color: #a14d34;
}

.rights-history-item__actions {
  margin-top: 6upx;
}

.rights-history-item__action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 76upx;
  padding: 0 24upx;
  border-radius: 999upx;
  background: #172119;
  font-size: 22upx;
  font-weight: 700;
  color: #f7ffdf;
}

.rights-history-actions__buttons {
  display: flex;
  flex-direction: column;
  gap: 12upx;
  margin-top: 2upx;
}
</style>
