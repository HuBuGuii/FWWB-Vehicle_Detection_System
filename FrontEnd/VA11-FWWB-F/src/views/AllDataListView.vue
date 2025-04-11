<template>
  <div id="allDataView">
    <el-container>
      <el-aside width="200px">
        <el-menu :default-active="activeMenu" class="sidebar-menu" router @select="handleSelect">
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

          <el-menu-item index="/user-manage" v-if="auth.role === 'manager'">
            <el-icon><Service /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-bar">
            <div class="user">
              <el-icon size="50px" color="rgba(229, 229, 229, 1)"><Avatar /></el-icon>
              <span>当前正在查看</span>
              <span style="color:#409eff">{{ nowCon }}</span>
            </div>
            <div class="tool">
              <el-button plain @click="data.clearF" v-if="data.isFiltered">清除筛选</el-button>
              <el-button plain @click="switchCC">实时/文件</el-button>
              <el-button plain @click="showScreen" style="margin-right: 20px;">筛选小助手</el-button>
              <ControlCom></ControlCom>
            </div>
          </div>
        </el-header>

        <el-main>
          <div class="tablecontainer" ref="tablecontainer">
            <el-table
              :data="data.showData"
               style="width: 100%"
              stripe
              border
              v-loading="loading"
              table-layout="auto"
            >
              <template v-for="col in tableColumns" :key="col.prop">
                <el-table-column
                  v-bind="col"
                  align="center"
                  min-width="120"
                >
                  <template #default="{ row }" v-if="col.formatter">
                    {{ col.formatter(row) }}
                  </template>
                </el-table-column>
              </template>

              <el-table-column label="操作" align="center" min-width="120">
                <template #default="scope">
                  <el-button
                    type="primary"
                    plain
                    @click="handleDetail(isRealTimeRecord(scope.row) ? scope.row.rdId : scope.row.nrdId)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="pagination">
            <el-pagination
              :page-size="data.pagination.pageSize"
              :pager-count="9"
              layout="prev, pager, next"
              :total="data.pagination.totalPage * data.pagination.pageSize"
              @current-change="data.handlePageChange"
            />
          </div>
        </el-main>
      </el-container>
    </el-container>
    <ScreenCom :ifshow-screen="ifshowScreen"></ScreenCom>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRoute } from 'vue-router'
import ScreenCom from '@/components/screenCom.vue'
import ControlCom from '@/components/controlCom.vue'
import { ElMessage } from 'element-plus'
import { useDataStore } from '@/stores/data'



// types.ts
// 基础接口
interface BaseDetectionRecord {
  time: Date;
  confidence: number;
  vehicleId: number;
  vehicleStatus: string;
  maxAge: number;
  exp: string;
  vehicleInfo?: VehicleInfo;
}

// 实时检测记录
interface RealTimeDetectionRecord extends BaseDetectionRecord {
  rdId: string;
  cameraId: string;
  temperature: number;
  weather: string;
}

// 非实时检测记录
interface NonRealTimeDetectionRecord extends BaseDetectionRecord {
  nrdId: string;
  userId: string;
}

interface VehicleInfo {
  vehicleId: number;
  licence: string | null;
  type: string;
}

type DetectionRecord = RealTimeDetectionRecord | NonRealTimeDetectionRecord;

// API 响应类型


const route = useRoute()
const data = useDataStore()
const auth = useAuthStore()
const ifshowScreen = ref(false)

const loading = ref(false)
const tablecontainer = ref<HTMLElement>()

const activeMenu = computed(() => route.path)

const nowCon = computed(() => {
  return data.isRealTime ? '实时监测' : '文件上传'
})


// 类型守卫
function isRealTimeRecord(record: DetectionRecord): record is RealTimeDetectionRecord {
  return 'rdId' in record;
}



// 表格列定义
const tableColumns = [
  {
    prop: 'time',
    label: '时间',
    formatter: (row: DetectionRecord) => new Date(row.time).toLocaleString()
  },
  {
    prop: 'vehicleId',
    label: '车辆ID'
  },
  {
    prop: 'vehicleInfo',
    label: '车牌号',
    formatter: (row: DetectionRecord) => row.vehicleInfo?.licence || '未知'
  },
  {
    prop: 'vehicleInfo',
    label: '车辆类型',
    formatter: (row: DetectionRecord) => row.vehicleInfo?.type || '未知'
  },
  {
    prop: 'confidence',
    label: '置信度',
    formatter: (row: DetectionRecord) => `${(row.confidence * 100).toFixed(2)}%`
  },
  {
    prop: 'vehicleStatus',
    label: '状态'
  }
]

const showScreen = () => {
  ifshowScreen.value = !ifshowScreen.value
  console.log('筛选组件已开')
}

const switchCC = () => {
  data.isRealTime = !data.isRealTime
  data.getData()
}

const handleSelect = (index: string) => {
  console.log('当前选中菜单:', index)
}

const handleDetail = (id: string) => {
  console.log('查看详情:', id)
  // 实现详情查看逻辑
}

// 监听数据源变化
watch(() => data.isRealTime, (newVal) => {
  ElMessage.success(`已切换至${newVal ? '实时监测' : '文件上传'}`)
  data.getData()
}, { immediate: true })

onMounted(() => {
  data.getData()
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

.header-bar {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 50px 0 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
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

  .tool {
    display: flex;
  }
}

.pagination {
  margin-top: 20px;
}
</style>
