package com.olivin.app.example.mapper;

import java.util.List;

import com.olivin.app.example.service.DeptVO;

public interface DeptMapper {
  
  List<DeptVO> selectAllList();
}
