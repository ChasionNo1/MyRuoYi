// 路由守卫
import router from './router'
import { ElMessage } from 'element-plus'
// 引入进度条插件
import NProgress from 'nprogress'
// 引入进度条的样式文件
import 'nprogress/nprogress.css'
// 引入路径验证相关的工具函数
import { isHttp, isPathMatch } from '@/utils/validate'
// 引入是否重新登录的状态标识
import { isRelogin } from '@/utils/request'
// 引入用户状态管理的组合式 store
import useUserStore from '@/stores/user'
// 引入设置状态管理的组合式 store
import useSettingsStore from '@/stores/settings'
// 引入权限状态管理的组合式 store
import usePermissionStore from '@/stores/permission'
// 配置进度条，不显示旋转的加载图标
NProgress.configure({ showSpinner: false })

// 定义免登录的白名单路径
const whiteList = ['/auth/login', '/auth/register']

// 判断给定的路径是否在白名单中
const isWhiteList = (path) => {
    // 使用 some 方法遍历白名单，检查是否有匹配的路径
    return whiteList.some(pattern => isPathMatch(pattern, path))
}


// 全局前置守卫，在路由跳转前执行
router.beforeEach(async (to, from, next) => {
    // 开始显示进度条
    NProgress.start()
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()
    // 检查本地是否存在 token
    if (userStore.token) {
        // 如果目标路由有 meta.title 属性，则设置页面标题
        to.meta.title && useSettingsStore().setTitle(to.meta.title)

        // 如果用户已经登录，且目标路径是登录页
        if (to.path === '/auth/login') {
            next({ path: '/' })
            NProgress.done()
            // 如果目标路径在白名单中
        } else if (isWhiteList(to.path)) {
            // 直接放行
            next()
        } else {
            // 其他情况
            // 检查用户角色列表是否为空
            if (userStore.roles.length === 0) {
                // 显示重新登录
                isRelogin.show = true
                // 调用用户状态管理中的 getInfo 方法获取用户信息
                try {
                    await userStore.getInfo()
                    // 隐藏重新登录提示
                    isRelogin.show = false
                    // 调用权限状态管理中的 generateRoutes 方法生成可访问的路由表
                    const routeResult = await permissionStore.generateRoutes()
                    // 遍历可访问的路由表
                    routeResult.forEach(route => {
                        // 如果路由路径不是 http 协议的
                        if (!isHttp(route.path)) {
                            // 动态添加可访问的路由到路由实例中
                            router.addRoute(route)
                        }
                    })
                    // 使用 hack 方法确保路由添加完成后再跳转
                    next({ ...to, replace: true })
                } catch (error) {
                    // 如果获取用户信息失败，调用用户状态管理中的 logOut 方法退出登录
                    // await useUserStore.logout()
                    ElMessage.error(error)
                    // 重定向到首页
                    next({ path: '/' })
                }
            } else {
                // 角色列表不为空，说明前面已经走过一遍了
                // 直接放行
                next()
            }
        }
    } else {
        // 没有token
        // 如果目标路径在白名单中
        if (isWhiteList(to.path)) {
            // 直接放行
            next()
        } else {
            // 重定向到登录页，并携带当前路径作为重定向参数
            next(`/auth/login?redirect=${to.fullPath}`)
            // 完成进度条
            NProgress.done()
        }
    }
})

// 全局后置守卫，在路由跳转后执行
router.afterEach(() => {
    // 完成进度条
    NProgress.done()
})