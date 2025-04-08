<template>
  <div class="mapView">
    <div class="common-layout">
    <el-container>
      <el-header>
        <div class="header">
          <span class="home"
            ><el-icon class="icon"><ArrowLeftBold /></el-icon
            ><RouterLink to="/" class="link">返回首页</RouterLink></span
          >
          <span class="controlCom" >
            <ControlCom></ControlCom>
            <span class="manage"
              ><el-button plain @click="handleManager" >进入路段规划</el-button>
              <el-icon class="icon"><ArrowLeftBold /></el-icon>
            </span>
          </span>
        </div>
      </el-header>
      <el-main>
        <div class="container">
          <FlowChart></FlowChart>
        </div>
      </el-main>
    </el-container>
  </div>
  <div class="manager">
    <MapManager></MapManager>
  </div>
  </div>
</template>

<script setup lang="ts">
import { useMapStore } from '@/stores/map'
import MapManager from '@/components/mapManager.vue'
import FlowChart from '@/components/flowChart.vue'
import ControlCom from '@/components/controlCom.vue'


const map = useMapStore()

const roadData = map.treeData

const getLevelElementCounts = <T>(arr: Array<T | Array<T>>): { [level: number]: number } => {
  const levelCounts: { [level: number]: number } = {};

  const traverse = (array: Array<T | Array<T>>, level: number): void => {
    if (!Array.isArray(array)) {
      return;
    }

    // 初始化当前层级的计数
    if (!levelCounts[level]) {
      levelCounts[level] = 0;
    }

    // 增加当前层级的元素计数
    levelCounts[level] += array.length;

    // 递归遍历子数组
    for (const item of array) {
      if (Array.isArray(item)) {
        traverse(item, level + 1);
      }
    }
  };

  traverse(arr, 0);
  return levelCounts;
};


const levels = getLevelElementCounts(roadData)
console.log(levels)

const handleManager = () => {
  map.showManager = true
  console.log(map.showManager)
}
</script>

<style scoped lang="scss">
.el-header {
  border-bottom: 3px solid rgba(229, 229, 229, 1);
}
.header {
  box-sizing: border-box;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .home {
    display: flex;
    align-items: center;
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
      color: rgba(42, 90, 145, 1);
    }
  }
  .manager{
    display: flex;
    align-items: center;
  }

}
.container{
  width: 100%;
  height: 100%;
}
.el-main{
  height: calc(100vh - 60px);
  padding:0;
}
.controlCom{
  display: flex;
  align-items: center;
  .manage{
    display: flex;
    align-items: center;
  }
}
</style>
