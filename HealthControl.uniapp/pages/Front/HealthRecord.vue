<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack record-screen">
      <hc-topbar
        :title="'\u5065\u5eb7\u6863\u6848'"
        :subtitle="'\u9879\u76ee\u5f0f\u98ce\u9669\uff0c\u8d8b\u52bf\u4e0e\u62a4\u7406\u8fdb\u5ea6'"
        :show-back="true"
        :right-text="'\u9996\u9875'"
        @left="goBack"
        @right="goToHome"
      />

      <view v-if="isLoading" class="hc-card-soft record-loading">
        <view class="record-loading__spinner"></view>
        <text class="record-loading__text">{{ '\u6b63\u5728\u52a0\u8f7d\u5065\u5eb7\u6863\u6848...' }}</text>
      </view>

      <template v-else-if="!healthRecord">
        <view class="hc-card-soft record-empty">
          <text class="record-empty__title">{{ '\u6682\u65e0\u5065\u5eb7\u6863\u6848' }}</text>
          <text class="record-empty__desc">{{ '\u5b8c\u6210\u7b2c\u4e00\u6b21\u97f3\u9891\u7b5b\u67e5\u540e\uff0c\u7cfb\u7edf\u4f1a\u81ea\u52a8\u751f\u6210\u4f60\u7684\u4e2a\u4eba\u6863\u6848\uff0c\u6c47\u603b\u8d8b\u52bf\uff0c\u75c7\u72b6\u548c\u63d0\u9192\u8ba1\u5212\u3002' }}</text>
          <view class="hc-pill-button-dark" @click="goToHome">{{ '\u56de\u5230\u9996\u9875\u5f00\u59cb\u81ea\u67e5' }}</view>
        </view>
      </template>

      <template v-else>
        <view class="hc-card-lime record-hero">
          <view class="record-hero__top">
            <view class="record-user">
              <image class="record-user__avatar" :src="UserInfo?.ImageUrls || defaultAvatar" mode="aspectFill" />
              <view class="record-user__meta">
                <text class="record-user__name">{{ UserInfo?.Name || '\u672a\u547d\u540d\u7528\u6237' }}</text>
                <text class="record-user__time">{{ '\u521b\u5efa\u4e8e' }} {{ formatTime(healthRecord.CreationTime) || '\u6682\u65e0' }}</text>
                <text class="record-user__time">{{ '\u6700\u8fd1\u81ea\u67e5' }} {{ formatTime(healthRecord.LastScreenTime) || '\u6682\u65e0' }}</text>
              </view>
            </view>
            <view class="record-risk" :class="riskCardClass">
              <text class="record-risk__label">{{ '\u98ce\u9669' }}</text>
              <text class="record-risk__value">{{ riskLevelText }}</text>
            </view>
          </view>

          <view class="record-hero__grid">
            <view class="record-pill">
              <text class="record-pill__label">{{ '\u8d8b\u52bf' }}</text>
              <text class="record-pill__value">{{ healthTrendText }}</text>
            </view>
            <view class="record-pill record-pill--dark">
              <text class="record-pill__label record-pill__label--dark">{{ '\u6700\u65b0\u7ed3\u679c' }}</text>
              <text class="record-pill__value record-pill__value--dark">
                {{ latestDetect ? (latestDetect.PrimaryScreenResult ? '\u5f02\u5e38' : '\u6b63\u5e38') : '\u6682\u65e0' }}
              </text>
            </view>
          </view>

          <text class="record-hero__tip">{{ riskTipText }}</text>
        </view>

        <view class="record-masonry">
          <view class="hc-card-dark record-panel">
            <view class="hc-section-head">
              <text class="hc-section-head__title record-panel__title-dark">{{ '\u7b5b\u67e5\u8d8b\u52bf' }}</text>
              <text class="hc-section-head__meta">{{ '\u6700\u8fd1' }} {{ trendData.length }} {{ '\u6b21' }}</text>
            </view>
            <view v-if="trendData.length > 0" class="record-bars">
              <view v-for="item in trendData" :key="item.Id" class="record-bars__item" @click="goToDetectDetail(item.Id)">
                <view class="record-bars__track">
                  <view
                    class="record-bars__bar"
                    :class="{ 'record-bars__bar--alert': item.PrimaryScreenResult }"
                    :style="{ height: `${Math.max(24, Math.min(96, Number((item.PrimaryScreenConfidence || 0) * 100)))}upx` }"
                  ></view>
                </view>
                <text class="record-bars__date">{{ formatDate(item.CreationTime) }}</text>
              </view>
            </view>
            <text v-else class="record-panel__empty-dark">{{ '\u6682\u65e0\u7b5b\u67e5\u8bb0\u5f55' }}</text>
          </view>

          <view class="hc-card-soft record-panel">
            <view class="hc-section-head">
              <text class="hc-section-head__title">{{ '\u8fd1\u671f\u75c7\u72b6' }}</text>
              <text class="hc-section-head__meta" @click="goToSymptomList">{{ '\u5168\u90e8\u8bb0\u5f55' }}</text>
            </view>
            <template v-if="latestSymptom">
              <view class="record-tags">
                <view class="record-tag">{{ latestSymptom.SymptomType }}</view>
                <view class="record-tag record-tag--soft">{{ getSymptomLevelText(latestSymptom.SymptomLevel) }}</view>
              </view>
              <text class="record-panel__headline">{{ latestSymptom.SymptomDuration }}</text>
              <text class="record-panel__desc">{{ '\u628a\u75c7\u72b6\u4e0e\u7b5b\u67e5\u7ed3\u679c\u653e\u5728\u540c\u4e00\u65f6\u95f4\u7ebf\u4e2d\uff0c\u65b9\u4fbf\u5224\u65ad\u662f\u5426\u9700\u8981\u540e\u7eed\u590d\u67e5\u3002' }}</text>
            </template>
            <text v-else class="record-panel__empty">{{ '\u6682\u65e0\u75c7\u72b6\u8bb0\u5f55' }}</text>
          </view>
        </view>

        <view class="hc-card-soft record-panel">
          <view class="hc-section-head">
            <text class="hc-section-head__title">{{ '\u62a4\u7406\u8fdb\u5ea6' }}</text>
            <text class="hc-section-head__meta" @click="goToHealthReminder">{{ '\u63d0\u9192\u8bbe\u7f6e' }}</text>
          </view>

          <view class="project-row">
            <view class="project-row__main">
              <text class="project-row__title">{{ '\u5b9a\u671f\u81ea\u67e5\u8ba1\u5212' }}</text>
              <text class="project-row__desc">{{ '\u7ef4\u6301\u7a33\u5b9a\u8282\u594f\uff0c\u628a\u7b5b\u67e5\u53d8\u6210\u53ef\u8ffd\u8e2a\u7684\u8fde\u7eed\u6837\u672c\u3002' }}</text>
            </view>
            <text class="project-row__meta">{{ healthRecord.LastScreenTime ? '\u5df2\u5f00\u59cb' : '\u5f85\u5f00\u59cb' }}</text>
          </view>
          <view class="hc-progress">
            <view class="hc-progress__bar" :style="{ width: healthRecord.LastScreenTime ? '78%' : '24%' }"></view>
          </view>

          <view class="project-row">
            <view class="project-row__main">
              <text class="project-row__title">{{ '\u62a4\u55d3\u4e60\u60ef' }}</text>
              <text class="project-row__desc">{{ healthRecord.DailyVoiceCare || '\u8fd8\u6ca1\u6709\u586b\u5199\u62a4\u55d3\u4e60\u60ef\uff0c\u5efa\u8bae\u8865\u5145\u3002' }}</text>
            </view>
            <text class="project-row__meta project-row__meta--link" @click="showEditVoiceCare">{{ '\u7f16\u8f91' }}</text>
          </view>

          <view
            v-if="healthRecord && (healthRecord.RiskAssessmentLevel === 1 || healthRecord.RiskAssessmentLevel === 2)"
            class="record-alert"
          >
            <text class="record-alert__title">{{ '\u5efa\u8bae\u5f00\u542f\u63d0\u9192' }}</text>
            <text class="record-alert__desc">{{ '\u5bf9\u4e2d\u9ad8\u98ce\u9669\u7528\u6237\uff0c\u7a33\u5b9a\u7684\u63d0\u9192\u7cfb\u7edf\u80fd\u8ba9\u81ea\u67e5\uff0c\u590d\u67e5\u548c\u62a4\u7406\u52a8\u4f5c\u4e32\u8d77\u6765\u3002' }}</text>
          </view>
        </view>

        <view v-if="laryngoscopeList.length > 0" class="hc-card-soft record-panel">
          <view class="hc-section-head">
            <text class="hc-section-head__title">{{ '\u5589\u955c\u5f71\u50cf' }}</text>
            <text class="hc-section-head__meta" @click="goToLaryngoscope">{{ '\u5168\u90e8\u5f71\u50cf' }}</text>
          </view>
          <view class="laryngoscope-list">
            <view
              v-for="photo in laryngoscopeList"
              :key="photo.Id"
              class="laryngoscope-item"
              @click="previewLaryngoscope(photo)"
            >
              <image class="laryngoscope-item__img" :src="photo.LaryngoscopePhotoUrl" mode="aspectFill" />
              <view class="laryngoscope-item__main">
                <text class="laryngoscope-item__title">{{ buildLaryngoscopeTitle(photo) }}</text>
                <text class="laryngoscope-item__time">{{ formatTime(photo.UploadTime) }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="record-actions">
          <view class="hc-pill-button-dark" @click="goToRecordList">{{ '\u5168\u90e8\u7b5b\u67e5\u8bb0\u5f55' }}</view>
          <view class="hc-pill-button-soft" @click="goToMedicalHome">{{ '\u5c31\u533b\u8f85\u52a9' }}</view>
        </view>
      </template>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import defaultAvatarImage from "@/assets/默认头像.png";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();

const healthRecord = ref(null);
const latestSymptom = ref(null);
const trendData = ref([]);
const isLoading = ref(false);
const defaultAvatar = ref(defaultAvatarImage);
const laryngoscopeList = ref([]);
const dialogRef = ref(null);

const UserInfo = computed(() => commonStore.UserInfo);

const riskLevelText = computed(() => {
  const level = healthRecord.value?.RiskAssessmentLevel;
  if (level === 0) return "\u4f4e\u98ce\u9669";
  if (level === 1) return "\u4e2d\u98ce\u9669";
  if (level === 2) return "\u9ad8\u98ce\u9669";
  return "\u5f85\u8bc4\u4f30";
});

const riskCardClass = computed(() => {
  const level = healthRecord.value?.RiskAssessmentLevel;
  if (level === 0) return "record-risk--low";
  if (level === 1) return "record-risk--medium";
  if (level === 2) return "record-risk--high";
  return "";
});

const healthTrendText = computed(() => {
  return healthRecord.value?.HealthTrendTag || "\u6682\u65e0\u6570\u636e";
});

const riskTipText = computed(() => {
  const level = healthRecord.value?.RiskAssessmentLevel;
  if (level === 0) return "\u5f53\u524d\u603b\u4f53\u98ce\u9669\u8f83\u4f4e\uff0c\u4fdd\u6301\u8f7b\u91cf\u4f46\u7a33\u5b9a\u7684\u62a4\u7406\u8282\u594f\u5373\u53ef\u3002";
  if (level === 1) return "\u5f53\u524d\u4e3a\u4e2d\u98ce\u9669\uff0c\u5efa\u8bae\u6301\u7eed\u8ddf\u8e2a\u75c7\u72b6\u53d8\u5316\uff0c\u5e76\u4fdd\u6301\u540e\u7eed\u590d\u67e5\u3002";
  if (level === 2) return "\u5f53\u524d\u4e3a\u9ad8\u98ce\u9669\uff0c\u5efa\u8bae\u5c3d\u5feb\u7ed3\u5408\u6700\u8fd1\u8bb0\u5f55\u8fdb\u5165\u590d\u67e5\u6216\u7ebf\u4e0b\u5c31\u533b\u6d41\u7a0b\u3002";
  return "\u8bf7\u5148\u5b8c\u6210\u4e00\u6b21\u7b5b\u67e5\uff0c\u4ee5\u4fbf\u7cfb\u7edf\u751f\u6210\u9636\u6bb5\u98ce\u9669\u5224\u65ad\u3002";
});

const latestDetect = computed(() => healthRecord.value?.LatestDetectDto || null);

const loadHealthRecord = async () => {
  if (!commonStore.UserId) {
    uni.showToast({ title: "\u8bf7\u5148\u767b\u5f55", icon: "none" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/Front/Login" });
    }, 1200);
    return;
  }

  isLoading.value = true;
  try {
    const response = await Post("/TPersonalLaryngealHealthRecord/Get", {
      UserId: commonStore.UserId,
      Page: 1,
      Limit: 1,
    });

    if (response.Success && response.Data) {
      healthRecord.value = response.Data;
      await loadLatestSymptom();
      await loadTrendData();
      await loadLaryngoscope();
    } else {
      healthRecord.value = null;
    }
  } catch (error) {
    console.error("Failed to load health record", error);
    uni.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "error" });
  } finally {
    isLoading.value = false;
  }
};

