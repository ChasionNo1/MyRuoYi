<script setup>
defineOptions({})
</script>

<template>
    <!-- 这里铺设登录和注册的背景 -->
     <div class="back-layout">
        <router-view></router-view>
        <!-- 底部 -->
          <div class="el-login-footer">
      <span>Copyright © 2018-2025 xxx.com All Rights Reserved.</span>
    </div>
     </div>
  
</template>

<style scoped>
.back-layout {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../../../assets/images/login-background.jpg");
  background-size: cover;  
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
</style>