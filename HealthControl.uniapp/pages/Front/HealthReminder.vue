<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack reminder-screen">
      <hc-topbar
        title="健康提醒"
        subtitle="安排自查、护嗓和复查节奏"
        :show-back="true"
        @left="goBack"
      />

      <view class="hc-card-dark reminder-hero">
        <view class="hc-row hc-space-between reminder-hero__top">
          <view class="hc-kicker hc-kicker--dark">{{ heroKicker }}</view>
          <view class="reminder-hero__risk" :class="riskToneClass">{{ riskLabel }}</view>
        </view>
        <text class="reminder-hero__title">把提醒整理成一条清晰的日常时间线</text>
        <text class="reminder-hero__desc">{{ heroDescription }}</text>
        <view class="reminder-hero__grid">
          <view class="reminder-hero__metric">
            <text class="reminder-hero__metric-label">已开启</text>
            <text class="reminder-hero__metric-value">{{ activeCount }}/3</text>
          </view>
          <view class="reminder-hero__metric">
            <text class="reminder-hero__metric-label">定期自查</text>
            <text class="reminder-hero__metric-value reminder-hero__metric-value--small">{{ checkupStatusText }}</text>
          </view>
          <view class="reminder-hero__metric">
            <text class="reminder-hero__metric-label">护嗓习惯</text>
            <text class="reminder-hero__metric-value reminder-hero__metric-value--small">{{ voiceCareStatusText }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft reminder-card">
        <view class="reminder-card__head">
          <view class="reminder-card__intro">
            <text class="reminder-card__title">定期自查提醒</text>
            <text class="reminder-card__desc">根据当前风险等级安排合适的自查频率，保持固定回看节奏。</text>
          </view>
          <hc-toggle v-model="checkupEnabled" />
        </view>

        <view v-if="checkupEnabled" class="reminder-card__body">
          <picker mode="selector" :range="checkupFrequencyLabels" :value="checkupFrequencyIndex" @change="onCheckupFrequencyChange">
            <view class="reminder-field hc-interactive-card">
              <view class="reminder-field__copy">
                <text class="reminder-field__label">提醒频率</text>
                <text class="reminder-field__hint">可根据近期状态随时调整</text>
              </view>
              <view class="reminder-field__value-wrap">
                <text class="reminder-field__value">{{ checkupFrequencyLabel }}</text>
                <uni-icons type="right" size="18" color="#72836f" />
              </view>
            </view>
          </picker>

          <picker mode="time" :value="checkupTime" @change="onCheckupTimeChange">
            <view class="reminder-field hc-interactive-card">
              <view class="reminder-field__copy">
                <text class="reminder-field__label">提醒时间</text>
                <text class="reminder-field__hint">建议固定在最容易完成自查的时段</text>
              </view>
              <view class="reminder-field__value-wrap">
                <view class="reminder-time-chip">
                  <uni-icons type="calendar" size="18" color="#64745f" />
                  <text class="reminder-time-chip__text">{{ checkupTime }}</text>
                </view>
                <uni-icons type="right" size="18" color="#72836f" />
              </view>
            </view>
          </picker>

          <view class="reminder-note">
            <text class="reminder-note__title">当前建议</text>
            <text class="reminder-note__text">{{ checkupRecommendText }}</text>
          </view>
        </view>

        <view v-else class="reminder-card__off">
          <text class="reminder-card__off-text">关闭后将不再推送定期自查提醒，但历史记录仍会保留。</text>
        </view>
      </view>

      <view class="hc-card-soft reminder-card">
        <view class="reminder-card__head">
          <view class="reminder-card__intro">
            <text class="reminder-card__title">护嗓习惯提醒</text>
            <text class="reminder-card__desc">每天保留一个低打扰时点，提醒补水和减少用嗓负担。</text>
          </view>
          <hc-toggle v-model="voiceCareEnabled" />
        </view>

        <view v-if="voiceCareEnabled" class="reminder-card__body">
          <picker mode="time" :value="voiceCareTime" @change="onVoiceCareTimeChange">
            <view class="reminder-field hc-interactive-card">
              <view class="reminder-field__copy">
                <text class="reminder-field__label">提醒时间</text>
                <text class="reminder-field__hint">选择一个不容易被会议或课程打断的时间点</text>
              </view>
              <view class="reminder-field__value-wrap">
                <view class="reminder-time-chip">
                  <uni-icons type="calendar" size="18" color="#64745f" />
                  <text class="reminder-time-chip__text">{{ voiceCareTime }}</text>
                </view>
                <uni-icons type="right" size="18" color="#72836f" />
              </view>
            </view>
          </picker>

          <view class="reminder-note reminder-note--soft">
            <text class="reminder-note__title">今日策略</text>
            <text class="reminder-note__text">如果今天已经连续高强度用嗓，可以保留提醒，但适当提前到更容易休息的时间。</text>
          </view>

          <view class="hc-pill-button-soft reminder-snooze" @click="snoozeToday">今天不再提醒</view>
        </view>

        <view v-else class="reminder-card__off">
          <text class="reminder-card__off-text">关闭后将不会收到护嗓习惯提醒。</text>
        </view>
      </view>

      <view class="hc-card-soft reminder-card">
        <view class="reminder-card__head">
          <view class="reminder-card__intro">
            <text class="reminder-card__title">复查提醒</text>
            <text class="reminder-card__desc">系统会结合风险等级、近期筛查和症状变化提示下一步动作。</text>
          </view>
          <hc-toggle v-model="followupEnabled" />
        </view>

        <view v-if="followupEnabled" class="reminder-card__body">
          <view class="reminder-followup">
            <view class="reminder-followup__badge" :class="riskToneClass">{{ riskLabel }}</view>
            <view class="reminder-followup__main">
              <text class="reminder-followup__title">动态触发提醒</text>
              <text class="reminder-followup__desc">{{ followupDescription }}</text>
            </view>
          </view>

          <view class="reminder-steps">
            <view class="reminder-step">
              <text class="reminder-step__index">01</text>
              <text class="reminder-step__text">当风险升高、症状持续或检查结果需要回看时，系统会优先提醒复查。</text>
            </view>
            <view class="reminder-step">
              <text class="reminder-step__index">02</text>
              <text class="reminder-step__text">提醒历史会保存在消息列表，方便你后续统一查看。</text>
            </view>
          </view>
        </view>

        <view v-else class="reminder-card__off">
          <text class="reminder-card__off-text">关闭后，系统不会主动推送复查节奏建议。</text>
        </view>
      </view>

      <view class="reminder-actions">
        <view class="hc-pill-button" @click="saveAll">保存设置</view>
        <view class="hc-pill-button-soft" @click="goToMessages">提醒历史</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onPullDownRefresh } from "@dcloudio/uni-app";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";
