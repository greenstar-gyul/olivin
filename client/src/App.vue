<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

onMounted(async () => {
  console.log('🚀 앱 초기화 시작')
  
  if (authStore.token) {
    console.log('📱 저장된 토큰 발견, 사용자 정보 복구 중...')
    
    try {
      const success = await authStore.initializeAuth()
      
      if (success) {
        console.log('✅ 인증 상태 복구 성공')
      } else {
        console.warn('❌ 토큰이 유효하지 않음, 로그인 페이지로 이동')
        router.push('/auth/login')
      }
    } catch (error) {
      console.error('❌ 인증 초기화 중 오류:', error)
      router.push('/auth/login')
    }
  } else {
    console.log('📱 저장된 토큰 없음')
  }
})
</script>

<template>
    <router-view />
</template>

<style scoped>
</style>