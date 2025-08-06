package com.olivin.app.inbnd.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InbndProdVO {
    private String inbndProductNo; // 입고제품번호
    private String inbndNo; // 입고번호
    private String productId; // 제품ID
    private String productName; // 제품명
    private Integer orderQuantity; // 발주수량
    private Integer totalInbndQuantity; // 총입고수량
    private String unit; // 단위
    private String inbndStatus; // 입고상태
    private List<InbndProdDVO> inbndProdDList; // 출고제품 상세 리스트
}
