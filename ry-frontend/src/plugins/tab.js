import useTagsViewStore from '@/stores/tagsView'
import router from '@/router'

export default {
  // 刷新当前tab页签
  async refreshPage(obj) {
    // 从 Vue Router 的当前路由中解构出三个属性：
    // path：当前路由路径
    // query：URL 查询参数对象
    // matched：当前匹配的路由记录数组（包含嵌套路由）
    const { path, query, matched } = router.currentRoute.value
    // 检查是否未传入 obj 参数（即需要自动确定要刷新的组件）
    if (obj === undefined) {
      // 遍历当前匹配的所有路由记录（matched 数组）
      matched.forEach((m) => {
        // 是否有组件配置
        // 是否有默认组件
        // 默认组件是否有名称
        if (m.components && m.components.default && m.components.default.name) {
          // 排除布局组件（名称是 'Layout' 或 'ParentView' 的组件）
          if (!['Layout', 'ParentView'].includes(m.components.default.name)) {
            /**
             * 构造刷新参数对象：
              name：组件名称（用于缓存标识）
              path：当前路径
              query：当前查询参数
              （注意：此处会覆盖外层 obj 参数，建议改用局部变量）
             */
            obj = { name: m.components.default.name, path: path, query: query }
          }
        }
      })
    }
    // 到这obj已经定义好了
  //     调用标签页管理 store 的 delCachedView 方法：
      // 清除指定组件的缓存（用于 keep-alive）
      // 返回 Promise 对象
        await useTagsViewStore().delCachedView(obj)
    const { path: path_1, query: query_1 } = obj
    // 刷新操作
    router.replace({
      path: '/redirect' + path_1,
      query: query_1
    })
  },
  // 关闭当前tab页签，打开新页签
  closeOpenPage(obj) {
    useTagsViewStore().delView(router.currentRoute.value)
    if (obj !== undefined) {
      return router.push(obj)
    }
  },
  // 关闭指定tab页签
  async closePage(obj) {
    if (obj === undefined) {
      const { visitedViews } = await useTagsViewStore().delView(router.currentRoute.value)
      const latestView = visitedViews.slice(-1)[0]
      if (latestView) {
        return router.push(latestView.fullPath)
      }
      return await router.push('/')
    }
    return useTagsViewStore().delView(obj)
  },
  // 关闭所有tab页签
  closeAllPage() {
    return useTagsViewStore().delAllViews()
  },
  // 关闭左侧tab页签
  closeLeftPage(obj) {
    // 调用删除方法
    return useTagsViewStore().delLeftTags(obj || router.currentRoute.value)
  },
  // 关闭右侧tab页签
  closeRightPage(obj) {
    return useTagsViewStore().delRightTags(obj || router.currentRoute.value)
  },
  // 关闭其他tab页签
  closeOtherPage(obj) {
    return useTagsViewStore().delOthersViews(obj || router.currentRoute.value)
  },
  // 打开tab页签
  openPage(title, url, params) {
    const obj = { path: url, meta: { title: title } }
    useTagsViewStore().addView(obj)
    return router.push({ path: url, query: params })
  },
  // 修改tab页签
  updatePage(obj) {
    return useTagsViewStore().updateVisitedView(obj)
  }
}
