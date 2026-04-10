<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack laryngoscope-edit-screen">
      <hc-topbar title="上传喉镜照片" subtitle="把线下喉镜资料补充到同一套健康档案中" :show-back="true" @left="back" />

      <view class="hc-card-lime laryngoscope-edit-hero hc-reveal-up">
        <view class="hc-kicker">影像补充</view>
        <text class="laryngoscope-edit-hero__title">建议上传医院出具的清晰喉镜图片，并补全检查时间与机构信息</text>
        <text class="laryngoscope-edit-hero__desc">保存接口、详情回显和历史兼容解析逻辑保持不变，这里只重组为主页化的卡片布局。</text>
      </view>

      <view class="hc-card-soft laryngoscope-edit-section hc-reveal-up" style="--delay: 90ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">喉镜图片</text>
          <text class="bg-section-head__meta">优先上传最清晰、最完整的一张</text>
        </view>
        <view class="laryngoscope-edit-upload">
          <upload-images v-model="form.LaryngoscopePhotoUrl" :limit="1" />
        </view>
      </view>

      <view class="hc-card-soft laryngoscope-edit-section hc-reveal-up" style="--delay: 140ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">检查信息</text>
          <text class="bg-section-head__meta">帮助后续按批次整理影像资料</text>
        </view>

        <view class="laryngoscope-field">
          <text class="laryngoscope-field__label">检查时间</text>
          <view class="laryngoscope-field__picker">
            <uni-datetime-picker
              v-model="checkTime"
              type="datetime"
              return-type="string"
              :border="false"
              :clear-icon="false"
              :hide-second="true"
              placeholder="请选择检查时间"
            />
          </view>
        </view>

        <view class="laryngoscope-field">
          <text class="laryngoscope-field__label">检查医院 / 科室</text>
          <uni-easyinput v-model="hospital" :inputBorder="false" placeholder="如：北京同仁医院 耳鼻喉科" :maxlength="50" />
        </view>

        <view class="laryngoscope-field">
          <text class="laryngoscope-field__label">检查方式</text>
          <uni-easyinput v-model="checkType" :inputBorder="false" placeholder="如：电子喉镜检查" :maxlength="30" />
        </view>

        <view class="laryngoscope-field">
          <text class="laryngoscope-field__label">备注说明</text>
          <textarea
            v-model="remark"
            class="laryngoscope-field__textarea"
            placeholder="可记录医生结论或个人感受，最多 100 字"
            maxlength="100"
          />
        </view>
      </view>

      <view class="hc-card-soft laryngoscope-edit-tip hc-reveal-up" style="--delay: 190ms">
        <text class="laryngoscope-edit-tip__title">隐私说明</text>
        <text class="laryngoscope-edit-tip__desc">照片默认仅对你本人可见，不会因为上传动作自动参与模型训练；如未来需要授权，会在标注链路中单独征得确认。</text>
      </view>

      <view class="hc-pill-button-dark hc-interactive-pill laryngoscope-edit-submit hc-reveal-up" style="--delay: 240ms" @click="submit">
        {{ submitting ? "保存中..." : "保存到我的喉镜档案" }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import UploadImages from "@/components/upload-images/upload-images.vue";
import { Post } from "@/utils/http";

const form = ref({
  Id: null,
  DetectId: null,
  LaryngoscopePhotoUrl: "",
  PhotoDesc: "",
});

const checkTime = ref("");
const hospital = ref("");
const checkType = ref("");
const remark = ref("");
const submitting = ref(false);

const back = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/LaryngoscopePhotoList" });
};

const buildPhotoDesc = () => {
  const parts = [];
  if (hospital.value) parts.push(hospital.value);
  if (checkType.value) parts.push(checkType.value);
  if (remark.value) parts.push(remark.value);
  return parts.join(" | ");
};

