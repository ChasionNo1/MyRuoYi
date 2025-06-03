<template>
  <div id="tags-view-container" class="tags-view-container">
    <scroll-pane ref="scrollPaneRef" class="tags-view-wrapper" @scroll="handleScroll">
      <!-- 
      向 HTML 元素添加自定义数据属性 data-path，存储当前标签页的路由路径。
      可通过 JavaScript 或 CSS 选择器快速获取标签页路径（如右键菜单中获取当前标签页）。  

      !isAffix(tag)：判断标签页是否为固定标签页（affix 为 false）。
      若是普通标签页，调用 closeSelectedTag(tag) 方法关闭；若是固定标签页，不执行操作（''）。
      用户通过中键快速关闭非固定标签页，提升操作效率。

      监听鼠标右键点击事件，打开标签页右键菜单。
      @contextmenu.prevent：阻止浏览器默认右键菜单，自定义菜单逻辑。
      openMenu(tag, $event)：接收当前标签页对象 tag 和鼠标事件对象 $event，用于计算菜单位置并显示。
      -->
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :data-path="tag.path"
        :class="isActive(tag) ? 'active' : ''"
        :to="{ path: tag.path, query: tag.query, fullPath: tag.fullPath }"
        class="tags-view-item"
        :style="activeStyle(tag)"
        @click.middle="!isAffix(tag) ? closeSelectedTag(tag) : ''"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        {{ tag.title }}
        <!-- v-if="!isAffix(tag)"：仅非固定标签页显示关闭图标。
         prevent：阻止事件冒泡到父元素（避免触发 router-link 的点击跳转）。
         stop：阻止事件继续传播，确保点击图标仅触发关闭操作。
         <close> 是自定义图标组件（可能基于 Element Plus 的 ElIcon），显示关闭符号（el-icon-close）。
         
        -->
        <span v-if="!isAffix(tag)" @click.prevent.stop="closeSelectedTag(tag)">
          <close class="el-icon-close" style="width: 1em; height: 1em;vertical-align: middle;" />
        </span>
      </router-link>
    </scroll-pane>
    <!-- 右键菜单 -->
    <ul v-show="visible" :style="{ left: left + 'px', top: top + 'px' }" class="contextmenu">
      <li @click="refreshSelectedTag(selectedTag)">
        <refresh-right style="width: 1em; height: 1em;" /> 刷新页面
      </li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">
        <close style="width: 1em; height: 1em;" /> 关闭当前
      </li>
      <li @click="closeOthersTags">
        <circle-close style="width: 1em; height: 1em;" /> 关闭其他
      </li>
      <li v-if="!isFirstView()" @click="closeLeftTags">
        <back style="width: 1em; height: 1em;" /> 关闭左侧
      </li>
      <li v-if="!isLastView()" @click="closeRightTags">
        <right style="width: 1em; height: 1em;" /> 关闭右侧
      </li>
      <li @click="closeAllTags(selectedTag)">
        <circle-close style="width: 1em; height: 1em;" /> 全部关闭
      </li>
    </ul>
  </div>
</template>

<script setup>
import ScrollPane from './ScrollPane'
import { getNormalPath } from '@/utils/ruoyi'
import useTagsViewStore from '@/stores/tagsView'
import useSettingsStore from '@/stores/settings'
import usePermissionStore from '@/stores/permission'
import {ref, getCurrentInstance, computed, watch, onMounted, nextTick} from 'vue'
import { useRoute, useRouter } from 'vue-router'

const visible = ref(false)
const top = ref(0)
const left = ref(0)
// 选中的tag
const selectedTag = ref({})
const affixTags = ref([])
// 滚动条
const scrollPaneRef = ref(null)

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()
// 这个是已经访问的路径，点击了一下就显示在tags view上
const visitedViews = computed(() => useTagsViewStore().visitedViews)
const routes = computed(() => usePermissionStore().routes)
const theme = computed(() => useSettingsStore().theme)

// 监听route，
watch(route, () => {
  // 将当前路由添加到标签页列表中。
  addTags()
  // 确保当前标签页在可视区域内（可能通过滚动实现）。
  moveToCurrentTag()
})

// 监听可见性，
// 通过在 document.body 上监听点击事件，可捕获页面任意位置的点击。
// 当用户点击菜单外部区域时，触发 closeMenu 关闭菜单。
watch(visible, (value) => {
  if (value) {
    // 当 visible 变为 true（菜单显示）时，在 document.body 上添加点击事件监听器 closeMenu。
    document.body.addEventListener('click', closeMenu)
  } else {
    // 当 visible 变为 false（菜单隐藏）时，移除该监听器，避免内存泄漏。
    document.body.removeEventListener('click', closeMenu)
  }
})

