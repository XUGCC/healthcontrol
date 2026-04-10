<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack science-publish-screen">
      <hc-topbar
        :title="isEdit ? '编辑科普' : '发布科普'"
        :subtitle="isEdit ? '继续完善已有内容并重新提交' : '用更容易读懂的方式整理你的科普内容'"
        :show-back="true"
        right-text="草稿"
        @left="back"
        @right="saveDraft"
      />

      <view class="hc-card-lime science-publish-hero hc-reveal-up">
        <view class="hc-kicker">内容编辑</view>
        <text class="science-publish-hero__title">{{ isEdit ? "继续完善这篇内容" : "先补齐关键信息，再提交审核" }}</text>
        <text class="science-publish-hero__desc">
          标题、分类、摘要和封面会决定用户第一眼看到的内容层级，尽量保持简洁、可扫读、行动导向。
        </text>
      </view>

      <view class="hc-card-soft science-publish-section hc-reveal-up" style="--delay: 80ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">基础信息</text>
          <text class="bg-section-head__meta">先确定主题和内容定位</text>
        </view>

        <view class="science-publish-field">
          <text class="science-publish-field__label">标题</text>
          <uni-easyinput
            v-model="form.Title"
            maxlength="40"
            placeholder="请输入 5-40 字标题"
            primaryColor="var(--primary-color)"
            :clearable="true"
            :inputBorder="false"
          />
        </view>

        <view class="science-publish-field-grid">
          <view class="science-publish-field">
            <text class="science-publish-field__label">分类</text>
            <view class="hc-official-select-trigger" :class="{ 'is-placeholder': !categoryLabel }" @click="openCategoryPopup">
              <text class="hc-official-select-trigger__value">{{ categoryLabel || "请选择分类" }}</text>
              <uni-icons type="down" size="18" color="#5e705d" />
            </view>
          </view>
          <view class="science-publish-field">
            <text class="science-publish-field__label">知识类型</text>
            <view class="hc-official-select-trigger" @click="openTypePopup">
              <text class="hc-official-select-trigger__value">{{ typeLabel || "请选择类型" }}</text>
              <uni-icons type="down" size="18" color="#5e705d" />
            </view>
          </view>
        </view>
      </view>

      <view class="hc-card-soft science-publish-section hc-reveal-up" style="--delay: 140ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">封面与摘要</text>
          <text class="bg-section-head__meta">帮助用户快速理解内容重点</text>
        </view>

        <view class="science-publish-field">
          <text class="science-publish-field__label">封面</text>
          <view class="science-publish-cover">
            <image v-if="form.CoverUrl" class="science-publish-cover__image" mode="aspectFill" :src="form.CoverUrl" />
            <view v-else class="science-publish-cover__placeholder">
              <text class="science-publish-cover__placeholder-title">上传封面图</text>
              <text class="science-publish-cover__placeholder-desc">推荐使用横图，便于列表和首页展示。</text>
            </view>
          </view>
          <view class="science-publish-cover__actions">
            <view class="hc-pill-button-soft hc-interactive-pill" @click="pickCover">
              {{ form.CoverUrl ? "更换封面" : "上传封面" }}
            </view>
          </view>
        </view>

        <view class="science-publish-field">
          <text class="science-publish-field__label">摘要（可选）</text>
          <textarea
            class="science-publish-textarea"
            v-model="form.Summary"
            placeholder="不填将自动截取正文前 120 字"
            maxlength="120"
          />
        </view>
      </view>

      <view class="hc-card-soft science-publish-section hc-reveal-up" style="--delay: 200ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">正文内容</text>
          <text class="bg-section-head__meta">建议多用短段落，便于移动端阅读</text>
        </view>
        <view class="science-publish-editor">
          <rich-text-edit v-model="form.ScienceContent" />
        </view>
      </view>

      <view class="hc-card-soft science-publish-tip hc-reveal-up" style="--delay: 260ms">
        <text class="science-publish-tip__title">发布提醒</text>
        <text class="science-publish-tip__desc">
          科普内容仅供参考，不替代诊断；请勿发布隐私信息、夸大疗效或违规内容。
        </text>
      </view>

      <view class="hc-pill-button-dark hc-interactive-pill science-publish-submit hc-reveal-up" style="--delay: 320ms" @click="submit">
        {{ isEdit ? "保存并提交审核" : "提交审核" }}
      </view>

      <uni-popup
        ref="categoryPopupRef"
        type="center"
        :safe-area="false"
        background-color="transparent"
        mask-background-color="rgba(16, 24, 19, 0.58)"
      >
        <view class="hc-official-popup-card">
          <view class="hc-official-popup-handle"></view>
          <view class="hc-official-popup-head">
            <text class="hc-official-popup-eyebrow">筛选与选择</text>
            <text class="hc-official-popup-title">选择科普分类</text>
            <text class="hc-official-popup-subtitle">分类会影响首页推荐和后续筛选归档</text>
          </view>
          <view class="hc-official-popup-summary">
            <text class="hc-official-popup-summary__label">当前</text>
            <text class="hc-official-popup-summary__value">{{ categoryLabel || "未选择" }}</text>
          </view>
          <scroll-view scroll-y class="hc-official-popup-list">
            <view
              v-for="option in categoryOptions"
              :key="String(option.value)"
              class="hc-official-popup-option"
              :class="{ active: form.CategoryId === option.value }"
              @click="onCategoryChange(option)"
            >
              <text class="hc-official-popup-option__text">{{ option.text }}</text>
              <text v-if="form.CategoryId === option.value" class="hc-official-popup-option__mark">当前</text>
            </view>
          </scroll-view>
        </view>
      </uni-popup>

      <uni-popup
        ref="typePopupRef"
        type="center"
        :safe-area="false"
        background-color="transparent"
        mask-background-color="rgba(16, 24, 19, 0.58)"
      >
        <view class="hc-official-popup-card">
          <view class="hc-official-popup-handle"></view>
          <view class="hc-official-popup-head">
            <text class="hc-official-popup-eyebrow">筛选与选择</text>
            <text class="hc-official-popup-title">选择知识类型</text>
            <text class="hc-official-popup-subtitle">帮助用户按内容方向快速筛选</text>
          </view>
          <view class="hc-official-popup-summary">
            <text class="hc-official-popup-summary__label">当前</text>
            <text class="hc-official-popup-summary__value">{{ typeLabel || "未选择" }}</text>
          </view>
          <scroll-view scroll-y class="hc-official-popup-list">
            <view
              v-for="option in typeOptions"
              :key="String(option.value)"
              class="hc-official-popup-option"
              :class="{ active: form.KnowledgeType === option.value }"
              @click="onTypeChange(option)"
            >
              <text class="hc-official-popup-option__text">{{ option.text }}</text>
              <text v-if="form.KnowledgeType === option.value" class="hc-official-popup-option__mark">当前</text>
            </view>
          </scroll-view>
        </view>
      </uni-popup>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post, UploadImageByCamera } from "@/utils/http";
