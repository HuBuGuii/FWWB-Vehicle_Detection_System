<template>
  <div id="fileUpload">
    <div class="mainContent">
      <div class="title">
        <span>实时监控</span>
        <controlCom></controlCom>
      </div>
      <div class="graph">
        <div class="video-container">
          <img
            v-show="false"
            :src="streamUrl || ''"
            class="camera-feed"
            @error="handleStreamError"
            @load="() => console.log('图像加载成功')"
            alt="YOLO Detection Stream"
          />
          <video
            ref="videoRef"
            autoplay
            muted
            playsInline
            class="camera-feed"
          ></video>
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
import { onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import controlCom from '@/components/controlCom.vue'
import { useDataStore } from '@/stores/data'
import { ArrowLeftBold, ArrowRightBold } from '@element-plus/icons-vue'
import request from '@/utils/request'

const datastore = useDataStore()
const recentData = ref([])
const videoRef = ref<HTMLVideoElement | null>(null)
const mediaStream = ref<MediaStream | null>(null)
const streamUrl = ref<string | null>(null)

const getToken = () => {
  return localStorage.getItem('access_token')
}

const checkDevices = async () => {
  try {
    const devices = await navigator.mediaDevices.enumerateDevices()
    const videoDevices = devices.filter(device => device.kind === 'videoinput')
    console.log('可用的视频设备:', videoDevices)
    return videoDevices.length > 0
  } catch (error) {
    console.error('获取设备列表失败:', error)
    return false
  }
}
// 启动本地摄像头
const startLocalCamera = async () => {
  try {
    // 首先检查是否有可用设备
    const hasDevices = await checkDevices()
    if (!hasDevices) {
      ElMessage.error('未检测到摄像头设备')
      return
    }

    // 尝试不同的约束条件
    const constraints = {
      video: {
        width: { ideal: 1280 },
        height: { ideal: 720 },
        facingMode: 'user', // 使用前置摄像头
      }
    }

    console.log('正在尝试获取媒体流，约束条件:', constraints)
    const stream = await navigator.mediaDevices.getUserMedia(constraints)
    console.log('成功获取媒体流:', stream)

    // 检查流的轨道
    const videoTracks = stream.getVideoTracks()
    console.log('视频轨道:', videoTracks)

    if (videoTracks.length === 0) {
      throw new Error('没有获取到视频轨道')
    }

    mediaStream.value = stream
    if (videoRef.value) {
      videoRef.value.srcObject = stream
      // 添加事件监听
      videoRef.value.onloadedmetadata = () => {
        console.log('视频元数据已加载')
        videoRef.value?.play()
          .then(() => console.log('视频开始播放'))
          .catch(err => console.error('视频播放失败:', err))
      }
      videoRef.value.onerror = (err) => {
        console.error('视频元素错误:', err)
      }
    } else {
      console.error('video元素引用未找到')
    }

  } catch (error) {
    console.error('启动摄像头详细错误:', error)
    if (error instanceof DOMException) {
      switch (error.name) {
        case 'NotAllowedError':
          ElMessage.error('摄像头权限被拒绝')
          break
        case 'NotFoundError':
          ElMessage.error('找不到摄像头设备')
          break
        case 'NotReadableError':
          ElMessage.error('摄像头被其他应用程序占用')
          break
        case 'OverconstrainedError':
          ElMessage.error('摄像头不支持请求的分辨率')
          break
        default:
          ElMessage.error(`摄像头错误: ${error.name}`)
      }
    } else {
      ElMessage.error('启动摄像头时发生未知错误')
    }
  }
}

// 启动YOLO检测
const startYoloDetection = async () => {
  try {
    const token = getToken()
    if (!token) {
      ElMessage.error('未登录或 token 已过期')
      return
    }

    // 添加时间戳避免缓存
    streamUrl.value = `/api/yolo/realtime/4`

    // 在 img 标签上添加请求头
    const img = document.querySelector('.camera-feed') as HTMLImageElement
    if (img) {
      const xhr = new XMLHttpRequest()
      xhr.open('GET', streamUrl.value)
      xhr.setRequestHeader('Authorization', `Bearer ${token}`)
      xhr.responseType = 'blob'

      xhr.onload = function() {
        if (xhr.status === 200) {
          const url = URL.createObjectURL(xhr.response)
          img.src = url
        } else {
          console.error('加载图像失败:', xhr.status)
          ElMessage.error('加载图像失败')
        }
      }

      xhr.send()
    }

  } catch (error) {
    console.error('启动YOLO检测失败:', error)
    ElMessage.error('启动检测失败，请检查登录状态')
  }
}


const handleStreamError = () => {
  console.error('视频流错误')
  ElMessage.error('视频流连接失败')
}

onMounted(async () => {
  await startLocalCamera()
  await startYoloDetection()
})

onUnmounted(() => {
  if (mediaStream.value) {
    mediaStream.value.getTracks().forEach(track => track.stop())
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
  }
}

.video-container {
  position: relative;
  width: 100%;
  height: 100%;

  .camera-feed {
    width: 100%;
    height: 100%;
    object-fit: contain;
    border-radius: 10px;
    background: #000;
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
