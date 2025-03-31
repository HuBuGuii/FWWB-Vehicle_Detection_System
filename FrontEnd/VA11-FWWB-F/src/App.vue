<template>
  <div id="app">
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <Transition name="changeComponent">
          <KeepAlive :include="cachedViews">
            <component :is="Component"></component>
          </KeepAlive>
        </Transition>
      </router-view>
    </main>

    <div class="dialog">
      <LogRegCom></LogRegCom>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import LogRegCom from './components/LogRegCom.vue'

const cachedViews = reactive([])
</script>
<style scoped lang="scss">
#app {
  height: 100%;
  width: 100%;
  overflow: hidden;
}
* {
  overflow: hidden;
}
.main-content {
  overflow: hidden;
}
.dialog {
  height: 100%;
  width: 100%;
  overflow: hidden;
}
</style>
