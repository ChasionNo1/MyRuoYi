<script setup>
import { ref } from "vue";
import { User, Lock } from '@element-plus/icons-vue'
import useUserStore from '@/stores/user'
import { ElMessage } from "element-plus"
import { useRouter } from "vue-router";
defineOptions({
  name: 'LoginPage'
})
const loginRef = ref()
const userStore = useUserStore()
const router = useRouter()
const loginForm = ref({
  username: "chasion",
  password: "cr199725..",
  rememberMe: false,
  code: "",
  uuid: ""
})
const title = ref('登录')
const captchaEnabled = ref(false)

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

// v1: 简单登录
const handleLogin = async () => {
  try {
    await loginRef.value.validate()
    const params = {
      username: loginForm.value.username,
      password: loginForm.value.password
    }
    const result = await userStore.login(params)
    if (result.code === 200) {
      ElMessage({
        message: '登录成功',
        type: 'success',
      })
      userStore.token = result.data
      router.push('/')
    }
  } catch (error) {
    ElMessage(error)
  }
}
</script>

<template>
  <!-- 这里写登录表单 -->
  <div class="login">
    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ title }}</h3>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" type="text" size="large" auto-complete="off" placeholder="账号">
          <template #prefix>
            <el-icon class="el-input__icon">
              <User />
            </el-icon>
          </template>
        </el-input>
      </el-form-item>
      <!--    -->
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" size="large" auto-complete="off" placeholder="密码"
          @keyup.enter="handleLogin">
          <template #prefix><el-icon>
              <Lock />
            </el-icon></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input v-model="loginForm.code" size="large" auto-complete="off" placeholder="验证码" style="width: 63%"
          @keyup.enter="handleLogin">
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img" />
        </div>
      </el-form-item>
      <div class="verifyWrapper">
        <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        <div class="forgot-password"><a>忘记密码</a></div>
      </div>

      <!--           @click.prevent="handleLogin" -->
      <el-form-item style="width:100%;">
        <el-button :loading="loading" size="large" type="primary" style="width:100%;" @click.prevent="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div>
          还没有账号？<router-link class="link-type" :to="'/auth/register'">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>

  </div>

</template>

<style scoped>
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  z-index: 1;

  .el-input {
    height: 40px;

    input {
      height: 40px;
    }
  }

  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

.login-code {
  width: 33%;
  height: 40px;
  float: right;

  img {
    cursor: pointer;
    vertical-align: middle;
  }
}

.verifyWrapper {
  display: flex;
  justify-content: space-between;
  /* 让元素两端对齐 */
  align-items: center;
  /* 垂直方向居中对齐 */
  margin-bottom: 15px;
  /* 移除内部元素的 margin，统一设置外部 margin */
}

.verifyWrapper .forgot-password {
  margin: 0;
  /* 移除默认的 margin */
}

.verifyWrapper .forgot-password a {
  color: #409EFF;
  /* 设置链接颜色，与 Element UI 主题色保持一致 */
  text-decoration: none;
  /* 去掉下划线 */
}

.verifyWrapper .forgot-password a:hover {
  text-decoration: underline;
  /* 鼠标悬停时显示下划线 */
}

.login-code-img {
  height: 40px;
  padding-left: 12px;
}
</style>