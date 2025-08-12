// LoginResponseVO.java
package com.olivin.app.auth.service;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseVO {
    
    private String token;                           // JWT 토큰
    private UserInfoVO user;                        // 사용자 정보
    private RoleInfoVO role;                        // 역할 정보
    private List<PermissionInfoVO> permissions;     // 권한 목록
    private List<Map<String, Object>> menus;       // 메뉴 목록
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfoVO {
        private String employeeId;      // 사원번호
        private String empName;         // 사원명
        private String email;           // 이메일
        private String position;        // 직책
        private String departmentId;      // 부서ID
        private String deptName;        // 부서명
        private String compId;         // 회사ID
        private String compName;       // 회사명
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleInfoVO {
        private Long roleId;            // 역할ID
        private String roleName;        // 역할명
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PermissionInfoVO {
        private Long permId;            // 권한ID
        private String permName;        // 권한명
        private String permDescription; // 권한 설명
    }
}