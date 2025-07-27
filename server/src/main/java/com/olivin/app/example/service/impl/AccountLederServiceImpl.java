package com.olivin.app.example.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.example.mapper.AccountLederMapper;
import com.olivin.app.example.service.AccountLederService;
import com.olivin.app.example.service.AccountLederVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountLederServiceImpl implements AccountLederService{
  
  private final AccountLederMapper accountLederMapper;

  @Override
  public List<AccountLederVO> getAllAccounts() {
    return accountLederMapper.selectAllList();
  }
}
