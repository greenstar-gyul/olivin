package com.olivin.app.common.service.impl;

import org.springframework.stereotype.Service;

import com.olivin.app.common.mapper.DataCodeMapper;

import lombok.RequiredArgsConstructor;

/**
 * 데이터 코드 서비스 구현 클래스 <br>
 * 데이터 코드와 관련된 기능을 제공하는 서비스 구현 클래스입니다.
 * 데이터 코드를 값으로 변환, 반대로 값을 데이터 코드로 변화하는 서비스를 제공합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.08.09
 * 수정이력:
 * - 2025.08.09 : 최초 작성
 */
@Service
@RequiredArgsConstructor
public class DataCodeServiceImpl {
    private final DataCodeMapper dataCodeMapper;

    /**
     * 데이터 코드 값을 받아서 해당 코드의 이름을 반환합니다.
     * 
     * @param codeValue 데이터 코드 값
     * @return 해당 코드의 이름
     */
    public String getCodeName(String codeValue) {
        return dataCodeMapper.getCodeName(codeValue);
    }

    /**
     * 데이터 코드의 이름을 받아서 해당 코드의 값을 반환합니다.
     * 
     * @param codeName 데이터 코드 이름
     * @return 해당 코드의 값
     */
    public String getCodeValue(String codeName) {
        return dataCodeMapper.getCodeValue(codeName);
    }
}
