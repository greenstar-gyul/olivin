package com.olivin.app.auth.service;

import com.olivin.app.auth.mapper.MenuMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;

    /**
     * 사용자별 메뉴 목록 조회
     */
    public List<Map<String, Object>> getUserMenus(String userId) {
        log.info("사용자 메뉴 조회 시작 - userId: {}", userId);
        
        try {
            // 사용자의 권한(메뉴) 조회
            List<Map<String, Object>> userPermissions = menuMapper.getUserPermissions(userId);
            log.info("사용자 권한 개수: {}", userPermissions.size());
            
            if (userPermissions.isEmpty()) {
                log.warn("사용자 권한이 없습니다 - userId: {}", userId);
                // 기본 메뉴 반환
                String userRole = menuMapper.getUserRole(userId);
                return getDefaultMenusByRole(userRole);
            }
            
            // 메뉴 트리 구조 생성
            List<Map<String, Object>> menuTree = buildMenuTreeFromPermissions(userPermissions);
            log.info("메뉴 트리 생성 완료 - 최상위 메뉴 개수: {}", menuTree.size());
            
            return menuTree;
        } catch (Exception e) {
            log.error("사용자 메뉴 조회 중 오류 발생 - userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 권한 데이터를 메뉴 트리 구조로 변환
     */
    private List<Map<String, Object>> buildMenuTreeFromPermissions(List<Map<String, Object>> permissions) {
        Map<String, Map<String, Object>> menuMap = new HashMap<>();
        List<Map<String, Object>> rootMenus = new ArrayList<>();
        
        // 1. 모든 권한을 메뉴 아이템으로 변환
        for (Map<String, Object> permission : permissions) {
            String menuPath = (String) permission.get("MENU_PATH");
            String parentPath = (String) permission.get("PARENT_MENU_PATH");
            
            Map<String, Object> menuItem = new HashMap<>();
            menuItem.put("label", permission.get("MENU_NAME"));
            menuItem.put("icon", permission.get("MENU_ICON"));
            menuItem.put("to", menuPath);
            menuItem.put("parentPath", parentPath);
            menuItem.put("items", new ArrayList<Map<String, Object>>());
            
            menuMap.put(menuPath, menuItem);
        }
        
        // 2. 부모-자식 관계 설정
        for (Map<String, Object> menu : menuMap.values()) {
            String parentPath = (String) menu.get("parentPath");
            
            if (parentPath == null || parentPath.isEmpty()) {
                // 최상위 메뉴
                rootMenus.add(menu);
            } else {
                // 하위 메뉴
                Map<String, Object> parent = menuMap.get(parentPath);
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parentItems = (List<Map<String, Object>>) parent.get("items");
                    parentItems.add(menu);
                }
            }
        }
        
        // 3. 불필요한 필드 제거 및 빈 items 제거
        cleanMenuItems(rootMenus);
        
        return rootMenus;
    }

    /**
     * 플랫 메뉴 리스트를 트리 구조로 변환 (기존 호환성)
     */
    private List<Map<String, Object>> buildMenuTree(List<Map<String, Object>> flatMenus) {
        Map<String, Map<String, Object>> menuMap = new HashMap<>();
        List<Map<String, Object>> rootMenus = new ArrayList<>();
        
        // 1. 모든 메뉴를 Map에 저장
        for (Map<String, Object> menu : flatMenus) {
            String menuId = (String) menu.get("MENU_ID");
            Map<String, Object> menuItem = new HashMap<>();
            menuItem.put("id", menuId);
            menuItem.put("label", menu.get("MENU_NAME"));
            menuItem.put("icon", menu.get("MENU_ICON"));
            menuItem.put("to", menu.get("MENU_PATH"));
            menuItem.put("order", menu.get("MENU_ORDER"));
            menuItem.put("parentId", menu.get("PARENT_MENU_ID"));
            menuItem.put("items", new ArrayList<Map<String, Object>>());
            
            menuMap.put(menuId, menuItem);
        }
        
        // 2. 부모-자식 관계 설정
        for (Map<String, Object> menu : menuMap.values()) {
            String parentId = (String) menu.get("parentId");
            
            if (parentId == null || parentId.isEmpty()) {
                // 최상위 메뉴
                rootMenus.add(menu);
            } else {
                // 하위 메뉴
                Map<String, Object> parent = menuMap.get(parentId);
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parentItems = (List<Map<String, Object>>) parent.get("items");
                    parentItems.add(menu);
                }
            }
        }
        
        // 3. 메뉴 정렬 (order 기준)
        sortMenus(rootMenus);
        
        // 4. 불필요한 필드 제거
        removeInternalFields(rootMenus);
        
        return rootMenus;
    }

    /**
     * 메뉴 정렬 (재귀적으로 모든 레벨 정렬)
     */
    private void sortMenus(List<Map<String, Object>> menus) {
        menus.sort((a, b) -> {
            Integer orderA = (Integer) a.get("order");
            Integer orderB = (Integer) b.get("order");
            if (orderA == null) orderA = 999;
            if (orderB == null) orderB = 999;
            return orderA.compareTo(orderB);
        });
        
        for (Map<String, Object> menu : menus) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) menu.get("items");
            if (items != null && !items.isEmpty()) {
                sortMenus(items);
            }
        }
    }

    /**
     * 메뉴 아이템 정리 (권한 기반)
     */
    private void cleanMenuItems(List<Map<String, Object>> menus) {
        for (Map<String, Object> menu : menus) {
            menu.remove("parentPath");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) menu.get("items");
            if (items != null && !items.isEmpty()) {
                cleanMenuItems(items);
            } else {
                // 빈 items 배열 제거
                menu.remove("items");
            }
        }
    }

    /**
     * 내부 필드 제거 (id, parentId, order 등) - 기존 호환성
     */
    private void removeInternalFields(List<Map<String, Object>> menus) {
        for (Map<String, Object> menu : menus) {
            menu.remove("id");
            menu.remove("parentId");
            menu.remove("order");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) menu.get("items");
            if (items != null && !items.isEmpty()) {
                removeInternalFields(items);
            } else {
                // 빈 items 배열 제거
                menu.remove("items");
            }
        }
    }

    /**
     * 역할별 기본 메뉴 생성 (데이터베이스에 메뉴가 없을 경우 사용)
     */
    public List<Map<String, Object>> getDefaultMenusByRole(String roleName) {
        switch (roleName.toUpperCase()) {
            case "SYSTEM_ADMIN":
            case "GENERAL_MANAGER":
                return getHeadquarterMenus();
            case "BRANCH_MANAGER":
            case "BRANCH_EMPLOYEE":
                return getBranchMenus();
            case "SUPPLIER_ADMIN":
            case "SUPPLIER_EMPLOYEE":
                return getSupplierMenus();
            default:
                return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getHeadquarterMenus() {
        return Arrays.asList(
            createMenu("대시보드", "pi pi-fw pi-home", "/main", Arrays.asList(
                createMenu("본사 대시보드", "pi pi-fw pi-chart-line", "/dashboard/hq"),
                createMenu("지점 대시보드", "pi pi-fw pi-chart-line", "/dashboard/branch"),
                createMenu("공급업체 대시보드", "pi pi-fw pi-chart-pie", "/dashboard/supplier")
            )),
            createMenu("기준정보", "pi pi-fw pi-cog", "/standard", Arrays.asList(
                createMenu("공급업체 기준정보", "pi pi-fw pi-pen-to-square", "/standard/supplier"),
                createMenu("지점 기준정보", "pi pi-fw pi-pen-to-square", "/standard/branch"),
                createMenu("제품 기준정보", "pi pi-fw pi-pen-to-square", "/standard/product"),
                createMenu("제품 승인 정보", "pi pi-fw pi-pen-to-square", "/standard/productapproval")
            )),
            createMenu("재고 / 물류", "pi pi-fw pi-box", "/inventory", Arrays.asList(
                createMenu("본사 재고 현황", "pi pi-fw pi-box", "/inventory/stock/head"),
                createMenu("지점 재고 현황", "pi pi-fw pi-box", "/inventory/stock/branch"),
                createMenu("본사출고관리", "pi pi-fw pi-book", "/hqOutbndMgmt"),
                createMenu("지점입고관리", "pi pi-fw pi-book", "/brInbndMgmt"),
                createMenu("공급업체 출고관리", "pi pi-fw pi-book", "/supOutbndMgmt"),
                createMenu("본사입고관리", "pi pi-fw pi-book", "/hqInbndMgmt")
            )),
            createMenu("구매", "pi pi-fw pi-shopping-cart", "/purchase", Arrays.asList(
                createMenu("본사발주서관리", "pi pi-fw pi-book", "/orders/head"),
                createMenu("본사발주서조회", "pi pi-fw pi-book", "/orders/head/view"),
                createMenu("지점발주서관리", "pi pi-fw pi-book", "/orders/branch"),
                createMenu("지점발주서조회", "pi pi-fw pi-book", "/orders/branch/view"),
                createMenu("발주제안관리", "pi pi-fw pi-book", "/orders/supplier"),
                createMenu("발주제안조회", "pi pi-fw pi-book", "/orders/supplier/view")
            )),
            createMenu("권한", "pi pi-fw pi-users", "/roles", Arrays.asList(
                createMenu("사원 권한 관리", "pi pi-fw pi-users", "/roles/empmanage"),
                createMenu("역할 권한 관리", "pi pi-fw pi-users", "/roles/rolemanage")
            ))
        );
    }

    private List<Map<String, Object>> getBranchMenus() {
        return Arrays.asList(
            createMenu("대시보드", "pi pi-fw pi-home", "/main", Arrays.asList(
                createMenu("지점 대시보드", "pi pi-fw pi-chart-line", "/dashboard/branch")
            )),
            createMenu("재고 / 물류", "pi pi-fw pi-box", "/inventory", Arrays.asList(
                createMenu("지점 재고 현황", "pi pi-fw pi-box", "/inventory/stock/branch"),
                createMenu("지점입고관리", "pi pi-fw pi-book", "/brInbndMgmt")
            )),
            createMenu("구매", "pi pi-fw pi-shopping-cart", "/purchase", Arrays.asList(
                createMenu("지점발주서관리", "pi pi-fw pi-book", "/orders/branch"),
                createMenu("지점발주서조회", "pi pi-fw pi-book", "/orders/branch/view")
            )),
            createMenu("판매", "pi pi-fw pi-chart-line", "/sales", Arrays.asList(
                createMenu("매출 계획 조회", "pi pi-fw pi-book", "/sales/plan"),
                createMenu("주문 등록", "pi pi-fw pi-book", "/sales/orders"),
                createMenu("주문 조회 및 정산", "pi pi-fw pi-book", "/sales/orders/view"),
                createMenu("판매 이력 조회", "pi pi-fw pi-book", "/sales/history"),
                createMenu("매출 실적 조회", "pi pi-fw pi-chart-line", "/sales/performance")
            ))
        );
    }

    private List<Map<String, Object>> getSupplierMenus() {
        return Arrays.asList(
            createMenu("대시보드", "pi pi-fw pi-home", "/main", Arrays.asList(
                createMenu("공급업체 대시보드", "pi pi-fw pi-chart-pie", "/dashboard/supplier")
            )),
            createMenu("재고 / 물류", "pi pi-fw pi-box", "/inventory", Arrays.asList(
                createMenu("공급업체 출고관리", "pi pi-fw pi-book", "/supOutbndMgmt")
            )),
            createMenu("구매", "pi pi-fw pi-shopping-cart", "/purchase", Arrays.asList(
                createMenu("발주제안관리", "pi pi-fw pi-book", "/orders/supplier"),
                createMenu("발주제안조회", "pi pi-fw pi-book", "/orders/supplier/view")
            ))
        );
    }

    private Map<String, Object> createMenu(String label, String icon, String to) {
        Map<String, Object> menu = new HashMap<>();
        menu.put("label", label);
        menu.put("icon", icon);
        menu.put("to", to);
        return menu;
    }

    private Map<String, Object> createMenu(String label, String icon, String to, List<Map<String, Object>> items) {
        Map<String, Object> menu = createMenu(label, icon, to);
        menu.put("items", items);
        return menu;
    }
}