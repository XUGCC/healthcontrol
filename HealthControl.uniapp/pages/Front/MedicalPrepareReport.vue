<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack medical-report-screen">
      <hc-topbar
        title="就诊准备报告"
        subtitle="生成一份结构化报告，方便线下就诊时快速沟通"
        rightText="导出记录"
        showBack
        @left="back"
        @right="goExportRecordList"
      />

      <view class="hc-card-lime medical-report-hero hc-reveal-up">
        <view class="hc-kicker">报告生成</view>
        <text class="medical-report-hero__title">先选报告风格，再把近期检测和症状整理成可分享的就诊材料</text>
        <text class="medical-report-hero__desc">不填写检测范围时，系统会自动选取最近几次检测和症状记录生成报告。</text>
      </view>

      <view class="hc-card-soft medical-report-create hc-reveal-up" style="--delay: 100ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">生成新报告</text>
          <text class="bg-section-head__meta">{{ createForm.TemplateType === 0 ? "简版" : "详版" }}</text>
        </view>

        <view class="hc-token-tabs">
          <view class="hc-token-tabs__item" :class="{ active: createForm.TemplateType === 0 }" @click="setTemplateType(0)">
            简版
          </view>
          <view class="hc-token-tabs__item" :class="{ active: createForm.TemplateType === 1 }" @click="setTemplateType(1)">
            详版
          </view>
        </view>

        <text class="medical-report-create__desc">
          {{ createForm.TemplateType === 0 ? "适合门诊快速沟通，重点呈现近期状态。" : "包含更多历史信息，适合做完整就诊准备。" }}
        </text>

        <view class="hc-pill-button-dark hc-interactive-pill" @click="create">
          {{ creating ? "正在生成..." : "生成报告" }}
        </view>
      </view>

      <view class="bg-section-head hc-reveal-fade" style="--delay: 160ms">
        <text class="bg-section-head__title">历史报告</text>
        <text class="bg-section-head__meta">{{ items.length }} 条记录</text>
      </view>

      <view v-if="items.length" class="medical-report-list">
        <view
          v-for="(item, index) in items"
          :key="item.Id"
          class="hc-card-soft medical-report-item hc-reveal-up"
          :class="{ 'medical-report-item--focus': focusReportId === item.Id }"
          :style="{ '--delay': `${200 + index * 30}ms` }"
        >
          <view class="medical-report-item__head">
            <text class="medical-report-item__title">{{ item.ReportTitle || "就诊准备报告" }}</text>
            <text class="medical-report-item__status" :class="`status-${item.GenerateStatus ?? 0}`">
              {{ formatStatusText(item.GenerateStatus) }}
            </text>
          </view>
          <text class="medical-report-item__meta">创建时间：{{ item.CreationTime || "-" }}</text>
          <text v-if="item.ViewCount != null" class="medical-report-item__meta">查看 {{ item.ViewCount }} 次</text>
          <view class="medical-report-item__actions">
            <view class="medical-report-item__action" @click="openReport(item)">查看报告</view>
            <view
              v-if="item.GenerateStatus === 2"
              class="medical-report-item__action medical-report-item__action--ghost"
              @click="downloadPdf(item)"
            >
              下载 PDF
            </view>
          </view>
        </view>
      </view>

      <view v-else-if="!loading" class="hc-card-soft medical-report-empty hc-reveal-up" style="--delay: 200ms">
        <text class="medical-report-empty__title">还没有报告记录</text>
        <text class="medical-report-empty__desc">建议在准备线下就诊前先生成一份就诊准备报告。</text>
      </view>

      <view class="hc-card-soft medical-report-tip hc-reveal-up" style="--delay: 260ms">
        <text class="medical-report-tip__title">说明</text>
        <text class="medical-report-tip__desc">
          报告中的内容主要来源于您在本小程序中的检测、症状记录和档案信息，仅供就诊沟通使用，不构成诊断结论。
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { API_BASE_URL } from "@/utils/config";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();

const createForm = reactive({
  TemplateType: 0,
  DetectIdList: [],
  ContentScope: "",
});

const items = ref([]);
const loading = ref(false);
const creating = ref(false);
const focusReportId = ref(0);

const back = () => {
  uni.navigateBack();
};

const setTemplateType = (value) => {
  createForm.TemplateType = value;
};

const formatStatusText = (value) => {
  if (value === 0) return "待生成";
  if (value === 1) return "生成中";
  if (value === 2) return "已完成";
  if (value === 3) return "生成失败";
  return "未知";
};

const toAbsoluteUrl = (url) => {
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return API_BASE_URL + url;
  return `${API_BASE_URL}/${url}`;
};

const reload = async () => {
  loading.value = true;
  try {
    const res = await Post("/Front/Medical/PrepareReportList", {
      Page: 1,
      Limit: 50,
    });
    if (!res.Success) {
      uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
      return;
    }
    const data = res.Data || {};
    items.value = data.Items || [];
  } finally {
    loading.value = false;
  }
};

const create = async () => {
  if (creating.value) return;
  creating.value = true;
  try {
    const res = await Post("/Front/Medical/PrepareReportCreate", {
      TemplateType: createForm.TemplateType,
      DetectIdList: createForm.DetectIdList,
      ContentScope: createForm.ContentScope || "",
    });
    if (!res.Success) {
      uni.showToast({ icon: "none", title: res.Msg || "生成失败" });
      return;
    }
    uni.showToast({ icon: "none", title: "已发起生成，请稍后在列表中查看状态" });
    reload();
  } finally {
    creating.value = false;
  }
};

