import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  // state
  const LogCondition = ref(false)
  const LoginVisible = ref(false)
  const RegisterVisible = ref(false)
  const lastPath = ref<string>('')
  const redirectPath = ref<string>('')
  const token = ref<string>('')


  // getters


  // actions


  return {
    LogCondition,
    LoginVisible,
    RegisterVisible,
    lastPath,
    redirectPath,
    token
  }
})
