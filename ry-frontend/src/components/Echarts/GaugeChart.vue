<script setup>
import BaseChart from "@/components/Echarts/BaseChart.vue";
import { ref, watch, computed } from "vue";

const props = defineProps({
  monitor: {
    type: Object,
    required: true,
    default: () => ({
      title: "指标",
      data: {}
    })
  }
});

// 计算使用率值
const gaugeValue = computed(() => {
  // 根据监控类型选择不同的使用率字段
  switch (props.monitor.title) {
    case 'cpu':
      return parseUsage(props.monitor.data.cpuUsage);
    case 'memory':
      return parseUsage(props.monitor.data.memoryUsage);
    case 'disk':
      return parseUsage(props.monitor.data.diskUsage);
    case 'jvm':
      // 使用新的 heapUsage 字段
      return parseUsage(props.monitor.data.heapUsage);
    default:
      { const usageKey = Object.keys(props.monitor.data).find(key => 
        key.toLowerCase().includes('usage')
      );
      return usageKey ? parseUsage(props.monitor.data[usageKey]) : 0; }
  }
});

// 解析使用率字符串
function parseUsage(usageStr) {
  if (!usageStr) return 0;
  const numStr = usageStr.replace('%', '').trim();
  return parseFloat(numStr) || 0;
}

// 创建过滤后的数据对象（排除使用率字段）
const filteredData = computed(() => {
  const data = {...props.monitor.data};
  
  // 根据监控类型移除特定的使用率字段
  switch (props.monitor.title) {
    case 'cpu':
      delete data.cpuUsage;
      break;
    case 'memory':
      delete data.memoryUsage;
      break;
    case 'disk':
      delete data.diskUsage;
      break;
    case 'jvm':
      // 移除新的 heapUsage 字段
      delete data.heapUsage;
      break;
    default:
      // 移除所有包含"usage"的字段
      Object.keys(data).forEach(key => {
        if (key.toLowerCase().includes('usage')) {
          delete data[key];
        }
      });
  }
  
  return data;
});

const gaugeChart = ref(null);
const gaugeOption = ref(getBaseOption());

// 创建基础配置
function getBaseOption() {
  return {
    tooltip: {
      formatter: "{a} <br/>{b} : {c}%",
    },
    series: [
      {
        name: "",
        type: "gauge",
        min: 0,
        max: 100,
        animation: true,
        animationDuration: 1500,
        animationEasing: "cubicOut",
        progress: {
          show: true,
          roundCap: true,
          width: 7,
        },
        pointer: {
          show: true,
          length: "70%",
          width: 5,
          color: "auto",
        },
        axisLine: {
          show: true,
          lineStyle: {
            width: 7,
            color: [
              [gaugeValue.value / 100, "#5470C6"],
              [1, "#E6F7FF"],
            ],
          },
        },
        axisTick: {
          show: false,
        },
        splitLine: {
          distance: 0,
          length: 10,
          lineStyle: {
            width: 1,
            color: "#999",
          },
        },
        axisLabel: {
          distance: 15,
          color: "#999",
          fontSize: 12,
        },
        detail: {
          valueAnimation: true,
          fontSize: 18,
          fontWeight: "bolder",
          formatter: "{value}%",
          color: "auto",
        },
        data: [
          {
            value: gaugeValue.value,
            name: "占用率",
          },
        ],
      },
    ],
  };
}

// 监听monitor变化
watch(() => props.monitor, (newVal) => {
  updateGaugeOption();
}, { deep: true, immediate: true });

// 更新图表配置
function updateGaugeOption() {
  const value = gaugeValue.value;
  
  gaugeOption.value = {
    ...gaugeOption.value,
    series: [
      {
        ...gaugeOption.value.series[0],
        name: props.monitor.title,
        axisLine: {
          lineStyle: {
            color: [
              [value / 100, "#5470C6"],
              [1, "#E6F7FF"],
            ],
          },
        },
        data: [
          {
            value: value,
            name: "占用率",
          },
        ],
      },
    ],
  };
}
</script>

<template>
  <div class="chart-section">
    <h3>{{ monitor.title }} 监控</h3>
    <BaseChart ref="gaugeChart" :option="gaugeOption" height="300px" />
    
    <div class="monitor-details">
      <div v-for="(value, key) in filteredData" :key="key" class="detail-item">
        <span class="detail-key">{{ key }}:</span>
        <span class="detail-value">{{ value }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chart-section {
  text-align: center;
  padding: 15px;
}

h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1.2rem;
}

.monitor-details {
  margin-top: 20px;
  text-align: left;
  background: #f8f9fa;
  border-radius: 6px;
  padding: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid #eee;
  font-size: 0.9rem;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-key {
  font-weight: 600;
  color: #555;
}

.detail-value {
  color: #2c3e50;
  font-weight: 500;
}
</style>