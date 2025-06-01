<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <!-- transition-group实现路由项的过渡动画，name属性用于自动生成CSS类名 -->
    <transition-group name="breadcrumb">
      <!-- 循环渲染面包屑层级列表，使用item.path作为唯一key -->
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <!-- 条件渲染：当路由redirect为noRedirect或当前项为最后一个时，显示为不可点击的span -->
        <span v-if="item.redirect === 'noRedirect' || index == levelList.length - 1" class="no-redirect">{{ item.meta.title }}</span>
         <!-- 否则显示为可点击的链接，prevent修饰符阻止默认跳转行为 -->
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import usePermissionStore from '@/stores/permission'
import {useRoute, useRouter} from 'vue-router'
import {ref, watchEffect} from 'vue'
const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
// 定义响应式的面包屑层级列表
const levelList = ref([])

/**
 * 生成面包屑数据的核心函数
 * 
 * 这里感觉七绕八绕的，最后不就是得到一个levellist：route list
 * 首页 / 系统管理 / 用户管理
 * 我自己的思路
 * 首先是得到当前访问的路径，对其进行解析，前提是按照规定设置好
 * 像这样的，/system/user，/system 可以被解析成系统管理，算是一级路由
 * 下面的二级需要参考一级的，/user 用户， /user 同时可以是一级路由，所以这里要考虑
 */
function getBreadcrumb() {
  // 存储匹配到的路由项
  let matched = []
  // 计算当前路由路径的层级数（斜杠数量）
  const pathNum = findPathNum(route.path)
  // 处理多级路由（层级数 > 2，如 /a/b/c 层级为3）
  if (pathNum > 2) {
    // 正则匹配路径片段（如 /user/123 匹配为 ['/user', '/123']）
    const reg = /\/\w+/gi
    const pathList = route.path.match(reg).map((item, index) => {
      // 处理首个路径片段（保留完整路径），其他片段去除开头斜杠
      // ['/user', '123']
      if (index !== 0) item = item.slice(1)
      return item
    })
    // 递归匹配路由配置中的多级路由
    getMatched(pathList, permissionStore.defaultRoutes, matched)
  } else {
    // 非多级路由：直接使用路由匹配结果，过滤掉无标题的路由
    matched = route.matched.filter((item) => item.meta && item.meta.title)
  }
  // 判断是否为首页
  if (!isDashboard(matched[0])) {
    // 非首页时，在面包屑开头添加「首页」项
    matched = [{ path: "/index", meta: { title: "首页" } }].concat(matched)
  }
  // 最终过滤：保留有标题且未禁止显示面包屑的路由项
  levelList.value = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
}
function findPathNum(str, char = "/") {
  let index = str.indexOf(char)
  let num = 0
  while (index !== -1) {
    num++
    index = str.indexOf(char, index + 1)
  }
  return num
}
/**
 * 递归匹配路由项（处理多级嵌套路由）
 * @param {Array} pathList - 路径片段数组（如 ['user', '123']）
 * @param {Array} routeList - 当前层级的路由列表
 * @param {Array} matched - 已匹配的路由项数组（递归累加）
 * routeList
 * {"name":"System","path":"/system","hidden":false,
 * "redirect":"noRedirect",
 * "component":{"name":"BasicLayout","__name":"index","__hmrId":"051739fd","__scopeId":"data-v-051739fd","__file":"D:/java_projects/MyRuoYi/ry-frontend/src/layout/index.vue"},
 * "alwaysShow":true,
 * "meta":{"title":"系统管理","icon":"system","noCache":false,"link":null},
 * "children":[{"name":"User","path":"user","hidden":false,
 * "meta":{"title":"用户管理","icon":"user","noCache":false,"link":null}},
 * {"name":"Role","path":"role","hidden":false,
 * "meta":{"title":"角色管理","icon":"peoples","noCache":false,"link":null}},
 * {"name":"Menu","path":"menu","hidden":false,
 * "meta":{"title":"菜单管理","icon":"tree-table","noCache":false,"link":null}},
 * {"name":"Dept","path":"dept","hidden":false,
 * "meta":{"title":"部门管理","icon":"tree","noCache":false,"link":null}},
 * {"name":"Post","path":"post","hidden":false,
 * "meta":{"title":"岗位管理","icon":"post","noCache":false,"link":null}},
 * {"name":"Dict","path":"dict","hidden":false,
 * "meta":{"title":"字典管理","icon":"dict","noCache":false,"link":null}},
 * {"name":"Config","path":"config","hidden":false,
 * "meta":{"title":"参数设置","icon":"edit","noCache":false,"link":null}},
 * {"name":"Notice","path":"notice","hidden":false,
 * "meta":{"title":"通知公告","icon":"message","noCache":false,"link":null}},
 * {"name":"Log","path":"log","hidden":false,"redirect":"noRedirect","component":{"__hmrId":"341491bb","__file":"D:/java_projects/MyRuoYi/ry-frontend/src/components/ParentView/index.vue"},
 * "alwaysShow":true,
 * "meta":{"title":"日志管理","icon":"log","noCache":false,"link":null},
 * "children":[{"name":"Operlog","path":"operlog","hidden":false,
 * "meta":{"title":"操作日志","icon":"form","noCache":false,"link":null}},
 * {"name":"Logininfor","path":"logininfor","hidden":false,
 * "meta":{"title":"登录日志","icon":"logininfor","noCache":false,"link":null}}]}]}
 */
function getMatched(pathList, routeList, matched) {
  // 查找匹配的路由项（路径或名称匹配，忽略名称大小写）
  // 这里不是漫无目的的匹配，如果从开头匹配对了，会接着往下递归遍历子级菜单的
  let data = routeList.find(item => item.path === pathList[0] || (item.name += '').toLowerCase() == pathList[0])
  if (data) {
    // 将匹配到的路由项加入列表
    matched.push(data)
     // 如果存在子路由且还有未处理的路径片段，递归处理下一层级
    if (data.children && pathList.length) {
      // 移除已处理的路径片段
      pathList.shift()
      // 递归匹配子路由
      getMatched(pathList, data.children, matched)
    }
  }
}
/**
 * 判断是否为首页路由
 * @param {Object} route - 路由项
 * @returns {boolean} 是否为首页
 */
function isDashboard(route) {
  // 获取路由名称
  const name = route && route.name
  // 无名称的路由不是首页
  if (!name) {
    return false
  }
  // 假设首页路由名称为'Index'
  return name.trim() === 'Index'
}

/**
 * 处理面包屑项点击事件
 * @param {Object} item - 路由项
 */
function handleLink(item) {
  // 解构路由的重定向路径和原始路径
  const { redirect, path } = item
  if (redirect) {
    // 存在重定向时跳转到重定向地址
    router.push(redirect)
    return
  }
  // 否则跳转到原始路径
  router.push(path)
}

// 监听路由变化，自动更新面包屑（路由变化时触发）
watchEffect(() => {
  // if you go to the redirect page, do not update the breadcrumbs
  // 过滤以/redirect/开头的路径（通常为临时重定向，不显示面包屑
  if (route.path.startsWith('/redirect/')) {
    return
  }
  // 重新生成面包屑数据
  getBreadcrumb()
})
getBreadcrumb()
</script>

<style lang='scss' scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;

  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
}
</style>