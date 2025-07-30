package com.olivin.app.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.common.service.VendorSearchService;
import com.olivin.app.common.service.VendorSearchVO;

import lombok.RequiredArgsConstructor;

/**
 * Vendor Search Controller 클래스 <br>
 * 벤더 검색 API 엔드포인트를 정의합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VendorSearchController {
    private final VendorSearchService vendorSearchService;

    @GetMapping("/search/vendors/all")
    public List<VendorSearchVO> getAllVendors() {
        return vendorSearchService.getAllVendors();
    }

    @GetMapping("/search/vendors")
    public List<VendorSearchVO> getVendorList(@RequestParam String searchValue) {
        return vendorSearchService.getVendorList(searchValue);
    }
}
