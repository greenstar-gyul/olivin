<script setup>
import { useLayout } from '@/layout/composables/layout';
import AppConfigurator from './AppConfigurator.vue';
import { useAuth } from '@/composables/useAuth';
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';

const { toggleMenu, toggleDarkMode, isDarkTheme } = useLayout();

// 사용자 인증 정보 가져오기
const { user } = useAuth();
const authStore = useAuthStore();
</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" @click="toggleMenu">
                <i class="pi pi-bars"></i>
            </button>
            <router-link to="/" class="layout-topbar-logo">
                <img src="../../public/demo/images/logo.png" alt="Olivin Logo" class="layout-topbar-logo-image" style="width: 120px; height: auto" />
            </router-link>
        </div>

        <!-- 로그인한 사용자 정보 표시 -->
        <div class="layout-topbar-user">
            <span class="layout-topbar-user-name">{{ `${user ? user.empName : 'Guest'}(${user ? user.employeeId : 'Guest'}, ${user ? user.compName : 'Guest'}) 님` }}</span>
            <!-- 로그인한 사용자일 경우 로그아웃 / 게스트일 경우 로그인 활성화 -->
            <span v-if="user" class="underline cursor-pointer hover:text-lime-600" @click="authStore.logout"> 로그아웃 </span>
        </div>

        <div class="layout-topbar-actions">
            <div class="layout-config-menu">
                <button type="button" class="layout-topbar-action" @click="toggleDarkMode">
                    <i :class="['pi', { 'pi-moon': isDarkTheme, 'pi-sun': !isDarkTheme }]"></i>
                </button>
                <div class="relative" style="display: none">
                    <button
                        v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
                        type="button"
                        class="layout-topbar-action layout-topbar-action-highlight"
                    >
                        <i class="pi pi-palette"></i>
                    </button>
                    <AppConfigurator />
                </div>
            </div>

            <button
                class="layout-topbar-menu-button layout-topbar-action"
                v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
            >
                <i class="pi pi-ellipsis-v"></i>
            </button>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    <button type="button" class="layout-topbar-action">
                        <i class="pi pi-calendar"></i>
                        <span>Calendar</span>
                    </button>
                    <button type="button" class="layout-topbar-action">
                        <i class="pi pi-inbox"></i>
                        <span>Messages</span>
                    </button>
                    <button type="button" class="layout-topbar-action">
                        <i class="pi pi-user"></i>
                        <span>Profile</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>
