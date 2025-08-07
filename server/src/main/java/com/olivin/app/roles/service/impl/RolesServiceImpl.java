package com.olivin.app.roles.service.impl;

import java.util.Date;
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
        
        // 권한의 권한 ID 목록도 함께 조회
        if (role != null) {
            List<Integer> permissionIds = roleMapper.selectPermissionIdsByRoleId(roleId);
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
        
        // 등록일 설정
        roleVO.setRegDate(new Date());
        
        // 등록자가 없으면 기본값 설정
        if (roleVO.getRegUser() == null || roleVO.getRegUser().isEmpty()) {
            roleVO.setRegUser("SYSTEM");
        }
        
        int result = roleMapper.insertRole(roleVO);
        
        // 권한 할당이 있는 경우 처리
        if (result > 0 && roleVO.getPermissionIds() != null && !roleVO.getPermissionIds().isEmpty()) {
            assignPermissionsToRole(roleVO.getRoleId(), roleVO.getPermissionIds());
        }
        
        return result;
    }
    
    @Override
    public int modifyRole(RolesVO roleVO) {
        roleVO.setUpdateDate(new Date());
        
        int result = roleMapper.updateRole(roleVO);
        
        // 권한 재할당이 있는 경우 처리
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
    
    // === 권한-권한 매핑 관련 (간소화) ===
    
    @Override
    public List<Map<String, Object>> getAllPermissions() {
        return roleMapper.selectAllPermissions();
    }
    
    @Override
    public List<Integer> getPermissionIdsByRoleId(Integer roleId) {
        return roleMapper.selectPermissionIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public int assignPermissionsToRole(Integer roleId, List<Integer> permissionIds) {
        // 기존 권한 매핑 모두 삭제
        roleMapper.deleteRolePermissions(roleId);
        
        int totalAssigned = 0;
        
        // 새로운 권한 매핑 추가
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Integer permId : permissionIds) {
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
}