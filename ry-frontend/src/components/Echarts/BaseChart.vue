<template>
  <div ref="chartRef" :style="{ width, height }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import echarts from '@/utils/echarts';

const props = defineProps({
  option: {
    type: Object,
    required: true,
    default: () => ({})
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '300px'
  },
  theme: {
    type: String,
    default: null
  },
  autoresize: {
    type: Boolean,
    default: true
  }
});

const chartRef = ref(null);
let chartInstance = null;
let resizeObserver = null;

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;
  
  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose();
  }
  
  // 创建新实例
  chartInstance = echarts.init(chartRef.value, props.theme);
  chartInstance.setOption(props.option);
  
  // 设置自动响应
  setupAutoResize();
};

// 设置自适应
const setupAutoResize = () => {
  if (!props.autoresize) return;
  
  // 使用 ResizeObserver 监听容器变化
  if (window.ResizeObserver) {
    resizeObserver = new ResizeObserver(() => {
      chartInstance?.resize();
    });
    resizeObserver.observe(chartRef.value);
  } 
  // 降级方案：监听窗口 resize
  else {
    window.addEventListener('resize', handleResize);
  }
};

// 窗口大小变化处理
const handleResize = () => {
  chartInstance?.resize();
};

// 更新图表
const updateChart = () => {
  if (!chartInstance) {
    initChart();
    return;
  }
  chartInstance.setOption(props.option);
};

// 组件挂载时初始化
onMounted(initChart);

// 组件卸载前清理
onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
  }
  window.removeEventListener('resize', handleResize);
});

// 监听 option 变化
watch(
  () => props.option,
  () => updateChart(),
  { deep: true }
);

// 监听尺寸变化
watch(
  () => [props.width, props.height],
  () => chartInstance?.resize()
);
</script>