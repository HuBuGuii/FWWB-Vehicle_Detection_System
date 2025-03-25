<template>
  <div class="flow-chart">
    <VueFlow
      :nodes="nodes"
      :edges="edges"
      :default-viewport="{ zoom: 1 }"
      :min-zoom="0.2"
      :max-zoom="4"
      class="vue-flow-wrapper"
      ref="vueFlowRef"
    >
      <Background pattern-color="#aaa" :gap="20" :size="1" variant="dots" />

      <MiniMap position="bottom-right" />

      <template #node-custom="nodeProps">
        <SectionCom :road="nodeProps.node.data.road" :camera="nodeProps.node.data.camera" />
      </template>
    </VueFlow>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, watch, nextTick } from 'vue'
import { VueFlow, Node, Edge, MarkerType } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { MiniMap } from '@vue-flow/minimap'
import { debounce } from 'lodash-es'
import SectionCom from '@/components/sectionCom.vue'
import { useMapStore } from '@/stores/map'

// 必须导入样式
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
// import '@vue-flow/background/dist/style.css'
import '@vue-flow/minimap/dist/style.css'

interface FlowNode extends Node {
  id: string
  type?: string
  position: { x: number; y: number }
  data: {
    road: { dir1: number; dir2: number }
    camera: string
  }
  __vf?: {
    sourcePosition: 'right'
    targetPosition: 'left'
  }
}

interface TreeNode {
  id: number
  dir1: number
  dir2: number
  camera: string
  children?: TreeNode[]
}

export default defineComponent({
  components: {
    VueFlow,
    Background,
    MiniMap,
    SectionCom,
  },
  setup() {
    const mapStore = useMapStore()
    const nodes = ref<FlowNode[]>([])
    const edges = ref<Edge[]>([])
    const vueFlowRef = ref<any>()

    const transformTreeToFlow = (
      treeNodes: TreeNode[],
      parent: { id?: string; x: number; y: number } | null = null,
      horizontalSpacing = 500,
      verticalSpacing = 300,
    ): { nodes: FlowNode[]; edges: Edge[] } => {
      const resultNodes: FlowNode[] = []
      const resultEdges: Edge[] = []

      treeNodes.forEach((node, index) => {
        const nodeId = `node-${node.id}`
        const xPos = parent ? parent.x + horizontalSpacing : 0
        const baseY = parent
          ? parent.y - (treeNodes.length * verticalSpacing) / 2
          : -((treeNodes.length - 1) * verticalSpacing) / 2

        const yPos = baseY + index * verticalSpacing

        // 生成节点
        const flowNode: FlowNode = {
          id: nodeId,
          type: 'custom',
          position: { x: xPos, y: yPos },
          data: {
            road: {
              dir1: Number(node.dir1) || 0,
              dir2: Number(node.dir2) || 0,
            },
            camera: node.camera,
          },
          __vf: {
            sourcePosition: 'right',
            targetPosition: 'left',
          },
        }
        resultNodes.push(flowNode)

        // 生成边
        if (parent?.id) {
          resultEdges.push({
            id: `edge-${parent.id}-${nodeId}`,
            source: parent.id,
            target: nodeId,
            type: 'smoothstep',
            style: {
              stroke: '#94a3b8',
              strokeWidth: 2,
            },
            markerEnd: {
              type: MarkerType.ArrowClosed,
              color: '#94a3b8',
            },
          })
        }

        // 递归处理子节点
        if (node.children?.length) {
          const childrenResult = transformTreeToFlow(
            node.children,
            { id: nodeId, x: xPos, y: yPos },
            horizontalSpacing * 0.9,
            verticalSpacing * 1.2,
          )
          resultNodes.push(...childrenResult.nodes)
          resultEdges.push(...childrenResult.edges)
        }
      })

      return { nodes: resultNodes, edges: resultEdges }
    }

    watch(
      mapStore.treeData,
      debounce((newValue: TreeNode[]) => {
        const { nodes: flowNodes, edges: flowEdges } = transformTreeToFlow(newValue)
        console.log('Nodes:', flowNodes)
        console.log('Edges:', flowEdges)
        nodes.value = flowNodes
        edges.value = flowEdges
        nextTick(() => {
          vueFlowRef.value?.$el.fitView({ padding: 0.3 })
        })
      }, 300),
    )

    return {
      nodes,
      edges,
      vueFlowRef,
    }
  },
})
</script>

<style scoped>
/* 原有样式保持不变 */
.flow-chart {
  position: relative;
  width: 100vw;
  height: 100vh;
}

.vue-flow-wrapper {
  width: 100%;
  height: 100%;
  background: #f8fafc;
}

:deep(.vue-flow__edge-path) {
  stroke-linecap: round;
}

:deep(.vue-flow__edge:hover .vue-flow__edge-path) {
  stroke: #64748b;
}

:deep(.vue-flow__minimap) {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
</style>
