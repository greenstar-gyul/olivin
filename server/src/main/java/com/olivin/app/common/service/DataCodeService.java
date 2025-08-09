package com.olivin.app.common.service;

/**
 * 데이터 코드 서비스 인터페이스 <br>
 * 데이터 코드와 관련된 기능을 제공하는 서비스 인터페이스입니다.
 * 데이터 코드를 값으로 변환, 반대로 값을 데이터 코드로 변화하는 서비스를 제공합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.09
 * 수정이력:
 * - 2025.08.09 : 최초 작성
 */
public interface DataCodeService {
    // 데이터 코드 값을 받아서 해당 코드의 이름을 반환합니다.
    public String getCodeName(String codeValue);

    // 데이커 코드의 이름을 받아서 해당 코드의 값을 반환합니다.
    public String getCodeValue(String codeName);
}
