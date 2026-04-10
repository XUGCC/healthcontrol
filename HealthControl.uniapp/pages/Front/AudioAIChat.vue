<template>
  <view class="hc-mobile-shell">
    <view class="hc-screen hc-stack ai-chat-screen">
      <hc-topbar
        title="AI 健康咨询"
        subtitle="继续追问结果、护理建议与就医时机"
        :show-back="true"
        @left="goBack"
      />

      <view class="bg-head ai-chat-head hc-reveal-up">
        <view class="hc-kicker">智能解读</view>
        <text class="bg-head__title">把筛查结果和日常疑问放进同一段对话里</text>
        <text class="bg-head__subtitle">
          可直接咨询症状、用嗓习惯，也能从历史检测或频谱图发起单次解读。
        </text>
      </view>

      <view class="ai-chat-grid">
        <view class="hc-card-dark ai-chat-summary hc-reveal-up hc-shine" style="--delay: 100ms">
          <view class="hc-kicker hc-kicker--dark">会话摘要</view>
          <text class="ai-chat-summary__title">已保留最近 {{ messageCount }} 条消息</text>
          <text class="ai-chat-summary__desc">
            {{ storageReady ? "当前账号已启用本地历史记录，返回后仍可继续追问。" : "登录后会自动绑定到你的本地会话历史。" }}
          </text>
          <view class="ai-chat-summary__meta">
            <view class="ai-chat-summary__metric">
              <text class="ai-chat-summary__metric-label">AI 回复</text>
              <text class="ai-chat-summary__metric-value">{{ assistantCount }}</text>
            </view>
            <view class="ai-chat-summary__metric">
              <text class="ai-chat-summary__metric-label">当前状态</text>
              <text class="ai-chat-summary__metric-value">{{ sending ? "思考中" : "可继续提问" }}</text>
            </view>
          </view>
        </view>

        <view class="hc-card-soft ai-chat-note hc-reveal-up" style="--delay: 160ms">
          <view class="hc-section-head">
            <text class="hc-section-head__title">参考边界</text>
            <text class="hc-section-head__meta">先咨询，再判断下一步</text>
          </view>
          <text class="ai-chat-note__text">
            AI 答复仅供健康参考，不构成医疗诊断或个性化医疗建议，不能替代医生就诊。
          </text>
          <view class="ai-chat-note__latest">
            <text class="ai-chat-note__latest-label">最近一条回复</text>
            <text class="ai-chat-note__latest-text">{{ latestAssistantPreview }}</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft ai-tool-card hc-reveal-up" style="--delay: 220ms">
        <view class="hc-section-head">
          <text class="hc-section-head__title">快捷工具</text>
          <text class="hc-section-head__meta">在对话外先完成一次结构化解读</text>
        </view>
        <view class="ai-tool-actions">
          <view class="ai-tool-action ai-tool-action--dark hc-interactive-card" @click="chooseRecordAndAnalyze">
            <text class="ai-tool-action__eyebrow">历史记录</text>
            <text class="ai-tool-action__title">选择录音分析</text>
            <text class="ai-tool-action__desc">从最近检测记录生成一次 AI 结果页，再回来继续追问。</text>
          </view>
          <view class="ai-tool-action hc-interactive-card" @click="chooseSpectrumAndAnalyze">
            <text class="ai-tool-action__eyebrow">频谱图</text>
            <text class="ai-tool-action__title">选择频谱分析</text>
            <text class="ai-tool-action__desc">上传 Mel 或 MFCC 图谱，快速拿到可读结论和建议动作。</text>
          </view>
        </view>
      </view>

      <view class="hc-card-soft conversation-card hc-reveal-up" style="--delay: 280ms">
        <view class="bg-section-head">
          <text class="bg-section-head__title">会话内容</text>
          <text class="bg-section-head__meta">{{ messageCount }} 条消息</text>
        </view>

        <scroll-view
          class="conversation-scroll"
          scroll-y
          :scroll-top="scrollTop"
          :scroll-into-view="scrollIntoView"
          scroll-with-animation
        >
          <view
            v-for="(message, index) in messages"
            :key="index"
            class="message-item"
            :class="`message-item--${message.role}`"
            :id="`msg_${index}`"
          >
            <view class="message-badge" :class="`message-badge--${message.role}`">
              {{ message.role === "user" ? "我" : "AI" }}
            </view>
            <view
              class="message-bubble"
              :class="[
                `message-bubble--${message.role}`,
                { 'message-bubble--thinking': message._thinking },
              ]"
            >
              <text>{{ message.content }}</text>
            </view>
          </view>
          <view class="conversation-scroll__tail"></view>
        </scroll-view>
      </view>
    </view>

    <view v-show="!isPopupOpen" class="ai-chat-composer">
      <view class="ai-chat-composer__shell">
        <input
          class="ai-chat-composer__input"
          v-model="question"
          placeholder="请输入你的问题，例如：嗓子干痒怎么办？"
          confirm-type="send"
          @confirm="send"
        />
        <view
          class="ai-chat-composer__send hc-pill-button-dark hc-interactive-pill"
          :class="{ disabled: sending || !questionTrim }"
          @click="send"
        >
          {{ sending ? "发送中" : "发送" }}
        </view>
      </view>
    </view>
    <hc-action-sheet ref="actionSheetRef" @popup-change="onPopupChange" />
    <hc-dialog ref="dialogRef" @popup-change="onPopupChange" />
  </view>
