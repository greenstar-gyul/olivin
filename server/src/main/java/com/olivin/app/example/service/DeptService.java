package com.olivin.app.example.service;

import java.util.List;

public interface DeptService {
  // 전체 목록
  List<DeptVO> selectAllDepts();
  // 조건 목록
  public List<DeptVO> searchDepts(String searchValue);
}