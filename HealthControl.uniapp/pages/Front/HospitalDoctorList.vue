<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack doctor-list-screen">
      <hc-topbar
        title="医院 / 医生列表"
        :subtitle="`按 ${currentDepartmentText} 与地区筛选耳鼻喉相关就诊资源`"
        showBack
        @left="back"
      />

      <view class="hc-card-dark doctor-list-hero hc-reveal-up hc-shine">
        <view class="hc-kicker hc-kicker--dark">医生检索</view>
        <text class="doctor-list-hero__title">先按地区和科室缩小范围，再进入医生详情准备就诊</text>
        <text class="doctor-list-hero__desc">
          当前关键词：{{ query.Keyword || "未填写" }}，地区：{{ query.Region || "不限" }}。
        </text>
      </view>

      <view class="hc-card-soft doctor-list-filter hc-reveal-up" style="--delay: 100ms">
        <uni-easyinput
          v-model="query.Keyword"
          placeholder="搜索医院或医生名称"
          clearable
          @confirm="reload"
        />

        <view class="doctor-list-filter__row">
          <view class="hc-official-select-trigger" @click="openDepartmentPopup">
            <text class="hc-official-select-trigger__value">{{ currentDepartmentText }}</text>
            <uni-icons type="down" size="18" color="#5e705d" />
          </view>
          <uni-easyinput
            v-model="query.Region"
            placeholder="地区，如上海"
            clearable
            @confirm="reload"
          />
        </view>

        <view class="doctor-list-filter__actions">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="reload">搜索</view>
        </view>
      </view>

      <view class="bg-section-head hc-reveal-fade" style="--delay: 160ms">
        <text class="bg-section-head__title">医生卡片</text>
        <text class="bg-section-head__meta">{{ items.length }} 条已加载</text>
      </view>

      <scroll-view scroll-y class="doctor-list-scroll" @scrolltolower="loadMore" :lower-threshold="80">
        <view v-if="items.length" class="doctor-list-cards">
          <view
            v-for="(item, index) in items"
            :key="item.Id"
            class="hc-card-soft doctor-card hc-interactive-card hc-reveal-up"
            :style="{ '--delay': `${200 + index * 30}ms` }"
            @click="goDetail(item.Id)"
          >
            <view class="doctor-card__main">
              <view class="doctor-card__avatar-wrap">
                <image v-if="item.PicUrl" :src="item.PicUrl" mode="aspectFill" class="doctor-card__avatar" />
                <view v-else class="doctor-card__avatar doctor-card__avatar--placeholder">
                  <uni-icons type="person" size="26" color="#172119" />
                </view>
              </view>
              <view class="doctor-card__body">
                <view class="doctor-card__head">
                  <text class="doctor-card__name">{{ item.DoctorName || "医生" }}</text>
                  <text v-if="item.DoctorTitle" class="doctor-card__title">{{ item.DoctorTitle }}</text>
                </view>
                <text class="doctor-card__hospital">{{ item.HospitalName || "医院名称" }}</text>
                <text class="doctor-card__meta">{{ formatDepartment(item.DepartmentType) }} · {{ item.Region || "地区待补充" }}</text>
                <text v-if="item.SpecialtyDesc" class="doctor-card__desc">擅长：{{ item.SpecialtyDesc }}</text>
              </view>
            </view>

            <view class="doctor-card__actions">
              <view v-if="item.ContactPhone" class="doctor-card__action" @click.stop="makePhoneCall(item.ContactPhone)">拨打电话</view>
              <view
                v-if="item.AppointmentUrl"
                class="doctor-card__action doctor-card__action--ghost"
                @click.stop="openAppointment(item.AppointmentUrl)"
              >
                预约链接
              </view>
            </view>
          </view>
        </view>

        <view v-else-if="!loading" class="hc-card-soft doctor-list-empty hc-reveal-up" style="--delay: 200ms">
          <text class="doctor-list-empty__title">暂无符合条件的医院 / 医生</text>
          <text class="doctor-list-empty__desc">可以修改筛选条件，或者清空关键词后重新搜索。</text>
        </view>

        <view class="doctor-list-loadmore">
          <text v-if="loading">加载中...</text>
          <text v-else-if="finished && items.length">已加载全部</text>
          <text v-else-if="items.length">继续下拉加载更多</text>
        </view>
      </scroll-view>

      <view class="hc-card-soft doctor-list-tip hc-reveal-up" style="--delay: 220ms">
        <text class="doctor-list-tip__title">提示</text>
        <text class="doctor-list-tip__desc">
          医院和医生信息仅供参考，请以线下医院公示信息为准。预约链接可能跳转到外部页面，请注意识别官网与第三方平台。
        </text>
      </view>

      <uni-popup
        ref="departmentPopupRef"
        type="center"
        :safe-area="false"
        background-color="transparent"
        mask-background-color="rgba(16, 24, 19, 0.58)"
      >
        <view class="hc-official-popup-card">
          <view class="hc-official-popup-handle"></view>
          <view class="hc-official-popup-head">
            <text class="hc-official-popup-eyebrow">筛选与选择</text>
            <text class="hc-official-popup-title">选择就诊科室</text>
            <text class="hc-official-popup-subtitle">先按科室缩小范围，再继续筛选医院与医生</text>
          </view>
          <view class="hc-official-popup-summary">
            <text class="hc-official-popup-summary__label">当前</text>
            <text class="hc-official-popup-summary__value">{{ currentDepartmentText }}</text>
          </view>
          <scroll-view scroll-y class="hc-official-popup-list">
            <view
              v-for="option in departmentOptions"
              :key="String(option.value)"
              class="hc-official-popup-option"
              :class="{ active: query.DepartmentType === option.value }"
              @click="selectDepartment(option)"
            >
              <text class="hc-official-popup-option__text">{{ option.text }}</text>
              <text v-if="query.DepartmentType === option.value" class="hc-official-popup-option__mark">当前</text>
            </view>
          </scroll-view>
        </view>
      </uni-popup>
    </view>
    <hc-dialog ref="dialogRef" />
  </view>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const query = reactive({
  Page: 1,
  Limit: 10,
  Region: "",
  DepartmentType: "",
  Keyword: "",
});

