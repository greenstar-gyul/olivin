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
  private String departmentId; // 회사 ID
  private String departmentName; // 회사명
}