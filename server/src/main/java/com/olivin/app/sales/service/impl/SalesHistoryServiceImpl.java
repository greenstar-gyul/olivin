package com.olivin.app.sales.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.sales.mapper.SalesHistoryMapper;
import com.olivin.app.sales.service.SalesHistoryService;
import com.olivin.app.sales.service.SalesHistoryVO;

import lombok.RequiredArgsConstructor;

/**
 * SalesHistoryServiceImpl 클래스 <br>
 * 판매 이력 서비스 구현 클래스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.06
 * 수정이력:
 * - 2025.08.06 : 최초 작성
 */

@Service
@RequiredArgsConstructor
public class SalesHistoryServiceImpl implements SalesHistoryService {
    private final SalesHistoryMapper salesHistoryMapper;

    @Override
    public List<SalesHistoryVO> getHistoryAll() {
        return salesHistoryMapper.selectHistoryAll();
    }

    @Override
    public List<SalesHistoryVO> getSalesHistoryList(SalesHistoryVO salesHistoryVO) {
        return salesHistoryMapper.selectSalesHistoryList(salesHistoryVO);
    }

}
