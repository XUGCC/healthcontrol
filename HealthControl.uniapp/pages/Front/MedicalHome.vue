<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack medical-home-screen">
      <hc-topbar title="就医辅助" subtitle="把风险提示、建议、医生信息和就诊准备统一到同一套路径" />

      <view class="hc-card-dark medical-home-hero hc-reveal-up hc-shine">
        <view class="medical-home-hero__head">
          <view class="hc-kicker hc-kicker--dark">就医判断</view>
          <view class="medical-home-hero__risk" :class="`level-${homeData.RiskLevel ?? -1}`">{{ riskLevelText }}</view>
        </view>
        <text class="medical-home-hero__title">先看当前风险，再决定是否打开建议、报告或医生信息</text>
        <text class="medical-home-hero__desc">{{ homeData.RiskSummaryText || defaultSummaryText }}</text>
        <view class="medical-home-hero__actions">
          <view class="hc-pill-button hc-interactive-pill" @click="goReportCreate">生成就诊准备报告</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="goDoctorList">查看医院 / 医生库</view>
        </view>
      </view>

      <view class="medical-home-grid">
        <view class="hc-card-soft medical-home-summary hc-reveal-up" style="--delay: 120ms">
          <text class="medical-home-summary__label">最近检测</text>
          <text class="medical-home-summary__value">{{ homeData.LatestDetect?.SummaryText || "暂无检测记录" }}</text>
          <text class="medical-home-summary__desc">检测结果会参与就医建议和报告生成。</text>
        </view>
        <view class="hc-card-soft medical-home-summary hc-reveal-up" style="--delay: 180ms">
          <text class="medical-home-summary__label">近期症状</text>
          <text class="medical-home-summary__value">{{ homeData.LatestSymptom?.SymptomSummary || "暂无症状记录" }}</text>
          <text class="medical-home-summary__desc">症状变化会帮助判断线下就诊时机。</text>
        </view>
      </view>

      <view class="bg-section-head hc-reveal-fade" style="--delay: 220ms">
        <text class="bg-section-head__title">关键入口</text>
        <text class="bg-section-head__meta">建议、报告、医生信息</text>
      </view>

      <view
        v-if="homeData.LatestRecommend"
        class="hc-card-soft medical-home-recommend hc-interactive-card hc-reveal-up"
        style="--delay: 260ms"
        @click="goRecommendDetail(homeData.LatestRecommend.RecommendId || homeData.LatestRecommend.Id)"
      >
        <view class="medical-home-recommend__head">
          <text class="medical-home-recommend__title">{{ homeData.LatestRecommend.Title || "最新就医建议" }}</text>
          <text class="medical-home-recommend__status" :class="{ active: !homeData.LatestRecommend.ViewStatus }">
            {{ homeData.LatestRecommend.ViewStatus ? "已查看" : "未查看" }}
          </text>
        </view>
        <text class="medical-home-recommend__desc">
          {{ homeData.LatestRecommend.RecommendReason || "基于近期检测结果和症状，系统建议继续关注线下就诊评估。" }}
        </text>
        <text class="medical-home-recommend__meta">推荐时间：{{ homeData.LatestRecommend.RecommendTime || "-" }}</text>
      </view>

      <view v-else class="hc-card-soft medical-home-empty hc-reveal-up" style="--delay: 260ms">
        <text class="medical-home-empty__title">当前还没有新的就医建议</text>
        <text class="medical-home-empty__desc">完成检测和症状记录后，系统会在风险升高时自动生成建议记录。</text>
      </view>

      <view class="medical-home-entry-grid">
        <view class="medical-home-entry medical-home-entry--dark hc-interactive-card hc-reveal-up" style="--delay: 320ms" @click="goRecommendList">
          <text class="medical-home-entry__index">01</text>
          <text class="medical-home-entry__title">就医建议列表</text>
          <text class="medical-home-entry__desc">查看系统已生成的建议记录和查看状态。</text>
        </view>
        <view class="medical-home-entry hc-interactive-card hc-reveal-up" style="--delay: 380ms" @click="goReportList">
          <text class="medical-home-entry__index">02</text>
          <text class="medical-home-entry__title">报告中心</text>
          <text class="medical-home-entry__desc">
            {{ homeData.LatestReport?.GenerateStatusText || "生成和查看就诊准备报告" }}
          </text>
        </view>
        <view class="medical-home-entry hc-interactive-card hc-reveal-up" style="--delay: 440ms" @click="goDoctorList">
          <text class="medical-home-entry__index">03</text>
          <text class="medical-home-entry__title">医院 / 医生库</text>
          <text class="medical-home-entry__desc">查找耳鼻喉相关医院、医生、电话和预约入口。</text>
        </view>
      </view>

      <view
        v-if="homeData.LatestVisitPlan && homeData.LatestVisitPlan.PlanVisitDate"
        class="hc-card-lime medical-home-plan hc-reveal-up"
        style="--delay: 500ms"
      >
        <text class="medical-home-plan__title">我的就诊计划</text>
        <text class="medical-home-plan__desc">计划日期：{{ homeData.LatestVisitPlan.PlanVisitDate }}</text>
        <text v-if="homeData.LatestVisitPlan.DoctorName" class="medical-home-plan__desc">医生：{{ homeData.LatestVisitPlan.DoctorName }}</text>
        <text v-if="homeData.LatestVisitPlan.PlanChannel" class="medical-home-plan__desc">就诊方式：{{ homeData.LatestVisitPlan.PlanChannel }}</text>
      </view>

      <view class="hc-card-soft medical-home-tip hc-reveal-up" style="--delay: 560ms">
        <text class="medical-home-tip__title">温馨提示</text>
        <text class="medical-home-tip__desc">
          本模块的建议与报告仅供参考，不能替代医生面诊。如出现呼吸困难、持续加重等情况，请及时就近就医或急诊。
        </text>
      </view>
    </view>

    <footer-bar />
  </view>
