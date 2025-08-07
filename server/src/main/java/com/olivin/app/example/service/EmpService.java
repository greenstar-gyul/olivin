package com.olivin.app.example.service;

import java.util.List;

public interface EmpService {
  // 전체목록
  List<EmpVO> selectAllList();
  // 조건 목록
  public List<EmpVO> searchEmps(String searchValue);
  // 등록
  public int insertEmp(EmpVO empVO);
  // 수정
  public int updateEmp(EmpVO empVO);
  // 삭제
  public int deleteEmp(String employeeId);
}
