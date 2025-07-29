package com.olivin.app.inventory.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
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
