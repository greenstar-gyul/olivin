package com.olivin.app.common.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.example.mapper.EmpMapper;
import com.olivin.app.example.service.EmpService;
import com.olivin.app.example.service.EmpVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpServiceImpl implements EmpService{
  
  private final EmpMapper empMapper;

  @Override
  public List<EmpVO> selectAllList() {
    return empMapper.selectAllList();
  }

  @Override
  public List<EmpVO> searchEmps(String searchValue) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'searchEmps'");
  }

  // @Override
  // public void addEmp(EmpVO) {
  //   empMapper.insertEmp(EmpVO);
  // }
}
