import { ref } from 'vue'
import { defineStore } from 'pinia'

interface Info{
  roleId:number
  realName:string
  userId:string
  token:string
}

export const useAuthStore = defineStore('auth', () => {
  // state
  const LogCondition = ref(false)
  const LoginVisible = ref(false)
  const RegisterVisible = ref(false)
  const lastPath = ref<string>('')
  const redirectPath = ref<string>('')
  const token = ref<string>('')
  const userName = ref<string>('系统用户')
  const userId = ref<string>('')
  const role = ref<string>('user')  // 用于标识超级管理员

  // getters

  // actions
const logout = () => {
  token.value = ''
  userName.value = '系统用户'
}

const setUser = (info:Info) => {
  if(info.roleId === 2){
    role.value='manager'
  }
  if(info.roleId === 1){
    role.value='user'
  }
  userName.value = info.realName
  userId.value = info.userId
  token.value = info.token
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
    userId,
    logout,
    setUser
  }
},{
  persist:true,
})
