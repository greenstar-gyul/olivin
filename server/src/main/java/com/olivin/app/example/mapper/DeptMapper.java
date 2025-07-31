package com.olivin.app.example.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.olivin.app.example.service.DeptVO;

@Mapper
public interface DeptMapper {

  List<DeptVO> selectAllList();
  List<DeptVO> selectAllList(DeptVO deptVO);
  DeptVO selectDeptVO(String deptId);

  int insertDept(DeptVO deptId);
  int updateDept(DeptVO deptVO);
  int deleteDept(String deptId);

  List<DeptVO> selecDeptsByCondition(Map<String, Object> params);
  int checkDeptId(String deptId);

  // 부서 코드 자동생성
  // String selectLastDept(@Param("deptId") String DeptId);

  
}
