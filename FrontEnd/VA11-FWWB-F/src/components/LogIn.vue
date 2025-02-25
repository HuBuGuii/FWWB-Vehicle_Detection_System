<template>
  
    <el-dialog v-model="dialogVisible" title="Tips" width="40vw" :before-close="handleClose">
      <div class="pic-content"></div>
      <el-form ref="form" :model="form" label-width="80px">

      </el-form>


    </el-dialog>
  
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'


</script>

<style scoped>

</style>
