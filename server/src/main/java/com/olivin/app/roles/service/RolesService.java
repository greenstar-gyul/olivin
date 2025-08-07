package com.olivin.app.roles.service;

import java.util.List;
import java.util.Map;

public interface RolesService {
    
    // 모든 권한 목록 조회
    List<RolesVO> getAllRoles();
    
    // 조건별 권한 목록 조회
    List<RolesVO> getRoleList(RolesVO roleVO);
    
    // 특정 권한 조회
    RolesVO getRole(Integer roleId);
    
    // 권한 등록
    int saveRole(RolesVO roleVO);
    
    // 권한 정보 수정
    int modifyRole(RolesVO roleVO);
    
    // 권한 삭제
    int removeRole(Integer roleId);
    
    // 검색 조건에 따른 권한 목록 조회
    List<RolesVO> searchRoles(Map<String, Object> searchParams);
    
    // 권한 ID 존재 여부 확인
    boolean isRoleIdExists(Integer roleId);
    
    // 권한명 중복 확인
    boolean isRoleNameExists(String roleName, Integer excludeRoleId);
    
    // 다음 권한 ID 생성
    Integer getNextRoleId();
    
    // 권한별 직원 수 통계
    List<Map<String, Object>> getRoleStatsByEmployee();
    
    // 최근 등록된 권한 목록
    List<RolesVO> getRecentRoles(int limit);
    
    // 권한 사용 여부 확인 (직원이 사용 중인지)
    boolean isRoleInUse(Integer roleId);
    
    // === 권한-권한 매핑 관련 (간소화) ===
    
    // 모든 권한 목록 조회
    List<Map<String, Object>> getAllPermissions();
    
    // 특정 권한의 권한 ID 목록 조회
    List<Integer> getPermissionIdsByRoleId(Integer roleId);
    
    // 권한에 권한 할당/수정
    int assignPermissionsToRole(Integer roleId, List<Integer> permissionIds);
    
    // 권한의 모든 권한 삭제
    int removeAllPermissionsFromRole(Integer roleId);
    
    // 권한별 권한 수 조회
    List<Map<String, Object>> getRolePermissionCount();
}