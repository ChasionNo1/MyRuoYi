<script setup>
// 导入各个组件
import Sidebar from './components/Sidebar/index.vue'
import NarBar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'
import {ref, computed} from 'vue'
import useSettingsStore from '@/stores/settings.js'
defineOptions({
  // 作为单页程序的基座，通过路由控制不同内容的展示
  name: 'BasicLayout'
})
const settingsStore = useSettingsStore()

// 这里的值应该从全局取的
const asideWidth = computed(() => {return settingsStore.sideStatus === 'open' ? '10%' : settingsStore.sideStatus === 'collapse' ? '5%' : '0%'})


</script>

  <template>
    <!-- 采用容器布局,每个部分的具体内容又各自的组件来实现 -->
  <div class="basic-layout">
    <el-container class="basic-container">
      <el-aside v-show="true" class="basic-aside" :style="{width: asideWidth}">
        <Sidebar></Sidebar>
      </el-aside>
      <el-container class="basic-content">
        <el-header class="basic-header"><NarBar></NarBar></el-header>
        <el-main class="basic-main"><AppMain></AppMain></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.basic-layout, .basic-container, .basic-content {
  height: 100%;
  width: 100%;
}
.basic-container {
  display: flex;
  height: 100vh;
}


.basic-aside {
  border: 1px solid red;
  flex-shrink: 0;
  /* background-color: aqua; */
  overflow: hidden; /* 防止内容在缩小时溢出 */
  transition: width 0.3s ease; /* 侧边栏宽度动画 */
}

.basic-header {
  height: 5%;
  border: 1px solid blue;
}

.basic-main {
  flex-grow: 1;
  border: 1px solid black;
}

</style>