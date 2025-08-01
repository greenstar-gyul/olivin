package com.olivin.app.inventory.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.inventory.mapper.BranchInventoryMapper;
import com.olivin.app.inventory.service.BranchInventoryService;
import com.olivin.app.inventory.service.BranchInventoryVO;

import lombok.RequiredArgsConstructor;

/**
 * BranchInventoryServiceImpl 클래스 <br>
 * 지점 재고 관리 서비스 구현 클래스입니다. 
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.31
 * 수정이력:
 * 2025.07.31 : 최초 작성
 */
@Service
@RequiredArgsConstructor
public class BranchInventoryServiceImpl implements BranchInventoryService {
    private final BranchInventoryMapper branchInventoryMapper;

    @Override
    public List<BranchInventoryVO> selectAllBranchInventory() {
        return branchInventoryMapper.selectAllBranchInventory();
    }

    @Override
    public List<BranchInventoryVO> selectBranchInventoryList(BranchInventoryVO branchInventoryVO) {
        return branchInventoryMapper.selectBranchInventoryList(branchInventoryVO);
    }

}
