package com.olivin.app.example.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.example.mapper.ExampleMapper;
import com.olivin.app.example.service.ExampleService;
import com.olivin.app.example.service.ExampleVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExampleServiceImpl implements ExampleService {

    private final ExampleMapper exampleMapper;

    @Override
    public List<ExampleVO> getAllExamples() {
        return exampleMapper.selectAllList();
    }
    
}
