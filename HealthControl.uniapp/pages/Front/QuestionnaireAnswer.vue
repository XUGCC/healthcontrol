<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack questionnaire-answer-screen">
      <hc-topbar
        :title="'风险自评'"
        :subtitle="'逐题完成答题并生成阶段风险结果'"
        :show-back="true"
        :right-text="'问卷首页'"
        @left="back"
        @right="goHome"
      />

      <view class="bg-head answer-head hc-reveal-up">
        <view class="hc-kicker">当前问卷</view>
        <text class="bg-head__title">{{ questionnaireTitle }}</text>
        <text class="bg-head__subtitle">
          {{ questionnaireDesc || "完成全部关键题目后，系统会生成风险等级、结果摘要和后续建议。" }}
        </text>
      </view>

      <view v-if="loading" class="hc-card-soft loading-card hc-reveal-up" style="--delay: 100ms">
        <view class="loading-card__spinner"></view>
        <text class="loading-card__text">正在加载问卷题目...</text>
      </view>

      <template v-else>
        <view v-if="questions.length" class="hc-card-lime summary-card hc-reveal-up" style="--delay: 120ms">
          <view class="summary-card__grid">
            <view class="summary-card__metric">
              <text class="hc-stat-value">{{ answeredCount }}</text>
              <text class="hc-stat-label">已完成题数</text>
            </view>
            <view class="summary-card__metric summary-card__metric--dark">
              <text class="hc-stat-value hc-stat-value--dark">{{ requiredCount }}</text>
              <text class="hc-stat-label">必答题数</text>
            </view>
          </view>

          <view class="summary-card__progress-row">
            <text class="summary-card__progress-label">整体进度</text>
            <text class="summary-card__progress-value">{{ progressPercent }}%</text>
          </view>
          <view class="hc-progress">
            <view class="hc-progress__bar" :style="{ width: `${progressPercent}%` }"></view>
          </view>

          <view class="summary-card__tips">
            <view class="summary-card__tip">按顺序作答更容易保持节奏</view>
            <view class="summary-card__tip">必答题未完成时不可提交</view>
          </view>
        </view>

        <view v-if="questions.length" class="question-list">
          <view
            v-for="(q, idx) in questions"
            :key="q.Id"
            class="hc-card-soft question-card hc-reveal-up"
            :style="{ '--delay': `${140 + idx * 40}ms` }"
          >
            <view class="question-card__head">
              <view class="question-card__index">{{ idx + 1 }}</view>
              <view class="question-card__meta">
                <text class="question-card__title">{{ q.QuestionTitle }}</text>
                <text v-if="q.QuestionDesc" class="question-card__desc">{{ q.QuestionDesc }}</text>
              </view>
              <view class="question-card__tag" :class="{ 'question-card__tag--required': Number(q.IsRequired) === 1 }">
                {{ Number(q.IsRequired) === 1 ? "必答" : "选答" }}
              </view>
            </view>

            <radio-group
              v-if="Number(q.QuestionType) === 1 || Number(q.QuestionType) === 3"
              class="question-options"
              :name="`q_${q.Id}`"
              @change="onSingleChange(q.Id, $event.detail.value)"
            >
              <label
                v-for="opt in q.Options"
                :key="opt.Id"
                class="question-option"
                :class="{ 'is-selected': isChecked(q.Id, opt.OptionValue) }"
              >
                <radio class="question-option__native" :value="opt.OptionValue" :checked="isChecked(q.Id, opt.OptionValue)" />
                <view class="question-option__mark"></view>
                <view class="question-option__body">
                  <text class="question-option__text">{{ opt.OptionText }}</text>
                </view>
              </label>
            </radio-group>

            <checkbox-group
              v-else-if="Number(q.QuestionType) === 2"
              class="question-options"
              :name="`q_${q.Id}`"
              @change="onMultiChange(q.Id, $event.detail.value)"
            >
              <label
                v-for="opt in q.Options"
                :key="opt.Id"
                class="question-option"
                :class="{ 'is-selected': isChecked(q.Id, opt.OptionValue) }"
              >
                <checkbox class="question-option__native" :value="opt.OptionValue" :checked="isChecked(q.Id, opt.OptionValue)" />
                <view class="question-option__mark question-option__mark--checkbox"></view>
                <view class="question-option__body">
                  <text class="question-option__text">{{ opt.OptionText }}</text>
                </view>
              </label>
            </checkbox-group>

            <view v-else-if="Number(q.QuestionType) === 4" class="question-textarea-wrap">
              <textarea
                class="question-textarea"
                :value="getTextAnswer(q.Id)"
                :placeholder="q.QuestionDesc || '请输入你的回答'"
                maxlength="200"
                auto-height
                @input="onTextChange(q.Id, $event.detail.value)"
              />
            </view>

            <view v-else class="question-unsupported">
              暂不支持的题型，请联系管理员调整为单选、多选或填空题。
            </view>
          </view>
        </view>

        <view v-else class="hc-card-soft empty-card hc-reveal-up" style="--delay: 100ms">
          <view class="empty-card__icon">
            <uni-icons type="help" size="32" color="#1a251d" />
          </view>
          <text class="empty-card__title">当前问卷暂无题目</text>
          <text class="empty-card__desc">请稍后再试，或联系管理员检查问卷配置。</text>
          <view class="hc-pill-button-soft empty-card__action" @click="goHome">返回问卷首页</view>
        </view>
      </template>
    </view>

    <view v-if="questions.length && !loading && !isPopupOpen" class="hc-fab">
      <view class="hc-pill-button-dark hc-interactive-pill" @click="submit">
        {{ submitting ? "提交中..." : "提交测评" }}
      </view>
    </view>
    <hc-dialog ref="dialogRef" @popup-change="onPopupChange" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const commonStore = useCommonStore();
