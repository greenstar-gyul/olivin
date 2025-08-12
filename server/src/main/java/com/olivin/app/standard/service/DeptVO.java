package com.olivin.app.standard.service;

import java.util.Date;

import org.apache.ibatis.type.Alias;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("DeptVO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeptVO {
    
    private String departmentId;     // DEPARTMENT_ID (PK) - 실제 테이블 컬럼명
    private String deptName;         // DEPT_NAME
    
    // 감사 추적용 필드들 (필요시)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date createDate;         // CREATE_DATE
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date updateDate;         // UPDATE_DATE
    
    private String createUser;       // CREATE_USER
    private String updateUser;       // UPDATE_USER
    
    // 상태 관리 (필요시)
    private String status;           // STATUS (활성/비활성)
    
    /**
     * 부서가 활성 상태인지 확인
     * @return STATUS가 활성이면 true
     */
    public boolean isActive() {
        return status == null || "Y".equals(status) || "ACTIVE".equals(status);
    }
    
    @Override
    public String toString() {
        return "DeptVO{" +
                "departmentId='" + departmentId + '\'' +
                ", deptName='" + deptName + '\'' +
                ", status='" + (status != null ? status : "null") + '\'' +
                ", isActive=" + isActive() +
                '}';
    }
}