<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <div class="header">
          <span class="home"
            ><el-icon class="icon"><ArrowLeftBold /></el-icon
            ><RouterLink to="/" class="link">返回首页</RouterLink></span
          >
          <span class="manager" @click="handleManager">
            <span class="manage"
              >进入路段规划
              <el-icon class="icon"><ArrowLeftBold /></el-icon>
            </span>
          </span>
        </div>
      </el-header>
      <el-main>Main</el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// import sectionCom from '@/components/sectionCom.vue'

const road = ref({ dir1: 32, dir2: 54 })
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
      cursor: pointer;
  }
}
</style>
