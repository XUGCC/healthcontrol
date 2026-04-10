<template>
  <view class="hc-toggle" :class="{ 'hc-toggle--active': modelValue, 'hc-toggle--disabled': disabled }" @click="toggle">
    <view class="hc-toggle__thumb"></view>
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:modelValue", "change"]);

const toggle = () => {
  if (props.disabled) return;
  const nextValue = !props.modelValue;
  emit("update:modelValue", nextValue);
  emit("change", nextValue);
};
</script>

<style scoped lang="scss">
.hc-toggle {
  position: relative;
  width: 96upx;
  height: 56upx;
  border-radius: 999upx;
  background: rgba(24, 33, 27, 0.12);
  border: 1upx solid rgba(205, 224, 145, 0.86);
  transition: all 0.24s ease;
}

.hc-toggle__thumb {
  position: absolute;
  top: 4upx;
  left: 4upx;
  width: 46upx;
  height: 46upx;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 8upx 18upx rgba(15, 18, 15, 0.16);
  transition: all 0.24s ease;
}

.hc-toggle--active {
  background: linear-gradient(135deg, #75ae40 0%, #9fd84e 52%, #c2f26f 100%);
  border-color: rgba(156, 195, 77, 0.72);
}

.hc-toggle--active .hc-toggle__thumb {
  left: 44upx;
  background: #f8fce9;
}

.hc-toggle--disabled {
  opacity: 0.56;
}
</style>
