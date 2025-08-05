package com.olivin.app.example.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.olivin.app.example.service.EmpVO;

@Mapper
public interface EmpMapper {
  List<EmpVO> selectAllList();
  List<EmpVO> selectAllList(EmpVO empVO);
  EmpVO selectEmpVO(String empId);
  int insertEmp(EmpVO empId);
  int updateEmp(EmpVO empVO);
  int deleteEmp(String empId);
  List<EmpVO> selectEmpsByCondition(Map<String, Object> params);
  int checkEmpId(String empId);

  // 사원코드 자동생성
  String selectEmp(@Param("empId") String empId);
}
