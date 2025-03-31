<template>
  <div class="auth-container">
    <!-- 登录对话框 -->
    <el-dialog v-model="authStore.LoginVisible" :close-on-click-modal="true" center>
      <svg
        t="1741584083381"
        class="icon"
        viewBox="0 0 1024 1024"
        version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        p-id="2812"
        width="150"
        height="150"
      >
        <path
          d="M401.2 518.8C306.8 462.4 268.8 344.8 312 244c43.2-101.2 154.4-154.8 260.4-125.6 106 29.2 174 132.4 159.2 241.2-14.8 108.8-107.6 190-217.6 190-175.2 0-317.2 142-317.2 317.2 0 13.6-10.8 24.4-24.4 24.4-13.6 0-24.4-10.8-24.4-24.4 0-158.8 102.4-299.2 253.2-348z m112.8-18c94.4 0 170.8-76.4 170.8-170.8s-76.4-170.8-170.8-170.8-170.8 76.4-170.8 170.8 76.4 170.8 170.8 170.8z m366 366c0 13.6-10.8 24.4-24.4 24.4-13.6 0-24.4-10.8-24.4-24.4 0-89.2-37.6-174.4-103.6-234.8-10-9.2-10.8-24.4-1.6-34.4 9.2-10 24.4-10.8 34.4-1.6 76.4 69.6 119.6 168 119.6 270.8z m0 0"
          fill="#3F51B5"
          p-id="2813"
        ></path>
        <path
          d="M855.6 897.2c-16.8 0-30.4-13.6-30.4-30.4 0-87.6-36.8-171.2-101.6-230-12.4-11.2-13.2-30.4-2-42.8 5.6-6 12.8-9.6 21.2-10 8-0.4 16 2.4 22 8 77.6 70.4 121.6 170.8 121.6 275.2-0.4 16.4-14 30-30.8 30zM744 596h-0.8c-4.8 0.4-9.6 2.4-12.8 6-6.8 7.6-6.4 19.2 1.2 26 67.2 61.2 106 148.4 105.6 239.2 0 10 8.4 18.4 18.4 18.4 10 0 18.4-8.4 18.4-18.4 0-101.2-42.8-198.4-117.6-266.4-3.6-3.2-8-4.8-12.4-4.8zM172.4 897.2c-16.8 0-30.4-13.6-30.4-30.4 0-157.6 98-296.8 245.6-350-88.8-60-123.6-176-81.2-275.6 44-103.2 159.2-158.8 267.6-128.8 108.4 30 178.8 136.4 163.6 247.6-15.2 111.2-111.2 195.2-223.6 195.2-171.6 0-311.2 139.6-311.2 311.2 0 17.2-13.6 30.8-30.4 30.8z m342-780.8c-83.6 0-162.8 49.6-196.8 129.6-41.6 98-4.4 212.8 86.8 267.2l11.2 6.8-12.4 4c-148.8 48.4-249.2 186-249.2 342.4 0 10 8.4 18.4 18.4 18.4s18.4-8.4 18.4-18.4c0-178.4 144.8-323.2 323.2-323.2 106.4 0 197.2-79.6 211.6-184.8C740 252.8 673.2 152 570.8 124c-18.8-4.8-37.6-7.6-56.4-7.6z m-0.4 390.4c-97.6 0-176.8-79.2-176.8-176.8 0-97.6 79.2-176.8 176.8-176.8 97.6 0 176.8 79.2 176.8 176.8 0 97.6-79.2 176.8-176.8 176.8z m0-341.6c-90.8 0-164.8 74-164.8 164.8s74 164.8 164.8 164.8 164.8-74 164.8-164.8-74-164.8-164.8-164.8z"
          fill="#3F51B5"
          p-id="2814"
        ></path>
      </svg>
      <div class="diaName">登录</div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="70px"
        label-position="left"
        status-icon
      >
        <el-form-item label="用户名" prop="account" style="margin-top: 25px">
          <el-input v-model="loginForm.account" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="authStore.LoginVisible = false">取消</el-button>
          <el-button type="primary" @click="handleLogin" :loading="loginLoading"> 登录 </el-button>
          <el-button link type="primary" @click="switchToRegister"> 没有账号?去注册 </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 注册对话框 -->
    <el-dialog v-model="authStore.RegisterVisible" :close-on-click-modal="true" center>
      <svg
        t="1741583992340"
        class="icon"
        viewBox="0 0 1024 1024"
        version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        p-id="1608"
        width="150"
        height="150"
      >
        <path
          d="M493.411643 554.452881c-132.722814 0-240.718457-107.974154-240.718457-240.696967 0-132.701325 107.995643-240.675478 240.718457-240.675478s240.675478 107.974154 240.675478 240.675478C734.087121 446.478727 626.134457 554.452881 493.411643 554.452881zM493.411643 139.247414c-96.235808 0-174.551478 78.294181-174.551478 174.508499 0 96.235808 78.31567 174.529989 174.551478 174.529989s174.508499-78.294181 174.508499-174.529989C667.920142 217.541595 589.647451 139.247414 493.411643 139.247414z"
          fill="#f3ca7e"
          p-id="1609"
        ></path>
        <path
          d="M96.92347 950.919565c-18.264992 0-33.083489-14.818497-33.083489-33.083489 0-236.863662 192.686511-429.550173 429.571662-429.550173 104.808044 0 205.739804 38.188764 284.22739 107.521852 13.699 12.0832 14.991436 32.997532 2.929726 46.696532-12.147668 13.72049-33.040511 14.925944-46.696532 2.885723-66.425875-58.650795-151.806451-90.937129-240.460584-90.937129-200.398146 0-363.404684 163.006538-363.404684 363.383194C130.007983 936.101067 115.188462 950.919565 96.92347 950.919565z"
          fill="#f3ca7e"
          p-id="1610"
        ></path>
        <path
          d="M927.075507 837.861626 701.003632 837.861626c-18.264992 0-33.083489-14.818497-33.083489-33.083489s14.818497-33.083489 33.083489-33.083489l226.071875 0c18.264992 0 33.083489 14.818497 33.083489 33.083489S945.340499 837.861626 927.075507 837.861626z"
          fill="#f3ca7e"
          p-id="1611"
        ></path>
        <path
          d="M814.040081 950.898075c-18.264992 0-33.083489-14.818497-33.083489-33.083489L780.956591 691.741687c0-18.264992 14.818497-33.083489 33.083489-33.083489s33.083489 14.818497 33.083489 33.083489l0 226.071875C847.12357 936.079578 832.305073 950.898075 814.040081 950.898075z"
          fill="#f3ca7e"
          p-id="1612"
        ></path>
      </svg>
      <div class="diaName">注册</div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="auto"
        label-position="left"
        status-icon
      >
        <el-form-item label="用户名" prop="account" style="margin-top: 25px">
          <el-input v-model="registerForm.account" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="电子邮箱" prop="contact">
          <el-input v-model="registerForm.contact" placeholder="请输入邮箱地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="authStore.RegisterVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRegister" :loading="registerLoading">
            注册
          </el-button>
          <el-button link type="primary" @click="switchToLogin"> 已有账号?去登录 </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