import HcToggle from "@/components/hc-toggle/hc-toggle.vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();
const UserId = computed(() => commonStore.UserId);

const REMIND_TYPE_CHECKUP = 0;
const REMIND_TYPE_VOICE = 1;
const REMIND_TYPE_FOLLOWUP = 4;

const checkupId = ref(null);
const checkupEnabled = ref(true);
const checkupTime = ref("09:00");
const checkupFrequencyIndex = ref(1);

const voiceId = ref(null);
const voiceCareEnabled = ref(true);
const voiceCareTime = ref("21:00");

const followupId = ref(null);
const followupEnabled = ref(true);
const riskLevel = ref(null);

const checkupFrequencyOptions = [
  { value: 0, label: "每天一次" },
  { value: 1, label: "每周 1-2 次" },
  { value: 2, label: "每周一次" },
  { value: 3, label: "每 30 天一次" },
];

const checkupFrequencyLabels = checkupFrequencyOptions.map((item) => item.label);

const activeCount = computed(() => [checkupEnabled.value, voiceCareEnabled.value, followupEnabled.value].filter(Boolean).length);
const heroKicker = computed(() => (activeCount.value === 3 ? "提醒计划已就绪" : "还有提醒处于关闭状态"));
const heroDescription = computed(() => {
  if (activeCount.value === 3) {
    return `当前三类提醒都已开启，自查 ${checkupTime.value}、护嗓 ${voiceCareTime.value}，可按固定节奏执行。`;
  }
  return `当前已开启 ${activeCount.value} 项提醒，建议至少保留自查或护嗓提醒中的一项。`;
});

const riskLabel = computed(() => {
  if (riskLevel.value === 2) return "高风险";
  if (riskLevel.value === 1) return "中风险";
  if (riskLevel.value === 0) return "低风险";
  return "待评估";
});

const riskToneClass = computed(() => {
  if (riskLevel.value === 2) return "is-high";
  if (riskLevel.value === 1) return "is-medium";
  return "is-low";
});

const checkupFrequencyLabel = computed(() => checkupFrequencyOptions[checkupFrequencyIndex.value]?.label || "请选择频率");

