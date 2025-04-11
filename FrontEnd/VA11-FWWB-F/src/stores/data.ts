import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'


interface FilterParams {
  startTime?: string
  endTime?: string
  location?: string
  license?: string
  type?: string
  pageNum:number
}

interface VehicleInfo {
  vehicleId: number
  licence: string | null
  type: string
}

interface BaseDetectionRecord {
  time: Date
  confidence: number
  vehicleId: number
  vehicleStatus: string
  maxAge: number
  exp: string
  vehicleInfo?: VehicleInfo
}

interface RealTimeDetectionRecord extends BaseDetectionRecord {
  rdId: string
  cameraId: string
  temperature: number
  weather: string
}

interface NonRealTimeDetectionRecord extends BaseDetectionRecord {
  nrdId: string
  userId: string
}

type DetectionRecord = RealTimeDetectionRecord | NonRealTimeDetectionRecord

interface ApiResponse {
  records: DetectionRecord[]
  totalPages: number
  // 其他可能的响应字段
}


export const useDataStore = defineStore('data', () => {
  // 筛选相关的状态
  const filters = reactive({
    startTime: '',
    endTime: '',
    location: '',
    license: '',
    type: '',
  })

  const pagination = reactive({
    nowPage: 1,
    pageSize: 13,
    totalPage: 0,
  })

  // 原有的数据相关状态
  const isRealTime = ref(true)
  const isFiltered = ref(false)
  const midData =  ref<DetectionRecord[]>([])
  const showData = ref<DetectionRecord[]>([])
  const loading = ref(false)

  // 计算属性
  const nowCon = computed(() => {
    return isRealTime.value ? '实时监测' : '文件上传'
  })

  // 筛选相关方法
  const loadParams = (): FilterParams => {
    const allParams: FilterParams = {
      pageNum: pagination.nowPage
    }

    // 只添加有值的参数，空字符串不添加
    if (filters.startTime) allParams.startTime = filters.startTime
    if (filters.endTime) allParams.endTime = filters.endTime
    if (filters.type) allParams.type = filters.type
    if (filters.license) allParams.license = filters.license
    if (isRealTime.value && filters.location) allParams.location = filters.location

    console.log('allparams is ===')
    console.log(allParams)
    return allParams
  }

  const applyFilters = async () => {
    pagination.nowPage = 1
    isFiltered.value = true
    await loadData()
  }

  const resetFilter = () => {
    filters.endTime = ''
    filters.startTime = ''
    filters.license = ''
    filters.location = ''
    filters.type = ''
    pagination.nowPage = 0
    isFiltered.value = false
    getData()
  }

  const getData = async () => {
    loading.value = true
    const recordType = isRealTime.value ? 'realtime' : 'nonrealtime'

    try {
      const response = await request.get(
        `/detections/${recordType}/${pagination.nowPage}`
      )
      console.log('API Response:', response) // 检查原始响应

      pagination.totalPage = response.pages

      if (response.records) {
        midData.value = response.records
        await fetchVehicleInfo() // 获取到记录后立即获取车辆信息
      }

      showData.value = midData.value

    } catch (error) {
      console.error('获取数据失败:', error)
      ElMessage.error('获取数据失败')
    } finally {
      loading.value = false
      console.log('getData is OK')
      console.log(showData.value)
    }
  }

  // 修改 fetchVehicleInfo 方法，使用原来view中的逻辑
  const fetchVehicleInfo = async () => {
    if (!midData.value.length) return;

    loading.value = true;
    try {
      console.log('原始数据:', midData.value); // 检查原始数据

      // 安全地获取 vehicleId
      const vehicleIds = [...new Set(
        midData.value
          .filter(record => record && typeof record === 'object') // 确保记录是对象
          .map(record => {
            console.log('处理记录:', record); // 检查每条记录
            return record.vehicleId || record.vehicleId; // 尝试两种可能的属性名
          })
          .filter((id): id is number => {
            console.log('过滤 ID:', id, typeof id); // 检查 ID 值和类型
            return typeof id === 'number' && !isNaN(id);
          })
      )];

      console.log('提取的车辆 IDs:', vehicleIds);

      if (vehicleIds.length === 0) {
        console.warn('没有有效的车辆ID');
        return;
      }

      const responses = await Promise.all(
        vehicleIds.map(async id => {
          try {
            const response = await request.get<VehicleInfo>(`/vehicles/info/${id}`);
            console.log(response)
            return response as unknown as VehicleInfo;
          } catch (error) {
            console.error(`获取车辆 ${id} 信息失败:`, error);
            return null;
          }
        })
      );

      const validResponses = responses.filter((response): response is VehicleInfo => response !== null);

      const vehicleMap = new Map(
        validResponses.map(vehicle => [vehicle.vehicleId, vehicle])
      );

      // 更新记录时也要安全地处理
      midData.value = midData.value.map(record => {
        const vehicleId = record.vehicleId || record.vehicleId;
        return {
          ...record,
          vehicleInfo: vehicleId ? vehicleMap.get(vehicleId) : undefined
        };
      });

      showData.value = midData.value

    } catch (error) {
      console.error('获取车辆信息失败:', error);
      ElMessage.error('获取车辆信息失败');
    } finally {
      loading.value = false;
    }
  }

  // 统一的数据加载方法
  const loadData = async () => {
    try {
      loading.value = true
      const baseUrl = `${isRealTime.value ? '/detections/realtime' : '/detections/nonrealtime'}/search`
      const params = loadParams()

      // 确保日期格式符合 ISO 标准
      if (params.startTime) {
        params.startTime = new Date(params.startTime).toISOString()
      }
      if (params.endTime) {
        params.endTime = new Date(params.endTime).toISOString()
      }

      console.log('request params:', params)
      const response = await request.get<ApiResponse>(baseUrl, { params })

      if (response.data.records) {
        showData.value = response.data.records
        await fetchVehicleInfo()
      }
    } catch (error) {
      console.log('Error details:', error)
      ElMessage.error('加载数据失败')
    } finally {
      loading.value = false
    }
  }

  const handlePageChange = (page: number) => {
    pagination.nowPage = page
    console.log(pagination.nowPage)
    getData()
    console.log(showData)
  }

  const clearF = () => {
    isFiltered.value = false
    getData()
  }

  return {
    // 状态
    filters,
    pagination,
    isFiltered,
    isRealTime,
    showData,
    loading,
    // 计算属性
    nowCon,
    // 方法
    resetFilter,
    applyFilters,
    loadData,
    getData,
    fetchVehicleInfo,
    handlePageChange,
    clearF
  }
})
