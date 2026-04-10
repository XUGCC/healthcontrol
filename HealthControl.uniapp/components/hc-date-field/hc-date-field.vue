<template>
  <view class="hc-date-field">
    <view class="hc-date-field__trigger hc-interactive-card" :class="{ 'is-placeholder': !hasDisplayValue }" @click="open">
      <text class="hc-date-field__value">{{ displayValue }}</text>
      <uni-icons type="calendar" size="18" color="#5e705d" />
    </view>

    <uni-popup
      ref="popupRef"
      type="center"
      :safe-area="false"
      background-color="transparent"
      mask-background-color="rgba(16, 24, 19, 0.58)"
      @maskClick="close"
    >
      <view class="hc-date-field__popup">
        <view class="hc-date-field__handle"></view>
        <view class="hc-date-field__popup-head">
          <text class="hc-date-field__popup-eyebrow">日期选择</text>
          <text class="hc-date-field__popup-title">{{ title }}</text>
          <text v-if="subtitle" class="hc-date-field__popup-subtitle">{{ subtitle }}</text>
        </view>

        <view v-if="isRange" class="hc-date-field__summary">
          <view class="hc-date-field__summary-item">
            <text class="hc-date-field__summary-label">开始</text>
            <text class="hc-date-field__summary-value">{{ pendingRange.start || startPlaceholder }}</text>
          </view>
          <view class="hc-date-field__summary-sep">{{ rangeSeparator }}</view>
          <view class="hc-date-field__summary-item">
            <text class="hc-date-field__summary-label">结束</text>
            <text class="hc-date-field__summary-value">{{ pendingRange.end || endPlaceholder }}</text>
          </view>
        </view>

        <view v-else class="hc-date-field__summary hc-date-field__summary--single">
          <text class="hc-date-field__summary-label">{{ hasTime ? "日期与时间" : "已选日期" }}</text>
          <text class="hc-date-field__summary-value">{{ singlePreview }}</text>
        </view>

        <view class="hc-date-field__calendar-shell">
          <uni-calendar
            :key="calendarKey"
            class="hc-date-field__calendar"
            :insert="true"
            :range="isRange"
            :date="calendarDate"
            :start-date="startDate"
            :end-date="endDate"
            :show-month="false"
            @change="onCalendarChange"
          />
        </view>

        <view v-if="hasTime" class="hc-date-field__time-panel">
          <text class="hc-date-field__time-title">选择时间</text>
          <picker-view
            :key="timePickerKey"
            class="hc-date-field__picker"
            :value="[hourIndex, minuteIndex]"
            indicator-class="hc-date-field__indicator"
            @change="onTimeChange"
          >
            <picker-view-column>
              <view v-for="hour in hours" :key="hour" class="hc-date-field__picker-item">{{ hour }}</view>
            </picker-view-column>
            <picker-view-column>
              <view v-for="minute in minutes" :key="minute" class="hc-date-field__picker-item">{{ minute }}</view>
            </picker-view-column>
          </picker-view>
        </view>

        <view class="hc-date-field__actions">
          <view v-if="clearable" class="hc-pill-button-soft hc-interactive-pill" @click="clearValue">清空</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="close">取消</view>
          <view class="hc-pill-button-dark hc-interactive-pill" @click="confirm">确定</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { computed, nextTick, reactive, ref } from "vue";

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: "",
  },
  type: {
    type: String,
    default: "date",
  },
  title: {
    type: String,
    default: "选择日期",
  },
  subtitle: {
    type: String,
    default: "",
  },
  placeholder: {
    type: String,
    default: "请选择日期",
  },
  startPlaceholder: {
    type: String,
    default: "开始日期",
  },
  endPlaceholder: {
    type: String,
    default: "结束日期",
  },
  rangeSeparator: {
    type: String,
    default: "至",
  },
  clearable: {
    type: Boolean,
    default: false,
  },
  startDate: {
    type: String,
    default: "",
  },
  endDate: {
    type: String,
    default: "",
  },
  hideSecond: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["update:modelValue", "change"]);

const popupRef = ref(null);
const calendarKey = ref(0);
const timePickerKey = ref(0);
const calendarDate = ref("");
const pendingDate = ref("");
const hourIndex = ref(9);
const minuteIndex = ref(0);
const pendingRange = reactive({
  start: "",
  end: "",
});

const hours = Array.from({ length: 24 }, (_, index) => String(index).padStart(2, "0"));
const minutes = Array.from({ length: 60 }, (_, index) => String(index).padStart(2, "0"));

const isRange = computed(() => props.type.includes("range"));
const hasTime = computed(() => props.type.includes("time"));
const clampIndex = (value, max) => Math.min(Math.max(Number(value || 0), 0), Math.max(max, 0));

