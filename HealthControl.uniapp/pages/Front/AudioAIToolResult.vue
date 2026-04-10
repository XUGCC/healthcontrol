<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack ai-tool-result-screen">
      <hc-topbar
        title="AI 工具解读"
        subtitle="录音与图谱统一结果视图"
        :show-back="true"
        @left="goBack"
      />

      <view class="bg-head ai-tool-result-head hc-reveal-up">
        <view class="hc-kicker">{{ mode === "upload" ? "上传结果" : "录音结果" }}</view>
        <text class="bg-head__title">{{ pageHeadline }}</text>
        <text class="bg-head__subtitle">{{ pageSubtitle }}</text>
      </view>

      <view v-if="loading && !report && !errorMsg" class="hc-card-soft ai-tool-result-state hc-reveal-up" style="--delay: 100ms">
        <text class="ai-tool-result-state__title">{{ loadingText }}</text>
        <text class="ai-tool-result-state__desc">系统会先整理摘要，再生成可继续行动的解释卡片。</text>
      </view>

      <view v-else-if="errorMsg" class="hc-card-soft ai-tool-result-error hc-reveal-up" style="--delay: 100ms">
        <text class="ai-tool-result-error__title">当前无法生成解读</text>
        <text class="ai-tool-result-error__desc">{{ errorMsg }}</text>
        <view class="ai-tool-result-actions">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="reload">重新生成 / 重试</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="goChat">回到健康咨询</view>
        </view>
      </view>

      <template v-else-if="report">
        <view class="hc-card-dark ai-tool-result-summary hc-reveal-up hc-shine" style="--delay: 120ms">
          <view class="hc-kicker hc-kicker--dark">{{ mode === "upload" ? "图谱结论" : "录音结论" }}</view>
          <view class="ai-tool-result-summary__head">
            <view class="ai-tool-result-summary__main">
              <text class="ai-tool-result-summary__title">{{ reportHeadline }}</text>
              <text class="ai-tool-result-summary__desc">{{ reportHint }}</text>
            </view>
            <view class="ai-tool-result-summary__badge" :class="riskClassName">
              <text class="ai-tool-result-summary__badge-label">风险等级</text>
              <text class="ai-tool-result-summary__badge-value">{{ riskText }}</text>
            </view>
          </view>
          <view class="ai-tool-result-summary__meta">
            <view class="ai-tool-result-summary__meta-item">
              <text class="ai-tool-result-summary__meta-label">解读来源</text>
              <text class="ai-tool-result-summary__meta-value">{{ modeLabel }}</text>
            </view>
            <view class="ai-tool-result-summary__meta-item">
              <text class="ai-tool-result-summary__meta-label">图谱类型</text>
              <text class="ai-tool-result-summary__meta-value">{{ spectrumType }}</text>
            </view>
            <view class="ai-tool-result-summary__meta-item">
              <text class="ai-tool-result-summary__meta-label">模型 / 时间</text>
              <text class="ai-tool-result-summary__meta-value">{{ modelAndTime }}</text>
            </view>
          </view>
        </view>

        <view class="hc-card-soft ai-tool-result-spectrum hc-reveal-up" style="--delay: 180ms">
          <view class="hc-section-head">
            <text class="hc-section-head__title">图谱与来源</text>
            <text class="hc-section-head__meta">{{ mode === "upload" ? "上传图片预览" : "切换图谱类型" }}</text>
          </view>

          <view v-if="mode === 'record'" class="hc-token-tabs ai-tool-result-tabs">
            <view
              class="hc-token-tabs__item"
              :class="{ active: spectrumType === 'MEL' }"
              @click="switchType('MEL')"
            >
              Mel 解读
            </view>
            <view
              class="hc-token-tabs__item"
              :class="{ active: spectrumType === 'MFCC' }"
              @click="switchType('MFCC')"
            >
              MFCC 解读
            </view>
          </view>

          <view v-if="spectrumUrl" class="ai-tool-result-spectrum__preview" @click="previewSpectrum">
            <image class="ai-tool-result-spectrum__image" :src="spectrumUrl" mode="widthFix" />
          </view>
          <view v-else class="ai-tool-result-spectrum__empty">
            当前没有可预览图谱，可直接阅读下方结构化解释。
          </view>
        </view>

        <view class="hc-card-soft ai-tool-result-plain hc-reveal-up" style="--delay: 240ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">一句话总结</text>
            <text class="bg-section-head__meta">建议先看这里</text>
          </view>
          <text class="ai-tool-result-plain__text">{{ plainSummary }}</text>
        </view>

        <view class="hc-card-soft ai-tool-result-sections hc-reveal-up" style="--delay: 300ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">结构化解释</text>
            <text class="bg-section-head__meta">先生活化建议，再看专业观察</text>
          </view>

          <template v-if="sectionGroups.length">
            <view
              v-for="section in sectionGroups"
              :key="section.key"
              class="ai-tool-result-sections__block"
            >
              <text class="ai-tool-result-sections__title">{{ section.title }}</text>
              <view class="ai-tool-result-sections__list">
                <text v-for="(item, index) in section.items" :key="`${section.key}-${index}`">
                  {{ index + 1 }}. {{ item }}
                </text>
              </view>
            </view>
          </template>

          <view v-else class="ai-tool-result-sections__raw">
            <text class="ai-tool-result-sections__title">AI 输出原文</text>
            <text class="ai-tool-result-sections__raw-text">{{ report.ReportJson || report.SummaryText || "-" }}</text>
          </view>
        </view>

        <view class="ai-tool-result-actions hc-reveal-up" style="--delay: 360ms">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="reload">重新生成 / 重试</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="goChat">回到健康咨询</view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { API_BASE_URL } from "@/utils/config";
