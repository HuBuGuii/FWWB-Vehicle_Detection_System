import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  // state
  const LogCondition = ref(false)
  const LoginVisible = ref(false)
  const RegisterVisible = ref(false)
  const lastPath = ref<string>('')
  const redirectPath = ref<string>('')
  const token = ref<string>('')
  const userName = ref<string>('系统用户')
  const role = ref<string>('user')  // 用于标识超级管理员

  // getters
  
  // actions
const logout = () => {
  token.value = ''
  userName.value = '系统用户'
}

  return {
    LogCondition,
    LoginVisible,
    RegisterVisible,
    lastPath,
    redirectPath,
    token,
    role,
    userName,
    logout
  }
},{
  persist:true,
})
