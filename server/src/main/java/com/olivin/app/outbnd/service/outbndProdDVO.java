package com.olivin.app.outbnd.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class outbndProdDVO {
    private String outbndProductDNo; // 출고제품상세번호
    private String outbndProductNo; // 출고제품번호
    private String lotNo; // LOT번호
    private String outbndQuantity; // 출고수량
    private String unit; // 단위
    private String outbndStatus; // 출고상태
}