const normalizeDate = (value) => {
  if (!value) return "";
  const text = Array.isArray(value) ? String(value[0] || "") : String(value);
  return text.slice(0, 10);
};

const normalizeTime = (value) => {
  if (!value) return "09:00";
  const text = String(value);
  const timePart = text.includes(" ") ? text.split(" ")[1] : text;
  return timePart.slice(0, 5) || "09:00";
};

const todayText = () => {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
};

const singleValue = computed(() => (typeof props.modelValue === "string" ? props.modelValue : ""));
const hasDisplayValue = computed(() => {
  if (isRange.value) return Array.isArray(props.modelValue) && props.modelValue.length === 2;
  return !!singleValue.value;
});

const displayValue = computed(() => {
  if (isRange.value) {
    const values = Array.isArray(props.modelValue) ? props.modelValue : [];
    if (values.length === 2 && values[0] && values[1]) return `${values[0]} ${props.rangeSeparator} ${values[1]}`;
    return props.placeholder;
  }
  return singleValue.value || props.placeholder;
});

const singlePreview = computed(() => {
  if (!pendingDate.value) return props.placeholder;
  if (!hasTime.value) return pendingDate.value;
  const time = `${hours[hourIndex.value]}:${minutes[minuteIndex.value]}${props.hideSecond ? "" : ":00"}`;
  return `${pendingDate.value} ${time}`;
});

const syncFromValue = () => {
  if (isRange.value) {
    const values = Array.isArray(props.modelValue) ? props.modelValue : [];
    pendingRange.start = values[0] || "";
    pendingRange.end = values[1] || "";
    calendarDate.value = pendingRange.start || todayText();
  } else {
    pendingDate.value = normalizeDate(props.modelValue) || todayText();
    const [hour, minute] = normalizeTime(props.modelValue).split(":");
    hourIndex.value = Math.min(Math.max(Number(hour || 9), 0), 23);
    minuteIndex.value = Math.min(Math.max(Number(minute || 0), 0), 59);
    calendarDate.value = pendingDate.value;
  }
  calendarKey.value += 1;
  timePickerKey.value += 1;
};

const open = async () => {
  syncFromValue();
  await nextTick();
  popupRef.value?.open();
};

const close = () => {
  popupRef.value?.close();
};

const onCalendarChange = (event) => {
  if (isRange.value) {
    pendingRange.start = event.range?.before || "";
    pendingRange.end = event.range?.after || "";
    return;
  }
  pendingDate.value = event.fulldate || pendingDate.value;
};

const onTimeChange = (event) => {
  hourIndex.value = clampIndex(event.detail.value?.[0], hours.length - 1);
  minuteIndex.value = clampIndex(event.detail.value?.[1], minutes.length - 1);
};

const clearValue = () => {
  const value = isRange.value ? [] : "";
  emit("update:modelValue", value);
  emit("change", value);
  close();
};

const confirm = () => {
  if (isRange.value) {
    if (!pendingRange.start || !pendingRange.end) {
      uni.showToast({ icon: "none", title: "请选择完整日期范围" });
      return;
    }
    const nextValue =
      pendingRange.start <= pendingRange.end
        ? [pendingRange.start, pendingRange.end]
        : [pendingRange.end, pendingRange.start];
    emit("update:modelValue", nextValue);
    emit("change", nextValue);
    close();
    return;
  }

  if (!pendingDate.value) {
    uni.showToast({ icon: "none", title: "请选择日期" });
    return;
  }

  const nextValue = hasTime.value
    ? `${pendingDate.value} ${hours[hourIndex.value]}:${minutes[minuteIndex.value]}${props.hideSecond ? "" : ":00"}`
    : pendingDate.value;

  emit("update:modelValue", nextValue);
  emit("change", nextValue);
  close();
};

defineExpose({
  open,
  close,
});
</script>

<style scoped lang="scss">
:deep(.hc-date-field__indicator) {
  height: 88upx;
  border-top: 1upx solid rgba(205, 224, 145, 0.9);
  border-bottom: 1upx solid rgba(205, 224, 145, 0.9);
  border-radius: 26upx;
  background: rgba(225, 243, 154, 0.14);
}

.hc-date-field__trigger {
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

.hc-date-field__trigger.is-placeholder .hc-date-field__value {
  color: #9aaa98;
}

.hc-date-field__value {
  flex: 1;
  font-size: 24upx;
  font-weight: 700;
  color: #172119;
}

.hc-date-field__popup {
  width: calc(100vw - 32upx);
  max-width: 710upx;
  max-height: calc(100vh - 48upx);
  padding: 26upx 24upx calc(env(safe-area-inset-bottom) + 24upx);
  border-radius: 40upx;
  border: 1upx solid rgba(205, 224, 145, 0.72);
  background:
    radial-gradient(circle at top right, rgba(219, 242, 159, 0.52), transparent 22%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(250, 252, 243, 0.98) 100%);
  box-shadow: 0 24upx 72upx rgba(8, 14, 10, 0.22);
  overflow: hidden;
}

.hc-date-field__handle {
  width: 88upx;
  height: 8upx;
  margin: 0 auto 24upx;
  border-radius: 999upx;
  background: rgba(111, 127, 77, 0.28);
}

.hc-date-field__popup-head {
  display: flex;
  flex-direction: column;
  gap: 10upx;
  text-align: center;
}

.hc-date-field__popup-eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 2upx;
  color: #6f835f;
}

