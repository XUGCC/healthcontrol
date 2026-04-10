<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack questionnaire-screen">
      <hc-topbar :title="'\u98ce\u9669\u95ee\u5377'" :subtitle="'\u9636\u6bb5\u76ee\u6807\u4e0e\u7edf\u8ba1\u603b\u89c8'" />

      <view class="hc-card-dark questionnaire-hero">
        <view class="hc-kicker hc-kicker--dark">{{ '\u98ce\u9669\u8bc4\u5206' }}</view>
        <text class="questionnaire-hero__title">
          {{ latest ? `\u6700\u8fd1\u4e00\u6b21\u8bc4\u5206 ${latest.Score}` : '\u8fd8\u6ca1\u6709\u95ee\u5377\u7ed3\u679c' }}
        </text>
        <view class="questionnaire-hero__stats">
          <view class="questionnaire-score-pill">
            <text class="questionnaire-score-pill__label">{{ '\u98ce\u9669\u7b49\u7ea7' }}</text>
            <text class="questionnaire-score-pill__value" :class="levelClass(latest?.RiskAssessmentLevel)">
              {{ latest ? levelText(latest.RiskAssessmentLevel) : '\u5f85\u5f00\u59cb' }}
            </text>
          </view>
          <view class="questionnaire-score-pill questionnaire-score-pill--soft">
            <text class="questionnaire-score-pill__label">{{ '\u5f53\u524d\u95ee\u5377' }}</text>
            <text class="questionnaire-score-pill__value questionnaire-score-pill__value--soft">
              {{ currentQuestionnaireTitle || '\u6682\u65e0' }}
            </text>
          </view>
        </view>
      </view>

      <view class="hc-card-lime questionnaire-summary">
        <view class="questionnaire-summary__grid">
          <view>
            <text class="hc-stat-value">{{ latest ? latest.Score : 0 }}</text>
            <text class="hc-stat-label">{{ '\u9636\u6bb5\u5f97\u5206' }}</text>
          </view>
          <view class="questionnaire-summary__dark">
            <text class="hc-stat-value hc-stat-value--dark">{{ list.length }}</text>
            <text class="hc-stat-label">{{ '\u53ef\u7528\u95ee\u5377' }}</text>
          </view>
        </view>
        <text v-if="latest" class="questionnaire-summary__time">{{ '\u6700\u8fd1\u5b8c\u6210\u4e8e' }} {{ latest.AnswerTime }}</text>
      </view>

      <view class="questionnaire-actions">
        <view class="hc-pill-button-dark" @click="startCurrent">
          {{ latest ? '\u91cd\u65b0\u6d4b\u8bc4\u4e00\u6b21' : '\u5f00\u59cb\u6d4b\u8bc4' }}
        </view>
        <view v-if="latest" class="hc-pill-button-soft" @click="goHistory">{{ '\u67e5\u770b\u5386\u53f2\u8bb0\u5f55' }}</view>
      </view>

      <view class="hc-card-soft questionnaire-list-card">
        <view class="hc-section-head">
          <text class="hc-section-head__title">{{ '\u95ee\u5377\u5217\u8868' }}</text>
          <text class="hc-section-head__meta">{{ '\u9009\u62e9\u540e\u76f4\u63a5\u8fdb\u5165' }}</text>
        </view>

        <view v-if="list.length" class="questionnaire-list">
          <view
            v-for="(q, index) in list"
            :key="q.Id"
            class="questionnaire-item"
            :class="{ 'questionnaire-item--active': q.Id === currentQuestionnaireId }"
            @click="selectAndStart(q)"
          >
            <view class="questionnaire-item__index">
              {{ q.Id === currentQuestionnaireId ? '\u8fdb\u5165' : `0${index + 1}` }}
            </view>
            <view class="questionnaire-item__main">
              <text class="questionnaire-item__title">{{ q.QuestionnaireTitle }}</text>
              <text class="questionnaire-item__desc">{{ q.QuestionnaireDesc }}</text>
              <text class="questionnaire-item__meta">{{ '\u9898\u76ee\u6570' }} {{ q.QuestionCount || 5 }}</text>
            </view>
          </view>
        </view>
        <view v-else class="questionnaire-empty">
          <text class="questionnaire-empty__text">{{ '\u6682\u65e0\u53ef\u7528\u95ee\u5377' }}</text>
        </view>
      </view>
    </view>

    <footer-bar />
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import FooterBar from "@/components/footer-bar/footer-bar.vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();
const list = ref([]);
const latest = ref(null);
const defaultQuestionnaireId = ref(null);
const currentQuestionnaireId = ref(null);
const currentQuestionnaireTitle = ref("");

