<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack symptom-detail-screen">
      <hc-topbar
        :title="'\u75c7\u72b6\u8be6\u60c5'"
        :subtitle="'\u56de\u770b\u8fd9\u6b21\u75c7\u72b6\u8bb0\u5f55\u4e0e\u5173\u8054\u7b5b\u67e5\u7ed3\u679c'"
        :show-back="true"
        :right-text="'编辑'"
        @left="goBack"
        @right="goToEdit"
      />

      <view v-if="isLoading" class="hc-card-soft symptom-detail-loading">
        <view class="symptom-detail-loading__spinner"></view>
        <text class="symptom-detail-loading__text">正在加载症状详情...</text>
      </view>

      <template v-else-if="symptomData">
        <view class="hc-card-dark symptom-detail-hero hc-reveal-up">
          <view class="symptom-detail-hero__top">
            <view class="symptom-detail-hero__chips">
              <view class="symptom-chip symptom-chip--type">{{ symptomData.SymptomType }}</view>
              <view class="symptom-chip" :class="getSymptomLevelClass(symptomData.SymptomLevel)">{{ getSymptomLevelText(symptomData.SymptomLevel) }}</view>
            </view>
            <text class="symptom-detail-hero__time">{{ formatTime(symptomData.CreationTime) }}</text>
          </view>
          <text class="symptom-detail-hero__title">{{ symptomData.SymptomDuration }}</text>
          <text class="symptom-detail-hero__desc">把这次不适放回健康时间线中，方便后续与音频筛查和护理动作一起复盘。</text>
        </view>

        <view class="hc-card-soft symptom-detail-card hc-reveal-up" style="--delay: 100ms">
          <view class="hc-section-head">
            <text class="hc-section-head__title">记录信息</text>
            <text class="hc-section-head__meta">结构化摘要</text>
          </view>
          <view class="symptom-detail-info">
            <view class="symptom-detail-row"><text class="symptom-detail-row__label">症状类型</text><text class="symptom-detail-row__value">{{ symptomData.SymptomType }}</text></view>
            <view class="symptom-detail-row"><text class="symptom-detail-row__label">严重程度</text><text class="symptom-detail-row__value">{{ getSymptomLevelText(symptomData.SymptomLevel) }}</text></view>
            <view class="symptom-detail-row"><text class="symptom-detail-row__label">持续时长</text><text class="symptom-detail-row__value">{{ symptomData.SymptomDuration }}</text></view>
            <view v-if="symptomData.SymptomOccurTime" class="symptom-detail-row"><text class="symptom-detail-row__label">首次出现</text><text class="symptom-detail-row__value">{{ formatTime(symptomData.SymptomOccurTime) }}</text></view>
            <view class="symptom-detail-row"><text class="symptom-detail-row__label">记录时间</text><text class="symptom-detail-row__value">{{ formatTime(symptomData.CreationTime) }}</text></view>
          </view>
        </view>

        <view v-if="symptomData.Remark" class="hc-card-soft symptom-detail-card hc-reveal-up" style="--delay: 160ms">
          <view class="hc-section-head">
            <text class="hc-section-head__title">备注说明</text>
            <text class="hc-section-head__meta">补充细节</text>
          </view>
          <text class="symptom-detail-remark">{{ symptomData.Remark }}</text>
        </view>

        <view v-if="symptomData.DetectDto" class="hc-card-soft symptom-detail-card hc-reveal-up" style="--delay: 220ms" @click="goToDetectDetail">
          <view class="hc-section-head">
            <text class="hc-section-head__title">关联筛查</text>
            <text class="hc-section-head__meta">点击查看结果</text>
          </view>
          <view class="symptom-detect-card">
            <view class="symptom-detect-card__stat">
              <text class="symptom-detect-card__label">筛查结果</text>
              <text class="symptom-detect-card__value" :class="{ 'symptom-detect-card__value--alert': symptomData.DetectDto.PrimaryScreenResult }">
                {{ symptomData.DetectDto.PrimaryScreenResult ? "异常" : "正常" }}
              </text>
            </view>
            <view class="symptom-detect-card__stat">
              <text class="symptom-detect-card__label">置信度</text>
              <text class="symptom-detect-card__value">{{ (symptomData.DetectDto.PrimaryScreenConfidence * 100).toFixed(1) }}%</text>
            </view>
          </view>
          <text class="symptom-detect-card__time">{{ formatTime(symptomData.DetectDto.CreationTime) }}</text>
        </view>
      </template>
    </view>

    <view v-if="symptomData && !isLoading && !isPopupOpen" class="hc-fab symptom-detail-actions">
      <view class="hc-pill-button-dark" @click="goToEdit">编辑记录</view>
      <view class="hc-pill-button-soft symptom-detail-actions__danger" @click="confirmDelete">删除记录</view>
    </view>
    <hc-dialog ref="dialogRef" @popup-change="onPopupChange" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const symptomData = ref(null);
const isLoading = ref(false);
const symptomId = ref(null);
const dialogRef = ref(null);
const activePopupCount = ref(0);
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });
const isPopupOpen = computed(() => activePopupCount.value > 0);
const onPopupChange = (show) => {
  activePopupCount.value = Math.max(0, activePopupCount.value + (show ? 1 : -1));
};

const loadSymptomDetail = async (id) => {
  isLoading.value = true;
  try {
    const response = await Post("/TSymptomLog/Get", { Id: id, Page: 1, Limit: 1 });
    if (response.Success && response.Data) {
      symptomData.value = response.Data;
    } else {
      uni.showToast({ title: "加载失败", icon: "error" });
      setTimeout(() => uni.navigateBack(), 1200);
    }
  } catch (error) {
    console.error("加载症状详情失败:", error);
    uni.showToast({ title: "加载失败，请重试", icon: "error" });
    setTimeout(() => uni.navigateBack(), 1200);
  } finally {
    isLoading.value = false;
  }
};

