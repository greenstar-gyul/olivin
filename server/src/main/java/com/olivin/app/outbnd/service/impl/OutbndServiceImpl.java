package com.olivin.app.outbnd.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.outbnd.mapper.OutbndMapper;
import com.olivin.app.outbnd.service.OutbndService;
import com.olivin.app.outbnd.service.OutbndVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutbndServiceImpl implements OutbndService {
    private final OutbndMapper outbndMapper;

    @Override
    public List<OutbndVO> getAllOutbnd() {
        return outbndMapper.selectAllList();
    } // end of getAllOutbnd()

    @Override
    public String getCodeForUpdate() {
        return outbndMapper.selectCodeForUpdate();
    } // end of getCodeForUpdate()

    /**
     * 본사 출고처리 프로시저 호출
     * param_orderId_발주 ID
     */
    @Override
    public void processHqOutbnd(String orderId) {
        outbndMapper.callProcHqOutbndProcess(orderId);
    } // end of processHqOutbnd
} // end of class
