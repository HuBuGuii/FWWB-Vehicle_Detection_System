<template>
  <div id="fileUpload">
    <div class="mainContent">
      <div class="title">实时监控</div>
      <div class="graph"></div>
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
      </div>
    </div>
    <div class="rightAside"></div>
  </div>
</template>

<script setup lang="ts"></script>

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
    flex-grow: 5;
    margin-right: 30px;
    padding: 0 30px;
    .title {
      margin: px-to-vh(30) 0 px-to-vh(15) 0;
      height: px-to-vh(80);
      font-size: 36px;
      font-weight: 900;
      letter-spacing: 0px;
      line-height: 52.13px;
      color: rgba(42, 90, 145, 1);
      text-align: left;
      vertical-align: top;
    }

    .graph{
      border-radius: 10px;
      background-color: darkgrey;
      flex:1;
    }

    .dashboard{
      height: px-to-vh(235);
      display: flex;
      margin: px-to-vh(30) 0;
      >div{
        width: 25%;
      }
      pie1{

      }
      pie2{

      }
      pie3{

      }
    }
  }
  .rightAside {
    position: relative;
    flex-grow: 3;
  }
}
</style>
