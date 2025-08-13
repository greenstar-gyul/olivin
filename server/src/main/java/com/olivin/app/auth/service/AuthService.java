// AuthService.java (수정됨)
package com.olivin.app.auth.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
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

import com.olivin.app.auth.mapper.MenuMapper;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final MenuMapper menuMapper;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    public AuthService(UserService userService, JwtService jwtService, MenuMapper menuMapper) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.menuMapper = menuMapper;
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
            
            // 사용자 권한 조회 (MenuMapper 사용)
            List<Map<String, Object>> userPermissions = menuMapper.getUserPermissions(user.getEmployeeId());
            List<PermissionVO> permissions = convertMapToPermissionVO(userPermissions);
            
            // UserDetails 생성
            UserDetails userDetails = userService.loadUserByUsername(request.getEmployeeId());
            
            // JWT 토큰 생성 (추가 정보 포함)
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("employeeId", user.getEmployeeId());
            extraClaims.put("empName", user.getEmpName() != null ? user.getEmpName() : "");
            extraClaims.put("role", user.getRoleName() != null ? user.getRoleName() : "USER");
            extraClaims.put("departmentId", user.getDepartmentId() != null ? user.getDepartmentId() : "");
            extraClaims.put("position", user.getPosition() != null ? user.getPosition() : "");
            extraClaims.put("compId", user.getCompId() != null ? user.getCompId() : "");
            extraClaims.put("compName", user.getCompName() != null ? user.getCompName() : "");
            
            String token = jwtService.generateToken(userDetails, extraClaims);
            
            // 응답 데이터 구성
            LoginResponseVO.UserInfoVO userDto = new LoginResponseVO.UserInfoVO(
                user.getEmployeeId(),
                user.getEmpName() != null ? user.getEmpName() : "",
                user.getEmail() != null ? user.getEmail() : "",
                user.getPosition() != null ? user.getPosition() : "",
                user.getDepartmentId() != null ? user.getDepartmentId() : "",
                user.getDeptName() != null ? user.getDeptName() : "",
                user.getCompId() != null ? user.getCompId() : "",
                user.getCompName() != null ? user.getCompName() : "",
                user.getCompType() != null ? user.getCompType() : ""
            );
            
            LoginResponseVO.RoleInfoVO roleDto = new LoginResponseVO.RoleInfoVO(
                user.getRoleId() != null ? user.getRoleId() : 0L,
                user.getRoleName() != null ? user.getRoleName() : "USER"
            );
            
            // 권한이 없는 경우 빈 목록 반환
            List<LoginResponseVO.PermissionInfoVO> permissionDtos = permissions.stream()
                .map(permission -> new LoginResponseVO.PermissionInfoVO(
                    permission.getPermId(),
                    permission.getPermName(),
                    permission.getPermDescription(),
                    permission.getIcon(),
                    permission.getParentTo()
                ))
                .collect(Collectors.toList());
            
            LoginResponseVO response = new LoginResponseVO(token, userDto, roleDto, permissionDtos, new ArrayList<>());
            
            log.info("로그인 성공: {} (역할: {}, 권한 수: {})", 
                user.getEmployeeId(), 
                user.getRoleName() != null ? user.getRoleName() : "USER", 
                permissions.size());
            
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
            
            // 사용자 권한 조회 (MenuMapper 사용)
            List<Map<String, Object>> userPermissions = menuMapper.getUserPermissions(user.getEmployeeId());
            List<PermissionVO> permissions = convertMapToPermissionVO(userPermissions);
            
            LoginResponseVO.UserInfoVO userDto = new LoginResponseVO.UserInfoVO(
                user.getEmployeeId(),
                user.getEmpName() != null ? user.getEmpName() : "",
                user.getEmail() != null ? user.getEmail() : "",
                user.getPosition() != null ? user.getPosition() : "",
                user.getDepartmentId() != null ? user.getDepartmentId() : "",
                user.getDeptName() != null ? user.getDeptName() : "",
                user.getCompId() != null ? user.getCompId() : "",
                user.getCompName() != null ? user.getCompName() : "",
                user.getCompType() != null ? user.getCompType() : ""
            );
            
            LoginResponseVO.RoleInfoVO roleDto = new LoginResponseVO.RoleInfoVO(
                user.getRoleId() != null ? user.getRoleId() : 0L,
                user.getRoleName() != null ? user.getRoleName() : "USER"
            );
            
            List<LoginResponseVO.PermissionInfoVO> permissionDtos = permissions.stream()
                .map(permission -> new LoginResponseVO.PermissionInfoVO(
                    permission.getPermId(),
                    permission.getPermName(),
                    permission.getPermDescription(),
                    permission.getIcon(),
                    permission.getParentTo()
                ))
                .collect(Collectors.toList());
            
            return new LoginResponseVO(null, userDto, roleDto, permissionDtos, new ArrayList<>());
            
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {} - {}", employeeId, e.getMessage());
            throw new RuntimeException("사용자 정보를 조회할 수 없습니다.");
        }
    }
    
    /**
     * Map을 PermissionVO로 변환
     */
    private List<PermissionVO> convertMapToPermissionVO(List<Map<String, Object>> mapList) {
        return mapList.stream()
            .map(map -> {
                PermissionVO perm = new PermissionVO();
                perm.setPermId((String) map.get("MENU_PATH"));
                perm.setPermName((String) map.get("MENU_NAME"));
                perm.setPermDescription((String) map.get("MENU_DESCRIPTION"));
                perm.setIcon((String) map.get("MENU_ICON"));
                perm.setParentTo((String) map.get("PARENT_MENU_PATH"));
                return perm;
            })
            .collect(Collectors.toList());
    }
}