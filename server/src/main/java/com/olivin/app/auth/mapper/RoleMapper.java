// RoleMapper.java
package com.olivin.app.auth.mapper;

import com.olivin.app.auth.service.RoleVO;
import com.olivin.app.auth.service.PermissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleMapper {
    
    /**
     * 역할 ID로 조회
     */
    Optional<RoleVO> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 역할 이름으로 조회
     */
    Optional<RoleVO> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * 모든 역할 조회
     */
    List<RoleVO> findAllRoles();
    
    /**
     * 역할별 권한 목록 조회
     */
    List<PermissionVO> findPermissionsByRoleId(@Param("roleId") Long roleId);
}