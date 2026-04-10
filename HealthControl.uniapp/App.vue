<script setup>
import { useCommonStore } from "@/store";
import { onHide, onLaunch, onShow } from "@dcloudio/uni-app";

const commonStore = useCommonStore();

onLaunch(async () => {
  if (commonStore.Token) {
    await commonStore.GetInfo();
  }
});

onShow(() => {});
onHide(() => {});
</script>

<style lang="scss">
page {
  --hc-popup-z-index: 99999;
  --color-white: #ffffff;
  --color-ink-950: #121815;
  --color-ink-900: #18211b;
  --color-ink-800: #223028;

  --brand-950: #1d281e;
  --brand-900: #2a392b;
  --brand-800: #365137;
  --brand-700: #4d7641;
  --brand-600: #6d9a4a;
  --brand-500: #8ec94f;
  --brand-400: #b5ea63;
  --brand-300: #cff28f;
  --brand-200: #e6f8bb;
  --brand-100: #f1f9d6;
  --brand-50: #f7fbe9;

  --accent-olive-600: #647f42;
  --accent-olive-500: #81a852;
  --accent-olive-100: #eef6d8;
  --accent-gold-500: #f3b641;
  --accent-coral-500: #ea7551;
  --accent-sky-400: #8bc7d7;

  --primary-color: var(--brand-600);
  --success-color: #86bd42;
  --warning-color: #efb34d;
  --error-color: #e46c49;
  --info-color: #84968a;

  --text-color: #172119;
  --text-color-light: #586858;
  --text-color-lighter: #809081;
  --text-color-placeholder: #a8b4a8;
  --text-color-disabled: #c6d0c8;
  --text-color-inverse: #ffffff;

  --bg-color: #f6fbe8;
  --bg-color-grey: #eef6df;
  --bg-color-hover: #e7f2d1;
  --bg-color-mask: rgba(14, 18, 15, 0.52);
  --bg-page-gradient: linear-gradient(180deg, #edf8ca 0%, #f4fadf 18%, #f8fceb 100%);
  --bg-card: rgba(249, 252, 239, 0.94);
  --bg-card-soft: rgba(243, 248, 230, 0.94);
  --bg-card-contrast: #151d17;
  --bg-chip: #e1f39a;

  --border-color: #d5e3b4;
  --border-color-light: #dfeac4;
  --border-color-lighter: #ebf3da;
  --border-brand: #cfe08f;
  --border-ai: #d6e8b7;

  --gradient-brand: linear-gradient(135deg, #83bd49 0%, #b4ea61 100%);
  --gradient-brand-strong: linear-gradient(135deg, #75ae40 0%, #9fd84e 52%, #c2f26f 100%);
  --gradient-ai: linear-gradient(135deg, #18211b 0%, #253329 100%);
  --gradient-contrast: linear-gradient(135deg, #101612 0%, #1d271f 100%);
  --gradient-warm: linear-gradient(135deg, #a7ca57 0%, #d7ef8e 100%);

  --font-size-xs: 20upx;
  --font-size-sm: 24upx;
  --font-size-base: 28upx;
  --font-size-md: 32upx;
  --font-size-lg: 36upx;
  --font-size-xl: 42upx;

  --type-display: 68upx;
  --type-h1: 54upx;
  --type-h2: 40upx;
  --type-h3: 30upx;
  --type-body: 24upx;
  --type-meta: 20upx;

  --border-radius-sm: 16upx;
  --border-radius-base: 24upx;
  --border-radius-lg: 34upx;
  --border-radius-xl: 46upx;
  --border-radius-2xl: 58upx;
  --border-radius-circle: 50%;
  --border-radius-pill: 9999upx;

  --spacing-xs: 10upx;
  --spacing-sm: 20upx;
  --spacing-base: 30upx;
  --spacing-md: 40upx;
  --spacing-lg: 52upx;
  --spacing-xl: 64upx;

  --box-shadow-sm: 0 8upx 22upx rgba(32, 41, 28, 0.08);
  --box-shadow-base: 0 16upx 36upx rgba(32, 41, 28, 0.12);
  --box-shadow-lg: 0 28upx 62upx rgba(15, 20, 16, 0.2);
  --box-shadow-hero: 0 32upx 72upx rgba(39, 56, 28, 0.18);

  --transition-base: all 0.3s ease;
  --transition-fast: all 0.2s ease;
  --transition-slow: all 0.4s ease;
  --opacity-medium: 0.7;

  color: var(--text-color);
  background: var(--bg-page-gradient);
  font-family: "HarmonyOS Sans SC", "PingFang SC", "Source Han Sans SC", sans-serif;
}

$uni-tabbar-height: 100upx;
$uni-img-size-base: 60upx;
$uni-font-size-base: 28upx;
$uni-font-size-md: 32upx;

view,
text,
navigator,
button,
input,
textarea,
label,
form,
scroll-view,
swiper,
swiper-item {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

button {
  border-radius: var(--border-radius-pill);
  border: 1upx solid var(--border-brand);
  background: var(--bg-card-soft);
  color: var(--text-color);
  font-weight: 700;
}

input,
textarea {
  border-radius: var(--border-radius-lg);
  border: 1upx solid var(--border-brand);
  background: rgba(248, 252, 239, 0.92);
  color: var(--text-color);
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-ellipsis-2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.hc-mobile-shell {
  min-height: 100vh;
  position: relative;
  isolation: isolate;
  overflow: hidden;
  background: var(--bg-page-gradient);
}

.hc-mobile-shell::before,
.hc-mobile-shell::after {
  content: "";
  position: fixed;
  border-radius: 9999upx;
  pointer-events: none;
  z-index: -1;
  filter: blur(16upx);
}

.hc-mobile-shell::before {
  width: 320upx;
  height: 320upx;
  top: 84upx;
  right: -76upx;
  background: rgba(180, 236, 100, 0.42);
}

.hc-mobile-shell::after {
  width: 260upx;
  height: 260upx;
  left: -70upx;
  bottom: 180upx;
  background: rgba(140, 198, 216, 0.18);
}

.hc-screen {
  position: relative;
  padding: calc(env(safe-area-inset-top) + 18upx) 28upx calc(env(safe-area-inset-bottom) + 160upx);
}

.hc-stack {
  display: flex;
  flex-direction: column;
  gap: 22upx;
}

.hc-row {
  display: flex;
  align-items: center;
}

.hc-space-between {
  justify-content: space-between;
}

.hc-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18upx;
}

.hc-card-soft,
.hc-card-lime,
.hc-card-dark {
  position: relative;
  overflow: visible;
  border-radius: var(--border-radius-2xl);
  padding: 28upx;
}

.hc-card-soft {
  background: var(--bg-card);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  box-shadow: var(--box-shadow-base);
}

.hc-card-lime {
  background: linear-gradient(145deg, #dbf29f 0%, #c8eb79 48%, #ebf8bf 100%);
  border: 1upx solid rgba(156, 195, 77, 0.56);
  box-shadow: var(--box-shadow-hero);
}

.hc-card-dark {
  background: var(--gradient-contrast);
  border: 1upx solid rgba(38, 56, 43, 0.9);
  box-shadow: var(--box-shadow-lg);
  color: #ffffff;
}

.hc-kicker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44upx;
  padding: 0 18upx;
  border-radius: var(--border-radius-pill);
  background: rgba(248, 252, 239, 0.92);
  border: 1upx solid rgba(203, 220, 150, 0.9);
  color: #2a3a2b;
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 1upx;
}

.hc-kicker--dark {
  background: rgba(225, 243, 154, 0.18);
  border-color: rgba(183, 232, 99, 0.24);
  color: #d7f494;
}

.hc-pill-button,
.hc-pill-button-dark,
.hc-pill-button-soft {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 86upx;
  padding: 0 30upx;
  border-radius: var(--border-radius-pill);
  font-size: 28upx;
  font-weight: 800;
  letter-spacing: 0.4upx;
}

.hc-pill-button {
  background: var(--gradient-brand-strong);
  color: #152012;
  border: 1upx solid rgba(145, 196, 65, 0.48);
  box-shadow: 0 16upx 32upx rgba(106, 150, 50, 0.24);
}

.hc-pill-button-dark {
  background: var(--gradient-contrast);
  color: #ffffff;
  border: 1upx solid rgba(30, 41, 33, 0.88);
  box-shadow: 0 16upx 32upx rgba(17, 23, 18, 0.24);
}

.hc-pill-button-soft {
  background: rgba(248, 252, 239, 0.84);
  color: #243123;
  border: 1upx solid rgba(205, 224, 145, 0.9);
}

.hc-token-tabs {
  display: flex;
  gap: 12upx;
  flex-wrap: wrap;
}

.hc-token-tabs__item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 62upx;
  padding: 0 24upx;
  border-radius: var(--border-radius-pill);
  background: rgba(248, 252, 239, 0.74);
  border: 1upx solid rgba(205, 224, 145, 0.86);
  color: #53624f;
  font-size: 24upx;
  font-weight: 700;
}

.hc-token-tabs__item.active {
  background: #151d17;
  color: #f4fbdd;
  border-color: #151d17;
}

.hc-official-select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16upx;
  width: 100%;
  min-height: 86upx;
  padding: 0 22upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.88);
}

.hc-official-select-trigger.is-placeholder .hc-official-select-trigger__value {
  color: var(--text-color-placeholder);
}

.hc-official-select-trigger__value {
  flex: 1;
  min-width: 0;
  font-size: 24upx;
  font-weight: 700;
  color: var(--text-color);
}

.hc-official-popup-card {
  width: min(670upx, calc(100vw - 56upx));
  max-height: calc(100vh - env(safe-area-inset-top) - env(safe-area-inset-bottom) - 80upx);
  overflow: hidden;
  padding: 26upx 24upx calc(env(safe-area-inset-bottom) + 28upx);
  border-radius: 42upx;
  background: linear-gradient(180deg, rgba(248, 252, 239, 0.99) 0%, rgba(239, 247, 224, 0.99) 100%);
  border: 1upx solid rgba(205, 224, 145, 0.9);
  box-shadow: 0 26upx 64upx rgba(18, 24, 20, 0.22);
}

.hc-official-popup-handle {
  width: 88upx;
  height: 8upx;
  margin: 0 auto 22upx;
  border-radius: 999upx;
  background: rgba(111, 127, 77, 0.28);
}

.hc-official-popup-head {
  display: flex;
  flex-direction: column;
  gap: 10upx;
  text-align: center;
}

.hc-official-popup-eyebrow {
  font-size: 20upx;
  font-weight: 800;
  letter-spacing: 2upx;
  color: #6f835f;
}

.hc-official-popup-title {
  display: block;
  font-size: 34upx;
  font-weight: 800;
  color: #172119;
}

.hc-official-popup-subtitle {
  display: block;
  font-size: 22upx;
  line-height: 1.65;
  color: #60705d;
}

.hc-official-popup-summary {
  margin-top: 18upx;
  padding: 18upx 20upx;
  border-radius: 28upx;
  background: rgba(248, 252, 239, 0.9);
  border: 1upx solid rgba(205, 224, 145, 0.88);
}

.hc-official-popup-summary__label {
  display: block;
  font-size: 20upx;
  color: #778874;
}

.hc-official-popup-summary__value {
  display: block;
  margin-top: 8upx;
  font-size: 28upx;
  font-weight: 800;
  color: #172119;
}

.hc-official-popup-list {
  margin-top: 18upx;
  max-height: 560upx;
}

.hc-official-popup-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12upx;
  min-height: 92upx;
  padding: 0 22upx;
  border-radius: 30upx;
  background: rgba(248, 252, 239, 0.84);
  border: 1upx solid rgba(205, 224, 145, 0.88);
}

.hc-official-popup-option + .hc-official-popup-option {
  margin-top: 12upx;
}

.hc-official-popup-option.active {
  background: linear-gradient(145deg, #dbf29f 0%, #c8eb79 48%, #ebf8bf 100%);
  border-color: rgba(156, 195, 77, 0.58);
  box-shadow: var(--box-shadow-base);
}

.hc-official-popup-option__text {
  flex: 1;
  min-width: 0;
  font-size: 26upx;
  font-weight: 700;
  color: #172119;
}

.hc-official-popup-option__mark {
  font-size: 22upx;
  font-weight: 800;
  color: #647f42;
}

.hc-official-popup-actions {
  margin-top: 18upx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14upx;
}

.hc-stat-value {
  display: block;
  font-size: 54upx;
  line-height: 1;
  font-weight: 800;
  color: var(--text-color);
}

.hc-stat-value--dark {
  color: #ffffff;
}

.hc-stat-label {
  display: block;
  margin-top: 8upx;
  font-size: 20upx;
  color: var(--text-color-light);
}

.hc-progress {
  width: 100%;
  height: 16upx;
  border-radius: 9999upx;
  background: rgba(29, 39, 31, 0.08);
  overflow: hidden;
}

.hc-progress__bar {
  height: 100%;
  border-radius: inherit;
  background: var(--gradient-brand-strong);
}

.hc-progress__bar--dark {
  background: linear-gradient(135deg, #d9f98a 0%, #96d641 100%);
}

.hc-fab {
  position: fixed;
  left: 28upx;
  right: 28upx;
  bottom: calc(env(safe-area-inset-bottom) + 104upx);
  z-index: 10;
}

.hc-section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18upx;
}

.hc-section-head__title {
  font-size: 34upx;
  font-weight: 800;
  color: var(--text-color);
}

.hc-section-head__meta {
  font-size: 20upx;
  color: var(--text-color-lighter);
}

.bg-head {
  display: flex;
  flex-direction: column;
  gap: 8upx;
  padding: 2upx 4upx;
}

.bg-head__title {
  font-size: 54upx;
  line-height: 1.08;
  font-weight: 800;
  color: var(--text-color);
}

.bg-head__subtitle {
  font-size: 22upx;
  line-height: 1.55;
  color: var(--text-color-light);
}

.bg-note {
  display: block;
  padding: 0 6upx;
  font-size: 22upx;
  line-height: 1.65;
  color: var(--text-color-light);
}

.bg-section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14upx;
  padding: 0 6upx;
}

.bg-section-head__title {
  font-size: 38upx;
  font-weight: 800;
  line-height: 1.18;
  color: var(--text-color);
}

.bg-section-head__meta {
  font-size: 22upx;
  color: var(--text-color-lighter);
}

.page-container,
.hc-page {
  min-height: 100vh;
  background: var(--bg-page-gradient);
}

.content-container,
.hc-content {
  padding: 72upx 32upx 136upx;
}

.card,
.hc-card {
  background-color: var(--bg-card);
  border: 1upx solid var(--border-brand);
  border-radius: var(--border-radius-xl);
  padding: var(--spacing-base);
  box-shadow: var(--box-shadow-base);
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22upx 34upx;
  font-size: var(--font-size-base);
  border-radius: var(--border-radius-pill);
  transition: var(--transition-base);
  font-weight: 700;
}

.btn-primary {
  background: var(--gradient-brand);
  color: #ffffff;
}

.btn-success {
  background-color: var(--success-color);
  color: #ffffff;
}

.btn-warning {
  background-color: var(--warning-color);
  color: #ffffff;
}

.btn-error {
  background-color: var(--error-color);
  color: #ffffff;
}

.ui-title {
  font-size: var(--type-h1);
  font-weight: 800;
  line-height: 1.2;
  color: var(--text-color);
}

.ui-sub {
  font-size: var(--type-body);
  font-weight: 500;
  line-height: 1.6;
  color: var(--text-color-light);
}

.ui-meta {
  font-size: var(--type-meta);
  font-weight: 500;
  line-height: 1.55;
  color: var(--text-color-lighter);
}

.hc-reveal-up {
  opacity: 0;
  transform: none;
  animation: hcRevealUp 520ms cubic-bezier(0.2, 0.7, 0.2, 1) both;
  animation-delay: var(--delay, 0ms);
}

.hc-reveal-fade {
  opacity: 1;
  animation: hcRevealFade 460ms ease both;
  animation-delay: var(--delay, 0ms);
}

.hc-interactive-card {
  transition:
    transform 0.22s ease,
    box-shadow 0.24s ease,
    filter 0.24s ease;
}

.hc-interactive-card:active {
  transform: translateY(2upx) scale(0.988);
  filter: brightness(0.98);
}

.hc-interactive-pill {
  transition: transform 0.2s ease, filter 0.2s ease;
}

.hc-interactive-pill:active {
  transform: scale(0.97);
  filter: brightness(0.96);
}

.hc-shine {
  position: relative;
  overflow: hidden;
}

.hc-shine::after {
  content: "";
  position: absolute;
  top: -140%;
  bottom: -140%;
  left: -55%;
  width: 34%;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.16) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: translateX(-220%) rotate(12deg);
  animation: hcShine 5.2s ease-in-out infinite;
  pointer-events: none;
}

.hc-icon-breathe {
  animation: hcIconBreathe 2.3s ease-in-out infinite;
}

.hc-icon-ping {
  animation: hcPing 1.8s ease-in-out infinite;
}

@keyframes hcFloat {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8upx);
  }
}

