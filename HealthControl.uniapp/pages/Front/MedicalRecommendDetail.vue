<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack recommend-detail-screen">
      <hc-topbar title="就医建议详情" subtitle="查看建议依据、推荐医生和后续动作" showBack @left="back" />

      <view v-if="loading" class="hc-card-soft recommend-detail-empty hc-reveal-up">
        <text class="recommend-detail-empty__title">正在加载建议详情</text>
        <text class="recommend-detail-empty__desc">请稍候片刻，我们正在整理这条建议记录。</text>
      </view>

      <view v-else-if="!detail" class="hc-card-soft recommend-detail-empty hc-reveal-up">
        <text class="recommend-detail-empty__title">暂未找到该建议记录</text>
        <text class="recommend-detail-empty__desc">请返回列表重新进入，或稍后再试。</text>
      </view>

      <template v-else>
        <view class="hc-card-dark recommend-detail-hero hc-reveal-up hc-shine">
          <view class="recommend-detail-hero__head">
            <text class="recommend-detail-hero__risk" :class="`level-${detail.RiskLevel ?? -1}`">{{ riskLevelText }}</text>
            <text class="recommend-detail-hero__status" :class="{ active: !detail.ViewStatus }">
              {{ detail.ViewStatus ? "已查看" : "未查看" }}
            </text>
          </view>
          <text class="recommend-detail-hero__title">{{ detail.Title || "就医建议" }}</text>
          <text class="recommend-detail-hero__desc">推荐时间：{{ detail.RecommendTime || "-" }}</text>
        </view>

        <view class="hc-card-soft recommend-detail-card hc-reveal-up" style="--delay: 100ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">为什么给出这个建议</text>
            <text class="bg-section-head__meta">建议依据</text>
          </view>
          <text class="recommend-detail-card__text">
            {{ detail.RecommendReason || "后端暂未提供详细解释，仅基于风险等级与检测结果给出建议。" }}
          </text>
        </view>

        <view class="hc-card-soft recommend-detail-card hc-reveal-up" style="--delay: 160ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">近期检测与症状概要</text>
            <text class="bg-section-head__meta">{{ riskLevelText }}</text>
          </view>
          <text class="recommend-detail-card__text">风险等级：{{ riskLevelText }}</text>
          <text v-if="detail.DetectSummary" class="recommend-detail-card__text">最近检测：{{ detail.DetectSummary }}</text>
          <text v-if="detail.SymptomSummary" class="recommend-detail-card__text">近期症状：{{ detail.SymptomSummary }}</text>
        </view>

        <view v-if="detail.DoctorInfo" class="hc-card-soft recommend-detail-card hc-reveal-up" style="--delay: 220ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">推荐就诊医生 / 医院</text>
            <text class="bg-section-head__meta">{{ formatDepartment(detail.DoctorInfo.DepartmentType) }}</text>
          </view>

          <view class="recommend-detail-doctor hc-interactive-card" @click="goDoctorDetail(detail.DoctorInfo.Id)">
            <view class="recommend-detail-doctor__avatar-wrap">
              <image
                v-if="detail.DoctorInfo.PicUrl"
                :src="detail.DoctorInfo.PicUrl"
                mode="aspectFill"
                class="recommend-detail-doctor__avatar"
              />
              <view v-else class="recommend-detail-doctor__avatar recommend-detail-doctor__avatar--placeholder">
                <uni-icons type="person" size="26" color="#172119" />
              </view>
            </view>
            <view class="recommend-detail-doctor__body">
              <text class="recommend-detail-doctor__name">{{ detail.DoctorInfo.DoctorName || "医生" }}</text>
              <text v-if="detail.DoctorInfo.DoctorTitle" class="recommend-detail-doctor__title">{{ detail.DoctorInfo.DoctorTitle }}</text>
              <text class="recommend-detail-doctor__hospital">{{ detail.DoctorInfo.HospitalName || "医院名称" }}</text>
              <text class="recommend-detail-doctor__meta">{{ detail.DoctorInfo.Region || "地区待补充" }}</text>
            </view>
          </view>

          <view class="recommend-detail-doctor__actions">
            <view
              v-if="detail.DoctorInfo.ContactPhone"
              class="recommend-detail-doctor__action"
              @click="makePhoneCall(detail.DoctorInfo.ContactPhone)"
            >
              拨打电话
            </view>
            <view
              v-if="detail.DoctorInfo.AppointmentUrl"
              class="recommend-detail-doctor__action recommend-detail-doctor__action--ghost"
              @click="openAppointment(detail.DoctorInfo.AppointmentUrl)"
            >
              打开预约链接
            </view>
          </view>
        </view>

        <view class="hc-card-lime recommend-detail-next hc-reveal-up" style="--delay: 280ms">
          <text class="recommend-detail-next__title">建议如何行动</text>
          <text class="recommend-detail-next__desc">{{ actionText }}</text>
          <view class="recommend-detail-next__actions">
            <view class="hc-pill-button-dark hc-interactive-pill" @click="goPrepareReport">生成就诊准备报告</view>
            <view v-if="UserId" class="hc-pill-button hc-interactive-pill" @click="goToLabelForm">{{ labelActionText }}</view>
          </view>
        </view>

        <view class="hc-card-soft recommend-detail-tip hc-reveal-up" style="--delay: 340ms">
          <text class="recommend-detail-tip__title">提醒</text>
          <text class="recommend-detail-tip__desc">
            本就医建议仅基于您在本小程序中的检测与记录信息，不能替代医生的专业诊断。如有疑虑，请以线下专科医生意见为准。
          </text>
        </view>
      </template>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const commonStore = useCommonStore();
