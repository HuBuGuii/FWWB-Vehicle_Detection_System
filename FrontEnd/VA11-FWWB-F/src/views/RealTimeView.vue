<template>
  <div id="fileUpload">
    <div class="mainContent">
      <div class="title">
        <span>实时监控</span>
        <controlCom></controlCom>
      </div>
      <div class="graph">
        <div v-if="streamActive" class="video-container">
          <img
          :src="streamUrl || ''"
  class="camera-feed"
  @error="handleStreamError"
  @load="() => console.log('图像加载成功')"
  @loadstart="() => console.log('图像开始加载')"
  @progress="(event) => console.log('接收数据进度:', event)"
  alt="YOLO Detection Stream"
          />
          <div class="control-overlay">
            <el-button type="danger" @click="stopDetection">停止检测</el-button>
          </div>
        </div>
        <div v-else-if="loading" class="loading">
          正在启动YOLO检测...
        </div>
        <div v-else class="no-signal">
          <el-button type="primary" @click="startDetection">开始检测</el-button>
        </div>
      </div>
      <div class="dashboard">
        <div class="board1">
          <div class="pie"></div>
          <div class="name">识别准确率</div>
        </div>
        <div class="board2">
          <div class="pie"></div>
          <div class="name">事故发生率</div>
        </div>
        <div class="board3">
          <div class="pie"></div>
          <div class="name">拥堵系数</div>
        </div>
        <span class="inlinelink">
          <RouterLink to="/map" class="link">进入路段地图</RouterLink>
          <el-icon class="icon"><ArrowLeftBold /></el-icon>
        </span>
      </div>
    </div>
    <div class="rightAside">
      <div class="totalData">
        <div class="title">检测数据</div>
        <div class="cards">
          <div class="card1">
            <div class="number"></div>
            <div class="string">车流量1</div>
          </div>
          <div class="card2">
            <div class="number"></div>
            <div class="string">车流量2</div>
          </div>
        </div>
      </div>
      <div class="listData">
        <div class="name">
          <div class="title">最新检测</div>
          <span class="inlinelink">
            <el-icon class="icon"><ArrowRightBold /></el-icon>
            <RouterLink to="/allData" class="link">进入全部记录</RouterLink>
          </span>
        </div>
        <div class="table">
          <el-table :data="recentData" stripe style="width: 100%;">
            <el-table-column prop="date" label="Date" width="100" />
            <el-table-column prop="car" label="Name" />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import controlCom from '@/components/controlCom.vue'
import { useDataStore } from '@/stores/data'
import { ArrowLeftBold, ArrowRightBold } from '@element-plus/icons-vue'
import request from '@/utils/request'

const datastore = useDataStore()
const recentData = datastore.showData.slice(0, 10)

const streamUrl = ref<string | null>(null)
const loading = ref(false)
const streamActive = ref(false)

const getToken = () => {
  return localStorage.getItem('access_token') // 或者从其他存储位置获取 token
}

// 启动YOLO检测
const startDetection = async () => {
  try {
    loading.value = true
    const token = getToken()
    if (!token) {
      ElMessage.error('未登录或 token 已过期')
      return
    }

    console.log('开始连接视频流...')

    // 使用 axios 请求视频流
    const response = await request({
      url: '/yolo/realtime/5',
      method: 'GET',
      responseType: 'blob', // 重要：设置响应类型为 blob
      headers: {
        'Accept': 'multipart/x-mixed-replace; boundary=frame'
      }
    })

    // 创建 Blob URL
    const blob = new Blob([response.data], { type: 'multipart/x-mixed-replace; boundary=frame' })
    streamUrl.value = URL.createObjectURL(blob)

    loading.value = false
    streamActive.value = true

  } catch (error) {
    console.error('启动检测失败:', error)
    ElMessage.error('启动检测失败，请检查登录状态')
    handleStreamError()
  }
}

const cleanup = () => {
  if (streamUrl.value) {
    URL.revokeObjectURL(streamUrl.value)
    streamUrl.value = null
  }
  loading.value = false
  streamActive.value = false
}

