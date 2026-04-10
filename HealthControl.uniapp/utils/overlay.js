let overlayZIndexSeed = 100000;

export const nextOverlayZIndex = () => {
  overlayZIndexSeed += 2;
  return overlayZIndexSeed;
};
