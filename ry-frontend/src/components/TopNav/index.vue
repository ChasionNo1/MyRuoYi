<template>
  <!-- 页面加载时默认激活菜单的 index
  菜单展示模式  水平
  菜单激活回调 
  是否省略多余的子项（仅在横向模式生效）
  -->
  <el-menu
    :default-active="activeMenu"
    mode="horizontal"
    @select="handleSelect"
    :ellipsis="false"
  >
  <!-- 渲染可见菜单项 -->
    <template v-for="(item, index) in topMenus">
      <el-menu-item :style="{'--theme': theme}" :index="item.path" :key="index" v-if="index < visibleNumber">
        <svg-icon
        v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
        :icon-class="item.meta.icon"/>
        {{ item.meta.title }}
      </el-menu-item>
    </template>

    <!-- 顶部菜单超出数量折叠 -->
    <el-sub-menu :style="{'--theme': theme}" index="more" v-if="topMenus.length > visibleNumber">
      <template #title>更多菜单</template>
      <template v-for="(item, index) in topMenus">
        <el-menu-item
          :index="item.path"
          :key="index"
          v-if="index >= visibleNumber">
        <svg-icon
          v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
          :icon-class="item.meta.icon"/>
        {{ item.meta.title }}
        </el-menu-item>
      </template>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { constantRoutes } from "@/router"
import { isHttp } from '@/utils/validate'
import useAppStore from '@/stores/app'
import useSettingsStore from '@/stores/settings'
import usePermissionStore from '@/stores/permission'
import {ref, computed, onMounted, onBeforeUnmount} from 'vue'
import {useRoute, useRouter} from 'vue-router'

// 顶部栏初始数
const visibleNumber = ref(null)
// 当前激活菜单的 index
const currentIndex = ref(null)
// 隐藏侧边栏路由
const hideList = ['/index', '/user/profile']

const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()
const route = useRoute()
const router = useRouter()

// 主题颜色
const theme = computed(() => settingsStore.theme)
// 所有的路由信息
const routers = computed(() => permissionStore.topbarRouters)

// 顶部显示菜单
const topMenus = computed(() => {
  let topMenus = []
  routers.value.map((menu) => {
    if (menu.hidden !== true) {
      // 兼容顶部栏一级菜单内部跳转
      if (menu.path === '/' && menu.children) {
          topMenus.push(menu.children[0])
      } else {
          topMenus.push(menu)
      }
    }
  })
  return topMenus
})

// 设置子路由
// 这里是点击了top nav，侧边栏显示的子路由列表
// 这里应该是设置侧边栏的路由列表
const childrenMenus = computed(() => {
  let childrenMenus = []
  routers.value.map((router) => {
    // 遍历子路由
    for (let item in router.children) {
      // 如果子路由的父路径未定义
      if (router.children[item].parentPath === undefined) {
        // 并且，当前路由为/
        if(router.path === "/") {
          // 那么就设置当前子路由的路径为：/+子路由路径
          router.children[item].path = "/" + router.children[item].path
        } else {
          // 如果不是，并且当前不是http链接
          if(!isHttp(router.children[item].path)) {
            // 设置子路径为：父路径+子路由
            router.children[item].path = router.path + "/" + router.children[item].path
          }
        }
        // 设置子路由的父路径为：当前路由的路径
        router.children[item].parentPath = router.path
      }
      // 添加子路由
      childrenMenus.push(router.children[item])
    }
  })
  // 拼接路由
  return constantRoutes.concat(childrenMenus)
})

// 默认激活的菜单
/**
 * 确定激活菜单：根据当前路由路径（如 /system/log），提取一级路径（如 /system）作为顶部导航的激活项。
  控制侧边栏显示：
  对于多级路由（如 /system/log），显示侧边栏并加载对应的二级菜单。
  对于特殊路由（如 /index、/user/profile），隐藏侧边栏。
 * 
 */
const activeMenu = computed(() => {
  // path:/system/log
  const path = route.path
   // 默认激活路径为当前完整路径
  let activePath = path
  // 如果当前路径不是未定义，并且不是'/xxx'，是二级的，并且不在隐藏列表里
  if (path !== undefined && path.lastIndexOf("/") > 0 && hideList.indexOf(path) === -1) {
    // 截取路径，system/log
    const tmpPath = path.substring(1, path.length)
    // link:null // 非外部链接路由
    if (!route.meta.link) {
      // 提取一级路径（如 "system"）并补全斜杠（如 "/system"）
      activePath = "/" + tmpPath.substring(0, tmpPath.indexOf("/"))
      // 显示侧边栏
      appStore.toggleSideBarHide(false)
    }
    // 处理无子路由的情况
  } else if(!route.children) {
    // 激活路径为完整路径
    activePath = path
    appStore.toggleSideBarHide(true)
  }
  // 根据激活路径加载侧边栏菜单
  activeRoutes(activePath)
  return activePath
})

