<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack science-detail-screen">
      <hc-topbar
        title="科普详情"
        subtitle="先读摘要，再看正文与互动区"
        :show-back="true"
        @left="back"
      />

      <view v-if="isLoading" class="hc-card-soft science-detail-state hc-reveal-up">
        <text class="science-detail-state__title">正在加载内容</text>
        <text class="science-detail-state__desc">正文、互动状态和评论区会一起整理完成。</text>
      </view>

      <view v-else-if="loadError" class="hc-card-soft science-detail-error hc-reveal-up">
        <text class="science-detail-error__title">当前无法打开这篇内容</text>
        <text class="science-detail-error__desc">{{ loadError }}</text>
        <view class="science-detail-error__actions">
          <view class="hc-pill-button-dark hc-interactive-pill" @click="reloadPage">重新加载</view>
          <view class="hc-pill-button-soft hc-interactive-pill" @click="back">返回上一页</view>
        </view>
      </view>

      <template v-else-if="science">
        <view class="hc-card-dark science-detail-hero hc-reveal-up hc-shine">
          <view class="hc-kicker hc-kicker--dark">科普内容</view>
          <text class="science-detail-hero__title">{{ science.Title }}</text>
          <text v-if="science.Summary" class="science-detail-hero__summary">{{ science.Summary }}</text>
          <view class="science-detail-hero__meta">
            <view class="science-detail-hero__meta-item">
              <text class="science-detail-hero__meta-label">阅读</text>
              <text class="science-detail-hero__meta-value">{{ science.ReadCount || 0 }}</text>
            </view>
            <view class="science-detail-hero__meta-item">
              <text class="science-detail-hero__meta-label">点赞</text>
              <text class="science-detail-hero__meta-value">{{ science.LikeCount || 0 }}</text>
            </view>
            <view class="science-detail-hero__meta-item">
              <text class="science-detail-hero__meta-label">收藏</text>
              <text class="science-detail-hero__meta-value">{{ science.CollectCount || 0 }}</text>
            </view>
          </view>
        </view>

        <view class="hc-card-soft science-detail-status hc-reveal-up" style="--delay: 100ms">
          <view class="hc-section-head">
            <text class="hc-section-head__title">互动状态</text>
            <text class="hc-section-head__meta">{{ comments.length }} 条评论</text>
          </view>
          <view class="science-detail-status__tags">
            <view class="science-detail-status__tag" :class="{ active: isLiked }">{{ isLiked ? "已点赞" : "点赞中立" }}</view>
            <view class="science-detail-status__tag" :class="{ active: isCollected }">{{ isCollected ? "已收藏" : "未收藏" }}</view>
          </view>
          <text class="science-detail-status__note">
            如果内容对你有帮助，可以点赞收藏；也可以在评论区分享自己的护理经验。
          </text>
        </view>

        <view class="hc-card-soft science-detail-body hc-reveal-up" style="--delay: 160ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">正文内容</text>
            <text class="bg-section-head__meta">向下阅读完整内容</text>
          </view>
          <view class="science-detail-body__content">
            <rich-text :nodes="science.ScienceContent"></rich-text>
          </view>
        </view>

        <view class="hc-card-soft science-detail-comments hc-reveal-up" style="--delay: 220ms">
          <view class="bg-section-head">
            <text class="bg-section-head__title">评论区</text>
            <text class="bg-section-head__meta">{{ comments.length }} 条主评论</text>
          </view>

          <template v-if="comments.length">
            <view
              v-for="comment in comments"
              :key="comment.Id"
              class="science-detail-comment"
              :id="`comment-main-${comment.Id}`"
            >
              <view class="science-detail-comment__head">
                <view>
                  <text class="science-detail-comment__user">{{ (comment.UserDto && comment.UserDto.Name) || "用户" }}</text>
                  <text class="science-detail-comment__time">{{ comment.CreationTime }}</text>
                </view>
                <text v-if="comment.AuditStatus !== 1" class="science-detail-comment__status">
                  {{ comment.AuditStatus === 0 ? "审核中" : comment.AuditStatus === 2 ? "已驳回" : "不可见" }}
                </text>
              </view>

              <text class="science-detail-comment__content">{{ comment.CommentContent }}</text>

              <view class="science-detail-comment__actions">
                <text
                  class="science-detail-comment__action"
                  :class="{ active: comment.IsLiked }"
                  @click="toggleCommentLike(comment)"
                >
                  {{ comment.LikeCount && comment.LikeCount > 0 ? `赞 ${comment.LikeCount}` : "赞" }}
                </text>
                <text class="science-detail-comment__action" @click="openReply(comment)">回复</text>
                <text
                  v-if="(comment.ReplyCount || 0) > 0"
                  class="science-detail-comment__action"
                  @click="toggleReplies(comment)"
                >
                  {{ comment._expanded ? "收起回复" : `展开 ${comment.ReplyCount} 条回复` }}
                </text>
              </view>

              <view v-if="comment.Children && comment.Children.length" class="science-detail-comment__children">
                <view v-for="reply in comment.Children" :key="reply.Id" class="science-detail-reply">
                  <view class="science-detail-comment__head">
                    <view>
                      <text class="science-detail-comment__user">{{ (reply.UserDto && reply.UserDto.Name) || "用户" }}</text>
                      <text class="science-detail-comment__time">{{ reply.CreationTime }}</text>
                    </view>
                  </view>
                  <text class="science-detail-comment__content">{{ reply.CommentContent }}</text>
                  <view class="science-detail-comment__actions">
                    <text
                      class="science-detail-comment__action"
                      :class="{ active: reply.IsLiked }"
                      @click="toggleCommentLike(reply)"
                    >
                      {{ reply.LikeCount && reply.LikeCount > 0 ? `赞 ${reply.LikeCount}` : "赞" }}
                    </text>
                    <text class="science-detail-comment__action" @click="openReply(reply)">回复</text>
                  </view>
                </view>
              </view>
            </view>
          </template>

          <view v-else class="science-detail-comments__empty">
            <text class="science-detail-comments__empty-title">还没有评论</text>
            <text class="science-detail-comments__empty-desc">如果这篇内容对你有帮助，可以留下第一条反馈。</text>
          </view>
        </view>
      </template>
    </view>

    <view v-if="science" class="science-detail-bottom">
      <view class="science-detail-bottom__shell">
        <view class="science-detail-bottom__button hc-pill-button-soft hc-interactive-pill" :class="{ active: isLiked }" @click="toggleLike">
          {{ isLiked ? "已点赞" : "点赞" }}
        </view>
        <view
          class="science-detail-bottom__button hc-pill-button-soft hc-interactive-pill"
          :class="{ active: isCollected }"
          @click="toggleCollect"
        >
          {{ isCollected ? "已收藏" : "收藏" }}
        </view>
        <view class="science-detail-bottom__button science-detail-bottom__button--primary hc-pill-button-dark hc-interactive-pill" @click="openComment">
          评论
        </view>
      </view>
    </view>

    <uni-popup
      ref="commentPopupRef"
      type="center"
      :safe-area="false"
      background-color="transparent"
      mask-background-color="rgba(16, 24, 19, 0.58)"
      @maskClick="closePopup"
    >
      <view class="science-detail-popup">
        <view class="science-detail-popup__handle"></view>
        <view class="science-detail-popup__head">
          <text class="science-detail-popup__eyebrow">互动反馈</text>
          <text class="science-detail-popup__title">
            {{ replyTo ? `回复 ${(replyTo.UserDto && replyTo.UserDto.Name) || "用户"}` : "发表评论" }}
          </text>
          <text class="science-detail-popup__subtitle">
            {{ replyTo ? "补充你的回复内容，系统会继续按原有评论逻辑提交。" : "分享你的护嗓心得或对内容的补充建议。" }}
          </text>
        </view>
        <textarea
          v-model="commentText"
          class="science-detail-popup__textarea"
          placeholder="写下你的护嗓心得，文明发言。"
          maxlength="200"
        />
        <view class="science-detail-popup__actions">
          <view class="hc-pill-button-soft hc-interactive-pill" @click="closePopup">取消</view>
          <view class="hc-pill-button-dark hc-interactive-pill" @click="submitComment">提交</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { Post, PostSilent } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";

