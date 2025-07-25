package com.olivin.app.example.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExampleVO {
    private String productId;
    private String productName;
    private String categoryMain;
    private String categorySub;
    private String vendorName;
    private String productSpec;
    private String unit;
    private Integer packQty;
    private Integer safetyStock;
    private Double purchasePrice;
    private Double sellPrice;
    private String regUser;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date regDate;

    private String updateUser;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateDate;

    private String status;
    private String productImage;
    private String note;
}
