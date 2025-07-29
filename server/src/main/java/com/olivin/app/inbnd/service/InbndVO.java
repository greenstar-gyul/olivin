package com.olivin.app.inbnd.service;

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
public class InbndVO {
    private String inbndNo; // 입고번호
    private String outbndNo; // 출고번호
    private String outbndFrom; // 출고지
    private String inbndTo; // 입고지
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date inbndDate; // 입고일
    private String inbndSattus; // 입고상태

}
