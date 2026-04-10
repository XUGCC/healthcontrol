<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack delete-request-screen">
      <hc-topbar
        title="数据删除申请"
        subtitle="高风险操作会单独突出，请确认后再提交"
        rightText="申请记录"
        showBack
        @left="back"
        @right="goHistory"
      />

      <view class="hc-card-soft delete-request-warning hc-reveal-up">
        <text class="delete-request-warning__title">重要提示</text>
        <text class="delete-request-warning__desc">
          删除操作不可恢复，请谨慎选择。已授权用于模型优化的数据可能已进入历史训练，无法从模型中完全移除。
        </text>
      </view>

      <view class="hc-card-soft delete-request-card hc-reveal-up" style="--delay: 100ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">删除数据类型</text>
          <text class="bg-section-head__meta">{{ selectedTypes.length }} 项已选</text>
        </view>
        <view class="delete-request-tags">
          <view
            v-for="item in dataTypes"
            :key="item.Value"
            class="delete-request-tag hc-interactive-pill"
            :class="{ active: selectedTypes.includes(item.Value) }"
            @click="toggleType(item.Value)"
          >
            {{ item.Label }}
          </view>
        </view>
      </view>

      <view class="hc-card-soft delete-request-card hc-reveal-up" style="--delay: 160ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">删除原因</text>
          <text class="bg-section-head__meta">{{ formData.DeleteReason || "可选" }}</text>
        </view>
        <view class="delete-request-reasons">
          <view
            v-for="item in reasonOptions"
            :key="item"
            class="delete-request-reason hc-interactive-pill"
            :class="{ active: formData.DeleteReason === item }"
            @click="pickReason(item)"
          >
            {{ item }}
          </view>
        </view>

        <uni-easyinput
          v-if="formData.DeleteReason === '其他原因'"
          v-model="formData.DeleteReasonDesc"
          type="textarea"
          maxlength="200"
          placeholder="请补充说明（最多 200 字）"
        />
      </view>

      <view class="hc-card-soft delete-request-card hc-reveal-up" style="--delay: 220ms">
        <view class="delete-request-confirm" :class="{ active: confirmed }" @click="confirmed = !confirmed">
          <view class="delete-request-confirm__dot">
            <uni-icons v-if="confirmed" type="checkmarkempty" size="16" color="#f7ffdf" />
          </view>
          <text class="delete-request-confirm__text">我已了解删除操作的后果，确认提交删除申请</text>
        </view>
      </view>

      <view class="hc-card-lime delete-request-actions hc-reveal-up" style="--delay: 280ms">
        <text class="delete-request-actions__title">提交前再确认</text>
        <text class="delete-request-actions__desc">删除申请提交后将进入管理员审核流程。审核通过后才会执行删除。</text>
        <view class="delete-request-actions__buttons">
          <view class="hc-pill-button-dark hc-interactive-pill delete-request-actions__danger" @click="submit">提交删除申请</view>
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
  { Value: "Account", Label: "账户信息（暂不支持自动删除）" },
]);

const reasonOptions = ref(["不再使用该服务", "隐私担忧", "数据不准确", "其他原因"]);

const selectedTypes = ref([]);
const confirmed = ref(false);
const formData = ref({
  DeleteReason: "",
  DeleteReasonDesc: "",
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

const pickReason = (value) => {
  formData.value.DeleteReason = value;
  if (value !== "其他原因") {
    formData.value.DeleteReasonDesc = "";
  }
};

const goHistory = () => {
  uni.navigateTo({ url: "/pages/Front/DataRightsHistory" });
};

const submit = async () => {
  if (!selectedTypes.value.length) {
    uni.showToast({ title: "请至少选择一种数据类型", icon: "none" });
    return;
  }
  if (!confirmed.value) {
    uni.showToast({ title: "请先勾选确认", icon: "none" });
    return;
  }
  const result = await showDialog({
    title: "确认提交",
    content: "删除申请提交后将进入管理员审核流程。审核通过后才会执行删除。是否确认提交？",
  });
  if (!result.confirm) return;
  uni.showLoading({ title: "提交中..." });
  try {
    const { Success } = await Post("/Front/Privacy/DeleteRequest", {
      DataType: selectedTypes.value,
      DeleteReason: formData.value.DeleteReason,
      DeleteReasonDesc: formData.value.DeleteReasonDesc,
    });
    if (Success) {
      uni.showToast({ title: "已提交", icon: "success" });
      setTimeout(() => goHistory(), 600);
    }
  } finally {
    uni.hideLoading();
  }
};
</script>

<style scoped lang="scss">
.delete-request-screen {
  gap: 24upx;
}

.delete-request-warning,
.delete-request-card,
.delete-request-actions {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.delete-request-warning {
  background: linear-gradient(135deg, rgba(255, 243, 231, 0.95) 0%, rgba(255, 251, 245, 0.95) 100%);
  border: 1upx solid rgba(239, 179, 77, 0.45);
}

.delete-request-warning__title,
.delete-request-actions__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #8e4e21;
}

.delete-request-warning__desc,
.delete-request-actions__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #7a4b27;
}

.delete-request-tags,
.delete-request-reasons {
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.delete-request-tag,
.delete-request-reason {
  padding: 14upx 18upx;
  border-radius: 999upx;
  background: rgba(245, 247, 235, 0.98);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  font-size: 22upx;
  font-weight: 700;
  color: #172119;
}

.delete-request-tag.active,
.delete-request-reason.active {
  background: #172119;
  border-color: #172119;
  color: #f7ffdf;
}

.delete-request-confirm {
  display: flex;
  align-items: center;
  gap: 16upx;
  padding: 6upx 0;
}

.delete-request-confirm__dot {
  width: 40upx;
  height: 40upx;
  border-radius: 999upx;
  background: rgba(205, 224, 145, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.delete-request-confirm.active .delete-request-confirm__dot {
  background: #172119;
}

.delete-request-confirm__text {
  font-size: 24upx;
  line-height: 1.6;
  color: #172119;
}

.delete-request-actions__buttons {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.delete-request-actions__danger {
  background: linear-gradient(135deg, #cf6641 0%, #e46c49 100%);
}
</style>