@keyframes hcPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.03);
  }
}

@keyframes hcRevealUp {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@keyframes hcRevealFade {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@keyframes hcShine {
  0%,
  62% {
    transform: translateX(-220%) rotate(12deg);
  }
  100% {
    transform: translateX(500%) rotate(12deg);
  }
}

@keyframes hcIconBreathe {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.06);
  }
}

@keyframes hcPing {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(0.92);
    opacity: 0.84;
  }
}

.uni-easyinput__content {
  min-height: 92upx !important;
  border-radius: 30upx !important;
  border: 1upx solid rgba(205, 224, 145, 0.9) !important;
  background: rgba(248, 252, 239, 0.92) !important;
  box-shadow: none !important;
}

.uni-easyinput__content.is-focused {
  border-color: rgba(156, 195, 77, 0.72) !important;
  box-shadow: 0 0 0 6upx rgba(193, 232, 109, 0.12) !important;
}

.uni-easyinput__content-input,
.uni-easyinput__content-textarea {
  color: var(--text-color) !important;
  font-size: 24upx !important;
  line-height: 1.65 !important;
  background: transparent !important;
}

.uni-easyinput__content-textarea {
  min-height: 200upx !important;
}

.uni-easyinput__placeholder-class {
  color: #a3af9f !important;
}