const checkupRecommendText = computed(() => {
  if (riskLevel.value === 2) return "当前为高风险，建议保持高频自查，并结合线下复查同步跟进。";
  if (riskLevel.value === 1) return "当前为中风险，建议每周固定安排一次回看，避免长时间中断。";
  if (riskLevel.value === 0) return "当前为低风险，保持较轻的自查频率即可，重点是持续执行。";
  return "建议先完成一次筛查评估，再根据风险结果调整提醒节奏。";
});

const followupDescription = computed(() => {
  if (riskLevel.value === 2) return "当前风险较高，建议保留复查提醒，出现新症状时尽快跟进。";
  if (riskLevel.value === 1) return "当前风险中等，建议持续观察症状变化，并按阶段复查。";
  if (riskLevel.value === 0) return "当前风险较低，复查提醒会在结果变化或症状波动时再主动出现。";
  return "完成一次有效评估后，系统会根据风险结果自动调整复查提醒节奏。";
});

const checkupStatusText = computed(() => (checkupEnabled.value ? `${checkupFrequencyLabel.value} · ${checkupTime.value}` : "已关闭"));
const voiceCareStatusText = computed(() => (voiceCareEnabled.value ? `每天 · ${voiceCareTime.value}` : "已关闭"));

const padTimeUnit = (value) => String(value).padStart(2, "0");

const normalizeTimeValue = (value, fallback = "09:00") => {
  if (typeof value !== "string") return fallback;
  const trimmed = value.trim();
  if (!trimmed) return fallback;

  const matched = trimmed.match(/(\d{1,2}):(\d{2})/);
  if (matched) {
    const hours = Number(matched[1]);
    const minutes = Number(matched[2]);
    if (Number.isFinite(hours) && Number.isFinite(minutes) && hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
      return `${padTimeUnit(hours)}:${padTimeUnit(minutes)}`;
    }
  }

  const parsed = new Date(trimmed);
  if (!Number.isNaN(parsed.getTime())) {
    return `${padTimeUnit(parsed.getHours())}:${padTimeUnit(parsed.getMinutes())}`;
  }

  return fallback;
};

const goBack = () => {
  uni.navigateBack();
};

const onCheckupFrequencyChange = (event) => {
  const nextIndex = Number(event?.detail?.value ?? checkupFrequencyIndex.value);
  if (!Number.isNaN(nextIndex) && checkupFrequencyOptions[nextIndex]) {
    checkupFrequencyIndex.value = nextIndex;
  }
};

const onCheckupTimeChange = (event) => {
  checkupTime.value = normalizeTimeValue(event?.detail?.value, checkupTime.value);
};

const onVoiceCareTimeChange = (event) => {
  voiceCareTime.value = normalizeTimeValue(event?.detail?.value, voiceCareTime.value);
};

const loadRiskAndDefaults = async () => {
  try {
    const { Data, Success } = await Post("/TPersonalLaryngealHealthRecord/Get", {
      UserId: UserId.value,
      Page: 1,
      Limit: 1,
    });
    if (Success && Data) {
      riskLevel.value = Data.RiskAssessmentLevel;
      if (riskLevel.value === 2) checkupFrequencyIndex.value = 0;
      else if (riskLevel.value === 1) checkupFrequencyIndex.value = 2;
      else checkupFrequencyIndex.value = 3;
    }
  } catch (error) {
    console.error("Failed to load risk level", error);
  }
};

const mapRecordToState = (item) => {
  const type = item.RemindType;
  if (type === REMIND_TYPE_CHECKUP) {
    checkupId.value = item.Id;
    checkupEnabled.value = item.RemindStatus === 1 || item.RemindStatus === true;
    if (item.RemindTime) checkupTime.value = normalizeTimeValue(item.RemindTime, checkupTime.value);
    if (item.RepeatFrequency !== null && item.RepeatFrequency !== undefined) {
      const index = checkupFrequencyOptions.findIndex((option) => option.value === item.RepeatFrequency);
      if (index >= 0) checkupFrequencyIndex.value = index;
    }
    return;
  }

  if (type === REMIND_TYPE_VOICE) {
    voiceId.value = item.Id;
    voiceCareEnabled.value = item.RemindStatus === 1 || item.RemindStatus === true;
    if (item.RemindTime) voiceCareTime.value = normalizeTimeValue(item.RemindTime, voiceCareTime.value);
    return;
  }

  if (type === REMIND_TYPE_FOLLOWUP) {
    followupId.value = item.Id;
    followupEnabled.value = item.RemindStatus === 1 || item.RemindStatus === true;
  }
};

