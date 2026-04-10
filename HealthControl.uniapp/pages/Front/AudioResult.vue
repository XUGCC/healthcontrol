<template>
  <view class="hc-mobile-shell audio-result-page">
    <view class="hc-screen hc-stack result-screen">
      <view class="bg-head result-head hc-reveal-up">
        <view class="result-head__top">
          <view class="result-head__back hc-interactive-pill" @click="goBack">
            <uni-icons type="left" size="16" color="#132015" />
            <text>返回</text>
          </view>
          <view class="result-head__kicker">{{ pageKickerText }}</view>
        </view>
        <text class="bg-head__title">语音筛查结果概览</text>
        <text class="bg-head__subtitle">统一展示风险判断、关键指标与后续处理入口。</text>
      </view>

      <view v-if="isInitializing && !hasDisplayContent" class="hc-card-soft loading-card hc-reveal-up" style="--delay: 120ms">
        <view class="loading-card__spinner"></view>
        <text class="loading-card__text">正在加载检测结果...</text>
      </view>

      <view v-if="error" class="hc-card-soft error-card hc-reveal-up" style="--delay: 140ms">
        <view class="error-card__icon">!</view>
        <text class="error-card__title">结果加载失败</text>
        <text class="error-card__text">{{ error }}</text>
        <view class="error-card__action hc-pill-button-dark hc-interactive-pill" @click="goToNewDetection">重新检测</view>
      </view>

      <template v-if="hasSuccess">
        <view class="hc-card-lime verdict-card hc-reveal-up" style="--delay: 160ms">
          <view class="verdict-card__icon-wrap">
            <view class="verdict-card__icon" :class="{ danger: !isBenign }">
              <uni-icons :type="isBenign ? 'checkmarkempty' : 'info'" size="22" color="#ffffff" />
            </view>
          </view>
          <view class="verdict-card__main">
            <text class="verdict-card__title">{{ mainResultTitle }}</text>
            <text class="verdict-card__subtitle">{{ mainResultSubtitle }}</text>
            <view class="verdict-card__tag" :class="{ danger: !isBenign }">
              {{ isBenign ? "建议持续管理" : "建议优先复查" }}
            </view>
          </view>
        </view>

        <view class="hc-card-soft key-card hc-reveal-up" style="--delay: 210ms">
          <view class="key-card__head">
            <text class="key-card__title">关键摘要</text>
            <view class="key-card__score">
              <text class="key-card__score-label">检测信息</text>
              <text class="key-card__score-value">{{ confidencePct }}%</text>
            </view>
          </view>

          <text class="key-card__progress-label">检测置信度</text>
          <view class="hc-progress key-card__progress">
            <view class="hc-progress__bar" :style="{ width: `${confidencePct}%` }"></view>
          </view>

          <view class="key-card__grid">
            <view class="key-item">
              <text class="key-item__label">判定规则</text>
              <text class="key-item__value">{{ ruleText }}</text>
            </view>
            <view class="key-item">
              <text class="key-item__label">预测类别</text>
              <text class="key-item__value">{{ predictedClassText || "-" }}</text>
            </view>
            <view class="key-item">
              <text class="key-item__label">语音性别</text>
              <text class="key-item__value">{{ genderDisplayText }}</text>
            </view>
            <view class="key-item">
              <text class="key-item__label">检测时间</text>
              <text class="key-item__value">{{ detectTimeText }}</text>
            </view>
            <view v-if="modelVersionText !== '-'" class="key-item">
              <text class="key-item__label">模型版本</text>
              <text class="key-item__value">{{ modelVersionText }}</text>
            </view>
          </view>
        </view>

        <view v-if="currentDetectId" class="hc-card-soft model-card hc-reveal-up" style="--delay: 260ms">
          <view class="model-card__head">
            <view>
              <text class="model-card__title">模型服务状态</text>
              <text class="model-card__subtitle">按来源拆分日志，默认显示最近 1 条。</text>
            </view>
            <view class="model-card__badge" :class="statusLevelClass">
              {{ statusLevelText }}
            </view>
          </view>

          <view v-if="modelSummaryLoading" class="model-card__loading">
            <text>正在加载模型调用记录...</text>
          </view>

          <view v-else-if="modelSummaryError" class="model-card__error">
            <text class="model-card__error-text">{{ modelSummaryError }}</text>
            <view class="model-card__retry hc-interactive-pill" @click="retryModelSummary">重试</view>
          </view>

          <template v-else>
            <view class="model-overview-grid">
              <view class="model-overview-item">
                <text class="model-overview-item__label">总调用</text>
                <text class="model-overview-item__value">{{ overallStats.total }}</text>
              </view>
              <view class="model-overview-item">
                <text class="model-overview-item__label">成功</text>
                <text class="model-overview-item__value success">{{ overallStats.success }}</text>
              </view>
              <view class="model-overview-item">
                <text class="model-overview-item__label">失败</text>
                <text class="model-overview-item__value danger">{{ overallStats.fail }}</text>
              </view>
              <view class="model-overview-item">
                <text class="model-overview-item__label">平均耗时</text>
                <text class="model-overview-item__value">{{ formatCost(overallStats.avgCost) }}</text>
              </view>
            </view>

            <view class="source-groups">
              <view class="source-group" v-for="section in sourceSections" :key="section.key">
                <view class="source-group__head">
                  <view class="source-group__head-main">
                    <text class="source-group__title">{{ section.title }}</text>
                    <text class="source-group__meta">{{ section.desc }}</text>
                  </view>
                  <view class="source-group__count">{{ section.total }}</view>
                </view>

                <view class="source-group__metrics">
                  <text>总{{ section.total }}</text>
                  <text class="ok">成功{{ section.success }}</text>
                  <text class="fail">失败{{ section.fail }}</text>
                  <text>均耗{{ formatCost(section.avgCost) }}</text>
                </view>

                <view v-if="section.items.length === 0" class="source-group__empty">暂无调用记录</view>

                <view v-else class="source-log-list">
                  <view
                    class="source-log-item"
                    v-for="(item, itemIndex) in visibleSourceItems(section.key)"
                    :key="`${section.key}-${item.CallTime || item._index}-${itemIndex}`"
                  >
                    <view class="source-log-item__head">
                      <text class="source-log-item__name">{{ displayCallLinkName(item) }}</text>
                      <view class="source-log-item__status" :class="{ ok: item.CallStatus === true, fail: item.CallStatus !== true }">
                        {{ item.CallStatus === true ? "成功" : "失败" }}
                      </view>
                    </view>

                    <view class="source-log-item__meta">
                      <text>{{ formatCallTime(item.CallTime) }}</text>
                      <text>{{ formatCost(item.CallCost) }}</text>
                    </view>

                    <text v-if="item.ResultSummary" class="source-log-item__summary">{{ item.ResultSummary }}</text>
                    <text class="source-log-item__service">{{ formatServiceMeta(item) }}</text>
                  </view>
                </view>

                <view
                  v-if="section.items.length > 1"
                  class="source-group__toggle hc-interactive-pill"
                  @click="toggleSourceSection(section.key)"
                >
                  {{ isSourceExpanded(section.key) ? "收起" : `展开 ${section.items.length - 1} 条` }}
                </view>
              </view>
            </view>
          </template>
        </view>

        <view v-if="spectrumList.length > 0" class="hc-card-soft spectrum-card hc-reveal-up" style="--delay: 300ms">
          <view class="hc-section-head">
            <text class="hc-section-head__title">图预览</text>
            <text class="hc-section-head__meta">点击可放大查看</text>
          </view>
          <view class="spectrum-grid">
            <view class="spectrum-item hc-interactive-card" v-for="item in spectrumList" :key="item.key" @click="previewImage(item.url)">
              <text class="spectrum-item__title">{{ item.title }}</text>
              <image class="spectrum-item__img" :src="item.url" mode="aspectFill" />
            </view>
          </view>
        </view>

        <view class="hc-card-soft ai-card hc-reveal-up" style="--delay: 350ms">
          <view class="ai-card__head">
            <view class="ai-card__icon"><uni-icons type="chatboxes" size="18" color="#f4fbdd" /></view>
            <view class="ai-card__meta">
              <text class="ai-card__title">AI 辅助解读</text>
              <text class="ai-card__subtitle">生成报告并支持追问</text>
            </view>
            <view class="ai-card__tag">推荐</view>
          </view>
          <view class="ai-card__actions">
            <view class="ai-btn ai-btn--dark hc-interactive-pill" :class="{ disabled: !hasDetectId }" @click="goToAIReport">AI 报告</view>
            <view class="ai-btn ai-btn--soft hc-interactive-pill" :class="{ disabled: !hasDetectId }" @click="goToAIChat">AI 咨询</view>
          </view>
        </view>

        <view class="quick-grid hc-reveal-up" style="--delay: 390ms">
          <view class="quick-item hc-interactive-card" @click="goToHistory">
            <text class="quick-item__title">历史记录</text>
            <text class="quick-item__desc">查看全部检测数据</text>
          </view>
          <view class="quick-item hc-interactive-card" :class="{ disabled: !hasDetectId }" @click="goToSymptomForm">
            <text class="quick-item__title">记录症状</text>
            <text class="quick-item__desc">关联当前检测填写症状</text>
          </view>
          <view class="quick-item hc-interactive-card" @click="goToMedicalHome">
            <text class="quick-item__title">就医辅助</text>
            <text class="quick-item__desc">查看复查与就诊建议</text>
          </view>
          <view class="quick-item hc-interactive-card" :class="{ disabled: !hasDetectId }" @click="goToLaryngoscopeUpload">
            <text class="quick-item__title">上传喉镜</text>
            <text class="quick-item__desc">补充影像辅助对照</text>
          </view>
        </view>

        <view class="result-bottom-actions hc-reveal-up" style="--delay: 440ms">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="goToNewDetection">再次检测</view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";

