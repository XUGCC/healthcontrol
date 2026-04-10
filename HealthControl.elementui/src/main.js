import ElementPlus from "element-plus";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import "element-plus/dist/index.css";
import "./styles/index.scss";

// 引入ElementPlus中文语言包
import zhCn from "element-plus/dist/locale/zh-cn.mjs";
// 引入Pinia
import { createPinia } from "pinia";
import { createPersistedState } from "pinia-plugin-persistedstate";
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

// 创建应用实例
const app = createApp(App);
// 关闭开发环境下的警告信息
app.config.warnHandler = (msg, vm, trace) => {};
// 注册路由
app.use(router);

// 注册状态管理
const pinia = createPinia();
pinia.use(createPersistedState());
app.use(pinia);

// 注册ElementPlus并配置中文
app.use(ElementPlus, {
  locale: zhCn,
  // 自动注册所有组件
  autoImport: true,
  // 全局配置Message组件
  message: {
    duration: 1000, // 消息提示显示时长（毫秒），默认1000ms，
  },
});


registerComponents(app);



app.mount("#app");


function registerComponents(app) {
  //注册@components下的所有组件
  const components = import.meta.glob("./components/**/*.vue", { eager: true });
  Object.entries(components).forEach(([path, module]) => {
    const componentName = path
      .split("/")
      .pop()
      .replace(/\.\w+$/, "");
    app.component(componentName, module.default);
  });
  // 注册ElementPlusIconsVue
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
  }
}
