import { defineStore } from "pinia";
import { ref, reactive, watch } from "vue";

export const useAppStore = defineStore("app", () => {
  // 响应式状态定义
  const sidebar = reactive({
    opened: true,
    withoutAnimation: false,
    hide: false,
    status: 1
  });

  const device = ref("desktop");
  const size = ref("default");

  // 切换侧边栏状态
  function toggleSideBar(withoutAnimation = false) {
    if (sidebar.hide) return;
    
    sidebar.opened = !sidebar.opened;
    sidebar.withoutAnimation = withoutAnimation;
    sidebar.status = sidebar.opened ? 1 : 0;
  }

  // 关闭侧边栏
  function closeSideBar({ withoutAnimation = false } = {}) {
    sidebar.opened = false;
    sidebar.status = 0;
    sidebar.withoutAnimation = withoutAnimation;
  }

  // 切换设备类型
  function toggleDevice(newDevice) {
    device.value = newDevice;
  }

  // 设置界面尺寸
  function setSize(newSize) {
    size.value = newSize;
  }

  // 切换侧边栏隐藏状态
  function toggleSideBarHide(status) {
    sidebar.hide = status;
  }

  // 监听状态变化并自动保存（可选，适用于复杂场景）
  watch(
    [() => sidebar, device, size],
    () => {
      // 手动保存状态（如果使用插件则不需要）
      // localStorage.setItem("app-store", JSON.stringify({ sidebar, device, size }));
    },
    { deep: true }
  );

  // 暴露状态和方法
  return {
    sidebar,
    device,
    size,
    toggleSideBar,
    closeSideBar,
    toggleDevice,
    setSize,
    toggleSideBarHide
  };
}, {
  // 持久化配置
  persist:true
});

export default useAppStore;