import { GetLoginToken } from "@/utils/cache";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const mode = ref("record");
const recordId = ref(null);
const spectrumType = ref("MEL");
const force = ref(false);
const localPath = ref("");
const loading = ref(false);
const loadingText = ref("正在生成解读，请稍候...");
const errorMsg = ref("");
const report = ref(null);
const parsed = ref(null);
const record = ref(null);

const modeLabel = computed(() => (mode.value === "upload" ? "上传图谱" : "历史录音"));

const pageHeadline = computed(() => {
  if (mode.value === "upload") return "把上传图谱转成更容易判断的结果卡";
  return "把一次录音检测拆成更可读的行动结论";
});

const pageSubtitle = computed(() => {
  if (mode.value === "upload") return "适合先快速看结论，再回到 AI 咨询页继续追问。";
  return "可在 Mel 与 MFCC 之间切换，比较不同图谱下的解释差异。";
});

const riskText = computed(() => {
  const level = report.value?.RiskLevel;
  if (level === "LOW") return "低风险";
  if (level === "MEDIUM") return "中风险";
  if (level === "HIGH") return "高风险";
  return "待判断";
});

const riskClassName = computed(() => {
  const level = report.value?.RiskLevel;
  if (level === "MEDIUM") return "is-medium";
  if (level === "HIGH") return "is-high";
  return "is-low";
});

const reportHeadline = computed(() => {
  const level = report.value?.RiskLevel;
  if (level === "HIGH") return "这次结果需要优先关注";
  if (level === "MEDIUM") return "这次结果建议继续跟进";
  if (level === "LOW") return "这次结果整体偏稳";
  return "AI 已完成解读";
});

const reportHint = computed(() => {
  const level = report.value?.RiskLevel;
  if (level === "HIGH") return "更适合优先安排后续判断，必要时尽快结合线下检查。";
  if (level === "MEDIUM") return "建议继续观察变化，不要只看一次结果后就中断跟踪。";
  if (level === "LOW") return "可以继续保持轻量复测和护理节奏，关注是否持续稳定。";
  return "可继续阅读结构化解释，或回到咨询页进一步追问。";
});

const modelAndTime = computed(() => {
  const model = report.value?.ModelName || "-";
  const time = report.value?.CreatedTime || (record.value?.CreationTime ? String(record.value.CreationTime).replace("T", " ") : "-");
  return `${model} / ${time}`;
});

const spectrumUrl = computed(() => {
  if (mode.value === "upload") return localPath.value || "";
  const currentRecord = record.value || {};
  if (spectrumType.value === "MFCC") return currentRecord.MfccSpectrumUrl || "";
  return currentRecord.MelSpectrumUrl || "";
});