const commonStore = useCommonStore();

const pageOptions = ref({});
const rawPayload = ref(null);
const isInitializing = ref(true);

const modelSummary = ref(null);
const modelSummaryLoading = ref(false);
const modelSummaryError = ref("");
const lastLoadedDetectId = ref(null);
const expandedSourceMap = ref({
  LOCAL_MODEL: false,
  QWEN_API: false,
});

const toBoolean = (value, fallback = null) => {
  if (typeof value === "boolean") return value;
  if (typeof value === "number") {
    if (value === 1) return true;
    if (value === 0) return false;
    return fallback;
  }
  if (typeof value === "string") {
    const normalized = value.trim().toLowerCase();
    if (["1", "true", "yes", "y"].includes(normalized)) return true;
    if (["0", "false", "no", "n"].includes(normalized)) return false;
  }
  return fallback;
};

const result = computed(() => rawPayload.value?.data || null);
const errorObj = computed(() => rawPayload.value?.error || null);

const error = computed(() => {
  if (!rawPayload.value) {
    return "检测结果为空";
  }
  if (rawPayload.value.success === false && errorObj.value) {
    const stage = errorObj.value.stage || "";
    const msg = errorObj.value.errorMessage || "";
    return stage ? `[${stage}] ${msg || "检测失败"}` : msg || "检测失败";
  }
  return null;
});

