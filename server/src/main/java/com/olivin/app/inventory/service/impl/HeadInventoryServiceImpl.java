package com.olivin.app.inventory.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.inventory.mapper.HeadInventoryMapper;
import com.olivin.app.inventory.service.HeadInventoryService;
import com.olivin.app.inventory.service.HeadInventoryVO;

import lombok.RequiredArgsConstructor;

/**
 * HeadInventoryServiceImpl 클래스 <br>
 * 본사 재고 관리 서비스 구현 클래스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 * 2025.07.31 : 재고 검색 기능 추가
 * 2025.08.07 : 로트 정보 조회 기능 추가 <br>
 */
@Service
@RequiredArgsConstructor
public class HeadInventoryServiceImpl implements HeadInventoryService {

    private final HeadInventoryMapper headInventoryMapper;

    @Override
    public List<HeadInventoryVO> selectAllHeadInventory() {
        return headInventoryMapper.selectAllHeadInventory();
    }

    @Override
    public List<HeadInventoryVO> selectHeadInventoryList(HeadInventoryVO headInventoryVO) {
        return headInventoryMapper.selectHeadInventoryList(headInventoryVO);
    }

    @Override
    public List<HeadInventoryVO> selectHeadInventoryLot(String productId) {
        return headInventoryMapper.selectHeadInventoryLot(productId);
    }
}
