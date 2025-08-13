package com.olivin.app.standard.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.ibatis.type.Alias;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date regDate;            // REG_DATE
    
    private String updateUser;       // UPDATE_USER
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date updateDate;         // UPDATE_DATE
    
    private String address;          // ADDRESS
    private String addressDetail;    // ADDRESS_DETAIL
    private String zipcode;          // ZIPCODE
    private String phone;            // PHONE
    private String note;             // NOTE
    
    // ✅ 프론트엔드에서 문자열로 날짜를 보낼 때를 위한 필드들
    private String regDateStr;       // 등록일 문자열 (YYYY-MM-DD)
    private String updateDateStr;    // 수정일 문자열 (YYYY-MM-DD)
    
    /**
     * 문자열 등록일을 Date 객체로 변환
     */
    public void setRegDateFromString(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                this.regDate = sdf.parse(dateStr.trim());
                this.regDateStr = dateStr.trim();
            } catch (ParseException e) {
                System.err.println("등록일 파싱 오류: " + dateStr + " - " + e.getMessage());
                this.regDate = new Date(); // 현재 날짜로 기본 설정
                this.regDateStr = new SimpleDateFormat("yyyy-MM-dd").format(this.regDate);
            }
        }
    }
    
    /**
     * 문자열 수정일을 Date 객체로 변환
     */
    public void setUpdateDateFromString(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                this.updateDate = sdf.parse(dateStr.trim());
                this.updateDateStr = dateStr.trim();
            } catch (ParseException e) {
                System.err.println("수정일 파싱 오류: " + dateStr + " - " + e.getMessage());
                this.updateDate = new Date(); // 현재 날짜로 기본 설정
                this.updateDateStr = new SimpleDateFormat("yyyy-MM-dd").format(this.updateDate);
            }
        }
    }
    
    /**
     * Date 객체를 문자열로 변환 (getter 오버라이드)
     */
    public String getRegDateStr() {
        if (this.regDateStr != null && !this.regDateStr.isEmpty()) {
            return this.regDateStr;
        }
        if (this.regDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(this.regDate);
        }
        return null;
    }
    
    /**
     * Date 객체를 문자열로 변환 (getter 오버라이드)
     */
    public String getUpdateDateStr() {
        if (this.updateDateStr != null && !this.updateDateStr.isEmpty()) {
            return this.updateDateStr;
        }
        if (this.updateDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(this.updateDate);
        }
        return null;
    }
    
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
        if (this.compType == null) {
            return "알 수 없음";
        }
        
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
    
    /**
     * 필수 필드 검증
     */
    public boolean isValidForSave() {
        return this.compName != null && !this.compName.trim().isEmpty() &&
               this.compType != null && !this.compType.trim().isEmpty() &&
               this.bizNumber != null && !this.bizNumber.trim().isEmpty() &&
               this.ceoName != null && !this.ceoName.trim().isEmpty();
    }
    
    /**
     * 등록/수정 전 데이터 정리
     */
    public void prepareForSave() {
        // 문자열 필드들 trim 처리
        if (this.compName != null) this.compName = this.compName.trim();
        if (this.bizNumber != null) this.bizNumber = this.bizNumber.trim();
        if (this.ceoName != null) this.ceoName = this.ceoName.trim();
        if (this.phone != null) this.phone = this.phone.trim();
        if (this.address != null) this.address = this.address.trim();
        if (this.addressDetail != null) this.addressDetail = this.addressDetail.trim();
        if (this.zipcode != null) this.zipcode = this.zipcode.trim();
        if (this.settleCycle != null) this.settleCycle = this.settleCycle.trim();
        if (this.settleMgr != null) this.settleMgr = this.settleMgr.trim();
        if (this.note != null) this.note = this.note.trim();
        
        // 문자열 날짜가 있으면 Date 객체로 변환
        if (this.regDateStr != null && !this.regDateStr.trim().isEmpty() && this.regDate == null) {
            setRegDateFromString(this.regDateStr);
        }
        if (this.updateDateStr != null && !this.updateDateStr.trim().isEmpty() && this.updateDate == null) {
            setUpdateDateFromString(this.updateDateStr);
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
                ", regDate=" + (regDate != null ? getRegDateStr() : null) +
                ", updateDate=" + (updateDate != null ? getUpdateDateStr() : null) +
                ", isActive=" + isActive() +
                '}';
    }
}