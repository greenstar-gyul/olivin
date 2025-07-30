package com.olivin.app.common.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.common.mapper.VendorSearchMapper;
import com.olivin.app.common.service.VendorSearchService;
import com.olivin.app.common.service.VendorSearchVO;

import lombok.RequiredArgsConstructor;

/**
 * Vendor Search Service 구현 클래스 <br>
 * 벤더 검색 기능을 위한 서비스 구현체입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
@Service
@RequiredArgsConstructor
public class VendorSearchServiceImpl implements VendorSearchService {
    private final VendorSearchMapper vendorSearchMapper;

    @Override
    public List<VendorSearchVO> getAllVendors() {
        return vendorSearchMapper.selectAllVendors();
    }

    @Override
    public List<VendorSearchVO> getVendorList(String searchValue) {
        return vendorSearchMapper.selectVendorList(searchValue);
    }

}
