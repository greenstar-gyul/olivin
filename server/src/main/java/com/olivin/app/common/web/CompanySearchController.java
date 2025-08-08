package com.olivin.app.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.common.service.CompanySearchService;
import com.olivin.app.common.service.CompanySearchService.CompanyType;
import com.olivin.app.common.service.CompanySearchVO;

import lombok.RequiredArgsConstructor;

/**
 * 회사 검색 컨트롤러 클래스 <br>
 * 모달창의 회사 영역에서 사용되는 상품 정보 검색 기능을 제공합니다.
 * 
 * 작성자: 이창현
 * 작성일: 2025.07.30
 * 수정이력:
 * - 2025.07.30 : 최초 작성
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CompanySearchController {
	private final CompanySearchService companySerchService;
	
	// 회사별 검색 조건으로 검색
  @GetMapping("/search/company/{compType}")
  public List<CompanySearchVO> searchCompanys(@PathVariable String compType, @RequestParam(required = false) String searchValue) {
      return companySerchService.searchCompanys(CompanyType.valueOf(compType), searchValue);
  }
}