</template>

<script setup>
import { computed, nextTick, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { Post } from "@/utils/http";
import { useCommonStore } from "@/store";
import HcTopbar from "@/components/hc-topbar/hc-topbar.vue";
import HcActionSheet from "@/components/hc-action-sheet/hc-action-sheet.vue";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const commonStore = useCommonStore();

const question = ref("");
const sending = ref(false);
const messages = ref([
  {
    role: "assistant",
    content: "你好，我可以提供喉部健康科普与就医建议。你可以描述症状，或咨询用声与饮食习惯。",
  },
]);

const scrollTop = ref(0);
const scrollIntoView = ref("");
const actionSheetRef = ref(null);
const dialogRef = ref(null);
const activePopupCount = ref(0);

const questionTrim = computed(() => (question.value || "").trim());
const storageKey = computed(() => {
  const uid = commonStore?.UserId || "";
  return uid ? `ai_chat_history_${uid}` : "";
});
const storageReady = computed(() => !!storageKey.value);
const messageCount = computed(() => messages.value.filter((item) => !item?._thinking).length);
const assistantCount = computed(() =>
  messages.value.filter((item) => item.role === "assistant" && !item?._thinking).length
);
const latestAssistantPreview = computed(() => {
  const latest = [...messages.value]
    .reverse()
    .find((item) => item.role === "assistant" && !item?._thinking && item.content);
  if (!latest) return "继续发送问题，AI 会根据上下文给出下一步建议。";
  return latest.content.length > 44 ? `${latest.content.slice(0, 44)}...` : latest.content;
});
const isPopupOpen = computed(() => activePopupCount.value > 0);
const showActionSheet = (options) =>
  actionSheetRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true, tapIndex: -1, action: null });
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });
const onPopupChange = (show) => {
  activePopupCount.value = Math.max(0, activePopupCount.value + (show ? 1 : -1));
};

const syncScroll = async () => {
  await nextTick();
  scrollTop.value = 999999;
  scrollIntoView.value = `msg_${messages.value.length - 1}`;
};

const saveHistoryLocal = () => {
  if (!storageKey.value) return;
  try {
    const cleaned = messages.value.filter((item) => !item?._thinking).slice(-60);
    uni.setStorageSync(storageKey.value, cleaned);
  } catch (error) {
    console.error("Failed to save AI chat history", error);
  }
};

const loadHistoryLocal = () => {
  if (!storageKey.value) return;
  try {
    const stored = uni.getStorageSync(storageKey.value);
    if (Array.isArray(stored) && stored.length) {
      messages.value = stored;
    }
  } catch (error) {
    console.error("Failed to load AI chat history", error);
  }
};

const buildHistory = () =>
  messages.value
    .slice(-8)
    .filter(
      (item) =>
        item &&
        !item._thinking &&
        (item.role === "user" || item.role === "assistant") &&
        typeof item.content === "string" &&
        item.content.trim()
    )
    .map((item) => ({
      Role: item.role,
      Content: item.content,
    }));

