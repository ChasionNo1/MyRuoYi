import { createApp, watch, nextTick  } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import locale from 'element-plus/es/locale/lang/zh-cn' // 中文语言包

import App from './App.vue'
import router from './router'

// svg图标
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
import elementIcons from '@/components/SvgIcon/svgicon'

// 引入持久化插件
import piniaPluginPersist from 'pinia-plugin-persistedstate'; 

import './permission' // 权限控制
import '@/assets/styles/index.scss' // 全局样式

const app = createApp(App)
const pinia = createPinia()

// 注册 Pinia 持久化插件
pinia.use(piniaPluginPersist);

// 挂载 Pinia 和路由（确保顺序正确）
app.use(pinia)
app.use(router)

// 导入 Pinia Store
import { useAppStore } from './stores/app' // 假设 Store 文件路径为 src/stores/app.js
const appStore = useAppStore()

// ✅ 先安装 Element Plus，确保 $ELEMENT 已创建
app.use(ElementPlus, {
  locale,
  size: appStore.size // 初始值（此时 $ELEMENT 已存在）
})

watch(
  () => appStore.size,
  async (newSize) => {
    await nextTick() // 等待 DOM 更新和插件初始化完成
    if (app.config.globalProperties.$ELEMENT) {
      app.config.globalProperties.$ELEMENT.size = newSize
    }
  },
  { immediate: true }
)

// 注册全局组件和插件
app.use(elementIcons)
app.component('svg-icon', SvgIcon)

app.mount('#app')