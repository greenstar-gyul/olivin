// UserService.java (최종 수정됨 - 권한 없어도 로그인 가능)
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
import java.util.ArrayList;
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
        List<PermissionVO> permissions = null;
        try {
            permissions = userMapper.findPermissionsByEmployeeId(user.getEmployeeId());
        } catch (Exception e) {
            log.warn("권한 조회 중 오류 발생: {} - {}", employeeId, e.getMessage());
            permissions = new ArrayList<>();
        }
        
        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        
        // Spring Security 권한 목록 생성
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 권한이 없는 경우에도 기본 권한 부여
        if (permissions.isEmpty()) {
            log.warn("사용자 {}에게 설정된 권한이 없습니다. 기본 권한을 부여합니다.", employeeId);
            // 기본 권한들 추가
            authorities.add(new SimpleGrantedAuthority("/main"));
            authorities.add(new SimpleGrantedAuthority("/test"));
        } else {
            // 실제 권한들 추가 (permId를 그대로 사용)
            authorities = permissions.stream()
                .filter(permission -> permission != null && permission.getPermId() != null)
                .map(permission -> new SimpleGrantedAuthority(permission.getPermId()))
                .collect(Collectors.toList());
            
            log.debug("사용자 {}에게 부여된 권한: {}", employeeId, 
                permissions.stream().map(PermissionVO::getPermId).collect(Collectors.toList()));
        }
        
        // 역할도 권한으로 추가
        if (user.getRoleName() != null && !user.getRoleName().trim().isEmpty()) {
            // ROLE_ 중복 제거 및 정리
            String cleanRoleName = user.getRoleName().trim()
                .replace("ROLE_", "")
                .toUpperCase();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + cleanRoleName));
            log.debug("사용자 {}에게 부여된 역할: ROLE_{}", employeeId, cleanRoleName);
        } else {
            // 역할이 없는 경우 기본 역할 부여
            log.warn("사용자 {}에게 설정된 역할이 없습니다. 기본 역할(USER)을 부여합니다.", employeeId);
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        log.info("사용자 권한 로드 완료: {} (총 권한 수: {})", employeeId, authorities.size());
        
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
        try {
            List<PermissionVO> permissions = userMapper.findPermissionsByEmployeeId(employeeId);
            
            if (permissions == null) {
                log.warn("사용자 {}의 권한 조회 결과가 null입니다.", employeeId);
                return new ArrayList<>();
            }
            
            if (permissions.isEmpty()) {
                log.warn("사용자 {}에게 설정된 권한이 없습니다.", employeeId);
            } else {
                log.debug("사용자 {}의 권한 조회 완료: {} 개", employeeId, permissions.size());
            }
            
            return permissions;
            
        } catch (Exception e) {
            log.error("사용자 {}의 권한 조회 중 오류 발생: {}", employeeId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    public boolean existsByEmployeeId(String employeeId) {
        try {
            return userMapper.existsByEmployeeId(employeeId);
        } catch (Exception e) {
            log.error("사용자 존재 여부 확인 중 오류: {} - {}", employeeId, e.getMessage());
            return false;
        }
    }
    
    @Transactional
    public int updateUser(UserVO user) {
        return userMapper.updateUser(user);
    }
}