const loadLatestSymptom = async () => {
  try {
    const response = await Post("/TSymptomLog/List", {
      UserId: commonStore.UserId,
      Page: 1,
      Limit: 1,
    });
    if (response.Success && response.Data?.Items?.length > 0) {
      latestSymptom.value = response.Data.Items[0];
    } else {
      latestSymptom.value = null;
    }
  } catch (error) {
    console.error("Failed to load symptoms", error);
  }
};

const loadTrendData = async () => {
  try {
    const response = await Post("/TAudioScreenRecord/List", {
      UserId: commonStore.UserId,
      Page: 1,
      Limit: 6,
      DetectTotalStatus: true,
    });
    if (response.Success && response.Data?.Items) {
      trendData.value = response.Data.Items.sort((a, b) => new Date(a.CreationTime) - new Date(b.CreationTime));
    }
  } catch (error) {
    console.error("Failed to load trend data", error);
  }
};

const loadLaryngoscope = async () => {
  try {
    const response = await Post("/Front/Laryngoscope/List", {
      Page: 1,
      Limit: 2,
    });
    if (response.Success && response.Data?.Items) {
      laryngoscopeList.value = response.Data.Items.filter((item) => item?.LaryngoscopePhotoUrl && item.IsDelete !== true);
    } else {
      laryngoscopeList.value = [];
    }
  } catch (error) {
    console.error("Failed to load laryngoscope assets", error);
    laryngoscopeList.value = [];
  }
};