const plainSummary = computed(() => {
  const summary =
    parsed.value?.plain_summary ||
    parsed.value?.plainSummary ||
    report.value?.SummaryText ||
    reportHint.value;
  return summary || "当前没有返回可读摘要，可先查看原始输出。";
});

const sectionGroups = computed(() => {
  const source = parsed.value || {};
  const groups = [
    { key: "at_home_tips", title: "在家先怎么做", items: source.at_home_tips || [] },
    { key: "key_observations", title: "关键观察", items: source.key_observations || [] },
    { key: "possible_explanations", title: "可能解释", items: source.possible_explanations || [] },
    { key: "suggestions", title: "下一步建议", items: source.suggestions || [] },
    { key: "when_to_see_doctor", title: "何时就医", items: source.when_to_see_doctor || [] },
    { key: "retest_tips", title: "重测建议", items: source.retest_tips || [] },
  ];

  return groups.filter((group) => Array.isArray(group.items) && group.items.length);
});

const parseJsonSafe = (text) => {
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch (error) {
    return null;
  }
};

const loadRecord = async () => {
  if (!recordId.value) return;
  try {
    const response = await Post("/TAudioScreenRecord/Get", {
      Id: Number(recordId.value),
      Page: 1,
      Limit: 1,
    });
    if (response.Success) {
      record.value = response.Data || null;
    }
  } catch (error) {
    console.error("Failed to load audio record", error);
  }
};

const analyzeRecordOnce = async () => {
  const response = await Post("/FrontAI/AnalyzeAudioRecord", {
    RecordId: Number(recordId.value),
    ForceRegenerate: !!force.value,
    SpectrumType: spectrumType.value,
  });
  if (!response.Success) {
    throw new Error(response.Msg || "生成失败");
  }
  return response.Data || null;
};

