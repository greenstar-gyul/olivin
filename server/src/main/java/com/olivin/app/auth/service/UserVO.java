// UserVO.java (로그인 전용)
package com.olivin.app.auth.service;

import java.util.List;

import lombok.Data;

@Data
public class UserVO {
    // EMPLOYEES 테이블 - 로그인에 필요한 필드들만
    private String employeeId;    // EMPLOYEE_ID (로그인 ID)
    private String compId;        // COMP_ID (지점 구분)
    private String compName;      // COMP_NAME (지점 이름, 조인으로 가져옴)
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
    private String compType;      // companys.comp_type (본사/지점/공급업체 구분)
    
    // 권한 정보
    private List<PermissionVO> permissions;
}