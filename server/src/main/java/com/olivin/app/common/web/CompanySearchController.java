package com.olivin.app.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.common.service.CompanySearchService;
import com.olivin.app.common.service.CompanySearchVO;

import lombok.RequiredArgsConstructor;

/**
 * Company Search Controller <br>
 * 회사 검색 기능을 위한 컨트롤러입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CompanySearchController {
    private final CompanySearchService companySearchService;

    @GetMapping("/search/companies/all")
    public List<CompanySearchVO> getAllCompanies() {
        return companySearchService.getAllCompanies();
    }

    @GetMapping("/search/companies")
    public List<CompanySearchVO> getCompanyList(@RequestParam String searchValue) {
        return companySearchService.getCompanyList(searchValue);
    }
}