const formatTime = (timeStr) => {
  if (!timeStr) return "";
  try {
    const date = new Date(timeStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")} ${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
  } catch {
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
  if (level === 1) return "轻度";
  if (level === 2) return "中度";
  if (level === 3) return "重度";
  return "\u672a\u6807\u6ce8";
};

const goToEdit = () => symptomId.value && uni.navigateTo({ url: `/pages/Front/SymptomLogForm?id=${symptomId.value}` });
const goToDetectDetail = () => symptomData.value?.DetectDto?.Id && uni.navigateTo({ url: `/pages/Front/AudioResult?id=${symptomData.value.DetectDto.Id}` });
const goBack = () => uni.navigateBack();

const doDelete = async () => {
  try {
    uni.showLoading({ title: "删除中..." });
    const response = await Post("/TSymptomLog/Delete", { Id: symptomId.value });
    if (response.Success) {
      uni.showToast({ title: "删除成功", icon: "success" });
      setTimeout(() => uni.navigateBack(), 1200);
    } else {
      uni.showToast({ title: response.Msg || "删除失败", icon: "error" });
    }
  } catch (error) {
    console.error("删除症状记录失败:", error);
    uni.showToast({ title: "删除失败，请重试", icon: "error" });
  } finally {
    uni.hideLoading();
  }
};

const confirmDelete = async () => {
  const result = await showDialog({
    title: "提示",
    content: "\u786e\u5b9a\u8981\u5220\u9664\u8fd9\u6761\u75c7\u72b6\u8bb0\u5f55\u5417\uff1f",
  });
  if (result.confirm) await doDelete();
};

onLoad((options) => {
  if (!options.id) {
    uni.showToast({ title: "参数错误", icon: "error" });
    setTimeout(() => uni.navigateBack(), 1200);
    return;
  }
  symptomId.value = Number(options.id);
  loadSymptomDetail(symptomId.value);
});
</script>

<style scoped lang="scss">
.symptom-detail-screen { padding-bottom: calc(env(safe-area-inset-bottom) + 220upx); }
.symptom-detail-loading { text-align: center; padding: 56upx 30upx; }
.symptom-detail-loading__spinner { width: 64upx; height: 64upx; margin: 0 auto; border-radius: 999upx; border: 5upx solid rgba(29, 39, 31, 0.14); border-top-color: #7ead49; animation: symptomSpin 0.9s linear infinite; }
.symptom-detail-loading__text { margin-top: 16upx; font-size: 24upx; color: var(--text-color-light); }
.symptom-detail-hero__top { display: flex; align-items: center; justify-content: space-between; gap: 12upx; }
.symptom-detail-hero__chips { display: flex; flex-wrap: wrap; gap: 10upx; }
.symptom-chip { min-height: 46upx; padding: 0 16upx; border-radius: 999upx; display: inline-flex; align-items: center; justify-content: center; font-size: 20upx; font-weight: 800; }
.symptom-chip--type { background: rgba(225, 243, 154, 0.18); color: #d7f494; }
.symptom-chip--mild { background: rgba(142, 201, 79, 0.18); color: #d7f494; }
.symptom-chip--moderate { background: rgba(243, 182, 65, 0.18); color: #ffd592; }
.symptom-chip--severe { background: rgba(234, 117, 81, 0.18); color: #ffc8bb; }
.symptom-detail-hero__time { font-size: 20upx; color: rgba(247, 255, 223, 0.72); }
.symptom-detail-hero__title { display: block; margin-top: 18upx; font-size: 44upx; line-height: 1.18; font-weight: 800; color: #f7ffdf; }
.symptom-detail-hero__desc { display: block; margin-top: 12upx; font-size: 22upx; line-height: 1.65; color: rgba(247, 255, 223, 0.72); }
.symptom-detail-card { display: flex; flex-direction: column; gap: 18upx; }
.symptom-detail-info,.symptom-detail-actions { display: flex; flex-direction: column; gap: 14upx; }
.symptom-detail-row { min-height: 82upx; padding: 0 20upx; border-radius: 28upx; background: rgba(248, 252, 239, 0.82); border: 1upx solid rgba(205, 224, 145, 0.88); display: flex; align-items: center; justify-content: space-between; gap: 12upx; }
.symptom-detail-row__label { font-size: 22upx; color: var(--text-color-light); }
.symptom-detail-row__value { font-size: 24upx; font-weight: 800; color: var(--text-color); text-align: right; }
.symptom-detail-remark { display: block; font-size: 24upx; line-height: 1.75; color: var(--text-color); white-space: pre-wrap; word-break: break-word; }
.symptom-detect-card { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12upx; }
.symptom-detect-card__stat { padding: 20upx; border-radius: 28upx; background: rgba(248, 252, 239, 0.84); border: 1upx solid rgba(205, 224, 145, 0.88); }
.symptom-detect-card__label { display: block; font-size: 20upx; color: var(--text-color-light); }
.symptom-detect-card__value { display: block; margin-top: 10upx; font-size: 30upx; font-weight: 800; color: var(--text-color); }
.symptom-detect-card__value--alert,.symptom-detail-actions__danger { color: #c9614d; }
.symptom-detect-card__time { font-size: 20upx; color: var(--text-color-lighter); }
@keyframes symptomSpin { to { transform: rotate(360deg); } }
</style>
