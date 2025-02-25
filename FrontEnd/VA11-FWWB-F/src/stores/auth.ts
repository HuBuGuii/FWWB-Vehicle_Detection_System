import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  // state
  const LogCondition = ref(false)
  const showLogin = ref(false)
  const showRegister = ref(false)
  const redirectPath = ref<string>('')
  const token = ref<string>('')
  

  // getters
  

  // actions
  

  return {
    LogCondition,
    showLogin,
    showRegister,
    redirectPath,
  }
})