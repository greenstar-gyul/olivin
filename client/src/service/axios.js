// src/service/axios.js
import axios from 'axios';

// 기본 axios 인스턴스 생성
const instance = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://43.200.191.236:3049/',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
});

// 요청 인터셉터 - 모든 요청에 토큰 자동 추가
instance.interceptors.request.use(
    (config) => {
        let token = null;
        try {
            const authRaw = localStorage.getItem('auth');
            token = authRaw ? JSON.parse(authRaw)?.token : null;
        } catch (e) {
            console.warn('auth 파싱 실패:', e);
        }

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        if (import.meta.env.DEV) {
            console.log(`🚀 API 요청: ${config.method?.toUpperCase()} ${config.url}`);
        }

        return config;
    },
    (error) => {
        console.error('❌ 요청 에러:', error);
        return Promise.reject(error);
    }
);

// 응답 인터셉터 - 에러 처리 및 자동 로그아웃
instance.interceptors.response.use(
    (response) => {
        if (import.meta.env.DEV) {
            console.log(`✅ API 응답: ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data);
        }
        return response;
    },
    (error) => {
        const { response } = error;

        if (response) {
            switch (response.status) {
                case 401:
                    console.warn('🔒 인증 만료, 자동 로그아웃');
                    localStorage.removeItem('auth');
                    delete instance.defaults.headers.common['Authorization'];

                    if (window.location.pathname !== '/auth/login') {
                        window.location.href = '/auth/login';
                    }
                    break;

                case 403:
                    console.error('🚫 권한 없음');
                    break;

                case 404:
                    console.error('🔍 요청한 리소스를 찾을 수 없음');
                    break;

                case 422:
                    console.error('📝 입력 데이터 유효성 검사 실패');
                    break;

                case 500:
                    console.error('💥 서버 내부 오류');
                    break;

                default:
                    console.error(`❌ API 에러 (${response.status}):`, response.data);
            }
        } else if (error.request) {
            console.error('🌐 네트워크 에러: 서버에 연결할 수 없습니다');
        } else {
            console.error('⚙️ 요청 설정 에러:', error.message);
        }

        return Promise.reject(error);
    }
);

// 편의 메서드들 추가
instance.apiGet = (url, config = {}) => instance.get(url, config);
instance.apiPost = (url, data, config = {}) => instance.post(url, data, config);
instance.apiPut = (url, data, config = {}) => instance.put(url, data, config);
instance.apiPatch = (url, data, config = {}) => instance.patch(url, data, config);
instance.apiDelete = (url, config = {}) => instance.delete(url, config);

instance.uploadFile = (url, formData, onUploadProgress) => {
    return instance.post(url, formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        onUploadProgress
    });
};

export default instance;
