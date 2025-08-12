package com.olivin.app.roles.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.roles.mapper.RolesMapper;
import com.olivin.app.roles.service.RolesService;
import com.olivin.app.roles.service.RolesVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RolesServiceImpl implements RolesService {
    
    private final RolesMapper roleMapper;
    
    // ========== 기존 역할 관리 구현 ==========
    
    @Override
    public List<RolesVO> getAllRoles() {
        return roleMapper.selectAllRoles();
    }
    
    @Override
    public List<RolesVO> getRoleList(RolesVO roleVO) {
        return roleMapper.selectRoleList(roleVO);
    }
    
    @Override
    public RolesVO getRole(Integer roleId) {
        RolesVO role = roleMapper.selectRole(roleId);
        
        // 권한의 권한 ID 목록도 함께 조회 (String 타입으로 변경)
        if (role != null) {
            List<String> permissionIds = roleMapper.selectPermissionIdsByRoleId(roleId);
            role.setPermissionIds(permissionIds);
        }
        
        return role;
    }
    
    @Override
    public int saveRole(RolesVO roleVO) {
        // 권한 ID 자동생성
        if (roleVO.getRoleId() == null) {
            Integer nextRoleId = getNextRoleId();
            roleVO.setRoleId(nextRoleId);
        }
        
        int result = roleMapper.insertRole(roleVO);
        
        // 권한 할당이 있는 경우 처리 (String 타입으로 변경)
        if (result > 0 && roleVO.getPermissionIds() != null && !roleVO.getPermissionIds().isEmpty()) {
            assignPermissionsToRole(roleVO.getRoleId(), roleVO.getPermissionIds());
        }
        
        return result;
    }
    
    @Override
    public int modifyRole(RolesVO roleVO) {
        int result = roleMapper.updateRole(roleVO);
        
        // 권한 재할당이 있는 경우 처리 (String 타입으로 변경)
        if (result > 0 && roleVO.getPermissionIds() != null) {
            assignPermissionsToRole(roleVO.getRoleId(), roleVO.getPermissionIds());
        }
        
        return result;
    }
    
    @Override
    public int removeRole(Integer roleId) {
        // 권한이 사용 중인지 확인
        if (isRoleInUse(roleId)) {
            throw new RuntimeException("해당 권한을 사용 중인 직원이 있어 삭제할 수 없습니다.");
        }
        
        // 권한-권한 매핑 먼저 삭제
        roleMapper.deleteRolePermissions(roleId);
        
        // 권한 삭제
        return roleMapper.deleteRole(roleId);
    }
    
    @Override
    public List<RolesVO> searchRoles(Map<String, Object> searchParams) {
        return roleMapper.selectRolesByCondition(searchParams);
    }
    
    @Override
    public boolean isRoleIdExists(Integer roleId) {
        return roleMapper.checkRoleId(roleId) > 0;
    }
    
    @Override
    public boolean isRoleNameExists(String roleName, Integer excludeRoleId) {
        return roleMapper.checkRoleName(roleName, excludeRoleId) > 0;
    }
    
    @Override
    public Integer getNextRoleId() {
        try {
            Integer lastRoleId = roleMapper.selectLastRoleId();
            return (lastRoleId != null) ? lastRoleId + 1 : 1;
        } catch (Exception e) {
            System.err.println("권한 ID 생성 중 오류: " + e.getMessage());
            e.printStackTrace();
            return 1; // 기본값
        }
    }
    
    @Override
    public List<Map<String, Object>> getRoleStatsByEmployee() {
        return roleMapper.selectRoleStatsByEmployee();
    }
    
    @Override
    public List<RolesVO> getRecentRoles(int limit) {
        return roleMapper.selectRecentRoles(limit);
    }
    
    @Override
    public boolean isRoleInUse(Integer roleId) {
        return roleMapper.checkRoleInUse(roleId) > 0;
    }
    
    // === 권한-권한 매핑 관련 (String 타입으로 변경) ===
    
    @Override
    public List<Map<String, Object>> getAllPermissions() {
        return roleMapper.selectAllPermissions();
    }
    
    @Override
    public List<String> getPermissionIdsByRoleId(Integer roleId) {
        return roleMapper.selectPermissionIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public int assignPermissionsToRole(Integer roleId, List<String> permissionIds) {
        // 기존 권한 매핑 모두 삭제
        roleMapper.deleteRolePermissions(roleId);
        
        int totalAssigned = 0;
        
        // 새로운 권한 매핑 추가 (String 타입으로 변경)
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (String permId : permissionIds) {
                int result = roleMapper.insertRolePermission(roleId, permId);
                totalAssigned += result;
            }
        }
        
        return totalAssigned;
    }
    
    @Override
    public int removeAllPermissionsFromRole(Integer roleId) {
        return roleMapper.deleteRolePermissions(roleId);
    }
    
    @Override
    public List<Map<String, Object>> getRolePermissionCount() {
        return roleMapper.selectRolePermissionCount();
    }
    
    // ========== 추가: 사원 관련 구현 ==========
    
    @Override
    public List<Map<String, Object>> getEmployeesWithPermissions(Map<String, Object> searchParams) {
        try {
            List<Map<String, Object>> employees = roleMapper.selectEmployeesWithPermissions(searchParams);
            
            // 데이터 후처리 (필요한 경우)
            for (Map<String, Object> employee : employees) {
                // permissionCount가 null인 경우 0으로 설정
                if (employee.get("permissionCount") == null) {
                    employee.put("permissionCount", 0);
                }
                // permissionNames가 null인 경우 빈 문자열로 설정
                if (employee.get("permissionNames") == null) {
                    employee.put("permissionNames", "");
                }
            }
            
            return employees;
        } catch (Exception e) {
            System.err.println("사원 목록 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("사원 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> getEmployeePermissionIds(String employeeId) {
        try {
            // 사원 존재 여부 확인
            if (!isEmployeeExists(employeeId)) {
                throw new RuntimeException("존재하지 않는 사원입니다: " + employeeId);
            }
            
            return roleMapper.selectEmployeePermissionIds(employeeId);
        } catch (Exception e) {
            System.err.println("사원 권한 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("사원 권한 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public int updateEmployeeRole(String employeeId, Integer roleId, String updateUser) {
        try {
            // 사원 존재 여부 확인
            if (!isEmployeeExists(employeeId)) {
                throw new RuntimeException("존재하지 않는 사원입니다: " + employeeId);
            }
            
            // 역할 존재 여부 확인
            if (!isRoleIdExists(roleId)) {
                throw new RuntimeException("존재하지 않는 역할입니다: " + roleId);
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("employeeId", employeeId);
            params.put("roleId", roleId);
            
            int result = roleMapper.updateEmployeeRole(params);
            
            if (result <= 0) {
                throw new RuntimeException("사원 역할 변경에 실패했습니다");
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("사원 역할 변경 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("사원 역할 변경에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isEmployeeExists(String employeeId) {
        try {
            return roleMapper.checkEmployeeExists(employeeId) > 0;
        } catch (Exception e) {
            System.err.println("사원 존재 여부 확인 중 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ========== 추가: 유틸리티 메서드 ==========
    
    /**
     * 사원의 역할명 조회
     */
    public String getEmployeeRoleName(String employeeId) {
        try {
            return roleMapper.selectEmployeeRoleName(employeeId);
        } catch (Exception e) {
            System.err.println("사원 역할명 조회 중 오류: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 역할별 사원 수 조회
     */
    public Map<String, Object> getRoleEmployeeCount(Integer roleId) {
        try {
            return roleMapper.selectRoleEmployeeCount(roleId);
        } catch (Exception e) {
            System.err.println("역할별 사원 수 조회 중 오류: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 사원의 권한 상세 정보 조회 (권한명 포함)
     */
    public List<Map<String, Object>> getEmployeePermissionDetails(String employeeId) {
        try {
            if (!isEmployeeExists(employeeId)) {
                throw new RuntimeException("존재하지 않는 사원입니다: " + employeeId);
            }
            
            return roleMapper.selectEmployeePermissionDetails(employeeId);
        } catch (Exception e) {
            System.err.println("사원 권한 상세 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("사원 권한 상세 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 부서별 권한 통계
     */
    public List<Map<String, Object>> getDepartmentPermissionStats() {
        try {
            return roleMapper.selectDepartmentPermissionStats();
        } catch (Exception e) {
            System.err.println("부서별 권한 통계 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}