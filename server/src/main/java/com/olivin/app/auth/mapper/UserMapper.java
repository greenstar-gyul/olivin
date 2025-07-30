// UserMapper.java
package com.olivin.app.auth.mapper;

import com.olivin.app.auth.service.UserVO;
import com.olivin.app.auth.service.PermissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    
    /**
     * 사원번호로 사용자 조회 (활성 사용자만)
     */
    Optional<UserVO> findByEmployeeIdAndStatusActive(@Param("employeeId") String employeeId);
    
    /**
     * 사원번호로 사용자 조회
     */
    Optional<UserVO> findByEmployeeId(@Param("employeeId") String employeeId);
    
    /**
     * 사용자의 권한 목록 조회
     */
    List<PermissionVO> findPermissionsByEmployeeId(@Param("employeeId") String employeeId);
    
    /**
     * 사용자 정보 수정
     */
    int updateUser(UserVO user);
    
    /**
     * 사원번호 존재 확인
     */
    boolean existsByEmployeeId(@Param("employeeId") String employeeId);
}