<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack laryngoscope-detail-screen">
      <hc-topbar
        title="喉镜影像详情"
        subtitle="查看影像、预测结果和分析建议"
        :show-back="true"
        right-text="编辑"
        @left="goBack"
        @right="editPhoto"
      />

      <view v-if="loading" class="hc-card-soft laryngoscope-detail-state hc-reveal-up">
        <text class="laryngoscope-detail-state__title">正在读取喉镜影像</text>
        <text class="laryngoscope-detail-state__desc">稍等一下，正在同步图片、预测和分析记录。</text>
      </view>

      <view v-else-if="errorMsg" class="hc-card-soft laryngoscope-detail-error hc-reveal-up">
        <text class="laryngoscope-detail-error__title">当前无法读取详情</text>
        <text class="laryngoscope-detail-error__desc">{{ errorMsg }}</text>
        <view class="hc-pill-button-dark hc-interactive-pill" @click="loadDetail">重新读取</view>
      </view>

      <template v-else-if="photo && photo.Id">
        <view class="hc-card-lime laryngoscope-photo-card hc-reveal-up">
          <view class="hc-kicker">影像资料</view>
          <text class="laryngoscope-photo-card__title">{{ photoTitle }}</text>
          <text class="laryngoscope-photo-card__desc">{{ photoDescText }}</text>
          <view class="laryngoscope-photo-card__image-wrap" @click="previewPhoto">
            <image class="laryngoscope-photo-card__image" :src="photo.LaryngoscopePhotoUrl" mode="aspectFill" />
          </view>
          <view class="laryngoscope-photo-card__meta">
            <view class="laryngoscope-photo-card__meta-item">
              <text>检查时间</text>
              <text>{{ formatTime(photo.CheckTime) || "未填写" }}</text>
            </view>
            <view class="laryngoscope-photo-card__meta-item">
              <text>医院 / 科室</text>
              <text>{{ photo.HospitalName || "未填写" }}</text>
            </view>
            <view class="laryngoscope-photo-card__meta-item">
              <text>检查方式</text>
              <text>{{ photo.CheckType || "未填写" }}</text>
            </view>
            <view class="laryngoscope-photo-card__meta-item">
              <text>上传时间</text>
              <text>{{ formatTime(photo.UploadTime) || "未填写" }}</text>
            </view>
          </view>
        </view>

        <view class="laryngoscope-summary-grid hc-reveal-up" style="--delay: 80ms">
          <view class="hc-card-dark laryngoscope-summary-card hc-shine">
            <view class="hc-kicker hc-kicker--dark">本地模型</view>
            <text class="laryngoscope-summary-card__title">{{ localHeadline }}</text>
            <text class="laryngoscope-summary-card__desc">{{ localHint }}</text>
          </view>
          <view class="hc-card-soft laryngoscope-summary-card">
            <view class="hc-kicker">Qwen-VL</view>
            <text class="laryngoscope-summary-card__title laryngoscope-summary-card__title--dark">{{ qwenHeadline }}</text>
            <text class="laryngoscope-summary-card__desc laryngoscope-summary-card__desc--dark">{{ qwenHint }}</text>
          </view>
        </view>

        <view class="hc-card-soft laryngoscope-section hc-reveal-up" style="--delay: 120ms">
          <view class="hc-section-head">
            <text class="hc-section-head__title">本地模型预测</text>
            <text class="hc-section-head__meta">{{ localStatusText }}</text>
          </view>

          <view v-if="localPrediction && localPrediction.Status === 'SUCCESS'" class="laryngoscope-local-result">
            <view class="laryngoscope-local-result__head">
              <view>
                <text class="laryngoscope-local-result__label">{{ localPrediction.PredictedLabel || "预测已完成" }}</text>
                <text class="laryngoscope-local-result__meta">
                  置信度 {{ formatPercent(localPrediction.Confidence) || "-" }}
                </text>
              </view>
            </view>

            <view class="laryngoscope-confidence">
              <view class="laryngoscope-confidence__top">
                <text>置信度</text>
                <text>{{ formatPercent(localPrediction.Confidence) || "-" }}</text>
              </view>
              <view class="laryngoscope-confidence__track">
                <view class="laryngoscope-confidence__bar" :style="{ width: confidenceWidth }"></view>
              </view>
            </view>

            <view v-if="localPrediction.HeatmapUrl" class="laryngoscope-result-toggle" @click="localExpanded = !localExpanded">
              <text>{{ localExpanded ? "收起热力图" : "展开查看热力图" }}</text>
              <text>{{ localExpanded ? "↑" : "↓" }}</text>
            </view>

            <view v-if="localExpanded && localPrediction.HeatmapUrl" class="laryngoscope-heatmap" @click="previewHeatmap">
              <view class="hc-section-head">
                <text class="hc-section-head__title">中间热力图</text>
                <text class="hc-section-head__meta">点击查看大图</text>
              </view>
              <image class="laryngoscope-heatmap__image" :src="localPrediction.HeatmapUrl" mode="widthFix" />
            </view>
          </view>

          <view v-else-if="localPrediction && localPrediction.Status === 'FAILED'" class="laryngoscope-result-empty is-error">
            <text class="laryngoscope-result-empty__title">本地模型预测失败</text>
            <text class="laryngoscope-result-empty__desc">{{ localPrediction.ErrorMessage || "请检查模型文件、Python 环境和图片文件是否可读取。" }}</text>
          </view>

          <view v-else class="laryngoscope-result-empty">
            <text class="laryngoscope-result-empty__title">还没有本地预测结果</text>
            <text class="laryngoscope-result-empty__desc">将使用本地 RepAlexNet 权重生成分类结果和 Grad-CAM 热力图。</text>
          </view>

          <view class="laryngoscope-section__actions">
            <view
              class="hc-pill-button-dark hc-interactive-pill"
              :class="{ 'is-disabled': localLoading }"
              @click="runLocalPredict"
            >
              {{ localLoading ? "预测中..." : localActionText }}
            </view>
          </view>
        </view>

        <view class="hc-card-soft laryngoscope-section hc-reveal-up" style="--delay: 170ms">
          <view class="hc-section-head">
            <text class="hc-section-head__title">Qwen-VL 图像分析</text>
            <text class="hc-section-head__meta">{{ qwenStatusText }}</text>
          </view>

          <view class="laryngoscope-qwen-option">
            <view>
              <text class="laryngoscope-qwen-option__title">带入本地模型预测结果</text>
              <text class="laryngoscope-qwen-option__desc">由用户手动选择，仅作为 Qwen 分析参考，不作为诊断结论。</text>
            </view>
            <switch :checked="includeLocalPredictionContext" color="#8ec94f" @change="onToggleLocalContext" />
          </view>

          <view v-if="qwenAnalysis && qwenAnalysis.Status === 'SUCCESS'" class="laryngoscope-qwen-result">
            <view class="laryngoscope-qwen-result__summary" :class="riskClassName">
              <text class="laryngoscope-qwen-result__risk">{{ riskText }}</text>
              <text class="laryngoscope-qwen-result__summary-text">{{ plainSummary }}</text>
            </view>

            <view class="laryngoscope-result-toggle" @click="qwenExpanded = !qwenExpanded">
              <text>{{ qwenExpanded ? "收起分析详情" : "展开查看建议" }}</text>
              <text>{{ qwenExpanded ? "↑" : "↓" }}</text>
            </view>

            <view v-if="qwenExpanded && qwenSections.length" class="laryngoscope-qwen-sections">
              <view v-for="section in qwenSections" :key="section.key" class="laryngoscope-qwen-sections__block">
                <text class="laryngoscope-qwen-sections__title">{{ section.title }}</text>
                <view class="laryngoscope-qwen-sections__list">
                  <text v-for="(item, index) in section.items" :key="`${section.key}-${index}`">
                    {{ index + 1 }}. {{ item }}
                  </text>
                </view>
              </view>
            </view>

            <text v-else-if="qwenExpanded" class="laryngoscope-qwen-result__raw">
              {{ qwenAnalysis.ReportJson || qwenAnalysis.SummaryText || "Qwen 已完成分析，但未返回结构化明细。" }}
            </text>
          </view>

          <view v-else-if="qwenAnalysis && qwenAnalysis.Status === 'FAILED'" class="laryngoscope-result-empty is-error">
            <text class="laryngoscope-result-empty__title">Qwen-VL 分析失败</text>
            <text class="laryngoscope-result-empty__desc">{{ qwenAnalysis.ErrorMessage || "请稍后重试，或检查 Qwen API 配置。" }}</text>
          </view>

          <view v-else class="laryngoscope-result-empty">
            <text class="laryngoscope-result-empty__title">还没有 Qwen-VL 分析结果</text>
            <text class="laryngoscope-result-empty__desc">将基于喉镜图像和可选的本地模型结果生成风险提示、观察点和就医建议。</text>
          </view>

          <view class="laryngoscope-section__actions">
            <view
              class="hc-pill-button-dark hc-interactive-pill"
              :class="{ 'is-disabled': qwenLoading }"
              @click="runQwenAnalyze"
            >
              {{ qwenLoading ? "分析中..." : qwenActionText }}
            </view>
          </view>
        </view>

        <view class="hc-card-lime laryngoscope-disclaimer hc-reveal-up" style="--delay: 220ms">
          <text class="laryngoscope-disclaimer__title">结果使用提醒</text>
          <text class="laryngoscope-disclaimer__desc">
            本地模型预测和 Qwen-VL 分析都只作为健康管理参考，不能替代耳鼻喉科医生面诊、喉镜复核或病理检查。若影像异常、症状持续或出现吞咽困难、声音嘶哑加重、出血等情况，请及时线下就医。
          </text>
        </view>
      </template>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onPullDownRefresh } from "@dcloudio/uni-app";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import { Post } from "@/utils/http";

