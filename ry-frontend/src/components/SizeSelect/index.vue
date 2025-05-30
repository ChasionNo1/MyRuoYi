<template>
  <div>
    <el-dropdown trigger="click" @command="handleSetSize">
      <div class="size-icon--style">
        <svg-icon class-name="size-icon" icon-class="size" />
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item 
            v-for="item of sizeOptions" 
            :key="item.value" 
            :disabled="size === item.value" 
            :command="item.value"
          >
            {{ item.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import useAppStore from "@/stores/app";
import { computed, ref } from "vue";
import { ElLoading } from "element-plus"; // 引入 Loading 服务

const appStore = useAppStore();
const size = computed(() => appStore.size);
const sizeOptions = ref([
  { label: "较大", value: "large" },
  { label: "默认", value: "default" },
  { label: "稍小", value: "small" },
]);

function handleSetSize(selectedSize) {
  // 创建 Loading 实例
  const loading = ElLoading.service({
    lock: true,
    text: "正在设置布局大小，请稍候...",
    background: "rgba(0, 0, 0, 0.15)",
  });

  appStore.setSize(selectedSize);
  
  // 延迟刷新并关闭 Loading
  setTimeout(() => {
    loading.close(); // 关闭 Loading
    window.location.reload();
  }, 1000);
}
</script>
<style lang='scss' scoped>
.size-icon--style {
  font-size: 18px;
  line-height: 50px;
  padding-right: 7px;
}
</style>