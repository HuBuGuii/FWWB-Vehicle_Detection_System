<template>
  <div id="fileUpload">
    <div class="mainContent">
      <div class="title"><span>文件分析</span><controlCom></controlCom></div>
      <div class="graph">
        <el-upload
          ref="uploadRef"
          class="upload"
          drag
          action="#"
          multiple
          :before-upload="beforeUpload"
          :on-success="handleSucess"
          :on-error="handleError"
          :auto-upload="false"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">拖拽文件或 <em>点击此处上传</em></div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传视频或图片，上传完成后点击右侧按钮开始分析 <el-button @click="handleUpload">开始分析</el-button>
            </div>
          </template>
        </el-upload>
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
        <span class="inlinelink"
          ><RouterLink to="/map" class="link">进入路段地图</RouterLink
          ><el-icon class="icon"><ArrowLeftBold /></el-icon
        ></span>
      </div>
    </div>
    <div class="rightAside">
      <div class="totalData">
        <div class="title"><span>检测数据</span></div>
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
          <el-table :data="recentData" stripe style="width: 100%">
            <el-table-column prop="date" label="Date" width="100" />
            <el-table-column prop="car" label="Name" />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import controlCom from '@/components/controlCom.vue'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadInstance } from 'element-plus'
const uploadRef = ref<UploadInstance>()

const recentData = [
  { date: '10/11', car: '小型车' },
  { date: '10/11', car: '小型车' },
  { date: '10/11', car: '小型车' },
  { date: '10/11', car: '小型车' },
  { date: '10/11', car: '小型车' },
  { date: '10/11', car: '小型车' },
  { date: '10/11', car: '小型车' },
  { date: '10/11', car: '小型车' },
  { date: '10/11', car: '小型车' },
  { date: '10/11', car: '小型车' },
]

const beforeUpload = (file:File) => {
  const isImage = file.type.startsWith('image/'); // 检查是否为图片
  const isVideo = file.type.startsWith('video/'); // 检查是否为视频
  if(!isImage && !isVideo){
    ElMessage.error('只能上传视频或图片')
    return false
  }
  return true
}

const handleSucess = () => {
  ElMessage.success('上传成功')
}

const handleError = () => {
  ElMessage.error('上传失败')
}

const handleUpload = () => {
  uploadRef.value?.submit()
}
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
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 10px;
      background-color: #dcdcdc;
      flex: 1;
      .upload {
        width: 80%;
        .el-upload__tip {
          display: flex;
          justify-content: space-between;
          padding: 0 20px;
        }
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
