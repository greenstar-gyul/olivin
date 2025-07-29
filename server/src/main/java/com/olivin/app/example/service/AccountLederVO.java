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
    private String accountlederId;
    private String increase;
    private String decrease;
    private String balance;
    private String detail;
    private String writeDate;
    private String compId;
    private String productId;
    private String accountId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date regDate;

    private String updateUser;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateDate;

    private String status;
    private String productImage;
    private String note;
}
