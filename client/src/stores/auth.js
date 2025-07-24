// stores/auth.js - DB 기반 권한 시스템
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/service/axios'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || null)
  const userRole = ref(null) // role 정보
  const userPermissions = ref([]) // permissions 배열
  const loading = ref(false)

  // Computed - 기본 정보
  const isAuthenticated = computed(() => !!token.value)
  const roleName = computed(() => userRole.value?.role_name || null)
  const userId = computed(() => user.value?.id || null)

  // 📍 DB 기반 권한 체크 함수들
  const hasPermission = (permissionName) => {
    return userPermissions.value.some(perm => perm.perm_name === permissionName)
  }

  const hasAnyPermission = (permissionNames) => {
    return permissionNames.some(permName => hasPermission(permName))
  }

  const hasAllPermissions = (permissionNames) => {
    return permissionNames.every(permName => hasPermission(permName))
  }

  const hasRole = (roleNameToCheck) => {
    return roleName.value === roleNameToCheck
  }

  // 📍 기능별 권한 체크 (권한명 기반)
  const permissions = computed(() => ({
    // 재고 관리 권한들
    canViewInventory: hasPermission('inventory.view'),
    canCreateInventory: hasPermission('inventory.create'),
    canUpdateInventory: hasPermission('inventory.update'),
    canDeleteInventory: hasPermission('inventory.delete'),
    canManageInventory: hasAnyPermission(['inventory.create', 'inventory.update', 'inventory.delete']),
    
    // 주문 관리 권한들
    canViewOrders: hasPermission('orders.view'),
    canCreateOrders: hasPermission('orders.create'),
    canUpdateOrders: hasPermission('orders.update'),
    canDeleteOrders: hasPermission('orders.delete'),
    canManageOrders: hasAnyPermission(['orders.create', 'orders.update', 'orders.delete']),
    
    // 사용자 관리 권한들
    canViewUsers: hasPermission('users.view'),
    canCreateUsers: hasPermission('users.create'),
    canUpdateUsers: hasPermission('users.update'),
    canDeleteUsers: hasPermission('users.delete'),
    canManageUsers: hasAnyPermission(['users.create', 'users.update', 'users.delete']),
    
    // 보고서 권한들
    canViewReports: hasPermission('reports.view'),
    canViewBranchReports: hasPermission('reports.branch.view'),
    canViewAllReports: hasPermission('reports.all.view'),
    
    // 시스템 관리 권한들
    canManageSystem: hasPermission('system.manage'),
    canViewSystemLogs: hasPermission('system.logs.view'),
    
    // 지점별 접근 권한
    canViewAllBranches: hasPermission('branches.all.view'),
    canViewOwnBranch: hasPermission('branches.own.view'),
    
    // 공급업체 관리
    canManageSuppliers: hasPermission('suppliers.manage'),
    canViewSuppliers: hasPermission('suppliers.view')
  }))

  // 📍 역할별 간편 체크 (호환성 유지)
  const roleChecks = computed(() => ({
    isHeadquarterAdmin: hasRole('headquarter_admin'),
    isHeadquarterSales: hasRole('headquarter_sales'),
    isBranchAdmin: hasRole('branch_admin'),
    isBranchEmployee: hasRole('branch_employee'),
    isSupplier: hasRole('supplier'),
    isHeadquarter: hasAnyPermission(['branches.all.view']), // 본사 직원들
    isBranch: hasRole('branch_admin') || hasRole('branch_employee')
  }))

  // Actions
  const login = async (email, password) => {
    loading.value = true
    try {
      const res = await axios.post('/auth/login', { email, password })
      
      // 기본 사용자 정보 저장
      token.value = res.data.token
      user.value = res.data.user
      
      // 📍 역할과 권한 정보 저장
      userRole.value = res.data.role // { role_id, role_name }
      userPermissions.value = res.data.permissions // [{ perm_id, perm_name, perm_description }]
      
      localStorage.setItem('token', res.data.token)
      
      console.log('✅ 로그인 성공:', {
        user: res.data.user.name,
        role: res.data.role.role_name,
        permissions: res.data.permissions.map(p => p.perm_name)
      })
      
      return { success: true, data: res.data }
      
    } catch (err) {
      console.error('❌ 로그인 실패:', err.response?.data || err.message)
      return { 
        success: false, 
        error: err.response?.data?.message || '로그인에 실패했습니다'
      }
    } finally {
      loading.value = false
    }
  }

  const logout = () => {
    user.value = null
    token.value = null
    userRole.value = null
    userPermissions.value = []
    localStorage.removeItem('token')
    console.log('👋 로그아웃 완료')
  }

  // 📍 사용자 정보 및 권한 복구
  const initializeAuth = async () => {
    if (token.value) {
      try {
        const res = await axios.get('/auth/me')
        
        user.value = res.data.user
        userRole.value = res.data.role
        userPermissions.value = res.data.permissions
        
        console.log('🔄 인증 상태 복구 완료:', {
          user: res.data.user.name,
          role: res.data.role.role_name,
          permissions: res.data.permissions.map(p => p.perm_name)
        })
        
        return true
      } catch (err) {
        console.warn('⚠️ 토큰이 유효하지 않음, 로그아웃 처리')
        logout()
        return false
      }
    }
    return false
  }

  // 📍 권한 동적 업데이트 (관리자가 권한 변경 시)
  const refreshPermissions = async () => {
    try {
      const res = await axios.get('/auth/permissions')
      userPermissions.value = res.data.permissions
      console.log('🔄 권한 정보 업데이트 완료')
      return true
    } catch (err) {
      console.error('❌ 권한 정보 업데이트 실패:', err)
      return false
    }
  }

  // 📍 데이터 필터링 헬퍼 (권한 기반)
  const filterDataByPermission = (data, requiredPermission) => {
    if (!hasPermission(requiredPermission)) {
      return []
    }
    
    // 전체 조회 권한이 있으면 모든 데이터
    if (hasPermission(`${requiredPermission}.all`)) {
      return data
    }
    
    // 자기 소속만 조회 권한이 있으면 필터링
    if (hasPermission(`${requiredPermission}.own`)) {
      return data.filter(item => {
        return item.userId === userId.value || 
               item.branchId === user.value?.branchId ||
               item.supplierId === user.value?.supplierId
      })
    }
    
    return []
  }

  // 📍 API 엔드포인트 생성 (권한 기반)
  const getApiEndpoint = (baseEndpoint) => {
    // 역할 기반 엔드포인트 매핑 (기존 호환성)
    const roleEndpointMap = {
      'headquarter_admin': `/admin${baseEndpoint}`,
      'headquarter_sales': `/sales${baseEndpoint}`,
      'branch_admin': `/branch/admin${baseEndpoint}`,
      'branch_employee': `/branch${baseEndpoint}`,
      'supplier': `/supplier${baseEndpoint}`
    }
    
    return roleEndpointMap[roleName.value] || baseEndpoint
  }

  return {
    // State
    user,
    token,
    userRole,
    userPermissions,
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
    
    // 기능별 권한들
    ...permissions.value,
    
    // 역할별 체크 (호환성)
    ...roleChecks.value,
    
    // Actions
    login,
    logout,
    initializeAuth,
    refreshPermissions,
    
    // 헬퍼 함수들
    filterDataByPermission,
    getApiEndpoint
  }
})