/**
 * 
 * 基于 Vue 3 + Pinia 实现的动态路由权限管理模块，
 * 主要功能是根据用户权限从后端获取路由配置并动态生成前端路由
 */
import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'
import { defineStore } from 'pinia'

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')
const usePermissionStore = defineStore('permission', {
    state: () => ({
        routes: [],           // 最终生成的完整路由表
        addRoutes: [],        // 动态添加的路由
        defaultRoutes: [],    // 默认路由（带侧边栏）
        topbarRouters: [],    // 顶部导航栏路由
        sidebarRouters: []    // 侧边栏导航路由
    }),
    actions: {
        // 设置路由
        setRoutes(routes) {
            this.addRoutes = routes
            this.routes = constantRoutes.concat(routes)
        },
        setDefaultRoutes(routes) {
            this.defaultRoutes = constantRoutes.concat(routes)
        },
        setTopbarRoutes(routes) {
            this.topbarRouters = routes
        },
        setSidebarRouters(routes) {
            this.sidebarRouters = routes
        },

        // 路由生成
        generateRoutes() {
            return new Promise(resolve => {
                // 1. 从后端获取路由配置
                getRouters().then(res => {
                  console.log("stores/permission:routers", res.data )
                    // 2. 深拷贝路由数据（用于不同场景处理）
                    const sdata = JSON.parse(JSON.stringify(res.data))
                    console.log(sdata)
                    const rdata = JSON.parse(JSON.stringify(res.data))
                    const defaultData = JSON.parse(JSON.stringify(res.data))

                    // 3. 过滤并转换路由组件
                    const sidebarRoutes = filterAsyncRouter(sdata)
                    const rewriteRoutes = filterAsyncRouter(rdata, false, true)
                    const defaultRoutes = filterAsyncRouter(defaultData)

                    // 4. 过滤本地定义的动态路由（基于角色/权限）
                    const asyncRoutes = filterDynamicRoutes(dynamicRoutes)

                    // 5. 将本地动态路由添加到路由实例
                    asyncRoutes.forEach(route => { router.addRoute(route) })

                    // 6. 更新状态
                    this.setRoutes(rewriteRoutes)
                    this.setSidebarRouters(constantRoutes.concat(sidebarRoutes))
                    console.log('sidebar router', this.sidebarRouters)
                    this.setDefaultRoutes(sidebarRoutes)
                    this.setTopbarRoutes(defaultRoutes)

                    resolve(rewriteRoutes)
                })
            })
        }

    }
})
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    // 1. 处理特殊组件（Layout、ParentView、InnerLink）
    if (route.component) {
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        // 2. 动态加载views目录下的组件
        route.component = loadView(route.component)
      }
    }
    
    // 3. 递归处理子路由
    if (route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    
    return true
  })
}
function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export const loadView = (view) => {
  let res
  for (const path in modules) {
    const dir = path.split('views/')[1].split('.vue')[0]
    if (dir === view) {
      res = () => modules[path]()
    }
  }
  return res
}

export default usePermissionStore
