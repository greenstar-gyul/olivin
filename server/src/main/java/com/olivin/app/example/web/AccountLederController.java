package com.olivin.app.example.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.example.service.AccountLederService;
import com.olivin.app.example.service.AccountLederVO;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class AccountLederController {
  private final AccountLederService accountlederService;

  @GetMapping("/account")
  public List<AccountLederVO> AccountList() {
    return accountlederService.getAllAccounts();
  }
}
