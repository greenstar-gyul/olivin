import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: AppLayout,
            children: [
                {
                    path: '/dashboard/hq',
                    name: 'HqDashboard',
                    component: () => import('@/views/dashboard/HqDashboard.vue'),
                    meta: { permissions: ['/dashboard/hq'] }
                },
                {
                    path: '/dashboard/branch',
                    name: 'BranchDashboard',
                    component: () => import('@/views/dashboard/BranchDashboard.vue'),
                    meta: { permissions: ['/dashboard/branch'] }
                },
                {
                    path: '/dashboard/supplier',
                    name: 'SupplierDashboard',
                    component: () => import('@/views/dashboard/SupplierDashboard.vue'),
                    meta: { permissions: ['/dashboard/supplier'] }
                },
                {
                    path: '/',
                    name: 'dashboard',
                    component: () => import('@/views/Dashboard.vue')
                },
                {
                    path: '/views/account',
                    name: '거래처원장',
                    component: () => import('@/views/account/AccountLeder.vue'),
                    meta: { permissions: ['/views/account'] }
                },
                {
                    path: '/uikit/formlayout',
                    name: 'formlayout',
                    component: () => import('@/views/uikit/FormLayout.vue'),
                    meta: { permissions: ['/uikit/formlayout'] }
                },
                {
                    path: '/uikit/input',
                    name: 'input',
                    component: () => import('@/views/uikit/InputDoc.vue'),
                    meta: { permissions: ['/uikit/input'] }
                },
                {
                    path: '/uikit/button',
                    name: 'button',
                    component: () => import('@/views/uikit/ButtonDoc.vue'),
                    meta: { permissions: ['/uikit/button'] }
                },
                {
                    path: '/uikit/table',
                    name: 'table',
                    component: () => import('@/views/uikit/TableDoc.vue'),
                    meta: { permissions: ['/uikit/table'] }
                },
                {
                    path: '/uikit/list',
                    name: 'list',
                    component: () => import('@/views/uikit/ListDoc.vue'),
                    meta: { permissions: ['/uikit/list'] }
                },
                {
                    path: '/uikit/tree',
                    name: 'tree',
                    component: () => import('@/views/uikit/TreeDoc.vue'),
                    meta: { permissions: ['/uikit/tree'] }
                },
                {
                    path: '/uikit/panel',
                    name: 'panel',
                    component: () => import('@/views/uikit/PanelsDoc.vue'),
                    meta: { permissions: ['/uikit/panel'] }
                },
                {
                    path: '/uikit/overlay',
                    name: 'overlay',
                    component: () => import('@/views/uikit/OverlayDoc.vue'),
                    meta: { permissions: ['/uikit/overlay'] }
                },
                {
                    path: '/uikit/media',
                    name: 'media',
                    component: () => import('@/views/uikit/MediaDoc.vue'),
                    meta: { permissions: ['/uikit/media'] }
                },
                {
                    path: '/uikit/message',
                    name: 'message',
                    component: () => import('@/views/uikit/MessagesDoc.vue'),
                    meta: { permissions: ['/uikit/message'] }
                },
                {
                    path: '/uikit/file',
                    name: 'file',
                    component: () => import('@/views/uikit/FileDoc.vue'),
                    meta: { permissions: ['/uikit/file'] }
                },
                {
                    path: '/uikit/menu',
                    name: 'menu',
                    component: () => import('@/views/uikit/MenuDoc.vue'),
                    meta: { permissions: ['/uikit/menu'] }
                },
                {
                    path: '/uikit/charts',
                    name: 'charts',
                    component: () => import('@/views/uikit/ChartDoc.vue'),
                    meta: { permissions: ['/uikit/charts'] }
                },
                {
                    path: '/uikit/misc',
                    name: 'misc',
                    component: () => import('@/views/uikit/MiscDoc.vue'),
                    meta: { permissions: ['/uikit/misc'] }
                },
                {
                    path: '/uikit/timeline',
                    name: 'timeline',
                    component: () => import('@/views/uikit/TimelineDoc.vue'),
                    meta: { permissions: ['/uikit/timeline'] }
                },
                {
                    path: '/pages/empty',
                    name: 'empty',
                    component: () => import('@/views/pages/Empty.vue'),
                    meta: { permissions: ['/pages/empty'] }
                },
                {
                    path: '/pages/crud',
                    name: 'crud',
                    component: () => import('@/views/pages/Crud.vue')
                    // 권한 없음 = 모든 로그인 사용자 접근 가능
                },
                {
                    path: '/documentation',
                    name: 'documentation',
                    component: () => import('@/views/pages/Documentation.vue')
                    // 권한 없음 = 모든 로그인 사용자 접근 가능
                },
                {
                    path: '/test',
                    name: 'test',
                    component: () => import('@/views/test/TestPage.vue'),
                    meta: { permissions: ['/test'] }
                },
                {
                    path: '/test2',
                    name: 'test2',
                    component: () => import('@/views/test/InputTestPage.vue'),
                    meta: { permissions: ['/test2'] }
                },
                {
                    path: '/test3',
                    name: 'test3',
                    component: () => import('@/views/test/StandardInputTestPage.vue'),
                    meta: { permissions: ['/test3'] }
                },
                {
                    path: '/test4',
                    name: 'test4',
                    component: () => import('@/views/test/ViewTestPage.vue')
                },
                {
                    path: '/test5',
                    name: 'test5',
                    component: () => import('@/views/test/InputMultiTablePage.vue')
                },
                /* inventory made by kms*/
                {
                    path: '/hqOutbndMgmt',
                    name: 'hqOutbndMgmt',
                    component: () => import('@/views/inventory/hqOutbndMgmt.vue'),
                    meta: { permissions: ['/hqOutbndMgmt'] }
                },
                {
                    path: '/hqInbndMgmt',
                    name: 'hqInbndMgmt',
                    component: () => import('@/views/inventory/hqInbndMgmt.vue'),
                    meta: { permissions: ['/hqInbndMgmt'] }
                },
                {
                    path: '/brInbndMgmt',
                    name: 'brInbndMgmt',
                    component: () => import('@/views/inventory/brInbndMgmt.vue'),
                    meta: { permissions: ['/brInbndMgmt'] }
                },
                {
                    path: '/supOutbndMgmt',
                    name: 'supOutbndMgmt',
                    component: () => import('@/views/inventory/supOutbndMgmt.vue'),
                    meta: { permissions: ['/supOutbndMgmt'] }
                },
                /* end of inventory */
                {
                    path: '/standard/supplier',
                    name: 'supplier',
                    component: () => import('@/views/standard/SupplierStandardPage.vue'),
                    meta: { permissions: ['/standard/supplier'] }
                },
                {
                    path: '/standard/branch',
                    name: 'branch',
                    component: () => import('@/views/standard/BranchStandardPage.vue'),
                    meta: { permissions: ['/standard/branch'] }
                },
                {
                    path: '/standard/product',
                    name: 'product',
                    component: () => import('@/views/standard/ProductStandardPage.vue'),
                    meta: { permissions: ['/standard/product'] }
                },
                {
                    path: '/standard/productapproval',
                    name: 'productApproval',
                    component: () => import('@/views/standard/ProductApprovalPage.vue'),
                    meta: { roles: ['ROLE_SYSTEM_ADMIN'] }
                },
                 {
                    path: '/views/dept',
                    name: '부서기준정보',
                    component: () => import('@/views/standard/DeptStandardPage.vue')
                },
                {
                    path: '/views/emp',
                    name: '사원기준정보',
                    component: () => import('@/views/standard/EmpStandardPage.vue')
                },
                /* order */
                {
                    path: '/orders/head',
                    name: 'HeadOrdersManage',
                    component: () => import('@/views/orderMgmt/HeadOrdersManage.vue'),
                    meta: { permissions: ['/orders/head'] }
                },
                {
                    path: '/orders/head/view',
                    name: 'HeadOrdersView',
                    component: () => import('@/views/orderMgmt/HeadOrdersView.vue'),
                    meta: { permissions: ['/orders/head/view'] }
                },
                {
                    path: '/orders/branch',
                    name: 'BranchOrdersManage',
                    component: () => import('@/views/orderMgmt/BranchOrdersManage.vue'),
                    meta: { permissions: ['/orders/branch'] }
                },
                {
                    path: '/orders/branch/view',
                    name: 'BranchOrdersView',
                    component: () => import('@/views/orderMgmt/BranchOrdersView.vue'),
                    meta: { permissions: ['/orders/branch/view'] }
                },
                {
                    path: '/orders/supplier',
                    name: 'SupplierOrdersManage',
                    component: () => import('@/views/orderMgmt/SupplierOrdersManage.vue'),
                    meta: { permissions: ['/orders/supplier'] }
                },
                {
                    path: '/orders/supplier/view',
                    name: 'SupplierOrdersView',
                    component: () => import('@/views/orderMgmt/SupplierOrdersView.vue'),
                    meta: { permissions: ['/orders/supplier/view'] }
                },
                {
                    path: '/orders/view/:orderId',
                    name: 'OrdersDetailView',
                    component: () => import('@/views/orderMgmt/OrdersDetailView.vue')
                },
                /* end of order */
                {
                    path: '/inventory/stock/head',
                    name: 'headStock',
                    component: () => import('@/views/inventory/HeadStockPage.vue'),
                    meta: { permissions: ['/inventory/stock/head'] }
                },
                {
                    path: '/inventory/stock/branch',
                    name: 'branchStock',
                    component: () => import('@/views/inventory/BranchStockPage.vue'),
                    meta: { permissions: ['/inventory/stock/branch'] }
                },
                /* sales */
                {
                    path: '/sales/plan',
                    name: 'salesPlan',
                    component: () => import('@/views/sales/SalesPlanPage.vue'),
                    meta: { permissions: ['/sales/plan'] }
                },
                {
                    path: '/sales/orders',
                    name: 'salesOrders',
                    component: () => import('@/views/sales/SalesOrdersMgmt.vue'),
                    meta: { permissions: ['/sales/orders'] }
                },
                {
                    path: '/sales/orders/view',
                    name: 'salesOrdersView',
                    component: () => import('@/views/sales/SalesOrdersPage.vue'),
                    meta: { permissions: ['/sales/orders/view'] }
                },
                {
                    path: '/sales/history',
                    name: 'salesHistory',
                    component: () => import('@/views/sales/SalesHistoryPage.vue'),
                    meta: { permissions: ['/sales/history'] }
                },
                {
                    path: '/sales/performance',
                    name: 'salesPerformance',
                    component: () => import('@/views/sales/SalesPerformancePage.vue'),
                    meta: { permissions: ['/sales/performance'] }
                },
                /* end of sales */
                // router/index.js 수정
                {
                    path: '/roles/empmanage',
                    name: 'empManage',
                    component: () => import('@/views/roles/EmpRoleManage.vue'), // 파일명 확인
                    meta: { permissions: ['/roles/empmanage'] }
                },
                {
                    path: '/roles/rolemanage',
                    name: 'roleManage',
                    component: () => import('@/views/roles/RoleManage.vue'),
                    meta: { permissions: ['/roles/rolemanage'] }
                }
            ]
        },
        // 로그인 불필요 페이지들
        {
            path: '/landing',
            name: 'landing',
            component: () => import('@/views/pages/Landing.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/pages/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/auth/login',
            name: 'login',
            component: () => import('@/views/pages/auth/Login.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/auth/access',
            name: 'accessDenied',
            component: () => import('@/views/pages/auth/Access.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/auth/error',
            name: 'error',
            component: () => import('@/views/pages/auth/Error.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/pages/notfound'
        }
    ]
});

// 📍 DB 기반 권한 체크 라우터 가드
router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();

    // 자동 페이지 타이틀
    const pageTitle = to.meta?.title || getDefaultTitle(to.name) || '페이지';
    document.title = `${pageTitle} - SCM 시스템`;

    // AppLayout 하위는 로그인 필요
    const requiresAuth = to.meta?.requiresAuth !== false && to.matched.some((record) => record.components?.default === AppLayout);

    if (requiresAuth) {
        // 로그인 체크
        if (!authStore.isAuthenticated) {
            console.warn('🔒 로그인이 필요합니다.');
            next('/auth/login');
            return;
        }

        // ✅ 사용자 정보 복구는 App.vue에서 처리하므로 여기서는 제거
        // 권한 체크는 사용자 정보가 있을 때만 수행
        if (authStore.user) {
            // 📍 권한 기반 접근 제어
            if (to.meta?.permissions && to.meta.permissions.length > 0) {
                const hasRequiredPermission = authStore.hasAnyPermission(to.meta.permissions);

                if (!hasRequiredPermission) {
                    console.warn(`🚫 접근 권한이 없습니다. 필요 권한: [${to.meta.permissions.join(', ')}]`);
                    next('/auth/access');
                    return;
                }
            }

            // 📍 역할 기반 접근 제어 (하위 호환성)
            if (to.meta?.roles && to.meta.roles.length > 0) {
                const hasRequiredRole = to.meta.roles.includes(authStore.roleName);

                if (!hasRequiredRole) {
                    console.warn(`🚫 접근 권한이 없습니다. 필요 역할: [${to.meta.roles.join(', ')}]`);
                    next('/auth/access');
                    return;
                }
            }

            console.log(`✅ 페이지 접근 허용: ${to.path} (역할: ${authStore.roleName})`);
        }
    }

    // 로그인된 사용자가 로그인 페이지 접근 시
    if (to.name === 'login' && authStore.isAuthenticated) {
        console.log('👤 이미 로그인되어 있습니다.');
        next('/');
        return;
    }

    next();
});

// 기본 타이틀 생성 함수
function getDefaultTitle(routeName) {
    const titleMap = {
        dashboard: '대시보드',
        HqDashboard: '본사 대시보드',
        BranchDashboard: '지점 대시보드',
        SupplierDashboard: '공급업체 대시보드',
        formlayout: 'Form Layout',
        input: 'Input',
        button: 'Button',
        table: 'Table',
        list: 'List',
        tree: 'Tree',
        panel: 'Panel',
        overlay: 'Overlay',
        media: 'Media',
        message: 'Message',
        file: 'File',
        menu: 'Menu',
        charts: 'Charts',
        misc: 'Misc',
        timeline: 'Timeline',
        empty: 'Empty Page',
        crud: 'CRUD',
        documentation: 'Documentation',
        test: 'Test Page',
        test2: 'Input Test Page',
        salesPerformance: '매출 실적 조회',
        landing: '랜딩 페이지',
        notfound: '페이지를 찾을 수 없음',
        login: '로그인',
        accessDenied: '접근 거부',
        error: '오류'
    };

    return titleMap[routeName];
}

export default router;
