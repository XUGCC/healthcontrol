import { Post } from "@/utils/http";
import { defineStore } from "pinia";
import { GetLoginToken, RemoveLoginToken, SetLoginToken } from "@/utils/cache";

export const useCommonStore = defineStore("common", {
  state: () => ({
    Token: GetLoginToken(),
    UserInfo: null,
    RoleType: undefined,
    HasUserInfo: false,
    FooterBarList: [
      {
        url: "/pages/Front/Index",
        label: "\u9996\u9875",
        iconType: "home",
      },
      {
        url: "/pages/Front/AudioRecord",
        label: "\u81ea\u67e5",
        iconType: "mic",
      },
      {
        url: "/pages/Front/ServiceHub",
        label: "\u670d\u52a1",
        iconType: "bars",
      },
      {
        url: "/pages/Front/UserCenter",
        label: "\u6211\u7684",
        iconType: "person",
      },
    ],
  }),
  getters: {
    UserId: (state) => state.UserInfo && state.UserInfo.Id,
  },
  actions: {
    async Login(userInfo) {
      const res = await Post("/User/SignIn", userInfo);
      if (res.Success) {
        this.$patch({
          Token: res.Data,
        });
        SetLoginToken(res.Data);
      }
      return res;
    },
    async GetInfo() {
      const res = await Post("/User/GetByToken", {});
      this.$patch({
        UserInfo: res.Data,
        HasUserInfo: res.Data != null,
        RoleType: res.Data?.RoleTypeFormat,
      });
      return res;
    },
    async Logout() {
      RemoveLoginToken();
      uni.reLaunch({ url: "/pages/Front/Login" });
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
      RemoveLoginToken();
    },
    CheckIsLogin() {
      if (!this.Token) {
        uni.reLaunch({ url: "/pages/Front/Login" });
        return false;
      }
      return true;
    },
  },
});
