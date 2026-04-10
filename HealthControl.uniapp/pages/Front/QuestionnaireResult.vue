<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack questionnaire-result-screen">
      <hc-topbar
        :title="'测评结果'"
        :subtitle="'阶段风险结论与后续建议'"
        :show-back="true"
        :right-text="'问卷首页'"
        @left="back"
        @right="goHome"
      />

      <view class="bg-head result-head hc-reveal-up">
        <view class="hc-kicker">结果概览</view>
        <text class="bg-head__title">{{ headlineTitle }}</text>
        <text class="bg-head__subtitle">{{ subtitleText }}</text>
      </view>

      <view
        class="verdict-card hc-reveal-up"
        :class="[verdictCardThemeClass, levelSurfaceClass]"
        style="--delay: 100ms"
      >
        <view class="verdict-card__head">
          <view class="verdict-card__icon" :class="levelClassName">
            <uni-icons :type="iconType" size="24" color="#ffffff" />
          </view>
          <view class="verdict-card__meta">
            <text class="verdict-card__eyebrow">风险结论</text>
            <text class="verdict-card__title">{{ levelText(level) }}</text>
            <text class="verdict-card__desc">{{ summaryText }}</text>
          </view>
          <view class="verdict-card__badge" :class="levelClassName">
            <text class="verdict-card__badge-label">得分</text>
            <text class="verdict-card__badge-value">{{ score }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft insight-card hc-reveal-up" style="--delay: 160ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">关键摘要</text>
          <text class="hc-section-head__meta">以结果卡形式快速理解状态</text>
        </view>

        <view class="insight-grid">
          <view class="insight-item">
            <text class="insight-item__label">风险等级</text>
            <text class="insight-item__value">{{ levelText(level) }}</text>
          </view>
          <view class="insight-item">
            <text class="insight-item__label">阶段得分</text>
            <text class="insight-item__value">{{ score }}</text>
          </view>
        </view>

        <view class="insight-note">
          <text class="insight-note__title">结果说明</text>
          <text class="insight-note__text">{{ summaryText }}</text>
        </view>
      </view>

      <view class="hc-card-soft guidance-card hc-reveal-up" style="--delay: 220ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">建议动作</text>
          <text class="hc-section-head__meta">先做关键动作，再继续跟进</text>
        </view>

        <view class="guidance-list">
          <view v-for="(item, index) in guidanceList" :key="index" class="guidance-item">
            <view class="guidance-item__dot" :class="levelClassName"></view>
            <text class="guidance-item__text">{{ item }}</text>
          </view>
        </view>

        <view class="guidance-alert" :class="levelClassName">
          {{ actionHint }}
        </view>
      </view>

      <view class="result-actions hc-reveal-up" style="--delay: 280ms">
        <view class="hc-pill-button-dark hc-interactive-pill" @click="goReminder">去设置提醒</view>
        <view class="hc-pill-button-soft hc-interactive-pill" @click="goScience">去看科普</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const score = ref(0);
const level = ref(0);
const summary = ref("");

const isHighRisk = computed(() => level.value === 2);
const isMediumRisk = computed(() => level.value === 1);
const isLowRisk = computed(() => level.value === 0);

const summaryText = computed(() => {
  if (summary.value) return summary.value;
  if (isHighRisk.value) return "当前测评提示高风险，建议尽快结合近期症状与线下检查继续评估。";
  if (isMediumRisk.value) return "当前测评提示中风险，建议持续跟踪并开启稳定提醒节奏。";
  return "当前测评结果偏稳，建议保持轻量但持续的自查与护理节奏。";
});

const headlineTitle = computed(() => {
  if (isHighRisk.value) return "本次测评提示高风险，建议优先跟进";
  if (isMediumRisk.value) return "本次测评提示中风险，需要持续观察";
  return "本次测评结果较稳，继续保持健康管理节奏";
});

const subtitleText = computed(() => {
  if (isHighRisk.value) return "先完成复查准备与提醒配置，再继续结合症状记录判断变化。";
  if (isMediumRisk.value) return "建议开启提醒、继续跟踪问卷和症状变化，不要中断观察。";
  return "继续用轻量方式做自查、记录和科普学习，把健康管理保持在日常节奏里。";
});

const guidanceList = computed(() => {
  if (isHighRisk.value) {
    return [
      "尽快开启自查与复查提醒，避免遗漏后续跟进。",
      "结合近期症状记录与历史检测结果整理变化趋势。",
      "如不适持续或加重，建议尽快线下就医。",
    ];
  }
  if (isMediumRisk.value) {
    return [
      "保持每周节奏继续完成后续问卷与自查。",
      "记录症状变化，观察是否出现持续加重趋势。",
      "通过科普内容补充护理与用嗓管理知识。",
    ];
  }
  return [
    "继续定期完成音频自查，维持稳定观察样本。",
    "保持温和用嗓、多喝温水、减少刺激性因素。",
    "可继续阅读喉部健康科普，巩固日常护理习惯。",
  ];
});

const actionHint = computed(() => {
  if (isHighRisk.value) return "高风险结果更适合优先开启提醒，并尽快结合线下检查继续判断。";
  if (isMediumRisk.value) return "中风险阶段的关键是持续跟踪，而不是只看一次结果。";
  return "低风险不代表完全忽略，稳定记录才有助于长期判断变化。";
});

const iconType = computed(() => {
  if (isHighRisk.value) return "info";
  if (isMediumRisk.value) return "help";
  return "checkmarkempty";
});

const levelClassName = computed(() => {
  if (isHighRisk.value) return "is-high";
  if (isMediumRisk.value) return "is-medium";
  return "is-low";
});

const verdictCardThemeClass = computed(() => (isLowRisk.value ? "hc-card-lime" : "hc-card-soft"));
const levelSurfaceClass = computed(() => {
  if (isHighRisk.value) return "verdict-card--high";
  if (isMediumRisk.value) return "verdict-card--medium";
  return "verdict-card--low";
});

const levelText = (value) => (value === 2 ? "高风险" : value === 1 ? "中风险" : "低风险");

const goReminder = () => {
  uni.navigateTo({ url: "/pages/Front/HealthReminder" });
};

const goScience = () => {
  uni.navigateTo({ url: "/pages/Front/ScienceHome" });
};

const goHome = () => {
  uni.reLaunch({ url: "/pages/Front/QuestionnaireHome" });
};

const back = () => {
  const pages = getCurrentPages();
  if (pages && pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  goHome();
};

onLoad((query) => {
  score.value = query && query.Score ? Number(query.Score) : 0;
  level.value = query && query.Level !== undefined ? Number(query.Level) : 0;
  summary.value = query && query.Summary ? decodeURIComponent(query.Summary) : "";
});
</script>

<style scoped lang="scss">
.questionnaire-result-screen {
  gap: 24upx;
}

.result-head {
  gap: 10upx;
}

.verdict-card {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.verdict-card--medium {
  border-color: rgba(220, 184, 103, 0.7);
  background: rgba(255, 248, 229, 0.92);
}

.verdict-card--high {
  border-color: rgba(213, 133, 111, 0.72);
  background: rgba(255, 241, 236, 0.92);
}

.verdict-card__head {
  display: flex;
  align-items: flex-start;
  gap: 16upx;
}

.verdict-card__icon {
  width: 70upx;
  height: 70upx;
  border-radius: 22upx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #6f9652;
  box-shadow: 0 10upx 22upx rgba(83, 116, 51, 0.22);
  flex-shrink: 0;
}

.verdict-card__icon.is-medium {
  background: #c59a49;
  box-shadow: 0 10upx 22upx rgba(142, 108, 40, 0.2);
}

.verdict-card__icon.is-high {
  background: #cb6a58;
  box-shadow: 0 10upx 22upx rgba(127, 70, 58, 0.22);
}

.verdict-card__meta {
  flex: 1;
  min-width: 0;
}

.verdict-card__eyebrow {
  display: block;
  font-size: 20upx;
  color: #7c8a79;
}

.verdict-card__title {
  display: block;
  margin-top: 8upx;
  font-size: 48upx;
  line-height: 1.1;
  font-weight: 800;
  color: #132015;
}

.verdict-card__desc {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #485947;
}

.verdict-card__badge {
  min-width: 136upx;
  border-radius: 30upx;
  padding: 16upx 18upx;
  background: rgba(248, 252, 239, 0.84);
  text-align: right;
  flex-shrink: 0;
}

.verdict-card__badge.is-medium {
  background: rgba(255, 244, 219, 0.92);
}

.verdict-card__badge.is-high {
  background: rgba(255, 236, 230, 0.92);
}

.verdict-card__badge-label {
  display: block;
  font-size: 20upx;
  color: #7d8b7d;
}

.verdict-card__badge-value {
  display: block;
  margin-top: 8upx;
  font-size: 52upx;
  line-height: 1;
  font-weight: 800;
  color: #233324;
}

.insight-grid {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.insight-item {
  min-height: 108upx;
  border-radius: 26upx;
  padding: 18upx;
  background: rgba(248, 252, 239, 0.76);
  border: 1upx solid rgba(201, 219, 145, 0.8);
}

.insight-item__label {
  display: block;
  font-size: 20upx;
  color: #859585;
}

.insight-item__value {
  display: block;
  margin-top: 8upx;
  font-size: 34upx;
  font-weight: 800;
  color: #1d2a1f;
}

.insight-note {
  margin-top: 18upx;
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.82);
  border: 1upx solid rgba(201, 220, 145, 0.8);
}

.insight-note__title {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #1f2c20;
}

.insight-note__text {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.guidance-list {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.guidance-item {
  display: flex;
  align-items: flex-start;
  gap: 12upx;
}

.guidance-item__dot {
  width: 16upx;
  height: 16upx;
  border-radius: 50%;
  background: #8ec94f;
  margin-top: 8upx;
  flex-shrink: 0;
}

.guidance-item__dot.is-medium {
  background: #c59a49;
}

.guidance-item__dot.is-high {
  background: #cb6a58;
}

.guidance-item__text {
  font-size: 24upx;
  line-height: 1.65;
  color: #556556;
}

.guidance-alert {
  margin-top: 18upx;
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(142, 201, 79, 0.12);
  color: #4b6938;
  font-size: 22upx;
  line-height: 1.6;
}

.guidance-alert.is-medium {
  background: rgba(243, 179, 77, 0.14);
  color: #7f5f1c;
}

.guidance-alert.is-high {
  background: rgba(228, 108, 73, 0.14);
  color: #824538;
}

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}
</style>
