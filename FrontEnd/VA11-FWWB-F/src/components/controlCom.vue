<template>
  <el-select
    v-model="selectedAction"
    :popper-append-to-body="false"
    placeholder=""
    @change="handleAction"
    class="user-selector"
    default-first-option
  >
    <template #prefix>
      <div class="user-prefix">
        <span class="username"> 当前用户：{{ userStore.userName }}</span>
      </div>
    </template>

    <el-option
      v-for="item in filteredMenuOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    >
      <div class="menu-item">
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </div>
    </el-option>
  </el-select>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { Component } from 'vue'
import {
  User,
  Setting,
  SwitchButton,
  Document,
} from '@element-plus/icons-vue'

type MenuOption = {
  value: string
  label: string
  icon: Component
  showForRoles:string[]
}

const router = useRouter()
const userStore = useAuthStore()
const selectedAction = ref('')

const menuOptions = computed<MenuOption[]>(() => [
  {
    value: 'uploadfile',
    label: '文件上传',
    icon: User,
    showForRoles:['user','manager']
  },
  {
    value: 'detectionmap',
    label: '路段地图',
    icon: Document,
    showForRoles:['user','manager']
  },
  {
    value: 'dataList',
    label: '全部记录',
    icon: Setting,
    showForRoles:['user','manager']
  },
  {
    value: 'userManage',
    label: '用户管理',
    icon: SwitchButton,
    showForRoles:['manager']
  },
  {
    value:'logout',
    label:'退出登录',
    icon:SwitchButton,
    showForRoles:['user','manager']
  }
])

const handleAction = (action: string) => {
  switch (action) {
    case 'uploadfile':
      router.push({name:'file-upload'})
      break
    case 'detectionmap':
      router.push({name:'detection-map'})
      break
    case 'dataList':
      router.push({name:'allData'})
      break
    case 'userManage':
      router.push({name:'user-manage'})
      break
    case 'logout':
      userStore.logout()
      router.push({name:'home'})
      break
  }
  selectedAction.value = '' // 重置选择
}

const filteredMenuOptions = computed<MenuOption[]>(() => {
  return menuOptions.value.filter(item =>
    item.showForRoles?.includes(userStore.role)
  )
})
</script>

<style scoped>
.user-selector {
  width: 180px;
  --el-select-input-focus-border-color: transparent;
}

.user-selector:deep(.el-select__wrapper) {
  box-shadow: none !important;
  padding-left: 8px;
}

.user-selector:deep(.el-select__caret) {
  display: inline-flex;
}

.user-prefix {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-item i {
  width: 1.1em;
}
</style>
