<template>
  <div id="allDataView">
    <el-container>
      <el-aside width="200px">
        <el-menu :default-active="activeMenu" class="sidebar-menu" router @select="handleSelect">
          <!-- 使用 el-menu-item 定义每个导航项 -->
          <el-menu-item index="/allData">
            <el-icon><TrendCharts /></el-icon>
            <span>全部记录</span>
          </el-menu-item>

          <el-menu-item index="/map">
            <el-icon><LocationInformation /></el-icon>
            <span>路段地图</span>
          </el-menu-item>

          <el-menu-item index="/realtime">
            <el-icon><Odometer /></el-icon>
            <span>实时监测</span>
          </el-menu-item>

          <el-menu-item index="/file-upload">
            <el-icon><FolderOpened /></el-icon>
            <span>文件分析</span>
          </el-menu-item>

          <el-menu-item index="/user-manage">
            <el-icon><Service /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="user">
            <el-icon size="50px" color="rgba(229, 229, 229, 1)"><Avatar /></el-icon>
            <span>welcome</span>
            <span style="color: #409eff">{{ auth.userName }}</span>
          </div>
          <div class="tools">
            <el-input v-model="searchInput" placeholder="输入用户名" :prefix-icon="Search" />
            <el-button plain type="info" style="margin-left: 10px">搜索</el-button>
            <el-button plain type="primary" style="margin-left: 15px" @click="handleNew"
              >新增用户</el-button
            >
          </div>
        </el-header>
        <el-main>
          <div class="tablecontainer" ref="tablecontainer">
            <el-table
              :data="allData"
              stripe
              border
              style="width: 100%"
              max-height="tableMaxH"
              v-loading="loading"
              table-layout="auto"
            >
              <el-table-column prop="account" label="用户名" align="center" min-width="120" />
              <el-table-column prop="contact" label="邮箱地址" align="center" min-width="120" />
              <el-table-column
                prop="authorizationStatus"
                label="用户状态"
                align="center"
                min-width="120"
              />
              <el-table-column prop="realName" label="真实姓名" align="center" min-width="120" />
              <el-table-column prop="position" label="工作岗位" align="center" min-width="120" />
              <el-table-column label="详细信息" align="center" min-width="120">
                <template #default="scope">
                  <el-button type="primary" plain @click="handleDetail(scope.row)">编辑</el-button>
                  <el-button type="danger" plain @click="handleDelete(scope.row.userId)"
                    >删除</el-button
                  >
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pagination">
            <el-pagination
              :current-page="currentPage"
              :page-size="13"
              :pager-count="9"
              layout="prev, pager, next"
              :page-count="pageCount"
              @current-change="handlePageChange"
            />
          </div>
        </el-main>
      </el-container>
    </el-container>
    <el-dialog
      v-model="dialogVisible"
      width="50%"
      :before-close="handleBeforeClose"
      @close="handleClose"
    >
      <el-form
        ref="formRef"
        :model="currentRecord"
        :rules="rules"
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="currentRecord!.userId" :disabled="true" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="currentRecord!.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="账号" prop="account">
          <el-input v-model="currentRecord!.account" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="currentRecord!.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="currentRecord!.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="授权状态" prop="authorizationStatus">
          <el-select v-model="currentRecord!.authorizationStatus" placeholder="请选择授权状态">
            <el-option label="已授权" value="authorized" />
            <el-option label="未授权" value="unauthorized" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="currentRecord!.contact" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="currentRecord!.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="角色ID" prop="roleId">
          <el-input v-model="currentRecord!.roleId" placeholder="请输入角色ID" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave"> 保存编辑 </el-button>
        </span>
      </template>
    </el-dialog>
    <LogRegCom></LogRegCom>
  </div>
</template>

<script setup lang="ts">
import LogRegCom from '@/components/LogRegCom.vue'
import { computed, ref, onMounted, reactive, toRaw } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

type Record = {
  userId: string
  realName: string
  account: string
  password: string
  position: string
  authorizationStatus: string
  contact: string
  department: string
  roleId: string
}

const route = useRoute()
const auth = useAuthStore()
const allData = reactive<Record[]>([])
const tablecontainer = ref()
const loading = ref(false)
const pageCount = ref(10)
const searchInput = ref('')
const currentPage = ref(1)
const dialogVisible = ref(false)
const currentRecord = ref<Record>()
const formRef = ref<FormInstance>()

const rules = reactive<FormRules>({
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, message: '长度大于3个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, message: '长度大于3个字符', trigger: 'blur' },
  ],
  contact: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
  roleId: [{ required: true, message: '请输入角色ID', trigger: 'blur' }],
})

const activeMenu = computed(() => route.path)
const handleSelect = (index: string) => {
  console.log('当前选中菜单:', index)
  // router.push(index)
}

const handleNew = () => {
  auth.RegisterVisible = true
}

const handleBeforeClose = (done: () => void) => {
  ElMessageBox.confirm('确认关闭？未保存的修改将会丢失', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      done()
    })
    .catch(() => {
      // 取消关闭
    })
}

const handleClose = () => {
  console.log('closed')
}

const handleDetail = async (record: Record) => {
  dialogVisible.value = true
  currentRecord.value = { ...record }
}

const getData = async (page = 1) => {
  loading.value = true
  try {
    const response = await request.get(`/users/${page}`)
    console.log(response)
    console.log('response is ==' + response)
    allData.splice(0, allData.length, ...response.records)
  } catch (error) {
    console.log(error)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    await formRef.value.validate()

    // 使用深拷贝处理数据
    const rawData = toRaw(currentRecord.value)
    const dataToSend: Record = JSON.parse(JSON.stringify({
      userId: rawData!.userId,
      realName: rawData!.realName,
      account: rawData!.account,
      password: rawData!.password,
      position: rawData!.position,
      authorizationStatus: rawData!.authorizationStatus,
      contact: rawData!.contact,
      department: rawData!.department,
      roleId: rawData!.roleId
    }))

    const response = await request.put('/users/updateProfile', dataToSend)
    console.log(response)
    await getData()
    ElMessage.success('修改成功')
    dialogVisible.value = false

  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

const handlePageChange = (newPage: number) => {
  currentPage.value = newPage
  getData(newPage)
}

const handleDelete = async (id: number) => {
  try {
    const response = await request.delete(`/users/${id}`)
    console.log(response)
    ElMessage.error(`id为${id}的用户已被删除`)
    getData()
  } catch (error) {
    console.log(error)
  }
}

onMounted(() => {
  getData()
})
</script>

<style scoped lang="scss">
#allDataView {
  height: 100vh;
  width: 100vw;
  position: relative;
}
.el-container {
  height: 100%;
  width: 100%;
}
.el-main {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.sidebar-menu {
  height: 100% !important;
  border-right: 1px solid #e4e7ed !important;
  box-sizing: border-box;
}
.tablecontainer {
  width: 100%;
}
.el-header {
  padding-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  .tools {
    margin-right: 20px;
    display: flex;
    align-items: center;
  }
  .user {
    box-sizing: border-box;
    display: flex;
    align-items: center;
    border-bottom: 4px solid rgba(229, 229, 229, 1);
    span {
      margin-left: 20px;
      font-size: 26px;
      font-weight: 400;
      letter-spacing: 0px;
      line-height: 52.13px;
      color: rgba(128, 128, 128, 1);
      text-align: left;
      vertical-align: top;
    }
  }
}
</style>
