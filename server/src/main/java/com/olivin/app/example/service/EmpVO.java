package com.olivin.app.example.service;

import java.util.Date;

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
@Alias("EmpVO")
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
    private Date createDate;         // CREATE_DATE
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date hireDate;           // HIRE_DATE
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date resignDate;         // RESIGN_DATE
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateDate;         // UPDATE_DATE
    
    private String address;          // ADDRESS
    private String gender;           // GENDER
    private String status;           // STATUS (ACTIVE/INACTIVE)
    
    /**
     * 사원이 활성 상태인지 확인
     * @return STATUS가 ACTIVE이면 활성 상태
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
    
    /**
     * 사원이 비활성 상태인지 확인
     * @return STATUS가 INACTIVE이면 비활성 상태
     */
    public boolean isInactive() {
        return "INACTIVE".equals(this.status);
    }
    
    /**
     * 고용형태명 반환
     * @return 고용형태에 따른 한글명
     */
    public String getEmpTypeName() {
        switch (this.empType) {
            case "FULL_TIME":
                return "정규직";
            case "PART_TIME":
                return "비정규직";
            case "CONTRACT":
                return "계약직";
            case "INTERN":
                return "인턴";
            default:
                return this.empType;
        }
    }
    
    /**
     * 성별명 반환
     * @return 성별에 따른 한글명
     */
    public String getGenderName() {
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
        switch (this.status) {
            case "ACTIVE":
                return "재직";
            case "INACTIVE":
                return "퇴사";
            case "LEAVE":
                return "휴직";
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
                ", status='" + status + '\'' +
                ", isActive=" + isActive() +
                '}';
    }
}