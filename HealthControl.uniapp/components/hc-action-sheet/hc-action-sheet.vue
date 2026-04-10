<template>
  <uni-popup
    ref="popupRef"
    type="center"
    :safe-area="false"
    :is-mask-click="true"
    background-color="transparent"
    mask-background-color="rgba(16, 24, 19, 0.58)"
    @change="handlePopupChange"
    @maskClick="cancel"
  >
    <view class="hc-action-sheet">
      <view class="hc-action-sheet__handle"></view>
      <view class="hc-action-sheet__head">
        <text class="hc-action-sheet__eyebrow">快捷操作</text>
        <text class="hc-action-sheet__title">{{ state.title }}</text>
        <text v-if="state.subtitle" class="hc-action-sheet__subtitle">{{ state.subtitle }}</text>
      </view>

      <scroll-view scroll-y class="hc-action-sheet__list">
        <view
          v-for="(action, index) in state.actions"
          :key="action.key || `${index}-${action.label}`"
          class="hc-action-sheet__item hc-interactive-card"
          :class="{ 'hc-action-sheet__item--danger': action.danger }"
          @click="choose(action, index)"
        >
          <view class="hc-action-sheet__item-main">
            <text class="hc-action-sheet__item-label">{{ action.label }}</text>
            <text v-if="action.desc" class="hc-action-sheet__item-desc">{{ action.desc }}</text>
          </view>
          <text v-if="action.meta" class="hc-action-sheet__item-meta">{{ action.meta }}</text>
        </view>
      </scroll-view>

      <view class="hc-pill-button-soft hc-interactive-pill" @click="cancel">取消</view>
    </view>
  </uni-popup>
</template>

<script setup>
import { reactive, ref } from "vue";

const resolver = ref(null);
const popupRef = ref(null);
const emit = defineEmits(["popup-change"]);

const state = reactive({
  title: "选择操作",
  subtitle: "",
  actions: [],
});

const reset = () => {
  state.title = "选择操作";
  state.subtitle = "";
  state.actions = [];
  resolver.value = null;
};

const finalize = (result) => {
  popupRef.value?.close();
  resolver.value?.(result);
  setTimeout(reset, 160);
};

const open = (options = {}) => {
  state.title = options.title || "选择操作";
  state.subtitle = options.subtitle || "";
  state.actions = Array.isArray(options.actions) ? options.actions : [];
  popupRef.value?.open();
  return new Promise((resolve) => {
    resolver.value = resolve;
  });
};

const choose = (action, index) => {
  finalize({
    confirm: true,
    cancel: false,
    tapIndex: index,
    action,
  });
};

const cancel = () => {
  finalize({
    confirm: false,
    cancel: true,
    tapIndex: -1,
    action: null,
  });
};

const handlePopupChange = (event) => {
  emit("popup-change", !!event?.show);
};

defineExpose({
  open,
  close: cancel,
});
</script>

<style scoped lang="scss">
.hc-action-sheet {
  width: calc(100vw - 52upx);
  max-width: 680upx;
  max-height: calc(100vh - 96upx);
  overflow: hidden;
  padding: 26upx 24upx calc(env(safe-area-inset-bottom) + 24upx);
  border-radius: 40upx;
  border: 1upx solid rgba(205, 224, 145, 0.72);
  background:
    radial-gradient(circle at top right, rgba(219, 242, 159, 0.48), transparent 22%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(250, 252, 243, 0.98) 100%);
  box-shadow: 0 24upx 72upx rgba(8, 14, 10, 0.22);
  display: flex;
  flex-direction: column;
}

.hc-action-sheet__handle {
  width: 88upx;
  height: 8upx;
  margin: 0 auto 24upx;
  border-radius: 999upx;
  background: rgba(111, 127, 77, 0.28);
}

.hc-action-sheet__head {
  display: flex;
  flex-direction: column;
  gap: 10upx;
  text-align: center;
}

.hc-action-sheet__eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 2upx;
  color: #6f835f;
}

.hc-action-sheet__title {
  font-size: 34upx;
  font-weight: 800;
  color: #172119;
}

.hc-action-sheet__subtitle {
  font-size: 22upx;
  line-height: 1.6;
  color: #63705c;
}

.hc-action-sheet__list {
  flex: 1;
  min-height: 0;
  margin: 24upx 0 22upx;
}

.hc-action-sheet__item {
  min-height: 94upx;
  padding: 20upx 24upx;
  border-radius: 30upx;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: rgba(248, 252, 239, 0.92);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16upx;
}

.hc-action-sheet__item + .hc-action-sheet__item {
  margin-top: 14upx;
}

.hc-action-sheet__item--danger {
  background: rgba(252, 238, 232, 0.92);
  border-color: rgba(230, 146, 119, 0.28);
}

.hc-action-sheet__item-main {
  flex: 1;
  min-width: 0;
}

.hc-action-sheet__item-label {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.hc-action-sheet__item-desc {
  display: block;
  margin-top: 6upx;
  font-size: 20upx;
  line-height: 1.6;
  color: #697666;
}

.hc-action-sheet__item-meta {
  font-size: 20upx;
  color: #6b7a68;
}
</style>
