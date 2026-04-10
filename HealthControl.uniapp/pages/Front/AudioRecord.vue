<template>
  <view class="hc-mobile-shell audio-record-page">
    <view class="hc-screen hc-stack record-screen">
      <view v-if="showBackButton" class="page-back hc-interactive-pill" @click="goBack">
        <uni-icons type="left" size="18" color="#18211b" />
        <text>返回</text>
      </view>

      <view class="bg-head audio-head hc-reveal-up">
        <view class="audio-head__row">
          <view class="audio-head__pill audio-head__pill--light">语音自查</view>
          <view class="audio-head__pill record-status-pill">{{ hasRecorded ? "已采集" : "待采集" }}</view>
        </view>
        <text class="bg-head__title">1分钟完成一次喉部语音自查</text>
        <text class="bg-head__subtitle">按住按钮录音，松开结束。支持试听、删除与上传检测。</text>
      </view>

      <view class="record-stats hc-reveal-up" style="--delay: 120ms">
        <view class="record-stat hc-interactive-card">
          <text class="record-stat__value">{{ timeDisplay }}</text>
          <text class="record-stat__label">当前时长</text>
        </view>
        <view class="record-stat hc-interactive-card">
          <text class="record-stat__value">{{ hasRecorded ? "已完成" : "等待中" }}</text>
          <text class="record-stat__label">录音状态</text>
        </view>
      </view>

      <view class="hc-card-lime record-main hc-reveal-up" style="--delay: 220ms">
        <view class="record-template-strip hc-shine">
          <text class="record-template-strip__label">发音模板</text>
          <text class="record-template-strip__text">“我叫xxx，我去黑龙江，他到无锡市，我爱北京天安门”</text>
          <text class="record-template-strip__meta">请按模板完整朗读，模型判断会更稳定。</text>
        </view>

        <view class="record-main__tips">
          <view class="tip-chip hc-interactive-pill">安静环境</view>
          <view class="tip-chip hc-interactive-pill">自然语速</view>
          <view class="tip-chip hc-interactive-pill">30-60秒</view>
        </view>

        <view class="record-button-wrap">
          <view class="record-button-halo"></view>
          <view
            class="record-button hc-interactive-pill"
            :class="{ recording: isRecording, paused: isPaused }"
            @touchstart.passive="startRecord"
            @touchend.passive="stopRecord"
            @touchcancel.passive="stopRecord"
          >
            <view class="record-icon">
              <uni-icons v-if="!isRecording && !isPaused" type="mic-filled" size="36" color="#ffffff" />
              <uni-icons v-else-if="isRecording" type="minus-filled" size="36" color="#ffffff" />
              <uni-icons v-else type="right" size="36" color="#ffffff" />
            </view>
            <text class="record-text">{{ recordButtonText }}</text>
          </view>
        </view>

        <view class="record-controls" v-if="hasRecorded">
          <view class="control-btn control-btn--soft hc-interactive-pill" @click="togglePlay">{{ playButtonText }}</view>
          <view class="control-btn control-btn--warn hc-interactive-pill" @click="deleteRecord">删除</view>
          <view class="control-btn control-btn--dark hc-interactive-pill" @click="uploadAudio">
            {{ isUploading ? "上传中..." : "开始检测" }}
          </view>
        </view>
      </view>

      <view class="bg-section-head audio-section-head hc-reveal-fade" style="--delay: 280ms">
        <text class="bg-section-head__title">文件上传</text>
        <text class="bg-section-head__meta">可直接上传已有音频</text>
      </view>
      <view class="hc-card-soft upload-card hc-interactive-card hc-reveal-up" style="--delay: 320ms">
        <view class="upload-progress" v-if="uploadProgress > 0 && uploadProgress < 100">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: uploadProgress + '%' }"></view>
          </view>
          <text class="progress-text">上传进度 {{ uploadProgress }}%</text>
        </view>

        <view class="file-upload-btn hc-interactive-pill" @click="chooseAudioFile">选择音频文件上传</view>
        <view class="file-info" v-if="selectedFile">
          <text>已选择：{{ selectedFile.name }}</text>
          <text class="file-size">大小：{{ formatFileSize(selectedFile.size) }}</text>
        </view>
      </view>

      <view class="bg-section-head audio-section-head hc-reveal-fade" style="--delay: 380ms">
        <text class="bg-section-head__title">录音提示</text>
        <text class="bg-section-head__meta">按步骤操作可提高检测稳定性</text>
      </view>
      <view class="guide-list-wrap hc-reveal-up" style="--delay: 420ms">
        <view class="guide-list">
          <view class="guide-item">
            <view class="guide-dot"></view>
            <text>开头停顿 1 秒，再完整朗读一句话。</text>
          </view>
          <view class="guide-item">
            <view class="guide-dot"></view>
            <text>手机距离口部约 20-30cm，避免贴得过近。</text>
          </view>
          <view class="guide-item">
            <view class="guide-dot"></view>
            <text>如环境噪声过大，建议重新录制后再上传。</text>
          </view>
        </view>
      </view>
    </view>
    <hc-dialog ref="dialogRef" />

    <footer-bar />
  </view>