const hasSuccess = computed(() => !!rawPayload.value && rawPayload.value.success === true && !!result.value);
const hasDisplayContent = computed(() => hasSuccess.value || !!error.value);

const pageKickerText = computed(() => {
  if (hasSuccess.value) return "检测结果";
  if (error.value) return "结果异常";
  return "检测记录";
});

const currentDetectId = computed(() => {
  const payloadId = Number(result.value?.Id || result.value?.detectId || 0);
  if (payloadId > 0) return payloadId;
  const routeId = Number(pageOptions.value?.id || 0);
  return routeId > 0 ? routeId : null;
});

const hasDetectId = computed(() => !!currentDetectId.value);

const confidence = computed(() => {
  const r = result.value || {};
  const val =
    typeof r.confidence === "number"
      ? r.confidence
      : typeof r.PrimaryScreenConfidence === "number"
        ? r.PrimaryScreenConfidence
        : typeof r.prob1 === "number"
          ? r.prob1
          : 0;
  if (!Number.isFinite(val)) return 0;
  return Math.max(0, Math.min(1, val));
});

const confidencePct = computed(() => (confidence.value * 100).toFixed(1));

const isBenign = computed(() => {
  const r = result.value || {};
  if (typeof r.benign === "boolean") return r.benign;
  if (typeof r.malignant === "boolean") return !r.malignant;
  if (typeof r.PrimaryScreenResult === "boolean") return !r.PrimaryScreenResult;
  const primaryScreenResult = toBoolean(r.PrimaryScreenResult, null);
  if (primaryScreenResult !== null) return !primaryScreenResult;
  const cls = Number(r.predicted_class);
  if (Number.isFinite(cls)) return cls === 0;
  return false;
});

const mainResultTitle = computed(() => {
  return isBenign.value ? "检测结果：良性（非肿瘤）" : "检测结果：风险提示（疑似肿瘤）";
});

const mainResultSubtitle = computed(() => {
  const r = result.value || {};
  if (r.zhExplanation) return r.zhExplanation;
  if (isBenign.value) {
    return `当前结果偏稳，置信度 ${confidencePct.value}%。建议按周期保持随访，持续记录。`;
  }
  return `当前存在风险提示，置信度 ${confidencePct.value}%。建议优先复查并结合专业检查判断。`;
});

const predictedClassText = computed(() => {
  const r = result.value || {};
  if (typeof r.PrimaryScreenResult === "boolean") {
    return r.PrimaryScreenResult ? "1（恶性 / 肿瘤）" : "0（良性 / 非肿瘤）";
  }
  const cls = Number(r.predicted_class);
  if (!Number.isFinite(cls)) return "";
  if (cls === 0) return "0（良性 / 非肿瘤）";
  if (cls === 1) return "1（恶性 / 肿瘤）";
  return String(cls);
});

const ruleText = computed(() => {
  const r = result.value || {};
  if (r.rule) return r.rule;
  return "0=良性，1=恶性。";
});

const genderText = computed(() => {
  const r = result.value || {};
  if (r.voiceGender) return r.voiceGender;
  if (typeof r.voiceGenderCode === "number") return r.voiceGenderCode === 0 ? "女性" : "男性";
  if (r.VoiceGender) return r.VoiceGender;
  if (typeof r.VoiceGenderCode === "number") return r.VoiceGenderCode === 0 ? "女性" : "男性";
  return "";
});

const genderConfidenceText = computed(() => {
  const r = result.value || {};
  if (typeof r.voiceGenderConfidence === "number") return `${(r.voiceGenderConfidence * 100).toFixed(1)}%`;
  if (typeof r.VoiceGenderConfidence === "number") return `${(r.VoiceGenderConfidence * 100).toFixed(1)}%`;
  return "";
});

const genderDisplayText = computed(() => {
  if (!genderText.value) return "-";
  if (!genderConfidenceText.value) return genderText.value;
  return `${genderText.value}（置信度 ${genderConfidenceText.value}）`;
});

const detectTimeText = computed(() => {
  const r = result.value || {};
  return formatDateTime(r.CreationTime || r.UpdateTime) || formatDateTime(new Date().toISOString());
});

const modelVersionText = computed(() => {
  const r = result.value || {};
  return r.usingModel || r.method || r.DensenetModelVersion || "-";
});

const waveformUrl = computed(() => result.value?.waveformUrl || result.value?.WaveformUrl || "");
const mfccUrl = computed(() => result.value?.mfccUrl || result.value?.MfccSpectrumUrl || "");
const melUrl = computed(() => result.value?.melUrl || result.value?.MelSpectrumUrl || "");

