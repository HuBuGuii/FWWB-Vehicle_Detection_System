<template>
  <div class="flow-chart">
    <VueFlow
      :model-value="elements"
      :nodes="nodes"
      :edges="edges"
      :default-viewport="{ x: 0, y: 0, zoom: 0.65 }"
      :min-zoom="0.2"
      :max-zoom="4"
      :fit-view-on-init="true"
      :fit-view-padding="0.2"
      @nodeClick="onNodeClick"
      class="vue-flow-wrapper"
    >
      <Background pattern-color="#aaa" :gap="20" :size="1" variant="dots" />
      <MiniMap position="bottom-right" />

      <template #node-custom="props">
        <div class="custom-node">
          <Handle
            type="target"
            :position="position.Left"
            :style="{ background: '#555' }"
          />
          <SectionCom
            :road="props.data.road"
            :camera="props.data.camera"
          />
          <Handle
            type="source"
            :position="position.Right"
            :style="{ background: '#555' }"
          />
        </div>
      </template>
    </VueFlow>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { VueFlow, useVueFlow, Handle, Position } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { MiniMap } from '@vue-flow/minimap'
import { MarkerType } from '@vue-flow/core'
import type { Edge, Node } from '@vue-flow/core'
import SectionCom from '@/components/sectionCom.vue'
import { useMapStore } from '@/stores/map'
import { useRouter } from 'vue-router'
import type { NodeMouseEvent } from '@vue-flow/core'

import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/minimap/dist/style.css'

interface FlowNode extends Node {
  id: string
  type: string
  position: { x: number; y: number }
  data: {
    road: { dir1: number; dir2: number }
    camera: string
  }
  draggable?: boolean
  selectable?: boolean
  connectable?: boolean
}

interface TreeNode {
  id: number
  dir1: number
  dir2: number
  camera: string
  children?: TreeNode[]
}

const elements = ref<(FlowNode | Edge)[]>([])
const mapStore = useMapStore()
const nodes = ref<FlowNode[]>([])
const edges = ref<Edge[]>([])
const { fitView } = useVueFlow()
const position = Position
const router = useRouter()

const onNodeClick = (event: NodeMouseEvent) => {
  console.log('Node clicked:', event.node)  // 调试输出

  if (event.node?.data) {
    console.log('Navigating to:', event.node.data)  // 调试输出
    router.push({
      name:'realtime',
      query: {
        camera: event.node.data.camera,
        dir1: event.node.data.road.dir1.toString(),
        dir2: event.node.data.road.dir2.toString()
      }
    })
  }
}

const transformTreeToFlow = (
  treeNodes: TreeNode[],
  parent: { id?: string; x: number; y: number } | null = null,
  horizontalSpacing = 400,
  verticalSpacing = 250,
): { nodes: FlowNode[]; edges: Edge[] } => {
  const resultNodes: FlowNode[] = []
  const resultEdges: Edge[] = []

  treeNodes.forEach((node, index) => {
    const nodeId = `node-${node.id}`
    const xPos = parent ? parent.x + horizontalSpacing : 0
    const yPos = index * verticalSpacing

    resultNodes.push({
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
      draggable: true,
      selectable: true,
      connectable: true,
    })

    if (parent?.id) {
      resultEdges.push({
        id: `edge-${parent.id}-${nodeId}`,
        source: parent.id,
        target: nodeId,
        type: 'smoothstep',
        animated: true,
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

    if (node.children?.length) {
      const childrenResult = transformTreeToFlow(
        node.children,
        { id: nodeId, x: xPos, y: yPos },
        horizontalSpacing,
        verticalSpacing,
      )
      resultNodes.push(...childrenResult.nodes)
      resultEdges.push(...childrenResult.edges)
    }
  })

  return { nodes: resultNodes, edges: resultEdges }
}

const updateView = () => {
  nextTick(() => {
    fitView({
      padding: 0.2,
      minZoom: 0.65,
      maxZoom: 0.65
    })
  })
}

onMounted(() => {
  window.addEventListener('resize', updateView)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateView)
})

watch(
  () => mapStore.treeData,
  (newValue: TreeNode[]) => {
    if (!newValue?.length) return
    const { nodes: flowNodes, edges: flowEdges } = transformTreeToFlow(newValue)
    nodes.value = flowNodes
    edges.value = flowEdges
    updateView()
  },
  { immediate: true }
)
</script>

<style scoped>
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

.custom-node {
  position: relative;
  width: 260px;
  height: 160px;
  cursor: pointer;
}

:deep(.vue-flow__handle) {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #555;
  border: 2px solid white;
}

:deep(.vue-flow__handle-left) {
  left: -4px;
}

:deep(.vue-flow__handle-right) {
  right: -4px;
}

:deep(.vue-flow__edge-path) {
  stroke-linecap: round;
  stroke: #94a3b8;
  stroke-width: 2;
}

:deep(.vue-flow__edge:hover .vue-flow__edge-path) {
  stroke: #64748b;
  stroke-width: 3;
}

:deep(.vue-flow__minimap) {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

:deep(.vue-flow__pane) {
  cursor: move;
}
</style>