</template>

<script setup>
import { computed, onUnmounted, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { useCommonStore } from "@/store";
import { GetLoginToken } from "@/utils/cache";
import HcDialog from "@/components/hc-dialog/hc-dialog.vue";

const commonStore = useCommonStore();
const baseUrl = import.meta.env.VITE_API_BASE_URL;

let recorderManager = null;
// #ifndef H5
recorderManager = uni.getRecorderManager();
// #endif
const audioContext = uni.createInnerAudioContext();

const isRecording = ref(false);
const isPaused = ref(false);
const isPlaying = ref(false);
const isPlayPaused = ref(false);
const hasRecorded = ref(false);
const duration = ref(0);
const playSeconds = ref(0);
const recordPath = ref("");
const selectedFile = ref(null);
const isUploading = ref(false);
const uploadProgress = ref(0);
const showBackButton = ref(false);
const dialogRef = ref(null);
const showDialog = (options) => dialogRef.value?.open(options) || Promise.resolve({ confirm: false, cancel: true });

let durationTimer = null;
let playProgressTimer = null;

onLoad(() => {
  updateBackState();
  if (!commonStore.Token) {
    commonStore.CheckIsLogin();
  }
});

onShow(() => {
  updateBackState();
});

const recordButtonText = computed(() => {
  if (isRecording.value) return "录音中...";
  if (isPaused.value) return "继续录音";
  return "按住录音";
});

const playButtonText = computed(() => {
  if (!recordPath.value) return "播放";
  if (isPlaying.value && !isPlayPaused.value) return "暂停";
  if (isPlaying.value && isPlayPaused.value) return "继续";
  return "播放";
});

const timeDisplay = computed(() => {
  if (!duration.value) return "00:00";
  if (isPlaying.value || isPlayPaused.value) {
    return `${formatDuration(playSeconds.value)} / ${formatDuration(duration.value)}`;
  }
  return formatDuration(duration.value);
});

const clearDurationTimer = () => {
  if (durationTimer) {
    clearInterval(durationTimer);
    durationTimer = null;
  }
};

const clearPlayProgressTimer = () => {
  if (playProgressTimer) {
    clearInterval(playProgressTimer);
    playProgressTimer = null;
  }
};

const startPlayProgressTimer = () => {
  clearPlayProgressTimer();
  playProgressTimer = setInterval(() => {
    const current = Number(audioContext.currentTime || 0);
    if (!Number.isFinite(current)) return;
    const nextSeconds = Math.floor(current);
    if (duration.value > 0) {
      playSeconds.value = Math.min(nextSeconds, duration.value);
      return;
    }
    playSeconds.value = nextSeconds;
  }, 200);
};

const startRecord = () => {
  if (isRecording.value) return;

  // #ifdef H5
  uni.showToast({
    title: "H5 不支持直接录音，请使用文件上传",
    icon: "none",
    duration: 2400,
  });
  return;
  // #endif

  const start = () => startRecording();
  if (typeof uni.authorize !== "function") {
    start();
    return;
  }

  uni.authorize({
    scope: "scope.record",
    success: start,
    fail: async () => {
      const result = await showDialog({
        title: "需要录音权限",
        content: "请在设置中开启录音权限后重试",
        confirmText: "去设置",
      });
      if (result.confirm && typeof uni.openSetting === "function") {
        uni.openSetting({});
      }
    },
  });
};

const startRecording = () => {
  if (!recorderManager) {
    uni.showToast({ title: "当前端不支持录音", icon: "none" });
    return;
  }

  isRecording.value = true;
  isPaused.value = false;
  duration.value = 0;
  playSeconds.value = 0;
  try {
    audioContext.stop();
  } catch (e) {}
  isPlaying.value = false;
  isPlayPaused.value = false;
  clearPlayProgressTimer();

  clearDurationTimer();
  durationTimer = setInterval(() => {
    duration.value += 1;
    if (duration.value >= 60) {
      stopRecord();
      uni.showToast({
        title: "录音最长 60 秒",
        icon: "none",
      });
    }
  }, 1000);

  recorderManager.start({
    duration: 60000,
    sampleRate: 44100,
    numberOfChannels: 1,
    encodeBitRate: 192000,
    format: "mp3",
    frameSize: 50,
  });

  recorderManager.onError((err) => {
    console.error("录音失败", err);
    isRecording.value = false;
    clearDurationTimer();
    uni.showToast({
      title: "录音失败，请重试",
      icon: "none",
    });
  });
};

const stopRecord = () => {
  if (!isRecording.value || !recorderManager) return;

  isRecording.value = false;
  isPaused.value = false;
  clearDurationTimer();

  recorderManager.stop();
  recorderManager.onStop((res) => {
    recordPath.value = res?.tempFilePath || "";
    if (!recordPath.value) return;
    hasRecorded.value = true;
    selectedFile.value = null;
    playSeconds.value = 0;
    audioContext.src = recordPath.value;
    if (duration.value < 1) {
      uni.showToast({
        title: "录音时长偏短，建议重录",
        icon: "none",
      });
    }
  });
};

const ensurePlayListeners = () => {
  audioContext.offPlay && audioContext.offPlay();
  audioContext.offPause && audioContext.offPause();
  audioContext.offStop && audioContext.offStop();
  audioContext.offEnded && audioContext.offEnded();
  audioContext.offError && audioContext.offError();

  audioContext.onPlay(() => {
    isPlaying.value = true;
    isPlayPaused.value = false;
    startPlayProgressTimer();
  });
  audioContext.onPause(() => {
    isPlaying.value = true;
    isPlayPaused.value = true;
    clearPlayProgressTimer();
  });
  audioContext.onStop(() => {
    isPlaying.value = false;
    isPlayPaused.value = false;
    clearPlayProgressTimer();
  });
  audioContext.onEnded(() => {
    isPlaying.value = false;
    isPlayPaused.value = false;
    playSeconds.value = duration.value > 0 ? duration.value : playSeconds.value;
    clearPlayProgressTimer();
  });
  audioContext.onError((err) => {
    console.error("播放失败", err);
    isPlaying.value = false;
    isPlayPaused.value = false;
    clearPlayProgressTimer();
    uni.showToast({
      title: "播放失败",
      icon: "none",
    });
  });
};

const togglePlay = () => {
  if (!recordPath.value) return;
  ensurePlayListeners();
  if (!isPlaying.value) {
    if (playSeconds.value >= duration.value && duration.value > 0) {
      playSeconds.value = 0;
    }
    audioContext.src = recordPath.value;
    audioContext.play();
    return;
  }
  if (!isPlayPaused.value) {
    audioContext.pause();
    return;
  }
  audioContext.play();
};

const deleteRecord = async () => {
  const result = await showDialog({
    title: "提示",
    content: "确认删除当前录音吗？",
  });
  if (!result.confirm) return;
  recordPath.value = "";
  hasRecorded.value = false;
  selectedFile.value = null;
  duration.value = 0;
  playSeconds.value = 0;
  isPlaying.value = false;
  isPlayPaused.value = false;
  clearPlayProgressTimer();
  try {
    audioContext.stop();
  } catch (e) {}
};

const chooseAudioFile = () => {
  // #ifdef MP-WEIXIN
  const chooser = uni.chooseMessageFile;
  const chooserOptions = {
    count: 1,
    type: "file",
    extension: ["mp3", "wav", "m4a", "flac"],
  };
  // #endif

  // #ifndef MP-WEIXIN
  const chooser = uni.chooseFile;
  const chooserOptions = {
    count: 1,
    extension: [".mp3", ".wav", ".m4a", ".flac"],
  };
  // #endif

  if (typeof chooser !== "function") {
    uni.showToast({
      title: "当前环境不支持文件选择",
      icon: "none",
    });
    return;
  }

  chooser({
    ...chooserOptions,
    success: (res) => {
      const file = (res.tempFiles && res.tempFiles[0]) || (res.tempFilePaths && res.tempFilePaths[0]) || null;
      if (!file) {
        uni.showToast({ title: "未选择文件", icon: "none" });
        return;
      }
      const size = file.size || file.fileSize || 0;
      if (size > 10 * 1024 * 1024) {
        uni.showToast({ title: "文件大小不能超过 10MB", icon: "none" });
        return;
      }

      const path = file.path || file.tempFilePath || (typeof file === "string" ? file : "");
      if (!path) {
        uni.showToast({ title: "读取文件失败", icon: "none" });
        return;
      }

      try {
        audioContext.stop();
      } catch (e) {}
      isPlaying.value = false;
      isPlayPaused.value = false;

      selectedFile.value = {
        name: file.name || "已选择音频文件",
        size,
        path,
      };
      recordPath.value = path;
      hasRecorded.value = true;
      playSeconds.value = 0;
      audioContext.src = recordPath.value;

      const tempAudio = uni.createInnerAudioContext();
      tempAudio.src = recordPath.value;
      tempAudio.onCanplay(() => {
        setTimeout(() => {
          const d = Number.isFinite(tempAudio.duration) ? tempAudio.duration : 0;
          duration.value = Math.floor(d || 0);
          tempAudio.destroy();
        }, 200);
      });
      tempAudio.onError(() => {
        tempAudio.destroy();
      });
    },
    fail: (err) => {
      console.error("选择文件失败", err);
      uni.showToast({
        title: "选择文件失败",
        icon: "none",
      });
    },
  });
};

const uploadAudio = async () => {
  if (!recordPath.value || isUploading.value) {
    if (!recordPath.value) {
      uni.showToast({ title: "请先录制或选择音频", icon: "none" });
    }
    return;
  }

  try {
    isUploading.value = true;
    uploadProgress.value = 5;
    uni.showLoading({
      title: "检测中...",
      mask: false,
    });

    const token = GetLoginToken();
    const header = {};
    if (token) {
      header.Authorization = `Bearer ${token}`;
    }

    if (!commonStore.UserId && commonStore.Token) {
      try {
        await commonStore.GetInfo();
      } catch (e) {
        console.error("获取用户信息失败", e);
      }
    }

    const resp = await new Promise((resolve, reject) => {
      const uploadTask = uni.uploadFile({
        url: `${baseUrl}/api/audio/detect`,
        filePath: recordPath.value,
        name: "file",
        formData: commonStore.UserId ? { userId: commonStore.UserId } : {},
        header,
        success: (res) => resolve(res),
        fail: (err) => reject(err),
      });

      uploadTask.onProgressUpdate &&
        uploadTask.onProgressUpdate((res) => {
          uploadProgress.value = res.progress || 0;
        });
    });

    uploadProgress.value = 100;

    let body = null;
    try {
      body = typeof resp.data === "string" ? JSON.parse(resp.data) : resp.data;
    } catch (e) {
      throw new Error("解析检测结果失败");
    }

    uni.hideLoading();

    let finalPayload = body;
    if (body && typeof body.success === "undefined" && body.Data) {
      finalPayload = body.Data;
    }

    if (!finalPayload || typeof finalPayload.success === "undefined") {
      throw new Error("检测服务返回格式异常");
    }

    const payload = encodeURIComponent(JSON.stringify(finalPayload));
    uni.navigateTo({
      url: `/pages/Front/AudioResult?payload=${payload}`,
    });
  } catch (error) {
    console.error("上传失败", error);
    uni.hideLoading();
    uni.showToast({
      title: error?.message || "检测失败，请重试",
      icon: "none",
      duration: 2000,
    });
  } finally {
    isUploading.value = false;
    setTimeout(() => {
      uploadProgress.value = 0;
    }, 1200);
  }
};

const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
};

