<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack questionnaire-history-screen">
      <hc-topbar
        :title="'风险问卷历史'"
        :subtitle="'回看各次测评结果与完整答题详情'"
        :show-back="true"
        :right-text="'问卷首页'"
        @left="back"
        @right="goHome"
      />

      <view class="bg-head history-head hc-reveal-up">
        <view class="hc-kicker">历史记录</view>
        <text class="bg-head__title">用历史结果看清阶段变化</text>
        <text class="bg-head__subtitle">每条记录保留风险等级、得分摘要和完整答题详情，便于持续跟踪。</text>
      </view>

      <view v-if="loading" class="hc-card-soft loading-card hc-reveal-up" style="--delay: 100ms">
        <view class="loading-card__spinner"></view>
        <text class="loading-card__text">正在加载历史记录...</text>
      </view>

      <template v-else>
        <view v-if="items.length" class="hc-card-lime summary-card hc-reveal-up" style="--delay: 120ms">
          <view class="summary-card__grid">
            <view class="summary-card__metric">
              <text class="hc-stat-value">{{ items.length }}</text>
              <text class="hc-stat-label">历史记录数</text>
            </view>
            <view class="summary-card__metric summary-card__metric--dark">
              <text class="summary-card__level">{{ latestLevelText }}</text>
              <text class="hc-stat-label">最近一次等级</text>
            </view>
          </view>

          <view class="summary-card__foot">
            <text class="summary-card__foot-main">最近得分 {{ latestScore }}</text>
            <text class="summary-card__foot-meta">{{ latestTime }}</text>
          </view>
        </view>

        <view v-if="items.length" class="history-list">
          <view
            v-for="(record, index) in items"
            :key="record.Id"
            class="hc-card-soft history-card hc-interactive-card hc-reveal-up"
            :style="{ '--delay': `${140 + index * 40}ms` }"
            @click="toggleExpand(record)"
          >
            <view class="history-card__head">
              <view class="history-card__main">
                <text class="history-card__title">{{ record.QuestionnaireTitle || "风险问卷" }}</text>
                <text class="history-card__time">测评时间：{{ formatTime(record.AnswerTime) }}</text>
              </view>
              <view class="history-card__tag" :class="levelClass(record.RiskLevel)">
                {{ levelText(record.RiskLevel) }}
              </view>
            </view>

            <view class="history-card__body">
              <view class="history-card__score">
                <text class="history-card__score-label">阶段得分</text>
                <text class="history-card__score-value">{{ record.RiskAssessmentScore ?? 0 }}</text>
              </view>
              <view class="history-card__summary">
                <text class="history-card__summary-label">结果摘要</text>
                <text class="history-card__summary-text">{{ record.ResultSummary || "该记录暂无结果摘要。" }}</text>
              </view>
            </view>

            <view class="history-card__toggle">
              <text class="history-card__toggle-text">{{ record._expanded ? "收起答题详情" : "展开答题详情" }}</text>
              <uni-icons :type="record._expanded ? 'top' : 'bottom'" size="16" color="#70806f" />
            </view>

            <view v-if="record._expanded" class="answer-list">
              <text class="answer-list__title">答题详情</text>
              <view v-if="record.Answers && record.Answers.length" class="answer-items">
                <view v-for="(answer, answerIndex) in record.Answers" :key="answer.QuestionId" class="answer-item">
                  <text class="answer-item__question">{{ answerIndex + 1 }}. {{ answer.QuestionTitle }}</text>
                  <text class="answer-item__answer">
                    {{ answer.AnswerTextList && answer.AnswerTextList.length ? answer.AnswerTextList.join("，") : "（未回答）" }}
                  </text>
                </view>
              </view>
              <text v-else class="answer-list__empty">该记录未能解析出答题详情。</text>
            </view>
          </view>
        </view>

        <view v-else class="hc-card-soft empty-card hc-reveal-up" style="--delay: 120ms">
          <view class="empty-card__icon">
            <uni-icons type="calendar" size="32" color="#1a251d" />
          </view>
          <text class="empty-card__title">暂无历史记录</text>
          <text class="empty-card__desc">完成一次风险问卷后，这里会沉淀你的阶段结果与答题详情。</text>
          <view class="hc-pill-button-dark empty-card__action" @click="goHome">开始测评</view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();
const items = ref([]);
const loading = ref(false);

const parseTime = (value) => {
  if (!value) return 0;
  const time = new Date(String(value).replace("T", " "));
  return Number.isNaN(time.getTime()) ? 0 : time.getTime();
};

const sortedItems = computed(() => {
  return [...items.value].sort((a, b) => parseTime(b.AnswerTime) - parseTime(a.AnswerTime));
});

const latestRecord = computed(() => (sortedItems.value.length ? sortedItems.value[0] : null));
const latestScore = computed(() => latestRecord.value?.RiskAssessmentScore ?? 0);
const latestTime = computed(() => formatTime(latestRecord.value?.AnswerTime));
const latestLevelText = computed(() => levelText(latestRecord.value?.RiskLevel ?? 0));

