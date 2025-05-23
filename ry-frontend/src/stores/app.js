// 定义后台管理系统应用状态存储模块
import { defineStore } from "pinia"
const useAppStore = defineStore('app', {
    state: () => ({
        sidebar: {
            // 侧边栏是否打开（从 Cookie 读取，默认值：true）
            opened: true,
            // 是否禁用动画（切换侧边栏时）
            withoutAnimation: false,
            // 是否隐藏侧边栏（常用于移动端或特殊布局）
            hide: false,
            // 侧边栏状态
            status: 1
        },
        device: 'desktop',
        size: 'default' // 由持久化插件自动读取

    }),
    actions: {
        // 切换侧边栏状态（移除 Cookie 操作）
      toggleSideBar(withoutAnimation) {
        if (this.sidebar.hide) return false;
        this.sidebar.opened = !this.sidebar.opened;
        this.sidebar.withoutAnimation = withoutAnimation;
        if (this.sidebar.opened) {
            this.sidebar.status = 1
        }else {
            this.sidebar.status = 0
        }
      },

      // 关闭侧边栏（移除 Cookie 操作）
      closeSideBar({ withoutAnimation }) {
        this.sidebar.status = 0
        this.sidebar.opened = false;
        this.sidebar.withoutAnimation = withoutAnimation;
      },

      // 切换设备类型
      toggleDevice(device) {
        this.device = device;
      },

      // 设置界面尺寸（移除 Cookie 操作）
      setSize(size) {
        this.size = size;
      },

      // 切换侧边栏隐藏状态
      toggleSideBarHide(status) {
        this.sidebar.hide = status;
      }
    }
}, {
    persist: true
})

export default useAppStore