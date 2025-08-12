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
    
    // ROLES 테이블에 실제로 존재하는 컬럼만 사용
    // REG_USER, REG_DATE가 없다면 주석 처리
    // private String regUser;          // REG_USER (등록자) - 테이블에 없으면 주석
    // @DateTimeFormat(pattern = "yyyy-MM-dd")
    // private Date regDate;            // REG_DATE (등록일) - 테이블에 없으면 주석
    
    // 통계용 필드 (쿼리 결과로만 사용)
    private Integer employeeCount;   // 해당 권한을 가진 직원 수
    private Integer permissionCount; // 해당 권한에 할당된 권한 수
    
    // 권한 정보 (조인으로 가져온 데이터)
    private String permissionNames;  // 권한명들을 콤마로 구분한 문자열
    
    // ✅ PERM_ID가 VARCHAR2(300)이므로 String 타입으로 변경
    private List<String> permissionIds; // 권한 ID 목록 (권한 할당/수정용) - String으로 변경
    
    @Override
    public String toString() {
        return "RolesVO{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", roleDesc='" + roleDesc + '\'' +
                ", permissionCount=" + permissionCount +
                ", permissionIds=" + permissionIds +
                '}';
    }
}