<template>
  <div ref="chartRef" :style="{ width, height }"></div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import echarts from '@/utils/echarts'; // 导入按需配置的 echarts

export default {
  name: 'BaseChart',
  props: {
    // 图表配置项
    option: {
      type: Object,
      required: true,
      default: () => ({})
    },
    // 宽度
    width: {
      type: String,
      default: '100%'
    },
    // 高度
    height: {
      type: String,
      default: '400px'
    },
    // 主题
    theme: {
      type: String,
      default: null
    },
    // 是否自动响应尺寸变化
    autoresize: {
      type: Boolean,
      default: true
    }
  },
  setup(props) {
    const chartRef = ref(null);
    let chartInstance = null;
    let resizeObserver = null;

    // 初始化图表
    const initChart = () => {
      if (!chartRef.value) return;
      
      // 如果已有实例则销毁
      if (chartInstance) {
        chartInstance.dispose();
      }
      
      // 创建新实例
      chartInstance = echarts.init(chartRef.value, props.theme);
      chartInstance.setOption(props.option);
      
      // 设置自动响应
      setupAutoResize();
    };

    // 设置自动响应
    const setupAutoResize = () => {
      if (!props.autoresize) return;
      
      // 使用 ResizeObserver 监听容器尺寸变化
      if (window.ResizeObserver) {
        resizeObserver = new ResizeObserver(() => {
          chartInstance && chartInstance.resize();
        });
        resizeObserver.observe(chartRef.value);
      } 
      // 兼容不支持 ResizeObserver 的浏览器
      else {
        window.addEventListener('resize', handleResize);
      }
    };

    // 窗口大小变化处理
    const handleResize = () => {
      chartInstance && chartInstance.resize();
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
    onMounted(() => {
      initChart();
    });

    // 组件卸载前清理
    onBeforeUnmount(() => {
      if (chartInstance) {
        chartInstance.dispose();
        chartInstance = null;
      }
      if (resizeObserver) {
        resizeObserver.disconnect();
      }
      window.removeEventListener('resize', handleResize);
    });

    // 监听 option 变化
    watch(() => props.option, () => {
      updateChart();
    }, { deep: true });

    // 监听尺寸变化
    watch(() => [props.width, props.height], () => {
      chartInstance && chartInstance.resize();
    });

    return {
      chartRef
    };
  }
};
</script>