const loadConfig = async () => {
  if (!UserId.value) return;
  try {
    const { Data, Success } = await Post("/TPersonalHealthRemind/List", {
      UserId: UserId.value,
      Page: 1,
      Limit: 50,
    });
    if (Success && Data && Array.isArray(Data.Items)) {
      Data.Items.forEach(mapRecordToState);
    }
  } catch (error) {
    console.error("Failed to load reminder config", error);
  }
};

const buildCheckupDto = () => ({
  Id: checkupId.value,
  UserId: UserId.value,
  RemindType: REMIND_TYPE_CHECKUP,
  RemindTime: normalizeTimeValue(checkupTime.value, "09:00"),
  RepeatFrequency: checkupFrequencyOptions[checkupFrequencyIndex.value]?.value ?? 1,
  RemindStatus: checkupEnabled.value ? 1 : 0,
  RemindContent: "定期自查提醒",
});

const buildVoiceDto = () => ({
  Id: voiceId.value,
  UserId: UserId.value,
  RemindType: REMIND_TYPE_VOICE,
  RemindTime: normalizeTimeValue(voiceCareTime.value, "21:00"),
  RepeatFrequency: 0,
  RemindStatus: voiceCareEnabled.value ? 1 : 0,
  RemindContent: "护嗓提醒",
});

const buildFollowupDto = () => ({
  Id: followupId.value,
  UserId: UserId.value,
  RemindType: REMIND_TYPE_FOLLOWUP,
  RemindTime: "09:00",
  RepeatFrequency: 2,
  RemindStatus: followupEnabled.value ? 1 : 0,
  RemindContent: "复查提醒",
});

const saveAll = async () => {
  if (!UserId.value) {
    uni.showToast({ title: "请先登录", icon: "none" });
    return;
  }

  uni.showLoading({ title: "保存中...", mask: true });
  try {
    const payloads = [buildCheckupDto(), buildVoiceDto(), buildFollowupDto()];
    for (const dto of payloads) {
      await Post("/TPersonalHealthRemind/CreateOrEdit", dto);
    }
    uni.showToast({ title: "已保存", icon: "success" });
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "保存失败", icon: "error" });
  } finally {
    uni.hideLoading();
  }
};

const snoozeToday = async () => {
  if (!UserId.value) return;

  try {
    uni.showLoading({ title: "处理中..." });
    const { Success } = await Post("/TPersonalHealthRemind/SnoozeToday", {
      UserId: UserId.value,
      RemindType: REMIND_TYPE_VOICE,
    });
    if (Success) {
      uni.showToast({ title: "今天不再提醒", icon: "success" });
    }
  } catch (error) {
    console.error(error);
    uni.showToast({ title: "操作失败", icon: "error" });
  } finally {
    uni.hideLoading();
  }
};

const goToMessages = () => {
  uni.navigateTo({ url: "/pages/Front/ReminderMessageList" });
};

const initPage = async () => {
  if (!commonStore.CheckIsLogin()) return;
  await loadRiskAndDefaults();
  await loadConfig();
};

onLoad(async () => {
  await initPage();
});

onPullDownRefresh(async () => {
  try {
    await initPage();
  } finally {
    uni.stopPullDownRefresh();
  }
});
</script>

<style scoped lang="scss">
.reminder-screen {
  padding-bottom: calc(env(safe-area-inset-bottom) + 180upx);
}

.reminder-hero {
  gap: 0;
}

.reminder-hero__top {
  align-items: center;
}

.reminder-hero__risk {
  min-height: 48upx;
  padding: 0 18upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
  border: 1upx solid rgba(225, 243, 154, 0.24);
  color: #eff9c8;
  background: rgba(225, 243, 154, 0.12);
}

.reminder-hero__risk.is-medium {
  background: rgba(243, 182, 65, 0.14);
  border-color: rgba(243, 182, 65, 0.22);
  color: #ffe3a5;
}

.reminder-hero__risk.is-high {
  background: rgba(234, 117, 81, 0.16);
  border-color: rgba(234, 117, 81, 0.24);
  color: #ffd2c5;
}

.reminder-hero__title {
  display: block;
  margin-top: 18upx;
  font-size: 48upx;
  line-height: 1.14;
  font-weight: 800;
  color: #ffffff;
}

.reminder-hero__desc {
  display: block;
  margin-top: 14upx;
  font-size: 24upx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.72);
}

.reminder-hero__grid {
  margin-top: 24upx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14upx;
}

.reminder-hero__metric {
  min-height: 156upx;
  padding: 22upx 20upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.08);
  border: 1upx solid rgba(225, 243, 154, 0.14);
}

.reminder-hero__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(255, 255, 255, 0.7);
}