const formatFileSize = (bytes) => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
};

const updateBackState = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  showBackButton.value = pages.length > 1;
};

const goBack = () => {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
    return;
  }
  uni.reLaunch({ url: "/pages/Front/Index" });
};

onUnmounted(() => {
  clearDurationTimer();
  clearPlayProgressTimer();
  try {
    audioContext.destroy();
  } catch (e) {}
  if (recorderManager && typeof recorderManager.stop === "function") {
    recorderManager.stop();
  }
});
</script>

<style scoped lang="scss">
.audio-record-page {
  min-height: 100vh;
}

.record-screen {
  gap: 36upx;
}

.page-back {
  align-self: flex-start;
  min-height: 64upx;
  padding: 0 20upx;
  border-radius: 999upx;
  background: rgba(248, 252, 239, 0.9);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  display: inline-flex;
  align-items: center;
  gap: 8upx;
  font-size: 22upx;
  font-weight: 800;
  color: #18211b;
}

.audio-head {
  padding: 2upx 6upx 0;
  display: flex;
  flex-direction: column;
  gap: 8upx;
}

.audio-head .bg-head__title {
  max-width: 640upx;
  font-size: 70upx;
  line-height: 1.08;
  letter-spacing: -0.8upx;
}

.audio-head .bg-head__subtitle {
  margin-top: 2upx;
}

