<template>
  <view class="entry-item" :class="{ 'entry-item-emphasis': emphasis }" @click="$emit('click')">
    <view class="entry-left">
      <view class="entry-icon" :class="{ 'entry-icon-emphasis': emphasis }">
        <uni-icons :type="icon" size="20" :color="emphasis ? '#ffffff' : '#4f7247'" />
      </view>
      <view>
        <text class="entry-title" :class="{ 'entry-title-emphasis': emphasis }">{{ title }}</text>
        <text class="entry-desc" :class="{ 'entry-desc-emphasis': emphasis }">{{ desc }}</text>
      </view>
    </view>
    <text class="entry-arrow" :class="{ 'entry-arrow-emphasis': emphasis }">></text>
  </view>
</template>

<script setup>
defineProps({
  icon: {
    type: String,
    default: "list",
  },
  title: {
    type: String,
    default: "",
  },
  desc: {
    type: String,
    default: "",
  },
  emphasis: {
    type: Boolean,
    default: false,
  },
});

defineEmits(["click"]);
</script>

<style scoped lang="scss">
.entry-item {
  min-height: 118upx;
  border-radius: 28upx;
  border: 1upx solid var(--border-brand);
  background: linear-gradient(160deg, #f8fcef 0%, #eef5e0 100%);
  padding: 16upx 20upx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--box-shadow-sm);
}

.entry-item:active {
  transform: scale(0.99);
}

.entry-item-emphasis {
  border-color: #1f281f;
  background: var(--gradient-contrast);
}

.entry-left {
  display: flex;
  align-items: center;
}

.entry-icon {
  width: 58upx;
  height: 58upx;
  border-radius: 18upx;
  background: var(--bg-chip);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14upx;
}

.entry-icon-emphasis {
  background: rgba(163, 198, 134, 0.24);
}

.entry-title {
  display: block;
  color: var(--text-color);
  font-size: 30upx;
  font-weight: 800;
}

.entry-title-emphasis {
  color: #ffffff;
}

.entry-desc {
  display: block;
  margin-top: 4upx;
  color: var(--text-color-light);
  font-size: 22upx;
}

.entry-desc-emphasis {
  color: rgba(228, 237, 225, 0.9);
}

.entry-arrow {
  color: #7f9181;
  font-size: 34upx;
  font-weight: 700;
}

.entry-arrow-emphasis {
  color: rgba(227, 236, 223, 0.88);
}
</style>
