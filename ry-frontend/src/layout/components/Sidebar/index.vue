<script setup>
import Logo from './Logo.vue'
import {ref, computed} from 'vue'
import useSettingsStore from '@/stores/settings';
import variables from '@/assets/styles/variables.module.scss'
import SidebarItem from './SidebarItem.vue';
import usePermissionStore from '@/stores/permission'
defineOptions({
  name: "SideBar",
});
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()
const isCollapse = computed(() => {
  return settingsStore.sideStatus === 'collapse' ? true : false
})
const showLogo = ref(true)

const sideTheme = computed(() => settingsStore.sideTheme)
const sidebarRouters = computed(() => permissionStore.sidebarRouters)
// console.log('sidebar routers:' + sidebarRouters.value)

// 获取菜单背景色
const getMenuBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

// 获取菜单文字颜色
const getMenuTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-text)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuText : variables.menuLightText
})
</script>
<template>
  <div class="sidebar-container" :class="{ 'has-logo': showLogo }">
    <Logo v-if="showLogo" :collapse="isCollapse"></Logo>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu 
      :background-color="getMenuBackground" 
      :text-color="getMenuTextColor"
      :class="sideTheme"
      >
        <SidebarItem 
        v-for="(route, index) in sidebarRouters"
        :key="route.path + index"
        :item="route"
        :base-path="route.path"
        ></SidebarItem>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<style lang="scss" scoped>
.sidebar-container {
  background-color: v-bind(getMenuBackground); // 使用CSS变量，默认值为red
   height: 100%; /* 关键 */
  width: 100%;
  display: flex; /* 新增 */
  flex-direction: column; /* 新增 */
  
  .scrollbar-wrapper {
    background-color: v-bind(getMenuBackground); /* 继承父级背景 */
  flex-grow: 1; /* 填满剩余空间 */
  }
  }
</style>