const openReport = async (item) => {
  if (!item) return;
  if (item.GenerateStatus !== 2) {
    uni.showToast({ icon: "none", title: `当前状态：${formatStatusText(item.GenerateStatus)}` });
    return;
  }
  const res = await Post("/Front/Medical/PrepareReportDetail", {
    ReportId: Number(item.Id) || 0,
  });
  if (!res.Success) {
    uni.showToast({ icon: "none", title: res.Msg || "打开失败" });
    return;
  }
  const data = res.Data || {};
  const htmlUrl = data.HtmlDocUrl || data.StandardDocUrl;
  const pdfUrl = data.PdfDocUrl;
  const absoluteHtmlUrl = htmlUrl ? toAbsoluteUrl(htmlUrl) : "";
  const absolutePdfUrl = pdfUrl ? toAbsoluteUrl(pdfUrl) : "";
  if (!absoluteHtmlUrl && !absolutePdfUrl) {
    uni.showToast({ icon: "none", title: "当前报告暂无可用预览链接" });
    return;
  }
  if (absoluteHtmlUrl) {
    await Post("/Front/Medical/ExportRecordCreate", {
      ReportId: Number(item.Id) || 0,
      ExportFormat: "HTML_VIEW",
      ExportPurpose: "VIEW",
      ExportFileUrl: absoluteHtmlUrl,
    });
    uni.navigateTo({
      url: `/pages/Front/MedicalReportWebView?url=${encodeURIComponent(absoluteHtmlUrl)}`,
    });
    return;
  }
  await Post("/Front/Medical/ExportRecordCreate", {
    ReportId: Number(item.Id) || 0,
    ExportFormat: "PDF",
    ExportPurpose: "VIEW",
    ExportFileUrl: absolutePdfUrl || pdfUrl,
  });
  uni.setClipboardData({
    data: absolutePdfUrl,
    success: () => {
      uni.showToast({ icon: "none", title: "已复制 PDF 链接，可在浏览器中打开" });
    },
  });
};

const downloadPdf = async (item) => {
  if (!item || item.GenerateStatus !== 2) {
    uni.showToast({ icon: "none", title: `当前状态：${formatStatusText(item?.GenerateStatus)}` });
    return;
  }
  const res = await Post("/Front/Medical/PrepareReportDetail", {
    ReportId: Number(item.Id) || 0,
  });
  if (!res.Success) {
    uni.showToast({ icon: "none", title: res.Msg || "获取报告信息失败" });
    return;
  }
  const data = res.Data || {};
  const absolutePdfUrl = data.PdfDocUrl ? toAbsoluteUrl(data.PdfDocUrl) : "";
  if (!absolutePdfUrl) {
    uni.showToast({ icon: "none", title: "当前报告暂无 PDF 链接" });
    return;
  }
  await Post("/Front/Medical/ExportRecordCreate", {
    ReportId: Number(item.Id) || 0,
    ExportFormat: "PDF",
    ExportPurpose: "DOWNLOAD",
    ExportFileUrl: absolutePdfUrl,
  });
  uni.downloadFile({
    url: absolutePdfUrl,
    header: {
      Authorization: commonStore.Token ? `Bearer ${commonStore.Token}` : "",
    },
    success: (resp) => {
      if (resp.statusCode === 200) {
        uni.openDocument({
          filePath: resp.tempFilePath,
          showMenu: true,
        });
      } else {
        uni.showToast({ icon: "none", title: "PDF 下载失败，请稍后重试" });
      }
    },
    fail: () => {
      uni.showToast({ icon: "none", title: "PDF 下载失败，请检查网络或服务地址" });
    },
  });
};

const goExportRecordList = () => {
  uni.navigateTo({ url: "/pages/Front/MedicalExportRecordList" });
};

onLoad((options) => {
  focusReportId.value = Number(options?.reportId || 0) || 0;
  reload();
});
</script>

<style scoped lang="scss">
.medical-report-screen {
  gap: 24upx;
}

.medical-report-hero,
.medical-report-create,
.medical-report-empty,
.medical-report-tip {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.medical-report-hero__title,
.medical-report-empty__title,
.medical-report-tip__title {
  display: block;
  font-size: 40upx;
  line-height: 1.15;
  font-weight: 800;
  color: #172119;
}

.medical-report-hero__desc,
.medical-report-create__desc,
.medical-report-empty__desc,
.medical-report-tip__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #455442;
}

.medical-report-list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.medical-report-item {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.medical-report-item--focus {
  border: 2upx solid rgba(140, 207, 67, 0.55);
}

.medical-report-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.medical-report-item__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.medical-report-item__status {
  padding: 8upx 14upx;
  border-radius: 999upx;
  font-size: 20upx;
  font-weight: 800;
}

.medical-report-item__status.status-0 {
  background: rgba(239, 179, 77, 0.16);
  color: #91692a;
}

.medical-report-item__status.status-1 {
  background: rgba(225, 243, 154, 0.42);
  color: #53683d;
}

.medical-report-item__status.status-2 {
  background: rgba(140, 207, 67, 0.14);
  color: #56773a;
}

.medical-report-item__status.status-3 {
  background: rgba(228, 108, 73, 0.12);
  color: #a14d34;
}

.medical-report-item__meta {
  font-size: 20upx;
  color: #7f917d;
}

.medical-report-item__actions {
  display: flex;
  gap: 12upx;
  margin-top: 6upx;
}

.medical-report-item__action {
  padding: 10upx 18upx;
  border-radius: 999upx;
  background: #172119;
  font-size: 22upx;
  font-weight: 700;
  color: #f7ffdf;
}

.medical-report-item__action--ghost {
  background: rgba(225, 243, 154, 0.42);
  color: #172119;
}
</style>
