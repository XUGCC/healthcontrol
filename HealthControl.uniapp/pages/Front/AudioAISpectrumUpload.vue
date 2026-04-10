<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack spectrum-upload-screen">
      <hc-topbar
        title="图谱上传解读"
        subtitle="上传频谱图，获取一段更可读的 AI 结论"
        :show-back="true"
        @left="goBack"
      />

      <view class="bg-head spectrum-upload-head hc-reveal-up">
        <view class="hc-kicker">上传分析</view>
        <text class="bg-head__title">先交给 AI 看图谱，再决定是否继续追问</text>
        <text class="bg-head__subtitle">
          支持 Mel 与 MFCC 图谱，上传后会生成简要结论并保留后续咨询入口。
        </text>
      </view>

      <view class="hc-card-lime spectrum-upload-hero hc-reveal-up" style="--delay: 100ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">上传前准备</text>
          <text class="hc-section-head__meta">PNG / JPG 均可</text>
        </view>
        <view class="hc-token-tabs spectrum-upload-tabs">
          <view
            class="hc-token-tabs__item"
            :class="{ active: spectrumType === 'MEL' }"
            @click="spectrumType = 'MEL'"
          >
            Mel 图谱
          </view>
          <view
            class="hc-token-tabs__item"
            :class="{ active: spectrumType === 'MFCC' }"
            @click="spectrumType = 'MFCC'"
          >
            MFCC 图谱
          </view>
        </view>
        <text class="spectrum-upload-hero__note">
          AI 解读仅供健康参考，不构成医疗诊断或个性化医疗建议，不能替代医生就诊。
        </text>
      </view>

      <view class="hc-card-soft spectrum-upload-card hc-reveal-up" style="--delay: 160ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">上传区域</text>
          <text class="bg-section-head__meta">{{ imagePath ? "已选中 1 张图谱" : "请选择一张图谱" }}</text>
        </view>

        <view class="spectrum-upload-card__dropzone" @click="chooseImage">
          <template v-if="imagePath">
            <image class="spectrum-upload-card__image" :src="imagePath" mode="widthFix" />
          </template>
          <template v-else>
            <text class="spectrum-upload-card__placeholder-title">从相册或相机选择图谱</text>
            <text class="spectrum-upload-card__placeholder-desc">建议上传清晰、无遮挡的频谱图，便于 AI 做结构化解释。</text>
          </template>
        </view>

        <view class="spectrum-upload-card__actions">
          <view class="hc-pill-button-soft hc-interactive-pill" @click="chooseImage">
            {{ imagePath ? "重新选择" : "选择图片" }}
          </view>
          <view
            class="hc-pill-button-dark hc-interactive-pill"
            :class="{ disabled: !imagePath || uploading }"
            @click="uploadAndAnalyze"
          >
            {{ uploading ? "上传中..." : "上传并解读" }}
          </view>
        </view>
      </view>

      <view v-if="uploading" class="hc-card-soft spectrum-upload-state hc-reveal-up" style="--delay: 220ms">
        <text class="spectrum-upload-state__title">正在上传并生成解读</text>
        <text class="spectrum-upload-state__desc">系统会先校验图片，再整理一段适合快速扫读的结果。</text>
      </view>

      <view v-if="report" class="hc-card-dark spectrum-upload-result hc-reveal-up hc-shine" style="--delay: 240ms">
        <view class="hc-kicker hc-kicker--dark">结果摘要</view>
        <text class="spectrum-upload-result__title">{{ reportTitle }}</text>
        <text class="spectrum-upload-result__desc">{{ summaryText }}</text>
        <view class="spectrum-upload-result__meta">
          <view class="spectrum-upload-result__meta-item">
            <text class="spectrum-upload-result__meta-label">图谱类型</text>
            <text class="spectrum-upload-result__meta-value">{{ spectrumType }}</text>
          </view>
          <view class="spectrum-upload-result__meta-item">
            <text class="spectrum-upload-result__meta-label">风险等级</text>
            <text class="spectrum-upload-result__meta-value">{{ riskText }}</text>
          </view>
        </view>
      </view>

      <view v-if="report" class="hc-card-soft spectrum-upload-next hc-reveal-up" style="--delay: 300ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">下一步动作</text>
          <text class="bg-section-head__meta">可继续看说明或回到咨询页</text>
        </view>
        <view class="spectrum-upload-next__actions">
          <view class="hc-pill-button-soft hc-interactive-pill" @click="goReport">查看结果说明</view>
          <view class="hc-pill-button-dark hc-interactive-pill" @click="goChat">继续咨询</view>
        </view>
      </view>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { API_BASE_URL } from "@/utils/config";
