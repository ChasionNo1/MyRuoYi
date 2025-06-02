<template>
  <el-scrollbar
    ref="scrollContainer"
    :vertical="false"
    class="scroll-container"
    @wheel.prevent="handleScroll"
  >
    <slot />
  </el-scrollbar>
</template>

<script setup>
import useTagsViewStore from '@/stores/tagsView'
import {ref, getCurrentInstance, onMounted, onBeforeUnmount, computed} from 'vue'

// 标签页之间的间距（像素）
const tagAndTagSpacing = ref(4)
// 获取 Vue 实例（仅在 setup 中需要）
const { proxy } = getCurrentInstance()
// 滚动内容的引用
const scrollWrapper = computed(() => proxy.$refs.scrollContainer.$refs.wrapRef)

onMounted(() => {
  scrollWrapper.value.addEventListener('scroll', emitScroll, true)
})

onBeforeUnmount(() => {
  scrollWrapper.value.removeEventListener('scroll', emitScroll)
})

function handleScroll(e) {
  const eventDelta = e.wheelDelta || -e.deltaY * 40
  const $scrollWrapper = scrollWrapper.value
  $scrollWrapper.scrollLeft = $scrollWrapper.scrollLeft + eventDelta / 4
}

const emits = defineEmits()
const emitScroll = () => {
  emits('scroll')
}

const tagsViewStore = useTagsViewStore()
const visitedViews = computed(() => tagsViewStore.visitedViews)

// 移动当前tags处
// offsetLeft：元素相对于父容器的左偏移量，用于确定标签页在滚动容器中的位置。
// scrollWidth：滚动内容的总宽度（包含不可见部分），用于定位最后一个标签页。
// scrollLeft：滚动条的水平偏移量，通过设置该值实现滚动效果。
function moveToTarget(currentTag) {
  // 滚动容器的 DOM 元素
  // $ 并不是特殊语法或关键字，而是一种约定俗成的命名习惯，用于标识 Vue 实例属性
  // 目的是将框架内置属性与用户自定义属性区分开
  // $refs 是 Vue 实例的一个对象属性，用于引用模板中的 DOM 元素或子组件
  const $container = proxy.$refs.scrollContainer.$el
  // 容器可视宽度
  const $containerWidth = $container.offsetWidth
  // 滚动内容的 DOM 元素（可能是一个包裹标签页的 div）
  const $scrollWrapper = scrollWrapper.value

  let firstTag = null
  let lastTag = null

  // find first tag and last tag
  if (visitedViews.value.length > 0) {
    firstTag = visitedViews.value[0]
    lastTag = visitedViews.value[visitedViews.value.length - 1]
  }

  if (firstTag === currentTag) {
    // 若为第一个标签页，滚动到最左侧
    $scrollWrapper.scrollLeft = 0
  } else if (lastTag === currentTag) {
    // 若为最后一个标签页，滚动到最右侧（内容总宽度 - 容器宽度）
    $scrollWrapper.scrollLeft = $scrollWrapper.scrollWidth - $containerWidth
  } else {
    // 所有标签页 DOM 元素
    const tagListDom = document.getElementsByClassName('tags-view-item')
    // 当前标签页索引
    const currentIndex = visitedViews.value.findIndex(item => item === currentTag)
    // 前后相邻标签页的 DOM 元素
    let prevTag = null
    let nextTag = null
    for (const k in tagListDom) {
      // HTMLCollection 对象自带 length 属性（表示元素数量），但它不是 DOM 元素，需要排除
      // 确保 k 是 tagListDom 自身的属性，而非从原型链继承的属性（如 toString、hasOwnProperty 等）。
      if (k !== 'length' && Object.hasOwnProperty.call(tagListDom, k)) {
        // 当前遍历到的 DOM 元素，标签页的 data-path 值（如 '/user'）匹配前一个标签页的路径
        if (tagListDom[k].dataset.path === visitedViews.value[currentIndex - 1].path) {
          // 记录前一个标签页的 DOM 元素
          prevTag = tagListDom[k]
        }
        // 同理，是后一个
        if (tagListDom[k].dataset.path === visitedViews.value[currentIndex + 1].path) {
          nextTag = tagListDom[k]
        }
      }
    }

    // 下一个标签之后的起始位置
    // 下一个标签页右侧 + 标签间距，用于判断是否需要向右滚动以显示下一个标签页。
    // 下一个标签的左侧 = 左侧起始位置+元素宽度+间隙
    const afterNextTagOffsetLeft = nextTag.offsetLeft + nextTag.offsetWidth + tagAndTagSpacing.value

    // 上一个标签之前（加一个间隙）的起始位置
    // 前一个标签页左侧 - 标签间距，用于判断是否需要向左滚动以显示前一个标签页。
    const beforePrevTagOffsetLeft = prevTag.offsetLeft - tagAndTagSpacing.value
    // 判断是否需要向右滚动（当前标签页右侧被遮挡）
    if (afterNextTagOffsetLeft > $scrollWrapper.scrollLeft + $containerWidth) {
      // 若下一个标签页右侧超出容器可视区域，滚动到下一个标签页右侧边界与容器右侧对齐。
      $scrollWrapper.scrollLeft = afterNextTagOffsetLeft - $containerWidth
      // 判断是否需要向左滚动（当前标签页左侧被遮挡）
    } else if (beforePrevTagOffsetLeft < $scrollWrapper.scrollLeft) {
      // 若前一个标签页左侧超出容器可视区域，滚动到前一个标签页左侧边界与容器左侧对齐。
      $scrollWrapper.scrollLeft = beforePrevTagOffsetLeft
    }
  }
}

defineExpose({
  moveToTarget,
})
</script>

<style lang='scss' scoped>
.scroll-container {
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  width: 100%;
  :deep(.el-scrollbar__bar) {
    bottom: 0px;
  }
  :deep(.el-scrollbar__wrap) {
    height: 39px;
  }
}
</style>