const commonStore = useCommonStore();

const Id = ref(null);
const science = ref(null);
const isLiked = ref(false);
const isCollected = ref(false);
const comments = ref([]);
const isLoading = ref(true);
const loadError = ref("");
const commentPopupRef = ref(null);
const commentText = ref("");
const replyTo = ref(null);

const ensureClientKey = () => {
  let key = uni.getStorageSync("ClientKey");
  if (!key) {
    key = `CK_${Date.now()}_${Math.random().toString(16).slice(2)}`;
    uni.setStorageSync("ClientKey", key);
  }
  return key;
};

const loadDetail = async () => {
  const { Data, Success, Msg } = await Post("/Front/Science/Detail", { ScienceId: Id.value });
  if (!Success) {
    loadError.value = Msg || "加载失败";
    uni.showToast({ icon: "none", title: Msg || "加载失败" });
    return;
  }

  science.value = Data.Science;
  isLiked.value = (Data.UserState && Data.UserState.IsLiked) || false;
  isCollected.value = (Data.UserState && Data.UserState.IsCollected) || false;

  await PostSilent("/Front/Science/Read", {
    ScienceId: Id.value,
    ClientKey: ensureClientKey(),
    ReadScene: "DETAIL",
  });
};

const loadComments = async () => {
  const { Data, Success, Msg } = await Post("/Front/Science/CommentList", {
    ScienceId: Id.value,
    Page: 1,
    Limit: 20,
    PreviewLimit: 2,
  });
  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "评论加载失败" });
    return;
  }

  const list = Data?.Items || [];
  list.forEach((comment) => {
    comment._expanded = false;
    comment._allChildren = null;
    comment.Children = [];
  });
  comments.value = list;
};

