package com.olivin.app.sales.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.sales.mapper.SalesHistoryMapper;
import com.olivin.app.sales.service.SalesHistoryService;
import com.olivin.app.sales.service.SalesHistoryVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalesHistoryServiceImpl implements SalesHistoryService {
    private final SalesHistoryMapper salesHistoryMapper;

    @Override
    public List<SalesHistoryVO> getHistoryAll() {
        return salesHistoryMapper.selectHistoryAll();
    }

}