const QuestionnaireId = ref(null);
const questions = ref([]);
const answers = ref({});
const loading = ref(false);
const submitting = ref(false);
const questionnaireTitle = ref("风险自评答题");
const questionnaireDesc = ref("");
const dialogRef = ref(null);
const activePopupCount = ref(0);

const ensureArray = (val) => (Array.isArray(val) ? val : val != null ? [val] : []);
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });
const onPopupChange = (show) => {
  activePopupCount.value = Math.max(0, activePopupCount.value + (show ? 1 : -1));
};

const getAnswerArray = (questionId) => {
  const arr = answers.value[questionId] || [];
  return ensureArray(arr).filter((item) => String(item || "").trim() !== "");
};

const answeredCount = computed(() => {
  return questions.value.filter((q) => getAnswerArray(q.Id).length > 0).length;
});

const requiredCount = computed(() => {
  return questions.value.filter((q) => Number(q.IsRequired) === 1).length;
});
const isPopupOpen = computed(() => activePopupCount.value > 0);

const progressPercent = computed(() => {
  if (!questions.value.length) return 0;
  return Math.round((answeredCount.value / questions.value.length) * 100);
});

const loadDetail = async () => {
  if (!QuestionnaireId.value) return;
  loading.value = true;
  try {
    const { Data, Success, Msg } = await Post("/Front/Questionnaire/Detail", {
      QuestionnaireId: QuestionnaireId.value,
    });
    if (!Success) {
      uni.showToast({ icon: "none", title: Msg || "加载问卷失败" });
      questions.value = [];
      return;
    }

    const detail = Data || {};
    const questionList = (detail && detail.Questions) || [];
    questions.value = questionList;
    questionnaireTitle.value =
      detail.QuestionnaireTitle ||
      detail.Title ||
      detail.Name ||
      questionList[0]?.QuestionnaireTitle ||
      "风险自评答题";
    questionnaireDesc.value =
      detail.QuestionnaireDesc ||
      detail.Description ||
      detail.Desc ||
      "";
  } catch (error) {
    console.error("加载问卷失败", error);
    questions.value = [];
    uni.showToast({ icon: "none", title: "加载问卷失败" });
  } finally {
    loading.value = false;
  }
};

const onSingleChange = (qid, value) => {
  answers.value = { ...answers.value, [qid]: value ? [value] : [] };
};

const onMultiChange = (qid, values) => {
  answers.value = { ...answers.value, [qid]: ensureArray(values) };
};

const onTextChange = (qid, value) => {
  answers.value = { ...answers.value, [qid]: value ? [value] : [] };
};

const isChecked = (qid, value) => {
  return getAnswerArray(qid).includes(value);
};

const getTextAnswer = (qid) => {
  const arr = getAnswerArray(qid);
  return arr.length ? arr[0] : "";
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

const confirmSubmit = async () => {
  const result = await showDialog({
    title: "确认提交",
    content: "测评结果会同步到健康档案并影响提醒推荐，是否继续？",
  });
  return !!result.confirm;
};

const submit = async () => {
  if (submitting.value) return;
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录" });
    return;
  }
  if (!questions.value.length) {
    uni.showToast({ icon: "none", title: "当前问卷暂无题目" });
    return;
  }

  for (const question of questions.value) {
    if (Number(question.IsRequired) === 1) {
      const arr = getAnswerArray(question.Id);
      if (!arr.length) {
        const index = questions.value.indexOf(question) + 1;
        uni.showToast({ icon: "none", title: `请先完成第 ${index} 题` });
        return;
      }
    }
  }

  const confirmed = await confirmSubmit();
  if (!confirmed) return;

  const payloadAnswers = Object.keys(answers.value).map((key) => ({
    QuestionId: Number(key),
    AnswerValues: ensureArray(answers.value[key]).filter((item) => String(item || "").trim() !== ""),
  }));

  submitting.value = true;
  try {
    const { Data, Success, Msg } = await Post("/Front/Questionnaire/Submit", {
      QuestionnaireId: QuestionnaireId.value,
      Answers: payloadAnswers,
    });
    if (!Success) {
      uni.showToast({ icon: "none", title: Msg || "提交失败" });
      return;
    }
    uni.navigateTo({
      url: `/pages/Front/QuestionnaireResult?Score=${Data.Score}&Level=${Data.RiskAssessmentLevel}&Summary=${encodeURIComponent(
        Data.ResultSummary || "",
      )}`,
    });
  } catch (error) {
    console.error("提交失败", error);
    uni.showToast({ icon: "none", title: "提交失败，请稍后重试" });
  } finally {
    submitting.value = false;
  }
};

