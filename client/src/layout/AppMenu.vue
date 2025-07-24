<script setup>
import { ref } from 'vue';

import AppMenuItem from './AppMenuItem.vue';

const model = ref([
    {
        label: 'Home',
        items: [{ label: 'Dashboard', icon: 'pi pi-fw pi-home', to: '/' }]
    },
    {   
        label: '대시보드',
        to: '/main',
        items: [

        ]
    },
    {   
        label: '기준정보',
        to: '/standard',
        items: [
            { label: '공급업체 기준정보', icon: 'pi pi-fw pi-pen-to-square', to: '/standard/supplier' },
            { label: '지점 기준정보', icon: 'pi pi-fw pi-pen-to-square', to: '/standard/branch' },
            { label: '제품 기준정보', icon: 'pi pi-fw pi-pen-to-square', to: '/standard/product' },
            { label: '제품 승인 정보', icon: 'pi pi-fw pi-pen-to-square', to: '/standard/productapproval' },
        ]
    },
    /* inventory */ 
    {   
        label: '재고 / 물류',
        to: '/inventory',
        items: [
            // made by hdu
            { label: '재고 현황', icon: 'pi pi-fw pi-book', to: '/inventory/stock/head'},

            // made by kms
            { label: '본사출고관리', icon: 'pi pi-fw pi-book', to: '/hqOutbndMgmt' },
            { label: '본사입고관리', icon: 'pi pi-fw pi-book', to: '/hqInbndMgmt' },
        ]
    },
    /* end of inventory */
    {   
        label: '구매',
        to: '/purchase',
        items: [
            // made by lch
            { label: '본사발주서관리', icon: 'pi pi-fw pi-book', to: '/orders/head'},
            { label: '본사발주서조회', icon: 'pi pi-fw pi-book', to: '/orders/head/view'},
            { label: '지점발주서관리', icon: 'pi pi-fw pi-book', to: '/orders/branch'},
            { label: '지점발주서조회', icon: 'pi pi-fw pi-book', to: '/orders/branch/view'},
            { label: '발주제안관리', icon: 'pi pi-fw pi-book', to: '/orders/supplier'},
            { label: '발주제안조회', icon: 'pi pi-fw pi-book', to: '/orders/supplier/view'},
        ]
    },
    {   
        label: '판매',
        to: '/sales',
        items: [

        ]
    },
    {   
        label: '매출 / 회계',
        to: '/account',
        items: [
            { label: '거래처원장', icon: 'pi pi-fw pi-book', to: '/views/account' },
            { label: '부서정보', icon: 'pi pi-fw pi-tablet', to: '/views/dept' },
            { label: '사원정보', icon: 'pi pi-fw pi-id-card', to: '/views/emp' },
        ]
    },
    {   
        label: '컴포넌트 테스트',
        to: '/test',
        items: [
            { label: '공통 조회 컴포넌트', icon: 'pi pi-fw pi-book', to: '/test' },
            { label: '공통 입력 컴포넌트', icon: 'pi pi-fw pi-file-edit', to: '/test2' },
            { label: '기준정보 컴포넌트', icon: 'pi pi-fw pi-pen-to-square', to: '/test3' },
        ]
    },
    {
        label: '기존 템플릿',
        to: '/old',
        items: [
            {
                label: 'UI Components',
                items: [
                    { label: 'Form Layout', icon: 'pi pi-fw pi-id-card', to: '/uikit/formlayout' },
                    { label: 'Input', icon: 'pi pi-fw pi-check-square', to: '/uikit/input' },
                    { label: 'Button', icon: 'pi pi-fw pi-mobile', to: '/uikit/button', class: 'rotated-icon' },
                    { label: 'Table', icon: 'pi pi-fw pi-table', to: '/uikit/table' },
                    { label: 'List', icon: 'pi pi-fw pi-list', to: '/uikit/list' },
                    { label: 'Tree', icon: 'pi pi-fw pi-share-alt', to: '/uikit/tree' },
                    { label: 'Panel', icon: 'pi pi-fw pi-tablet', to: '/uikit/panel' },
                    { label: 'Overlay', icon: 'pi pi-fw pi-clone', to: '/uikit/overlay' },
                    { label: 'Media', icon: 'pi pi-fw pi-image', to: '/uikit/media' },
                    { label: 'Menu', icon: 'pi pi-fw pi-bars', to: '/uikit/menu' },
                    { label: 'Message', icon: 'pi pi-fw pi-comment', to: '/uikit/message' },
                    { label: 'File', icon: 'pi pi-fw pi-file', to: '/uikit/file' },
                    { label: 'Chart', icon: 'pi pi-fw pi-chart-bar', to: '/uikit/charts' },
                    { label: 'Timeline', icon: 'pi pi-fw pi-calendar', to: '/uikit/timeline' },
                    { label: 'Misc', icon: 'pi pi-fw pi-circle', to: '/uikit/misc' }
                ]
            },
            {
                label: 'Pages',
                icon: 'pi pi-fw pi-briefcase',
                to: '/pages',
                items: [
                    {
                        label: 'Landing',
                        icon: 'pi pi-fw pi-globe',
                        to: '/landing'
                    },
                    {
                        label: 'Auth',
                        icon: 'pi pi-fw pi-user',
                        items: [
                            {
                                label: 'Login',
                                icon: 'pi pi-fw pi-sign-in',
                                to: '/auth/login'
                            },
                            {
                                label: 'Error',
                                icon: 'pi pi-fw pi-times-circle',
                                to: '/auth/error'
                            },
                            {
                                label: 'Access Denied',
                                icon: 'pi pi-fw pi-lock',
                                to: '/auth/access'
                            }
                        ]
                    },
                    {
                        label: 'Crud',
                        icon: 'pi pi-fw pi-pencil',
                        to: '/pages/crud'
                    },
                    {
                        label: 'Not Found',
                        icon: 'pi pi-fw pi-exclamation-circle',
                        to: '/pages/notfound'
                    },
                    {
                        label: 'Empty',
                        icon: 'pi pi-fw pi-circle-off',
                        to: '/pages/empty'
                    }
                ]
            },
            {
                label: 'Hierarchy',
                items: [
                    {
                        label: 'Submenu 1',
                        icon: 'pi pi-fw pi-bookmark',
                        items: [
                            {
                                label: 'Submenu 1.1',
                                icon: 'pi pi-fw pi-bookmark',
                                items: [
                                    { label: 'Submenu 1.1.1', icon: 'pi pi-fw pi-bookmark' },
                                    { label: 'Submenu 1.1.2', icon: 'pi pi-fw pi-bookmark' },
                                    { label: 'Submenu 1.1.3', icon: 'pi pi-fw pi-bookmark' }
                                ]
                            },
                            {
                                label: 'Submenu 1.2',
                                icon: 'pi pi-fw pi-bookmark',
                                items: [{ label: 'Submenu 1.2.1', icon: 'pi pi-fw pi-bookmark' }]
                            }
                        ]
                    },
                    {
                        label: 'Submenu 2',
                        icon: 'pi pi-fw pi-bookmark',
                        items: [
                            {
                                label: 'Submenu 2.1',
                                icon: 'pi pi-fw pi-bookmark',
                                items: [
                                    { label: 'Submenu 2.1.1', icon: 'pi pi-fw pi-bookmark' },
                                    { label: 'Submenu 2.1.2', icon: 'pi pi-fw pi-bookmark' }
                                ]
                            },
                            {
                                label: 'Submenu 2.2',
                                icon: 'pi pi-fw pi-bookmark',
                                items: [{ label: 'Submenu 2.2.1', icon: 'pi pi-fw pi-bookmark' }]
                            }
                        ]
                    }
                ]
            },
            {
                label: 'Get Started',
                items: [
                    {
                        label: 'Documentation',
                        icon: 'pi pi-fw pi-book',
                        to: '/documentation'
                    },
                    {
                        label: 'View Source',
                        icon: 'pi pi-fw pi-github',
                        url: 'https://github.com/primefaces/sakai-vue',
                        target: '_blank'
                    }
                ]
            }
        ]
    }
]);
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
