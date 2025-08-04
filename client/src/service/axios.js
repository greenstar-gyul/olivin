// src/service/axios.js
import axios from 'axios'

// 기본 axios 인스턴스 생성
const instance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:3049/', // 환경변수 사용
  timeout: 15000, // 15초 타임아웃
  headers: {
    'Content-Type': 'application/json',
  }
})

// 요청 인터셉터 - 모든 요청에 토큰 자동 추가
instance.interceptors.request.use(
  (config) => {
    // localStorage에서 토큰 가져와서 헤더에 자동 추가
    // const token = localStorage.getItem('token')
    const token = JSON.parse(localStorage.getItem('auth')).token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 요청 로그 (개발 환경에서만)
    if (import.meta.env.DEV) {
      console.log(`🚀 API 요청: ${config.method?.toUpperCase()} ${config.url}`)
    }
    
    return config
  },
  (error) => {
    console.error('❌ 요청 에러:', error)
    return Promise.reject(error)
  }
)

// 응답 인터셉터 - 에러 처리 및 자동 로그아웃
instance.interceptors.response.use(
  (response) => {
    // 성공 응답 로그 (개발 환경에서만)
    if (import.meta.env.DEV) {
      console.log(`✅ API 응답: ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data)
    }
    return response
  },
  (error) => {
    const { response } = error
    
    // 응답이 있는 경우 (서버 에러)
    if (response) {
      switch (response.status) {
        case 401:
          // 토큰 만료 또는 유효하지 않음 - 자동 로그아웃
          console.warn('🔒 인증 만료, 자동 로그아웃')
          localStorage.removeItem('token')
          delete instance.defaults.headers.common['Authorization']
          
          // 로그인 페이지가 아니면 리다이렉트
          if (window.location.pathname !== '/auth/login') {
            window.location.href = '/auth/login'
          }
          break
          
        case 403:
          console.error('🚫 권한 없음')
          // 권한 없음 페이지로 리다이렉트하거나 알림 표시
          break
          
        case 404:
          console.error('🔍 요청한 리소스를 찾을 수 없음')
          break
          
        case 422:
          console.error('📝 입력 데이터 유효성 검사 실패')
          break
          
        case 500:
          console.error('💥 서버 내부 오류')
          break
          
        default:
          console.error(`❌ API 에러 (${response.status}):`, response.data)
      }
    } else if (error.request) {
      // 요청은 보냈지만 응답을 받지 못함 (네트워크 에러)
      console.error('🌐 네트워크 에러: 서버에 연결할 수 없습니다')
    } else {
      // 요청 설정 중 에러 발생
      console.error('⚙️ 요청 설정 에러:', error.message)
    }
    
    return Promise.reject(error)
  }
)

// 편의 메서드들 추가
instance.apiGet = (url, config = {}) => instance.get(url, config)
instance.apiPost = (url, data, config = {}) => instance.post(url, data, config)
instance.apiPut = (url, data, config = {}) => instance.put(url, data, config)
instance.apiPatch = (url, data, config = {}) => instance.patch(url, data, config)
instance.apiDelete = (url, config = {}) => instance.delete(url, config)

// 파일 업로드용 메서드
instance.uploadFile = (url, formData, onUploadProgress) => {
  return instance.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

export default instance