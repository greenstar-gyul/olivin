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
@Alias("EmpVO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmpVO {
    
    private String employeeId;       // EMPLOYEE_ID (PK)
    private String compId;           // COMP_ID (FK)
    private String compName;         // 회사명 (JOIN)
    private String departmentId;     // DEPARTMENT_ID (FK)
    private String deptName;         // 부서명 (JOIN)
    private String empName;          // EMP_NAME
    private String empType;          // EMP_TYPE
    private String email;            // EMAIL
    private String password;         // PASSWORD
    private String phone;            // PHONE
    private String position;         // POSITION
    private Integer roleId;          // ROLE_ID
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date createDate;         // CREATE_DATE
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date hireDate;           // HIRE_DATE
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date resignDate;         // RESIGN_DATE
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date updateDate;         // UPDATE_DATE
    
    private String address;          // ADDRESS
    private String gender;           // GENDER
    private String status;           // STATUS (050001=재직중, 050002=퇴사)
    
    /**
     * 사원이 재직중인지 확인
     * @return STATUS가 050001이면 재직중
     */
    public boolean isActive() {
        return "050001".equals(this.status);
    }
    
    /**
     * 사원이 퇴사했는지 확인
     * @return STATUS가 050002이면 퇴사
     */
    public boolean isResigned() {
        return "050002".equals(this.status);
    }
    
    /**
     * 고용형태명 반환
     * @return 고용형태에 따른 한글명
     */
    public String getEmpTypeName() {
        switch (this.empType) {
            case "정규직":
                return "정규직";
            case "계약직":
                return "계약직";
            case "인턴":
                return "인턴";
            case "파트타임":
                return "파트타임";
            default:
                return this.empType;
        }
    }
    
    /**
     * 성별명 반환
     * @return 성별에 따른 한글명
     */
    public String getGenderName() {
        if (this.gender == null) {
            return "미지정";
        }
        switch (this.gender) {
            case "M":
                return "남성";
            case "F":
                return "여성";
            default:
                return "미지정";
        }
    }
    
    /**
     * 상태명 반환
     * @return 상태에 따른 한글명
     */
    public String getStatusName() {
        if (this.status == null) {
            return "재직중"; // 기본값
        }
        switch (this.status) {
            case "050001":
                return "재직중";
            case "050002":
                return "퇴사";
            default:
                return this.status;
        }
    }
    
    @Override
    public String toString() {
        return "EmpVO{" +
                "employeeId='" + employeeId + '\'' +
                ", compId='" + compId + '\'' +
                ", compName='" + compName + '\'' +
                ", departmentId='" + departmentId + '\'' +
                ", deptName='" + deptName + '\'' +
                ", empName='" + empName + '\'' +
                ", empType='" + empType + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", position='" + position + '\'' +
                ", gender='" + (gender != null ? gender : "null") + '\'' +
                ", status='" + (status != null ? status : "null") + '\'' +
                ", isActive=" + isActive() +
                '}';
    }
}