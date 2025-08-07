package com.olivin.app.inbnd.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.inbnd.mapper.InbndMapper;
import com.olivin.app.inbnd.service.InbndProdDVO;
import com.olivin.app.inbnd.service.InbndService;
import com.olivin.app.inbnd.service.InbndVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InbndServiceImpl implements InbndService{
    private final InbndMapper inbndMapper;

    @Override
    public List<InbndVO> getAllInbnd() {
        return inbndMapper.selectAllList();
    } // end of getAllInbnd()

    @Override
    public List<InbndProdDVO> getProdByLot(){
        return inbndMapper.selectProdLot();
    } // end of getProdByLot()

    @Override
    public List<InbndProdDVO> test(String productId){
        return inbndMapper.test(productId);
    }
} // end of class