const UserId = computed(() => commonStore.UserId);

const detail = ref(null);
const loading = ref(false);
const existingLabelId = ref(null);
const dialogRef = ref(null);

const labelActionText = computed(() => (existingLabelId.value ? "修改标注" : "去标注"));
const riskLevelText = computed(() => {
  const value = detail.value?.RiskLevel;
  if (value === 2) return "高风险";
  if (value === 1) return "中风险";
  if (value === 0) return "低风险";
  return "未知风险";
});
const actionText = computed(() => {
  if (detail.value?.RiskLevel === 2) {
    return "当前为高风险，建议尽快线下就诊或复查。如出现呼吸困难、吞咽明显困难、持续咯血等情况，请及时就近急诊。";
  }
  if (detail.value?.RiskLevel === 1) {
    return "当前为中风险，建议近期安排线下就诊或复查，并持续关注症状变化。";
  }
  return "当前为低风险，如持续不适或症状加重，建议就医咨询耳鼻喉科医生。";
});

const back = () => {
  uni.navigateBack();
};
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const formatDepartment = (value) => {
  if (value === 0) return "耳鼻喉科";
  if (value === 1) return "咽喉科";
  if (value === 2) return "头颈外科";
  return "耳鼻喉相关";
};

const loadDetail = async (recommendId) => {
  loading.value = true;
  try {
    const res = await Post("/Front/Medical/RecommendDetail", {
      RecommendId: Number(recommendId) || 0,
    });
    if (!res.Success) {
      uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
      return;
    }
    detail.value = res.Data || null;
    await loadLabelStatus(detail.value?.DetectId || detail.value?.DetectDto?.Id);
    if (recommendId) {
      await Post("/Front/Medical/RecommendMarkViewed", {
        RecommendId: Number(recommendId) || 0,
      });
      if (detail.value) {
        detail.value.ViewStatus = true;
      }
    }
  } finally {
    loading.value = false;
  }
};

const loadLabelStatus = async (detectId) => {
  if (!detectId || !UserId.value) {
    existingLabelId.value = null;
    return;
  }
  try {
    const { Data, Success } = await Post("/Front/ModelLabel/Get", {
      DetectId: Number(detectId),
    });
    existingLabelId.value = Success && Data && Data.Id ? Data.Id : null;
  } catch (error) {
    console.error("加载标注状态失败:", error);
    existingLabelId.value = null;
  }
};

