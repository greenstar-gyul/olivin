// UserVO.java (로그인 전용)
package com.olivin.app.auth.service;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    // EMPLOYEES 테이블 - 로그인에 필요한 필드들만
    private String employeeId;    // EMPLOYEE_ID (로그인 ID)
    private String empName;       // EMP_NAME (사용자 이름)
    private String password;      // PASSWORD
    private String email;         // EMAIL
    private String phone;         // PHONE
    private String position;      // POSITION
    private Long roleId;          // ROLE_ID
    private String status;        // STATUS (활성/비활성 체크용)
    private String departmentId;    // DEPARTMENT_ID
    
    // 조인으로 가져올 추가 정보 (로그인 응답용)
    private String roleName;      // roles.role_name
    private String deptName;      // departments.dept_name
    
    // 권한 정보
    private List<PermissionVO> permissions;
}