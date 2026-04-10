<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack symptom-screen">
      <hc-topbar
        :title="'\u75c7\u72b6\u8bb0\u5f55'"
        :subtitle="'\u50cf\u4e60\u60ef\u8ffd\u8e2a\u4e00\u6837\u8bb0\u5f55\u6bcf\u6b21\u4e0d\u9002\u53d8\u5316'"
        :show-back="true"
        @left="goBack"
      >
        <template #right>
          <text class="symptom-screen__right" @click="goToForm">{{ '\u65b0\u589e' }}</text>
        </template>
      </hc-topbar>

      <view class="hc-card-lime symptom-hero">
        <view class="hc-section-head">
          <text class="hc-section-head__title">{{ '\u75c7\u72b6\u8ffd\u8e2a\u9762\u677f' }}</text>
          <text class="hc-section-head__meta">{{ symptomList.length }} {{ '\u6761\u8bb0\u5f55' }}</text>
        </view>
        <view class="symptom-hero__stats">
          <view class="symptom-hero__stat">
            <text class="hc-stat-value">{{ symptomList.length }}</text>
            <text class="hc-stat-label">{{ '\u603b\u8bb0\u5f55\u6570' }}</text>
          </view>
          <view class="symptom-hero__stat symptom-hero__stat--dark">
            <text class="hc-stat-value hc-stat-value--dark">{{ currentFilter === '\u5168\u90e8' ? '\u5168\u90e8' : currentFilter }}</text>
            <text class="hc-stat-label">{{ '\u5f53\u524d\u7b5b\u9009' }}</text>
          </view>
        </view>
      </view>

      <scroll-view class="symptom-filter-scroll" scroll-x>
        <view class="hc-token-tabs symptom-filter-tabs">
          <view
            v-for="filter in filters"
            :key="filter"
            class="hc-token-tabs__item"
            :class="{ active: currentFilter === filter }"
            @click="setFilter(filter)"
          >
            {{ filter }}
          </view>
        </view>
      </scroll-view>

      <view v-if="symptomList.length > 0" class="symptom-list">
        <view v-for="item in symptomList" :key="item.Id" class="symptom-item" @click="goToDetail(item.Id)">
          <view class="symptom-item__top">
            <view class="symptom-item__tags">
              <view class="symptom-chip symptom-chip--type">{{ item.SymptomType }}</view>
              <view class="symptom-chip" :class="getSymptomLevelClass(item.SymptomLevel)">
                {{ getSymptomLevelText(item.SymptomLevel) }}
              </view>
            </view>
            <text class="symptom-item__time">{{ formatTime(item.CreationTime) }}</text>
          </view>

          <view class="symptom-item__body">
            <view class="symptom-item__main">
              <text class="symptom-item__title">{{ item.SymptomType }} · {{ item.SymptomDuration }}</text>
              <text class="symptom-item__desc">
                {{ item.DetectDto ? `\u5173\u8054\u7ed3\u679c\uff1a${item.DetectDto.PrimaryScreenResult ? '\u5f02\u5e38' : '\u6b63\u5e38'} ${(item.DetectDto.PrimaryScreenConfidence * 100).toFixed(1)}%` : '\u6682\u65e0\u5173\u8054\u7b5b\u67e5\u7ed3\u679c' }}
              </text>
            </view>
            <view class="symptom-item__score">
              <text class="symptom-item__score-value">{{ item.SymptomLevel || "-" }}</text>
              <text class="symptom-item__score-label">{{ '\u7ea7\u522b' }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else-if="!isLoading" class="hc-card-soft symptom-empty">
        <view class="symptom-empty__icon">
          <uni-icons type="compose" size="34" color="#18211b" />
        </view>
        <text class="symptom-empty__title">{{ '\u8fd8\u6ca1\u6709\u75c7\u72b6\u8bb0\u5f55' }}</text>
        <text class="symptom-empty__desc">{{ '\u5f00\u59cb\u8bb0\u5f55\u58f0\u97f3\u4e0d\u9002\uff0c\u75bc\u75db\uff0c\u5e72\u75d2\u7b49\u53d8\u5316\uff0c\u65b9\u4fbf\u540e\u7eed\u4e0e\u7b5b\u67e5\u7ed3\u679c\u4e00\u8d77\u89c2\u5bdf\u3002' }}</text>
        <view class="hc-pill-button-dark" @click="goToForm">{{ '\u521b\u5efa\u7b2c\u4e00\u6761\u8bb0\u5f55' }}</view>
      </view>

      <view v-if="hasMore && !isLoading && symptomList.length > 0" class="hc-pill-button-soft symptom-more" @click="loadMore">
        {{ '\u52a0\u8f7d\u66f4\u591a' }}
      </view>
    </view>

    <view class="hc-fab" @click="goToForm">
      <view class="hc-pill-button-dark">{{ '\u6dfb\u52a0\u75c7\u72b6\u8bb0\u5f55' }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();
const symptomList = ref([]);
const currentFilter = ref("\u5168\u90e8");
const isLoading = ref(false);
const hasMore = ref(true);
const currentPage = ref(1);
const pageSize = 10;
const filters = ["\u5168\u90e8", "\u58f0\u97f3\u5636\u54d1", "\u75bc\u75db", "\u541e\u54bd\u56f0\u96be", "\u54bd\u5e72", "\u54bd\u75d2", "\u5176\u4ed6"];

const symptomTypeMap = {
  "\u5168\u90e8": null,
  "\u58f0\u97f3\u5636\u54d1": "\u58f0\u97f3\u5636\u54d1",
  "\u75bc\u75db": "\u75bc\u75db",
  "\u541e\u54bd\u56f0\u96be": "\u541e\u54bd\u56f0\u96be",
  "\u54bd\u5e72": "\u54bd\u5e72",
  "\u54bd\u75d2": "\u54bd\u75d2",
  "\u5176\u4ed6": "\u5176\u4ed6",
};

const loadSymptomList = async (reset = false) => {
  if (!commonStore.UserId) {
    uni.showToast({ title: "\u8bf7\u5148\u767b\u5f55", icon: "none" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/Front/Login" });
    }, 1500);
    return;
  }

  if (reset) {
    currentPage.value = 1;
    symptomList.value = [];
    hasMore.value = true;
  }

  if (!hasMore.value) return;

  isLoading.value = true;
  try {
    const params = {
      UserId: commonStore.UserId,
      Page: currentPage.value,
      Limit: pageSize,
    };

    const mappedType = symptomTypeMap[currentFilter.value];
    if (mappedType) {
      params.SymptomType = mappedType;
    }

    const response = await Post("/TSymptomLog/List", params);
    if (response.Success && response.Data && response.Data.Items) {
      if (reset) {
        symptomList.value = response.Data.Items;
      } else {
        symptomList.value.push(...response.Data.Items);
      }
      hasMore.value = symptomList.value.length < response.Data.TotalCount;
      currentPage.value += 1;
    } else if (reset) {
      symptomList.value = [];
    }
  } catch (error) {
    console.error("Failed to load symptoms", error);
    uni.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "error" });
  } finally {
    isLoading.value = false;
  }
};

