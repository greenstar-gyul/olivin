package com.olivin.app.roles.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.olivin.app.roles.service.RolesVO;

@Mapper
public interface RolesMapper {
    
    // ========== 기존 권한 관리 메서드 ==========
    
    // 모든 권한 목록 조회
    List<RolesVO> selectAllRoles();
    
    // 조건별 권한 목록 조회
    List<RolesVO> selectRoleList(RolesVO roleVO);
    
    // 검색 조건에 따른 권한 목록 조회
    List<RolesVO> selectRolesByCondition(Map<String, Object> searchParams);
    
    // 특정 권한 조회 (권한 정보 포함)
    RolesVO selectRole(Integer roleId);
    
    // 권한 등록
    int insertRole(RolesVO roleVO);
    
    // 권한 정보 수정
    int updateRole(RolesVO roleVO);
    
    // 권한 삭제
    int deleteRole(Integer roleId);
    
    // 권한 ID 존재 여부 확인
    int checkRoleId(Integer roleId);
    
    // 권한명 중복 확인
    int checkRoleName(@Param("roleName") String roleName, @Param("excludeRoleId") Integer excludeRoleId);
    
    // 마지막 권한 ID 조회 (ID 자동생성용)
    Integer selectLastRoleId();
    
    // 권한별 직원 수 통계
    List<Map<String, Object>> selectRoleStatsByEmployee();
    
    // 최근 등록된 권한 조회
    List<RolesVO> selectRecentRoles(int limit);
    
    // 권한 사용 여부 확인 (직원이 사용 중인지)
    int checkRoleInUse(Integer roleId);
    
    // === 권한-권한 매핑 관련 (PERM_ID를 String으로 변경) ===
    
    // 모든 권한 목록 조회 (Map 형태)
    List<Map<String, Object>> selectAllPermissions();
    
    // ✅ 특정 권한의 권한 ID 목록 조회 - String으로 변경
    List<String> selectPermissionIdsByRoleId(Integer roleId);
    
    // ✅ 권한에 권한 할당 - permId를 String으로 변경
    int insertRolePermission(@Param("roleId") Integer roleId, @Param("permId") String permId);
    
    // 권한의 모든 권한 삭제
    int deleteRolePermissions(Integer roleId);
    
    // 권한별 권한 수 조회
    List<Map<String, Object>> selectRolePermissionCount();
    
    // ========== 추가: 사원 관련 메서드 ==========
    
    // 사원 목록 조회 (권한 정보 포함)
    List<Map<String, Object>> selectEmployeesWithPermissions(Map<String, Object> searchParams);
    
    // ✅ 특정 사원의 권한 ID 목록 조회 (역할을 통해) - String으로 변경
    List<String> selectEmployeePermissionIds(String employeeId);
    
    // 사원의 역할 변경
    int updateEmployeeRole(Map<String, Object> params);
    
    // 사원 존재 여부 확인
    int checkEmployeeExists(String employeeId);
    
    // ========== 추가: 유틸리티 메서드 ==========
    
    // 사원의 역할명 조회
    String selectEmployeeRoleName(String employeeId);
    
    // 역할별 사원 수 조회
    Map<String, Object> selectRoleEmployeeCount(Integer roleId);
    
    // 사원의 권한 상세 정보 조회 (권한명 포함)
    List<Map<String, Object>> selectEmployeePermissionDetails(String employeeId);
    
    // 부서별 권한 통계
    List<Map<String, Object>> selectDepartmentPermissionStats();
}