.audio-head__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10upx;
}

.audio-head__pill {
  width: 148upx;
  height: 46upx;
  border-radius: 999upx;
  padding: 0 10upx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20upx;
  font-weight: 800;
  line-height: 1;
}

.audio-head__pill--light {
  background: rgba(248, 252, 239, 0.9);
  border: 1upx solid rgba(205, 224, 145, 0.92);
  color: #2a3b2b;
}

.record-status-pill {
  background: #161f18;
  border: 1upx solid #161f18;
  color: #effbd3;
}

.record-stats {
  margin-top: 40upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16upx;
}

.record-stat {
  min-height: 118upx;
  border-radius: 28upx;
  padding: 16upx 18upx;
  background: rgba(248, 252, 239, 0.48);
  border: 1upx solid rgba(205, 224, 145, 0.6);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.record-stat__value {
  font-size: 44upx;
  line-height: 1.1;
  font-weight: 800;
  color: #18231c;
}

.record-stat__label {
  margin-top: 8upx;
  font-size: 22upx;
  color: #60705f;
}

.record-template-strip {
  display: flex;
  flex-direction: column;
  gap: 10upx;
  padding: 16upx 16upx 14upx;
  border-radius: 24upx;
  background: rgba(247, 251, 235, 0.54);
  border: 1upx solid rgba(188, 215, 125, 0.52);
}

.record-template-strip__label {
  font-size: 22upx;
  font-weight: 800;
  color: #3f5d2d;
}

.record-template-strip__text {
  font-size: 24upx;
  line-height: 1.55;
  color: #1f2f22;
}

.record-template-strip__meta {
  font-size: 20upx;
  color: #5e6f5b;
}

.record-main__tips {
  margin-top: 16upx;
  display: flex;
  flex-wrap: wrap;
  gap: 12upx;
}

.record-main {
  margin-top: -20upx;
}

.tip-chip {
  padding: 8upx 16upx;
  border-radius: 999upx;
  background: rgba(248, 252, 239, 0.82);
  border: 1upx solid rgba(190, 214, 127, 0.72);
  font-size: 20upx;
  color: #2d3e2d;
}

.record-button-wrap {
  margin-top: 32upx;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.record-button-halo {
  position: absolute;
  width: 288upx;
  height: 288upx;
  border-radius: 999upx;
  background: radial-gradient(circle, rgba(150, 187, 93, 0.3) 0%, rgba(150, 187, 93, 0.08) 65%, rgba(150, 187, 93, 0) 100%);
  animation: haloBreathe 2.6s ease-in-out infinite;
}

.record-button {
  width: 248upx;
  height: 248upx;
  border-radius: 999upx;
  background: var(--gradient-contrast);
  border: 4upx solid rgba(248, 252, 239, 0.92);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 18upx 40upx rgba(18, 24, 19, 0.28);
  position: relative;
  z-index: 1;
  transition: transform 0.2s ease;
}

.record-button.recording {
  background: linear-gradient(135deg, #cb6a58 0%, #e18474 100%);
  animation: pulse 1.3s ease-in-out infinite;
}

.record-button.paused {
  background: linear-gradient(135deg, #c39a53 0%, #d8af63 100%);
}

.record-icon {
  margin-bottom: 8upx;
}

.record-text {
  color: #ffffff;
  font-size: 24upx;
  font-weight: 700;
}

.record-controls {
  margin-top: 26upx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10upx;
}

.control-btn {
  min-height: 74upx;
  border-radius: 999upx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24upx;
  font-weight: 700;
}

.control-btn--soft {
  background: rgba(248, 252, 239, 0.88);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  color: #223127;
}

.control-btn--warn {
  background: rgba(243, 188, 171, 0.82);
  border: 1upx solid rgba(220, 129, 105, 0.75);
  color: #5a241b;
}

.control-btn--dark {
  background: var(--gradient-contrast);
  border: 1upx solid rgba(30, 41, 33, 0.88);
  color: #ffffff;
}

.audio-section-head {
  margin-top: 14upx;
}

.upload-progress {
  margin-top: 2upx;
  background: #f8fcee;
  border-radius: 22upx;
  padding: 14upx;
  border: 1upx solid #d5e1c0;
}

.progress-bar {
  width: 100%;
  height: 12upx;
  background: #d7e0c8;
  border-radius: 999upx;
  overflow: hidden;
  margin-bottom: 8upx;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-brand);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: var(--type-meta);
  color: #59665d;
  text-align: center;
  display: block;
}

.file-upload-btn {
  margin-top: 16upx;
  min-height: 88upx;
  border-radius: 999upx;
  background: rgba(240, 246, 229, 0.72);
  border: 1upx dashed #7f9d58;
  color: #2f442f;
  font-size: 26upx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-info {
  margin-top: 12upx;
  font-size: var(--type-meta);
  color: #59665d;
  display: flex;
  flex-direction: column;
  gap: 4upx;
}

.file-size {
  color: #8f9b91;
}

.guide-list-wrap {
  padding: 4upx 8upx 8upx;
}

.guide-list {
  display: flex;
  flex-direction: column;
  gap: 10upx;
}

.guide-item {
  display: flex;
  align-items: center;
  gap: 10upx;
  font-size: 22upx;
  color: #566655;
}

.guide-dot {
  width: 12upx;
  height: 12upx;
  border-radius: 999upx;
  background: #85ad51;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.04);
  }
}

@keyframes haloBreathe {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.08);
    opacity: 0.7;
  }
}
</style>
