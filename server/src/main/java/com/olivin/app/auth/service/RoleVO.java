// RoleVO.java (실제 테이블 구조 반영)
package com.olivin.app.auth.service;

import lombok.Data;
import java.util.List;

@Data
public class RoleVO {
    private Long roleId;        // ROLE_ID
    private String roleName;    // ROLE_NAME
    private String field;       // FIELD
    
    // 역할에 속한 권한들
    private List<PermissionVO> permissions;
}