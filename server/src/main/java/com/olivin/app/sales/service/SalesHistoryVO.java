package com.olivin.app.sales.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesHistoryVO {
    private String productId;
    private String productName;
    private String categoryMain;
    private String categorySub;
    private String compName;
    private String vendorName;
    private String productSpec;
    private int stockQuantity;
    private int safetyStock;
}
