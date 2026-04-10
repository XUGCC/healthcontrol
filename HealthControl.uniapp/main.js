import * as Pinia from "pinia";
import App from "./App";

import { createSSRApp } from "vue";

const app = createSSRApp(App);
app.config.warnHandler = (msg, vm, trace) => {};
app.use(Pinia.createPinia());


export function createApp() {
  return {
    app,
    Pinia, // 此处必须将 Pinia
  };
}
