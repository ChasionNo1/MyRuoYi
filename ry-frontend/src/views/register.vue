<script setup>
import {ref} from 'vue'
import { ElMessage } from "element-plus"
import useUserStore from '@/stores/user'
import { useRouter } from "vue-router";
defineOptions({
  name: 'RegisterPage'
})
// 获取路由实例
const router = useRouter();
const title = ref('注册')
const registerRef = ref()
const userStore = useUserStore()
// 注册的表单数据
const registerFormStatus = ref({
  username: "",
  password: "",
  confirmPassword: "",
  email: "",
  code: "",
  uuid: ""
})
const equalToPassword = (rule, value, callback) => {
  if (registerFormStatus.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}
// 邮箱校验规则
const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  if (!emailRegex.test(value)) {
    callback(new Error('请输入有效的邮箱地址'));
  } else {
    callback();
  }
};

const registerRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" },
    { min: 2, max: 20, message: "用户账号长度必须介于 2 和 20 之间", trigger: "blur" }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入您的密码" },
    { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" },
    { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\ |", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }],
  email: [
    { required: true, trigger: "blur", message: "请输入您的邮箱" },
    { validator: validateEmail, trigger: "blur" }
  ]
}

/**
 * 发送邮箱验证码
 * 
 */
const btnAvailable = ref(true)
const countdown = ref(0)
const sendMailCode = async() => {
  try {
    // 先对邮箱进行校验
    await registerRef.value.validateField('email')
    const params = {
      email: registerFormStatus.value.email
    }
    // 发送请求
    const result = await userStore.sendEmailApi(params)
    if (result.code === 200) {
       ElMessage.success("发送成功，请到邮箱查看")
       // 赋值uuid
       registerFormStatus.value.uuid = result.data
       // 按钮倒计时
       btnAvailable.value = false
       countdown.value = 60
       const timer = setInterval(() => {
          countdown.value--
          if (countdown.value <=0 ){
            clearInterval(timer)
            btnAvailable.value = true
          }
       }, 1000);
    }else {
      ElMessage.error('发送失败')
    }
  }catch (error) {
    console.log(error)
  }
}

/**
 * v1: 简单注册，没要验证码等操作
 */
const handleRegister = async () => {
  try {
    // 先进行参数校验
    await registerRef.value.validate()
    // 校验通过，发送请求
    const value = {
      username: registerFormStatus.value.username,
      password: registerFormStatus.value.password,
      email: registerFormStatus.value.email,
      code: registerFormStatus.value.code,
      uuid: registerFormStatus.value.uuid
    }
    const result = await userStore.register(value)
    console.log(result)
    if (result.code === 200) {
        ElMessage.success("注册成功")
        router.push('/auth/login')
    }else {
      ElMessage.error('注册失败')
    }
  }catch (error) {
    console.log(error)
  }
}
</script>

<template>
 <div class="register">
  <el-form ref="registerRef" :model="registerFormStatus" :rules="registerRules" class="register-form">
      <h3 class="title">{{ title }}</h3>
      <el-form-item prop="username">
        <el-input 
          v-model="registerFormStatus.username" 
          type="text" 
          size="large" 
          auto-complete="off" 
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="registerFormStatus.password"
          type="password"
          size="large" 
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerFormStatus.confirmPassword"
          type="password"
          size="large" 
          auto-complete="off"
          placeholder="确认密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <!-- 邮箱 -->
      <el-form-item prop="email">
        <el-input
        v-model="registerFormStatus.email" 
        type="email"
        size="large"
        auto-complete="off"
        placeholder="输入邮箱"
        >
        <template #prefix><svg-icon icon-class="email" class="el-input__icon input-icon"></svg-icon></template>
        </el-input>
      </el-form-item>
      <!-- 邮箱验证码 -->
      <el-form-item prop="code">
        <el-input
          size="large" 
          v-model="registerFormStatus.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
       <el-button size="large" :disabled="!btnAvailable" class="send-email-btn" @click="sendMailCode">{{ btnAvailable ?"发送验证码": `${countdown}`}}</el-button>
      </el-form-item>

      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large" 
          type="primary"
          style="width:100%;"
          @click.prevent="handleRegister"
        >
          <span v-if="!loading">注 册</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <div style="float: right;">
          <router-link class="link-type" :to="'/auth/login'">使用已有账户登录</router-link>
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

.register-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
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
.register-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.register-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-register-footer {
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
.register-code-img {
  height: 40px;
  padding-left: 12px;
}
.send-email-btn {
  margin-left: 3%;
  border-width: 1.5px;
  width: 30%;
}

</style>