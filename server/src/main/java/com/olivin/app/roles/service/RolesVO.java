package com.olivin.app.roles.service;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.type.Alias;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("RolesVO")
public class RolesVO {
    
    private Integer roleId;          // ROLE_ID
    private String roleName;         // ROLE_NAME
    private String roleDesc;         // ROLE_DESC
    private String regUser;          // REG_USER (등록자)
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date regDate;            // REG_DATE (등록일)
    
    private String updateUser;       // UPDATE_USER (수정자)
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateDate;         // UPDATE_DATE (수정일)
    
    // 통계용 필드
    private Integer employeeCount;   // 해당 권한을 가진 직원 수
    private Integer permissionCount; // 해당 권한에 할당된 권한 수
    
    // 권한 정보 (조인으로 가져온 데이터)
    private String permissionNames;  // 권한명들을 콤마로 구분한 문자열 (예: "조회권한,수정권한,삭제권한")
    private List<Integer> permissionIds; // 권한 ID 목록 (권한 할당/수정용)
    
    @Override
    public String toString() {
        return "RoleVO{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", roleDesc='" + roleDesc + '\'' +
                ", permissionCount=" + permissionCount +
                '}';
    }
}