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
import { ref } from 'vue'

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../views/**/*.vue')

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref([])
  const addRoutes = ref([])
  const defaultRoutes = ref([])
  const topbarRouters = ref([])
  const sidebarRouters = ref([])
  // 设置路由（保持原有函数名）
  function setRoutes(newRoutes) {
    addRoutes.value = newRoutes;
    routes.value = [...constantRoutes, ...newRoutes];
  }

  function setDefaultRoutes(newRoutes) {
    defaultRoutes.value = [...constantRoutes, ...newRoutes];
  }

  function setTopbarRoutes(newRoutes) {
    topbarRouters.value = newRoutes;
  }

  function setSidebarRouters(newRoutes) {
    sidebarRouters.value = newRoutes;
  }

  // 路由生成（保持原有函数名）
  function generateRoutes() {
    return new Promise(resolve => {
      // 1. 从后端获取路由配置
      getRouters().then(res => {
        // 2. 深拷贝路由数据（用于不同场景处理）
        const sdata = JSON.parse(JSON.stringify(res.data));
        // console.log('sdata', sdata)
        const rdata = JSON.parse(JSON.stringify(res.data));
        const defaultData = JSON.parse(JSON.stringify(res.data));

        // 3. 过滤并转换路由组件
        const sidebarRoutes = filterAsyncRouter(sdata);
        const rewriteRoutes = filterAsyncRouter(rdata, false, true);
        const defaultRoutes = filterAsyncRouter(defaultData);

        // 4. 过滤本地定义的动态路由（基于角色/权限）
        const asyncRoutes = filterDynamicRoutes(dynamicRoutes);

        // 5. 将本地动态路由添加到路由实例
        asyncRoutes.forEach(route => {
          router.addRoute(route);
        });

        // 6. 更新状态
        setRoutes(rewriteRoutes);
        setSidebarRouters([...constantRoutes, ...sidebarRoutes]);
        setDefaultRoutes(sidebarRoutes);
        setTopbarRoutes(defaultRoutes);

        resolve(rewriteRoutes);
      });
    });
  }
  // 返回状态和方法（保持原有命名）
  return {
    routes,
    addRoutes,
    defaultRoutes,
    topbarRouters,
    sidebarRouters,
    setRoutes,
    setDefaultRoutes,
    setTopbarRoutes,
    setSidebarRouters,
    generateRoutes
  };

}, {
  persist: true
})

/**
 * 遍历后台传来的路由字符串，转换为组件对象
 * lastRouter：可选参数，默认为 false，表示父路由对象，用于递归调用时传递父路由信息。
 * type：可选参数，默认为 false，用于控制是否对路由的子路由进行特殊处理。
 */
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = () => import("/src/views/system/user/index.vue")
        // route.component = loadView(route.component)
      }
    }

    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}
/**
 * childrenMap：子路由配置数组，每个元素是一个子路由对象。
 * lastRouter：可选参数，默认为 false，表示父路由对象，用于生成子路由的完整路径。
 */
function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    // 如果 lastRouter 存在，则将子路由的路径拼接在父路由路径后面。
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    // 递归处理子路由：如果子路由对象有子路由且组件为 'ParentView'，则递归调用 filterChildren 函数处理子子路由。
    if (el.children && el.children.length && el.component === 'ParentView') {
      // 将处理后的子路由对象添加到 children 数组中：如果子路由对象没有子路由或组件不为 'ParentView'，则直接将其添加到 children 数组中。
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
  let res;
  console.log('modules', modules);
  for (const path in modules) {
    const dir = path.split('views/')[1].split('.vue')[0];
    console.log('Checking dir:', dir, 'against view:', view);
    if (dir === view) {
      console.log('Found match:', path);
      res = () => {
        return modules[path]().catch((error) => {
          console.error('Failed to load component:', error);
          throw error;
        });
      };
    }
  }
  console.log('res:', res);
  return res;
};

export default usePermissionStore
