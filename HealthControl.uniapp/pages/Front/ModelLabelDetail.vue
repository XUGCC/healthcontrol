<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack model-detail-screen">
      <hc-topbar title="标注详情" subtitle="先看摘要，再展开关联检测、图像和授权信息" :show-back="true" @left="back" />

      <view v-if="loading" class="hc-card-soft model-detail-state hc-reveal-up">
        <text class="model-detail-state__title">正在加载标注详情</text>
        <text class="model-detail-state__desc">确诊结果、授权信息和喉镜资料会一起整理出来。</text>
      </view>

      <view v-else-if="!detail" class="hc-card-soft model-detail-state hc-reveal-up">
        <text class="model-detail-state__title">当前标注记录不存在</text>
        <text class="model-detail-state__desc">可以返回上一页重新选择标注记录。</text>
      </view>

      <template v-else>
        <view class="hc-card-dark model-detail-hero hc-reveal-up hc-shine">
          <view class="hc-kicker hc-kicker--dark">确诊反馈</view>
          <text class="model-detail-hero__title">{{ detail.HospitalDiagnoseResult || "未填写确诊结果" }}</text>
          <view class="model-detail-hero__meta">
            <view class="model-detail-hero__badge" :class="{ 'model-detail-hero__badge--warn': !detail.LabelType }">
              {{ detail.LabelType ? "确诊" : "误报" }}
            </view>
            <view class="model-detail-hero__badge" :class="{ 'model-detail-hero__badge--warn': !detail.AuthStatus }">
              {{ detail.AuthStatus ? "已授权" : "未授权" }}
            </view>
          </view>
          <text v-if="detail.LabelDesc" class="model-detail-hero__desc">{{ detail.LabelDesc }}</text>
        </view>

        <view class="hc-card-soft model-detail-section hc-reveal-up" style="--delay: 90ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">检测记录信息</text>
            <text class="bg-section-head__meta">回看触发这次反馈的检测结果</text>
          </view>
          <view class="model-detail-grid">
            <view class="model-detail-grid__item">
              <text class="model-detail-grid__label">检测时间</text>
              <text class="model-detail-grid__value">{{ formatTime(detail.DetectDto?.CreationTime) || "-" }}</text>
            </view>
            <view class="model-detail-grid__item">
              <text class="model-detail-grid__label">初筛结果</text>
              <text class="model-detail-grid__value">{{ getResultText(detail.DetectDto) }}</text>
            </view>
            <view class="model-detail-grid__item">
              <text class="model-detail-grid__label">置信度</text>
              <text class="model-detail-grid__value">
                {{ detail.DetectDto?.PrimaryScreenConfidence ? `${(detail.DetectDto.PrimaryScreenConfidence * 100).toFixed(1)}%` : "-" }}
              </text>
            </view>
            <view class="model-detail-grid__item">
              <text class="model-detail-grid__label">更新时间</text>
              <text class="model-detail-grid__value">{{ formatTime(detail.UpdateTime || detail.CreationTime) || "-" }}</text>
            </view>
          </view>
        </view>

        <view v-if="laryngoscopeList.length" class="hc-card-soft model-detail-section hc-reveal-up" style="--delay: 150ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">关联喉镜资料</text>
            <text class="bg-section-head__meta">作为线下检查记录参考展示</text>
          </view>
          <view class="model-detail-photos">
            <view v-for="photo in laryngoscopeList" :key="photo.Id" class="model-detail-photo hc-interactive-card" @click="previewLaryngoscope(photo)">
              <image class="model-detail-photo__image" :src="photo.LaryngoscopePhotoUrl" mode="aspectFill" />
              <text class="model-detail-photo__title">{{ buildLaryngoscopeTitle(photo) }}</text>
              <text class="model-detail-photo__time">{{ formatTime(photo.UploadTime) }}</text>
            </view>
          </view>
        </view>

        <view class="hc-card-lime model-detail-auth hc-reveal-up" style="--delay: 210ms">
          <text class="model-detail-auth__title">{{ detail.AuthStatus ? "当前匿名数据已授权用于模型优化" : "当前记录仍只保留在个人账户里" }}</text>
          <text class="model-detail-auth__desc">
            {{ detail.AuthStatus ? "如果你后续改变想法，仍可在这里取消授权。" : "如果你希望帮助后续模型优化，可以在这里开启授权。" }}
          </text>
        </view>

        <view class="model-detail-actions hc-reveal-up" style="--delay: 270ms">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="toggleAuthStatus">
            {{ detail.AuthStatus ? "取消授权" : "开启授权" }}
          </view>
          <view class="hc-pill-button hc-interactive-pill model-detail-actions__danger" @click="deleteLabel">删除标注</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="back">返回列表</view>
        </view>
      </template>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import { useCommonStore } from "@/store";
import { Post } from "@/utils/http";

const commonStore = useCommonStore();
const userId = computed(() => commonStore.UserId);

