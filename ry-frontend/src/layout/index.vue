<script setup>
// 导入各个组件
import Sidebar from './components/Sidebar/index.vue'
import NarBar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'
import {ref, computed} from 'vue'
import useSettingsStore from '@/stores/settings.js'
import useAppStore from '@/stores/app'
defineOptions({
  // 作为单页程序的基座，通过路由控制不同内容的展示
  name: 'BasicLayout'
})
const settingsStore = useSettingsStore()

// 这里的值应该从全局取的
const sidebar = computed(() => useAppStore().sidebar)

</script>

  <template>
    <!-- 采用容器布局,每个部分的具体内容又各自的组件来实现 -->
  <div class="basic-layout">
    <el-container class="basic-container">
      <el-aside v-if="!sidebar.hide" class="basic-aside">
        <Sidebar></Sidebar>
      </el-aside>
      <el-container class="basic-content">
        <el-header class="basic-header"><NarBar></NarBar></el-header>
        <el-main class="basic-main"><AppMain></AppMain></el-main>
      </el-container>
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

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
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