.hc-date-field__popup-title {
  font-size: 34upx;
  font-weight: 800;
  color: #172119;
}

.hc-date-field__popup-subtitle {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.6;
  color: #63705c;
}

.hc-date-field__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16upx;
  margin-top: 20upx;
  padding: 18upx 22upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.hc-date-field__summary--single {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.hc-date-field__summary-item {
  flex: 1;
  min-width: 0;
}

.hc-date-field__summary-label {
  display: block;
  font-size: 20upx;
  color: #7b8b77;
}

.hc-date-field__summary-value {
  display: block;
  margin-top: 8upx;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.hc-date-field__summary-sep {
  font-size: 24upx;
  font-weight: 800;
  color: #647056;
}

.hc-date-field__calendar-shell {
  margin-top: 18upx;
  border-radius: 34upx;
  overflow: hidden;
  border: 1upx solid rgba(205, 224, 145, 0.88);
  background: rgba(248, 252, 239, 0.92);
}

.hc-date-field__calendar-shell :deep(.uni-calendar) {
  width: 100%;
}

.hc-date-field__calendar-shell :deep(.uni-calendar__content) {
  background: transparent;
}

.hc-date-field__calendar-shell :deep(.uni-calendar__header) {
  height: 96upx;
  border-bottom-color: rgba(205, 224, 145, 0.82);
}

.hc-date-field__calendar-shell :deep(.uni-calendar__header-text) {
  width: auto;
  min-width: 220upx;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.hc-date-field__calendar-shell :deep(.uni-calendar__backtoday) {
  top: 22upx;
  right: 18upx;
  height: 50upx;
  line-height: 50upx;
  padding: 0 18upx;
  border-radius: 999upx;
  background: rgba(219, 242, 159, 0.82);
  color: #172119;
  font-size: 20upx;
}

.hc-date-field__calendar-shell :deep(.uni-calendar__weeks-day) {
  height: 78upx;
  border-bottom-color: rgba(205, 224, 145, 0.36);
}

.hc-date-field__calendar-shell :deep(.uni-calendar__weeks-day-text) {
  font-size: 20upx;
  color: #172119;
}

.hc-date-field__calendar-shell :deep(.uni-calendar-item__weeks-box-item) {
  width: 88upx;
  height: 88upx;
  border-radius: 28upx;
}

.hc-date-field__calendar-shell :deep(.uni-calendar-item__weeks-box-text) {
  font-size: 22upx;
  font-weight: 700;
  color: #172119;
}

.hc-date-field__calendar-shell :deep(.uni-calendar-item__weeks-lunar-text) {
  font-size: 18upx;
  color: #72826e;
}

.hc-date-field__calendar-shell :deep(.uni-calendar-item--checked),
.hc-date-field__calendar-shell :deep(.uni-calendar-item--multiple),
.hc-date-field__calendar-shell :deep(.uni-calendar-item--isDay) {
  background: linear-gradient(145deg, #dbf29f 0%, #c8eb79 48%, #ebf8bf 100%);
  color: #172119;
  opacity: 1;
}

.hc-date-field__calendar-shell :deep(.uni-calendar-item--before-checked),
.hc-date-field__calendar-shell :deep(.uni-calendar-item--after-checked) {
  background: linear-gradient(145deg, #dbf29f 0%, #c8eb79 48%, #ebf8bf 100%);
  color: #172119;
  opacity: 1;
}

.hc-date-field__calendar-shell :deep(.uni-calendar-item--disable) {
  background: transparent;
  opacity: 0.35;
}

.hc-date-field__time-panel {
  margin-top: 16upx;
  padding: 22upx;
  border-radius: 32upx;
  background: rgba(248, 252, 239, 0.86);
  border: 1upx solid rgba(205, 224, 145, 0.88);
}

.hc-date-field__time-title {
  display: block;
  font-size: 22upx;
  font-weight: 800;
  color: #172119;
}

.hc-date-field__picker {
  width: 100%;
  height: 300upx;
  margin-top: 14upx;
}

.hc-date-field__picker-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88upx;
  line-height: 88upx;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.hc-date-field__actions {
  display: flex;
  gap: 14upx;
  margin-top: 22upx;
}

.hc-date-field__actions > view {
  flex: 1;
}
</style>
