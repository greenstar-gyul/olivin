package com.olivin.app.example.service;

import java.util.List;

public interface DeptService {
  // 전체 목록
  List<DeptVO> selectAllDepts();
  // 조건 목록
  public List<DeptVO> searchDepts(String searchValue);

  // 등록
  public int insertDept(DeptVO deptVO);
  // 수정
  public int updateDept(DeptVO deptVO);
  // 삭제
  public int deleteDept(String departmentId);
}