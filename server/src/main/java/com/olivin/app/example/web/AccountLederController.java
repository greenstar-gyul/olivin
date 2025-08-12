package com.olivin.app.example.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.example.service.AccountLederService;
import com.olivin.app.example.service.AccountLederVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Slf4j
public class AccountLederController {
    
    private final AccountLederService accountLederService;

    @GetMapping("/account")
    public ResponseEntity<List<AccountLederVO>> getAccountList(
            @RequestParam(required = false) Map<String, Object> params
    ) {
        try {
            log.info("거래처원장 조회 요청 - 파라미터: {}", params);
            
            List<AccountLederVO> accountList;
            
            // 검색 조건이 있는 경우와 없는 경우를 구분
            if (params != null && !params.isEmpty()) {
                accountList = accountLederService.getAccountsByCondition(params);
            } else {
                accountList = accountLederService.getAllAccounts();
            }
            
            log.info("거래처원장 조회 결과: {} 건", accountList.size());
            return ResponseEntity.ok(accountList);
            
        } catch (Exception e) {
            log.error("거래처원장 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}