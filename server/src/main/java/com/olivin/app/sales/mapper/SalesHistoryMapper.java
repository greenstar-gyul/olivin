package com.olivin.app.sales.mapper;

import java.util.List;

import com.olivin.app.sales.service.SalesHistoryVO;

/**
 * SalesHistoryMapper 인터페이스 <br>
 * 판매 이력 관련 데이터베이스 작업을 위한 매퍼 인터페이스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.06
 * 수정이력:
 * - 2025.08.06 : 최초 작성
 */
public interface SalesHistoryMapper {
    List<SalesHistoryVO> selectHistoryAll();
}