const spectrumList = computed(() => {
  const list = [];
  if (waveformUrl.value) {
    list.push({ key: "waveform", title: "波形图", url: waveformUrl.value });
  }
  if (melUrl.value) {
    list.push({ key: "mel", title: "Mel 图", url: melUrl.value });
  }
  if (mfccUrl.value) {
    list.push({ key: "mfcc", title: "MFCC 图", url: mfccUrl.value });
  }
  return list;
});

const normalizedModelItems = computed(() => {
  const items = Array.isArray(modelSummary.value?.Items) ? modelSummary.value.Items : [];
  return items
    .map((item, index) => {
      return {
        ...item,
        _index: index,
        _sourceType: resolveSourceType(item),
        _callTimeMs: parseCallTimeMs(item?.CallTime),
      };
    })
    .sort((a, b) => {
      const aHasTime = a._callTimeMs !== null;
      const bHasTime = b._callTimeMs !== null;
      if (aHasTime && bHasTime) return b._callTimeMs - a._callTimeMs;
      if (aHasTime) return -1;
      if (bHasTime) return 1;
      return b._index - a._index;
    });
});

const overallStats = computed(() => {
  const items = normalizedModelItems.value;
  const total = safeNumber(modelSummary.value?.TotalCalls, items.length);
  const success = safeNumber(
    modelSummary.value?.SuccessCalls,
    items.filter((item) => item.CallStatus === true).length,
  );
  const fail = safeNumber(modelSummary.value?.FailCalls, Math.max(total - success, 0));
  const avgCost =
    modelSummary.value?.AvgCost !== null && modelSummary.value?.AvgCost !== undefined
      ? safeNumber(modelSummary.value?.AvgCost, 0)
      : averageCost(items);
  return { total, success, fail, avgCost };
});

const sourceSections = computed(() => {
  const localItems = normalizedModelItems.value.filter((item) => item._sourceType === "LOCAL_MODEL");
  const qwenItems = normalizedModelItems.value.filter((item) => item._sourceType === "QWEN_API");
  return [
    buildSourceSection("LOCAL_MODEL", "本地模型调用", "本地推理与预处理链路", localItems),
    buildSourceSection("QWEN_API", "千问 API 调用", "外部大模型服务链路", qwenItems),
  ];
});

const sourceSectionMap = computed(() => {
  const map = {};
  sourceSections.value.forEach((section) => {
    map[section.key] = section;
  });
  return map;
});

const statusLevel = computed(() => {
  const levelRaw = String(modelSummary.value?.StatusLevel || "").toUpperCase();
  if (["NORMAL", "WARNING", "ERROR", "NONE"].includes(levelRaw)) {
    return levelRaw;
  }
  if (overallStats.value.total === 0) return "NONE";
  if (overallStats.value.fail > 0) return "WARNING";
  return "NORMAL";
});

const statusLevelText = computed(() => {
  const level = statusLevel.value;
  if (level === "NORMAL") return "正常";
  if (level === "WARNING") return "轻微波动";
  if (level === "ERROR") return "异常";
  return "暂无调用";
});

const statusLevelClass = computed(() => {
  const level = statusLevel.value.toLowerCase();
  return `is-${level}`;
});

watch(
  () => currentDetectId.value,
  (detectId) => {
    if (!detectId) return;
    if (detectId === lastLoadedDetectId.value) return;
    lastLoadedDetectId.value = detectId;
    void loadModelSummary(detectId);
  },
  { immediate: true },
);

onLoad(async (options) => {
  pageOptions.value = options || {};
  isInitializing.value = true;

  try {
    if (options?.payload) {
      rawPayload.value = parsePayload(options.payload);
      return;
    }

    const routeId = Number(options?.id || 0);
    if (routeId > 0) {
      await loadHistoryPayload(routeId);
      return;
    }

    rawPayload.value = {
      success: false,
      error: {
        stage: "init",
        errorMessage: "检测结果为空，请重新检测。",
      },
    };
  } finally {
    isInitializing.value = false;
  }
});

const parsePayload = (payloadText) => {
  let text = payloadText;
  try {
    text = decodeURIComponent(payloadText);
  } catch (errorDecode) {
    text = payloadText;
  }

  try {
    const parsed = JSON.parse(text);
    return normalizePayload(parsed);
  } catch (errorParse) {
    console.error("解析检测结果参数失败", errorParse, text);
    return {
      success: false,
      error: {
        stage: "parse_payload",
        errorMessage: "检测结果解析失败，请重新检测。",
      },
    };
  }
};

const normalizePayload = (parsed) => {
  if (!parsed || typeof parsed !== "object") {
    return {
      success: false,
      error: {
        stage: "parse_payload",
        errorMessage: "检测结果格式无效。",
      },
    };
  }

  if (typeof parsed.success === "boolean") {
    return parsed;
  }

  if (typeof parsed.Success === "boolean") {
    return {
      success: parsed.Success,
      data: parsed.Data || null,
      error:
        parsed.Success === true
          ? null
          : {
              stage: "payload",
              errorMessage: parsed.Msg || "检测失败",
            },
    };
  }

  if (parsed.data && typeof parsed.data === "object") {
    return {
      success: true,
      data: parsed.data,
      error: null,
    };
  }

  if (parsed.Id || parsed.detectId) {
    return {
      success: true,
      data: parsed,
      error: null,
    };
  }

  return {
    success: false,
    error: {
      stage: "payload",
      errorMessage: "检测结果缺少有效数据。",
    },
  };
};

