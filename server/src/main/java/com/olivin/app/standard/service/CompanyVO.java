package com.olivin.app.standard.service;

import java.util.Date;
import org.apache.ibatis.type.Alias;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("CompanyVO")
public class CompanyVO {
    
    private String compId;           // COMP_ID
    private String compName;         // COMP_NAME
    private String compType;         // COMP_TYPE
    private String bizNumber;        // BIZ_NUMBER
    private String ceoName;          // CEO_NAME
    private String settleCycle;      // SETTLE_CYCLE
    private String settleMgr;        // SETTLE_MGR
    private String regUser;          // REG_USER
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date regDate;            // REG_DATE
    
    private String updateUser;       // UPDATE_USER
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateDate;         // UPDATE_DATE
    
    private String address;          // ADDRESS
    private String addressDetail;    // ADDRESS_DETAIL
    private String zipcode;          // ZIPCODE
    private String phone;            // PHONE
    private String note;             // NOTE
    
    @Override
    public String toString() {
        return "CompanyVO{" +
                "compId='" + compId + '\'' +
                ", compName='" + compName + '\'' +
                ", compType='" + compType + '\'' +
                ", bizNumber='" + bizNumber + '\'' +
                ", ceoName='" + ceoName + '\'' +
                ", phone='" + phone + '\'' +
                ", settleCycle='" + settleCycle + '\'' +
                '}';
    }
}