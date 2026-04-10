<template>
  <view v-if="rendered" class="hc-popup-layer" :style="{ zIndex: String(layerZIndex) }">
    <view class="hc-popup-layer__mask" :class="{ 'is-active': active }" @click="handleMaskClick" @touchmove.stop.prevent="noop"></view>
    <view class="hc-popup-layer__viewport">
      <view
        class="hc-popup-layer__panel"
        :class="[{ 'is-active': active }, panelClass]"
        :style="panelStyle"
        @click.stop
        @touchmove.stop
      >
        <slot />
      </view>
    </view>
  </view>
</template>

<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from "vue";
import { nextOverlayZIndex } from "@/utils/overlay";
import { notifyGlobalPopupChange } from "@/utils/popup";

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  maskClosable: {
    type: Boolean,
    default: true,
  },
  panelClass: {
    type: [String, Array, Object],
    default: "",
  },
  panelStyle: {
    type: [String, Array, Object],
    default: "",
  },
});

const emit = defineEmits(["update:modelValue", "change", "mask"]);

const ANIMATION_DURATION = 220;
const rendered = ref(props.modelValue);
const active = ref(props.modelValue);
const layerZIndex = ref(nextOverlayZIndex());
const hasRegisteredGlobalPopup = ref(false);
let leaveTimer = 0;
let openTimer = 0;

const noop = () => {};

const syncGlobalPopupState = (show) => {
  if (show) {
    if (hasRegisteredGlobalPopup.value) return;
    hasRegisteredGlobalPopup.value = true;
    notifyGlobalPopupChange(true);
    return;
  }
  if (!hasRegisteredGlobalPopup.value) return;
  hasRegisteredGlobalPopup.value = false;
  notifyGlobalPopupChange(false);
};

if (props.modelValue) {
  syncGlobalPopupState(true);
}

const clearLeaveTimer = () => {
  if (!leaveTimer) return;
  clearTimeout(leaveTimer);
  leaveTimer = 0;
};

const clearOpenTimer = () => {
  if (!openTimer) return;
  clearTimeout(openTimer);
  openTimer = 0;
};

const openLayer = async () => {
  clearLeaveTimer();
  clearOpenTimer();
  layerZIndex.value = nextOverlayZIndex();
  rendered.value = true;
  await nextTick();
  openTimer = setTimeout(() => {
    active.value = true;
    openTimer = 0;
  }, 16);
};

const closeLayer = () => {
  clearLeaveTimer();
  clearOpenTimer();
  active.value = false;
  leaveTimer = setTimeout(() => {
    rendered.value = false;
    leaveTimer = 0;
  }, ANIMATION_DURATION);
};

watch(
  () => props.modelValue,
  (visible, oldVisible) => {
    if (visible === oldVisible) return;
    if (visible) {
      openLayer();
      syncGlobalPopupState(true);
      emit("change", { show: true });
      return;
    }
    closeLayer();
    syncGlobalPopupState(false);
    emit("change", { show: false });
  }
);

const handleMaskClick = () => {
  emit("mask");
  if (!props.maskClosable) return;
  emit("update:modelValue", false);
};

onBeforeUnmount(() => {
  clearLeaveTimer();
  clearOpenTimer();
  syncGlobalPopupState(false);
});
</script>

<style scoped lang="scss">
.hc-popup-layer {
  position: fixed;
  inset: 0;
  display: block;
  padding: 0;
}

.hc-popup-layer__mask {
  position: absolute;
  inset: 0;
  opacity: 0;
  background: rgba(0, 0, 0, 0.48);
  transition: opacity 220ms ease;
}

.hc-popup-layer__mask.is-active {
  opacity: 1;
}

.hc-popup-layer__viewport {
  position: absolute;
  inset: 0;
  padding: calc(env(safe-area-inset-top) + 8upx) 8upx calc(env(safe-area-inset-bottom) + 8upx);
  display: flex;
  align-items: stretch;
  justify-content: center;
  pointer-events: none;
}

.hc-popup-layer__panel {
  position: relative;
  width: 100%;
  height: 100%;
  max-width: 750upx;
  max-height: none;
  overflow: hidden;
  border-radius: 32upx;
  border: 1upx solid rgba(0, 0, 0, 0.08);
  background: #ffffff;
  box-shadow: 0 20upx 60upx rgba(0, 0, 0, 0.24);
  opacity: 0;
  pointer-events: auto;
  transform: translateY(24upx);
  transition:
    opacity 220ms ease,
    transform 220ms cubic-bezier(0.22, 0.78, 0.22, 1);
}

.hc-popup-layer__panel.is-active {
  opacity: 1;
  transform: translateY(0);
}
</style>
