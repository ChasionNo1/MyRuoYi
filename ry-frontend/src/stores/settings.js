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

const { sideTheme, showSettings, topNav, tagsView, fixedHeader, sidebarLogo, dynamicTitle } = defaultSettings

const useSettingsStore = defineStore(
  "settings",
  {
    state: () => ({
      // 应用的标题（通常显示在浏览器标签页或顶部导航栏）
      title: '',
      // 应用的主色调（全局主题色）。
      theme: '#409EFF',
      // 侧边栏的主题风格（深色或浅色）。
      sideTheme: sideTheme,
      // 是否显示右上角的「设置面板」（用于切换主题、布局等）。
      showSettings: showSettings,
      // 是否启用顶部导航栏布局（而非侧边栏主导航）。
      topNav: topNav,
      // 是否启用标签页视图（多页签功能）。
      tagsView: tagsView,
      // 是否固定顶部导航栏（滚动时保持顶部栏可见）。
      fixedHeader: fixedHeader,
      // 是否在侧边栏显示 logo 图标。
      sidebarLogo: sidebarLogo,
      // 是否启用动态标题功能（根据路由名称自动拼接页面标题）。
      dynamicTitle: dynamicTitle,
      // 是否启用暗黑模式（全局深色主题）。
      isDark: isDark.value
    }),
    actions: {
      // 修改布局设置
      changeSetting(data) {
        // console.log("data", data);
        const { key, value } = data;
        // console.log(key);
        // console.log(value);
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
