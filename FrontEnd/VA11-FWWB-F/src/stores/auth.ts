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
  const userName = ref<string>('小明')
  const identity = ref<boolean>(false)  // 用于标识超级管理员

  // getters


  // actions


  return {
    LogCondition,
    LoginVisible,
    RegisterVisible,
    lastPath,
    redirectPath,
    token,
    identity,
    userName
  }
},{
  persist:true,
})
