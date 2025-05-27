/**
 * 后台管理系统设置存储模块分析
该代码是基于 Pinia 的设置存储模块，用于管理系统的主题、布局、用户偏好等配置，
结合 vueuse 实现暗黑模式切换，并通过 localStorage 实现持久化存储。
 * 
 */
import { defineStore } from "pinia";
import defaultSettings from "@/settings";
import { useDark, useToggle } from "@vueuse/core";
import { useDynamicTitle } from "@/utils/dynamicTitle";

const isDark = useDark();
const toggleDark = useToggle(isDark);

const {
  sideStatus: defaultSideStatus,
  sideTheme: defaultSideTheme,
  sidebarLogo: defaultSidebarLogo,
} = defaultSettings;

const useSettingsStore = defineStore(
  "settings",
  {
    state: () => ({
      sideStatus: defaultSideStatus,
      sideTheme: defaultSideTheme,
      sidebarLogo: defaultSidebarLogo,
      isDark: isDark.value
    }),
    actions: {
      // 修改布局设置
      changeSetting(data) {
        console.log("data", data);
        const { key, value } = data;
        console.log(key);
        console.log(value);
        if (key in this.$state) {
          this[key] = value;
        }
      },
        // 设置网页标题
        setTitle(title) {
          this.title = title;
          useDynamicTitle();
        },
        // 切换暗黑模式
        toggleTheme() {
          this.isDark = !this.isDark;
          toggleDark();
        },
    },
  },
  {
    persist: {
      key: "settings-store",
      storage: localStorage,
      paths: [
        "sideStatus",
        "sideTheme",
        "sidebarLogo"
      ],
      afterRestore: (ctx) => {
        // 持久化状态恢复后同步暗黑模式
        toggleDark(ctx.store.isDark);
      },
    },
  }
);
export default useSettingsStore;
