package com.olivin.app.sales.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SalesHistoryVO 클래스 <br>
 * 판매 이력 정보를 담는 Value Object 클래스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.06
 * 수정이력:
 * - 2025.08.06 : 최초 작성
 * - 2025.08.07 : 날짜 필드 및 검색용 날짜 필드 추가
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesHistoryVO {
    private String productId; // 제품 번호
    private String productName; // 제품명
    private String categoryMain; // 대분류
    private String categorySub; // 소분류
    private String compName; // 지점명
    private String productSpec; // 제품 규격
    private int sellPrice; // 판매가
    private int quantity; // 총 판매 수량
    private int totalPrice; // 총 판매 금액

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date soDate; // 판매 주문 날짜

    // 검색용 날짜 필드
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date salesDatesFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date salesDatesTo;
}
