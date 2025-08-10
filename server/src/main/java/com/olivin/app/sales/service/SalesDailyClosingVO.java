package com.olivin.app.sales.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SalesDailyClosingVO 클래스 <br>
 * 일일 정산 관련 정보를 담는 Value Object 클래스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.09
 * 수정이력:
 * - 2025.08.09 : 최초 작성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesDailyClosingVO {
    private String compId; // 회사 ID

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date closingDate; // 정산 날짜
    private int totalPrice; // 총 매출액
}
