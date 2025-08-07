package com.olivin.app.sales.service;

import java.util.List;

/**
 * SalesHistoryService 인터페이스 <br>
 * 판매 이력 관련 서비스 인터페이스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.06
 * 수정이력:
 * - 2025.08.06 : 최초 작성
 */
public interface SalesHistoryService {
    /**
     * 판매 이력 전체 조회
     */
    List<SalesHistoryVO> getHistoryAll();

    /**
     * 판매 이력 검색
     * @param salesHistoryVO 검색 조건을 담은 VO 객체
     * @return 검색된 판매 이력 목록
     */
    List<SalesHistoryVO> getSalesHistoryList(SalesHistoryVO salesHistoryVO);
}