import { useCommonStore } from "@/store";
import RichTextEdit from "@/components/rich-text-edit/rich-text-edit.vue";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();
const Id = ref(null);

const form = ref({
  Id: null,
  Title: "",
  CoverUrl: "",
  CategoryId: null,
  KnowledgeType: 2,
  ScienceContent: "",
  Summary: "",
});

const categoryOptions = ref([]);
const categoryLabel = ref("");
const categoryPopupRef = ref(null);
const typePopupRef = ref(null);
const typeOptions = [
  { text: "疾病科普", value: 1 },
  { text: "护喉贴士", value: 2 },
  { text: "发音训练", value: 3 },
  { text: "就医指南/随访", value: 4 },
  { text: "其他", value: 9 },
];
const typeLabel = ref("护喉贴士");

const isEdit = computed(() => !!Id.value);

const loadCategories = async () => {
  const { Data, Success } = await Post("/Front/Science/CategoryList", { IncludeEmpty: true });
  if (!Success) return;
  const flat = (Data || []).flatMap((root) => (root.Children && root.Children.length ? root.Children : [root]));
  categoryOptions.value = flat.map((item) => ({ text: item.CategoryName, value: item.Id }));
};

const openCategoryPopup = () => categoryPopupRef.value?.open();
const openTypePopup = () => typePopupRef.value?.open();

