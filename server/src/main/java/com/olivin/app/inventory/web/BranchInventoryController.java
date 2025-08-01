package com.olivin.app.inventory.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.inventory.service.BranchInventoryService;
import com.olivin.app.inventory.service.BranchInventoryVO;

import lombok.RequiredArgsConstructor;

/**
 * BranchInventoryController 클래스 <br>
 * 지점 재고 관리 컨트롤러입니다. 
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.31
 * 수정이력:
 * 2025.07.31 : 최초 작성
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BranchInventoryController {
    private final BranchInventoryService branchInventoryService;

    @GetMapping("/inventory/branchStock/all")
    public List<BranchInventoryVO> getAllBranchInventory() {
        return branchInventoryService.selectAllBranchInventory();
    }

    @GetMapping("/inventory/branchStock/search")
    public List<BranchInventoryVO> searchBranchInventory(BranchInventoryVO branchInventoryVO) {
        return branchInventoryService.selectBranchInventoryList(branchInventoryVO);
    }
}