</template>

<script setup>
import { computed, reactive } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import FooterBar from "@/components/footer-bar/footer-bar.vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const homeData = reactive({
  RiskLevel: null,
  LatestDetect: null,
  LatestSymptom: null,
  LatestRecommend: null,
  LatestReport: null,
  LatestVisitPlan: null,
  RiskSummaryText: "",
});

const defaultSummaryText = "基于您的检测结果与症状，系统会在风险升高时给出就医或复查建议。";

const riskLevelText = computed(() => {
  if (homeData.RiskLevel === 2) return "高风险";
  if (homeData.RiskLevel === 1) return "中风险";
  if (homeData.RiskLevel === 0) return "低风险";
  return "待评定";
});

const loadHome = async () => {
  const res = await Post("/Front/Medical/Home", {});
  if (!res.Success) {
    uni.showToast({ icon: "none", title: res.Msg || "加载失败" });
    return;
  }
  const data = res.Data || {};
  homeData.RiskLevel = data.RiskLevel;
  homeData.LatestDetect = data.LatestDetect || null;
  homeData.LatestSymptom = data.LatestSymptom || null;
  homeData.LatestRecommend = data.LatestRecommend || null;
  homeData.LatestReport = data.LatestReport || null;
  homeData.LatestVisitPlan = data.LatestVisitPlan || null;
  homeData.RiskSummaryText = data.RiskSummaryText || defaultSummaryText;
};

onShow(() => {
  loadHome();
});

const goDoctorList = () => {
  uni.navigateTo({ url: "/pages/Front/HospitalDoctorList" });
};

const goRecommendList = () => {
  uni.navigateTo({ url: "/pages/Front/MedicalRecommendList" });
};

const goRecommendDetail = (id) => {
  if (!id) return;
  uni.navigateTo({ url: `/pages/Front/MedicalRecommendDetail?recommendId=${id}` });
};

const goReportList = () => {
  uni.navigateTo({ url: "/pages/Front/MedicalPrepareReport" });
};

const goReportDetail = (id) => {
  if (!id) return;
  uni.navigateTo({ url: `/pages/Front/MedicalPrepareReport?reportId=${id}` });
};

const goReportCreate = () => {
  uni.navigateTo({ url: "/pages/Front/MedicalPrepareReport" });
};
</script>

<style scoped lang="scss">
.medical-home-screen {
  gap: 28upx;
}

.medical-home-hero {
  display: flex;
  flex-direction: column;
  gap: 16upx;
}

.medical-home-hero__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16upx;
}

.medical-home-hero__risk {
  padding: 10upx 18upx;
  border-radius: 999upx;
  background: rgba(241, 248, 223, 0.12);
  font-size: 22upx;
  font-weight: 800;
  color: #f7ffdf;
}

.medical-home-hero__risk.level-0 {
  color: #cfe98d;
}

.medical-home-hero__risk.level-1 {
  color: #efdc89;
}

.medical-home-hero__risk.level-2 {
  color: #f3b39c;
}

.medical-home-hero__title {
  display: block;
  font-size: 52upx;
  line-height: 1.08;
  font-weight: 800;
  color: #f7ffdf;
}

.medical-home-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: rgba(241, 248, 223, 0.74);
}

.medical-home-hero__actions {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.medical-home-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16upx;
}

.medical-home-summary {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.medical-home-summary__label {
  font-size: 20upx;
  color: #7c8e7b;
}

.medical-home-summary__value {
  display: block;
  font-size: 30upx;
  line-height: 1.4;
  font-weight: 800;
  color: #172119;
}

.medical-home-summary__desc,
.medical-home-empty__desc,
.medical-home-plan__desc,
.medical-home-tip__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.medical-home-recommend,
.medical-home-empty,
.medical-home-tip {
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.medical-home-recommend__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16upx;
}

.medical-home-recommend__title,
.medical-home-empty__title,
.medical-home-plan__title,
.medical-home-tip__title {
  display: block;
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.medical-home-recommend__status {
  padding: 8upx 14upx;
  border-radius: 999upx;
  background: rgba(225, 243, 154, 0.38);
  font-size: 20upx;
  font-weight: 800;
  color: #53683d;
}

.medical-home-recommend__status.active {
  background: rgba(228, 108, 73, 0.12);
  color: #a14d34;
}

.medical-home-recommend__desc {
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.medical-home-recommend__meta {
  font-size: 20upx;
  color: #7f917d;
}

.medical-home-entry-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18upx;
}

.medical-home-entry {
  min-height: 214upx;
  border-radius: 34upx;
  padding: 22upx 20upx;
  background: rgba(248, 252, 239, 0.82);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.medical-home-entry--dark {
  background: linear-gradient(135deg, #101612 0%, #1e2820 100%);
  border-color: #101612;
}

.medical-home-entry__index {
  font-size: 46upx;
  line-height: 1;
  font-weight: 800;
  color: #172119;
}

.medical-home-entry--dark .medical-home-entry__index,
.medical-home-entry--dark .medical-home-entry__title,
.medical-home-entry--dark .medical-home-entry__desc {
  color: #f7ffdf;
}

.medical-home-entry__title {
  display: block;
  font-size: 26upx;
  font-weight: 800;
  color: #172119;
}

.medical-home-entry__desc {
  margin-top: 8upx;
  font-size: 20upx;
  line-height: 1.55;
  color: #556556;
}
</style>
