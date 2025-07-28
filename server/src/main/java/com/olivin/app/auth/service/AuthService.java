// AuthService.java
package com.olivin.app.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }
    
    public LoginResponseVO login(LoginRequestVO request) {
        try {
            log.info("로그인 시도: {}", request.getEmployeeId());
            
            // 사용자 정보 조회
            UserVO user = userService.findByEmployeeId(request.getEmployeeId());
            
            // TODO: 비밀번호 검증 로직 추가 필요
            // 예: if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            //     throw new RuntimeException("비밀번호가 올바르지 않습니다.");
            // }
            
            // 사용자 권한 조회
            List<PermissionVO> permissions = userService.getUserPermissions(user.getEmployeeId());
            
            // JWT 토큰 생성 (추가 정보 포함)
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("employeeId", user.getEmployeeId());
            extraClaims.put("empName", user.getEmpName());
            extraClaims.put("role", user.getRoleName());
            extraClaims.put("departmentId", user.getDepartmentId());
            extraClaims.put("position", user.getPosition());
            
            String token = jwtService.generateToken(user.getEmployeeId(), extraClaims);
            
            // 응답 데이터 구성
            LoginResponseVO.UserInfoVO userDto = new LoginResponseVO.UserInfoVO(
                user.getEmployeeId(),
                user.getEmpName(),
                user.getEmail(),
                user.getPosition(),
                user.getDepartmentId(),
                user.getDeptName()
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
            
            LoginResponseVO response = new LoginResponseVO(token, userDto, roleDto, permissionDtos);
            
            log.info("로그인 성공: {} (역할: {}, 권한 수: {})", 
                user.getEmployeeId(), user.getRoleName(), permissions.size());
            
            return response;
            
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
                user.getDeptName()
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
            
            return new LoginResponseVO(null, userDto, roleDto, permissionDtos);
            
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {} - {}", employeeId, e.getMessage());
            throw new RuntimeException("사용자 정보를 조회할 수 없습니다.");
        }
    }
}