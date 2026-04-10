<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack ai-report-screen">
      <hc-topbar
        title="AI 智能解读"
        subtitle="围绕单次录音生成更可读的结果说明"
        :show-back="true"
        @left="goBack"
      />

      <view class="bg-head ai-report-head hc-reveal-up">
        <view class="hc-kicker">结果页</view>
        <text class="bg-head__title">把一次音频检测转成更容易执行的建议</text>
        <text class="bg-head__subtitle">
          先看结论与风险，再查看图谱、结构化解释和下一步动作。
        </text>
      </view>

      <view v-if="loading && !report && !errorMsg" class="hc-card-soft ai-report-state hc-reveal-up" style="--delay: 100ms">
        <text class="ai-report-state__title">正在生成 AI 解读</text>
        <text class="ai-report-state__desc">系统会结合当前频谱类型整理风险、解释和后续建议。</text>
      </view>

      <view v-else-if="errorMsg" class="hc-card-soft ai-report-error hc-reveal-up" style="--delay: 100ms">
        <text class="ai-report-error__title">当前无法完成解读</text>
        <text class="ai-report-error__desc">{{ errorMsg }}</text>
        <view class="ai-report-error__actions">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="reload(true)">重新生成</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="goChat">继续咨询</view>
        </view>
      </view>

      <template v-else-if="report">
        <view class="hc-card-dark ai-report-summary hc-reveal-up hc-shine" style="--delay: 120ms">
          <view class="hc-kicker hc-kicker--dark">风险结论</view>
          <view class="ai-report-summary__head">
            <view class="ai-report-summary__main">
              <text class="ai-report-summary__title">{{ reportHeadline }}</text>
              <text class="ai-report-summary__desc">{{ reportHint }}</text>
            </view>
            <view class="ai-report-summary__badge" :class="riskClassName">
              <text class="ai-report-summary__badge-label">风险等级</text>
              <text class="ai-report-summary__badge-value">{{ riskText }}</text>
            </view>
          </view>
          <view class="ai-report-summary__meta">
            <view class="ai-report-summary__meta-item">
              <text class="ai-report-summary__meta-label">模型</text>
              <text class="ai-report-summary__meta-value">{{ report.ModelName || "-" }}</text>
            </view>
            <view class="ai-report-summary__meta-item">
              <text class="ai-report-summary__meta-label">结果时间</text>
              <text class="ai-report-summary__meta-value">{{ report.CreatedTime || recordTimeText }}</text>
            </view>
            <view class="ai-report-summary__meta-item">
              <text class="ai-report-summary__meta-label">检测状态</text>
              <text class="ai-report-summary__meta-value">{{ recordStatusText }}</text>
            </view>
          </view>
        </view>

        <view class="hc-card-soft ai-report-spectrum hc-reveal-up" style="--delay: 180ms">
          <view class="hc-section-head">
            <text class="hc-section-head__title">图谱与来源</text>
            <text class="hc-section-head__meta">{{ spectrumType === "MFCC" ? "MFCC 解读" : "Mel 解读" }}</text>
          </view>

          <view class="hc-token-tabs ai-report-tabs">
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

          <view v-if="spectrumUrl" class="ai-report-spectrum__preview" @click="previewSpectrum">
            <image class="ai-report-spectrum__image" :src="spectrumUrl" mode="widthFix" />
          </view>
          <view v-else class="ai-report-spectrum__empty">
            当前记录没有可预览的图谱，仍可先阅读结构化结果。
          </view>
        </view>

        <view class="hc-card-soft ai-report-plain hc-reveal-up" style="--delay: 240ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">一句话总结</text>
            <text class="bg-section-head__meta">先读这段，再展开细节</text>
          </view>
          <text class="ai-report-plain__text">{{ plainSummary }}</text>
        </view>

        <view class="hc-card-soft ai-report-sections hc-reveal-up" style="--delay: 300ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">结构化解释</text>
            <text class="bg-section-head__meta">把观察、解释和建议拆开阅读</text>
          </view>

          <template v-if="sectionGroups.length">
            <view
              v-for="section in sectionGroups"
              :key="section.key"
              class="ai-report-sections__block"
            >
              <text class="ai-report-sections__title">{{ section.title }}</text>
              <view class="ai-report-sections__list">
                <text v-for="(item, index) in section.items" :key="`${section.key}-${index}`">
                  {{ index + 1 }}. {{ item }}
                </text>
              </view>
            </view>
          </template>

          <view v-else class="ai-report-sections__raw">
            <text class="ai-report-sections__title">AI 输出原文</text>
            <text class="ai-report-sections__raw-text">{{ report.ReportJson || report.SummaryText || "-" }}</text>
          </view>
        </view>

        <view class="ai-report-actions hc-reveal-up" style="--delay: 360ms">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="reload(true)">重新生成</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="goChat">继续咨询</view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const recordId = ref(null);
const spectrumType = ref("MEL");
const loading = ref(false);
const errorMsg = ref("");
const report = ref(null);
const parsed = ref(null);
const record = ref(null);

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
  if (level === "MEDIUM") return "这次结果建议持续跟进";
  if (level === "LOW") return "这次结果整体偏稳";
  return "这次结果已整理完成";
});

const reportHint = computed(() => {
  const level = report.value?.RiskLevel;
  if (level === "HIGH") return "建议结合近期不适和线下就诊安排，尽快完成后续判断。";
  if (level === "MEDIUM") return "更适合继续观察变化，别只看一次结果就结束跟踪。";
  if (level === "LOW") return "保持轻量复测和护理节奏，更容易判断后续是否稳定。";
  return "可继续切换图谱类型或回到咨询页继续追问。";
});

