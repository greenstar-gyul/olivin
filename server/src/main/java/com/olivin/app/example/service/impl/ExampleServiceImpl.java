package com.olivin.app.example.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.example.mapper.ExampleMapper;
import com.olivin.app.example.service.ExampleService;
import com.olivin.app.example.service.ExampleVO;

import lombok.RequiredArgsConstructor;

/**
 * Example Service 구현 클래스 <br>
 * 예제 서비스 구현 클래스입니다. <br>
 * <br>
 * 작성자: 함동의 <br>
 * 작성일: 2025.07.24 <br>
 * 수정이력: <br>
 * 2025.07.24 : 최초 작성 <br>
 * 2025.07.29 : 설명 추가 <br>
 */
@Service // 서비스 클래스임을 명시합니다. 꼭! 추가해야 합니다.
@RequiredArgsConstructor
public class ExampleServiceImpl implements ExampleService {
    private final ExampleMapper exampleMapper;

    // 모든 목록을 조회합니다.
    @Override
    public List<ExampleVO> getAllExamples() {
        return exampleMapper.selectAllList();
    }
    
}