const submit = async () => {
  if (!form.value.LaryngoscopePhotoUrl) {
    uni.showToast({ title: "请先上传喉镜照片", icon: "none" });
    return;
  }

  submitting.value = true;
  try {
    if (checkTime.value) {
      let value = String(checkTime.value);
      if (value.length === 16) {
        value += ":00";
      }
      checkTime.value = value;
    }

    form.value.PhotoDesc = buildPhotoDesc();
    const { Success, Msg } = await Post("/Front/Laryngoscope/SavePhoto", {
      Id: form.value.Id,
      DetectId: form.value.DetectId,
      LaryngoscopePhotoUrl: form.value.LaryngoscopePhotoUrl,
      PhotoDesc: form.value.PhotoDesc,
      CheckTime: checkTime.value || null,
      HospitalName: hospital.value || "",
      CheckType: checkType.value || "",
    });

    if (!Success) {
      uni.showToast({ title: Msg || "保存失败", icon: "none" });
      return;
    }

    uni.showToast({ title: "保存成功", icon: "success" });
    setTimeout(() => {
      uni.navigateBack({ delta: 1 });
    }, 500);
  } catch (error) {
    console.error("submit laryngoscope error:", error);
    uni.showToast({ title: "网络异常，请稍后重试", icon: "none" });
  } finally {
    submitting.value = false;
  }
};

const loadDetail = async (id) => {
  const { Data, Success } = await Post("/Front/Laryngoscope/Get", { Id: Number(id) });
  if (!Success || !Data) return;

  form.value.Id = Data.Id || null;
  form.value.DetectId = Data.DetectId || null;
  form.value.LaryngoscopePhotoUrl = Data.LaryngoscopePhotoUrl || "";
  checkTime.value = Data.CheckTime || "";
  hospital.value = Data.HospitalName || "";
  checkType.value = Data.CheckType || "";

  if (Data.PhotoDesc) {
    const parts = String(Data.PhotoDesc)
      .split("|")
      .map((item) => item.trim());
    if (!hospital.value && parts.length > 0) hospital.value = parts[0] || "";
    if (!checkType.value && parts.length > 1) checkType.value = parts[1] || "";
    if (!remark.value && parts.length > 2) {
      const maybeTime = parts[2];
      const rest = /^\d{4}-\d{2}-\d{2}/.test(maybeTime) && parts.length > 3 ? parts.slice(3) : parts.slice(2);
      remark.value = rest.join(" | ");
    }
  }
};

onLoad((options) => {
  if (options?.detectId) {
    const detectId = Number(options.detectId);
    if (detectId) {
      form.value.DetectId = detectId;
    }
  }
  if (options?.id) {
    const id = Number(options.id);
    if (id) {
      loadDetail(id);
    }
  }
});
</script>

<style scoped lang="scss">
.laryngoscope-edit-screen {
  gap: 24upx;
}

.laryngoscope-edit-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.laryngoscope-edit-hero__title {
  display: block;
  font-size: 42upx;
  line-height: 1.14;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-edit-hero__desc,
.laryngoscope-edit-tip__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.laryngoscope-edit-upload,
.laryngoscope-field {
  margin-top: 18upx;
}

.laryngoscope-field__picker :deep(.uni-date) {
  width: 100%;
}

.laryngoscope-field__picker :deep(.uni-date-editor) {
  width: 100%;
}

.laryngoscope-field__picker :deep(.uni-date-editor--x) {
  min-height: 96upx;
  padding: 0 22upx;
  border-radius: 30upx;
  border: 1upx solid rgba(205, 224, 145, 0.9) !important;
  background: rgba(248, 252, 239, 0.92) !important;
  box-shadow: none !important;
}

.laryngoscope-field__picker :deep(.uni-date-x) {
  min-height: 82upx;
}

.laryngoscope-field__picker :deep(.uni-date__x-input) {
  font-size: 24upx;
  font-weight: 700;
  color: #172119 !important;
}

.laryngoscope-field__picker :deep(.icon-calendar) {
  color: #6c7d69 !important;
}

.laryngoscope-field__label {
  display: block;
  margin-bottom: 10upx;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.laryngoscope-field__textarea {
  width: 100%;
  min-height: 180upx;
  padding: 22upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  box-sizing: border-box;
  font-size: 24upx;
  line-height: 1.7;
  color: #243123;
}

.laryngoscope-edit-tip__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}
</style>
