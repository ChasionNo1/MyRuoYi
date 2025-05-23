/**
 * 后台管理系统设置存储模块分析
该代码是基于 Pinia 的设置存储模块，用于管理系统的主题、布局、用户偏好等配置，
结合 vueuse 实现暗黑模式切换，并通过 localStorage 实现持久化存储。
 * 
 */
import { defineStore } from 'pinia'
import defaultSettings from '@/settings'
import { useDark, useToggle } from '@vueuse/core'
import { useDynamicTitle } from '@/utils/dynamicTitle'

const isDark = useDark()
const toggleDark = useToggle(isDark)

const {
    sideTheme: defaultSideTheme,
    showSettings: defaultShowSettings,
    topNav: defaultTopNav,
    tagsView: defaultTagsView,
    fixedHeader: defaultFixedHeader,
    sidebarLogo: defaultSidebarLogo,
    dynamicTitle: defaultDynamicTitle
} = defaultSettings

const useSettingsStore = defineStore(
    'settings', {
    state: () => ({
        title: '',
        theme: '#409EFF',
        sideTheme: defaultSideTheme,
        showSettings: defaultShowSettings,
        topNav: defaultTopNav,
        tagsView: defaultTagsView,
        fixedHeader: defaultFixedHeader,
        sidebarLogo: defaultSidebarLogo,
        dynamicTitle: defaultDynamicTitle,
        isDark: isDark.value
    }),
    actions: {
        // 修改布局设置
        changeSetting(data) {
            const { key, value } = data
            if (key in this.$state) {
                this[key] = value
            }
        },
        // 设置网页标题
        setTitle(title) {
            this.title = title
            useDynamicTitle()
        },
        // 切换暗黑模式
        toggleTheme() {
            this.isDark = !this.isDark
            toggleDark()
        }
    }
}, {
    persist: {
        key: 'settings-store',
        storage: localStorage,
        paths: [
            'theme',
            'sideTheme',
            'topNav',
            'tagsView',
            'fixedHeader',
            'sidebarLogo',
            'dynamicTitle',
            'isDark'
        ],
        afterRestore: (ctx) => {
            // 持久化状态恢复后同步暗黑模式
            toggleDark(ctx.store.isDark)
        }

    }
}
)
export default useSettingsStore
