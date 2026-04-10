<template>
  <view class="hc-time-field">
    <view class="hc-time-field__trigger hc-interactive-card" :class="{ 'is-placeholder': !modelValue }" @click="open">
      <text class="hc-time-field__value">{{ displayValue }}</text>
      <uni-icons type="bottom" size="16" color="#5e705d" />
    </view>

    <uni-popup
      ref="popupRef"
      type="center"
      :safe-area="false"
      background-color="transparent"
      mask-background-color="rgba(16, 24, 19, 0.58)"
      @maskClick="close"
    >
      <view class="hc-time-field__popup">
        <view class="hc-time-field__handle"></view>
        <view class="hc-time-field__popup-head">
          <text class="hc-time-field__popup-eyebrow">时间选择</text>
          <text class="hc-time-field__popup-title">{{ title }}</text>
          <text v-if="subtitle" class="hc-time-field__popup-subtitle">{{ subtitle }}</text>
        </view>

        <picker-view
          :key="pickerKey"
          class="hc-time-field__picker"
          :value="[hourIndex, minuteIndex]"
          indicator-class="hc-time-field__indicator"
          @change="onPickerChange"
        >
          <picker-view-column>
            <view v-for="hour in hours" :key="hour" class="hc-time-field__picker-item">{{ hour }}</view>
          </picker-view-column>
          <picker-view-column>
            <view v-for="minute in minutes" :key="minute" class="hc-time-field__picker-item">{{ minute }}</view>
          </picker-view-column>
        </picker-view>

        <view class="hc-time-field__preview">{{ previewValue }}</view>

        <view class="hc-time-field__actions">
          <view v-if="clearable" class="hc-pill-button-soft hc-interactive-pill" @click="clearValue">清空</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="close">取消</view>
          <view class="hc-pill-button-dark hc-interactive-pill" @click="confirm">确定</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { computed, nextTick, ref } from "vue";

const props = defineProps({
  modelValue: {
    type: String,
    default: "",
  },
  title: {
    type: String,
    default: "选择时间",
  },
  subtitle: {
    type: String,
    default: "",
  },
  placeholder: {
    type: String,
    default: "请选择时间",
  },
  clearable: {
    type: Boolean,
    default: false,
  },
  minuteStep: {
    type: Number,
    default: 1,
  },
  hideSecond: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["update:modelValue", "change"]);
const popupRef = ref(null);
const pickerKey = ref(0);

const hours = Array.from({ length: 24 }, (_, index) => String(index).padStart(2, "0"));
const minuteStep = Math.max(1, Number(props.minuteStep) || 1);
const minutes = Array.from({ length: 60 }, (_, index) => index)
  .filter((minute) => minute % minuteStep === 0)
  .map((minute) => String(minute).padStart(2, "0"));

const hourIndex = ref(9);
const minuteIndex = ref(0);

const formatTime = (hour, minute) => `${hour}:${minute}${props.hideSecond ? "" : ":00"}`;
const clampIndex = (value, max) => Math.min(Math.max(Number(value || 0), 0), Math.max(max, 0));

const findNearestMinuteIndex = (minute) => {
  let bestIndex = 0;
  let bestDistance = Number.POSITIVE_INFINITY;
  minutes.forEach((item, index) => {
    const distance = Math.abs(Number(item) - minute);
    if (distance < bestDistance) {
      bestDistance = distance;
      bestIndex = index;
    }
  });
  return bestIndex;
};

const displayValue = computed(() => props.modelValue || props.placeholder);
const previewValue = computed(() => formatTime(hours[hourIndex.value] || hours[0], minutes[minuteIndex.value] || minutes[0]));

const syncFromValue = () => {
  const value = props.modelValue || "09:00";
  const parts = String(value).split(":");
  const nextHour = Math.min(Math.max(Number(parts[0] || 9), 0), 23);
  const nextMinute = Math.min(Math.max(Number(parts[1] || 0), 0), 59);
  hourIndex.value = nextHour;
  minuteIndex.value = findNearestMinuteIndex(nextMinute);
  pickerKey.value += 1;
};

const open = async () => {
  syncFromValue();
  await nextTick();
  popupRef.value?.open();
};

const close = () => {
  popupRef.value?.close();
};

const onPickerChange = (event) => {
  hourIndex.value = clampIndex(event.detail.value?.[0], hours.length - 1);
  minuteIndex.value = clampIndex(event.detail.value?.[1], minutes.length - 1);
};

const confirm = () => {
  const value = formatTime(hours[hourIndex.value], minutes[minuteIndex.value]);
  emit("update:modelValue", value);
  emit("change", value);
  close();
};

const clearValue = () => {
  emit("update:modelValue", "");
  emit("change", "");
  close();
};

defineExpose({
  open,
  close,
});
</script>

<style scoped lang="scss">
:deep(.hc-time-field__indicator) {
  height: 88upx;
  border-top: 1upx solid rgba(205, 224, 145, 0.9);
  border-bottom: 1upx solid rgba(205, 224, 145, 0.9);
  border-radius: 26upx;
  background: rgba(225, 243, 154, 0.14);
}

.hc-time-field__trigger {
  min-height: 92upx;
  padding: 0 24upx;
  border-radius: 30upx;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: rgba(248, 252, 239, 0.9);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14upx;
}

.hc-time-field__trigger.is-placeholder .hc-time-field__value {
  color: #9aaa98;
}

.hc-time-field__value {
  flex: 1;
  font-size: 24upx;
  font-weight: 700;
  color: #172119;
}

.hc-time-field__popup {
  width: calc(100vw - 68upx);
  max-width: 620upx;
  max-height: calc(100vh - 96upx);
  padding: 26upx 24upx calc(env(safe-area-inset-bottom) + 24upx);
  border-radius: 40upx;
  border: 1upx solid rgba(205, 224, 145, 0.72);
  background:
    radial-gradient(circle at top right, rgba(219, 242, 159, 0.52), transparent 22%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(250, 252, 243, 0.98) 100%);
  box-shadow: 0 24upx 72upx rgba(8, 14, 10, 0.22);
}

.hc-time-field__handle {
  width: 88upx;
  height: 8upx;
  margin: 0 auto 24upx;
  border-radius: 999upx;
  background: rgba(111, 127, 77, 0.28);
}

.hc-time-field__popup-head {
  display: flex;
  flex-direction: column;
  gap: 10upx;
  text-align: center;
}

.hc-time-field__popup-eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 2upx;
  color: #6f835f;
}

.hc-time-field__popup-title {
  font-size: 34upx;
  font-weight: 800;
  color: #172119;
}

.hc-time-field__popup-subtitle {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.6;
  color: #63705c;
}

.hc-time-field__picker {
  width: 100%;
  height: 360upx;
  margin-top: 22upx;
}

.hc-time-field__picker-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88upx;
  line-height: 88upx;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.hc-time-field__preview {
  margin-top: 14upx;
  text-align: center;
  font-size: 24upx;
  color: #63705c;
}

.hc-time-field__actions {
  display: flex;
  gap: 14upx;
  margin-top: 22upx;
}

.hc-time-field__actions > view {
  flex: 1;
}
</style>