const recordStatusText = computed(() => {
  if (!record.value) return "未读取";
  if (!record.value.DetectTotalStatus) return "检测中";
  return record.value.PrimaryScreenResult ? "筛查提示异常" : "筛查提示正常";
});

const recordTimeText = computed(() => {
  if (!record.value?.CreationTime) return "-";
  return String(record.value.CreationTime).replace("T", " ");
});

const spectrumUrl = computed(() => {
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
  return summary || "当前没有返回可读摘要，可先查看下方原始输出。";
});

const sectionGroups = computed(() => {
  const source = parsed.value || {};
  const groups = [
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

const load = async (forceRegenerate = false) => {
  if (!recordId.value) {
    errorMsg.value = "缺少检测记录 Id";
    return;
  }

  loading.value = true;
  errorMsg.value = "";
  report.value = null;
  parsed.value = null;

  try {
    const response = await Post("/FrontAI/AnalyzeAudioRecord", {
      RecordId: Number(recordId.value),
      ForceRegenerate: !!forceRegenerate,
      SpectrumType: spectrumType.value,
    });
    if (!response.Success) {
      errorMsg.value = response.Msg || "AI 解读失败";
      return;
    }
    report.value = response.Data || null;
    parsed.value = parseJsonSafe(response.Data?.ReportJson);
  } catch (error) {
    errorMsg.value = "AI 解读失败，请稍后重试";
  } finally {
    loading.value = false;
  }
};

const reload = async (force) => {
  await load(!!force);
};

const switchType = async (type) => {
  if (spectrumType.value === type) return;
  spectrumType.value = type;
  await load(false);
};

const goChat = () => {
  if (!recordId.value) return;
  uni.navigateTo({
    url: `/pages/Front/AudioAIChat?recordId=${recordId.value}`,
  });
};

const previewSpectrum = () => {
  if (!spectrumUrl.value) return;
  uni.previewImage({
    urls: [spectrumUrl.value],
    current: spectrumUrl.value,
  });
};

const goBack = () => uni.navigateBack({ delta: 1 });

onLoad(async (options) => {
  recordId.value = options?.recordId || options?.id || null;
  spectrumType.value = options?.spectrumType || "MEL";
  await loadRecord();
  await load(false);
});
</script>

<style scoped lang="scss">
.ai-report-screen {
  gap: 24upx;
}

.ai-report-head {
  gap: 10upx;
}

.ai-report-state,
.ai-report-error {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.ai-report-state__title,
.ai-report-error__title {
  font-size: 32upx;
  font-weight: 800;
  color: #1d2a1f;
}

.ai-report-state__desc,
.ai-report-error__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.ai-report-error {
  border-color: rgba(228, 108, 73, 0.34);
  background: rgba(255, 243, 238, 0.92);
}

.ai-report-error__actions,
.ai-report-actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.ai-report-summary {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.ai-report-summary__head {
  display: flex;
  align-items: flex-start;
  gap: 18upx;
}

.ai-report-summary__main {
  flex: 1;
  min-width: 0;
}

.ai-report-summary__title {
  display: block;
  font-size: 46upx;
  line-height: 1.1;
  font-weight: 800;
  color: #f7ffdf;
}

.ai-report-summary__desc {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.72);
}

.ai-report-summary__badge {
  min-width: 164upx;
  padding: 18upx 20upx;
  border-radius: 30upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
  text-align: right;
}

.ai-report-summary__badge.is-medium {
  background: rgba(243, 179, 77, 0.16);
  border-color: rgba(243, 179, 77, 0.22);
}

.ai-report-summary__badge.is-high {
  background: rgba(228, 108, 73, 0.16);
  border-color: rgba(228, 108, 73, 0.22);
}

.ai-report-summary__badge-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.ai-report-summary__badge-value {
  display: block;
  margin-top: 8upx;
  font-size: 30upx;
  font-weight: 800;
  color: #f7ffdf;
}

.ai-report-summary__meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12upx;
}

.ai-report-summary__meta-item {
  padding: 16upx 18upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.ai-report-summary__meta-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.ai-report-summary__meta-value {
  display: block;
  margin-top: 8upx;
  font-size: 24upx;
  line-height: 1.45;
  color: #f7ffdf;
}

.ai-report-tabs {
  margin-top: 18upx;
}

.ai-report-spectrum__preview {
  margin-top: 18upx;
  border-radius: 30upx;
  overflow: hidden;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: rgba(248, 252, 239, 0.9);
}

.ai-report-spectrum__image {
  width: 100%;
  display: block;
}

.ai-report-spectrum__empty {
  margin-top: 18upx;
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.8);
  border: 1upx solid rgba(201, 220, 145, 0.8);
  color: #556556;
  font-size: 22upx;
  line-height: 1.6;
}

.ai-report-plain__text {
  display: block;
  margin-top: 18upx;
  font-size: 24upx;
  line-height: 1.7;
  color: #243123;
}

.ai-report-sections__block + .ai-report-sections__block {
  margin-top: 22upx;
}

.ai-report-sections__title {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #1d2a1f;
}

.ai-report-sections__list {
  margin-top: 12upx;
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.ai-report-sections__list text,
.ai-report-sections__raw-text {
  font-size: 22upx;
  line-height: 1.7;
  color: #556556;
}

.ai-report-sections__raw {
  margin-top: 18upx;
}

.ai-report-sections__raw-text {
  display: block;
  margin-top: 12upx;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
