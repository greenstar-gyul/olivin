package com.olivin.app.common.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.common.mapper.CompanySearchMapper;
import com.olivin.app.common.service.CompanySearchService;
import com.olivin.app.common.service.CompanySearchVO;

import lombok.RequiredArgsConstructor;

/**
 * Company Search Service 구현 클래스 <br>
 * 회사 검색 기능을 위한 서비스 구현 클래스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
@Service
@RequiredArgsConstructor
public class CompanySearchServiceImpl implements CompanySearchService {
    private final CompanySearchMapper companySearchMapper;

    @Override
    public List<CompanySearchVO> getAllCompanies() {
        return companySearchMapper.selectAllCompanies();
    }

    @Override
    public List<CompanySearchVO> getCompanyList(String searchValue) {
        return companySearchMapper.selectCompanyList(searchValue);
    }
}
