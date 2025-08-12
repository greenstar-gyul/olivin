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
public class AccountLederVO {
    // DB 컬럼명과 일치하도록 수정
    private String accountLederId;  // account_leder_id -> accountLederId
    private String increase;
    private String decrease;
    private String balance;
    private String detail;
    private String writeDate;
    
    // 계정 정보
    private String accountId;
    private String accountName;
    
    // 회사 정보
    private String compId;
    private String compName;
    
    // 제품 정보
    private String productId;
    private String productName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date regDate;

    private String updateUser;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateDate;

    private String status;
    private String productImage;
    private String note;
}