const photoId = ref(0);
const photo = ref(null);
const loading = ref(false);
const localLoading = ref(false);
const qwenLoading = ref(false);
const errorMsg = ref("");
const localPrediction = ref(null);
const qwenAnalysis = ref(null);
const includeLocalPredictionContext = ref(false);
const localExpanded = ref(false);
const qwenExpanded = ref(false);
const dialogRef = ref(null);

const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false });

const photoTitle = computed(() => {
  const desc = photo.value?.PhotoDesc || "";
  if (desc) return String(desc).split("|")[0].trim();
  const date = extractDatePart(photo.value?.CheckTime);
  return date ? `${date} 喉镜检查` : "喉镜检查记录";
});

const photoDescText = computed(() => photo.value?.PhotoDesc || "这张影像将作为本地模型预测和 Qwen-VL 分析的输入。");

const localStatusText = computed(() => {
  if (localLoading.value) return "预测中";
  if (localPrediction.value?.Status === "SUCCESS") return "已完成";
  if (localPrediction.value?.Status === "FAILED") return "预测失败";
  return "未预测";
});

const localHeadline = computed(() => {
  if (localPrediction.value?.Status === "SUCCESS") return localPrediction.value.PredictedLabel || "预测已完成";
  if (localPrediction.value?.Status === "FAILED") return "本地模型预测失败";
  return "可生成分类结果与热力图";
});