const setFilter = (filter) => {
  currentFilter.value = filter;
  loadSymptomList(true);
};

const loadMore = () => {
  loadSymptomList(false);
};

const formatTime = (timeStr) => {
  if (!timeStr) return "";
  try {
    const date = new Date(timeStr);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    return `${month}-${day} ${hours}:${minutes}`;
  } catch (error) {
    return "";
  }
};

const getSymptomLevelClass = (level) => {
  if (level === 1) return "symptom-chip--mild";
  if (level === 2) return "symptom-chip--moderate";
  if (level === 3) return "symptom-chip--severe";
  return "";
};

const getSymptomLevelText = (level) => {
  if (level === 1) return "\u8f7b\u5ea6";
  if (level === 2) return "\u4e2d\u5ea6";
  if (level === 3) return "\u91cd\u5ea6";
  return "\u672a\u6807\u6ce8";
};

const goToForm = () => {
  uni.navigateTo({ url: "/pages/Front/SymptomLogForm" });
};

const goToDetail = (id) => {
  uni.navigateTo({ url: `/pages/Front/SymptomLogDetail?id=${id}` });
};

const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  loadSymptomList(true);
});
</script>

<style scoped lang="scss">
.symptom-screen {
  padding-bottom: calc(env(safe-area-inset-bottom) + 250upx);
}

.symptom-screen__right {
  font-size: 24upx;
  color: var(--text-color);
  font-weight: 800;
}

.symptom-hero__stats {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12upx;
}

.symptom-hero__stat {
  border-radius: 32upx;
  padding: 22upx;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(205, 224, 145, 0.86);
}

.symptom-hero__stat--dark {
  background: linear-gradient(135deg, #101612 0%, #1d271f 100%);
  border-color: #101612;
}

.symptom-filter-scroll {
  white-space: nowrap;
}

.symptom-filter-tabs {
  flex-wrap: nowrap;
  padding-right: 20upx;
}

.symptom-list {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.symptom-item {
  padding: 24upx;
  border-radius: 38upx;
  background: rgba(248, 252, 239, 0.92);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  box-shadow: var(--box-shadow-base);
}

.symptom-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.symptom-item__tags {
  display: flex;
  align-items: center;
  gap: 10upx;
  flex-wrap: wrap;
}

.symptom-chip {
  min-height: 50upx;
  padding: 0 18upx;
  border-radius: 999upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(24, 33, 27, 0.08);
  color: #18211b;
  font-size: 20upx;
  font-weight: 800;
}

.symptom-chip--type {
  background: #ddf29c;
}

.symptom-chip--mild {
  background: rgba(142, 201, 79, 0.18);
}

.symptom-chip--moderate {
  background: rgba(243, 182, 65, 0.18);
}

.symptom-chip--severe {
  background: rgba(234, 117, 81, 0.18);
}

.symptom-item__time {
  font-size: 20upx;
  color: var(--text-color-lighter);
}

.symptom-item__body {
  margin-top: 18upx;
  display: flex;
  gap: 18upx;
}

.symptom-item__main {
  flex: 1;
  min-width: 0;
}

.symptom-item__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: var(--text-color);
}

.symptom-item__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.6;
  color: var(--text-color-light);
}

.symptom-item__score {
  width: 120upx;
  border-radius: 30upx;
  background: linear-gradient(135deg, #101612 0%, #1d271f 100%);
  padding: 18upx 12upx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.symptom-item__score-value {
  font-size: 42upx;
  line-height: 1;
  font-weight: 800;
  color: #f7ffdf;
}

.symptom-item__score-label {
  margin-top: 8upx;
  font-size: 18upx;
  color: rgba(247, 255, 223, 0.7);
}

.symptom-empty {
  text-align: center;
  align-items: center;
}

.symptom-empty__icon {
  width: 88upx;
  height: 88upx;
  margin: 0 auto 18upx;
  border-radius: 32upx;
  background: #ddf29c;
  display: flex;
  align-items: center;
  justify-content: center;
}

.symptom-empty__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: var(--text-color);
}

.symptom-empty__desc {
  display: block;
  margin: 12upx 0 22upx;
  font-size: 24upx;
  line-height: 1.65;
  color: var(--text-color-light);
}

.symptom-more {
  margin-bottom: 10upx;
}
</style>
