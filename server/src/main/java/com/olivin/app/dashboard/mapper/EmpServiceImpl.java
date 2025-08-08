package com.olivin.app.dashboard.mapper;

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
  
  // 전체 목록 조회
  @Override
  public List<EmpVO> selectAllList() {
    return empMapper.selectAllList();
  }
      
  // 등록
  @Override
  public int insertEmp(EmpVO empVO) {
      empMapper.insertCompanyIfNotExists(empVO);
      return empMapper.insertEmp(empVO);
  }

  // 수정
  @Override
  public int updateEmp(EmpVO empVO) {
      return empMapper.updateEmp(empVO);
  }

  // 삭제
  @Override
  public int deleteEmp(String employeeId) {
      return empMapper.deleteEmp(employeeId);
  }

  @Override
  public List<EmpVO> searchEmps(String searchValue) {
    return empMapper.searchEmps(searchValue);
  }
}