import { GetLoginToken } from "@/utils/cache";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const spectrumType = ref("MEL");
const imagePath = ref("");
const uploading = ref(false);
const report = ref(null);
const dialogRef = ref(null);
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const riskText = computed(() => {
  const level = report.value?.RiskLevel;
  if (level === "LOW") return "低风险";
  if (level === "MEDIUM") return "中风险";
  if (level === "HIGH") return "高风险";
  return "已生成";
});

const reportTitle = computed(() => {
  const level = report.value?.RiskLevel;
  if (level === "HIGH") return "当前图谱更值得优先关注";
  if (level === "MEDIUM") return "当前图谱建议继续跟进观察";
  if (level === "LOW") return "当前图谱结果整体偏稳";
  return "图谱解读已生成";
});

const summaryText = computed(() => {
  return report.value?.SummaryText || "已生成上传图谱的 AI 解读结果，可继续查看说明或返回咨询页。";
});

const chooseImage = async () => {
  uni.chooseImage({
    count: 1,
    sizeType: ["original", "compressed"],
    sourceType: ["album", "camera"],
    success: (result) => {
      const path = result?.tempFilePaths?.[0];
      if (path) imagePath.value = path;
    },
  });
};

const uploadAndAnalyze = async () => {
  if (!imagePath.value || uploading.value) return;

  uploading.value = true;
  report.value = null;

  const token = GetLoginToken();
  const header = token ? { Authorization: `Bearer ${token}` } : {};

  uni.uploadFile({
    url: `${API_BASE_URL}/FrontAI/AnalyzeSpectrumUpload?SpectrumType=${spectrumType.value}`,
    filePath: imagePath.value,
    name: "file",
    header,
    success: (uploadFileRes) => {
      try {
        const data = JSON.parse(uploadFileRes.data || "{}");
        if (data.Success) {
          report.value = data.Data || null;
          uni.showToast({ title: "解读成功", icon: "success" });
        } else {
          uni.showToast({ title: data.Msg || "解读失败", icon: "none" });
        }
      } catch (error) {
        uni.showToast({ title: "解析响应失败", icon: "none" });
      }
    },
    fail: () => {
      uni.showToast({ title: "上传失败", icon: "none" });
    },
    complete: () => {
      uploading.value = false;
    },
  });
};

const goReport = () => {
  if (!report.value?.ReportId) return;
  showDialog({
    title: "提示",
    content: "上传解读报告已生成。当前版本暂不提供独立报告详情页，可先回到咨询页继续追问。",
    showCancel: false,
  });
};

const goChat = () => {
  uni.navigateTo({ url: "/pages/Front/AudioAIChat" });
};

const goBack = () => uni.navigateBack({ delta: 1 });
</script>

<style scoped lang="scss">
.spectrum-upload-screen {
  gap: 24upx;
}

.spectrum-upload-head {
  gap: 10upx;
}

.spectrum-upload-hero {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.spectrum-upload-tabs {
  margin-top: 4upx;
}

.spectrum-upload-hero .hc-token-tabs__item {
  background: rgba(248, 252, 239, 0.78);
}

.spectrum-upload-hero .hc-token-tabs__item.active {
  background: #151d17;
  color: #f4fbdd;
}

.spectrum-upload-hero__note {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #435442;
}

.spectrum-upload-card__dropzone {
  margin-top: 18upx;
  min-height: 360upx;
  border-radius: 34upx;
  border: 1upx dashed rgba(145, 196, 65, 0.62);
  background: rgba(248, 252, 239, 0.82);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22upx;
  overflow: hidden;
}

.spectrum-upload-card__image {
  width: 100%;
  display: block;
  border-radius: 24upx;
}

.spectrum-upload-card__placeholder-title {
  display: block;
  font-size: 30upx;
  line-height: 1.3;
  font-weight: 800;
  color: #1d2a1f;
  text-align: center;
}

.spectrum-upload-card__placeholder-desc {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
  text-align: center;
}

.spectrum-upload-card__actions,
.spectrum-upload-next__actions {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.hc-pill-button-dark.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.spectrum-upload-state__title {
  font-size: 32upx;
  font-weight: 800;
  color: #1d2a1f;
}

.spectrum-upload-state__desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.6;
  color: #556556;
}

.spectrum-upload-result {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.spectrum-upload-result__title {
  display: block;
  font-size: 42upx;
  line-height: 1.15;
  font-weight: 800;
  color: #f7ffdf;
}

.spectrum-upload-result__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.7;
  color: rgba(241, 248, 223, 0.76);
}

.spectrum-upload-result__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.spectrum-upload-result__meta-item {
  padding: 16upx 18upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.spectrum-upload-result__meta-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.spectrum-upload-result__meta-value {
  display: block;
  margin-top: 8upx;
  font-size: 24upx;
  font-weight: 800;
  color: #f7ffdf;
}
</style>
