<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack doctor-detail-screen">
      <hc-topbar title="医生详情" subtitle="查看坐诊信息、预约方式和就诊准备入口" showBack @left="back" />

      <template v-if="detail">
        <view class="hc-card-dark doctor-detail-hero hc-reveal-up hc-shine">
          <view class="doctor-detail-hero__main">
            <view class="doctor-detail-hero__avatar-wrap">
              <image v-if="detail.PicUrl" :src="detail.PicUrl" mode="aspectFill" class="doctor-detail-hero__avatar" />
              <view v-else class="doctor-detail-hero__avatar doctor-detail-hero__avatar--placeholder">
                <uni-icons type="person" size="30" color="#172119" />
              </view>
            </view>
            <view class="doctor-detail-hero__body">
              <view class="hc-kicker hc-kicker--dark">医生信息</view>
              <text class="doctor-detail-hero__name">{{ detail.DoctorName || "医生" }}</text>
              <text v-if="detail.DoctorTitle" class="doctor-detail-hero__title">{{ detail.DoctorTitle }}</text>
              <text class="doctor-detail-hero__hospital">{{ detail.HospitalName || "医院名称" }}</text>
              <view class="doctor-detail-hero__tags">
                <text class="doctor-detail-hero__tag">{{ formatDepartment(detail.DepartmentType) }}</text>
                <text v-if="detail.HospitalLevel" class="doctor-detail-hero__tag">{{ detail.HospitalLevel }}</text>
                <text v-for="tag in tagList" :key="tag" class="doctor-detail-hero__tag doctor-detail-hero__tag--soft">{{ tag }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="hc-card-soft doctor-detail-card hc-reveal-up" style="--delay: 100ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">擅长方向 / 简介</text>
            <text class="bg-section-head__meta">专业说明</text>
          </view>
          <text class="doctor-detail-card__text">{{ detail.SpecialtyDesc || "暂无擅长方向简介。" }}</text>
        </view>

        <view class="hc-card-soft doctor-detail-card hc-reveal-up" style="--delay: 160ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">坐诊信息</text>
            <text class="bg-section-head__meta">{{ detail.Region || "地区待补充" }}</text>
          </view>
          <text class="doctor-detail-card__text">医院地址：{{ detail.Address || "暂无地址信息" }}</text>
          <text class="doctor-detail-card__text">联系电话：{{ detail.ContactPhone || "暂无电话信息" }}</text>
          <text v-if="detail.OutpatientTime" class="doctor-detail-card__text">门诊时间：{{ detail.OutpatientTime }}</text>
          <view class="doctor-detail-card__actions">
            <view class="doctor-detail-card__action" @click="openMap">地图导航</view>
            <view v-if="detail.ContactPhone" class="doctor-detail-card__action doctor-detail-card__action--ghost" @click="makePhoneCall(detail.ContactPhone)">
              拨打电话
            </view>
          </view>
        </view>

        <view class="hc-card-lime doctor-detail-cta hc-reveal-up" style="--delay: 220ms">
          <text class="doctor-detail-cta__title">下一步建议</text>
          <text class="doctor-detail-cta__desc">
            可以先复制预约链接，或把最近检测和症状整理成就诊准备报告，方便和医生沟通。
          </text>
          <view class="doctor-detail-cta__actions">
            <view
              v-if="detail.AppointmentUrl"
              class="hc-pill-button-dark hc-interactive-pill"
              @click="openAppointment(detail.AppointmentUrl)"
            >
              打开预约链接
            </view>
            <view class="hc-pill-button hc-interactive-pill" @click="goPrepareReport">生成就诊准备报告</view>
          </view>
        </view>

        <view class="hc-card-soft doctor-detail-tip hc-reveal-up" style="--delay: 280ms">
          <text class="doctor-detail-tip__title">提示</text>
          <text class="doctor-detail-tip__desc">
            本页面信息仅供参考，不构成诊疗建议。请以医院现场公示和医生实际坐诊安排为准。
          </text>
        </view>
      </template>

      <view v-else class="hc-card-soft doctor-detail-empty hc-reveal-up">
        <text class="doctor-detail-empty__title">正在加载医生详情</text>
        <text class="doctor-detail-empty__desc">如果长时间没有内容，请返回列表重新进入。</text>
      </view>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const detail = ref(null);
const dialogRef = ref(null);

const tagList = computed(() => {
  if (!detail.value || !detail.value.Tags) return [];
  return detail.value.Tags.split(/[，,]/).map((item) => item.trim()).filter(Boolean);
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

const loadDetail = async (doctorId) => {
  const res = await Post("/Front/Medical/DoctorDetail", {
    DoctorId: Number(doctorId) || 0,
  });
  if (!res.Success) {
    uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
    return;
  }
  detail.value = res.Data || null;
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

const openMap = () => {
  if (!detail.value) return;
  const current = detail.value;
  if (current.Longitude != null && current.Latitude != null) {
    const longitude = Number(current.Longitude);
    const latitude = Number(current.Latitude);
    if (!Number.isNaN(longitude) && !Number.isNaN(latitude)) {
      uni.openLocation({
        longitude,
        latitude,
        name: current.HospitalName || "医院",
        address: current.Address || "",
      });
      return;
    }
  }
  if (current.Address) {
    uni.setClipboardData({
      data: current.Address,
      success: () => {
        uni.showToast({ icon: "none", title: "已复制地址，可在地图中粘贴搜索" });
      },
    });
    return;
  }
  uni.showToast({ icon: "none", title: "暂无可用地址信息" });
};

const goPrepareReport = () => {
  uni.navigateTo({ url: "/pages/Front/MedicalPrepareReport" });
};

onLoad((options) => {
  if (options && options.doctorId) {
    loadDetail(options.doctorId);
    return;
  }
  uni.showToast({ icon: "none", title: "参数缺失，无法加载详情" });
});
</script>

<style scoped lang="scss">
.doctor-detail-screen {
  gap: 24upx;
}

.doctor-detail-hero {
  display: flex;
  flex-direction: column;
  gap: 18upx;
}

.doctor-detail-hero__main {
  display: flex;
  gap: 18upx;
}

.doctor-detail-hero__avatar-wrap {
  flex-shrink: 0;
}

.doctor-detail-hero__avatar {
  width: 132upx;
  height: 132upx;
  border-radius: 32upx;
  background: rgba(225, 243, 154, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}

.doctor-detail-hero__avatar--placeholder {
  border: 1upx solid rgba(202, 235, 134, 0.18);
}

.doctor-detail-hero__body {
  flex: 1;
  min-width: 0;
}

.doctor-detail-hero__name {
  display: block;
  margin-top: 10upx;
  font-size: 42upx;
  line-height: 1.1;
  font-weight: 800;
  color: #f7ffdf;
}

.doctor-detail-hero__title,
.doctor-detail-hero__hospital {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  color: rgba(241, 248, 223, 0.76);
}

.doctor-detail-hero__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10upx;
  margin-top: 12upx;
}

.doctor-detail-hero__tag {
  padding: 8upx 14upx;
  border-radius: 999upx;
  background: rgba(241, 248, 223, 0.08);
  font-size: 20upx;
  color: #f7ffdf;
}

.doctor-detail-hero__tag--soft {
  color: rgba(241, 248, 223, 0.72);
}

.doctor-detail-card__text,
.doctor-detail-cta__desc,
.doctor-detail-tip__desc,
.doctor-detail-empty__desc {
  display: block;
  margin-top: 12upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.doctor-detail-card__actions,
.doctor-detail-cta__actions {
  display: flex;
  flex-direction: column;
  gap: 12upx;
  margin-top: 16upx;
}

.doctor-detail-card__action {
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

.doctor-detail-card__action--ghost {
  background: rgba(225, 243, 154, 0.42);
  color: #172119;
}

.doctor-detail-cta__title,
.doctor-detail-tip__title,
.doctor-detail-empty__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.doctor-detail-tip,
.doctor-detail-empty {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}
</style>
