<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack diet-food-detail-screen">
      <hc-topbar title="食物详情" :subtitle="detail?.CategoryName || '查看食物对喉部的影响、建议与记录入口'" @left="back" />

      <template v-if="detail">
        <view class="hc-card-dark diet-food-detail-hero hc-reveal-up hc-shine">
          <image class="diet-food-detail-hero__image" :src="detail.PicUrl || defaultImg" mode="aspectFill" />
          <view class="diet-food-detail-hero__body">
            <view class="hc-kicker hc-kicker--dark">饮食详情</view>
            <view class="diet-food-detail-hero__head">
              <text class="diet-food-detail-hero__title">{{ detail.FoodName }}</text>
              <text class="diet-food-detail-hero__badge" :class="{ warn: detail.CategoryType === 1 }">
                {{ detail.CategoryType === 1 ? "需要注意" : "友好食物" }}
              </text>
            </view>
            <text v-if="detail.FoodAlias" class="diet-food-detail-hero__meta">别名：{{ detail.FoodAlias }}</text>
            <text v-if="detail.CategoryName" class="diet-food-detail-hero__meta">所属分类：{{ detail.CategoryName }}</text>
            <text class="diet-food-detail-hero__desc">
              {{ detail.Summary || detail.EffectHarmDesc || "继续查看食物对喉部的影响和建议安排。" }}
            </text>
          </view>
        </view>

        <view class="diet-food-detail-grid">
          <view class="hc-card-soft diet-food-detail-card hc-reveal-up" style="--delay: 100ms">
            <view class="bg-section-head">
              <text class="bg-section-head__title">对喉部的影响</text>
              <text class="bg-section-head__meta">先看结论</text>
            </view>
            <text class="diet-food-detail-card__text">{{ detail.EffectHarmDesc || "暂无说明" }}</text>
          </view>

          <view class="hc-card-soft diet-food-detail-card hc-reveal-up" style="--delay: 160ms">
            <view class="bg-section-head">
              <text class="bg-section-head__title">食用建议</text>
              <text class="bg-section-head__meta">安排方式</text>
            </view>
            <text class="diet-food-detail-card__text">{{ detail.SuggestContent || "暂无建议" }}</text>
          </view>
        </view>

        <view class="hc-card-lime diet-food-detail-tip hc-reveal-up" style="--delay: 220ms">
          <text class="diet-food-detail-tip__title">风险提示</text>
          <text class="diet-food-detail-tip__desc">
            {{ detail.RiskTip || "请结合个人耐受情况控制摄入频率，若食后不适明显可减少或暂停摄入。" }}
          </text>
        </view>
      </template>

      <view v-else class="hc-card-soft diet-food-detail-empty hc-reveal-up">
        <text class="diet-food-detail-empty__title">暂未找到这项食物</text>
        <text class="diet-food-detail-empty__desc">请返回食物库重新选择，或稍后再试。</text>
      </view>
    </view>

    <view v-if="detail" class="diet-food-detail-footer">
      <view class="hc-pill-button-dark hc-interactive-pill" @click="openRecord">加入今天的饮食记录</view>
    </view>

    <uni-popup
      ref="recordPopupRef"
      type="center"
      :safe-area="false"
      background-color="transparent"
      mask-background-color="rgba(16, 24, 19, 0.58)"
      @maskClick="closeRecord"
    >
      <view class="diet-food-detail-popup">
        <view class="diet-food-detail-popup__handle"></view>
        <view class="diet-food-detail-popup__head">
          <text class="diet-food-detail-popup__eyebrow">饮食记录</text>
          <text class="diet-food-detail-popup__title">记录一次饮食</text>
          <text class="diet-food-detail-popup__subtitle">把时间、频次和食后感受整理进今天的护理轨迹。</text>
        </view>

        <view class="hc-card-soft diet-food-detail-popup__card">
          <view class="diet-food-detail-popup__row">
            <text class="diet-food-detail-popup__label">食物</text>
            <text class="diet-food-detail-popup__value">{{ detail?.FoodName || "-" }}</text>
          </view>
          <view class="diet-food-detail-popup__field">
            <text class="diet-food-detail-popup__label">摄入时间</text>
            <uni-datetime-picker
              v-model="intakeTime"
              type="datetime"
              return-type="string"
              :border="false"
              :clear-icon="false"
              class="diet-food-detail-popup__picker"
              placeholder="请选择摄入时间"
            />
          </view>
          <view class="diet-food-detail-popup__field">
            <text class="diet-food-detail-popup__label">当天频次</text>
            <view class="diet-food-detail-stepper">
              <view class="diet-food-detail-stepper__button hc-interactive-pill" :class="{ disabled: intakeFrequency <= 1 }" @click="decreaseFrequency">-</view>
              <view class="diet-food-detail-stepper__value">{{ intakeFrequency }}</view>
              <view class="diet-food-detail-stepper__button hc-interactive-pill" :class="{ disabled: intakeFrequency >= 10 }" @click="increaseFrequency">+</view>
            </view>
          </view>
          <view class="diet-food-detail-popup__field">
            <text class="diet-food-detail-popup__label">食后感受</text>
            <uni-easyinput
              v-model="eatFeeling"
              type="textarea"
              placeholder="可选：如缓解不适、无明显变化或稍有刺激"
            />
          </view>
        </view>

        <view class="diet-food-detail-popup__actions">
          <view class="hc-pill-button-soft hc-interactive-pill" @click="closeRecord">取消</view>
          <view class="hc-pill-button-dark hc-interactive-pill" @click="submitRecord">保存</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();