const loadHistoryPayload = async (detectId) => {
  try {
    const body = await Post("/TAudioScreenRecord/Get", {
      Id: detectId,
      UserId: commonStore.UserId || 0,
      Page: 1,
      Limit: 1,
    });

    if (!body.Success || !body.Data || !body.Data.Id) {
      rawPayload.value = {
        success: false,
        error: {
          stage: "history",
          errorMessage: body.Msg || "未找到对应的检测记录。",
        },
      };
      return;
    }

    const dto = body.Data;
    const detectDone = toBoolean(dto.DetectTotalStatus, false);
    const malignantValue = toBoolean(dto.PrimaryScreenResult, null);
    const malignant = malignantValue === true;
    const benign = malignantValue === false;
    const confidenceValue = typeof dto.PrimaryScreenConfidence === "number" ? dto.PrimaryScreenConfidence : 0;

    rawPayload.value = {
      success: detectDone,
      data: {
        ...dto,
        confidence: confidenceValue,
        predicted_class: benign ? 0 : malignant ? 1 : -1,
        benign,
        malignant,
        usingModel: dto.DensenetModelVersion || "",
        waveformUrl: dto.WaveformUrl || "",
        mfccUrl: dto.MfccSpectrumUrl || "",
        melUrl: dto.MelSpectrumUrl || "",
      },
      error: detectDone
        ? null
        : {
            stage: "history",
            errorMessage: "该记录检测尚未完成或结果为空。",
          },
    };
  } catch (requestError) {
    console.error("加载历史检测记录失败", requestError);
    rawPayload.value = {
      success: false,
      error: {
        stage: "history",
        errorMessage: "加载历史检测结果失败，请稍后重试。",
      },
    };
  }
};

const loadModelSummary = async (detectId) => {
  modelSummaryLoading.value = true;
  modelSummaryError.value = "";
  expandedSourceMap.value = {
    LOCAL_MODEL: false,
    QWEN_API: false,
  };

  try {
    const response = await Post("/Front/Model/CallLogSummary", {
      DetectId: detectId,
    });

    if (!response.Success) {
      throw new Error(response.Msg || "模型调用记录加载失败");
    }

    modelSummary.value = {
      ...(response.Data || {}),
      Items: Array.isArray(response.Data?.Items) ? response.Data.Items : [],
    };
  } catch (err) {
    modelSummary.value = null;
    modelSummaryError.value = err?.message || "模型调用记录加载失败";
  } finally {
    modelSummaryLoading.value = false;
  }
};

const retryModelSummary = () => {
  if (!currentDetectId.value) return;
  lastLoadedDetectId.value = null;
  void loadModelSummary(currentDetectId.value);
};

const buildSourceSection = (key, title, desc, items) => {
  const total = items.length;
  const success = items.filter((item) => item.CallStatus === true).length;
  const fail = Math.max(total - success, 0);
  return {
    key,
    title,
    desc,
    total,
    success,
    fail,
    avgCost: averageCost(items),
    items,
  };
};

const visibleSourceItems = (sourceKey) => {
  const section = sourceSectionMap.value[sourceKey];
  if (!section) return [];
  if (isSourceExpanded(sourceKey)) return section.items;
  return section.items.slice(0, 1);
};

const toggleSourceSection = (sourceKey) => {
  expandedSourceMap.value = {
    ...expandedSourceMap.value,
    [sourceKey]: !expandedSourceMap.value[sourceKey],
  };
};

const isSourceExpanded = (sourceKey) => {
  return !!expandedSourceMap.value[sourceKey];
};

const resolveSourceType = (item) => {
  const sourceType = String(item?.SourceType || "").toUpperCase();
  if (sourceType === "QWEN_API") return "QWEN_API";
  if (sourceType === "LOCAL_MODEL") return "LOCAL_MODEL";

  const serviceName = String(item?.ServiceName || "").toLowerCase();
  if (serviceName.includes("qwen")) return "QWEN_API";
  return "LOCAL_MODEL";
};