.reminder-hero__metric-value {
  display: block;
  margin-top: 12upx;
  font-size: 38upx;
  line-height: 1.25;
  font-weight: 800;
  color: #ffffff;
}

.reminder-hero__metric-value--small {
  font-size: 26upx;
  line-height: 1.45;
}

.reminder-card {
  display: flex;
  flex-direction: column;
  gap: 22upx;
}

.reminder-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18upx;
}

.reminder-card__intro {
  flex: 1;
  min-width: 0;
}

.reminder-card__title {
  display: block;
  font-size: 34upx;
  font-weight: 800;
  color: var(--text-color);
}

.reminder-card__desc {
  display: block;
  margin-top: 10upx;
  font-size: 23upx;
  line-height: 1.7;
  color: var(--text-color-light);
}

.reminder-card__body {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.reminder-card__off {
  padding: 20upx 22upx;
  border-radius: 28upx;
  background: rgba(243, 248, 230, 0.84);
  border: 1upx dashed rgba(205, 224, 145, 0.92);
}

.reminder-card__off-text {
  font-size: 22upx;
  line-height: 1.7;
  color: var(--text-color-light);
}

.reminder-field {
  min-height: 112upx;
  padding: 22upx 24upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18upx;
}

.reminder-field__copy {
  flex: 1;
  min-width: 0;
}

.reminder-field__label {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: var(--text-color);
}

.reminder-field__hint {
  display: block;
  margin-top: 8upx;
  font-size: 20upx;
  line-height: 1.55;
  color: var(--text-color-lighter);
}

.reminder-field__value-wrap {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14upx;
  flex-shrink: 0;
}

.reminder-field__value {
  max-width: 260upx;
  font-size: 26upx;
  font-weight: 800;
  color: var(--text-color);
  text-align: right;
}

.reminder-time-chip {
  min-height: 64upx;
  padding: 0 18upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  gap: 10upx;
  background: rgba(223, 242, 168, 0.34);
  border: 1upx solid rgba(205, 224, 145, 0.86);
}

.reminder-time-chip__text {
  font-size: 26upx;
  font-weight: 800;
  color: var(--text-color);
}

.reminder-note {
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(142, 201, 79, 0.12);
  border: 1upx solid rgba(189, 222, 121, 0.54);
}

.reminder-note--soft {
  background: rgba(228, 242, 188, 0.46);
}

.reminder-note__title {
  display: block;
  font-size: 22upx;
  font-weight: 800;
  color: #49633b;
}

.reminder-note__text {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.7;
  color: #5b6b58;
}

.reminder-snooze {
  margin-top: 2upx;
}

.reminder-followup {
  display: flex;
  align-items: flex-start;
  gap: 18upx;
  padding: 22upx 24upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.reminder-followup__badge {
  min-width: 110upx;
  min-height: 54upx;
  padding: 0 16upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
  background: rgba(142, 201, 79, 0.12);
  border: 1upx solid rgba(189, 222, 121, 0.52);
  color: #49633b;
}

.reminder-followup__badge.is-medium {
  background: rgba(243, 182, 65, 0.12);
  border-color: rgba(243, 182, 65, 0.22);
  color: #9d6b17;
}

.reminder-followup__badge.is-high {
  background: rgba(234, 117, 81, 0.12);
  border-color: rgba(234, 117, 81, 0.2);
  color: #b44f34;
}

.reminder-followup__main {
  flex: 1;
  min-width: 0;
}

.reminder-followup__title {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: var(--text-color);
}

.reminder-followup__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.7;
  color: var(--text-color-light);
}

.reminder-steps {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.reminder-step {
  display: flex;
  align-items: flex-start;
  gap: 16upx;
  padding: 16upx 18upx;
  border-radius: 24upx;
  background: rgba(243, 248, 230, 0.7);
  border: 1upx solid rgba(223, 234, 196, 0.9);
}

.reminder-step__index {
  min-width: 54upx;
  height: 54upx;
  border-radius: 18upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
  color: #416132;
  background: rgba(223, 242, 168, 0.56);
}

.reminder-step__text {
  flex: 1;
  font-size: 22upx;
  line-height: 1.65;
  color: var(--text-color-light);
}

.reminder-actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

@media (max-width: 720px) {
  .reminder-hero__grid {
    grid-template-columns: 1fr;
  }

  .reminder-card__head {
    gap: 14upx;
  }

  .reminder-field {
    align-items: flex-start;
    flex-direction: column;
  }

  .reminder-field__value-wrap {
    width: 100%;
    justify-content: space-between;
  }

  .reminder-field__value {
    max-width: none;
    text-align: left;
  }
}
</style>