const uploadAndAnalyze = async () => {
  const token = GetLoginToken();
  const header = token ? { Authorization: `Bearer ${token}` } : {};
  const currentSpectrum = (spectrumType.value || "MEL").toUpperCase();

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${API_BASE_URL}/FrontAI/AnalyzeSpectrumUpload?SpectrumType=${currentSpectrum}`,
      filePath: localPath.value,
      name: "file",
      header,
      success: (uploadFileRes) => {
        try {
          const data = JSON.parse(uploadFileRes.data || "{}");
          if (!data.Success) {
            reject(new Error(data.Msg || "生成失败"));
            return;
          }
          resolve(data.Data || null);
        } catch (error) {
          reject(new Error("解析返回失败"));
        }
      },
      fail: () => reject(new Error("上传失败")),
    });
  });
};

const load = async () => {
  loading.value = true;
  errorMsg.value = "";
  report.value = null;
  parsed.value = null;

  try {
    if (mode.value === "record") {
      if (!recordId.value) {
        throw new Error("缺少检测记录 Id");
      }
      loadingText.value = `正在生成 ${spectrumType.value} 解读...`;
      await loadRecord();
      const currentReport = await analyzeRecordOnce();
      report.value = currentReport;
      parsed.value = parseJsonSafe(currentReport?.ReportJson);
    } else {
      if (!localPath.value) {
        throw new Error("缺少待上传图谱");
      }
      loadingText.value = `正在上传并生成 ${spectrumType.value} 解读...`;
      const currentReport = await uploadAndAnalyze();
      report.value = currentReport;
      parsed.value = parseJsonSafe(currentReport?.ReportJson);
    }
  } catch (error) {
    errorMsg.value = error?.message || "生成失败，请稍后重试";
  } finally {
    loading.value = false;
  }
};

const reload = async () => {
  if (mode.value === "record") {
    force.value = true;
  }
  await load();
};

const switchType = async (type) => {
  if (spectrumType.value === type) return;
  spectrumType.value = type;
  await load();
};

const previewSpectrum = () => {
  if (!spectrumUrl.value) return;
  uni.previewImage({ urls: [spectrumUrl.value], current: spectrumUrl.value });
};

const goChat = () => {
  const pages = getCurrentPages();
  if (pages && pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.navigateTo({ url: "/pages/Front/AudioAIChat" });
};

const goBack = () => uni.navigateBack({ delta: 1 });

onLoad(async (options) => {
  mode.value = options?.mode === "upload" ? "upload" : "record";
  if (mode.value === "record") {
    recordId.value = options?.recordId ? Number(options.recordId) : null;
    force.value = String(options?.force || "0") === "1";
    spectrumType.value = options?.spectrumType ? String(options.spectrumType).toUpperCase() : "MEL";
  } else {
    spectrumType.value = (options?.spectrumType || "MEL").toUpperCase();
    localPath.value = options?.localPath ? decodeURIComponent(options.localPath) : "";
  }
  await load();
});
</script>

<style scoped lang="scss">
.ai-tool-result-screen {
  gap: 24upx;
}

.ai-tool-result-head {
  gap: 10upx;
}

.ai-tool-result-state__title,
.ai-tool-result-error__title {
  font-size: 32upx;
  font-weight: 800;
  color: #1d2a1f;
}

.ai-tool-result-state__desc,
.ai-tool-result-error__desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.ai-tool-result-error {
  border-color: rgba(228, 108, 73, 0.34);
  background: rgba(255, 243, 238, 0.92);
}

.ai-tool-result-summary {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.ai-tool-result-summary__head {
  display: flex;
  align-items: flex-start;
  gap: 18upx;
}

.ai-tool-result-summary__main {
  flex: 1;
  min-width: 0;
}

.ai-tool-result-summary__title {
  display: block;
  font-size: 46upx;
  line-height: 1.1;
  font-weight: 800;
  color: #f7ffdf;
}

.ai-tool-result-summary__desc {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.72);
}

.ai-tool-result-summary__badge {
  min-width: 164upx;
  padding: 18upx 20upx;
  border-radius: 30upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
  text-align: right;
}

.ai-tool-result-summary__badge.is-medium {
  background: rgba(243, 179, 77, 0.16);
  border-color: rgba(243, 179, 77, 0.22);
}

.ai-tool-result-summary__badge.is-high {
  background: rgba(228, 108, 73, 0.16);
  border-color: rgba(228, 108, 73, 0.22);
}

.ai-tool-result-summary__badge-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.ai-tool-result-summary__badge-value {
  display: block;
  margin-top: 8upx;
  font-size: 30upx;
  font-weight: 800;
  color: #f7ffdf;
}

.ai-tool-result-summary__meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12upx;
}

.ai-tool-result-summary__meta-item {
  padding: 16upx 18upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.ai-tool-result-summary__meta-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.ai-tool-result-summary__meta-value {
  display: block;
  margin-top: 8upx;
  font-size: 24upx;
  line-height: 1.45;
  color: #f7ffdf;
}

.ai-tool-result-tabs {
  margin-top: 18upx;
}

.ai-tool-result-spectrum__preview {
  margin-top: 18upx;
  border-radius: 30upx;
  overflow: hidden;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: rgba(248, 252, 239, 0.9);
}

.ai-tool-result-spectrum__image {
  width: 100%;
  display: block;
}

.ai-tool-result-spectrum__empty {
  margin-top: 18upx;
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.8);
  border: 1upx solid rgba(201, 220, 145, 0.8);
  color: #556556;
  font-size: 22upx;
  line-height: 1.6;
}

.ai-tool-result-plain__text {
  display: block;
  margin-top: 18upx;
  font-size: 24upx;
  line-height: 1.7;
  color: #243123;
}

.ai-tool-result-sections__block + .ai-tool-result-sections__block {
  margin-top: 22upx;
}

.ai-tool-result-sections__title {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #1d2a1f;
}

.ai-tool-result-sections__list {
  margin-top: 12upx;
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.ai-tool-result-sections__list text,
.ai-tool-result-sections__raw-text {
  font-size: 22upx;
  line-height: 1.7;
  color: #556556;
}

.ai-tool-result-sections__raw {
  margin-top: 18upx;
}

.ai-tool-result-sections__raw-text {
  display: block;
  margin-top: 12upx;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-tool-result-actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}
</style>