const onCategoryChange = (item) => {
  if (!item) {
    form.value.CategoryId = null;
    categoryLabel.value = "";
    categoryPopupRef.value?.close();
    return;
  }
  form.value.CategoryId = item.value;
  categoryLabel.value = item.text;
  categoryPopupRef.value?.close();
};

const onTypeChange = (item) => {
  if (!item) return;
  form.value.KnowledgeType = item.value;
  typeLabel.value = item.text;
  typePopupRef.value?.close();
};

const pickCover = async () => {
  const list = await UploadImageByCamera(1);
  if (list && list.length) {
    form.value.CoverUrl = list[0].Url;
  }
};

const saveDraft = async () => {
  await save("DRAFT");
};

const submit = async () => {
  await save("SUBMIT");
};

const save = async (type) => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录" });
    return;
  }

  const { Success, Msg } = await Post("/Front/Science/CreateOrEdit", {
    ...form.value,
    SubmitType: type,
  });

  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "保存失败" });
    return;
  }

  uni.showToast({ icon: "none", title: type === "DRAFT" ? "已保存草稿" : "已提交审核" });
  setTimeout(() => {
    uni.navigateBack({ delta: 1 });
  }, 800);
};

const back = () => uni.navigateBack({ delta: 1 });

onLoad(async (query) => {
  await loadCategories();
  if (!commonStore.UserId) return;

  if (query && query.Id) {
    Id.value = Number(query.Id);
    form.value.Id = Id.value;
    const { Data, Success } = await Post("/Front/Science/MyDetail", { ScienceId: Id.value });
    if (Success && Data) {
      form.value.Title = Data.Title || "";
      form.value.CoverUrl = Data.CoverUrl || "";
      form.value.CategoryId = Data.CategoryId || null;
      form.value.KnowledgeType = Data.KnowledgeType || 2;
      form.value.ScienceContent = Data.ScienceContent || "";
      form.value.Summary = Data.Summary || "";

      const category = categoryOptions.value.find((item) => item.value === form.value.CategoryId);
      if (category) categoryLabel.value = category.text;
      const type = typeOptions.find((item) => item.value === form.value.KnowledgeType);
      if (type) typeLabel.value = type.text;
    }
  }
});
</script>

<style scoped lang="scss">
.science-publish-screen {
  gap: 24upx;
}

.science-publish-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.science-publish-hero__title {
  display: block;
  font-size: 46upx;
  line-height: 1.1;
  font-weight: 800;
  color: #172119;
}

.science-publish-hero__desc {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #435442;
}

.science-publish-field {
  margin-top: 18upx;
}

.science-publish-field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14upx;
}

.science-publish-field__label {
  display: block;
  margin-bottom: 10upx;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.science-publish-picker {
  min-height: 88upx;
  padding: 0 22upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  align-items: center;
  font-size: 24upx;
  color: #243123;
}

.science-publish-cover {
  min-height: 260upx;
  border-radius: 34upx;
  overflow: hidden;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: rgba(248, 252, 239, 0.82);
  display: flex;
  align-items: center;
  justify-content: center;
}

.science-publish-cover__image {
  width: 100%;
  height: 260upx;
}

.science-publish-cover__placeholder-title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
  text-align: center;
}

.science-publish-cover__placeholder-desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.6;
  color: #556556;
  text-align: center;
}

.science-publish-cover__actions {
  margin-top: 16upx;
}

.science-publish-textarea {
  min-height: 160upx;
  padding: 20upx 22upx;
  border-radius: 30upx;
  font-size: 24upx;
  line-height: 1.7;
}

.science-publish-editor {
  margin-top: 18upx;
}

.science-publish-tip__title {
  display: block;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.science-publish-tip__desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.science-publish-submit {
  margin-top: 4upx;
}
</style>
