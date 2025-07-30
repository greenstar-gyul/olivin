package com.olivin.app.common.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.common.mapper.CompanySearchMapper;
import com.olivin.app.common.service.CompanySearchService;
import com.olivin.app.common.service.CompanySearchVO;

import lombok.RequiredArgsConstructor;

/**
 * 회사 검색 서비스 구현 클래스 <br>
 * 모달창의 검색 영역에서 사용되는 회사 검색 기능을 구현합니다.
 * 
 * 작성자: 이창현
 * 작성일: 2025.07.30
 * 수정이력:
 * - 2025.07.30 : 최초 작성
 */
@Service
@RequiredArgsConstructor
public class CompanySearchServiceImpl implements CompanySearchService {
	private final CompanySearchMapper companySearchMapper;
	
	@Override
	public List<CompanySearchVO> selectAllCompanys() {
		return companySearchMapper.selectAllCompanys();
	}
	
	@Override
	public List<CompanySearchVO> selectAllCompanys(CompanyType comType) {
		return companySearchMapper.selectTypeCompanys(comType.getName());
	}
	
	@Override
	public List<CompanySearchVO> searchCompanys(CompanyType compType, String searchValue) {
		return companySearchMapper.selectCompanyList(compType.getName(), searchValue);
	}
}
