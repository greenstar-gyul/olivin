package com.olivin.app.example.mapper;

import java.util.List;

import com.olivin.app.example.service.AccountLederVO;

public interface AccountLederMapper {
  // 전체조회
  List<AccountLederVO> selectAllList();
  }