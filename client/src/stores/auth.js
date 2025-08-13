// stores/auth.js - DB 기반 권한 시스템 (Persistence 추가)
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import axios from '@/service/axios';

export const useAuthStore = defineStore(
    'auth',
    () => {
        // State
        const user = ref(null);
        const token = ref(null); // localStorage에서 직접 읽지 않고 persistence로 관리
        const userRole = ref(null); // role 정보
        const userPermissions = ref([]); // permissions 배열
        const userMenus = ref([]); // 사용자별 메뉴 정보
        const loading = ref(false);

        // Computed - 기본 정보
        const isAuthenticated = computed(() => !!token.value);
        const roleName = computed(() => userRole.value?.roleName || null);
        const userId = computed(() => user.value?.id || null);

        // 📍 DB 기반 권한 체크 함수들
        const hasPermission = (permissionPath) => {
            if (!userPermissions.value || userPermissions.value.length === 0) {
                return false;
            }
            return userPermissions.value.some((perm) => {
                // PERM_ID가 URL 경로와 일치하는지 확인
                return perm.permId === permissionPath || perm.perm_id === permissionPath;
            });
        };

        const hasAnyPermission = (permissionNames) => {
            return permissionNames.some((permName) => hasPermission(permName));
        };

        const hasAllPermissions = (permissionNames) => {
            return permissionNames.every((permName) => hasPermission(permName));
        };

        const hasRole = (roleNameToCheck) => {
            return roleName.value === roleNameToCheck;
        };

        // Actions
        const login = async (employeeId, password) => {
            loading.value = true;
            try {
                const res = await axios.post('/api/auth/login', { employeeId, password });

                // 기본 사용자 정보 저장
                token.value = res.data.data.token;
                user.value = res.data.data.user;

                // 📍 역할과 권한 정보 저장
                userRole.value = res.data.data.role; // { role_id, role_name }
                userPermissions.value = res.data.data.permissions; // [{ perm_id, permName, perm_description }]
                userMenus.value = res.data.data.menus || []; // 사용자별 메뉴 정보

                // localStorage 수동 저장 제거 (persistence가 자동 처리)

                console.log('✅ 로그인 성공:', {
                    user: res.data.data.user.empName,
                    role: res.data.data.role.roleName,
                    permissions: res.data.data.permissions.map((p) => p.permName)
                });

                return { success: true, data: res.data };
            } catch (err) {
                console.error('❌ 로그인 실패:', err.response?.data || err.message);
                return {
                    success: false,
                    error: err.response?.data?.message || '로그인에 실패했습니다'
                };
            } finally {
                loading.value = false;
            }
        };

        const logout = () => {
            user.value = null;
            token.value = null;
            userRole.value = null;
            userPermissions.value = [];
            userMenus.value = [];

            // localStorage에 저장된 persist 항목 제거
            localStorage.removeItem('auth');

            // axios 헤더에서 Authorization 제거
            delete axios.defaults.headers.common['Authorization'];

            // 로그인 페이지로 강제 이동
            window.location.href = '/auth/login';

            console.log('👋 로그아웃 완료');
        };

        function isJwtExpired(token) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                const now = Math.floor(Date.now() / 1000);
                return payload.exp && payload.exp < now;
            } catch (e) {
                return true;
            }
        }

        // 📍 사용자 정보 및 권한 복구
        const initializeAuth = async () => {
            if (loading.value) return;

            if (token.value) {
                if (isJwtExpired(token.value)) {
                    console.warn('🚨 만료된 토큰 → 로그아웃 처리');
                    logout();
                    return false;
                }

                try {
                    const res = await axios.get('/api/auth/me');
                    user.value = res.data.data.user;
                    userRole.value = res.data.data.role;
                    userPermissions.value = res.data.data.permissions;
                    userMenus.value = res.data.data.menus || [];
                    return true;
                } catch (err) {
                    console.warn('⚠️ 토큰 유효하지 않음, 로그아웃 처리');
                    logout();
                    return false;
                }
            }

            return false;
        };

        // 📍 권한 동적 업데이트 (관리자가 권한 변경 시)
        const refreshPermissions = async () => {
            try {
                const res = await axios.get('/api/auth/permissions');
                userPermissions.value = res.data.data.permissions;
                console.log('🔄 권한 정보 업데이트 완료');
                return true;
            } catch (err) {
                console.error('❌ 권한 정보 업데이트 실패:', err);
                return false;
            }
        };

        // 📍 데이터 필터링 헬퍼 (권한 기반)
        const filterDataByPermission = (data, requiredPermission) => {
            if (!hasPermission(requiredPermission)) {
                return [];
            }

            // 전체 조회 권한이 있으면 모든 데이터
            if (hasPermission(`${requiredPermission}.all`)) {
                return data;
            }

            // 자기 소속만 조회 권한이 있으면 필터링
            if (hasPermission(`${requiredPermission}.own`)) {
                return data.data.filter((item) => {
                    return item.userId === userId.value || item.branchId === user.value?.branchId || item.supplierId === user.value?.supplierId;
                });
            }

            return [];
        };

        // 📍 API 엔드포인트 생성 (권한 기반)
        const getApiEndpoint = (baseEndpoint) => {
            // 역할 기반 엔드포인트 매핑 (기존 호환성)
            const roleEndpointMap = {
                headquarter_admin: `/admin${baseEndpoint}`,
                headquarter_sales: `/sales${baseEndpoint}`,
                branch_admin: `/branch/admin${baseEndpoint}`,
                branch_employee: `/branch${baseEndpoint}`,
                supplier: `/supplier${baseEndpoint}`
            };

            return roleEndpointMap[roleName.value] || baseEndpoint;
        };

        // 📍 메뉴 관련 함수들
        const fetchUserMenus = async () => {
            try {
                const res = await axios.get('/api/auth/menus');
                userMenus.value = res.data.data || [];
                console.log('🔄 메뉴 정보 업데이트 완료');
                return true;
            } catch (err) {
                console.error('❌ 메뉴 정보 조회 실패:', err);
                return false;
            }
        };

        const hasMenuAccess = (menuPath) => {
            if (!userMenus.value.length) return false;
            
            const checkMenuAccess = (menuItems, path) => {
                for (const menu of menuItems) {
                    if (menu.to === path) return true;
                    if (menu.items && checkMenuAccess(menu.items, path)) return true;
                }
                return false;
            };
            
            return checkMenuAccess(userMenus.value, menuPath);
        };

        return {
            // State
            user,
            token,
            userRole,
            userPermissions,
            userMenus,
            loading,

            // Computed
            isAuthenticated,
            roleName,
            userId,

            // 권한 체크 함수들
            hasPermission,
            hasAnyPermission,
            hasAllPermissions,
            hasRole,

            // Actions
            login,
            logout,
            initializeAuth,
            refreshPermissions,
            fetchUserMenus,

            // 헬퍼 함수들
            filterDataByPermission,
            getApiEndpoint,
            hasMenuAccess
        };
    },
    {
        // 🔥 Persistence 설정 추가
        persist: {
            storage: localStorage,
            paths: ['token', 'user', 'userRole', 'userPermissions', 'userMenus'] // 새로고침 시 유지할 상태들
        }
    }
);
