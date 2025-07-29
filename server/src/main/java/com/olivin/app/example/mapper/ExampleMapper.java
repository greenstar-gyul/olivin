package com.olivin.app.example.mapper;

import java.util.List;

import com.olivin.app.example.service.ExampleVO;

/**
 * Example Mapper 인터페이스 <br>
 * 데이터베이스와 연동하는 예제 Mapper 인터페이스입니다. <br>
 * <br>
 * 작성자: 함동의 <br>
 * 작성일: 2025.07.24 <br>
 * 수정이력: <br>
 * 2025.07.24 : 최초 작성 <br>
 * 2025.07.29 : 설명 추가 <br>
 */
public interface ExampleMapper {
    // 모든 목록을 조회합니다.
    List<ExampleVO> selectAllList();
}