const send = async () => {
  const currentQuestion = questionTrim.value;
  if (!currentQuestion || sending.value) return;

  sending.value = true;
  question.value = "";
  messages.value.push({ role: "user", content: currentQuestion });
  messages.value.push({
    role: "assistant",
    content: "AI 正在思考中…",
    _thinking: true,
    _id: Date.now(),
  });
  saveHistoryLocal();
  await syncScroll();

  try {
    const startTime = Date.now();
    const response = await Post("/FrontAI/Chat", {
      Question: currentQuestion,
      History: buildHistory(),
    });
    const elapsed = Date.now() - startTime;
    if (elapsed < 600) {
      await new Promise((resolve) => setTimeout(resolve, 600 - elapsed));
    }

    messages.value = messages.value.filter((item) => !item._thinking);

    if (!response.Success) {
      messages.value.push({
        role: "assistant",
        content: response.Msg || "AI 咨询失败，请稍后重试。",
      });
    } else {
      const data = response.Data || {};
      messages.value.push({
        role: "assistant",
        content: data.Answer || "（无内容）",
      });
      if (data.Disclaimer) {
        messages.value.push({
          role: "assistant",
          content: data.Disclaimer,
        });
      }
    }
  } catch (error) {
    messages.value = messages.value.filter((item) => !item._thinking);
    messages.value.push({
      role: "assistant",
      content: "AI 咨询失败，请检查网络或稍后重试。",
    });
  } finally {
    sending.value = false;
    saveHistoryLocal();
    await syncScroll();
  }
};

const chooseRecordAndAnalyze = async () => {
  try {
    const response = await Post("/TAudioScreenRecord/List", {
      Page: 1,
      Limit: 20,
    });
    if (!response.Success) {
      uni.showToast({ title: response.Msg || "加载记录失败", icon: "none" });
      return;
    }

    const items = response.Data?.Items || [];
    if (!items.length) {
      uni.showToast({ title: "暂无检测记录", icon: "none" });
      return;
    }

    const labels = items.map((item) => {
      const time = item.CreationTime ? String(item.CreationTime).replace("T", " ") : "";
      const status = item.DetectTotalStatus ? (item.PrimaryScreenResult ? "异常" : "正常") : "检测中";
      return `#${item.Id} ${status} ${time}`;
    });

    const result = await showActionSheet({
      title: "选择历史检测记录",
      subtitle: "从最近检测记录生成一次 AI 解读",
      actions: items.map((item, index) => ({
        key: item.Id || index,
        label: labels[index],
      })),
    });
    if (!result.confirm || result.tapIndex < 0) return;
    const picked = items[result.tapIndex];
    const dialogResult = await showDialog({
      title: "生成方式",
      content: "若该记录已生成过 AI 解读，默认会复用上次结果。是否强制重新生成？",
      confirmText: "重新生成",
      cancelText: "复用",
    });
    const force = !!dialogResult.confirm;

    uni.navigateTo({
      url: `/pages/Front/AudioAIToolResult?mode=record&recordId=${picked.Id}&force=${force ? 1 : 0}`,
    });
  } catch (error) {
    uni.showToast({ title: "加载记录失败", icon: "none" });
  }
};

const chooseSpectrumAndAnalyze = async () => {
  uni.chooseImage({
    count: 1,
    sizeType: ["original", "compressed"],
    sourceType: ["album", "camera"],
    success: (result) => {
      const filePath = result?.tempFilePaths?.[0];
      if (!filePath) return;

      showActionSheet({
        title: "选择图谱类型",
        subtitle: "图谱类型会影响后续 AI 解读维度",
        actions: [
          { key: "MEL", label: "Mel 图谱" },
          { key: "MFCC", label: "MFCC 图谱" },
        ],
      }).then((pick) => {
        if (!pick.confirm) return;
        const spectrumType = pick.tapIndex === 1 ? "MFCC" : "MEL";
        uni.navigateTo({
          url: `/pages/Front/AudioAIToolResult?mode=upload&spectrumType=${spectrumType}&localPath=${encodeURIComponent(filePath)}`,
        });
      });
    },
  });
};

const goBack = () => uni.navigateBack({ delta: 1 });

onLoad(async () => {
  loadHistoryLocal();
  await syncScroll();
});

onShow(async () => {
  loadHistoryLocal();
  await syncScroll();
});
</script>

<style scoped lang="scss">
.ai-chat-screen {
  gap: 24upx;
  padding-bottom: calc(env(safe-area-inset-bottom) + 280upx);
}

.ai-chat-head {
  gap: 10upx;
}

.ai-chat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18upx;
}

.ai-chat-summary,
.ai-chat-note {
  min-height: 260upx;
  display: flex;
  flex-direction: column;
}

.ai-chat-summary__title {
  display: block;
  margin-top: 18upx;
  font-size: 34upx;
  line-height: 1.25;
  font-weight: 800;
  color: #f6ffdf;
}

