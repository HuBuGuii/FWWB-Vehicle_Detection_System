<template>
  <div id="sectionCom">
    <div class="card">
      <div class="title">
        <div class="total">
          <span class="name">总流量</span><span :class="totalClass">{{ total }}</span>
        </div>
        <div class="direction1">
          <span class="name">分车流量1</span><span :class="dir1Class">{{ props.road.dir1 }}</span>
        </div>
        <div class="direction2">
          <span class="name">分车流量2</span><span :class="dir2Class">{{ props.road.dir2 }}</span>
        </div>
      </div>
      <div class="graph">
        <span class="camera">{{props.camera}}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { watchEffect } from 'vue'
import { computed } from 'vue'
import { ref } from 'vue'

//接收父组件数据
const props = defineProps({
  road: {
    type: Object,
    required: true,
  },
  isFirst: {
    type: Boolean,
    default: false,
  },
  camera:{
    type:String,
    default:'demoCamera'
  }
})

//常量
const totalClass = ref('')
const dir1Class = ref('')
const dir2Class = ref('')

//方法
const decideClass = (value: number) => {
  if (value < 30) {
    return 'safe'
  }
  if (value < 100) {
    return 'normal'
  }
  return 'danger'
}

//computed
const total = computed(() => {
  return props.road.dir1 + props.road.dir2
})

//watch
watchEffect(() => {
  dir1Class.value = decideClass(props.road.dir1)
  dir2Class.value = decideClass(props.road.dir2)
  totalClass.value = decideClass(total.value / 2)
})
</script>

<style scoped lang="scss">
#sectionCom {
  width: 100%;
  height: 300px;
}
.card {
  box-sizing: border-box;
  padding: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: calc(100% - 10px);
  height: calc(100% - 10px);
  opacity: 1;
  border-radius: 20px;
  background: rgba(255, 255, 255, 1);
  border: 5px solid rgba(229, 229, 229, 1);
  .title {
    width: 100%;
    margin-bottom: 10px;
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: space-around;
    .name {
      font-size: 16px;
      font-weight: 400;
      letter-spacing: 0px;
      line-height: 28.96px;
      color: rgba(42, 130, 228, 1);
      text-align: left;
      vertical-align: top;
    }
    > div {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }
  }
  .graph {
    position: relative;
    background: rgba(229, 229, 229, 1);
    width: 98%;
    flex-basis: 50px;
    flex-grow: 1;
    .camera {
      position: absolute;
      right: 0;
      bottom: 0;
      transform: translateX(-10px) translateY(-10px);
    }
  }
}

.safe {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0px;
  line-height: 30px;
  color: rgba(165, 214, 63, 1);
  text-align: left;
  vertical-align: top;
}
.normal {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0px;
  line-height: 30px;
  color: rgba(255, 195, 0, 1);
  text-align: left;
  vertical-align: top;
}
.danger {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0px;
  line-height: 30px;
  color: rgba(212, 48, 48, 1);
  text-align: left;
  vertical-align: top;
}
</style>
