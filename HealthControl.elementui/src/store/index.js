import { Post } from "@/api/http.js";

import { defineStore } from "pinia";

//  状态管理
export const useCommonStore = defineStore("common", {
  state: () => ({
    // 初始状态
    Token: null,
    UserInfo: null,
    RoleType: undefined,
    HasUserInfo: false,
  }),
  getters: {
    UserId: (state) => state.UserInfo && state.UserInfo.Id,
  },
  actions: {
    async Login(UserInfo) {
      let res = await Post("/User/SignIn", UserInfo);

      if (res.Success) {
        this.$patch({ Token: res.Data });
      }
 
      return res;
    },
    async GetInfo() {
      let res = await Post("/User/GetByToken", {});

      this.$patch({
        UserInfo: res.Data,
        HasUserInfo: res.Data != null,
        RoleType: res.Data.RoleTypeFormat,
      });
      return res;
    },
    async Logout() {
      this.$patch({
        Token: null,
        UserInfo: null,
        RoleType: null,
        HasUserInfo: false,
      });
    },
    resetState() {
      this.$patch({
        Token: null,
        UserInfo: null,
        RoleType: undefined,
        HasUserInfo: false,
      });
    },
  },

  persist: {
    key: "common-store",
    storage: localStorage,
    paths: ["Token"],
  },
});

