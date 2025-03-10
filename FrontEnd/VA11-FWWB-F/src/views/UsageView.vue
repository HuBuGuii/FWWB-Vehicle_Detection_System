<template>
  <div id="home-view">
    <div class="view-background">
      <div class="background-mask"></div>
      <div class="background-circle"></div>
    </div>
    <main class="view-content">
      <div class="header">
        <div class="smallTitle">VA11</div>
        <div class="user">用户123 已登入</div>
      </div>
      <div class="Title">
        <div class="name">智航云轨</div>
        <div class="desc">大型交通枢纽辖域内通行车辆智能检测系统</div>
      </div>

      <div class="cards">
        <div class="card1" @click="toMap()" style="cursor: pointer">
          <div class="card-icon" style="background-color: rgba(106, 168, 234, 1)"></div>
          <div class="card-title" style="color: rgba(27, 120, 227, 1)">实时监测</div>
          <div class="card-desc">
            进入到“监测地图”，您可以通过点击选中特定路段，进行更加细致的监测。
          </div>
        </div>

        <div class="card2" @click="toFileUpload()" style="cursor: pointer">
          <div class="card-icon" style="background-color: rgba(255, 213, 0, 1)"></div>
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
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

//初始化
const authStore = useAuthStore()

const toMap = () => {
  router.push({ name: 'detection-map' })
}
const toFileUpload = () => {
  router.push({ name: 'file-upload' })
}
</script>

<style scoped lang="scss">
$design-width: 1920;
$design-height: 1080;

@function px-to-vw($px) {
  @return ($px / $design-width) * 100vw;
}

@function px-to-vh($px) {
  @return ($px / $design-height) * 100vh;
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
    .buttons {
      width: px-to-vw(378);
      position: relative;
      top: px-to-vh(350);
      left: 50%;
      transform: translateX(-50%);
      display: flex;
      flex-direction: row;
      justify-content: space-between;

      .registerButton {
        width: px-to-vw(151);
        height: 72px;
        font-size: 24px;
        font-weight: 900;
        letter-spacing: 0px;
        line-height: 34.75px;
        color: rgba(255, 255, 255, 1);
        text-align: left;
        vertical-align: top;
      }
      .loginButton {
        width: px-to-vw(151);
        height: 72px;
        font-size: 24px;
        font-weight: 900;
        letter-spacing: 0px;
        line-height: 34.75px;
        color: rgba(255, 195, 0, 1);
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
