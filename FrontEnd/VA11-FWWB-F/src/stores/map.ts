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
      label: '示例节点1',
      dir1:70,
      dir2:30,
      camera: 'CAM-001',
      children: [
        {
          id: 3,
          dir1:50,
          dir2:50,
          label: '示例节点1-1',
          camera: 'CAM-003',
        },
      ],
    },
    {
      id: 2,
      dir1:90,
      dir2:10,
      label: '示例节点2',
      camera: 'CAM-002',
    },
  ])



  return{
    showManager,
    treeData
  }
},{
  persist:true,
})
