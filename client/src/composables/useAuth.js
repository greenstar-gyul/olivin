// composables/useAuth.js - DB 기반 권한 시스템용
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

export function useAuth() {
  const authStore = useAuthStore()

  // 기본 정보
  const user = computed(() => authStore.user)
  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const userRole = computed(() => authStore.roleName) // role_name
  const loading = computed(() => authStore.loading)
  const permissions = computed(() => authStore.userPermissions)

  // 📍 권한 체크 함수들 (DB 기반)
  const hasPermission = (permissionName) => authStore.hasPermission(permissionName)
  const hasAnyPermission = (permissionNames) => authStore.hasAnyPermission(permissionNames)
  const hasAllPermissions = (permissionNames) => authStore.hasAllPermissions(permissionNames)
  const hasRole = (roleName) => authStore.hasRole(roleName)

  // 📍 기능별 권한 체크 (자주 사용하는 것들)
  const canManageInventory = computed(() => hasPermission('inventory.manage') || hasAnyPermission(['inventory.create', 'inventory.update']))
  const canViewInventory = computed(() => hasPermission('inventory.view'))
  const canManageOrders = computed(() => hasPermission('orders.manage') || hasAnyPermission(['orders.create', 'orders.update']))
  const canViewOrders = computed(() => hasPermission('orders.view'))
  const canManageUsers = computed(() => hasPermission('users.manage'))
  const canViewReports = computed(() => hasPermission('reports.view'))
  const canViewAllBranches = computed(() => hasPermission('branches.all.view'))

  // 📍 역할별 간편 체크 (기존 호환성 유지)
  const isHeadquarterAdmin = computed(() => hasRole('headquarter_admin'))
  const isHeadquarterSales = computed(() => hasRole('headquarter_sales'))
  const isBranchAdmin = computed(() => hasRole('branch_admin'))
  const isBranchEmployee = computed(() => hasRole('branch_employee'))
  const isSupplier = computed(() => hasRole('supplier'))
  const isHeadquarter = computed(() => hasAnyPermission(['branches.all.view']))
  const isBranch = computed(() => hasRole('branch_admin') || hasRole('branch_employee'))

  // 📍 UI 표시용 권한 그룹
  const uiPermissions = computed(() => ({
    // 메뉴 표시 권한
    showInventoryMenu: hasPermission('inventory.view'),
    showOrdersMenu: hasPermission('orders.view'),
    showUsersMenu: hasPermission('users.view'),
    showReportsMenu: hasPermission('reports.view'),
    showSupplierMenu: hasPermission('suppliers.view'),
    
    // 버튼 표시 권한
    showCreateButton: hasAnyPermission(['inventory.create', 'orders.create', 'users.create']),
    showEditButton: hasAnyPermission(['inventory.update', 'orders.update', 'users.update']),
    showDeleteButton: hasAnyPermission(['inventory.delete', 'orders.delete', 'users.delete']),
    
    // 데이터 표시 권한
    showAllBranchData: hasPermission('branches.all.view'),
    showOwnBranchData: hasPermission('branches.own.view'),
    showSupplierData: hasPermission('suppliers.view')
  }))

  // 📍 데이터 필터링 (권한 기반)
  const filterByPermission = (data, requiredPermission) => {
    return authStore.filterDataByPermission(data, requiredPermission)
  }

  // 📍 API 엔드포인트 생성
  const getApiEndpoint = (baseEndpoint) => {
    return authStore.getApiEndpoint(baseEndpoint)
  }

  // 📍 페이지별 권한 체크
  const canAccessPage = (requiredPermissions) => {
    if (!requiredPermissions || requiredPermissions.length === 0) {
      return true // 권한 요구사항 없으면 접근 허용
    }
    
    // 배열로 전달된 권한 중 하나라도 있으면 접근 허용
    if (Array.isArray(requiredPermissions)) {
      return hasAnyPermission(requiredPermissions)
    }
    
    // 단일 권한 체크
    return hasPermission(requiredPermissions)
  }

  // 📍 동적 메뉴 생성 (권한 기반)
  const getAvailableMenus = () => {
    const menus = []
    
    if (hasPermission('inventory.view')) {
      menus.push({
        name: 'inventory',
        label: '재고 관리',
        icon: '📦',
        path: getInventoryPath(),
        permissions: ['inventory.view']
      })
    }
    
    if (hasPermission('orders.view')) {
      menus.push({
        name: 'orders',
        label: '주문 관리', 
        icon: '📋',
        path: getOrdersPath(),
        permissions: ['orders.view']
      })
    }
    
    if (hasPermission('users.view')) {
      menus.push({
        name: 'users',
        label: '사용자 관리',
        icon: '👥',
        path: '/users',
        permissions: ['users.view']
      })
    }
    
    if (hasPermission('reports.view')) {
      menus.push({
        name: 'reports',
        label: '보고서',
        icon: '📊', 
        path: getReportsPath(),
        permissions: ['reports.view']
      })
    }
    
    return menus
  }

  // 📍 역할별 기본 경로 생성 (기존 호환성)
  const getInventoryPath = () => {
    if (hasRole('headquarter_admin')) return '/admin/inventory'
    if (hasRole('headquarter_sales')) return '/sales/inventory'
    if (hasRole('branch_admin')) return '/branch/admin/inventory'
    if (hasRole('branch_employee')) return '/branch/inventory'
    if (hasRole('supplier')) return '/supplier/inventory'
    return '/inventory'
  }

  const getOrdersPath = () => {
    if (hasRole('headquarter_admin')) return '/admin/orders'
    if (hasRole('headquarter_sales')) return '/sales/orders'
    if (hasRole('branch_admin')) return '/branch/admin/orders'
    if (hasRole('branch_employee')) return '/branch/orders'
    if (hasRole('supplier')) return '/supplier/orders'
    return '/orders'
  }

  const getReportsPath = () => {
    if (hasRole('headquarter_admin')) return '/admin/reports'
    if (hasRole('headquarter_sales')) return '/sales/reports'
    if (hasRole('branch_admin')) return '/branch/reports'
    return '/reports'
  }

  return {
    // 기본 정보
    user,
    isAuthenticated,
    userRole,
    loading,
    permissions,
    
    // 권한 체크 함수들
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    
    // 기능별 권한
    canManageInventory,
    canViewInventory,
    canManageOrders,
    canViewOrders,
    canManageUsers,
    canViewReports,
    canViewAllBranches,
    
    // 역할별 체크 (호환성)
    isHeadquarterAdmin,
    isHeadquarterSales,
    isBranchAdmin,
    isBranchEmployee,
    isSupplier,
    isHeadquarter,
    isBranch,
    
    // UI 권한
    ...uiPermissions.value,
    
    // 헬퍼 함수들
    filterByPermission,
    getApiEndpoint,
    canAccessPage,
    getAvailableMenus,
    getInventoryPath,
    getOrdersPath,
    getReportsPath
  }
}