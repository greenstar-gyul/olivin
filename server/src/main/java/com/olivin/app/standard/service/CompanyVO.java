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
    private String compType;         // COMP_TYPE (100001=본사, 100002=지점, 100003=공급업체, FFFFFF=비활성)
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
    
    /**
     * 회사가 활성 상태인지 확인
     * @return COMP_TYPE이 FFFFFF가 아니면 활성 상태
     */
    public boolean isActive() {
        return !"FFFFFF".equals(this.compType);
    }
    
    /**
     * 회사가 비활성 상태인지 확인
     * @return COMP_TYPE이 FFFFFF이면 비활성 상태
     */
    public boolean isInactive() {
        return "FFFFFF".equals(this.compType);
    }
    
    /**
     * 회사 유형명 반환
     * @return 회사 유형에 따른 한글명
     */
    public String getCompTypeName() {
        switch (this.compType) {
            case "100001":
                return "본사";
            case "100002":
                return "지점";
            case "100003":
                return "공급업체";
            case "FFFFFF":
                return "비활성";
            default:
                return "알 수 없음";
        }
    }
    
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
                ", isActive=" + isActive() +
                '}';
    }
}