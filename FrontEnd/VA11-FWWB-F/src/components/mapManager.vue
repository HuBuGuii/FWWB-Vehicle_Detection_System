<template>
  <div class="tree-container">
    <el-dialog v-model="map.showManager" title="路段规划小助手" :before-close="handleclose">
      <el-tree
        :data="treeData"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        :props="defaultProps"
      >
        <template #default="{ node, data }">
          <span class="custom-node">
            <!-- 编辑状态 -->
            <template v-if="data.editing">
              <div class="edit-wrapper">
                <el-input
                  v-model="editLabel"
                  size="small"
                  placeholder="节点名称"
                  @keyup.enter="saveEdit(node, data)"
                />
                <el-input
                  v-model="editCamera"
                  size="small"
                  placeholder="摄像头ID"
                  @keyup.enter="saveEdit(node, data)"
                />
              </div>
              <el-button link size="small" :icon="Check" @click="saveEdit(node, data)" />
            </template>

            <!-- 正常状态 -->
            <template v-else>
              <span class="node-content">
                <span class="label">{{ node.label }}</span>
                <span class="camera">(摄像头: {{ data.camera }})</span>
              </span>
              <span class="node-actions">
                <el-button link size="small" :icon="Plus" @click="addNode(data)" />
                <el-button link size="small" :icon="Edit" @click="startEdit(data)" />
                <el-button link size="small" :icon="Delete" @click="removeNode(node, data)" />
              </span>
            </template>
          </span>
        </template>
      </el-tree>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, toRaw } from 'vue'
import { Check, Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { TreeNodeData } from 'element-plus/es/components/tree/src/tree.type'
import { ElMessage } from 'element-plus'
import { useMapStore } from '@/stores/map'
import { useRouter } from 'vue-router'

const router = useRouter()
const map = useMapStore()

interface TreeNode {
  id: number
  label: string
  camera: string
  dir1: number
  dir2: number
  children?: TreeNode[]
  editing?: boolean
}

// 将store数据转换为响应式数据
const treeData = ref<TreeNode[]>(map.treeData.map(node => ({
  ...node,
  editing: false
})))

const defaultProps = {
  children: 'children',
  label: 'label',
}

const editLabel = ref('')
const editCamera = ref('')
let nodeId = 1000

// 添加节点
const addNode = (data: TreeNode) => {
  const newChild: TreeNode = {
    id: nodeId++,
    label: '新节点',
    camera: '新摄像头',
    dir1: 0,
    dir2: 0,
    children: [],
  }

  if (!data.children) {
    data.children = []
  }
  data.children.push(newChild)
}

// 删除节点
const removeNode = (node: TreeNodeData, data: TreeNode) => {
  const parent = node.parent
  const children = parent?.data?.children || parent?.data || treeData.value
  const index = children.findIndex((d: TreeNode) => d.id === data.id)
  if (index !== -1) {
    children.splice(index, 1)
  }

  ElMessage({
    message: '节点已删除',
    type: 'error'
  })
}

// 开始编辑
const startEdit = (data: TreeNode) => {
  data.editing = true
  editLabel.value = data.label
  editCamera.value = data.camera
}

// 保存编辑
const saveEdit = (node: TreeNodeData, data: TreeNode) => {
  data.editing = false
  data.label = editLabel.value
  data.camera = editCamera.value
  editLabel.value = ''
  editCamera.value = ''
  ElMessage({
    message: '编辑已保存',
    type: 'success'
  })
}

const handleclose = () => {
  const rawData = toRaw(treeData.value)
  console.log(rawData)
  map.treeData = rawData
  map.showManager = false
  // 添加页面刷新
  router.go(0)
}
</script>

<style scoped>
.custom-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  font-weight: 500;
}

.camera {
  color: #666;
  font-size: 0.9em;
}

.edit-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-right: 8px;
}

.node-actions {
  margin-left: 10px;
  opacity: 0;
  transition: opacity 0.3s;
}

.custom-node:hover .node-actions {
  opacity: 1;
}
</style>
