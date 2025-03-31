import axios, {
  AxiosInstance,
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosError
} from 'axios';

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  },
  withCredentials: true,
  responseType: 'json'
});

// 请求拦截器 - 使用 InternalAxiosRequestConfig
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// 响应拦截器保持不变
request.interceptors.response.use(
  (response: AxiosResponse) => {
    // 情况1：标准JSON响应（含code字段）
    if (typeof response.data === 'object' && 'code' in response.data) {
      if (response.data.code === 200) {
        return response.data.data; // 返回data部分
      }
      return Promise.reject(response.data); // 业务错误
    }

    // 情况2：纯token字符串或其他结构
    return response.data; // 直接返回原始数据
  },
  (error: AxiosError) => {
    // 处理HTTP错误（如401）
    if (error.response?.status === 401) {
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default request;