.uni-easyinput .content-clear-icon {
  color: #73816f !important;
}

.uni-forms-item__label {
  font-size: 24upx !important;
  font-weight: 800 !important;
  color: var(--text-color) !important;
}

.uni-forms-item__error-msg,
.uni-forms-item__error {
  color: #c86143 !important;
  font-size: 20upx !important;
}

.uni-data-checklist .checklist-group {
  display: flex !important;
  flex-wrap: wrap !important;
  gap: 12upx !important;
}

.uni-data-checklist .checklist-box {
  min-height: 62upx !important;
  padding: 0 24upx !important;
  border-radius: 999upx !important;
  border: 1upx solid rgba(205, 224, 145, 0.9) !important;
  background: rgba(248, 252, 239, 0.84) !important;
}

.uni-data-checklist .radio__inner,
.uni-data-checklist .checkbox__inner {
  display: none !important;
}

.uni-data-checklist .checklist-text {
  color: #53624f !important;
  font-size: 24upx !important;
  font-weight: 700 !important;
}

.uni-data-checklist .checklist-box.is-checked {
  border-color: #151d17 !important;
  background: #151d17 !important;
}

.uni-data-checklist .checklist-box.is-checked .checklist-text {
  color: #f4fbdd !important;
}

.uni-numbox {
  align-items: stretch !important;
}

