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

          <el-menu-item index="/">
            <el-icon><Odometer /></el-icon>
            <span>实时监测</span>
          </el-menu-item>

          <el-menu-item index="/file-upload">
            <el-icon><FolderOpened /></el-icon>
            <span>文件分析</span>
          </el-menu-item>

          <el-menu-item index="/map">
            <el-icon><LocationInformation /></el-icon>
            <span>路段地图</span>
          </el-menu-item>

          <el-menu-item index="/services" v-if="auth.identity">
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
            <span>{{ auth.userName }}</span>
          </div>
        </el-header>
        <el-main>
          <div class="tablecontainer" ref="tablecontainer">
            <el-table
              :data="allData"
              stripe
              style="width: 100%"
              max-height="tableMaxH"
              v-loading="loading"
              table-layout="auto"
            >
              <el-table-column prop="time" label="时间" align="center" min-width="120" />
              <el-table-column prop="address" label="路段" align="center" min-width="120" />
              <el-table-column prop="number" label="车牌号" align="center" min-width="120" />
              <el-table-column prop="type" label="车辆类型" align="center" min-width="120" />
              <el-table-column prop="color" label="车辆颜色" align="center" min-width="120" />
              <el-table-column label="详细信息" min-width="120">
                <template #default="scope">
                  <el-button type="primary" plain @click="handleDetail(scope.row.id)"
                    >详情</el-button
                  >
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pagination">
            <el-pagination
              :page-size="pageSize"
              :pager-count="10"
              layout="prev, pager, next"
              :total="1000"
            />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRoute } from 'vue-router'

const route = useRoute()
const auth = useAuthStore()
const allData = ref([
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  { id: 1, time: '1/1', address: '路段1', number: 'h132kc', type: '小车', color: '白' },
  //排个13行差不多，问后端要的时候就一页13行
])
const tablecontainer = ref()
const tableMaxH = ref<number>(0)
const loading = ref(false)
const pageSize = ref(0)

const activeMenu = computed(() => route.path)
const handleSelect = (index: string) => {
  console.log('当前选中菜单:', index)
  // router.push(index)
}
const calMaxH = () => {
  if (tablecontainer.value) {
    const temp = tablecontainer.value.getBoundingClientRect()
    tableMaxH.value = temp.height - 2
  }
}

const handleDetail = (id: number) => {
  console.log(id)
}

const getData = async() => {
  console.log("等着写")
}

onMounted(() => {
  calMaxH()
  console.log(tableMaxH)
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
.user {
  box-sizing: border-box;
  padding-top: 10px;
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
</style>