onMounted(() => {
  initTags()
  addTags()
})

function isActive(r) {
  return r.path === route.path
}

function activeStyle(tag) {
  if (!isActive(tag)) return {}
  return {
    "background-color": theme.value,
    "border-color": theme.value
  }
}

function isAffix(tag) {
  return tag.meta && tag.meta.affix
}

function isFirstView() {
  try {
    return selectedTag.value.fullPath === '/index' || selectedTag.value.fullPath === visitedViews.value[1].fullPath
  } catch (err) {
    return false
  }
}

function isLastView() {
  try {
    return selectedTag.value.fullPath === visitedViews.value[visitedViews.value.length - 1].fullPath
  } catch (err) {
    return false
  }
}
// 从路由配置中递归筛选出所有需要固定显示在标签页（通常是多标签页导航系统）中的路由项
function filterAffixTags(routes, basePath = '') {
  let tags = []
  routes.forEach(route => {
    if (route.meta && route.meta.affix) {
      const tagPath = getNormalPath(basePath + '/' + route.path)
      tags.push({
        fullPath: tagPath,
        path: tagPath,
        name: route.name,
        meta: { ...route.meta }
      })
    }
    if (route.children) {
      // 递归过滤固定tags
      const tempTags = filterAffixTags(route.children, route.path)
      if (tempTags.length >= 1) {
        tags = [...tags, ...tempTags]
      }
    }
  })
  return tags
}

// 初始化tags
// 先添加要固定显示的tags（必须要有name）
function initTags() {
  // 得到固定tags
  const res = filterAffixTags(routes.value)
  affixTags.value = res
  for (const tag of res) {
    // Must have tag name
    if (tag.name) {
       useTagsViewStore().addVisitedView(tag)
    }
  }
}

function addTags() {
  // 解析路由的名称
  const { name } = route
  // 如果路由名称存在
  if (name) {
    // 才将当前路由添加到已访问的标签页列表和缓存列表
    useTagsViewStore().addView(route)
  }
}

// 移动到当前tag处
function moveToCurrentTag() {
  // tags创建好再操作
  nextTick(() => {
    for (const r of visitedViews.value) {
      if (r.path === route.path) {
        // 滚动条移动到目标位置
        scrollPaneRef.value.moveToTarget(r)
        // when query is different then update
        // 当标签页的 fullPath（含查询参数）与当前路由不一致时，说明参数发生了变化（如 /user?id=1 → /user?id=2）。
        if (r.fullPath !== route.fullPath) {
          // 调用 updateVisitedView 方法更新标签页数据，确保标签页显示最新信息。
          useTagsViewStore().updateVisitedView(route)
        }
      }
    }
  })
}

function refreshSelectedTag(view) {
  proxy.$tab.refreshPage(view)
  // 标记该路由是否为 iframe 视图（link 通常存储 iframe 的 src 地址）。
  if (route.meta.link) {
    // 从标签页管理状态中删除 iframe 视图的缓存，确保刷新后重新加载 iframe。
    useTagsViewStore().delIframeView(route)
  }
}
// view -> tag
function closeSelectedTag(view) {
  // $tab 插件 通常是基于 Vue Router 封装的标签页管理器，提供如 openPage、closePage、refreshPage 等方法。
  // closePage(view) 方法用于关闭指定的标签页 view，通常会返回一个 Promise，包含操作结果和最新的标签页列表 visitedViews。
  proxy.$tab.closePage(view).then(({ visitedViews }) => {
    // 判断被关闭的是否为当前激活标签页，若是则跳转至最后一个有效标签页
    if (isActive(view)) {
      toLastView(visitedViews, view)
    }
  })
}

function closeRightTags() {
  // 在src/plugins/tab.js 调用store下的删除方法
  proxy.$tab.closeRightPage(selectedTag.value).then(visitedViews => {
    // 如果当前是激活项，
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      // 跳转到最后一个
      toLastView(visitedViews)
    }
  })
}

