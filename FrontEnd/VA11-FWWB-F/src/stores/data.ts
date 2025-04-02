import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import request from '@/utils/request'


interface FilterParams{
  startTime: string
  endTime: string
  location: string  // 可选参数
  license: string
  type: string
}


export const useDataStore = defineStore('data', () => {
  // data
  const filters = reactive({
    startTime: '',
    endTime: '',
    location: '',
    license: '',
    type: '',
  })

  const pagination = reactive({
    nowPage: 0,
    pageSize: 13,
    totalPage: 0,
  })

  const isRealTime = ref(true)
  const isFiltered = ref(false)
  const currentData = ref([])

  const loadParams = () => {
    const baseParam = {
      pageNum:pagination.nowPage
    }

    if(!isFiltered.value){
      return baseParam
    }

    const filterParams: Partial<FilterParams> = {
      startTime: filters.startTime,
      endTime: filters.endTime,
      license: filters.license,
      type: filters.type,
      ...(isRealTime.value && filters.location? {location:filters.location}:{})
    }

    // 只在实时数据时添加 location 参数


    return {
      ...baseParam,
      ...filterParams
    }

  }

  const loadData = async() => {
    try {
      const url = isRealTime.value? '/detections/realtime':'/detections/nonrealtime'
      const params = loadParams()
      console.log('params is ===' + params)
      const response = await request.get(url,{params})
      console.log('response is ===' + response)
    } catch (error) {
      console.log(error)
    }
  }

  const applyFilters = async() => {
    pagination.nowPage = 0
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
    loadData()
  }

  const handlePageChange = (page:number) => {
    pagination.nowPage = page
    loadData()
  }



  return {
    filters,
    pagination,
    isFiltered,
    isRealTime,
    currentData,
    resetFilter,
    applyFilters,
    loadData,
    handlePageChange
  }
})