const defaultImg = "/static/food-placeholder.png";
const foodId = ref(0);
const detail = ref(null);
const recordPopupRef = ref(null);
const intakeTime = ref("");
const intakeFrequency = ref(1);
const eatFeeling = ref("");

onLoad((options) => {
  if (options && options.foodId) {
    foodId.value = Number.parseInt(options.foodId, 10) || 0;
  }
  loadDetail();
});

const back = () => {
  const pages = getCurrentPages();
  if (pages && pages.length > 1) {
    uni.navigateBack({ delta: 1 });
  } else {
    uni.reLaunch({ url: "/pages/Front/DietHome" });
  }
};

const loadDetail = async () => {
  if (!foodId.value) return;
  const res = await Post("/Front/Diet/FoodDetail", {
    FoodId: foodId.value,
  });
  if (!res.Success) {
    uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
    return;
  }
  detail.value = res.Data || null;
};

const openRecord = () => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录后再记录饮食" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/Front/Login" });
    }, 1500);
    return;
  }
  const now = new Date();
  intakeTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(
    now.getMinutes()
  )}:${pad(now.getSeconds())}`;
  intakeFrequency.value = 1;
  eatFeeling.value = "";
  recordPopupRef.value?.open();
};

const closeRecord = () => {
  recordPopupRef.value?.close();
};

const decreaseFrequency = () => {
  if (intakeFrequency.value <= 1) return;
  intakeFrequency.value -= 1;
};

const increaseFrequency = () => {
  if (intakeFrequency.value >= 10) return;
  intakeFrequency.value += 1;
};

const submitRecord = async () => {
  if (!detail.value) return;
  const res = await Post("/Front/Diet/RecordCreateOrEdit", {
    FoodId: detail.value.FoodId,
    IntakeTime: intakeTime.value,
    IntakeFrequency: intakeFrequency.value,
    EatFeeling: eatFeeling.value,
  });
  if (!res.Success) {
    uni.showToast({ icon: "none", title: res.Msg || "保存失败" });
    return;
  }
  uni.showToast({ icon: "success", title: "已记录" });
  closeRecord();
};

const pad = (value) => (value < 10 ? `0${value}` : `${value}`);
</script>

<style scoped lang="scss">
.diet-food-detail-screen {
  gap: 24upx;
  padding-bottom: 176upx;
}

.diet-food-detail-hero {
  display: flex;
  flex-direction: column;
  gap: 20upx;
}

.diet-food-detail-hero__image {
  width: 100%;
  height: 320upx;
  border-radius: 34upx;
  background: rgba(225, 243, 154, 0.2);
}

.diet-food-detail-hero__body {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.diet-food-detail-hero__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16upx;
}

.diet-food-detail-hero__title {
  display: block;
  font-size: 46upx;
  line-height: 1.1;
  font-weight: 800;
  color: #f7ffdf;
}

.diet-food-detail-hero__badge {
  padding: 10upx 16upx;
  border-radius: 999upx;
  background: rgba(225, 243, 154, 0.16);
  border: 1upx solid rgba(202, 235, 134, 0.2);
  font-size: 20upx;
  font-weight: 800;
  color: #f7ffdf;
  flex-shrink: 0;
}

.diet-food-detail-hero__badge.warn {
  background: rgba(239, 179, 77, 0.18);
  border-color: rgba(239, 179, 77, 0.24);
}

.diet-food-detail-hero__meta {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.diet-food-detail-hero__desc {
  display: block;
  margin-top: 6upx;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.78);
}

.diet-food-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16upx;
}

.diet-food-detail-card__text,
.diet-food-detail-tip__desc,
.diet-food-detail-empty__desc {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.diet-food-detail-tip__title,
.diet-food-detail-empty__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.diet-food-detail-empty {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.diet-food-detail-footer {
  position: fixed;
  left: 28upx;
  right: 28upx;
  bottom: calc(env(safe-area-inset-bottom) + 24upx);
  z-index: 15;
}

.diet-food-detail-popup {
  position: relative;
  width: min(670upx, calc(100vw - 56upx));
  max-height: calc(100vh - env(safe-area-inset-top) - 36upx);
  overflow-y: auto;
  border-radius: 42upx;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: linear-gradient(180deg, rgba(248, 252, 239, 0.99) 0%, rgba(239, 247, 224, 0.99) 100%);
  box-shadow: 0 26upx 64upx rgba(18, 24, 20, 0.22);
  padding: 26upx 24upx calc(env(safe-area-inset-bottom) + 30upx);
}

.diet-food-detail-popup__handle {
  width: 88upx;
  height: 8upx;
  margin: 0 auto 22upx;
  border-radius: 999upx;
  background: rgba(111, 127, 77, 0.28);
}

.diet-food-detail-popup__head {
  display: flex;
  flex-direction: column;
  gap: 10upx;
  text-align: center;
}

.diet-food-detail-popup__eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 2upx;
  color: #6f835f;
}

.diet-food-detail-popup__title {
  display: block;
  font-size: 34upx;
  font-weight: 800;
  color: #172119;
}

.diet-food-detail-popup__subtitle {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #60705d;
}

.diet-food-detail-popup__card {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.diet-food-detail-popup__row,
.diet-food-detail-popup__field {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.diet-food-detail-popup__label {
  font-size: 22upx;
  font-weight: 700;
  color: #172119;
}

.diet-food-detail-popup__value {
  font-size: 24upx;
  color: #556556;
}

.diet-food-detail-popup__picker :deep(.uni-date) {
  width: 100%;
}

.diet-food-detail-popup__picker :deep(.uni-date-editor) {
  width: 100%;
}

.diet-food-detail-popup__picker :deep(.uni-date-editor--x) {
  min-height: 92upx;
  padding: 0 22upx;
  border-radius: 30upx;
  border: 1upx solid rgba(205, 224, 145, 0.9) !important;
  background: rgba(248, 252, 239, 0.92) !important;
  box-shadow: none !important;
}

.diet-food-detail-popup__picker :deep(.uni-date-x) {
  min-height: 78upx;
}

.diet-food-detail-popup__picker :deep(.uni-date__x-input) {
  font-size: 24upx;
  font-weight: 700;
  color: #172119 !important;
}

.diet-food-detail-popup__picker :deep(.icon-calendar) {
  color: #6c7d69 !important;
}

.diet-food-detail-stepper {
  display: flex;
  align-items: center;
  gap: 12upx;
}

.diet-food-detail-stepper__button,
.diet-food-detail-stepper__value {
  min-height: 76upx;
  border-radius: 28upx;
  border: 1upx solid rgba(205, 224, 145, 0.88);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 28upx;
  font-weight: 800;
}

.diet-food-detail-stepper__button {
  width: 76upx;
  background: rgba(225, 243, 154, 0.46);
  color: #172119;
}

.diet-food-detail-stepper__button.disabled {
  opacity: 0.42;
}

.diet-food-detail-stepper__value {
  min-width: 108upx;
  padding: 0 18upx;
  background: rgba(248, 252, 239, 0.92);
  color: #172119;
}

.diet-food-detail-popup__actions {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14upx;
}
</style>
