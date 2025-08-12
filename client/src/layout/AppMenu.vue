<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';
import AppMenuItem from './AppMenuItem.vue';

const authStore = useAuthStore();

// 사용자별 메뉴 데이터 (Pinia에서 가져옴)
const model = computed(() => {
    if (authStore.userMenus && authStore.userMenus.length > 0) {
        return authStore.userMenus;
    }
    
    // 메뉴가 없을 경우 빈 배열 반환
    return [];
});

// 메뉴 로딩 함수
const loadMenus = async () => {
    try {
        if (authStore.isAuthenticated) {
            await authStore.fetchUserMenus();
        }
    } catch (error) {
        console.error('메뉴 로딩 실패:', error);
    }
};

// 컴포넌트 마운트 시 메뉴 로딩
onMounted(() => {
    loadMenus();
});

// 인증 상태 변경 시 메뉴 재로딩
watch(() => authStore.isAuthenticated, (isAuth) => {
    if (isAuth) {
        loadMenus();
    }
});
</script>

<template>
    <ul class="layout-menu">
        <!-- 메뉴가 있는 경우 -->
        <template v-if="model.length > 0">
            <template v-for="(item, i) in model" :key="item.label || i">
                <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
                <li v-if="item.separator" class="menu-separator"></li>
            </template>
        </template>
        
        <!-- 메뉴가 없는 경우 -->
        <li v-else-if="authStore.isAuthenticated" class="menu-empty">
            <div class="text-center p-4 text-muted-color">
                <i class="pi pi-info-circle text-2xl mb-2"></i>
                <p class="text-sm">메뉴를 불러오는 중입니다...</p>
            </div>
        </li>
        
        <!-- 로그인하지 않은 경우 -->
        <li v-else class="menu-empty">
            <div class="text-center p-4 text-muted-color">
                <i class="pi pi-sign-in text-2xl mb-2"></i>
                <p class="text-sm">로그인이 필요합니다</p>
            </div>
        </li>
    </ul>
</template>

<style lang="scss" scoped>
.menu-empty {
    list-style: none;
    margin: 0;
    padding: 0;
    
    .text-center {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        min-height: 100px;
    }
    
    i {
        display: block;
        margin-bottom: 0.5rem;
    }
    
    p {
        margin: 0;
        opacity: 0.7;
    }
}
</style>
