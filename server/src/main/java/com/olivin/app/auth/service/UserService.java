// UserService.java
package com.olivin.app.auth.service;

import com.olivin.app.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    
    private final UserMapper userMapper;
    
    @Override
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        log.debug("사용자 로드 시도: {}", employeeId);
        
        UserVO user = userMapper.findByEmployeeIdAndStatusActive(employeeId)
            .orElseThrow(() -> {
                log.error("사용자를 찾을 수 없습니다: {}", employeeId);
                return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + employeeId);
            });
        
        // 사용자의 권한 목록 조회
        List<PermissionVO> permissions = userMapper.findPermissionsByEmployeeId(user.getEmployeeId());
        
        // Spring Security 권한 목록 생성
        List<SimpleGrantedAuthority> authorities = permissions.stream()
            .map(permission -> new SimpleGrantedAuthority("PERM_" + permission.getPermName()))
            .collect(Collectors.toList());
        
        // 역할도 권한으로 추가
        if (user.getRoleName() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleName()));
        }
        
        log.debug("사용자 권한 로드 완료: {} (권한 수: {})", employeeId, authorities.size());
        
        return User.builder()
            .username(user.getEmployeeId())
            .password(user.getPassword())
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!"050001".equals(user.getStatus()))
            .build();
    }
    
    public UserVO findByEmployeeId(String employeeId) {
        return userMapper.findByEmployeeIdAndStatusActive(employeeId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + employeeId));
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
}