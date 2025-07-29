package com.olivin.app.example.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.example.service.ExampleService;
import com.olivin.app.example.service.ExampleVO;

import lombok.RequiredArgsConstructor;

/**
 * Example Controller 클래스 <br>
 * 예제 컨트롤러 클래스입니다. <br>
 * <br>
 * 작성자: 함동의 <br>
 * 작성일: 2025.07.24 <br>
 * 수정이력: <br>
 * 2025.07.24 : 최초 작성 <br>
 * 2025.07.29 : 설명 추가 <br>
 */
@RestController // REST API 컨트롤러임을 명시합니다. 꼭! 추가해야 합니다.
// @RequestMapping("/example") // API 경로의 기본 경로를 설정합니다. 지정하면 모든 메소드에 적용됩니다.
@RequiredArgsConstructor
public class ExampleController {
    private final ExampleService exampleService;

    // 예제 데이터를 조회하는 API 엔드포인트입니다.
    // 이 메소드는 "/test" 경로로 온 GET 요청을 처리합니다.
    @GetMapping("/test")
    public List<ExampleVO> exampleList() {
        return exampleService.getAllExamples();
    }
    
}
