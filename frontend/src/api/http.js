import axios from 'axios';

export const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
});

http.interceptors.response.use(
  (res) => res,
  (error) => {
    const message = error?.response?.data?.message || error?.message || '请求失败';
    return Promise.reject(new Error(message));
  },
);
