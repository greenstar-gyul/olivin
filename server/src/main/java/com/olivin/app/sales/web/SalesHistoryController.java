package com.olivin.app.sales.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.sales.service.SalesHistoryService;
import com.olivin.app.sales.service.SalesHistoryVO;

import lombok.RequiredArgsConstructor;

/**
 * SalesHistoryController 클래스 <br>
 * 판매 이력 관련 API 엔드포인트를 정의하는 컨트롤러 클래스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.07
 * 수정이력:
 * - 2025.08.07 : 최초 작성
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SalesHistoryController {
    private final SalesHistoryService salesHistoryService;

    // 판매 이력 전체 조회 API 엔드포인트
    @GetMapping("/sales/history/all")
    public List<SalesHistoryVO> getAllSalesHistory() {
        return salesHistoryService.getHistoryAll();
    }

    // 판매 이력 검색 API 엔드포인트
    @GetMapping("/sales/history/search")
    public List<SalesHistoryVO> searchSalesHistory(
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "categorySub", required = false) String categorySub,
            @RequestParam(value = "salesDatesFrom", required = false) String salesDatesFrom,
            @RequestParam(value = "salesDatesTo", required = false) String salesDatesTo,
            @RequestParam(value = "compName", required = false) String compName) {
        
        // 날짜 변환을 위한 SimpleDateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFrom = null;
        Date dateTo = null;
        
        try {
            if (salesDatesFrom != null && !salesDatesFrom.isEmpty()) {
                dateFrom = dateFormat.parse(salesDatesFrom);
            }
            if (salesDatesTo != null && !salesDatesTo.isEmpty()) {
                dateTo = dateFormat.parse(salesDatesTo);
            }
        } catch (ParseException e) {
            // 날짜 파싱 오류 시 로그 출력 (실제로는 로거 사용 권장)
            System.err.println("Date parsing error: " + e.getMessage());
        }
        
        // VO 객체 생성 및 파라미터 설정
        SalesHistoryVO salesHistoryVO = SalesHistoryVO.builder()
                .productName(productName)
                .categorySub(categorySub)
                .compName(compName)
                .salesDatesFrom(dateFrom)
                .salesDatesTo(dateTo)
                .build();
        
        return salesHistoryService.getSalesHistoryList(salesHistoryVO);
    }
}