const toggleReplies = async (comment) => {
  if (!comment._expanded) {
    if (!comment._allChildren) {
      const { Data, Success, Msg } = await Post("/Front/Science/CommentList", {
        ScienceId: Id.value,
        Page: 1,
        Limit: 50,
        ParentCommentId: comment.Id,
      });
      if (!Success) {
        uni.showToast({ icon: "none", title: Msg || "加载回复失败" });
        return;
      }
      comment._allChildren = Data?.Items || [];
    }
    comment.Children = comment._allChildren;
    comment._expanded = true;
  } else {
    comment.Children = [];
    comment._expanded = false;
    uni.pageScrollTo({
      selector: `#comment-main-${comment.Id}`,
      duration: 200,
      offsetTop: 120,
    });
  }
  comments.value = [...comments.value];
};

const toggleLike = async () => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录" });
    return;
  }

  const { Data, Success, Msg } = await Post("/Front/Science/LikeToggle", { ScienceId: Id.value });
  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "操作失败" });
    return;
  }

  isLiked.value = !!Data.State;
  if (science.value) science.value.LikeCount = Data.Count;
};

const toggleCollect = async () => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录" });
    return;
  }

  const { Data, Success, Msg } = await Post("/Front/Science/CollectToggle", { ScienceId: Id.value });
  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "操作失败" });
    return;
  }

  isCollected.value = !!Data.State;
  if (science.value) science.value.CollectCount = Data.Count;
};

const toggleCommentLike = async (comment) => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录" });
    return;
  }
  if (!comment || !comment.Id) return;

  const { Data, Success, Msg } = await Post("/Front/Science/CommentLikeToggle", {
    CommentId: comment.Id,
  });
  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "操作失败" });
    return;
  }

  comment.IsLiked = !!Data.State;
  comment.LikeCount = Data.Count != null ? Data.Count : 0;
  comments.value = [...comments.value];
};

const openComment = () => {
  replyTo.value = null;
  commentText.value = "";
  commentPopupRef.value?.open();
};

const openReply = (comment) => {
  replyTo.value = comment;
  commentText.value = "";
  commentPopupRef.value?.open();
};

const closePopup = () => {
  commentPopupRef.value?.close();
};

const submitComment = async () => {
  if (!commonStore.UserId) {
    uni.showToast({ icon: "none", title: "请先登录" });
    return;
  }

  const text = (commentText.value || "").trim();
  if (!text) {
    uni.showToast({ icon: "none", title: "请输入评论" });
    return;
  }

  const body = {
    ScienceId: Id.value,
    CommentContent: text,
  };

  if (replyTo.value) {
    body.ParentCommentId = replyTo.value.Id;
    body.RootCommentId = replyTo.value.RootCommentId || replyTo.value.Id;
    body.ReplyToUserId = replyTo.value.UserId;
  }

  const { Data, Success, Msg } = await Post("/Front/Science/CommentCreate", body);
  if (!Success) {
    uni.showToast({ icon: "none", title: Msg || "提交失败" });
    return;
  }

  uni.showToast({ icon: "none", title: Data.MsgTip || "已提交" });
  closePopup();
  await loadComments();
};

const reloadPage = async () => {
  loadError.value = "";
  isLoading.value = true;
  await loadDetail();
  if (!loadError.value) {
    await loadComments();
  }
  isLoading.value = false;
};

const back = () => uni.navigateBack({ delta: 1 });

onLoad(async (query) => {
  Id.value = query && query.Id ? Number(query.Id) : null;
  await reloadPage();
});
</script>

<style scoped lang="scss">
.science-detail-screen {
  gap: 24upx;
  padding-bottom: calc(env(safe-area-inset-bottom) + 270upx);
}

.science-detail-state__title,
.science-detail-error__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.science-detail-state__desc,
.science-detail-error__desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.science-detail-error {
  border-color: rgba(228, 108, 73, 0.34);
  background: rgba(255, 243, 238, 0.92);
}

.science-detail-error__actions,
.science-detail-popup__actions {
  margin-top: 18upx;
  display: flex;
  flex-direction: column;
  gap: 12upx;
}

.science-detail-hero {
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.science-detail-hero__title {
  display: block;
  font-size: 48upx;
  line-height: 1.12;
  font-weight: 800;
  color: #f7ffdf;
}

.science-detail-hero__summary {
  display: block;
  font-size: 22upx;
  line-height: 1.7;
  color: rgba(241, 248, 223, 0.76);
}

.science-detail-hero__meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12upx;
}

