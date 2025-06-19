<script setup>
import { ref } from 'vue'
import { getMonitorData } from '@/api/api'
import GaugeChart from '@/components/Echarts/GaugeChart.vue'
import { onMounted } from 'vue'

const monitorItems = ref([])

onMounted(() => {
  getMonitorData().then(res => {
    if (res.code === 200) {
      // 直接使用后端返回的数据结构
      monitorItems.value = res.data
    }
  })
})
</script>

<template>
  <div class="dashboard-container">
    <div v-for="(item, index) in monitorItems" :key="index" class="dashboard-item">
      <!-- 直接传递整个监控项对象 -->
      <GaugeChart :monitor="item" />
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px;
}

.dashboard-item {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 15px;
  transition: transform 0.3s ease;
}

.dashboard-item:hover {
  transform: translateY(-5px);
}
</style>