// 停止检测
const stopDetection = async () => {
  try {
    cleanup()
    await request({
      url: '/yolo/stop/4',
      method: 'POST'
    })
  } catch (error) {
    console.error('停止检测失败:', error)
    ElMessage.error('停止检测失败')
  }
}

// 处理视频流错误
const handleStreamError = () => {
  console.error('视频流错误')
  ElMessage.error('视频流连接失败')
  cleanup()
}

// 页面卸载时确保停止检测
onUnmounted(() => {
  cleanup()
  if (streamActive.value) {
    stopDetection()
  }
})
</script>

<style scoped lang="scss">
@use 'sass:math';
$design-width: 1920;
$design-height: 1080;

@function px-to-vw($px) {
  @return math.div($px, 1920) * 100vw;
}

@function px-to-vh($px) {
  @return math.div($px, 1080) * 100vh;
}

#fileUpload {
  background: linear-gradient(90deg, rgba(235, 250, 250, 1) 0%, rgba(242, 248, 250, 1) 100%);
  box-sizing: border-box;
  overflow: hidden;
  padding: 30px;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: space-evenly;
  flex-direction: row;
  > div {
    background: rgba(255, 255, 255, 1);
    border-radius: 20px;
  }
  .mainContent {
    position: relative;
    display: flex;
    flex-direction: column;
    width: 66%;
    margin-right: 30px;
    padding: 0 30px;
    .title {
      margin: px-to-vh(30) 0 px-to-vh(15) 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .graph {
      border-radius: 10px;
      background-color: darkgrey;
      flex: 1;
      overflow: hidden;

      .video-container {
        position: relative;
        width: 100%;
        height: 100%;

        .control-overlay {
          position: absolute;
          top: 10px;
          right: 10px;
          z-index: 10;
        }

        .camera-feed {
          width: 100%;
          height: 100%;
          object-fit: contain;
          border-radius: 10px;
        }
      }

      .loading,
      .no-signal {
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        color: white;
        font-size: 18px;
      }
    }

    .dashboard {
      height: px-to-vh(235);
      display: flex;
      margin: px-to-vh(30) 0;
      > div {
        width: px-to-vw(220);
        height: px-to-vh(235);
        margin-right: px-to-vw(40);
        border-radius: 20px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
      }
      .board1 {
        background-color: #d7fcfa;
      }
      .board2 {
        background-color: #fffac9;
      }
      .board3 {
        background-color: #fad3ca;
      }
      .inlinelink {
        margin-left: 20%;
        transform: translateY(6%);
      }
    }
  }
  .rightAside {
    box-sizing: border-box;
    padding: 30px;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    width: 27%;
    > div {
      width: 100%;
    }
  }
}

.title {
  height: px-to-vh(80);
  font-size: 30px;
  font-weight: 900;
  letter-spacing: 0px;
  line-height: 52.13px;
  color: rgba(42, 90, 145, 1);
  text-align: left;
  vertical-align: top;
}

.inlinelink {
  width: px-to-vw(180);
  display: flex;
}

.link {
  height: px-to-vh(63);
  opacity: 1;
  font-size: 18px;
  font-weight: 400;
  letter-spacing: 0px;
  line-height: 34.75px;
  color: rgba(137, 174, 217, 1);
  text-align: left;
  vertical-align: middle;
}

a {
  text-decoration: none;
  color: inherit;
}

.icon {
  transform: translateY(50%);
  color: rgba(42, 90, 145, 1);
}

.totalData {
  flex-grow: 2;
  flex-shrink: 0;
  flex-basis: 25%;
  margin-bottom: 20px;
}

.listData {
  flex-grow: 7;
  flex-basis: 70%;
}

.name {
  height: px-to-vh(110);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table {
  flex: 1;
  overflow-y: auto;
}

.cards {
  display: flex;
  justify-content: space-between;
  width: 100%;
  height: 80%;
  > div {
    opacity: 1;
    border-radius: 20px;
    background: rgba(237, 247, 252, 1);
    width: 45%;
    height: 90%;
  }
}
</style>
