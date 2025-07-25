package com.olivin.app.example.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.example.service.ExampleService;
import com.olivin.app.example.service.ExampleVO;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class ExampleController {
    private final ExampleService exampleService;

    @GetMapping("/test")
    public List<ExampleVO> exampleList() {
        return exampleService.getAllExamples();
    }
    
}
