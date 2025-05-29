<script setup>
import { isExternal } from '@/utils/validate'
import AppLink from './Link'
import { getNormalPath } from '@/utils/ruoyi'
import {ref} from 'vue'

const props = defineProps({
  // route object
  item: {
    type: Object,
    required: true
  },
  // 是否为嵌套菜单
  isNest: {
    type: Boolean,
    default: false
  },
  basePath: {
    type: String,
    default: ''
  }
})

const onlyOneChild = ref({})

// 判断是否单子菜单项
function hasOneShowingChild(children = [], parent) {
  // 该item项如果没有子项，这里就初始化一个
  if (!children) {
    children = []
  }
  // 过滤子项的内容，如果children是空list，就不用过滤
  const showingChildren = children.filter(item => {
    if (item.hidden) {
      // 过滤掉不显示
      return false
    }
    // 临时存储当前子项到 onlyOneChild 引用
    // （注意：这里会在遍历过程中不断覆盖，最终存储的是最后一个可见子项）
    // 这里是只有一个子项的情况，所以说，如果这个showingChildren的长度不是1或者0，那么就返回false，不是这个显示逻辑了
    // 因此最终存储的可见子项就是我们要显示的内容
    onlyOneChild.value = item
    // 保留非隐藏项
    return true
  })

  // 3. 单子项逻辑：当只有一个可见子项时
  if (showingChildren.length === 1) {
    // 返回 true 表示应显示为单子菜单
    return true
  }

  // 4. 无子项逻辑：当没有可见子项时
  if (showingChildren.length === 0) {
    // 4.1 创建特殊路由对象：
    //   - 复制父路由属性
    //   - 清空路径 (path: '')
    //   - 标记无子项 (noShowingChildren: true)
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
    //返回 true 表示应显示父级本身
    return true
  }
// 5. 多子项情况：当有多个可见子项时
// 返回 false 表示需要显示为带下拉的菜单
  return false
}

function resolvePath(routePath, routeQuery) {
  // 1. 检查当前路径是否是外部链接
  if (isExternal(routePath)) {
    return routePath  // 直接返回原始外部路径（如 https://example.com）
  }
  
  // 2. 检查基础路径是否是外部链接
  if (isExternal(props.basePath)) {
    return props.basePath  // 返回基础路径（当整个菜单项是外部链接时）
  }
  
  // 3. 处理带查询参数的路由
  if (routeQuery) {
    // 3.1 解析查询参数字符串为对象
    let query = JSON.parse(routeQuery)
    
    // 3.2 返回包含路径和查询参数的对象
    return { 
      path: getNormalPath(props.basePath + '/' + routePath), // 拼接并规范化路径
      query: query  // 添加查询参数
    }
  }
  
  // 4. 默认情况：返回规范化后的基础路径+当前路径
  return getNormalPath(props.basePath + '/' + routePath)
}

function hasTitle(title){
  if (title.length > 5) {
    return title
  } else {
    return ""
  }
}
</script>

<template>
  <!-- 注释：每个菜单项由外层循环处理，这里只处理单个菜单项的显示 -->
  
  <!-- 1. 条件渲染：只有当菜单项不隐藏时才渲染 -->
  <div v-if="!item.hidden">
    
    <!-- 2. 单子菜单项渲染 
     满足三个条件时渲染单子菜单项：
    只有一个可见子项（或没有子项时显示父项）
    该子项没有自己的子项 或 标记为无显示子项
    父项不要求总是显示为子菜单
    -->
    <template v-if="hasOneShowingChild(item.children, item) && 
                   (!onlyOneChild.children || onlyOneChild.noShowingChildren) && 
                   !item.alwaysShow">
      <!-- 3. 路由链接组件 当唯一子项有 meta 信息时渲染
使用 resolvePath 解析完整路由路径和查询参数 -->
      <app-link v-if="onlyOneChild.meta" 
                :to="resolvePath(onlyOneChild.path, onlyOneChild.query)">
        
        <!-- 4. Element Plus 菜单项 
         index 属性是菜单项的唯一标识（使用解析后的路径）
动态添加 submenu-title-noDropdown 类（当不是嵌套菜单时）
          -->
        <el-menu-item :index="resolvePath(onlyOneChild.path)" 
                      :class="{ 'submenu-title-noDropdown': !isNest }">
          
          <!-- 5. 菜单图标 优先使用子项的图标
子项无图标时使用父项的图标 -->
          <svg-icon :icon-class="onlyOneChild.meta.icon || (item.meta && item.meta.icon)"/>
          
          <!-- 6. 菜单标题
           显示子项的标题
title 属性用于长标题提示（超过5字符显示完整标题）
            -->
          <template #title>
            <span class="menu-title" 
                  :title="hasTitle(onlyOneChild.meta.title)">
              {{ onlyOneChild.meta.title }}
            </span>
          </template>
        </el-menu-item>
      </app-link>
    </template>

    <!-- 7. 多子菜单项渲染
     当不满足单子项条件时渲染子菜单
index 使用父项路径作为标识
teleported 确保子菜单弹出层不受父组件样式影响
      -->
    <el-sub-menu v-else 
                 ref="subMenu" 
                 :index="resolvePath(item.path)" 
                 teleported>
      
      <!-- 8. 子菜单标题 -->
      <template v-if="item.meta" #title>
        <!-- 父菜单图标 -->
        <svg-icon :icon-class="item.meta && item.meta.icon" />
        <!-- 父菜单标题 -->
        <span class="menu-title" 
              :title="hasTitle(item.meta.title)">
          {{ item.meta.title }}
        </span>
      </template>

      <!-- 9. 递归渲染子菜单项 -->
      <sidebar-item
        v-for="(child, index) in item.children"
        :key="child.path + index"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-sub-menu>
  </div>
</template>

<style scoped>
</style>