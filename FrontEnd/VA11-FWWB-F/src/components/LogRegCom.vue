<template>
  <div class="auth-container">
    <!-- 登录对话框 -->
    <el-dialog v-model="authStore.LoginVisible" :close-on-click-modal="true" center class="dialog">
      <svg t="1741272360079" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2786" width="200" height="200"><path d="M401.2 518.8C306.8 462.4 268.8 344.8 312 244c43.2-101.2 154.4-154.8 260.4-125.6 106 29.2 174 132.4 159.2 241.2-14.8 108.8-107.6 190-217.6 190-175.2 0-317.2 142-317.2 317.2 0 13.6-10.8 24.4-24.4 24.4-13.6 0-24.4-10.8-24.4-24.4 0-158.8 102.4-299.2 253.2-348z m112.8-18c94.4 0 170.8-76.4 170.8-170.8s-76.4-170.8-170.8-170.8-170.8 76.4-170.8 170.8 76.4 170.8 170.8 170.8z m366 366c0 13.6-10.8 24.4-24.4 24.4-13.6 0-24.4-10.8-24.4-24.4 0-89.2-37.6-174.4-103.6-234.8-10-9.2-10.8-24.4-1.6-34.4 9.2-10 24.4-10.8 34.4-1.6 76.4 69.6 119.6 168 119.6 270.8z m0 0" fill="#3F51B5" p-id="2787"></path><path d="M855.6 897.2c-16.8 0-30.4-13.6-30.4-30.4 0-87.6-36.8-171.2-101.6-230-12.4-11.2-13.2-30.4-2-42.8 5.6-6 12.8-9.6 21.2-10 8-0.4 16 2.4 22 8 77.6 70.4 121.6 170.8 121.6 275.2-0.4 16.4-14 30-30.8 30zM744 596h-0.8c-4.8 0.4-9.6 2.4-12.8 6-6.8 7.6-6.4 19.2 1.2 26 67.2 61.2 106 148.4 105.6 239.2 0 10 8.4 18.4 18.4 18.4 10 0 18.4-8.4 18.4-18.4 0-101.2-42.8-198.4-117.6-266.4-3.6-3.2-8-4.8-12.4-4.8zM172.4 897.2c-16.8 0-30.4-13.6-30.4-30.4 0-157.6 98-296.8 245.6-350-88.8-60-123.6-176-81.2-275.6 44-103.2 159.2-158.8 267.6-128.8 108.4 30 178.8 136.4 163.6 247.6-15.2 111.2-111.2 195.2-223.6 195.2-171.6 0-311.2 139.6-311.2 311.2 0 17.2-13.6 30.8-30.4 30.8z m342-780.8c-83.6 0-162.8 49.6-196.8 129.6-41.6 98-4.4 212.8 86.8 267.2l11.2 6.8-12.4 4c-148.8 48.4-249.2 186-249.2 342.4 0 10 8.4 18.4 18.4 18.4s18.4-8.4 18.4-18.4c0-178.4 144.8-323.2 323.2-323.2 106.4 0 197.2-79.6 211.6-184.8C740 252.8 673.2 152 570.8 124c-18.8-4.8-37.6-7.6-56.4-7.6z m-0.4 390.4c-97.6 0-176.8-79.2-176.8-176.8 0-97.6 79.2-176.8 176.8-176.8 97.6 0 176.8 79.2 176.8 176.8 0 97.6-79.2 176.8-176.8 176.8z m0-341.6c-90.8 0-164.8 74-164.8 164.8s74 164.8 164.8 164.8 164.8-74 164.8-164.8-74-164.8-164.8-164.8z" fill="#3F51B5" p-id="2788"></path></svg>
      <div class="diaName">登录</div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="70px"
        label-position="left"
        status-icon
      >
        <el-form-item label="用户名" prop="username" style="margin-top: 25px">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
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
    <el-dialog
      v-model="authStore.RegisterVisible"
      :close-on-click-modal="false"
      center
      class="dialog"
    >
      <div class="diaName">注册</div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="auto"
        label-position="left"
        status-icon
      >
        <el-form-item label="用户名" prop="username" style="margin-top: 25px">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
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

// 类型定义
interface LoginFormData {
  username: string
  password: string
  remember: boolean
}

interface RegisterFormData {
  username: string
  password: string
  confirmPassword: string
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
  username: '',
  password: '',
  remember: false,
})

// 注册表单数据
const registerForm = reactive<RegisterFormData>({
  username: '',
  password: '',
  confirmPassword: '',
})

// 登录表单验证规则
const loginRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
  ],
})

// 注册表单验证规则
const registerRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
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
  email: [
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
    await new Promise((resolve) => setTimeout(resolve, 1000))

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
    console.log('注册表单数据:', registerForm)

    // 模拟注册请求
    await new Promise((resolve) => setTimeout(resolve, 1000))

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
  height: 100%;
  width: 100%;
}
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
:deep(.el-dialog) {
  display: flex;
  flex-direction: column;
  margin-top: 50vh;
  transform: translateY(-60%);
  width: 400px;
  opacity: 1;
  border-radius: 23px;
  background: linear-gradient(135deg, rgba(255, 242, 255, 1) 0%, rgba(232, 243, 255, 1) 100%);
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
