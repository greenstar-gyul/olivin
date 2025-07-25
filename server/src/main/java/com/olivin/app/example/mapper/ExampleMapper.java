package com.olivin.app.example.mapper;

import java.util.List;

import com.olivin.app.example.service.ExampleVO;

public interface ExampleMapper {
    // 전체 조회
    List<ExampleVO> selectAllList();
}
