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
}