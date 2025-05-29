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
import { ref } from "vue";

const isDark = useDark();
const toggleDark = useToggle(isDark);


const useSettingsStore = defineStore("settings", () => {
  // 直接使用 defaultSettings 初始化 ref，避免解构未使用的变量
  const title = ref('');
  const theme = ref('#409EFF');
  const sideTheme = ref(defaultSettings.sideTheme); // 直接引用 defaultSettings 属性
  const showSettings = ref(defaultSettings.showSettings);
  const topNav = ref(defaultSettings.topNav);
  const tagsView = ref(defaultSettings.tagsView);
  const fixedHeader = ref(defaultSettings.fixedHeader);
  const sidebarLogo = ref(defaultSettings.sidebarLogo);
  const dynamicTitle = ref(defaultSettings.dynamicTitle);
  const isDarkState = ref(isDark.value); // 重命名避免与 useDark 冲突
  // 修改布局设置（保持方法名不变）
  function changeSetting(data) {
    const { key, value } = data;
    if (key in {
      title: true,
      theme: true,
      sideTheme: true,
      showSettings: true,
      topNav: true,
      tagsView: true,
      fixedHeader: true,
      sidebarLogo: true,
      dynamicTitle: true,
      isDark: true
    }) {
      // 通过动态属性名访问并修改 ref
      const refObj = {
        title,
        theme,
        sideTheme,
        showSettings,
        topNav,
        tagsView,
        fixedHeader,
        sidebarLogo,
        dynamicTitle,
        isDark
      }[key];
      refObj.value = value;
    }
  }

  // 设置网页标题（保持方法名不变）
  function setTitle(newTitle) {
    title.value = newTitle; // ❗注意：此处参数名与状态名冲突，可能导致问题
    useDynamicTitle();
  }

  // 切换暗黑模式（保持方法名不变）
  function toggleTheme() {
    isDarkState.value = !isDark.value;
    toggleDark();
  }

  // 返回状态和方法（保持原有命名）
  return {
    title,
    theme,
    sideTheme,
    showSettings,
    topNav,
    tagsView,
    fixedHeader,
    sidebarLogo,
    dynamicTitle,
    isDarkState,
    changeSetting,
    setTitle,
    toggleTheme
  };
}, {
  persist: true
}
)
export default useSettingsStore;
