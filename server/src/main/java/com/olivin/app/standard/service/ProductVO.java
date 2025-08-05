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
@Alias("ProductVO")
public class ProductVO {
    
    private String productId;
    private String compId;
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
    
    // 조인으로 가져올 직원 이름 필드 추가
    private String regUserName;      // 등록자 이름 (employees.EMP_NAME)
    private String updateUserName;   // 수정자 이름 (employees.EMP_NAME)
    
    @Override
    public String toString() {
        return "ProductVO{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", categoryMain='" + categoryMain + '\'' +
                ", categorySub='" + categorySub + '\'' +
                ", productSpec='" + productSpec + '\'' +
                ", sellPrice=" + sellPrice +
                ", status='" + status + '\'' +
                ", compId='" + compId + '\'' +
                ", regUser='" + regUser + '\'' +
                ", regUserName='" + regUserName + '\'' +
                ", updateUser='" + updateUser + '\'' +
                ", updateUserName='" + updateUserName + '\'' +
                '}';
    }
}