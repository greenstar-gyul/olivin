package com.olivin.app.inbnd.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InbndProdDVO {
    private String inbndProductDNo; // 입고제품상세번호
    private String inbndProductNo; // 입고제품번호
    private String lotNo; // LOT번호
    private String inbndQuantity; // 입고수량
    private String remainQuantity; // 입고수량
    private String unitCode; // 단위코드
    private String unit; // 단위
    private String inbndStatusCode; // 입고상태코드
    private String inbndStatus; // 입고상태

    /* 필요한가? */
    private String productId; // 제품 ID
    private String productName; // 제품명
}
