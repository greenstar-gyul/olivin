// UserService.java
package com.olivin.app.auth.service;

import com.olivin.app.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    
    private final UserMapper userMapper;
    
    public UserVO findByEmployeeId(String employeeId) {
        return userMapper.findByEmployeeIdAndStatusActive(employeeId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + employeeId));
    }
    
    public List<PermissionVO> getUserPermissions(String employeeId) {
        return userMapper.findPermissionsByEmployeeId(employeeId);
    }
    
    public boolean existsByEmployeeId(String employeeId) {
        return userMapper.existsByEmployeeId(employeeId);
    }
    
    @Transactional
    public int updateUser(UserVO user) {
        return userMapper.updateUser(user);
    }
    
    // 비밀번호 검증 메소드 추가 (필요시)
    public boolean validatePassword(String employeeId, String password) {
        UserVO user = findByEmployeeId(employeeId);
        // TODO: 실제 비밀번호 검증 로직 구현
        // 예: return passwordEncoder.matches(password, user.getPassword());
        return user.getPassword().equals(password); // 임시 코드
    }
}