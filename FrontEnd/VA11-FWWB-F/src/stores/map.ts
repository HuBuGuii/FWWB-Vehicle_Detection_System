import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useMapStore = defineStore('map',() => {
  interface TreeNode {
    id: number
    label: string
    dir1:number
    dir2:number
    camera: string
    children?: TreeNode[]
    editing?: boolean
  }

  const showManager = ref(false)

  const treeData = ref<TreeNode[]>([
    {
      id: 1,
      label: '空港大道',
      dir1:70,
      dir2:30,
      camera: 'CAM-KG',
      children: [
        {
          id: 3,
          dir1:50,
          dir2:50,
          label: 'T3航站楼',
          camera: 'CAM-T3',
          children:[
            {id:5,
              dir1:40,
              dir2:90,
              label:'T1航站楼',
              camera:'CAM-T1',
              children:[{
                id:6,
                dir1:10,
                dir2:20,
                label:'T2航站楼',
                camera:'CAM-T2'
              }]
            }
          ]
        },
        {
          id:4,
          dir1:30,
          dir2:80,
          label:'T4航站楼',
          camera:'CAM-T4',
        },
      ],
    },
    {
      id: 2,
      dir1:90,
      dir2:10,
      label: '示例节点',
      camera: 'CAM-DEMO',
    },
  ])



  return{
    showManager,
    treeData
  }
},{
  persist:true,
})
