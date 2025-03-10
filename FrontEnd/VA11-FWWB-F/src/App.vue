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
  </div>
  <LogRegCom></LogRegCom>
 </div>

</template>

<script setup lang='ts'>
import {ref,reactive} from 'vue'
import LogRegCom from './components/LogRegCom.vue';


const cachedViews = reactive([])



</script>
<style scoped lang="scss">
#app{
  width: 100%;
  height: 100%;
}
</style>