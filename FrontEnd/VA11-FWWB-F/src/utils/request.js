import axios from 'axios'
import { useRouter } from 'vue-router';
const router = useRouter()

const axiosInstance = axios.create({
  baseURL:'https://8080/api',
  headers:{
    'Content-Type':'application/json',
    'X-Requested-With':'XMLHttpRequest'
  },
  withCredentials: true,
  responseType: 'json'
})

axiosInstance.interceptors.request.use(
  (config) => {
    // 在发送请求前处理
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    // 对请求错误处理
    return Promise.reject(error);
  }
);

// ----------------- 响应拦截器 -----------------
axiosInstance.interceptors.response.use(
  (response) => {
    // 2xx 范围内的状态码都会触发该函数
    // 可在此处处理全局响应结构
    if (response.data.code === 200) {
      return response.data.data;
    }
    return Promise.reject(response.data);
  },
  (error) => {
    // 超出 2xx 范围的状态码都会触发该函数
    const status = error.response?.status;

    // 统一错误处理
    switch (status) {
      case 401:
        // 跳转到登录页
        router.push({name:'home'})
        break;
      case 403:
        console.error('无访问权限');
        break;
      case 500:
        console.error('服务器错误');
        break;
      default:
        console.error('请求错误:', error.message);
    }

    return Promise.reject(error);
  }
);



export { axiosInstance as default };
