<template>
  <div id="screenCom">
    <el-dialog v-model="showScreen" title="筛选小助手" width="500" :before-close="handleClose">
      <div class="container">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="80px"
          class="vehicle-form"
        >
          <!-- 日期选择 -->
          <el-form-item label="选择日期" prop="date">
            <el-date-picker
              v-model="formData.date"
              type="daterange"
              unlink-panels
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="To"
              start-placeholder="起始日期"
              end-placeholder="结束日期"
            >
            </el-date-picker>
          </el-form-item>


          <!-- 路段选择器 -->
          <el-form-item label="选择路段" prop="section" v-if="condition===true">
            <el-select v-model="formData.section" placeholder="请选择路段" clearable>
              <el-option
                v-for="item in sectionOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <!-- 车牌号输入 -->
          <el-form-item label="车牌号码" prop="plateNumber">
            <el-input v-model="formData.plateNumber" placeholder="请输入车牌号" clearable />
          </el-form-item>


          <!-- 车辆类型选择 -->
          <el-form-item label="车辆类型" prop="vehicleType">
            <el-select v-model="formData.vehicleType" placeholder="请选择车辆类型" clearable>
              <el-option
                v-for="item in vehicleTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <span><el-button plain @click="resetForm" type="primary">重置</el-button></span>
          <span
            ><el-button plain @click="submitForm"  type="success">确认</el-button>
            <el-button plain @click="handleClose">取消</el-button></span
          >
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'

const showScreen = ref(false)
const conDition = ref(true)
const props = defineProps({ ifshowScreen: Boolean , condition:Boolean})
// 表单数据类型
interface FormData {
  date: string
  section: string
  plateNumber: string
  vehicleType: string
}

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive<FormData>({
  date: '',
  section: '',
  plateNumber: '',
  vehicleType: '',
})

// 路段选项（示例数据）
const sectionOptions = [
  { value: 'section1', label: '路段一：中山路' },
  { value: 'section2', label: '路段二：人民路' },
  { value: 'section3', label: '路段三：解放路' },
]

// 车辆类型选项（示例数据）
const vehicleTypeOptions = [
  { value: '1', label: '小型客车' },
  { value: '2', label: '大型客车' },
  { value: '3', label: '货运卡车' },
  { value: '4', label: '特种车辆' },
]


// 验证规则
const formRules = reactive<FormRules<FormData>>({
  section: [{ required: false, message: '请选择路段', trigger: 'change' }],
  plateNumber: [
    { required: false, message: '请输入车牌号', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!value) {
          callback()
          return
        }
        if (
          !/^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$/.test(
            value,
          )
        ) {
          callback(new Error('请输入有效的车牌号码'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  vehicleType: [{ required: false, message: '请选择车辆类型', trigger: 'change' }],
})

// 提交表单
const submitForm = async () => {
  const isFormEmpty = Object.values(formData).every((value) => value === '')
  if (isFormEmpty) {
    ElMessage.warning('至少选择一项筛选')
    return
  }
  const [startTime,endTime]=formData.date
  const params = {
    startTime:startTime || '',
    endTime:endTime || '',
    location:formData.section || '',
    license:formData.plateNumber || '',
    type:formData.vehicleType || ''
  }
  if(conDition.value){
    console.log()
    const response = await request.get('/detections/realtime',{params:params})
    console.log(response)
  }

}

const handleClose = () => {
  console.log('close')
  showScreen.value = false
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
}

watch(
  () => props.ifshowScreen,
  () => {
    showScreen.value = true
  },
)
watch(
  () => props.condition,
  (newVal) => {
    conDition.value = newVal
    console.log('condition?'+conDition.value)
  }
)
</script>

<style scoped lang="scss">
.dialog-footer {
  padding: 0 10px;
  display: flex;
  justify-content: space-between;
}
</style>