.uni-numbox-btns {
  min-width: 72upx;
  border-radius: 26upx !important;
  border: 1upx solid rgba(205, 224, 145, 0.88);
}

.uni-numbox__value {
  min-width: 108upx !important;
  height: auto !important;
  margin: 0 8upx !important;
  border-radius: 26upx !important;
  border: 1upx solid rgba(205, 224, 145, 0.88) !important;
  font-size: 24upx !important;
  font-weight: 800 !important;
}

.uni-calendar__mask,
.uni-date-mask--pc {
  background: rgba(11, 17, 13, 0.5) !important;
}

.uni-popup,
.uni-popup.fixforpc-z-index {
  z-index: var(--hc-popup-z-index) !important;
}

.uni-calendar--fixed,
.uni-date-picker__container {
  z-index: var(--hc-popup-z-index) !important;
}

.uni-calendar__content,
.uni-date-single--x,
.uni-date-range--x {
  background: linear-gradient(180deg, rgba(248, 252, 239, 0.99) 0%, rgba(239, 247, 224, 0.99) 100%) !important;
  border-radius: 36upx 36upx 0 0 !important;
  border: 1upx solid rgba(205, 224, 145, 0.9) !important;
  box-shadow: 0 26upx 64upx rgba(18, 24, 20, 0.22) !important;
}