const localHint = computed(() => {
  if (localPrediction.value?.Status === "SUCCESS") {
    const percent = formatPercent(localPrediction.value.Confidence);
    return percent ? `本地模型置信度 ${percent}，可继续查看热力图定位。` : "本地模型已返回结果，可继续查看热力图定位。";
  }
  if (localPrediction.value?.Status === "FAILED") return "可在确认模型环境后重新预测。";
  return "使用本地 RepAlexNet 权重，不调用云端 AI。";
});

const qwenStatusText = computed(() => {
  if (qwenLoading.value) return "分析中";
  if (qwenAnalysis.value?.Status === "SUCCESS") return "已完成";
  if (qwenAnalysis.value?.Status === "FAILED") return "分析失败";
  return "未分析";
});

const qwenHeadline = computed(() => {
  if (qwenAnalysis.value?.Status === "SUCCESS") return `${riskText.value}提示`;
  if (qwenAnalysis.value?.Status === "FAILED") return "Qwen-VL 分析失败";
  return "可生成影像观察和建议";
});

const qwenHint = computed(() => {
  if (qwenAnalysis.value?.Status === "SUCCESS") return plainSummary.value;
  if (qwenAnalysis.value?.Status === "FAILED") return "可稍后重试，或检查 Qwen API 配置。";
  return "可选择是否带入本地模型预测结果作为参考。";
});

const localActionText = computed(() => (localPrediction.value?.PredictionId ? "重新预测" : "本地模型预测"));
const qwenActionText = computed(() => (qwenAnalysis.value?.AnalysisId ? "重新分析" : "Qwen-VL 分析"));

const confidenceWidth = computed(() => {
  const percent = Number(localPrediction.value?.Confidence) * 100;
  if (!Number.isFinite(percent)) return "0%";
  return `${Math.max(2, Math.min(100, percent))}%`;
});