function closeLeftTags() {
  proxy.$tab.closeLeftPage(selectedTag.value).then(visitedViews => {
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

function closeOthersTags() {
  router.push(selectedTag.value).catch(() => { })
  proxy.$tab.closeOtherPage(selectedTag.value).then(() => {
    moveToCurrentTag()
  })
}

function closeAllTags(view) {
  proxy.$tab.closeAllPage().then(({ visitedViews }) => {
    if (affixTags.value.some(tag => tag.path === route.path)) {
      return
    }
    toLastView(visitedViews, view)
  })
}
// 跳转到最后一个view
// 用于导航到最后一个有效标签页的函数 toLastView，其核心逻辑是在关闭当前标签页后，根据剩余标签页的情况决定跳转到哪个页面
function toLastView(visitedViews, view) {
  // visitedViews.slice(-1) 返回数组的最后一个元素（即最近访问的标签页）。
  const latestView = visitedViews.slice(-1)[0]
  // 存在则跳转到最后一个标签页
  if (latestView) {
    router.push(latestView.fullPath)
  } else {
    // now the default is to redirect to the home page if there is no tags-view,
    // you can adjust it according to your needs.
    // 刷新首页
    if (view.name === 'Dashboard') {
      // to reload home page
      router.replace({ path: '/redirect' + view.fullPath })
    } else {
      // 跳转到根路径（通常是首页）
      router.push('/')
    }
  }
}
// 邮件打开菜单 根据鼠标点击位置计算菜单的显示位置，并确保菜单不会超出容器边界
function openMenu(tag, e) {
  const menuMinWidth = 105
  // 容器左边界相对于视口的位置
  const offsetLeft = proxy.$el.getBoundingClientRect().left // container margin left
  const offsetWidth = proxy.$el.offsetWidth // container width
  // 菜单最大左偏移量（防止菜单超出容器右边界）
  const maxLeft = offsetWidth - menuMinWidth // left boundary
// 菜单的初始左偏移量=鼠标点击的x坐标-容器的坐标+15
  const l = e.clientX - offsetLeft + 15 // 15: margin right

  // 如果超过了最大的左偏移量
  if (l > maxLeft) {
    // 就最大的左偏移量作为菜单的left值
    left.value = maxLeft
  } else {
    left.value = l
  }

  top.value = e.clientY
  visible.value = true
  // 选中tag
  selectedTag.value = tag
}

function closeMenu() {
  visible.value = false
}

function handleScroll() {
  closeMenu()
}
</script>

<style lang="scss" scoped>
.tags-view-container {
  height: 34px;
  width: 100%;
  background: var(--tags-bg, #fff);
  border-bottom: 1px solid var(--tags-item-border, #d8dce5);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, .12), 0 0 3px 0 rgba(0, 0, 0, .04);

  .tags-view-wrapper {
    .tags-view-item {
      display: inline-block;
      position: relative;
      cursor: pointer;
      height: 26px;
      line-height: 26px;
      border: 1px solid var(--tags-item-border, #d8dce5);
      color: var(--tags-item-text, #495060);
      background: var(--tags-item-bg, #fff);
      padding: 0 8px;
      font-size: 12px;
      margin-left: 5px;
      margin-top: 4px;

      &:first-of-type {
        margin-left: 15px;
      }

      &:last-of-type {
        margin-right: 15px;
      }

      &.active {
        background-color: #42b983;
        color: #fff;
        border-color: #42b983;

        &::before {
          content: '';
          background: #fff;
          display: inline-block;
          width: 8px;
          height: 8px;
          border-radius: 50%;
          position: relative;
          margin-right: 5px;
        }
      }
    }
  }

  .contextmenu {
    margin: 0;
    background: var(--el-bg-color-overlay, #fff);
    z-index: 3000;
    position: absolute;
    list-style-type: none;
    padding: 5px 0;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 400;
    color: var(--tags-item-text, #333);
    box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, .3);
    border: 1px solid var(--el-border-color-light, #e4e7ed);

    li {
      margin: 0;
      padding: 7px 16px;
      cursor: pointer;

      &:hover {
        background: var(--tags-item-hover, #eee);
      }
    }
  }
}
</style>

<style lang="scss">
//reset element css of el-icon-close
.tags-view-wrapper {
  .tags-view-item {
    .el-icon-close {
      width: 16px;
      height: 16px;
      vertical-align: 2px;
      border-radius: 50%;
      text-align: center;
      transition: all .3s cubic-bezier(.645, .045, .355, 1);
      transform-origin: 100% 50%;

      &:before {
        transform: scale(.6);
        display: inline-block;
        vertical-align: -3px;
      }

      &:hover {
        background-color: var(--tags-close-hover, #b4bccc);
        color: #fff;
        width: 12px !important;
        height: 12px !important;
      }
    }
  }
}
</style>