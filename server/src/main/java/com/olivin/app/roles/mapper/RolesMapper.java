package com.olivin.app.roles.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.olivin.app.roles.service.RolesVO;

@Mapper
public interface RolesMapper {
    
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
    
    // === 권한-권한 매핑 관련 (간소화) ===
    
    // 모든 권한 목록 조회 (Map 형태)
    List<Map<String, Object>> selectAllPermissions();
    
    // 특정 권한의 권한 ID 목록 조회
    List<Integer> selectPermissionIdsByRoleId(Integer roleId);
    
    // 권한에 권한 할당
    int insertRolePermission(@Param("roleId") Integer roleId, @Param("permId") Integer permId);
    
    // 권한의 모든 권한 삭제
    int deleteRolePermissions(Integer roleId);
    
    // 권한별 권한 수 조회
    List<Map<String, Object>> selectRolePermissionCount();
}