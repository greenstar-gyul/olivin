package com.olivin.app.example.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.olivin.app.example.mapper.AccountLederMapper;
import com.olivin.app.example.service.AccountLederService;
import com.olivin.app.example.service.AccountLederVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLederServiceImpl implements AccountLederService {
    
    private final AccountLederMapper accountLederMapper;

    @Override
    public List<AccountLederVO> getAllAccounts() {
        try {
            log.debug("모든 거래처원장 조회 시작");
            List<AccountLederVO> result = accountLederMapper.selectAllList();
            log.debug("거래처원장 조회 완료: {} 건", result.size());
            return result;
        } catch (Exception e) {
            log.error("거래처원장 조회 중 오류 발생", e);
            throw new RuntimeException("거래처원장 조회에 실패했습니다.", e);
        }
    }

    @Override
    public List<AccountLederVO> getAccountsByCondition(Map<String, Object> params) {
        try {
            log.debug("조건부 거래처원장 조회 시작 - 조건: {}", params);
            List<AccountLederVO> result = accountLederMapper.selectByCondition(params);
            log.debug("조건부 거래처원장 조회 완료: {} 건", result.size());
            return result;
        } catch (Exception e) {
            log.error("조건부 거래처원장 조회 중 오류 발생", e);
            throw new RuntimeException("거래처원장 조회에 실패했습니다.", e);
        }
    }
}