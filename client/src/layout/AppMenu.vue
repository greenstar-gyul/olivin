<script setup>
import { ref, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';

import AppMenuItem from './AppMenuItem.vue';

const authStore = useAuthStore();

// PERMISSIONS 테이블 데이터를 기반으로 메뉴 트리 구조 생성
const buildMenuFromPermissions = (permissions) => {
    if (!permissions || permissions.length === 0) {
        return [];
    }
    
    const menuMap = new Map();
    const rootMenus = [];
    
    // 1. 모든 권한을 메뉴 아이템으로 변환
    permissions.forEach((perm) => {
        const menuItem = {
            label: perm.permName,
            icon: perm.icon || '',
            to: perm.permId,
            parentTo: perm.parentTo || null,
            items: []
        };
        menuMap.set(perm.permId, menuItem);
    });
    
    // 2. 부모-자식 관계 설정
    menuMap.forEach((menu, permId) => {
        if (!menu.parentTo) {
            // 최상위 메뉴
            rootMenus.push(menu);
        } else {
            // 하위 메뉴
            const parent = menuMap.get(menu.parentTo);
            if (parent) {
                parent.items.push(menu);
            }
        }
    });
    
    // 3. parentTo 속성 제거 및 빈 items 정리
    const cleanMenu = (menuItems) => {
        return menuItems.map(item => {
            const cleanItem = { ...item };
            delete cleanItem.parentTo;
            
            if (cleanItem.items && cleanItem.items.length > 0) {
                cleanItem.items = cleanMenu(cleanItem.items);
            } else {
                delete cleanItem.items;
            }
            
            return cleanItem;
        });
    };
    
    return cleanMenu(rootMenus);
};

const model = computed(() => {
    if (!authStore.isAuthenticated) {
        return [];
    }
    
    // userPermissions에서 메뉴 구조 생성
    return buildMenuFromPermissions(authStore.userPermissions);
});
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item">
            <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
            <li v-if="item.separator" class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>