// 类型定义
interface LoginFormData {
  account: string
  password: string
}

interface RegisterFormData {
  account: string
  password: string
  confirmPassword: string
  realName: string
  contact: string
}

// 实例化
const authStore = useAuthStore()
const router = useRouter()

// 表单引用
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

// 加载状态
const loginLoading = ref<boolean>(false)
const registerLoading = ref<boolean>(false)

// 登录表单数据
const loginForm = reactive<LoginFormData>({
  account: '',
  password: '',

})

// 注册表单数据
const registerForm = reactive<RegisterFormData>({
  account: '',
  password: '',
  confirmPassword: '',
  realName: '',
  contact: '',
})

// 登录表单验证规则
const loginRules = reactive<FormRules>({
  account: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
  ],
})

// 注册表单验证规则
const registerRules = reactive<FormRules>({
  account: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback: (error?: Error) => void) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  contact: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
})

// 切换到注册对话框
const switchToRegister = (): void => {
  authStore.LoginVisible = false
  authStore.RegisterVisible = true
}

// 切换到登录对话框
const switchToLogin = (): void => {
  authStore.RegisterVisible = false
  authStore.LoginVisible = true
}

// 处理登录
const handleLogin = async (): Promise<void> => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loginLoading.value = true

    // 这里添加登录逻辑
    console.log('登录表单数据:', loginForm)
    // 模拟登录请求
    const response = await request.post('auth/login',loginForm)
    console.log(response)
    const token = response.token                                          //没有问题，后端没按格式返
    console.log('token是' + token)
    localStorage.setItem('access_token',token)
    console.log('token has been stored')
    ElMessage.success('登录成功')
    authStore.LogCondition = true
    router.push(authStore.redirectPath || { name: 'usage' })
    authStore.redirectPath = ''
    authStore.LoginVisible = false
  } catch (error) {
    console.error('登录验证失败:', error)
  } finally {
    loginLoading.value = false
  }
}

// 处理注册
const handleRegister = async (): Promise<void> => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    // 这里添加注册逻辑
    const { confirmPassword, ...sendM } = registerForm
    const send = JSON.stringify(sendM)
    console.log('发送的表单数据', send)
    // 模拟注册请求
    const response = await request.post('/auth/register', send)
    console.log(response)
    ElMessage.success('注册成功')
    authStore.RegisterVisible = false
    authStore.LoginVisible = true
  } catch (error) {
    console.error('注册验证失败:', error)
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  overflow: hidden;
  height: 100%;
  width: 100%;
}
.dialog-footer {
  display: flex;
  justify-content: center;
  align-items: center;
}
:deep(.el-dialog) {
  margin-top: 50vh;
  transform: translateY(-60%);
  width: 350px;
  opacity: 1;
  border-radius: 23px;
  background: linear-gradient(135deg, rgba(255, 242, 255, 1) 0%, rgba(232, 243, 255, 1) 100%);
  .el-dialog__body {
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
  }
}
:deep(.el-overlay-dialog) {
  overflow: hidden;
}

.diaName {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 0px;
  line-height: 52.13px;
  color: rgba(166, 166, 166, 1);
  text-align: left;
  vertical-align: top;
}
</style>
