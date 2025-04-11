<template>
  <div id="home-view">
    <div class="view-background">
      <div class="background-mask"></div>
      <div class="background-circle"></div>
    </div>
    <main class="view-content">
      <div class="header">
        <div class="smallTitle">VA11</div>
        <div class="user"><controlCom></controlCom></div>
      </div>
      <div class="Title">
        <div class="name">智航云轨</div>
        <div class="desc">大型交通枢纽辖域内通行车辆智能检测系统</div>
      </div>

      <div class="cards">
        <div class="card1" @click="toMap()" style="cursor: pointer">
          <div class="card-icon" style="background-color: rgba(106, 168, 234, 1)">
            <div class="innericon">
              <svg
                t="1741585059437"
                class="icon"
                viewBox="0 0 1024 1024"
                version="1.1"
                xmlns="http://www.w3.org/2000/svg"
                p-id="4016"
                width="80%"
                height="80%"
              >
                <path
                  d="M512 772.44c-143.33 0-259.95-116.61-259.95-259.94s116.62-260 260-260a26.52 26.52 0 0 1 0 53c-114.09 0-206.91 92.82-206.91 206.92S397.91 719.4 512 719.4s206.91-92.82 206.91-206.9a206.45 206.45 0 0 0-11.3-67.64 26.52 26.52 0 0 1 50.13-17.33 259.43 259.43 0 0 1 14.21 85C772 655.83 655.34 772.44 512 772.44z"
                  fill="#e6e6e6"
                  p-id="4017"
                ></path>
                <path
                  d="M512 615.29A102.8 102.8 0 1 1 614.79 512.5 102.9 102.9 0 0 1 512 615.29z m0-152.55a49.76 49.76 0 1 0 49.75 49.76A49.81 49.81 0 0 0 512 462.74z"
                  fill="#e6e6e6"
                  p-id="4018"
                ></path>
                <path
                  d="M512 960.77c-247.18 0-448.27-201.1-448.27-448.27S264.82 64.22 512 64.22a26.52 26.52 0 0 1 26.52 26.52v345.48a26.52 26.52 0 0 1-53 0V118.14C279.86 131.83 116.77 303.47 116.77 512.5c0 217.93 177.3 395.23 395.23 395.23s395.23-177.3 395.23-395.23a395.83 395.83 0 0 0-47.68-188.38 26.52 26.52 0 0 1 46.61-25.32 449 449 0 0 1 54.11 213.7c0 247.17-201.09 448.27-448.27 448.27z"
                  fill="#e6e6e6"
                  p-id="4019"
                ></path>
              </svg>
            </div>
          </div>
          <div class="card-title" style="color: rgba(27, 120, 227, 1)">实时监测</div>
          <div class="card-desc">
            进入到“监测地图”，您可以通过点击选中特定路段，进行更加细致的监测。
          </div>
        </div>

        <div class="card2" @click="toFileUpload()" style="cursor: pointer">
          <div class="card-icon" style="background-color: rgba(124, 242, 171, 0.6)">
            <div class="innericon">
              <svg
                t="1744365845321"
                class="icon"
                viewBox="0 0 1024 1024"
                version="1.1"
                xmlns="http://www.w3.org/2000/svg"
                p-id="2930"
                width="200"
                height="200"
              >
                <path
                  d="M768 640v128H256v-128H170.666667v128c0 46.933333 38.4 85.333333 85.333333 85.333333h512c46.933333 0 85.333333-38.4 85.333333-85.333333v-128h-85.333333zM298.666667 384l60.16 60.16L469.333333 334.08V682.666667h85.333334V334.08l110.506666 110.08L725.333333 384l-213.333333-213.333333-213.333333 213.333333z"
                  p-id="2931"
                  fill="#ffffff"
                ></path>
              </svg>
            </div>
          </div>
          <div class="card-title" style="color: rgba(250, 161, 27, 1)">文件上传</div>
          <div class="card-desc">
            可以选择视频或是照片集进行上传，等待片刻之后得到数据分析结果。
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
//导入

import router from '@/router'
import controlCom from '@/components/controlCom.vue'
//初始化