// 设计top nav 的可见数量
function setVisibleNumber() {
  // getBoundingClientRect()：返回 document.body 的可见区域尺寸（单位：像素），包含 width 和 height 等属性。
  const width = document.body.getBoundingClientRect().width / 3
  visibleNumber.value = parseInt(width / 85)
}
/**
 * 顶部导航菜单的点击事件处理函数，主要负责根据点击的菜单项类型（外部链接、内部路由、带子路由的菜单）进行不同的路由跳转逻辑，并控制侧边栏的显示状态
 * @param key 点击的菜单项的 index（通常对应路由路径，如 "/system"）。
 * @param keyPath 点击的菜单项的路径数组（Element Plus 提供，此处未使用）。
 */
function handleSelect(key, keyPath) {
  // 记录当前点击的菜单项索引（路径）
  currentIndex.value = key
   // 判断当前点击的菜单是否为一级路由且包含子路由。
  const route = routers.value.find(item => item.path === key)
  //  处理外部链接（HTTP 协议路径）
  if (isHttp(key)) {
    // http(s):// 路径新窗口打开，这里是跳转到他的官网
    window.open(key, "_blank")
    // 处理无子路由的内部路由
  } else if (!route || !route.children) {
    // 查找子路由（可能为二级路由或带参数的路由）
    const routeMenu = childrenMenus.value.find(item => item.path === key)
    // 解析路由参数（如 query 字符串）
    if (routeMenu && routeMenu.query) {
      // 带参数跳转
      let query = JSON.parse(routeMenu.query)
      router.push({ path: key, query: query })
    } else {
      router.push({ path: key })
    }
    // 隐藏侧边栏
    appStore.toggleSideBarHide(true)
  } else {
    // 处理带子路由的一级路由
    // 显示左侧联动菜单
    activeRoutes(key)
    appStore.toggleSideBarHide(false)
  }
}
/**
 * 根据当前激活的顶部导航菜单路径（key），筛选出对应的子路由列表，并更新侧边栏的显示状态
 * key：当前激活的顶部导航菜单路径（如 /system、/index），通常对应一级路由的路径。
 */
function activeRoutes(key) {
  let routes = []
  if (childrenMenus.value && childrenMenus.value.length > 0) {
    childrenMenus.value.map((item) => {
      // 当前激活路径和这个子菜单项的父路径一致时，或者当前激活路径为index，子菜单项的路径为空
      // 匹配条件：子路由的父路径等于激活键（key），或特殊处理首页场景
      // 特殊处理首页场景：当 key 为 "index"（对应路径 /index）时，匹配 path 为空的子路由（通常为首页的默认子路由）
      // 首页可能配置为 path: "/"，其子路由 path 为空（如 { path: "", component: Home }），此时通过此条件匹配。
      if (key == item.parentPath || (key == "index" && "" == item.path)) {
        routes.push(item)
      }
    })
  }
  if(routes.length > 0) {
    // 将筛选出的子路由列表存入权限存储，供侧边栏组件读取并渲染
    permissionStore.setSidebarRouters(routes)
  } else {
    // 若没有匹配的子路由（如顶级路由无子菜单），隐藏侧边栏。
    appStore.toggleSideBarHide(true)
  }
  return routes
}

onMounted(() => {
  window.addEventListener('resize', setVisibleNumber)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', setVisibleNumber)
})

onMounted(() => {
  setVisibleNumber()
})
</script>

<style lang="scss">
.topmenu-container.el-menu--horizontal > .el-menu-item {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: #999093 !important;
  padding: 0 5px !important;
  margin: 0 10px !important;
}

.topmenu-container.el-menu--horizontal > .el-menu-item.is-active, .el-menu--horizontal > .el-sub-menu.is-active .el-submenu__title {
  border-bottom: 2px solid #{'var(--theme)'} !important;
  color: #303133;
}

/* sub-menu item */
.topmenu-container.el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: #999093 !important;
  padding: 0 5px !important;
  margin: 0 10px !important;
}

/* 背景色隐藏 */
.topmenu-container.el-menu--horizontal>.el-menu-item:not(.is-disabled):focus, .topmenu-container.el-menu--horizontal>.el-menu-item:not(.is-disabled):hover, .topmenu-container.el-menu--horizontal>.el-submenu .el-submenu__title:hover {
  background-color: #ffffff;
}

/* 图标右间距 */
.topmenu-container .svg-icon {
  margin-right: 4px;
}

/* topmenu more arrow */
.topmenu-container .el-sub-menu .el-sub-menu__icon-arrow {
  position: static;
  vertical-align: middle;
  margin-left: 8px;
  margin-top: 0px;
}


</style>
