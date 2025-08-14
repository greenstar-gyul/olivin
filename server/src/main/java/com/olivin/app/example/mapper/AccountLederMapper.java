package com.olivin.app.example.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.olivin.app.example.service.AccountLederVO;

@Mapper
public interface AccountLederMapper {
    
    /**
     * 전체 거래처원장 조회
     * @return 거래처원장 목록
     */
    List<AccountLederVO> selectAllList();
    
    /**
     * 조건에 따른 거래처원장 조회
     * @param params 검색 조건
     * @return 거래처원장 목록
     */
    List<AccountLederVO> selectByCondition(Map<String, Object> params);
    
    // ------------------ 모달 ------------------

    /**
     * accounts 테이블에서 계정 목록만 조회 (모달창용)
     * @param params 검색 조건
     * @return 계정 목록
     */
    List<AccountLederVO> selectAccountsOnly(Map<String, Object> params);
}