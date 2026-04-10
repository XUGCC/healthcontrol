import { computed, ref } from "vue";

const activeGlobalPopupCount = ref(0);

export const notifyGlobalPopupChange = (show) => {
  activeGlobalPopupCount.value = Math.max(0, activeGlobalPopupCount.value + (show ? 1 : -1));
};

export const useGlobalPopupVisibility = () => ({
  activeGlobalPopupCount,
  isPopupOpen: computed(() => activeGlobalPopupCount.value > 0),
});

export const usePopupVisibility = () => {
  const activePopupCount = ref(0);

  const onPopupChange = (input) => {
    const show = typeof input === "boolean" ? input : !!input?.show;
    activePopupCount.value = Math.max(0, activePopupCount.value + (show ? 1 : -1));
  };

  const resetPopupVisibility = () => {
    activePopupCount.value = 0;
  };

  return {
    activePopupCount,
    isPopupOpen: computed(() => activePopupCount.value > 0),
    onPopupChange,
    resetPopupVisibility,
  };
};
