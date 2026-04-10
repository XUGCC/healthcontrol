<template>
  <uni-popup
    ref="popupRef"
    type="center"
    :safe-area="false"
    :is-mask-click="false"
    background-color="transparent"
    mask-background-color="rgba(16, 24, 19, 0.58)"
    @change="handlePopupChange"
  >
    <view class="hc-dialog">
      <view class="hc-dialog__handle"></view>
      <view class="hc-dialog__head">
        <text class="hc-dialog__eyebrow">提示信息</text>
        <text class="hc-dialog__title">{{ state.title }}</text>
        <text v-if="state.subtitle" class="hc-dialog__subtitle">{{ state.subtitle }}</text>
      </view>

      <text v-if="state.content && !state.editable" class="hc-dialog__content">{{ state.content }}</text>

      <textarea
        v-if="state.editable"
        v-model="inputValue"
        class="hc-dialog__textarea"
        :placeholder="state.placeholderText"
        :maxlength="state.maxlength"
        :show-confirm-bar="false"
      />

      <view class="hc-dialog__actions" :class="{ 'hc-dialog__actions--single': !state.showCancel }">
        <view v-if="state.showCancel" class="hc-pill-button-soft hc-interactive-pill" @click="handleCancel">
          {{ state.cancelText }}
        </view>
        <view class="hc-pill-button-dark hc-interactive-pill" @click="handleConfirm">
          {{ state.confirmText }}
        </view>
      </view>
    </view>
  </uni-popup>
</template>

<script setup>
import { reactive, ref } from "vue";

const inputValue = ref("");
const popupRef = ref(null);

const state = reactive({
  title: "提示",
  subtitle: "",
  content: "",
  editable: false,
  placeholderText: "请输入内容",
  confirmText: "确定",
  cancelText: "取消",
  showCancel: true,
  maxlength: 200,
});

let currentOptions = null;
let resolver = null;
const emit = defineEmits(["popup-change"]);

const resetState = () => {
  state.title = "提示";
  state.subtitle = "";
  state.content = "";
  state.editable = false;
  state.placeholderText = "请输入内容";
  state.confirmText = "确定";
  state.cancelText = "取消";
  state.showCancel = true;
  state.maxlength = 200;
  inputValue.value = "";
  currentOptions = null;
  resolver = null;
};

const finalize = (result) => {
  const options = currentOptions || {};
  popupRef.value?.close();
  uni.hideKeyboard?.();
  options.success?.(result);
  options.complete?.(result);
  resolver?.(result);
  setTimeout(() => {
    resetState();
  }, 180);
};

const open = (options = {}) => {
  currentOptions = options;
  state.title = options.title || "提示";
  state.subtitle = options.subtitle || "";
  state.content = options.content || "";
  state.editable = !!options.editable;
  state.placeholderText = options.placeholderText || "请输入内容";
  state.confirmText = options.confirmText || "确定";
  state.cancelText = options.cancelText || "取消";
  state.showCancel = options.showCancel !== false;
  state.maxlength = options.maxlength || 200;
  inputValue.value = state.editable ? String(options.content || "") : "";
  popupRef.value?.open();
  return new Promise((resolve) => {
    resolver = resolve;
  });
};

const handleCancel = () => {
  finalize({
    confirm: false,
    cancel: true,
    content: state.editable ? inputValue.value : undefined,
  });
};

const handleConfirm = () => {
  finalize({
    confirm: true,
    cancel: false,
    content: state.editable ? inputValue.value : undefined,
  });
};

const close = () => {
  popupRef.value?.close();
};

const handlePopupChange = (event) => {
  emit("popup-change", !!event?.show);
};

defineExpose({
  open,
  close,
});
</script>

<style scoped lang="scss">
.hc-dialog {
  width: calc(100vw - 88upx);
  max-width: 620upx;
  max-height: calc(100vh - 96upx);
  overflow-y: auto;
  padding: 24upx 26upx calc(env(safe-area-inset-bottom) + 26upx);
  border-radius: 40upx;
  border: 1upx solid rgba(205, 224, 145, 0.72);
  background:
    radial-gradient(circle at top right, rgba(219, 242, 159, 0.48), transparent 22%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(250, 252, 243, 0.98) 100%);
  box-shadow: 0 24upx 72upx rgba(8, 14, 10, 0.22);
}

.hc-dialog__handle {
  width: 88upx;
  height: 8upx;
  margin: 0 auto 24upx;
  border-radius: 999upx;
  background: rgba(111, 127, 77, 0.28);
}

.hc-dialog__head {
  display: flex;
  flex-direction: column;
  gap: 10upx;
  text-align: center;
}

.hc-dialog__eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 2upx;
  color: #6f835f;
}

.hc-dialog__title {
  font-size: 34upx;
  line-height: 1.2;
  font-weight: 800;
  color: #172119;
}

.hc-dialog__subtitle,
.hc-dialog__content {
  font-size: 24upx;
  line-height: 1.7;
  color: #5b6958;
}

.hc-dialog__content {
  display: block;
  margin-top: 18upx;
  padding: 18upx 22upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.82);
  border: 1upx solid rgba(205, 224, 145, 0.72);
  text-align: left;
}

.hc-dialog__textarea {
  width: 100%;
  min-height: 220upx;
  max-height: 360upx;
  margin-top: 20upx;
  padding: 24upx;
  border-radius: 32upx;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: rgba(248, 252, 239, 0.96);
  font-size: 24upx;
  line-height: 1.7;
  color: #172119;
  box-sizing: border-box;
}

.hc-dialog__actions {
  display: flex;
  gap: 16upx;
  margin-top: 26upx;
}

.hc-dialog__actions > view {
  flex: 1;
}

.hc-dialog__actions--single > view {
  flex: none;
  width: 100%;
}
</style>
