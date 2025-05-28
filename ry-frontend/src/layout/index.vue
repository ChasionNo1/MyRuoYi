<script setup>
// 导入各个组件
import Sidebar from './components/Sidebar/index.vue'
import NarBar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'
import Settings from './components/Settings'
import {ref, computed} from 'vue'
import useSettingsStore from '@/stores/settings.js'
import useAppStore from '@/stores/app'
import { useWindowSize } from '@vueuse/core'
import { watch, watchEffect } from 'vue'
defineOptions({
  // 作为单页程序的基座，通过路由控制不同内容的展示
  name: 'BasicLayout'
})
const settingsStore = useSettingsStore()

// 这里的值应该从全局取的
const sidebar = computed(() => useAppStore().sidebar)
const theme = computed(() => settingsStore.theme)
const device = computed(() => useAppStore().device)
const needTagsView = computed(() => settingsStore.tagsView)
const fixedHeader = computed(() => settingsStore.fixedHeader)

const classObj = computed(() => ({
  // 侧边栏折叠成迷你形式
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

function handleClickOutside() {
  // 关闭侧边栏
  useAppStore().closeSideBar({ withoutAnimation: false })
}
const { width, height } = useWindowSize()
const WIDTH = 992 // refer to Bootstrap's responsive design

// 监控设备值，如果当前是手机并且侧边栏是打开的，就给关闭了
watch(() => device.value, () => {
  if (device.value === 'mobile' && sidebar.value.opened) {
    useAppStore().closeSideBar({ withoutAnimation: false })
  }
})
// 监听窗口宽度变化，并根据宽度自动切换设备模式和侧边栏状态。
watchEffect(() => {
  if (width.value - 1 < WIDTH) {
    useAppStore().toggleDevice('mobile')
    useAppStore().closeSideBar({ withoutAnimation: true })
  } else {
    useAppStore().toggleDevice('desktop')
  }
})

const settingRef = ref(null)
// 这段代码实现了一个跨组件调用方法的功能，
// 让父组件可以直接触发子组件 <Settings> 的 openSetting() 方法
  // 可以改成状态管理
function setLayout() {
  settingRef.value.openSetting()
}
</script>

  <template>
    <!-- 采用容器布局,每个部分的具体内容又各自的组件来实现 -->
  <div class="app-wrapper" :class="classObj" :style="{'--current-color': theme}">
    <!-- 手机端的蒙层，当当前的设备是手机，并且侧边栏是打开状态，点击 -->
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside"></div>
      <el-aside v-if="!sidebar.hide" class="sidebar-container">
        <Sidebar></Sidebar>
      </el-aside>
<!-- 定义了两个动态加载的类，标签页导航和侧边栏隐藏 -->
      <el-container :class="{ hasTagsView: needTagsView, sidebarHide: sidebar.hide }" class="main-container">
        <el-header :class="{ 'fixed-header': fixedHeader }">
          <NarBar @setLayout="setLayout"></NarBar>
        </el-header>
        <el-main class="basic-main"><AppMain></AppMain></el-main>
        <settings ref="settingRef" />
      </el-container>
  </div>
</template>

<style lang="scss" scoped>
  @use "@/assets/styles/mixin.scss" as mixin;
  @use "@/assets/styles/variables.module.scss" as variables;

.app-wrapper {
  @include  mixin.clearfix;
  position: relative;
  height: 100%;
  width: 100%;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}
// 在移动端侧边栏展开时，这个背景层会覆盖整个屏幕，使用户只能与侧边栏交互，点击背景层可关闭侧边栏
.drawer-bg {
  background: #000;
  // 透明度
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  // 确保背景层显示在页面其他内容的上方，避免被遮挡。
  // 通常用于模态框、侧边栏等需要 “浮层” 效果的组件
  z-index: 999;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - variables.$base-sidebar-width);
  transition: width 0.28s;
}

.hideSidebar .fixed-header {
  width: calc(100% - 54px);
}

.sidebarHide .fixed-header {
  width: 100%;
}

.mobile .fixed-header {
  width: 100%;
}

</style>