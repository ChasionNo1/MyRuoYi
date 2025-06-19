<template>
  <div class="dashboard">
    <h2>销售数据仪表盘</h2>
    
    <!-- 仪表盘 -->
    <div class="chart-section">
      <h3>业绩完成率</h3>
      <BaseChart 
        ref="gaugeChart"
        :option="gaugeOption" 
        height="350px"
      />
      <div class="gauge-controls">
        <label for="gaugeValue">目标完成率:</label>
        <input 
          type="range" 
          id="gaugeValue" 
          min="0" 
          max="100" 
          v-model.number="gaugeValue"
        >
        <span>{{ gaugeValue }}%</span>
      </div>
    </div>
  </div>
</template>

<script>
import BaseChart from '@/components/Echarts/BaseChart.vue';
import { ref, watch, onMounted } from 'vue';

export default {
  components: { BaseChart },
  setup() {
    const gaugeValue = ref(0);
    const gaugeChart = ref(null);

    // 仪表盘配置 - 已完成部分蓝色，未完成部分白色
    const gaugeOption = ref({
      tooltip: {
        formatter: '{a} <br/>{b} : {c}%'
      },
      series: [
        {
          name: '业绩完成率',
          type: 'gauge',
          min: 0,
          max: 100,
          animation: true,
          animationDuration: 1500,
          animationEasing: 'cubicOut',
          progress: {
            show: true,
            roundCap: true,
            width: 15
          },
          pointer: {
            show: true,
            length: '60%',
            width: 8,
            itemStyle: {
              color: 'auto'
            }
          },
          axisLine: {
            roundCap: true,
            lineStyle: {
              width: 15,
              // 已完成部分蓝色，未完成部分白色
              color: [
                [gaugeValue.value / 100, '#5470C6'],  // 已完成部分
                [1, '#E6F7FF']                       // 未完成部分
              ]
            }
          },
          axisTick: {
            show: false
          },
          splitLine: {
            distance: 0,
            length: 10,
            lineStyle: {
              width: 2,
              color: '#999'
            }
          },
          axisLabel: {
            distance: 15,
            color: '#999',
            fontSize: 12
          },
          detail: {
            valueAnimation: true,
            fontSize: 30,
            fontWeight: 'bolder',
            formatter: '{value}%',
            color: 'auto'
          },
          data: [
            {
              value: gaugeValue.value,
              name: '完成率'
            }
          ]
        }
      ]
    });

    // 监听仪表盘值变化
    watch(gaugeValue, (newVal) => {
      gaugeOption.value = {
        ...gaugeOption.value,
        series: [{
          ...gaugeOption.value.series[0],
          axisLine: {
            lineStyle: {
              color: [
                [newVal / 100, '#5470C6'],  // 动态更新已完成部分
                [1, '#E6F7FF']             // 未完成部分保持白色
              ]
            }
          },
          data: [{ value: newVal, name: '完成率' }]
        }]
      };
    });

    // 页面加载时触发指针动画
    onMounted(() => {
      setTimeout(() => {
        gaugeValue.value = 75;
      }, 300);
    });

    return {
      gaugeOption,
      gaugeValue,
      gaugeChart
    };
  }
};
</script>

<style scoped>
.gauge-controls {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

input[type="range"] {
  width: 300px;
}

.dashboard {
  padding: 20px;
}

.chart-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
</style>