package com.olivin.app.example.mapper;

import java.util.List;

import com.olivin.app.example.service.ExampleVO;

public interface ExampleMapper {
    List<ExampleVO> selectAllList();
}
