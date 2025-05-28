import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css'
// svg图标
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
import elementIcons from '@/components/SvgIcon/svgicon'
// 引入插件
import piniaPluginPersist from 'pinia-plugin-persistedstate'; 
import './permission' // permission control

import '@/assets/styles/index.scss' // global css

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersist);
app.use(pinia)
app.use(router)
app.use(elementIcons)
app.component('svg-icon', SvgIcon)

app.mount('#app')
