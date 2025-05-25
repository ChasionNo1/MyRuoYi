<script setup>
import useSettingsStore from '@/stores/settings'
import {computed, ref} from 'vue'

defineOptions({
  name: 'NarBar'
})
const settingsStore = useSettingsStore()
const btnName1 = computed(() => {return settingsStore.sideStatus === 'open' ? '折叠' : '打开'})
const btnName2 = computed(() => {return settingsStore.sideStatus === 'open' ? '关闭' : '打开'})

// 按钮1控制缩小和放大
const show1 = () => {
  // 先查看当前的侧边栏是什么状态
  let status = settingsStore.sideStatus
  if (status === 'open') {
      settingsStore.changeSetting({key: 'sideStatus', value:'collapse'})
      btnName1.value = '折叠'
  }else {
      settingsStore.changeSetting({key: 'sideStatus', value:'open'})
      btnName1.value = '打开'
  }
}
// 按钮2控制关闭和展开
const show2 = () => {
  let status = settingsStore.sideStatus
  if (status === 'open') {
      settingsStore.changeSetting({key: 'sideStatus', value:'close'})
      btnName1.value = '关闭'
  }else {
      settingsStore.changeSetting({key: 'sideStatus', value:'open'})
      btnName1.value = '打开'
  }
}
const model = computed(() => { return settingsStore.sideTheme === 'theme-dark' ? '浅色' : '暗黑'})
const changeTheme = () => {
  settingsStore.toggleTheme()
  settingsStore.changeSetting({key: 'sideTheme', 
  value: settingsStore.sideTheme === 'theme-dark' ? 'theme-light' : 'theme-dark'})
}

</script>

<template>
  <div >
    <h1>nav bar</h1>
    <el-button @click="show1">{{ btnName1 }}</el-button>
    <el-button @click="show2">{{ btnName2 }}</el-button>
    <el-button @click="changeTheme">{{model}}模式</el-button>

  </div>
</template>

<style scoped>
h1 {
  display: inline;
}
</style>