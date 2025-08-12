// AuthService.java
package com.olivin.app.auth.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }
    
    public LoginResponseVO login(LoginRequestVO request) {
        try {
            log.info("로그인 시도: {}", request.getEmployeeId());
            
            // Spring Security 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmployeeId(), request.getPassword())
            );
            
            log.debug("Spring Security 인증 성공: {}", request.getEmployeeId());
            
            // 사용자 정보 조회
            UserVO user = userService.findByEmployeeId(request.getEmployeeId());
            
            // 사용자 권한 조회
            List<PermissionVO> permissions = userService.getUserPermissions(user.getEmployeeId());
            
            // UserDetails 생성
            UserDetails userDetails = userService.loadUserByUsername(request.getEmployeeId());
            
            // JWT 토큰 생성 (추가 정보 포함)
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("employeeId", user.getEmployeeId());
            extraClaims.put("empName", user.getEmpName());
            extraClaims.put("role", user.getRoleName());
            extraClaims.put("departmentId", user.getDepartmentId());
            extraClaims.put("position", user.getPosition());
            extraClaims.put("compId", user.getCompId());
            extraClaims.put("compName", user.getCompName());
            
            String token = jwtService.generateToken(userDetails, extraClaims);
            
            // 응답 데이터 구성
            LoginResponseVO.UserInfoVO userDto = new LoginResponseVO.UserInfoVO(
                user.getEmployeeId(),
                user.getEmpName(),
                user.getEmail(),
                user.getPosition(),
                user.getDepartmentId(),
                user.getDeptName(),
                user.getCompId(),
                user.getCompName()
            );
            
            LoginResponseVO.RoleInfoVO roleDto = new LoginResponseVO.RoleInfoVO(
                user.getRoleId(),
                user.getRoleName()
            );
            
            List<LoginResponseVO.PermissionInfoVO> permissionDtos = permissions.stream()
                .map(permission -> new LoginResponseVO.PermissionInfoVO(
                    permission.getPermId(),
                    permission.getPermName(),
                    permission.getPermDescription()
                ))
                .collect(Collectors.toList());
            
            LoginResponseVO response = new LoginResponseVO(token, userDto, roleDto, permissionDtos, new ArrayList<>());
            
            log.info("로그인 성공: {} (역할: {}, 권한 수: {})", 
                user.getEmployeeId(), user.getRoleName(), permissions.size());
            
            return response;
            
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 인증 정보: {}", request.getEmployeeId());
            throw new RuntimeException("사원번호 또는 비밀번호가 올바르지 않습니다.");
        } catch (UsernameNotFoundException e) {
            log.warn("로그인 실패 - 사용자 없음: {}", request.getEmployeeId());
            throw new RuntimeException("사원번호 또는 비밀번호가 올바르지 않습니다.");
        } catch (Exception e) {
            log.error("로그인 처리 중 예외 발생: {} - {}", request.getEmployeeId(), e.getMessage(), e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.");
        }
    }
    
    public LoginResponseVO getUserInfo(String employeeId) {
        try {
            log.debug("사용자 정보 조회: {}", employeeId);
            
            UserVO user = userService.findByEmployeeId(employeeId);
            List<PermissionVO> permissions = userService.getUserPermissions(user.getEmployeeId());
            
            LoginResponseVO.UserInfoVO userDto = new LoginResponseVO.UserInfoVO(
                user.getEmployeeId(),
                user.getEmpName(),
                user.getEmail(),
                user.getPosition(),
                user.getDepartmentId(),
                user.getDeptName(),
                user.getCompId(),
                user.getCompName()
            );
            
            LoginResponseVO.RoleInfoVO roleDto = new LoginResponseVO.RoleInfoVO(
                user.getRoleId(),
                user.getRoleName()
            );
            
            List<LoginResponseVO.PermissionInfoVO> permissionDtos = permissions.stream()
                .map(permission -> new LoginResponseVO.PermissionInfoVO(
                    permission.getPermId(),
                    permission.getPermName(),
                    permission.getPermDescription()
                ))
                .collect(Collectors.toList());
            
            return new LoginResponseVO(null, userDto, roleDto, permissionDtos, new ArrayList<>());
            
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {} - {}", employeeId, e.getMessage());
            throw new RuntimeException("사용자 정보를 조회할 수 없습니다.");
        }
    }
}