const parseCallTimeMs = (timeText) => {
  if (!timeText || typeof timeText !== "string") return null;
  const normalized = timeText.trim().replace(/\//g, "-");
  const isoText = normalized.includes("T") ? normalized : normalized.replace(" ", "T");
  const ms = Date.parse(isoText);
  if (Number.isNaN(ms)) return null;
  return ms;
};

const safeNumber = (value, fallback = 0) => {
  const num = Number(value);
  return Number.isFinite(num) ? num : fallback;
};

const averageCost = (items) => {
  const costs = items
    .map((item) => safeNumber(item.CallCost, NaN))
    .filter((value) => Number.isFinite(value) && value >= 0);
  if (costs.length === 0) return 0;
  const total = costs.reduce((sum, value) => sum + value, 0);
  return Math.round(total / costs.length);
};

const formatCost = (cost) => {
  const value = safeNumber(cost, 0);
  return `${value} ms`;
};

const formatCallTime = (callTime) => {
  if (!callTime) return "时间未知";
  return String(callTime).replace("T", " ");
};

const displayCallLinkName = (item) => {
  if (item?.CallLinkName) return item.CallLinkName;
  if (item?.CallLink === 0) return "预处理";
  if (item?.CallLink === 1) return "诊断流程";
  return "模型调用";
};

const formatServiceMeta = (item) => {
  const parts = [];
  if (item?.ServiceName) parts.push(item.ServiceName);
  if (item?.ModelVersion) parts.push(item.ModelVersion);
  return parts.length > 0 ? parts.join(" / ") : "服务信息未上报";
};

const formatDateTime = (dateText) => {
  if (!dateText) return "";
  const date = new Date(dateText);
  if (Number.isNaN(date.getTime())) {
    return String(dateText).replace("T", " ");
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hour = String(date.getHours()).padStart(2, "0");
  const minute = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day} ${hour}:${minute}`;
};

const previewImage = (url) => {
  if (!url) return;
  uni.previewImage({
    urls: [url],
    current: url,
  });
};

const goBack = () => {
  const pages = getCurrentPages();
  if (pages && pages.length > 1) {
    uni.navigateBack();
    return;
  }
  uni.reLaunch({ url: "/pages/Front/Index" });
};

const ensureLogin = () => {
  if (commonStore.UserId) return true;
  commonStore.CheckIsLogin();
  return false;
};

const goToHistory = () => {
  if (!ensureLogin()) return;
  uni.navigateTo({ url: "/pages/Front/AudioRecordList" });
};

const goToNewDetection = () => {
  if (!ensureLogin()) return;
  uni.redirectTo({ url: "/pages/Front/AudioRecord" });
};

const goToSymptomForm = () => {
  if (!ensureLogin()) return;
  if (!hasDetectId.value) {
    uni.showToast({ title: "缺少检测记录，无法关联症状。", icon: "none" });
    return;
  }
  uni.navigateTo({
    url: `/pages/Front/SymptomLogForm?detectId=${currentDetectId.value}`,
  });
};

const goToAIReport = () => {
  if (!ensureLogin()) return;
  if (!hasDetectId.value) {
    uni.showToast({ title: "缺少检测记录，无法生成 AI 报告。", icon: "none" });
    return;
  }
  uni.navigateTo({
    url: `/pages/Front/AudioAIReport?recordId=${currentDetectId.value}`,
  });
};

const goToAIChat = () => {
  if (!ensureLogin()) return;
  if (!hasDetectId.value) {
    uni.showToast({ title: "缺少检测记录，无法发起 AI 咨询。", icon: "none" });
    return;
  }
  uni.navigateTo({
    url: `/pages/Front/AudioAIChat?recordId=${currentDetectId.value}`,
  });
};

const goToMedicalHome = () => {
  if (!ensureLogin()) return;
  uni.navigateTo({ url: "/pages/Front/MedicalHome" });
};

const goToLaryngoscopeUpload = () => {
  if (!ensureLogin()) return;
  if (!hasDetectId.value) {
    uni.showToast({ title: "缺少检测记录，无法关联喉镜。", icon: "none" });
    return;
  }
  uni.navigateTo({
    url: `/pages/Front/LaryngoscopePhotoEdit?detectId=${currentDetectId.value}`,
  });
};
</script>

<style scoped lang="scss">
.audio-result-page {
  min-height: 100vh;
}

.result-screen {
  gap: 30upx;
}

.result-head {
  gap: 10upx;
}

.result-head__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.result-head__back {
  min-height: 58upx;
  padding: 0 20upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  gap: 8upx;
  background: rgba(248, 252, 239, 0.9);
  border: 1upx solid rgba(199, 218, 141, 0.92);
  color: #152012;
  font-size: 22upx;
  font-weight: 700;
}

.result-head__kicker {
  min-height: 52upx;
  padding: 0 22upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(248, 252, 239, 0.86);
  border: 1upx solid rgba(200, 219, 141, 0.92);
  color: #2d3c2f;
  font-size: 22upx;
  font-weight: 800;
}

.loading-card,
.error-card {
  border-radius: 38upx;
  text-align: center;
}

.loading-card {
  padding: 54upx 36upx;
}

.loading-card__spinner {
  width: 64upx;
  height: 64upx;
  margin: 0 auto;
  border-radius: 999upx;
  border: 5upx solid rgba(30, 40, 30, 0.14);
  border-top-color: #7fb04a;
  animation: resultSpin 0.9s linear infinite;
}

.loading-card__text {
  margin-top: 16upx;
  display: block;
  font-size: 24upx;
  color: #5c6c5d;
}

.error-card {
  padding: 42upx 28upx;
  border-color: rgba(218, 140, 120, 0.55);
  background: rgba(252, 244, 239, 0.88);
}

.error-card__icon {
  width: 64upx;
  height: 64upx;
  margin: 0 auto;
  border-radius: 999upx;
  background: #d26e5d;
  color: #ffffff;
  font-size: 36upx;
  line-height: 64upx;
  font-weight: 800;
}

.error-card__title {
  margin-top: 14upx;
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #1f291f;
}

.error-card__text {
  margin-top: 10upx;
  display: block;
  font-size: 22upx;
  line-height: 1.6;
  color: #6a5a57;
}

.error-card__action {
  margin-top: 20upx;
}

.verdict-card {
  display: flex;
  align-items: flex-start;
  gap: 18upx;
  border-radius: 40upx;
}

.verdict-card__icon-wrap {
  padding-top: 6upx;
}

.verdict-card__icon {
  width: 70upx;
  height: 70upx;
  border-radius: 22upx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #6f9552;
  box-shadow: 0 8upx 20upx rgba(74, 111, 48, 0.28);
}

.verdict-card__icon.danger {
  background: #ca6a58;
  box-shadow: 0 8upx 20upx rgba(126, 67, 55, 0.25);
}

.verdict-card__main {
  flex: 1;
}

.verdict-card__title {
  display: block;
  font-size: 66upx;
  line-height: 1.03;
  font-weight: 800;
  color: #132014;
}

.verdict-card__subtitle {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #425241;
}

.verdict-card__tag {
  margin-top: 16upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 54upx;
  padding: 0 26upx;
  border-radius: 999upx;
  background: rgba(248, 252, 239, 0.7);
  border: 1upx solid rgba(163, 198, 94, 0.84);
  color: #35522f;
  font-size: 24upx;
  font-weight: 800;
}

.verdict-card__tag.danger {
  border-color: rgba(204, 116, 94, 0.7);
  color: #784234;
  background: rgba(251, 238, 232, 0.68);
}

.key-card {
  border-radius: 38upx;
  padding: 24upx;
}

.key-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14upx;
}

.key-card__title {
  font-size: 56upx;
  line-height: 1.05;
  font-weight: 800;
  color: #172119;
}

.key-card__score {
  text-align: right;
}

.key-card__score-label {
  display: block;
  font-size: 20upx;
  color: #8b9a8b;
}

.key-card__score-value {
  display: block;
  margin-top: 8upx;
  font-size: 70upx;
  line-height: 1;
  font-weight: 800;
  color: #243927;
}

.key-card__progress-label {
  margin-top: 14upx;
  display: block;
  font-size: 22upx;
  color: #556455;
}

.key-card__progress {
  margin-top: 10upx;
}

.key-card__grid {
  margin-top: 16upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.key-item {
  min-height: 114upx;
  border-radius: 24upx;
  padding: 16upx 18upx;
  background: rgba(248, 252, 239, 0.74);
  border: 1upx solid rgba(201, 220, 146, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.key-item__label {
  display: block;
  font-size: 20upx;
  color: #839282;
}

.key-item__value {
  margin-top: 8upx;
  display: block;
  font-size: 22upx;
  line-height: 1.52;
  color: #243126;
  font-weight: 700;
}

.model-card {
  border-radius: 38upx;
  padding: 24upx;
}

.model-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14upx;
}

.model-card__title {
  display: block;
  font-size: 46upx;
  line-height: 1.08;
  font-weight: 800;
  color: #172119;
}

.model-card__subtitle {
  margin-top: 8upx;
  display: block;
  font-size: 22upx;
  color: #6a7a6a;
}

.model-card__badge {
  min-height: 54upx;
  padding: 0 20upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22upx;
  font-weight: 800;
  background: rgba(248, 252, 239, 0.9);
  border: 1upx solid rgba(201, 220, 145, 0.88);
  color: #415541;
}

.model-card__badge.is-normal {
  color: #3f6531;
  border-color: rgba(130, 176, 84, 0.75);
  background: rgba(236, 249, 214, 0.9);
}

.model-card__badge.is-warning {
  color: #7a5d1f;
  border-color: rgba(220, 185, 103, 0.76);
  background: rgba(255, 246, 220, 0.9);
}

.model-card__badge.is-error {
  color: #7a3e32;
  border-color: rgba(213, 133, 111, 0.78);
  background: rgba(255, 236, 230, 0.9);
}

.model-card__badge.is-none {
  color: #687869;
}

.model-card__loading {
  margin-top: 18upx;
  border-radius: 22upx;
  padding: 22upx;
  background: rgba(248, 252, 239, 0.68);
  border: 1upx solid rgba(203, 220, 150, 0.72);
  font-size: 22upx;
  color: #6d7b6d;
}

.model-card__error {
  margin-top: 18upx;
  border-radius: 22upx;
  padding: 22upx;
  background: rgba(252, 244, 239, 0.82);
  border: 1upx solid rgba(218, 140, 121, 0.72);
}

.model-card__error-text {
  display: block;
  font-size: 22upx;
  line-height: 1.58;
  color: #6e4d46;
}

.model-card__retry {
  margin-top: 14upx;
  min-height: 54upx;
  padding: 0 20upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(248, 252, 239, 0.9);
  border: 1upx solid rgba(195, 213, 137, 0.86);
  color: #3f523f;
  font-size: 22upx;
  font-weight: 700;
}

.model-overview-grid {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.model-overview-item {
  min-height: 108upx;
  border-radius: 22upx;
  padding: 16upx;
  background: rgba(248, 252, 239, 0.76);
  border: 1upx solid rgba(200, 218, 145, 0.76);
}

.model-overview-item__label {
  display: block;
  font-size: 20upx;
  color: #869486;
}

.model-overview-item__value {
  margin-top: 8upx;
  display: block;
  font-size: 42upx;
  line-height: 1.1;
  font-weight: 800;
  color: #243126;
}

.model-overview-item__value.success {
  color: #4e7940;
}

.model-overview-item__value.danger {
  color: #9f5646;
}

.source-groups {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.source-group {
  border-radius: 26upx;
  padding: 18upx;
  background: rgba(248, 252, 239, 0.66);
  border: 1upx solid rgba(201, 219, 145, 0.76);
}

.source-group__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.source-group__head-main {
  min-width: 0;
}

.source-group__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #1d2b1f;
}

.source-group__meta {
  margin-top: 4upx;
  display: block;
  font-size: 20upx;
  color: #7e8e7e;
}

.source-group__count {
  min-width: 48upx;
  height: 48upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22upx;
  font-weight: 800;
  background: rgba(217, 241, 156, 0.8);
  color: #223325;
}

.source-group__metrics {
  margin-top: 12upx;
  display: flex;
  flex-wrap: wrap;
  gap: 10upx;
  font-size: 20upx;
  color: #5e6e5f;
}

.source-group__metrics .ok {
  color: #4d7840;
}

.source-group__metrics .fail {
  color: #9a5546;
}

.source-group__empty {
  margin-top: 12upx;
  font-size: 22upx;
  color: #7b8a7b;
}

.source-log-list {
  margin-top: 12upx;
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.source-log-item {
  border-radius: 20upx;
  padding: 14upx 16upx;
  background: rgba(248, 252, 239, 0.86);
  border: 1upx solid rgba(198, 216, 138, 0.7);
}

.source-log-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10upx;
}

.source-log-item__name {
  min-width: 0;
  font-size: 24upx;
  font-weight: 800;
  color: #223026;
}

.source-log-item__status {
  min-height: 40upx;
  padding: 0 12upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18upx;
  font-weight: 800;
  color: #556555;
  background: rgba(236, 247, 213, 0.9);
  border: 1upx solid rgba(161, 191, 98, 0.72);
}

.source-log-item__status.ok {
  color: #3f6432;
}

.source-log-item__status.fail {
  color: #9d5546;
  background: rgba(252, 241, 236, 0.88);
  border-color: rgba(210, 126, 105, 0.72);
}

.source-log-item__meta {
  margin-top: 8upx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10upx;
  font-size: 20upx;
  color: #7c8a7d;
}

.source-log-item__summary {
  margin-top: 8upx;
  display: block;
  font-size: 21upx;
  line-height: 1.55;
  color: #4a5a4a;
}

.source-log-item__service {
  margin-top: 6upx;
  display: block;
  font-size: 19upx;
  color: #8a988b;
}

.source-group__toggle {
  margin-top: 12upx;
  min-height: 54upx;
  border-radius: 999upx;
  border: 1upx solid rgba(193, 212, 134, 0.82);
  background: rgba(248, 252, 239, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #385038;
  font-size: 22upx;
  font-weight: 700;
}

.spectrum-card {
  border-radius: 36upx;
  padding: 22upx;
}

.spectrum-grid {
  margin-top: 14upx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10upx;
}

.spectrum-item {
  border-radius: 20upx;
  padding: 10upx;
  background: rgba(248, 252, 239, 0.74);
  border: 1upx solid rgba(203, 220, 148, 0.8);
}

.spectrum-item__title {
  display: block;
  font-size: 20upx;
  color: #516151;
  margin-bottom: 8upx;
}

.spectrum-item__img {
  width: 100%;
  height: 120upx;
  border-radius: 14upx;
}

.ai-card {
  border-radius: 36upx;
  padding: 22upx;
}

.ai-card__head {
  display: flex;
  align-items: center;
  gap: 12upx;
}

.ai-card__icon {
  width: 64upx;
  height: 64upx;
  border-radius: 20upx;
  background: #6f9154;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-card__meta {
  flex: 1;
  min-width: 0;
}

.ai-card__title {
  display: block;
  font-size: 34upx;
  font-weight: 800;
  color: #1b271d;
}

.ai-card__subtitle {
  margin-top: 6upx;
  display: block;
  font-size: 22upx;
  color: #758574;
}

.ai-card__tag {
  min-height: 52upx;
  padding: 0 18upx;
  border-radius: 999upx;
  background: rgba(226, 244, 165, 0.74);
  border: 1upx solid rgba(177, 209, 104, 0.78);
  color: #516c31;
  font-size: 22upx;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.ai-card__actions {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.ai-btn {
  min-height: 82upx;
  border-radius: 999upx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28upx;
  font-weight: 800;
}

.ai-btn--dark {
  background: var(--gradient-contrast);
  color: #ffffff;
  border: 1upx solid rgba(30, 41, 33, 0.88);
}

.ai-btn--soft {
  background: rgba(248, 252, 239, 0.84);
  color: #243123;
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.quick-item {
  border-radius: 28upx;
  padding: 18upx;
  background: rgba(248, 252, 239, 0.82);
  border: 1upx solid rgba(203, 220, 148, 0.84);
}

.quick-item__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #1d2a1f;
}

.quick-item__desc {
  margin-top: 8upx;
  display: block;
  font-size: 21upx;
  line-height: 1.55;
  color: #667667;
}

.result-bottom-actions {
  margin-top: 2upx;
}

.disabled {
  opacity: 0.48;
}

@keyframes resultSpin {
  to {
    transform: rotate(360deg);
  }
}
</style>
