// LoginRequestVO.java
package com.olivin.app.auth.service;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequestVO {
    
    @NotBlank(message = "사원번호는 필수입니다")
    private String employeeId;    // 사원번호 (로그인 ID)
    
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;      // 비밀번호
}