const load = async () => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录" });
    return;
  }
  loading.value = true;
  try {
    const { Data, Success, Msg } = await Post("/Front/Questionnaire/MyHistory", {
      Page: 1,
      Limit: 20,
    });
    if (!Success) {
      uni.showToast({ icon: "none", title: Msg || "加载失败" });
      items.value = [];
      return;
    }
    const list = ((Data && Data.Items) || [])
      .sort((a, b) => parseTime(b.AnswerTime) - parseTime(a.AnswerTime))
      .map((item) => ({
        ...item,
        _expanded: false,
      }));
    items.value = list;
  } catch (error) {
    console.error("加载历史记录失败", error);
    items.value = [];
    uni.showToast({ icon: "none", title: "加载失败" });
  } finally {
    loading.value = false;
  }
};

const levelText = (value) => (value === 2 ? "高风险" : value === 1 ? "中风险" : "低风险");
const levelClass = (value) => (value === 2 ? "is-high" : value === 1 ? "is-medium" : "is-low");

const formatTime = (value) => {
  if (!value) return "--";
  const normalized = String(value).replace("T", " ");
  return normalized.length >= 16 ? normalized.slice(0, 16) : normalized;
};

const toggleExpand = (item) => {
  item._expanded = !item._expanded;
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

onLoad(load);
</script>

<style scoped lang="scss">
.questionnaire-history-screen {
  gap: 24upx;
}

.history-head {
  gap: 10upx;
}

.summary-card__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12upx;
}

.summary-card__metric {
  border-radius: 30upx;
  padding: 22upx;
  background: rgba(248, 252, 239, 0.76);
  border: 1upx solid rgba(199, 218, 141, 0.74);
}

.summary-card__metric--dark {
  background: linear-gradient(135deg, #101612 0%, #1d271f 100%);
}

.summary-card__level {
  display: block;
  font-size: 42upx;
  line-height: 1.1;
  font-weight: 800;
  color: #f7ffdf;
}

.summary-card__foot {
  margin-top: 18upx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
  flex-wrap: wrap;
}

.summary-card__foot-main {
  font-size: 28upx;
  font-weight: 800;
  color: #162116;
}

.summary-card__foot-meta {
  font-size: 22upx;
  color: #596959;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.history-card {
  padding: 24upx;
}

.history-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14upx;
}

.history-card__main {
  flex: 1;
  min-width: 0;
}

.history-card__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #18231b;
}

.history-card__time {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  color: #748373;
}

.history-card__tag {
  min-height: 50upx;
  padding: 0 18upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
  color: #ffffff;
  flex-shrink: 0;
}

.history-card__tag.is-low {
  background: #8fb852;
}

.history-card__tag.is-medium {
  background: #c19446;
}

.history-card__tag.is-high {
  background: #cc6354;
}

.history-card__body {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: 180upx minmax(0, 1fr);
  gap: 14upx;
}

.history-card__score {
  border-radius: 28upx;
  padding: 18upx;
  background: rgba(248, 252, 239, 0.76);
  border: 1upx solid rgba(201, 220, 146, 0.82);
}

.history-card__score-label {
  display: block;
  font-size: 20upx;
  color: #7b8a7b;
}

.history-card__score-value {
  display: block;
  margin-top: 10upx;
  font-size: 52upx;
  line-height: 1;
  font-weight: 800;
  color: #1b2b1c;
}

.history-card__summary {
  border-radius: 28upx;
  padding: 18upx;
  background: rgba(248, 252, 239, 0.76);
  border: 1upx solid rgba(201, 220, 146, 0.82);
}

.history-card__summary-label {
  display: block;
  font-size: 20upx;
  color: #7b8a7b;
}

.history-card__summary-text {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #536353;
}

.history-card__toggle {
  margin-top: 16upx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10upx;
}

.history-card__toggle-text {
  font-size: 22upx;
  font-weight: 700;
  color: #5d6d5d;
}

.answer-list {
  margin-top: 16upx;
  padding-top: 16upx;
  border-top: 1upx solid rgba(205, 224, 145, 0.78);
}

.answer-list__title {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #1d2a1f;
}

.answer-items {
  margin-top: 12upx;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.answer-item {
  border-radius: 24upx;
  padding: 16upx 18upx;
  background: rgba(248, 252, 239, 0.72);
  border: 1upx solid rgba(202, 220, 148, 0.78);
}

.answer-item__question {
  display: block;
  font-size: 22upx;
  line-height: 1.6;
  color: #1e2a1f;
  font-weight: 700;
}

.answer-item__answer {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #5a695a;
}

.answer-list__empty {
  margin-top: 12upx;
  display: block;
  font-size: 22upx;
  color: #788777;
}

.loading-card,
.empty-card {
  text-align: center;
}

.loading-card {
  padding: 52upx 34upx;
}

.loading-card__spinner {
  width: 64upx;
  height: 64upx;
  margin: 0 auto;
  border-radius: 999upx;
  border: 5upx solid rgba(29, 39, 31, 0.14);
  border-top-color: #7ead49;
  animation: historySpin 0.9s linear infinite;
}

.loading-card__text {
  margin-top: 16upx;
  display: block;
  font-size: 24upx;
  color: #5b6a5a;
}

.empty-card {
  padding: 48upx 30upx;
}

.empty-card__icon {
  width: 82upx;
  height: 82upx;
  margin: 0 auto;
  border-radius: 28upx;
  background: #def39a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-card__title {
  margin-top: 18upx;
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #1a261c;
}

.empty-card__desc {
  margin-top: 12upx;
  display: block;
  font-size: 24upx;
  line-height: 1.65;
  color: #627262;
}

.empty-card__action {
  margin-top: 22upx;
}

@keyframes historySpin {
  to {
    transform: rotate(360deg);
  }
}
</style>