onLoad((query) => {
  QuestionnaireId.value = query && query.Id ? Number(query.Id) : null;
  if (QuestionnaireId.value) {
    loadDetail();
  } else {
    loading.value = false;
    questions.value = [];
  }
});
</script>

<style scoped lang="scss">
.questionnaire-answer-screen {
  gap: 24upx;
  padding-bottom: calc(env(safe-area-inset-bottom) + 210upx);
}

.answer-head {
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
  background: rgba(248, 252, 239, 0.74);
  border: 1upx solid rgba(199, 218, 141, 0.74);
}

.summary-card__metric--dark {
  background: linear-gradient(135deg, #101612 0%, #1d271f 100%);
}

.summary-card__progress-row {
  margin-top: 18upx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.summary-card__progress-label {
  font-size: 22upx;
  color: #455442;
}

.summary-card__progress-value {
  font-size: 24upx;
  font-weight: 800;
  color: #1c2a1d;
}

.summary-card__tips {
  margin-top: 16upx;
  display: flex;
  flex-wrap: wrap;
  gap: 10upx;
}

.summary-card__tip {
  min-height: 46upx;
  padding: 0 16upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(248, 252, 239, 0.74);
  border: 1upx solid rgba(183, 207, 118, 0.72);
  color: #36502e;
  font-size: 20upx;
  font-weight: 700;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.question-card {
  padding: 24upx;
}

.question-card__head {
  display: flex;
  align-items: flex-start;
  gap: 14upx;
}

.question-card__index {
  width: 64upx;
  height: 64upx;
  border-radius: 22upx;
  background: #111713;
  color: #f7ffdf;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26upx;
  font-weight: 800;
  flex-shrink: 0;
}

.question-card__meta {
  flex: 1;
  min-width: 0;
}

.question-card__title {
  display: block;
  font-size: 30upx;
  line-height: 1.45;
  font-weight: 800;
  color: #162118;
}

.question-card__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.6;
  color: #60705f;
}

.question-card__tag {
  min-height: 46upx;
  padding: 0 16upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(202, 220, 147, 0.86);
  color: #667665;
  font-size: 20upx;
  font-weight: 800;
  flex-shrink: 0;
}

.question-card__tag--required {
  background: rgba(251, 238, 232, 0.8);
  border-color: rgba(212, 124, 101, 0.72);
  color: #8a483c;
}

.question-options {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.question-option {
  position: relative;
  min-height: 88upx;
  border-radius: 28upx;
  padding: 18upx 18upx 18upx 74upx;
  background: rgba(248, 252, 239, 0.72);
  border: 1upx solid rgba(205, 224, 145, 0.84);
  display: flex;
  align-items: center;
}

.question-option.is-selected {
  background: linear-gradient(135deg, #e0f59b 0%, #cdef75 100%);
  border-color: rgba(152, 193, 74, 0.62);
  box-shadow: 0 10upx 24upx rgba(103, 140, 57, 0.14);
}

.question-option__native {
  position: absolute;
  opacity: 0;
  width: 2upx;
  height: 2upx;
  left: 0;
  top: 0;
}

.question-option__mark {
  position: absolute;
  left: 18upx;
  top: 50%;
  width: 34upx;
  height: 34upx;
  margin-top: -17upx;
  border-radius: 50%;
  border: 3upx solid rgba(138, 160, 121, 0.86);
  background: rgba(255, 255, 255, 0.72);
}

.question-option.is-selected .question-option__mark {
  border-color: #30502d;
  background: #30502d;
  box-shadow: inset 0 0 0 8upx #eef8d6;
}

.question-option__mark--checkbox {
  border-radius: 12upx;
}

.question-option.is-selected .question-option__mark--checkbox {
  box-shadow: inset 0 0 0 7upx #eef8d6;
}

.question-option__body {
  flex: 1;
  min-width: 0;
}

.question-option__text {
  font-size: 26upx;
  line-height: 1.58;
  color: #223025;
  font-weight: 700;
}

.question-textarea-wrap {
  margin-top: 18upx;
}

.question-textarea {
  width: 100%;
  min-height: 180upx;
  border-radius: 30upx;
  padding: 22upx 24upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.88);
  font-size: 26upx;
  line-height: 1.7;
  color: #1e2c20;
  box-sizing: border-box;
}

.question-unsupported {
  margin-top: 18upx;
  padding: 18upx 20upx;
  border-radius: 24upx;
  background: rgba(252, 241, 236, 0.82);
  border: 1upx solid rgba(216, 134, 114, 0.64);
  font-size: 22upx;
  line-height: 1.6;
  color: #7a4d43;
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
  animation: answerSpin 0.9s linear infinite;
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

@keyframes answerSpin {
  to {
    transform: rotate(360deg);
  }
}
</style>
