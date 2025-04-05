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

          <el-menu-item index="/user-manage" >
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
              <span style="color:#409eff ;">{{ auth.userName }}</span>
          </div>
          <div class="tools">
            <el-input
            v-model="searchInput"
            placeholder="输入用户名"
            :prefix-icon="Search"
            />
            <el-button plain type="info" style="margin-left:10px">搜索</el-button>
            <el-button plain type="primary" style="margin-left: 15px;">新增用户</el-button>
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
            <el-table-column prop="name" label="用户名" align="center" min-width="120" />
              <el-table-column prop="email" label="邮箱地址" align="center" min-width="120" />
              <el-table-column prop="status" label="用户状态" align="center" min-width="120" />
              <el-table-column prop="regTime" label="注册时间" align="center" min-width="120" />
              <el-table-column prop="lastLog" label="最后登录时间" align="center" min-width="120" />
              <el-table-column label="详细信息" align="center" min-width="120">
                <template #default="scope">
                  <el-button type="primary" plain @click="handleDetail(scope.row.id)"
                    >编辑</el-button
                  >
                  <el-button type="danger"  plain @click="handleDelete(scope.row.id) "
                    >删除</el-button
                  >
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pagination">
            <el-pagination
              :page-size="13"
              :pager-count="9"
              layout="prev, pager, next"
              :page-count="pageCount"
            />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
  <LogRegCom></LogRegCom>
</template>

<script setup lang="ts">
import LogRegCom from '@/components/LogRegCom.vue'
import { computed, ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const auth = useAuthStore()
const allData = ref([
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  { id: 1, name: 'guigui', email: '1232132123@gmail.com', status: 'nromal', regTime: '3/12 12:00', lastLog: '3/18 13:30' },
  //排个13行差不多，问后端要的时候就一页13行
])
const tablecontainer = ref()
const loading = ref(false)
const pageCount = ref(10)
const searchInput = ref('')

const activeMenu = computed(() => route.path)
const handleSelect = (index: string) => {
  console.log('当前选中菜单:', index)
  // router.push(index)
}


const handleDetail = (id: number) => {
  console.log(id)
}

const getData = async() => {
  console.log("等着写getData")
}

const handleDelete = (id:number) => {
  console.log(id)
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
.el-main{
  display: flex;
  flex-direction: column;
  align-items: center;
}
.sidebar-menu {
  height: 100% !important;
  border-right: 1px solid #e4e7ed !important;
  box-sizing: border-box;
}
.tablecontainer{
  width: 100%;
}
.el-header{
  padding-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  .tools{
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