.uni-date-editor--x,
.uni-date-x--border,
.uni-date__input {
  border-radius: 30upx !important;
  border-color: rgba(205, 224, 145, 0.9) !important;
  background: rgba(248, 252, 239, 0.92) !important;
  color: #172119 !important;
}

.uni-date__x-input,
.range-separator,
.uni-calendar__header-text,
.uni-calendar__backtoday,
.popup-x-footer text {
  color: #172119 !important;
}

.popup-x-footer .confirm-text {
  color: #5a8038 !important;
}

.popup-x-body {
  border-top-color: rgba(205, 224, 145, 0.7) !important;
}

.uni-calendar__header,
.uni-calendar--fixed-top,
.uni-calendar__weeks-day {
  border-color: rgba(223, 234, 196, 0.9) !important;
}

.uni-calendar__weeks-day-text,
.uni-calendar-item__weeks-box-text,
.uni-calendar-item__weeks-lunar-text {
  color: #172119 !important;
}

.uni-calendar__backtoday {
  background: rgba(223, 242, 168, 0.7) !important;
}

.uni-calendar-item--checked,
.uni-calendar-item--multiple,
.uni-calendar-item--after-checked,
.uni-calendar-item--before-checked,
.uni-calendar-item--isDay {
  background: linear-gradient(145deg, #dbf29f 0%, #c8eb79 48%, #ebf8bf 100%) !important;
  border-radius: 22upx !important;
}

.uni-calendar-item--disable {
  opacity: 0.34 !important;
}

@media (prefers-reduced-motion: reduce) {
  .hc-reveal-up,
  .hc-reveal-fade,
  .hc-icon-breathe,
  .hc-icon-ping,
  .hc-shine::after {
    animation: none !important;
    transform: none !important;
    opacity: 1 !important;
  }
}
</style>
