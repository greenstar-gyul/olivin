package com.olivin.app.outbnd.service;

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
public class OutbndVO {
    private String outbndNo; // 출고번호
    private String orderId; // 발주ID
    private String orderTitle; // 발주명 
    private String outbndFrom; // 출고지
    private String inbndTo; // 입고지

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date outbndDate; // 출고일
    private String outbndStatus; // 출고상태
}
