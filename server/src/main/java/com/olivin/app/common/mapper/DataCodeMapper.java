package com.olivin.app.common.mapper;

/**
 * 데이터 코드 매퍼 인터페이스 <br>
 * 데이터 코드와 관련된 데이터베이스 작업을 수행하는 매퍼 인터페이스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.09
 * 수정이력:
 * - 2025.08.09 : 최초 작성
 */
public interface DataCodeMapper {
    // 데이터 코드 값을 받아서 해당 코드의 이름을 반환합니다.
    public String getCodeName(String codeValue);

    // 데이터 코드의 이름을 받아서 해당 코드의 값을 반환합니다.
    public String getCodeValue(String codeName);
}
