import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { login as loginApi, getInfo as getInfoApi, register as registerApi } from '@/api/login';

export const useUserStore = defineStore('user', () => {
  const token = ref('');
  const roles = ref([]);
  const permissions = ref([]);
  const nickName = ref('chasion')

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
  function getInfo() {
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
  function logout() {
    token.value = '';
    roles.value = [];
    permissions.value = [];
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
    hasAdminRole
  };
}, {
  persist: true
});

export default useUserStore