const load = async () => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "\u8bf7\u5148\u767b\u5f55" });
    return;
  }
  const latestResponse = await Post("/Front/Questionnaire/LatestResult", {});
  latest.value = (latestResponse.Success && latestResponse.Data) || null;

  const listResponse = await Post("/Front/Questionnaire/List", {});
  if (listResponse.Success) {
    list.value = (listResponse.Data && listResponse.Data.Items) || [];
    if (list.value.length) {
      defaultQuestionnaireId.value = list.value[0].Id;
      let target = list.value[0];
      if (latest.value && latest.value.QuestionnaireId) {
        const hit = list.value.find((item) => item.Id === latest.value.QuestionnaireId);
        if (hit) target = hit;
      }
      currentQuestionnaireId.value = target.Id;
      currentQuestionnaireTitle.value = target.QuestionnaireTitle;
    } else {
      defaultQuestionnaireId.value = null;
      currentQuestionnaireId.value = null;
      currentQuestionnaireTitle.value = "";
    }
  }
};

const startCurrent = () => {
  const id = currentQuestionnaireId.value || defaultQuestionnaireId.value;
  if (!id) {
    uni.showToast({ icon: "none", title: "\u6682\u65e0\u53ef\u7528\u95ee\u5377" });
    return;
  }
  uni.navigateTo({ url: `/pages/Front/QuestionnaireAnswer?Id=${id}` });
};

const selectAndStart = (questionnaire) => {
  currentQuestionnaireId.value = questionnaire.Id;
  currentQuestionnaireTitle.value = questionnaire.QuestionnaireTitle || "";
  uni.navigateTo({ url: `/pages/Front/QuestionnaireAnswer?Id=${questionnaire.Id}` });
};

const goHistory = () => {
  uni.navigateTo({ url: "/pages/Front/QuestionnaireHistory" });
};

const levelText = (value) => {
  if (value === 2) return "\u9ad8\u98ce\u9669";
  if (value === 1) return "\u4e2d\u98ce\u9669";
  if (value === 0) return "\u4f4e\u98ce\u9669";
  return "\u5f85\u5f00\u59cb";
};

const levelClass = (value) => {
  if (value === 2) return "level-high";
  if (value === 1) return "level-medium";
  if (value === 0) return "level-low";
  return "";
};

onShow(load);
</script>

<style scoped lang="scss">
.questionnaire-screen {
  gap: 18upx;
}

.questionnaire-hero__title {
  display: block;
  margin-top: 16upx;
  font-size: 50upx;
  line-height: 1.08;
  font-weight: 800;
  color: #ffffff;
}

.questionnaire-hero__stats {
  margin-top: 20upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.questionnaire-score-pill {
  border-radius: 30upx;
  padding: 20upx;
  background: rgba(225, 243, 154, 0.14);
  border: 1upx solid rgba(183, 232, 99, 0.2);
}

.questionnaire-score-pill--soft {
  background: rgba(248, 252, 239, 0.08);
}

.questionnaire-score-pill__label {
  display: block;
  font-size: 20upx;
  color: rgba(247, 255, 223, 0.68);
}

.questionnaire-score-pill__value {
  display: block;
  margin-top: 10upx;
  font-size: 28upx;
  font-weight: 800;
  color: #ffffff;
}

.questionnaire-score-pill__value--soft {
  font-size: 24upx;
}

.level-low {
  color: #d7f494;
}

.level-medium {
  color: #ffd181;
}

.level-high {
  color: #ffb7a0;
}

.questionnaire-summary__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12upx;
}

.questionnaire-summary__dark {
  border-radius: 30upx;
  padding: 22upx;
  background: linear-gradient(135deg, #101612 0%, #1d271f 100%);
}

.questionnaire-summary__time {
  display: block;
  margin-top: 16upx;
  font-size: 22upx;
  color: #3d4c39;
}

.questionnaire-actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.questionnaire-list {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.questionnaire-item {
  padding: 18upx;
  border-radius: 32upx;
  background: rgba(248, 252, 239, 0.86);
  border: 1upx solid rgba(205, 224, 145, 0.88);
  display: flex;
  gap: 16upx;
}

.questionnaire-item--active {
  background: linear-gradient(135deg, #e0f59b 0%, #cdef75 100%);
  border-color: rgba(156, 195, 77, 0.5);
}

.questionnaire-item__index {
  width: 72upx;
  height: 72upx;
  border-radius: 24upx;
  background: #111713;
  color: #f7ffdf;
  font-size: 24upx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.questionnaire-item__main {
  flex: 1;
  min-width: 0;
}

.questionnaire-item__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: var(--text-color);
}

.questionnaire-item__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.6;
  color: var(--text-color-light);
}

.questionnaire-item__meta {
  display: block;
  margin-top: 10upx;
  font-size: 20upx;
  color: var(--text-color-lighter);
}

.questionnaire-empty {
  margin-top: 18upx;
  min-height: 180upx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.questionnaire-empty__text {
  font-size: 24upx;
  color: var(--text-color-lighter);
}
</style>
