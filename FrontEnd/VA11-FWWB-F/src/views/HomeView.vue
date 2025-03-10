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
      <div class="buttons">
        <el-button
          @click="handleRegister"
          color="rgba(255, 213, 3, 1)"
          class="registerButton"
          :round="true"
        >
          <template #icon
            ><svg
              t="1740399189175"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="2082"
              width="200"
              height="200"
            >
              <path
                d="M320 307.2c6.4-95.2 84.8-172.8 180-178.4 111.2-6.4 204 81.6 204 192 0 105.6-85.6 192-192 192-110.4 0-199.2-93.6-192-205.6zM575.2 768c0-72.8 40.8-136.8 100.8-168.8C616 584 556 576 511.2 576 384 576 128 640 128 768v96c0 17.6 14.4 32 32 32h464c-30.4-33.6-48.8-78.4-48.8-128z m320.8-32h-96V640h-64v96H640v64h96v96h64v-96h96v-64z"
                p-id="2083"
              ></path></svg></template
          ><span>注册</span></el-button
        >
        <el-button
          @click="handleLogin"
          color="rgba(255, 255, 255, 1)"
          class="loginButton"
          :round="true"
          ><template #icon
            ><svg
              t="1740399283118"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="3086"
              width="200"
              height="200"
            >
              <path
                d="M783.69 882.537H289.438c-54.588 0-98.848-44.243-98.848-98.811v-98.808h49.424v98.808c0 27.286 22.132 49.406 49.424 49.406H783.69c27.294 0 49.423-22.124 49.423-49.406V240.274c0-27.287-22.133-49.407-49.423-49.407H289.438c-27.297 0-49.424 22.12-49.424 49.407v98.807H190.59v-98.807c0-54.568 44.26-98.812 98.848-98.812H783.69c54.587 0 98.847 44.243 98.847 98.812v543.451c0 54.568-44.26 98.812-98.847 98.812zM529.746 357.802c-9.63-9.649-9.63-25.304 0-34.93 9.65-9.65 25.313-9.65 34.967 0l170.501 170.432c5.139 5.14 7.335 11.966 6.999 18.695 0.336 6.731-1.86 13.557-6.999 18.723l-170.501 170.43c-9.655 9.648-25.318 9.648-34.967 0-9.63-9.65-9.63-25.306 0-34.932l129.57-129.52h-493.14c-13.637 0-24.713-11.047-24.713-24.702 0-13.627 11.076-24.703 24.711-24.703h493.143L529.746 357.8z m-40.774 0"
                p-id="3087"
              ></path></svg></template
          ><span>登录</span></el-button
        >
      </div>
      <div class="cards">
        <div class="card1">
          <div class="card-icon" style="background-color: rgba(106, 168, 234, 1)"></div>
          <div class="card-title" style="color: rgba(27, 120, 227, 1);">智能预警</div>
          <div class="card-desc">
            我们的系统可以自动分析车辆的行驶轨迹，并实时预警出车辆异常行为，帮助交通运输管理者及时发现车辆违规行为，保障交通安全。


          </div>
        </div>
        <div class="card2">
          <div class="card-icon" style="background-color: rgba(255, 213, 0, 1);"></div>
          <div class="card-title" style="color: rgba(250, 161, 27, 1);">路段规划</div>
          <div class="card-desc">
            通过我们的路段规划系统，您可以快速、准确地规划出交通枢纽的路线，并实时掌握交通状况，确保交通畅通。


          </div>
        </div>
        <div class="card3">
          <div class="card-icon" style="background-color: rgba(162, 231, 211, 1);"></div>
          <div class="card-title" style="color: rgba(162, 231, 211, 1);">实时追踪</div>
          <div class="card-desc">
            通过我们的实时追踪系统，您可以随时掌握车辆的位置、速度、方向、状态等信息，并实时掌握交通状况，确保交通畅通。


          </div>
        </div>
      </div>
    </main>


  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'



const authStore = useAuthStore()
const handleRegister = () => {
  authStore.RegisterVisible = true
  console.log('register')
}

const handleLogin = () => {
  authStore.LoginVisible = true
  console.log('login')
}


</script>

<style scoped lang="scss">
@use "sass:math";
$design-width: 1920;
$design-height: 1080;

@function px-to-vw($px) {
  @return math.div($px,1920) * 100vw;
}

@function px-to-vh($px) {
  @return math.div($px,1080) * 100vh;
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
      background: linear-gradient(132.84deg, rgba(106, 168, 234, 1) 0%, rgba(162, 231, 211, 1) 100% );
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
        color:rgba(255, 255, 255, 1);
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
      width: px-to-vw(1433);
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
  top:145px;
  height: 35%;
  margin: 0px 25px ;
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
