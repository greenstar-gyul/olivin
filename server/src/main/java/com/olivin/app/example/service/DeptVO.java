package com.olivin.app.example.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeptVO {
  private String compId;// 회사 ID
  private String compName;// 회사명
  private String departmentId; // 부서 ID
  private String deptName; // 부서명
  private String position; // 직급
  private String phone; // 연락처
  
  private String employeeId; //사원코드
  private String empName; //사원이름
  private String empType; //고용형태
  private String email; //이메일
  private String password; //비밀번호
}