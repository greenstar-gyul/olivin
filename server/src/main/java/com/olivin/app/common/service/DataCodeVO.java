package com.olivin.app.common.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 데이터 코드 VO 클래스 <br>
 * 데이터 코드와 관련된 정보를 담는 클래스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.09
 * 수정이력:
 * - 2025.08.09 : 최초 작성
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataCodeVO {
    private String codeValue;
    private String codeName;
    private String codeGroup;
    private String note;
}