const qwenParsed = computed(() => parseJsonSafe(qwenAnalysis.value?.ReportJson));

const plainSummary = computed(() => {
  return (
    qwenParsed.value?.plain_summary ||
    qwenParsed.value?.plainSummary ||
    qwenAnalysis.value?.SummaryText ||
    "Qwen-VL 已完成分析，请结合线下检查和医生意见理解。"
  );
});

const riskText = computed(() => {
  const level = qwenAnalysis.value?.RiskLevel || qwenParsed.value?.risk_level || "待判断";
  if (level === "低") return "低风险";
  if (level === "中") return "中风险";
  if (level === "高") return "高风险";
  if (level === "无法判断") return "无法判断";
  return level;
});

const riskClassName = computed(() => {
  const level = qwenAnalysis.value?.RiskLevel || qwenParsed.value?.risk_level;
  if (level === "高") return "is-high";
  if (level === "中") return "is-medium";
  if (level === "无法判断") return "is-unknown";
  return "is-low";
});

const qwenSections = computed(() => {
  const source = qwenParsed.value || {};
  const groups = [
    { key: "image_observations", title: "影像观察", items: normalizeList(source.image_observations) },
    { key: "possible_explanations", title: "可能解释", items: normalizeList(source.possible_explanations) },
    { key: "suggestions", title: "下一步建议", items: normalizeList(source.suggestions) },
    { key: "when_to_see_doctor", title: "何时就医", items: normalizeList(source.when_to_see_doctor) },
    { key: "retest_tips", title: "复查和重拍提示", items: normalizeList(source.retest_tips) },
  ];
  return groups.filter((item) => item.items.length);
});

const formatTime = (val) => (val ? String(val).replace("T", " ") : "");
const extractDatePart = (val) => (formatTime(val) ? formatTime(val).split(" ")[0] : "");

function formatPercent(value) {
  const num = Number(value);
  if (!Number.isFinite(num)) return "";
  return `${Math.round(num * 100)}%`;
}

function parseJsonSafe(text) {
  if (!text) return null;
  if (typeof text === "object") return text;
  try {
    return JSON.parse(text);
  } catch (error) {
    return null;
  }
}

function normalizeList(value) {
  if (Array.isArray(value)) return value.map((item) => String(item || "").trim()).filter(Boolean);
  if (value) return [String(value).trim()].filter(Boolean);
  return [];
}

