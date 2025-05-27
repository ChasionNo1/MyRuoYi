<script setup>
import Logo from "./Logo.vue";
import { computed } from "vue";
import useSettingsStore from "@/stores/settings";
import variables from "@/assets/styles/variables.module.scss";
import SidebarItem from "./SidebarItem.vue";
import usePermissionStore from "@/stores/permission";
import useAppStore from "@/stores/app";
import { useRoute } from 'vue-router'

defineOptions({
  name: "SideBar",
});

const settingsStore = useSettingsStore();
const permissionStore = usePermissionStore();
const appStore = useAppStore()
// 获取当前激活的路由信息，用于确定侧边栏菜单中需要高亮显示的菜单项
const route = useRoute() 

// 获取属性值
const isCollapse = computed(() => !appStore.sidebar.opened)
const showLogo = computed(() => settingsStore.sidebarLogo)
const theme = computed(() => settingsStore.theme)
const sideTheme = computed(() => settingsStore.sideTheme);
const sidebarRouters = computed(() => permissionStore.sidebarRouters);
// console.log('sidebar routers:' + sidebarRouters.value)

// 获取菜单背景色
const getMenuBackground = computed(() => {
  if (settingsStore.isDark) {
    return "var(--sidebar-bg)";
  }
  return sideTheme.value === "theme-dark" ? variables.menuBg : variables.menuLightBg;
});

// 获取菜单文字颜色
const getMenuTextColor = computed(() => {
  if (settingsStore.isDark) {
    return "var(--sidebar-text)";
  }
  return sideTheme.value === "theme-dark" ? variables.menuText : variables.menuLightText;
});

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>
<!-- 
:default-active="activeMenu"
作用：设置菜单的默认激活项（高亮显示的菜单项）。

:collapse="isCollapse"
作用：控制菜单是否折叠（收起为简洁模式）。

:background-color="getMenuBackground"
作用：设置菜单的背景颜色。

:text-color="getMenuTextColor"
作用：设置菜单文字的默认颜色

:unique-opened="true"
作用：限制同一时间只能展开一个子菜单（折叠其他已展开的子菜单）。

:active-text-color="theme"
作用：设置激活状态（选中）的菜单项文字颜色。

:collapse-transition="false"
作用：关闭菜单折叠 / 展开时的过渡动画。

mode="vertical"
作用：设置菜单模式为垂直排列（侧边栏常见模式）。

:class="sideTheme"
作用：为菜单组件添加自定义类名（如 theme-dark 或 theme-light）。
-->
<template>
  <div class="sidebar-container" :class="{ 'has-logo': showLogo }">
    <Logo v-if="showLogo" :collapse="isCollapse"></Logo>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="getMenuBackground"
        :text-color="getMenuTextColor"
        :unique-opened="true"
        :active-text-color="theme"
        :collapse-transition="false"
        mode="vertical"
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
  background-color: v-bind(getMenuBackground);
  
  .scrollbar-wrapper {
    background-color: v-bind(getMenuBackground);
  }

  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;
    
    .el-menu-item, .el-sub-menu__title {
      &:hover {
        background-color: var(--menu-hover, rgba(0, 0, 0, 0.06)) !important;
      }
    }

    .el-menu-item {
      color: v-bind(getMenuTextColor);
      
      &.is-active {
        color: var(--menu-active-text, #409eff);
        background-color: var(--menu-hover, rgba(0, 0, 0, 0.06)) !important;
      }
    }

    .el-sub-menu__title {
      color: v-bind(getMenuTextColor);
    }
  }
}
</style>
