import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { createPinia } from 'pinia';

import { useAuthStore } from '@/stores/auth';
import Aura from '@primeuix/themes/aura';
import PrimeVue from 'primevue/config';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';


import '@/assets/styles.scss';

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
app.use(PrimeVue, {
    theme: {
        preset: Aura,
        options: {
            darkModeSelector: '.app-dark',
            prefix: 'p',
            cssLayer: false
        }
    }
});

app.use(ConfirmationService);
app.use(ToastService);

app.use(router);

// 앱 시작 전 인증 상태 복구
const initializeApp = async () => {
    try {
        const authStore = useAuthStore()
        
        // 토큰이 있으면 사용자 정보 복구 시도
        if (authStore.token) {
            console.log('🔄 저장된 토큰 발견, 인증 상태 복구 중...')
            await authStore.initializeAuth()
        }
        
        console.log('🚀 앱 초기화 완료')
    } catch (error) {
        console.error('❌ 앱 초기화 중 오류:', error)
        // 에러가 있어도 앱은 시작
    } finally {
        // 앱 마운트
        app.mount('#app')
    }
}

// 앱 초기화 실행
initializeApp()
