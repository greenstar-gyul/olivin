package com.olivin.app.outbnd.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class outbndProdVO {
    private String outbndProductNo; // 출고제품번호
    private String outbndNo; // 출고번호
    private String productId; // 제품ID
    private String productName; // 제품명
    private Integer orderQuantity; // 발주수량
    private Integer totalOutbndQuantity; // 총출고수량
    private String unit; // 단위
    private String outbndStatus; // 출고상태
    private List<outbndProdDVO> outbndProdDList; // 출고제품 상세 리스트
}