const items = ref([]);
const loading = ref(false);
const finished = ref(false);
const dialogRef = ref(null);
const departmentPopupRef = ref(null);

const departmentOptions = [
  { value: "", text: "全部科室" },
  { value: 0, text: "耳鼻喉科" },
  { value: 1, text: "咽喉科" },
  { value: 2, text: "头颈外科" },
];

const currentDepartmentText = computed(() => {
  const found = departmentOptions.find((item) => item.value === query.DepartmentType);
  return found ? found.text : "全部科室";
});

const formatDepartment = (value) => {
  if (value === 0) return "耳鼻喉科";
  if (value === 1) return "咽喉科";
  if (value === 2) return "头颈外科";
  return "耳鼻喉相关";
};

const back = () => {
  uni.navigateBack();
};

const openDepartmentPopup = () => {
  departmentPopupRef.value?.open();
};

const selectDepartment = (option) => {
  query.DepartmentType = option?.value ?? "";
  departmentPopupRef.value?.close();
  reload();
};

const reload = () => {
  query.Page = 1;
  items.value = [];
  finished.value = false;
  load();
};

const load = async () => {
  if (loading.value || finished.value) return;
  loading.value = true;
  try {
    const res = await Post("/Front/Medical/DoctorList", {
      Page: query.Page,
      Limit: query.Limit,
      Region: query.Region || "",
      DepartmentType: query.DepartmentType === "" ? null : Number(query.DepartmentType),
      Keyword: query.Keyword || "",
    });
    if (!res.Success) {
      uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
      return;
    }
    const data = res.Data || {};
    const list = data.Items || [];
    items.value = query.Page === 1 ? list : items.value.concat(list);
    const total = data.TotalCount || 0;
    if (items.value.length >= total || list.length < query.Limit) {
      finished.value = true;
    } else {
      query.Page += 1;
    }
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  load();
};

const goDetail = (id) => {
  if (!id) return;
  uni.navigateTo({ url: `/pages/Front/HospitalDoctorDetail?doctorId=${id}` });
};

const makePhoneCall = (phone) => {
  if (!phone) return;
  uni.makePhoneCall({ phoneNumber: phone.toString() });
};

const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

const openAppointment = async (url) => {
  if (!url) return;
  const res = await showDialog({
    title: "即将打开预约链接",
    content: "该操作可能跳转到外部页面，请确认链接来源可靠。",
  });
  if (!res.confirm) return;
  uni.setClipboardData({
    data: url,
    success: () => {
      uni.showToast({ icon: "none", title: "已复制链接，请在浏览器中打开" });
    },
  });
};

onLoad((options) => {
  if (options && options.Region) {
    query.Region = options.Region;
  }
  if (options && options.DepartmentType !== undefined) {
    const value = Number(options.DepartmentType);
    if (!Number.isNaN(value)) {
      query.DepartmentType = value;
    }
  }
  reload();
});
</script>

<style scoped lang="scss">
.doctor-list-screen {
  gap: 24upx;
}

.doctor-list-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.doctor-list-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.doctor-list-hero__desc,
.doctor-list-tip__desc,
.doctor-list-empty__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.doctor-list-filter {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.doctor-list-filter__row {
  display: grid;
  grid-template-columns: 230upx minmax(0, 1fr);
  gap: 12upx;
}

.doctor-list-filter__picker {
  min-height: 72upx;
  padding: 0 20upx;
  border-radius: 24upx;
  background: rgba(225, 243, 154, 0.32);
  display: flex;
  align-items: center;
  font-size: 24upx;
  font-weight: 700;
  color: #172119;
}

.doctor-list-filter__actions {
  display: flex;
  justify-content: flex-start;
}

.doctor-list-scroll {
  max-height: calc(100vh - 560upx);
}

.doctor-list-cards {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.doctor-card {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.doctor-card__main {
  display: flex;
  gap: 18upx;
}

.doctor-card__avatar-wrap {
  flex-shrink: 0;
}

.doctor-card__avatar {
  width: 112upx;
  height: 112upx;
  border-radius: 28upx;
  background: rgba(225, 243, 154, 0.32);
  display: flex;
  align-items: center;
  justify-content: center;
}

.doctor-card__avatar--placeholder {
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.doctor-card__body {
  flex: 1;
  min-width: 0;
}

.doctor-card__head {
  display: flex;
  align-items: center;
  gap: 10upx;
  flex-wrap: wrap;
}

.doctor-card__name {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.doctor-card__title {
  font-size: 22upx;
  color: #7f917d;
}

.doctor-card__hospital {
  display: block;
  margin-top: 8upx;
  font-size: 24upx;
  color: #172119;
}

.doctor-card__meta,
.doctor-card__desc {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.6;
  color: #556556;
}

.doctor-card__actions {
  display: flex;
  gap: 12upx;
}

.doctor-card__action {
  padding: 10upx 18upx;
  border-radius: 999upx;
  background: #172119;
  font-size: 22upx;
  font-weight: 700;
  color: #f7ffdf;
}

.doctor-card__action--ghost {
  background: rgba(225, 243, 154, 0.42);
  color: #172119;
}

.doctor-list-empty__title,
.doctor-list-tip__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.doctor-list-loadmore {
  padding: 20upx 0 8upx;
  text-align: center;
  font-size: 22upx;
  color: #7f917d;
}

.doctor-list-tip {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.doctor-list-tip__desc,
.doctor-list-empty__desc {
  color: #556556;
}
</style>
