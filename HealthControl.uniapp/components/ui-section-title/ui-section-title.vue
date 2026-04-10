<template>
  <view class="hc-section-title">
    <text class="hc-section-title__text">{{ title }}</text>
    <text class="hc-section-title__sub" v-if="subtitle">{{ subtitle }}</text>
  </view>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: "",
  },
  subtitle: {
    type: String,
    default: "",
  },
});
</script>