const toMap = () => {
  router.push({ name: 'detection-map' })
}
const toFileUpload = () => {
  router.push({ name: 'file-upload' })
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

#home-view {
  overflow: hidden;
  position: fixed;
  width: 100vw;
  height: 100vh;
  top: 0;
  left: 0;

  .view-background {
    overflow: visible;
    position: absolute;
    z-index: 0;
    width: 100vw;
    height: 100vh;

    .background-mask {
      position: absolute;
      left: 0px;
      top: 0px;
      width: px-to-vw(1920);
      height: px-to-vh(1080);
      opacity: 1;
      background: linear-gradient(
        132.84deg,
        rgba(106, 168, 234, 1) 0%,
        rgba(162, 231, 211, 1) 100%
      );
    }
    .background-circle {
      position: absolute;
      left: px-to-vw(-1398.67);
      top: px-to-vh(714.43);
      width: px-to-vw(4717.33);
      height: px-to-vh(1426.86);
      border-radius: 50%;
      opacity: 1;
      background: rgba(255, 255, 255, 1);
    }
  }
  .view-content {
    display: flex;
    flex-direction: column;
    position: relative;
    z-index: 1;

    .header {
      position: relative;
      top: px-to-vh(50);
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      > div {
        margin: 0 5vw;
      }
      .smallTitle {
        opacity: 1;
        font-size: 30px;
        font-weight: 900;
        letter-spacing: 0px;
        line-height: 52.13px;
        color: rgba(255, 255, 255, 1);
        text-align: left;
        vertical-align: top;
      }
      .user {
        right: px-to-vw(49);
        color: rgba(255, 255, 255, 1);
      }
    }
    .Title {
      position: absolute;
      top: px-to-vh(193);
      left: 50%;
      transform: translateX(-50%);
      display: flex;
      flex-direction: column;
      align-items: center;
      .name {
        font-size: 60px;
        font-weight: 900;
        letter-spacing: 0px;
        line-height: 104.26px;
        color: rgba(255, 255, 255, 1);
        text-align: left;
        vertical-align: top;
      }
      .desc {
        font-size: 24px;
        font-weight: 500;
        letter-spacing: 0px;
        line-height: 34.75px;
        color: rgba(255, 255, 255, 1);
        text-align: left;
        vertical-align: top;
      }
    }
    .cards {
      position: relative;
      top: px-to-vh(450);
      left: 50%;
      transform: translateX(-50%);
      width: px-to-vw(1150);
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      > div {
        position: relative;
        width: px-to-vw(390);
        height: px-to-vh(369);
        opacity: 1;
        border-radius: 45px;
        background: rgba(255, 255, 255, 1);
        box-shadow:
          0 1px 3px rgba(0, 0, 0, 0.12),
          0 1px 2px rgba(0, 0, 0, 0.24);
        transition: box-shadow 0.3s ease-in-out;
        transition: transform 0.3s ease;
        &:hover {
          box-shadow:
            0 3px 6px rgba(0, 0, 0, 0.16),
            0 3px 6px rgba(0, 0, 0, 0.23);
          transform: scale(1.05) translateY(-10px);
        }
      }
    }
  }
}
.card-icon {
  position: absolute;
  border-radius: 25px;
  width: 50px;
  height: 50px;
  opacity: 1;
  top: 20px;
  left: 20px;
}
.innericon {
  width: 100%;
  height: 100%;
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
}
.card-title {
  position: absolute;
  top: 85px;
  left: 25px;
  height: 44px;
  opacity: 1;
  font-size: 30px;
  font-weight: 300;
  letter-spacing: 0px;
  line-height: 43.44px;
  text-align: left;
  vertical-align: top;
}
.card-desc {
  position: absolute;
  top: 145px;
  height: 35%;
  margin: 0px 25px;
  opacity: 1;
  font-size: 18px;
  font-weight: 300;
  letter-spacing: 0px;
  line-height: 26.06px;
  color: rgba(128, 128, 128, 1);
  text-align: left;
  vertical-align: top;
  overflow: hidden;
}
</style>