.ai-chat-summary__desc {
  display: block;
  margin-top: 10upx;
  font-size: 22upx;
  line-height: 1.6;
  color: rgba(241, 248, 223, 0.72);
}

.ai-chat-summary__meta {
  margin-top: auto;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12upx;
}

.ai-chat-summary__metric {
  padding: 16upx 18upx;
  border-radius: 24upx;
  background: rgba(241, 248, 223, 0.08);
  border: 1upx solid rgba(202, 235, 134, 0.14);
}

.ai-chat-summary__metric-label {
  display: block;
  font-size: 20upx;
  color: rgba(241, 248, 223, 0.72);
}

.ai-chat-summary__metric-value {
  display: block;
  margin-top: 8upx;
  font-size: 26upx;
  font-weight: 800;
  color: #f7ffdf;
}

.ai-chat-note__text {
  display: block;
  margin-top: 18upx;
  font-size: 22upx;
  line-height: 1.65;
  color: #556556;
}

.ai-chat-note__latest {
  margin-top: auto;
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.8);
  border: 1upx solid rgba(201, 220, 145, 0.8);
}

.ai-chat-note__latest-label {
  display: block;
  font-size: 20upx;
  color: #859585;
}

.ai-chat-note__latest-text {
  display: block;
  margin-top: 8upx;
  font-size: 22upx;
  line-height: 1.55;
  color: #243123;
}

.ai-tool-actions {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16upx;
}

.ai-tool-action {
  min-height: 218upx;
  padding: 24upx;
  border-radius: 34upx;
  background: rgba(248, 252, 239, 0.78);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.ai-tool-action--dark {
  background: linear-gradient(135deg, #101612 0%, #1e2820 100%);
  border-color: #101612;
}

.ai-tool-action__eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 1upx;
  color: #7f917d;
}

.ai-tool-action__title {
  font-size: 32upx;
  line-height: 1.2;
  font-weight: 800;
  color: #172119;
}

.ai-tool-action__desc {
  font-size: 22upx;
  line-height: 1.6;
  color: #556556;
}

.ai-tool-action--dark .ai-tool-action__eyebrow,
.ai-tool-action--dark .ai-tool-action__title,
.ai-tool-action--dark .ai-tool-action__desc {
  color: #f7ffdf;
}

.conversation-card {
  padding-bottom: 18upx;
}

.conversation-scroll {
  height: 58vh;
  margin-top: 18upx;
  padding: 4upx 6upx 0;
}

.conversation-scroll__tail {
  height: 24upx;
}

.message-item {
  display: flex;
  gap: 12upx;
  margin-bottom: 16upx;
}

.message-item--user {
  justify-content: flex-end;
}

.message-badge {
  width: 54upx;
  height: 54upx;
  border-radius: 18upx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22upx;
  font-weight: 800;
  flex-shrink: 0;
}

.message-badge--assistant {
  background: rgba(21, 29, 23, 0.94);
  color: #f7ffdf;
}

.message-badge--user {
  order: 2;
  background: rgba(226, 243, 173, 0.96);
  color: #1d281e;
}

.message-bubble {
  max-width: calc(100% - 72upx);
  padding: 18upx 22upx;
  border-radius: 30upx;
  font-size: 24upx;
  line-height: 1.7;
  word-break: break-word;
}

.message-bubble--assistant {
  background: rgba(248, 252, 239, 0.94);
  border: 1upx solid rgba(205, 224, 145, 0.88);
  color: #2a392b;
}

.message-bubble--user {
  background: linear-gradient(135deg, #dff49d 0%, #cbed7d 100%);
  border: 1upx solid rgba(145, 196, 65, 0.46);
  color: #162113;
}

.message-bubble--thinking {
  opacity: 0.88;
}

.ai-chat-composer {
  position: fixed;
  left: 28upx;
  right: 28upx;
  bottom: calc(env(safe-area-inset-bottom) + 18upx);
  z-index: 12;
}

.ai-chat-composer__shell {
  padding: 18upx;
  border-radius: 38upx;
  background: rgba(248, 252, 239, 0.94);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  box-shadow: var(--box-shadow-lg);
  display: flex;
  align-items: center;
  gap: 16upx;
}

.ai-chat-composer__input {
  flex: 1;
  min-height: 84upx;
  padding: 0 26upx;
  border-radius: 28upx;
  font-size: 24upx;
}

.ai-chat-composer__send {
  min-width: 170upx;
}

.ai-chat-composer__send.disabled {
  opacity: 0.5;
  pointer-events: none;
}
</style>
