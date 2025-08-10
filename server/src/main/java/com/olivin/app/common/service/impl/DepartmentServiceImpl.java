package com.olivin.app.common.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.example.mapper.DeptMapper;
import com.olivin.app.example.service.DeptService;
import com.olivin.app.example.service.DeptVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DeptService{

  private final DeptMapper deptMapper;

  @Override
  public List<DeptVO> selectAllDepts() {
    return deptMapper.selectAllList();
  }

  // 등록
  @Override
  public int insertDept(DeptVO deptVO) {
      deptMapper.insertDeptIfNotExists(deptVO);
      return deptMapper.insertDept(deptVO);
  }

  // 수정
  @Override
  public int updateDept(DeptVO deptVO) {
      return deptMapper.updateDept(deptVO);
  }

  // 삭제
  @Override
  public int deleteDept(String departmentId) {
      return deptMapper.deleteDept(departmentId);
  }

  @Override
  public List<DeptVO> searchDepts(String searchValue) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'searchDepts'");
  }
  
}