const formatTime = (timeStr) => {
  if (!timeStr) return "";
  try {
    const date = new Date(timeStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch (error) {
    return "";
  }
};

const formatDate = (timeStr) => {
  if (!timeStr) return "";
  try {
    const date = new Date(timeStr);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${month}-${day}`;
  } catch (error) {
    return "";
  }
};

const getSymptomLevelText = (level) => {
  if (level === 1) return "\u8f7b\u5ea6";
  if (level === 2) return "\u4e2d\u5ea6";
  if (level === 3) return "\u91cd\u5ea6";
  return "\u672a\u6807\u6ce8";
};

const goToSymptomList = () => {
  uni.navigateTo({ url: "/pages/Front/SymptomLogList" });
};

const goToRecordList = () => {
  uni.navigateTo({ url: "/pages/Front/AudioRecordList" });
};

const goToDetectDetail = (detectId) => {
  if (detectId) {
    uni.navigateTo({ url: `/pages/Front/AudioResult?id=${detectId}` });
  }
};

const goToHealthReminder = () => {
  uni.navigateTo({ url: "/pages/Front/HealthReminder" });
};

const goToMedicalHome = () => {
  uni.navigateTo({ url: "/pages/Front/MedicalHome" });
};

const goBack = () => {
  uni.navigateBack();
};

const goToHome = () => {
  uni.reLaunch({ url: "/pages/Front/Index" });
};

const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const showEditVoiceCare = async () => {
  const res = await showDialog({
    title: "\u7f16\u8f91\u62a4\u55d3\u4e60\u60ef",
    editable: true,
    placeholderText: "\u8f93\u5165\u4f60\u7684\u65e5\u5e38\u62a4\u55d3\u4e60\u60ef",
    content: healthRecord.value?.DailyVoiceCare || "",
  });
  if (!res.confirm || res.content === undefined) return;
  try {
    const updateData = {
      ...healthRecord.value,
      DailyVoiceCare: res.content,
    };
    const response = await Post("/TPersonalLaryngealHealthRecord/CreateOrEdit", updateData);
    if (response.Success) {
      uni.showToast({ title: "\u4fdd\u5b58\u6210\u529f", icon: "success" });
      await loadHealthRecord();
    } else {
      uni.showToast({ title: response.Msg || "\u4fdd\u5b58\u5931\u8d25", icon: "error" });
    }
  } catch (error) {
    console.error("Failed to save voice care", error);
    uni.showToast({ title: "\u4fdd\u5b58\u5931\u8d25", icon: "error" });
  }
};

const goToLaryngoscope = () => {
  uni.navigateTo({ url: "/pages/Front/LaryngoscopePhotoList" });
};

const buildLaryngoscopeTitle = (photo) => {
  if (!photo) return "\u5589\u955c\u68c0\u67e5";
  if (photo.PhotoDesc) {
    const parts = String(photo.PhotoDesc).split("|");
    if (parts[0]) return parts[0].trim();
  }
  if (photo.DetectDto?.CreationTime) {
    return `\u5173\u8054\u68c0\u6d4b\uff1a${formatTime(photo.DetectDto.CreationTime)}`;
  }
  return "\u5589\u955c\u68c0\u67e5";
};

const previewLaryngoscope = (photo) => {
  if (!photo?.LaryngoscopePhotoUrl) return;
  uni.previewImage({
    urls: [photo.LaryngoscopePhotoUrl],
    current: photo.LaryngoscopePhotoUrl,
  });
};

onMounted(() => {
  loadHealthRecord();
});
</script>

<style scoped lang="scss">
.record-screen {
  padding-bottom: calc(env(safe-area-inset-bottom) + 180upx);
}

.record-loading,
.record-empty {
  min-height: 340upx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.record-loading__spinner {
  width: 72upx;
  height: 72upx;
  border-radius: 50%;
  border: 5upx solid rgba(24, 33, 27, 0.12);
  border-top-color: #8ec94f;
  animation: spin 1s linear infinite;
}

.record-loading__text,
.record-empty__desc {
  margin-top: 18upx;
  font-size: 24upx;
  line-height: 1.65;
  color: var(--text-color-light);
}

.record-empty__title {
  font-size: 36upx;
  font-weight: 800;
  color: var(--text-color);
}

.record-empty .hc-pill-button-dark {
  margin-top: 20upx;
  width: 100%;
}

.record-hero__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16upx;
}

.record-user {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16upx;
}

.record-user__avatar {
  width: 104upx;
  height: 104upx;
  border-radius: 34upx;
  background: rgba(248, 252, 239, 0.9);
}

.record-user__name {
  display: block;
  font-size: 34upx;
  font-weight: 800;
  color: #152012;
}

.record-user__time {
  display: block;
  margin-top: 6upx;
  font-size: 20upx;
  color: #41513e;
}

.record-risk {
  min-width: 176upx;
  padding: 18upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.8);
}

.record-risk--medium {
  background: rgba(243, 182, 65, 0.18);
}

.record-risk--high {
  background: rgba(234, 117, 81, 0.2);
}

.record-risk__label {
  display: block;
  font-size: 20upx;
  color: #4f5f4b;
}

.record-risk__value {
  display: block;
  margin-top: 8upx;
  font-size: 30upx;
  font-weight: 800;
  color: #152012;
}

.record-hero__grid {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.record-pill {
  border-radius: 30upx;
  padding: 20upx;
  background: rgba(248, 252, 239, 0.78);
}

.record-pill--dark {
  background: linear-gradient(135deg, #101612 0%, #1d271f 100%);
}

.record-pill__label {
  display: block;
  font-size: 20upx;
  color: #4f5f4b;
}

.record-pill__label--dark {
  color: rgba(247, 255, 223, 0.72);
}

.record-pill__value {
  display: block;
  margin-top: 10upx;
  font-size: 32upx;
  font-weight: 800;
  color: #152012;
}

.record-pill__value--dark {
  color: #f7ffdf;
}

.record-hero__tip {
  display: block;
  margin-top: 18upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #33412f;
}

.record-masonry {
  display: grid;
  grid-template-columns: 1.06fr 0.94fr;
  gap: 14upx;
}

.record-panel {
  min-height: 280upx;
}

.record-panel__title-dark {
  color: #ffffff;
}

.record-bars {
  margin-top: 18upx;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10upx;
  min-height: 170upx;
}

.record-bars__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10upx;
}

.record-bars__track {
  width: 100%;
  min-height: 108upx;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.record-bars__bar {
  width: 30upx;
  border-radius: 999upx;
  background: linear-gradient(180deg, #d9f98a 0%, #96d641 100%);
}

.record-bars__bar--alert {
  background: linear-gradient(180deg, #f6be83 0%, #ea7551 100%);
}

.record-bars__date {
  font-size: 18upx;
  color: rgba(247, 255, 223, 0.7);
}

.record-panel__empty-dark {
  margin-top: 28upx;
  display: block;
  color: rgba(247, 255, 223, 0.7);
}

.record-tags {
  margin-top: 18upx;
  display: flex;
  gap: 10upx;
  flex-wrap: wrap;
}

.record-tag {
  min-height: 50upx;
  padding: 0 18upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ddf29c;
  color: #152012;
  font-size: 20upx;
  font-weight: 800;
}

.record-tag--soft {
  background: rgba(24, 33, 27, 0.08);
}

.record-panel__headline {
  display: block;
  margin-top: 18upx;
  font-size: 30upx;
  font-weight: 800;
  color: var(--text-color);
}

.record-panel__desc,
.record-panel__empty {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: var(--text-color-light);
}

.project-row {
  margin-top: 18upx;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12upx;
}

.project-row__main {
  flex: 1;
}

.project-row__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: var(--text-color);
}

.project-row__desc {
  display: block;
  margin-top: 6upx;
  font-size: 22upx;
  line-height: 1.6;
  color: var(--text-color-light);
}

.project-row__meta {
  font-size: 22upx;
  color: var(--text-color-lighter);
}

.project-row__meta--link {
  color: var(--text-color);
  font-weight: 800;
}

.record-alert {
  margin-top: 18upx;
  padding: 18upx;
  border-radius: 26upx;
  background: rgba(243, 182, 65, 0.12);
}

.record-alert__title {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #6f4d14;
}

.record-alert__desc {
  display: block;
  margin-top: 6upx;
  font-size: 20upx;
  line-height: 1.6;
  color: #7f6431;
}

.laryngoscope-list {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.laryngoscope-item {
  display: flex;
  align-items: center;
  gap: 14upx;
  padding: 14upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.86);
  border: 1upx solid rgba(205, 224, 145, 0.88);
}

.laryngoscope-item__img {
  width: 122upx;
  height: 122upx;
  border-radius: 28upx;
}

.laryngoscope-item__main {
  flex: 1;
}

.laryngoscope-item__title {
  display: block;
  font-size: 26upx;
  font-weight: 800;
  color: var(--text-color);
}

.laryngoscope-item__time {
  display: block;
  margin-top: 8upx;
  font-size: 20upx;
  color: var(--text-color-lighter);
}

.record-actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