async function loadDetail() {
  if (!photoId.value) {
    errorMsg.value = "缺少喉镜照片 Id";
    return;
  }
  loading.value = true;
  errorMsg.value = "";
  try {
    const response = await Post("/Front/Laryngoscope/Get", {
      Id: photoId.value,
      Page: 1,
      Limit: 1,
    });
    if (!response.Success) {
      throw new Error(response.Msg || "读取失败");
    }
    photo.value = response.Data || null;
    localPrediction.value = response.Data?.LatestLocalPrediction || null;
    qwenAnalysis.value = response.Data?.LatestQwenAnalysis || null;
    includeLocalPredictionContext.value = !!response.Data?.LatestQwenAnalysis?.IncludeLocalPredictionContext;
    localExpanded.value = false;
    qwenExpanded.value = false;
  } catch (error) {
    errorMsg.value = error?.message || "读取失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}

async function runLocalPredict() {
  if (localLoading.value || !photoId.value) return;
  const confirmed = await showDialog({
    title: localPrediction.value?.PredictionId ? "重新预测" : "本地模型预测",
    content: "将使用本地 RepAlexNet 模型对当前喉镜图片进行预测，并生成中间热力图。",
    confirmText: "开始",
  });
  if (!confirmed.confirm) return;

  localLoading.value = true;
  try {
    const response = await Post("/Front/Laryngoscope/LocalPredict", {
      Id: photoId.value,
      ForceRegenerate: !!localPrediction.value?.PredictionId,
    });
    if (!response.Success) {
      throw new Error(response.Msg || "预测失败");
    }
    localPrediction.value = response.Data || null;
    if (photo.value) photo.value.LatestLocalPrediction = response.Data || null;
    localExpanded.value = false;
    const success = response.Data?.Status === "SUCCESS";
    uni.showToast({ title: success ? "预测完成" : "预测失败", icon: success ? "success" : "none" });
  } catch (error) {
    uni.showToast({ title: error?.message || "预测失败", icon: "none" });
  } finally {
    localLoading.value = false;
  }
}

async function runQwenAnalyze() {
  if (qwenLoading.value || !photoId.value) return;
  const content = includeLocalPredictionContext.value && localPrediction.value?.Status !== "SUCCESS"
    ? "当前还没有成功的本地模型预测结果，Qwen-VL 会主要基于喉镜图片进行分析。"
    : "将调用 Qwen-VL 对当前喉镜图片进行分析，输出仅作为健康建议参考。";
  const confirmed = await showDialog({
    title: qwenAnalysis.value?.AnalysisId ? "重新分析" : "Qwen-VL 分析",
    content,
    confirmText: "开始",
  });
  if (!confirmed.confirm) return;

  qwenLoading.value = true;
  try {
    const response = await Post("/Front/Laryngoscope/QwenAnalyze", {
      Id: photoId.value,
      ForceRegenerate: !!qwenAnalysis.value?.AnalysisId,
      IncludeLocalPredictionContext: includeLocalPredictionContext.value,
    });
    if (!response.Success) {
      throw new Error(response.Msg || "分析失败");
    }
    qwenAnalysis.value = response.Data || null;
    if (photo.value) photo.value.LatestQwenAnalysis = response.Data || null;
    qwenExpanded.value = false;
    const success = response.Data?.Status === "SUCCESS";
    uni.showToast({ title: success ? "分析完成" : "分析失败", icon: success ? "success" : "none" });
  } catch (error) {
    uni.showToast({ title: error?.message || "分析失败", icon: "none" });
  } finally {
    qwenLoading.value = false;
  }
}

function onToggleLocalContext(event) {
  includeLocalPredictionContext.value = !!event?.detail?.value;
}

function previewPhoto() {
  const url = photo.value?.LaryngoscopePhotoUrl;
  if (!url) return;
  uni.previewImage({ urls: [url], current: url });
}

function previewHeatmap() {
  const url = localPrediction.value?.HeatmapUrl;
  if (!url) return;
  uni.previewImage({ urls: [url], current: url });
}

function editPhoto() {
  if (!photoId.value) return;
  const detectId = photo.value?.DetectId || "";
  uni.navigateTo({ url: `/pages/Front/LaryngoscopePhotoEdit?id=${photoId.value}&detectId=${detectId}` });
}

function goBack() {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/LaryngoscopePhotoList" });
}

onLoad(async (options) => {
  photoId.value = Number(options?.id || options?.Id || 0);
  await loadDetail();
});

onPullDownRefresh(async () => {
  await loadDetail();
  uni.stopPullDownRefresh();
});
</script>

<style scoped lang="scss">
.laryngoscope-detail-screen {
  gap: 24upx;
}

.laryngoscope-detail-state,
.laryngoscope-detail-error,
.laryngoscope-section {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.laryngoscope-detail-state__title,
.laryngoscope-detail-error__title,
.laryngoscope-result-empty__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-detail-state__desc,
.laryngoscope-detail-error__desc,
.laryngoscope-result-empty__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.laryngoscope-detail-error,
.laryngoscope-result-empty.is-error {
  border-color: rgba(228, 108, 73, 0.34);
  background: rgba(255, 243, 238, 0.92);
}

.laryngoscope-photo-card {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.laryngoscope-photo-card__title {
  font-size: 44upx;
  line-height: 1.16;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-photo-card__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.laryngoscope-photo-card__image-wrap {
  width: 100%;
  height: 420upx;
  overflow: hidden;
  border-radius: 30upx;
  border: 1upx solid rgba(183, 207, 108, 0.8);
  background: rgba(248, 252, 239, 0.88);
}

.laryngoscope-photo-card__image {
  width: 100%;
  height: 100%;
}

.laryngoscope-photo-card__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.laryngoscope-photo-card__meta-item {
  padding: 16upx 18upx;
  border-radius: 24upx;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(205, 224, 145, 0.82);
}

.laryngoscope-photo-card__meta-item text {
  display: block;
  font-size: 20upx;
  color: #7c8e7b;
}

.laryngoscope-photo-card__meta-item text + text {
  margin-top: 8upx;
  font-size: 23upx;
  line-height: 1.45;
  font-weight: 800;
  color: #172119;
  word-break: break-word;
}

.laryngoscope-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14upx;
}

.laryngoscope-summary-card {
  min-height: 210upx;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.laryngoscope-summary-card__title {
  font-size: 34upx;
  line-height: 1.18;
  font-weight: 800;
  color: #f7ffdf;
}

.laryngoscope-summary-card__desc {
  font-size: 22upx;
  line-height: 1.6;
  color: rgba(241, 248, 223, 0.72);
}

.laryngoscope-summary-card__title--dark {
  color: #172119;
}

.laryngoscope-summary-card__desc--dark {
  color: #556556;
}

.laryngoscope-local-result,
.laryngoscope-qwen-result,
.laryngoscope-result-empty {
  padding: 18upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(205, 224, 145, 0.82);
}

.laryngoscope-result-empty {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.laryngoscope-local-result__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16upx;
}

.laryngoscope-local-result__label {
  display: block;
  font-size: 34upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-local-result__meta {
  display: block;
  margin-top: 8upx;
  font-size: 21upx;
  line-height: 1.55;
  color: #556556;
}

.laryngoscope-confidence {
  margin-top: 18upx;
  padding: 16upx 18upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.9);
  border: 1upx solid rgba(205, 224, 145, 0.82);
}

.laryngoscope-confidence__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.laryngoscope-confidence__top text {
  font-size: 22upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-confidence__track {
  height: 14upx;
  margin-top: 12upx;
  border-radius: 999upx;
  overflow: hidden;
  background: rgba(177, 197, 136, 0.34);
}

.laryngoscope-confidence__bar {
  height: 100%;
  border-radius: 999upx;
  background: linear-gradient(90deg, #8ec94f 0%, #d7ef8f 100%);
}

.laryngoscope-heatmap {
  margin-top: 20upx;
}

.laryngoscope-heatmap__image {
  width: 68%;
  max-width: 420upx;
  display: block;
  margin-top: 14upx;
  margin-left: auto;
  margin-right: auto;
  border-radius: 26upx;
  overflow: hidden;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: #dfe7d2;
}

.laryngoscope-qwen-option {
  padding: 18upx;
  border-radius: 28upx;
  background: rgba(241, 248, 223, 0.8);
  border: 1upx solid rgba(205, 224, 145, 0.82);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18upx;
}

.laryngoscope-qwen-option__title {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-qwen-option__desc {
  display: block;
  margin-top: 8upx;
  font-size: 21upx;
  line-height: 1.55;
  color: #556556;
}

.laryngoscope-qwen-result__summary {
  padding: 18upx 20upx;
  border-radius: 26upx;
  background: rgba(142, 201, 79, 0.12);
  border: 1upx solid rgba(142, 201, 79, 0.26);
}

.laryngoscope-qwen-result__summary.is-medium {
  background: rgba(243, 179, 77, 0.14);
  border-color: rgba(243, 179, 77, 0.28);
}

.laryngoscope-qwen-result__summary.is-high {
  background: rgba(228, 108, 73, 0.14);
  border-color: rgba(228, 108, 73, 0.28);
}

.laryngoscope-qwen-result__summary.is-unknown {
  background: rgba(135, 145, 132, 0.14);
  border-color: rgba(135, 145, 132, 0.28);
}

.laryngoscope-qwen-result__risk {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-qwen-result__summary-text,
.laryngoscope-qwen-result__raw {
  display: block;
  margin-top: 10upx;
  font-size: 23upx;
  line-height: 1.7;
  color: #556556;
  word-break: break-word;
  white-space: pre-wrap;
}

.laryngoscope-result-toggle {
  margin-top: 16upx;
  min-height: 64upx;
  padding: 0 20upx;
  border-radius: 999upx;
  background: rgba(241, 248, 223, 0.92);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.laryngoscope-result-toggle text {
  font-size: 22upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-qwen-sections__block {
  margin-top: 22upx;
}

.laryngoscope-qwen-sections__title {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-qwen-sections__list {
  margin-top: 12upx;
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.laryngoscope-qwen-sections__list text {
  font-size: 22upx;
  line-height: 1.7;
  color: #556556;
}

.laryngoscope-section__actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.is-disabled {
  opacity: 0.62;
}

.laryngoscope-disclaimer {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.laryngoscope-disclaimer__title {
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-disclaimer__desc {
  font-size: 22upx;
  line-height: 1.7;
  color: #556556;
}
</style>