const detail = ref(null);
const loading = ref(false);
const laryngoscopeList = ref([]);
const dialogRef = ref(null);

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/ModelLabelList" });
};
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const formatTime = (timeStr) => {
  if (!timeStr) return "";
  const date = new Date(timeStr);
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day} ${hours}:${minutes}`;
};

const getResultText = (item) => {
  if (!item) return "未知";
  if (item.PrimaryScreenResult === 0) return "良性";
  if (item.PrimaryScreenResult === 1) return "恶性倾向";
  return "未知";
};

const loadLaryngoscope = async (detectId) => {
  const { Data, Success } = await Post("/Front/Laryngoscope/List", {
    Page: 1,
    Limit: 6,
    DetectId: Number(detectId),
  });

  if (!Success) {
    laryngoscopeList.value = [];
    return;
  }

  laryngoscopeList.value = (Data?.Items || []).filter((item) => item?.LaryngoscopePhotoUrl && item.IsDelete !== true);
};

const loadDetail = async (labelId) => {
  loading.value = true;
  try {
    const { Data, Success } = await Post("/Front/ModelLabel/Get", {
      Id: labelId,
      UserId: userId.value,
    });
    if (!Success || !Data) {
      detail.value = null;
      return;
    }

    detail.value = Data;
    const detectId = Data.DetectId || Data.DetectDto?.Id;
    if (detectId) {
      await loadLaryngoscope(detectId);
    }
  } finally {
    loading.value = false;
  }
};

const toggleAuthStatus = async () => {
  if (!detail.value) return;
  const nextValue = !detail.value.AuthStatus;
  uni.showLoading({ title: "处理中..." });
  try {
    const { Success } = await Post("/Front/ModelLabel/UpdateAuth", {
      Id: detail.value.Id,
      UserId: userId.value,
      AuthStatus: nextValue,
    });
    if (!Success) return;

    detail.value.AuthStatus = nextValue;
    detail.value.UpdateTime = new Date().toISOString();
    uni.showToast({ title: "操作成功", icon: "success" });
  } finally {
    uni.hideLoading();
  }
};

const deleteLabel = () => {
  if (!detail.value) return;
  showDialog({
    title: "确认删除",
    content: "确定要删除这条标注记录吗？",
  }).then(async (result) => {
      if (!result.confirm) return;
      uni.showLoading({ title: "删除中..." });
      try {
        const { Success } = await Post("/Front/ModelLabel/Delete", {
          Id: detail.value.Id,
          UserId: userId.value,
        });
        if (!Success) return;

        uni.showToast({ title: "删除成功", icon: "success" });
        setTimeout(() => {
          uni.navigateBack({ delta: 1 });
        }, 500);
      } finally {
        uni.hideLoading();
      }
    });
};

const buildLaryngoscopeTitle = (item) => {
  if (!item) return "喉镜检查";
  if (item.PhotoDesc) {
    const first = String(item.PhotoDesc).split("|")[0]?.trim();
    if (first) return first;
  }
  if (item.UploadTime) {
    return `检查时间：${formatTime(item.UploadTime)}`;
  }
  return "喉镜检查";
};

const previewLaryngoscope = (item) => {
  if (!item?.LaryngoscopePhotoUrl) return;
  uni.previewImage({
    urls: [item.LaryngoscopePhotoUrl],
    current: item.LaryngoscopePhotoUrl,
  });
};

onLoad((options) => {
  if (!userId.value) {
    uni.showToast({ title: "请先登录", icon: "none" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/Front/Login" });
    }, 1200);
    return;
  }

  if (!options?.id) {
    uni.showToast({ title: "参数错误", icon: "none" });
    setTimeout(() => {
      back();
    }, 600);
    return;
  }

  loadDetail(Number(options.id));
});
</script>

<style scoped lang="scss">
.model-detail-screen {
  gap: 24upx;
}

.model-detail-state__title,
.model-detail-auth__title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.model-detail-state__desc,
.model-detail-auth__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.model-detail-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.model-detail-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.12;
  font-weight: 800;
  color: #f7ffdf;
}

.model-detail-hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.model-detail-hero__badge {
  min-height: 56upx;
  padding: 0 18upx;
  border-radius: 999upx;
  background: rgba(140, 207, 67, 0.14);
  color: #f7ffdf;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
}

.model-detail-hero__badge--warn {
  background: rgba(243, 188, 171, 0.18);
}

.model-detail-hero__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.76);
}

.model-detail-grid {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14upx;
}

.model-detail-grid__item {
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.model-detail-grid__label {
  display: block;
  font-size: 20upx;
  color: #7c8e7b;
}

.model-detail-grid__value {
  display: block;
  margin-top: 8upx;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.model-detail-photos {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14upx;
}

.model-detail-photo {
  padding: 14upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.model-detail-photo__image {
  width: 100%;
  height: 220upx;
  border-radius: 22upx;
  background: #dfe7d2;
}

.model-detail-photo__title {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  font-weight: 800;
  color: #172119;
}

.model-detail-photo__time {
  display: block;
  margin-top: 6upx;
  font-size: 20upx;
  color: #7c8e7b;
}

.model-detail-actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.model-detail-actions__danger {
  background: rgba(243, 188, 171, 0.86);
  border-color: rgba(220, 129, 105, 0.72);
  color: #5f271d;
}
</style>
