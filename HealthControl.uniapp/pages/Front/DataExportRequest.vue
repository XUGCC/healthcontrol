<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack export-request-screen">
      <hc-topbar
        title="数据导出申请"
        subtitle="选择导出范围、格式和补充说明后提交申请"
        rightText="申请记录"
        showBack
        @left="back"
        @right="goHistory"
      />

      <view class="hc-card-dark export-request-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">数据导出</view>
        <text class="export-request-hero__title">先选需要导出的数据，再决定用哪种格式接收</text>
        <text class="export-request-hero__desc">至少选择一种数据类型。审核通过后，您可在申请记录中生成并下载导出文件。</text>
      </view>

      <view class="hc-card-soft export-request-card hc-reveal-up" style="--delay: 100ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">导出数据类型</text>
          <text class="bg-section-head__meta">{{ selectedTypes.length }} 项已选</text>
        </view>
        <view class="export-request-tags">
          <view
            v-for="item in dataTypes"
            :key="item.Value"
            class="export-request-tag hc-interactive-pill"
            :class="{ active: selectedTypes.includes(item.Value) }"
            @click="toggleType(item.Value)"
          >
            {{ item.Label }}
          </view>
        </view>
      </view>

      <view class="hc-card-soft export-request-card hc-reveal-up" style="--delay: 160ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">导出格式</text>
          <text class="bg-section-head__meta">{{ exportFormat }}</text>
        </view>
        <view class="hc-token-tabs">
          <view class="hc-token-tabs__item" :class="{ active: exportFormat === 'JSON' }" @click="exportFormat = 'JSON'">JSON</view>
          <view class="hc-token-tabs__item" :class="{ active: exportFormat === 'EXCEL' }" @click="exportFormat = 'EXCEL'">Excel</view>
          <view class="hc-token-tabs__item" :class="{ active: exportFormat === 'PDF' }" @click="exportFormat = 'PDF'">PDF</view>
        </view>
      </view>

      <view class="hc-card-soft export-request-card hc-reveal-up" style="--delay: 220ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">申请说明</text>
          <text class="bg-section-head__meta">可选</text>
        </view>
        <uni-easyinput
          v-model="formData.RequestDesc"
          type="textarea"
          maxlength="200"
          placeholder="如有特殊要求，请在此说明（最多 200 字）"
        />
      </view>

      <view class="hc-card-lime export-request-actions hc-reveal-up" style="--delay: 280ms">
        <text class="export-request-actions__title">提交前确认</text>
        <text class="export-request-actions__desc">导出申请会进入管理员审核流程，审核通过后才能生成下载文件。</text>
        <view class="export-request-actions__buttons">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="submit">提交导出申请</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="goHistory">查看申请记录</view>
        </view>
      </view>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { ref } from "vue";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const dataTypes = ref([
  { Value: "AudioRecord", Label: "音频自查记录" },
  { Value: "HealthRecord", Label: "健康档案记录" },
  { Value: "SymptomLog", Label: "症状日志" },
  { Value: "DietRecord", Label: "饮食记录" },
  { Value: "ModelLabel", Label: "确诊标注记录" },
  { Value: "QuestionnaireAnswer", Label: "问卷答题记录" },
  { Value: "HealthRemind", Label: "健康提醒设置" },
  { Value: "MessageNotice", Label: "消息通知记录" },
]);

const selectedTypes = ref(["AudioRecord", "HealthRecord", "ModelLabel"]);
const exportFormat = ref("JSON");
const formData = ref({
  RequestDesc: "",
});
const dialogRef = ref(null);

const back = () => {
  uni.navigateBack({ delta: 1 });
};
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const toggleType = (value) => {
  if (selectedTypes.value.includes(value)) {
    selectedTypes.value = selectedTypes.value.filter((item) => item !== value);
    return;
  }
  selectedTypes.value = selectedTypes.value.concat(value);
};

const goHistory = () => {
  uni.navigateTo({ url: "/pages/Front/DataRightsHistory" });
};

const submit = async () => {
  if (!selectedTypes.value.length) {
    uni.showToast({ title: "请至少选择一种数据类型", icon: "none" });
    return;
  }
  uni.showLoading({ title: "提交中..." });
  try {
    const { Data, Success } = await Post("/Front/Privacy/ExportRequest", {
      DataType: selectedTypes.value,
      ExportFormat: exportFormat.value,
      RequestDesc: formData.value.RequestDesc,
    });
    if (!Success) return;
    const requestId = Data?.RequestId;
    if (!requestId) {
      uni.showToast({ title: "提交成功，但未获取到申请编号", icon: "none" });
      return;
    }
    await showDialog({
      title: "导出申请已提交",
      content: "申请将进入管理员审核流程。审核通过后，您可在申请记录中生成并下载导出文件。",
      showCancel: false,
    });
    goHistory();
  } finally {
    uni.hideLoading();
  }
};
</script>

<style scoped lang="scss">
.export-request-screen {
  gap: 24upx;
}

.export-request-hero,
.export-request-card,
.export-request-actions {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.export-request-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.export-request-hero__desc,
.export-request-actions__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.export-request-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.export-request-tag {
  padding: 14upx 18upx;
  border-radius: 999upx;
  background: rgba(225, 243, 154, 0.26);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  font-size: 22upx;
  font-weight: 700;
  color: #172119;
}

.export-request-tag.active {
  background: #172119;
  border-color: #172119;
  color: #f7ffdf;
}

.export-request-actions__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.export-request-actions__desc {
  color: #455442;
}

.export-request-actions__buttons {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}
</style>
