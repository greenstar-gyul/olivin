// PermissionMapper.java
package com.olivin.app.auth.mapper;

import com.olivin.app.auth.service.PermissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PermissionMapper {
    
    /**
     * 권한 ID로 조회
     */
    Optional<PermissionVO> findByPermId(@Param("permId") Long permId);
    
    /**
     * 권한 이름으로 조회
     */
    Optional<PermissionVO> findByPermName(@Param("permName") String permName);
    
    /**
     * 모든 권한 조회
     */
    List<PermissionVO> findAllPermissions();
    
    /**
     * 사원별 권한 조회 (사원번호로)
     */
    List<PermissionVO> findPermissionsByEmployeeId(@Param("employeeId") String employeeId);
}