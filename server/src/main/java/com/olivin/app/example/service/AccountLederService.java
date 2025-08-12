package com.olivin.app.example.service;

import java.util.List;
import java.util.Map;

public interface AccountLederService {
    
    /**
     * 모든 거래처원장 조회
     * @return 거래처원장 목록
     */
    List<AccountLederVO> getAllAccounts();
    
    /**
     * 검색 조건에 따른 거래처원장 조회
     * @param params 검색 조건
     * @return 거래처원장 목록
     */
    List<AccountLederVO> getAccountsByCondition(Map<String, Object> params);
}