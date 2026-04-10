<template>
  <view class="hc-select-sheet">
    <view class="hc-select-sheet__trigger hc-interactive-card" :class="{ 'is-placeholder': !currentLabel }" @click="open">
      <text class="hc-select-sheet__value">{{ currentLabel || placeholder }}</text>
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
      <view class="hc-select-sheet__popup">
        <view class="hc-select-sheet__handle"></view>
        <view class="hc-select-sheet__popup-head">
          <text class="hc-select-sheet__popup-eyebrow">筛选与选择</text>
          <text class="hc-select-sheet__popup-title">{{ title }}</text>
          <text v-if="subtitle" class="hc-select-sheet__popup-subtitle">{{ subtitle }}</text>
        </view>

        <view class="hc-select-sheet__summary">
          <text class="hc-select-sheet__summary-label">当前</text>
          <text class="hc-select-sheet__summary-value">{{ currentLabel || placeholder }}</text>
        </view>

        <scroll-view scroll-y class="hc-select-sheet__popup-list">
          <view
            v-for="option in normalizedOptions"
            :key="option.key"
            class="hc-select-sheet__option hc-interactive-card"
            :class="{ active: isSelected(option.value) }"
            @click="choose(option)"
          >
            <text class="hc-select-sheet__option-text">{{ option.label }}</text>
            <text v-if="isSelected(option.value)" class="hc-select-sheet__option-mark">当前</text>
          </view>
        </scroll-view>

        <view class="hc-select-sheet__popup-actions">
          <view v-if="clearable" class="hc-pill-button-soft hc-interactive-pill" @click="clearSelection">清空</view>
          <view class="hc-pill-button-dark hc-interactive-pill" @click="close">完成</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";

const props = defineProps({
  modelValue: {
    type: [String, Number, Boolean, Object, Array],
    default: null,
  },
  options: {
    type: Array,
    default: () => [],
  },
  title: {
    type: String,
    default: "选择选项",
  },
  subtitle: {
    type: String,
    default: "",
  },
  placeholder: {
    type: String,
    default: "请选择",
  },
  labelKey: {
    type: String,
    default: "text",
  },
  valueKey: {
    type: String,
    default: "value",
  },
  clearable: {
    type: Boolean,
    default: false,
  },
  clearValue: {
    type: [String, Number, Boolean, Object, Array],
    default: null,
  },
});

const emit = defineEmits(["update:modelValue", "change"]);
const popupRef = ref(null);

const normalizedOptions = computed(() =>
  props.options.map((item, index) => {
    if (item && typeof item === "object" && !Array.isArray(item)) {
      return {
        key: item[props.valueKey] ?? `${index}`,
        label: item[props.labelKey] ?? String(item[props.valueKey] ?? ""),
        value: item[props.valueKey],
        raw: item,
      };
    }
    return {
      key: `${index}-${String(item)}`,
      label: String(item ?? ""),
      value: item,
      raw: item,
    };
  })
);

const isSameValue = (left, right) => {
  if (left === right) return true;
  if (left === null || left === undefined || right === null || right === undefined) return false;
  return String(left) === String(right);
};

const currentOption = computed(() => normalizedOptions.value.find((item) => isSameValue(item.value, props.modelValue)) || null);
const currentLabel = computed(() => currentOption.value?.label || "");

const isSelected = (value) => isSameValue(value, props.modelValue);

const open = () => {
  popupRef.value?.open();
};

const close = () => {
  popupRef.value?.close();
};

const choose = (option) => {
  emit("update:modelValue", option.value);
  emit("change", option.raw);
  close();
};

const clearSelection = () => {
  emit("update:modelValue", props.clearValue);
  emit("change", null);
  close();
};

defineExpose({
  open,
  close,
});
</script>

<style scoped lang="scss">
.hc-select-sheet__trigger {
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

.hc-select-sheet__trigger.is-placeholder .hc-select-sheet__value {
  color: #9aaa98;
}

.hc-select-sheet__value {
  flex: 1;
  font-size: 24upx;
  font-weight: 700;
  color: #172119;
}

.hc-select-sheet__popup {
  width: calc(100vw - 52upx);
  max-width: 682upx;
  max-height: calc(100vh - 72upx);
  padding: 26upx 24upx calc(env(safe-area-inset-bottom) + 24upx);
  border-radius: 40upx;
  border: 1upx solid rgba(205, 224, 145, 0.72);
  background:
    radial-gradient(circle at top right, rgba(219, 242, 159, 0.52), transparent 22%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(250, 252, 243, 0.98) 100%);
  box-shadow: 0 24upx 72upx rgba(8, 14, 10, 0.22);
  display: flex;
  flex-direction: column;
}

.hc-select-sheet__handle {
  width: 88upx;
  height: 8upx;
  margin: 0 auto 24upx;
  border-radius: 999upx;
  background: rgba(111, 127, 77, 0.28);
}

.hc-select-sheet__popup-head {
  display: flex;
  flex-direction: column;
  gap: 10upx;
  text-align: center;
}

.hc-select-sheet__popup-eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 2upx;
  color: #6f835f;
}

.hc-select-sheet__popup-title {
  font-size: 34upx;
  font-weight: 800;
  color: #172119;
}

.hc-select-sheet__popup-subtitle {
  font-size: 22upx;
  line-height: 1.6;
  color: #63705c;
}

.hc-select-sheet__summary {
  margin-top: 20upx;
  padding: 18upx 22upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.76);
}

.hc-select-sheet__summary-label {
  display: block;
  font-size: 20upx;
  color: #7a8977;
}

.hc-select-sheet__summary-value {
  display: block;
  margin-top: 8upx;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.hc-select-sheet__popup-list {
  flex: 1;
  min-height: 0;
  max-height: 760upx;
  margin-top: 20upx;
}

.hc-select-sheet__option {
  min-height: 96upx;
  padding: 0 24upx;
  border-radius: 30upx;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: rgba(248, 252, 239, 0.92);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16upx;
}

.hc-select-sheet__option + .hc-select-sheet__option {
  margin-top: 14upx;
}

.hc-select-sheet__option.active {
  background: linear-gradient(145deg, #dbf29f 0%, #c8eb79 48%, #ebf8bf 100%);
  border-color: rgba(156, 195, 77, 0.58);
  box-shadow: var(--box-shadow-base);
}

.hc-select-sheet__option-text {
  flex: 1;
  font-size: 24upx;
  font-weight: 700;
  color: #172119;
}

.hc-select-sheet__option-mark {
  font-size: 20upx;
  color: #57743e;
  font-weight: 800;
}

.hc-select-sheet__popup-actions {
  display: flex;
  gap: 14upx;
  margin-top: 22upx;
}

.hc-select-sheet__popup-actions > view {
  flex: 1;
}
</style>
