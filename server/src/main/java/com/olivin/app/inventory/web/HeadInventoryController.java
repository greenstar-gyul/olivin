package com.olivin.app.inventory.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.inventory.service.HeadInventoryService;
import com.olivin.app.inventory.service.HeadInventoryVO;

import lombok.RequiredArgsConstructor;

/**
 * HeadInventory Controller 클래스 <br>
 * 본사 재고 관리 API 엔드포인트를 정의합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.25
 * 수정이력:
 * 2025.07.25 : 최초 작성
 * 2025.07.30 : 주석 추가, 구현
 * 2025.07.31 : API 경로 매핑 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HeadInventoryController {
    private final HeadInventoryService headInventoryService;

    // This is a sample endpoint to demonstrate how to use the InventoryService
    @GetMapping("/inventory/headStock/all")
    public List<HeadInventoryVO> getAllHeadInventory() {
        return headInventoryService.selectAllHeadInventory();
    }

    // This endpoint retrieves head inventory based on search criteria
    @GetMapping("/inventory/headStock/search")
    public List<HeadInventoryVO> searchHeadInventory(HeadInventoryVO headInventoryVO) {
        return headInventoryService.selectHeadInventoryList(headInventoryVO);
    }

    // This endpoint retrieves lot information for a specific product
    @GetMapping("/inventory/headStock/lot/{productId}")
    public List<HeadInventoryVO> getHeadInventoryLot(@PathVariable String productId) {
        return headInventoryService.selectHeadInventoryLot(productId);
    }
}
