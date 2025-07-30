package com.olivin.app.inventory.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;

/**
 * Inventory Controller 클래스 <br>
 * 재고 관리 API 엔드포인트를 정의합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.25
 * 수정이력:
 * 2025.07.25 : 최초 작성
 * 2025.07.30 : 주석 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InventoryController {
    // private final InventoryService inventoryService;

    // This is a sample endpoint to demonstrate how to use the InventoryService
    @GetMapping("/inventory")
    public Map<String, String> getInventory() {
        Map<String, String> inventorySample = new HashMap<String, String>();
        inventorySample.put("message", "Inventory service is working!");
        inventorySample.put("status", "success");
        
        return inventorySample;
    }
}