.science-detail-hero__meta-item {
  padding: 16upx 18upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.science-detail-hero__meta-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.science-detail-hero__meta-value {
  display: block;
  margin-top: 8upx;
  font-size: 28upx;
  font-weight: 800;
  color: #f7ffdf;
}

.science-detail-status__tags {
  margin-top: 18upx;
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.science-detail-status__tag {
  min-height: 62upx;
  padding: 0 24upx;
  border-radius: 9999upx;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(205, 224, 145, 0.88);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22upx;
  color: #556556;
}

.science-detail-status__tag.active {
  background: #151d17;
  border-color: #151d17;
  color: #f4fbdd;
}

.science-detail-status__note {
  display: block;
  margin-top: 16upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.science-detail-body__content {
  margin-top: 18upx;
  font-size: 24upx;
  line-height: 1.8;
  color: #243123;
}

.science-detail-comment + .science-detail-comment {
  margin-top: 18upx;
  padding-top: 18upx;
  border-top: 1upx solid rgba(205, 224, 145, 0.82);
}

.science-detail-comment__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16upx;
}

.science-detail-comment__user {
  display: block;
  font-size: 24upx;
  font-weight: 800;
  color: #172119;
}

.science-detail-comment__time {
  display: block;
  margin-top: 6upx;
  font-size: 20upx;
  color: #7f917d;
}

.science-detail-comment__status {
  font-size: 20upx;
  color: #8d5b1f;
}

.science-detail-comment__content {
  display: block;
  margin-top: 12upx;
  font-size: 24upx;
  line-height: 1.7;
  color: #243123;
}

.science-detail-comment__actions {
  margin-top: 12upx;
  display: flex;
  flex-wrap: wrap;
  gap: 16upx;
}

.science-detail-comment__action {
  font-size: 22upx;
  color: #556556;
}

.science-detail-comment__action.active {
  color: #1d281e;
  font-weight: 800;
}

.science-detail-comment__children {
  margin-top: 14upx;
  padding-left: 18upx;
  border-left: 4upx solid rgba(145, 196, 65, 0.26);
  display: flex;
  flex-direction: column;
  gap: 14upx;
}

.science-detail-reply {
  padding: 14upx 0;
}

.science-detail-comments__empty-title {
  display: block;
  font-size: 30upx;
  font-weight: 800;
  color: #172119;
}

.science-detail-comments__empty-desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.science-detail-bottom {
  position: fixed;
  left: 28upx;
  right: 28upx;
  bottom: calc(env(safe-area-inset-bottom) + 18upx);
  z-index: 12;
}

.science-detail-bottom__shell {
  padding: 18upx;
  border-radius: 38upx;
  background: rgba(248, 252, 239, 0.94);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  box-shadow: var(--box-shadow-lg);
  display: flex;
  gap: 12upx;
}

.science-detail-bottom__button {
  flex: 1;
}

.science-detail-bottom__button.active {
  background: #151d17;
  color: #f7ffdf;
  border-color: #151d17;
}

.science-detail-bottom__button--primary {
  flex: 1.2;
}

.science-detail-popup {
  position: relative;
  width: min(670upx, calc(100vw - 56upx));
  max-height: calc(100vh - env(safe-area-inset-top) - 36upx);
  overflow-y: auto;
  border-radius: 42upx;
  border: 1upx solid rgba(205, 224, 145, 0.9);
  background: linear-gradient(180deg, rgba(248, 252, 239, 0.99) 0%, rgba(239, 247, 224, 0.99) 100%);
  box-shadow: 0 26upx 64upx rgba(18, 24, 20, 0.22);
  padding: 24upx 24upx calc(env(safe-area-inset-bottom) + 24upx);
}

.science-detail-popup__handle {
  width: 88upx;
  height: 8upx;
  margin: 0 auto 24upx;
  border-radius: 999upx;
  background: rgba(111, 127, 77, 0.28);
}

.science-detail-popup__head {
  display: flex;
  flex-direction: column;
  gap: 10upx;
  text-align: center;
}

.science-detail-popup__eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 2upx;
  color: #6f835f;
}

.science-detail-popup__title {
  font-size: 32upx;
  font-weight: 800;
  color: #172119;
}

.science-detail-popup__subtitle {
  font-size: 22upx;
  line-height: 1.65;
  color: #60705d;
}

.science-detail-popup__textarea {
  margin-top: 18upx;
  min-height: 220upx;
  padding: 22upx;
  border-radius: 30upx;
  font-size: 24upx;
  line-height: 1.7;
  border: 1upx solid rgba(205, 224, 145, 0.88);
  background: rgba(248, 252, 239, 0.88);
}
</style>