const goDoctorDetail = (doctorId) => {
  if (!doctorId) return;
  uni.navigateTo({ url: `/pages/Front/HospitalDoctorDetail?doctorId=${doctorId}` });
};

const makePhoneCall = (phone) => {
  if (!phone) return;
  uni.makePhoneCall({ phoneNumber: phone.toString() });
};

const openAppointment = (url) => {
  if (!url) return;
  showDialog({
    title: "即将打开预约链接",
    content: "将复制链接到剪贴板，请在浏览器中打开完成预约。",
  }).then((res) => {
      if (!res.confirm) return;
      uni.setClipboardData({
        data: url,
        success: () => {
          uni.showToast({ icon: "none", title: "已复制链接" });
        },
      });
    });
};

const goPrepareReport = () => {
  uni.navigateTo({ url: "/pages/Front/MedicalPrepareReport" });
};

const goToLabelForm = () => {
  const detectId = detail.value?.DetectId || detail.value?.DetectDto?.Id;
  if (!detectId) {
    uni.showToast({ title: "未找到关联的检测记录", icon: "none" });
    return;
  }
  uni.navigateTo({ url: `/pages/Front/ModelLabelForm?detectId=${detectId}` });
};

onLoad((options) => {
  const recommendId = options?.recommendId || options?.id;
  if (recommendId) {
    loadDetail(recommendId);
    return;
  }
  uni.showToast({ icon: "none", title: "参数缺失，无法加载详情" });
});
</script>

<style scoped lang="scss">
.recommend-detail-screen {
  gap: 24upx;
}

.recommend-detail-empty,
.recommend-detail-tip {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.recommend-detail-empty__title,
.recommend-detail-next__title,
.recommend-detail-tip__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.recommend-detail-empty__desc,
.recommend-detail-card__text,
.recommend-detail-next__desc,
.recommend-detail-tip__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.recommend-detail-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.recommend-detail-hero__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
}

.recommend-detail-hero__risk,
.recommend-detail-hero__status {
  padding: 8upx 14upx;
  border-radius: 999upx;
  font-size: 20upx;
  font-weight: 800;
}

.recommend-detail-hero__risk {
  background: rgba(225, 243, 154, 0.16);
  color: #cfe98d;
}

.recommend-detail-hero__risk.level-1 {
  color: #efdc89;
}

.recommend-detail-hero__risk.level-2 {
  color: #f3b39c;
}

.recommend-detail-hero__status {
  background: rgba(241, 248, 223, 0.08);
  color: #f7ffdf;
}

.recommend-detail-hero__status.active {
  color: #f3b39c;
}

.recommend-detail-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.recommend-detail-hero__desc {
  display: block;
  font-size: 22upx;
  color: rgba(241, 248, 223, 0.74);
}

.recommend-detail-doctor {
  display: flex;
  gap: 18upx;
  margin-top: 12upx;
}

.recommend-detail-doctor__avatar {
  width: 108upx;
  height: 108upx;
  border-radius: 28upx;
  background: rgba(225, 243, 154, 0.32);
  display: flex;
  align-items: center;
  justify-content: center;
}

.recommend-detail-doctor__avatar--placeholder {
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.recommend-detail-doctor__body {
  flex: 1;
  min-width: 0;
}

.recommend-detail-doctor__name {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.recommend-detail-doctor__title,
.recommend-detail-doctor__hospital,
.recommend-detail-doctor__meta {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  color: #556556;
}

.recommend-detail-doctor__actions,
.recommend-detail-next__actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
  margin-top: 16upx;
}

.recommend-detail-doctor__action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 80upx;
  padding: 0 26upx;
  border-radius: 999upx;
  background: #172119;
  font-size: 24upx;
  font-weight: 700;
  color: #f7ffdf;
}

.recommend-detail-doctor__action--ghost {
  background: rgba(225, 243, 154, 0.42);
  color: #172119;
}
</style>
