import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { login as loginApi, getInfo as getInfoApi, register as registerApi, logoutApi } from '@/api/login';
import { getAction } from '@/api/manage';

export const useUserStore = defineStore('user', () => {
  const token = ref('');
  const roles = ref([]);
  const permissions = ref([]);
  const nickName = ref('chasion')
  // 发送邮箱验证码
  async function sendEmailApi(params) {
    try {
      const result = await getAction('/auth/verify/email', params)
      return result
    }catch (error) {
      throw new Error(error)
    }
  }

  // 注册方法
  async function register(params) {
    try {
      const result = await registerApi(params); // 使用重命名后的 API
      return result;
    } catch (error) {
      throw new Error(error);
    }
  }

  // 登录方法
  async function login(params) {
    try {
      const result = await loginApi(params); // 使用重命名后的 API
      token.value = result.token;
      return result;
    } catch (error) {
      throw new Error(error);
    }
  }

  // 获取用户信息
  async function getInfo() {
    return new Promise((resolve, reject) => {
      getInfoApi() // 使用重命名后的 API
        .then((res) => {
          if (res.roles && res.roles.length > 0) {
            roles.value = res.roles;
            permissions.value = res.permissions;
          } else {
            roles.value = ['ROLE_DEFAULT'];
          }
          resolve(res);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }

  // 退出登录
  async function logout() {
    try {
      const result = await logoutApi();
      if (result.code === 200) {
        token.value = '';
        roles.value = [];
        permissions.value = [];
      }
    }catch (error) {
      throw new Error(error)
    }
  }

  const hasAdminRole = computed(() => roles.value.includes('admin'));

  return {
    token,
    roles,
    permissions,
    nickName,
    register,
    login,
    getInfo,
    logout,
    hasAdminRole,
    sendEmailApi
  };
}, {
  persist: true
});

export default useUserStore