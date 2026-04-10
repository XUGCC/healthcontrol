<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack symptom-form-screen">
      <hc-topbar
        :title="isEdit ? '编辑症状' : '记录症状'"
        :subtitle="isEdit ? '调整这次不适记录，保持症状链路完整' : '把这次喉部不适补充到健康时间线里'"
        :show-back="true"
        @left="goBack"
      >
        <template #right>
          <text class="symptom-form-screen__right" @click="goToList">列表</text>
        </template>
      </hc-topbar>

      <view class="hc-card-lime symptom-form-hero hc-reveal-up">
        <view class="hc-kicker">{{ isEdit ? "记录修订" : "新增记录" }}</view>
        <text class="symptom-form-hero__title">先给出症状类型和严重程度，再补充持续时间、首次出现时间与备注。</text>
        <view class="symptom-form-hero__stats">
          <view class="symptom-form-hero__stat">
            <text class="hc-stat-value">{{ formData.SymptomType || "\u5f85\u9009" }}</text>
            <text class="hc-stat-label">当前类型</text>
          </view>
          <view class="symptom-form-hero__stat symptom-form-hero__stat--dark">
            <text class="hc-stat-value hc-stat-value--dark">{{ symptomLevelText || "待定" }}</text>
            <text class="hc-stat-label">当前级别</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft symptom-form-section hc-reveal-up" style="--delay: 80ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">症状类型</text>
          <text class="hc-section-head__meta">先选一个最接近的变化</text>
        </view>
        <view class="symptom-option-grid">
          <view
            v-for="type in symptomTypes"
            :key="type"
            class="symptom-option-card hc-interactive-card"
            :class="{ 'symptom-option-card--active': formData.SymptomType === type }"
            @click="formData.SymptomType = type"
          >
            <text class="symptom-option-card__title">{{ type }}</text>
            <text class="symptom-option-card__desc">{{ symptomTypeDescMap[type] }}</text>
          </view>
        </view>
        <view v-if="formData.SymptomType === '其他'" class="symptom-field">
          <text class="symptom-field__label">具体症状</text>
          <input v-model="formData.CustomSymptom" class="symptom-field__input" :placeholder="'\u8bf7\u8f93\u5165\u5177\u4f53\u75c7\u72b6\u63cf\u8ff0'" />
        </view>
      </view>

      <view class="hc-card-soft symptom-form-section hc-reveal-up" style="--delay: 140ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">严重程度</text>
          <text class="hc-section-head__meta">越严重越应尽快结合筛查结果判断</text>
        </view>
        <view class="symptom-level-list">
          <view
            v-for="level in symptomLevels"
            :key="level.value"
            class="symptom-level-card hc-interactive-card"
            :class="{ 'symptom-level-card--active': formData.SymptomLevel === level.value }"
            @click="formData.SymptomLevel = level.value"
          >
            <view class="symptom-level-card__value">{{ level.label }}</view>
            <text class="symptom-level-card__desc">{{ level.desc }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft symptom-form-section hc-reveal-up" style="--delay: 200ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">时间与关联信息</text>
          <text class="hc-section-head__meta">用结构化字段保留后续比对线索</text>
        </view>
        <view class="symptom-picker-list">
          <view class="symptom-picker-row">
            <text class="symptom-picker-row__label">持续时长</text>
            <view class="symptom-picker-row__value-group">
              <view class="symptom-picker-row__control symptom-picker-row__control--number">
                <view class="hc-official-select-trigger" :class="{ 'is-placeholder': !formData.SymptomDuration }" @click="openDurationNumberPopup">
                  <text class="hc-official-select-trigger__value">{{ formData.SymptomDuration || "请选择" }}</text>
                  <uni-icons type="down" size="18" color="#5e705d" />
                </view>
              </view>
              <view class="symptom-picker-row__control symptom-picker-row__control--unit">
                <view class="hc-official-select-trigger" @click="openDurationUnitPopup">
                  <text class="hc-official-select-trigger__value">{{ formData.SymptomDurationUnit || "请选择" }}</text>
                  <uni-icons type="down" size="18" color="#5e705d" />
                </view>
              </view>
            </view>
          </view>

          <view class="symptom-picker-row">
            <text class="symptom-picker-row__label">首次出现</text>
            <view class="symptom-picker-row__control symptom-picker-row__control--single">
              <picker mode="date" :value="datePickerValue" @change="onOccurDateChange">
                <view class="hc-official-select-trigger symptom-picker-row__native-trigger" :class="{ 'is-placeholder': !formData.SymptomOccurTime }">
                  <text class="hc-official-select-trigger__value">{{ formData.SymptomOccurTime || "请选择日期" }}</text>
                  <uni-icons type="calendar" size="18" color="#5e705d" />
                </view>
              </picker>
            </view>
          </view>

          <view class="symptom-picker-row">
            <text class="symptom-picker-row__label">关联筛查</text>
            <view class="symptom-picker-row__control symptom-picker-row__control--single">
              <view class="hc-official-select-trigger" @click="openDetectPopup">
                <text class="hc-official-select-trigger__value">{{ selectedDetectLabel }}</text>
                <uni-icons type="down" size="18" color="#5e705d" />
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="hc-card-soft symptom-form-section hc-reveal-up" style="--delay: 260ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">备注说明</text>
          <text class="hc-section-head__meta">{{ formData.Remark.length }}/200</text>
        </view>
        <textarea
          v-model="formData.Remark"
          class="symptom-form-textarea"
          maxlength="200"
          :placeholder="'\u53ef\u8865\u5145\u8bf1\u56e0\u3001\u4f34\u968f\u8868\u73b0\u3001\u5f53\u5929\u72b6\u6001\u7b49\uff0c\u65b9\u4fbf\u540e\u7eed\u548c\u7b5b\u67e5\u7ed3\u679c\u4e00\u8d77\u56de\u770b\u3002'"
        />
      </view>
    </view>

    <view class="hc-fab symptom-form-actions">
      <view class="hc-pill-button-dark" @click="submitForm">{{ isEdit ? "保存修改" : "提交记录" }}</view>
    </view>

    <uni-popup
      ref="durationNumberPopupRef"
      type="center"
      :safe-area="false"
      background-color="transparent"
      mask-background-color="rgba(16, 24, 19, 0.58)"
    >
      <view class="hc-official-popup-card">
        <view class="hc-official-popup-handle"></view>
        <view class="hc-official-popup-head">
          <text class="hc-official-popup-eyebrow">筛选与选择</text>
          <text class="hc-official-popup-title">选择持续时长</text>
          <text class="hc-official-popup-subtitle">先选数字</text>
        </view>
        <view class="hc-official-popup-summary">
          <text class="hc-official-popup-summary__label">当前</text>
          <text class="hc-official-popup-summary__value">{{ formData.SymptomDuration || "未选择" }}</text>
        </view>
        <scroll-view scroll-y class="hc-official-popup-list">
          <view
            v-for="option in durationNumberOptions"
            :key="String(option.value)"
            class="hc-official-popup-option"
            :class="{ active: formData.SymptomDuration === option.value }"
            @click="onDurationNumberChange(option)"
          >
            <text class="hc-official-popup-option__text">{{ option.text }}</text>
            <text v-if="formData.SymptomDuration === option.value" class="hc-official-popup-option__mark">当前</text>
          </view>
        </scroll-view>
      </view>
    </uni-popup>

    <uni-popup
      ref="durationUnitPopupRef"
      type="center"
      :safe-area="false"
      background-color="transparent"
      mask-background-color="rgba(16, 24, 19, 0.58)"
    >
      <view class="hc-official-popup-card">
        <view class="hc-official-popup-handle"></view>
        <view class="hc-official-popup-head">
          <text class="hc-official-popup-eyebrow">筛选与选择</text>
          <text class="hc-official-popup-title">选择持续时长单位</text>
          <text class="hc-official-popup-subtitle">再选单位</text>
        </view>
        <view class="hc-official-popup-summary">
          <text class="hc-official-popup-summary__label">当前</text>
          <text class="hc-official-popup-summary__value">{{ formData.SymptomDurationUnit || "未选择" }}</text>
        </view>
        <scroll-view scroll-y class="hc-official-popup-list">
          <view
            v-for="option in durationUnitOptions"
            :key="String(option.value)"
            class="hc-official-popup-option"
            :class="{ active: formData.SymptomDurationUnit === option.value }"
            @click="onDurationUnitChange(option)"
          >
            <text class="hc-official-popup-option__text">{{ option.text }}</text>
            <text v-if="formData.SymptomDurationUnit === option.value" class="hc-official-popup-option__mark">当前</text>
          </view>
        </scroll-view>
      </view>
    </uni-popup>

    <uni-popup
      ref="detectPopupRef"
      type="center"
      :safe-area="false"
      background-color="transparent"
      mask-background-color="rgba(16, 24, 19, 0.58)"
    >
      <view class="hc-official-popup-card">
        <view class="hc-official-popup-handle"></view>
        <view class="hc-official-popup-head">
          <text class="hc-official-popup-eyebrow">筛选与选择</text>
          <text class="hc-official-popup-title">选择关联筛查记录</text>
          <text class="hc-official-popup-subtitle">不关联也可以单独提交症状记录</text>
        </view>
        <view class="hc-official-popup-summary">
          <text class="hc-official-popup-summary__label">当前</text>
          <text class="hc-official-popup-summary__value">{{ selectedDetectLabel }}</text>
        </view>
        <scroll-view scroll-y class="hc-official-popup-list">
          <view
            v-for="option in detectOptions"
            :key="String(option.value)"
            class="hc-official-popup-option"
            :class="{ active: formData.DetectId === option.value }"
            @click="onDetectChange(option)"
          >
            <text class="hc-official-popup-option__text">{{ option.label }}</text>
            <text v-if="formData.DetectId === option.value" class="hc-official-popup-option__mark">当前</text>
          </view>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();
const formData = ref({
  Id: null,
  UserId: commonStore.UserId,
  DetectId: null,
  SymptomType: "",
  CustomSymptom: "",
  SymptomLevel: null,
  SymptomDuration: "",
  SymptomDurationUnit: "\u5929",
  SymptomOccurTime: "",
  Remark: "",
});

const symptomTypes = ["声音嘶哑", "疼痛", "吞咽困难", "咽干", "咽痒", "其他"];
const symptomTypeDescMap = {
  "\u58f0\u97f3\u5636\u54d1": "\u9002\u5408\u8bb0\u5f55\u58f0\u97f3\u53d1\u7d27\u3001\u6c99\u54d1\u6216\u6613\u75b2\u52b3\u3002",
  "\u75bc\u75db": "\u9002\u5408\u8bb0\u5f55\u523a\u75db\u3001\u707c\u75db\u6216\u660e\u663e\u4e0d\u9002\u3002",
  "\u541e\u54bd\u56f0\u96be": "\u9002\u5408\u8bb0\u5f55\u541e\u54bd\u8d39\u529b\u6216\u5f02\u7269\u611f\u3002",
  "\u54bd\u5e72": "\u9002\u5408\u8bb0\u5f55\u5e72\u6da9\u3001\u7f3a\u6c34\u611f\u6216\u6301\u7eed\u5e72\u71e5\u3002",
  "\u54bd\u75d2": "\u9002\u5408\u8bb0\u5f55\u523a\u6fc0\u611f\u3001\u53d1\u75d2\u6216\u60f3\u6e05\u55d3\u3002",
  "\u5176\u4ed6": "\u4e0d\u5728\u5e38\u89c1\u9009\u9879\u4e2d\u7684\u5176\u4ed6\u53d8\u5316\u3002",
};
const symptomLevels = [
  { value: 1, label: "\u8f7b\u5ea6", desc: "\u8f7b\u5fae\u4e0d\u9002\uff0c\u5bf9\u65e5\u5e38\u8bf4\u8bdd\u548c\u8fdb\u98df\u5f71\u54cd\u8f83\u5c0f\u3002" },
  { value: 2, label: "\u4e2d\u5ea6", desc: "\u4e0d\u9002\u5df2\u660e\u663e\u51fa\u73b0\uff0c\u5efa\u8bae\u6301\u7eed\u8ddf\u8e2a\u5e76\u7ed3\u5408\u7b5b\u67e5\u7ed3\u679c\u89c2\u5bdf\u3002" },
  { value: 3, label: "\u91cd\u5ea6", desc: "\u5bf9\u65e5\u5e38\u4f7f\u7528\u5589\u90e8\u5f71\u54cd\u8f83\u5927\uff0c\u5efa\u8bae\u5c3d\u5feb\u5173\u6ce8\u540e\u7eed\u5904\u7406\u3002" },
];
const durationUnits = ["\u5929", "\u5468", "\u6708"];
const durationNumbers = ref([]);
const detectOptions = ref([{ value: null, label: "\u4e0d\u5173\u8054\u7b5b\u67e5\u8bb0\u5f55" }]);
const isEdit = ref(false);
const durationNumberPopupRef = ref(null);
const durationUnitPopupRef = ref(null);
const detectPopupRef = ref(null);

const symptomLevelText = computed(() => symptomLevels.find((item) => item.value === formData.value.SymptomLevel)?.label || "");
const durationNumberOptions = computed(() => durationNumbers.value.map((item) => ({ text: item, value: item })));
const durationUnitOptions = computed(() => durationUnits.map((item) => ({ text: item, value: item })));
const selectedDetectLabel = computed(() => detectOptions.value.find((item) => item.value === formData.value.DetectId)?.label || "请选择记录");
const datePickerValue = computed(() => formData.value.SymptomOccurTime || getLocalDateString());

const getLocalDateString = (input = new Date()) => {
  const date = input instanceof Date ? input : new Date(input);
  if (Number.isNaN(date.getTime())) return "";
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const normalizeDateValue = (value) => {
  if (!value) return "";
  if (typeof value === "string") {
    const matched = value.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (matched) return matched[0];
  }
  return getLocalDateString(value);
};

const generateDurationNumbers = (unit) => {
  const numbers = [];
  const max = unit === "天" ? 365 : unit === "周" ? 52 : 12;
  for (let i = 1; i <= max; i += 1) numbers.push(String(i));
  return numbers;
};

const formatTime = (timeStr) => {
  if (!timeStr) return "";
  try {
    const date = new Date(timeStr);
    return `${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")} ${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
  } catch {
    return "";
  }
};

const loadDetectRecords = async () => {
  try {
    const response = await Post("/TAudioScreenRecord/List", { UserId: commonStore.UserId, Page: 1, Limit: 10 });
    if (response.Success && response.Data?.Items) {
      const options = [{ value: null, label: "\u4e0d\u5173\u8054\u7b5b\u67e5\u8bb0\u5f55" }];
      response.Data.Items.forEach((item) => {
        options.push({ value: item.Id, label: `${formatTime(item.CreationTime)} · ${item.PrimaryScreenResult ? "异常" : "正常"}` });
      });
      detectOptions.value = options;
    }
  } catch (error) {
    console.error("加载筛查记录失败", error);
  }
};

const loadSymptomData = async (id) => {
  try {
    const response = await Post("/TSymptomLog/Get", { Id: id, Page: 1, Limit: 1 });
    if (response.Success && response.Data) {
      const data = response.Data;
      formData.value = { ...formData.value, Id: data.Id, SymptomType: data.SymptomType, SymptomLevel: data.SymptomLevel, Remark: data.Remark || "", DetectId: data.DetectId || null };
      if (data.SymptomDuration) {
        const match = data.SymptomDuration.match(/(\d+)(天|周|月)/);
        if (match) {
          formData.value.SymptomDuration = match[1];
          formData.value.SymptomDurationUnit = match[2];
          durationNumbers.value = generateDurationNumbers(match[2]);
          if (!durationNumbers.value.includes(match[1])) {
            formData.value.SymptomDuration = durationNumbers.value[0];
          }
        }
      }
      if (data.SymptomOccurTime) formData.value.SymptomOccurTime = normalizeDateValue(data.SymptomOccurTime);
    }
  } catch (error) {
    console.error("加载症状数据失败", error);
    uni.showToast({ title: "加载失败", icon: "error" });
  }
};

const onDurationNumberChange = (item) => {
  formData.value.SymptomDuration = item?.value || "";
  durationNumberPopupRef.value?.close();
};

const onDurationUnitChange = (item) => {
  const newUnit = item?.value || "天";
  formData.value.SymptomDurationUnit = newUnit;
  durationNumbers.value = generateDurationNumbers(newUnit);
  if (!durationNumbers.value.includes(String(formData.value.SymptomDuration))) {
    formData.value.SymptomDuration = durationNumbers.value[0];
  }
  durationUnitPopupRef.value?.close();
};

const onDetectChange = (item) => {
  formData.value.DetectId = item?.value || null;
  detectPopupRef.value?.close();
};

const openDurationNumberPopup = () => durationNumberPopupRef.value?.open();
const openDurationUnitPopup = () => durationUnitPopupRef.value?.open();
const openDetectPopup = () => detectPopupRef.value?.open();
const onOccurDateChange = (event) => {
  formData.value.SymptomOccurTime = normalizeDateValue(event?.detail?.value);
};

const submitForm = async () => {
  if (!formData.value.SymptomType) return uni.showToast({ title: "请选择症状类型", icon: "none" });
  if (formData.value.SymptomType === "其他" && !formData.value.CustomSymptom) return uni.showToast({ title: "请选择其他时请补充具体症状", icon: "none" });
  if (!formData.value.SymptomLevel) return uni.showToast({ title: "请选择严重程度", icon: "none" });
  if (!formData.value.SymptomDuration) return uni.showToast({ title: "请选择持续时长", icon: "none" });

  try {
    uni.showLoading({ title: "保存中..." });
    const payload = {
      Id: formData.value.Id,
      UserId: formData.value.UserId,
      DetectId: formData.value.DetectId || null,
      SymptomType: formData.value.SymptomType === "其他" ? formData.value.CustomSymptom : formData.value.SymptomType,
      SymptomLevel: formData.value.SymptomLevel,
      SymptomDuration: `${formData.value.SymptomDuration}${formData.value.SymptomDurationUnit}`,
      SymptomOccurTime: formData.value.SymptomOccurTime ? `${normalizeDateValue(formData.value.SymptomOccurTime)}T00:00:00` : null,
      Remark: formData.value.Remark || "",
    };
    const response = await Post("/TSymptomLog/CreateOrEdit", payload);
    if (response.Success) {
      uni.showToast({ title: "保存成功", icon: "success" });
      setTimeout(() => uni.navigateBack(), 1200);
    } else {
      uni.showToast({ title: response.Msg || "保存失败", icon: "error" });
    }
  } catch (error) {
    console.error("保存症状记录失败", error);
    uni.showToast({ title: error?.response?.data?.Msg || error?.message || "保存失败，请重试", icon: "error", duration: 3000 });
  } finally {
    uni.hideLoading();
  }
};

const goBack = () => uni.navigateBack();
const goToList = () => uni.navigateTo({ url: "/pages/Front/SymptomLogList" });

onLoad(async (options) => {
  durationNumbers.value = generateDurationNumbers("\u5929");
  formData.value.SymptomDuration = durationNumbers.value[0];
  formData.value.SymptomOccurTime = getLocalDateString();
  if (options.detectId) formData.value.DetectId = Number(options.detectId);
  if (options.id) isEdit.value = true;
  await loadDetectRecords();
  if (options.id) await loadSymptomData(Number(options.id));
});
</script>

<style scoped lang="scss">
.symptom-form-screen { padding-bottom: calc(env(safe-area-inset-bottom) + 220upx); }
.symptom-form-screen__right { font-size: 24upx; color: var(--text-color); font-weight: 800; }
.symptom-form-hero__title { display: block; margin-top: 16upx; font-size: 42upx; line-height: 1.2; font-weight: 800; color: #152012; }
.symptom-form-hero__stats { margin-top: 22upx; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12upx; }
.symptom-form-hero__stat { padding: 22upx; border-radius: 30upx; background: rgba(248, 252, 239, 0.84); border: 1upx solid rgba(205, 224, 145, 0.88); }
.symptom-form-hero__stat--dark { background: linear-gradient(135deg, #101612 0%, #1d271f 100%); border-color: #101612; }
.symptom-form-section { display: flex; flex-direction: column; gap: 18upx; }
.symptom-option-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12upx; }
.symptom-option-card,.symptom-level-card { padding: 24upx; border-radius: 32upx; background: rgba(248, 252, 239, 0.84); border: 1upx solid rgba(205, 224, 145, 0.88); }
.symptom-option-card--active,.symptom-level-card--active { background: linear-gradient(145deg, #dbf29f 0%, #c8eb79 48%, #ebf8bf 100%); border-color: rgba(156, 195, 77, 0.58); box-shadow: var(--box-shadow-base); }
.symptom-option-card__title,.symptom-level-card__value { display: block; font-size: 28upx; font-weight: 800; color: var(--text-color); }
.symptom-option-card__desc,.symptom-level-card__desc { display: block; margin-top: 10upx; font-size: 22upx; line-height: 1.6; color: var(--text-color-light); }
.symptom-level-list,.symptom-picker-list,.symptom-form-actions,.symptom-field { display: flex; flex-direction: column; gap: 12upx; }
.symptom-field__label { font-size: 22upx; color: var(--text-color-light); }
.symptom-field__input,.symptom-form-textarea { width: 100%; border-radius: 32upx; border: 1upx solid rgba(205, 224, 145, 0.9); background: rgba(248, 252, 239, 0.92); padding: 24upx; font-size: 24upx; color: var(--text-color); }
.symptom-picker-row { min-height: 92upx; padding: 18upx 20upx; border-radius: 30upx; background: rgba(248, 252, 239, 0.84); border: 1upx solid rgba(205, 224, 145, 0.88); display: flex; align-items: center; justify-content: space-between; gap: 12upx; }
.symptom-picker-row__label { font-size: 24upx; color: var(--text-color-light); }
.symptom-picker-row__value-group { display: flex; align-items: center; gap: 10upx; }
.symptom-picker-row__control { min-width: 0; }
.symptom-picker-row__control--number { width: 180upx; }
.symptom-picker-row__control--unit { width: 160upx; }
.symptom-picker-row__control--single { width: 100%; max-width: 420upx; }
.symptom-picker-row__native-trigger { min-height: 72upx; padding: 0 20upx; }
.symptom-form-textarea { min-height: 240upx; line-height: 1.7; }
</style>
