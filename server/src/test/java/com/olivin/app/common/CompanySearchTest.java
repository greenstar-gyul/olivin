package com.olivin.app.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.olivin.app.common.service.CompanySearchService;
import com.olivin.app.common.service.CompanySearchService.CompanyType;
import com.olivin.app.common.service.CompanySearchVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class CompanySearchTest {
	@Autowired
	private CompanySearchService comSearchService;
	
	@Test
	@Disabled
	public void selectAllCompany() {
		List<CompanySearchVO> list = comSearchService.selectAllCompanys();
		
		int c = 0;
		for (CompanySearchVO vo : list) {
			log.debug("list["+(c++)+"] = "+vo);
		}
		
		assertTrue(!list.isEmpty());
	}
	
	@Test
	@Disabled
	public void selectTypeCompany() {
		//지점 목록
		List<CompanySearchVO> list = comSearchService.selectAllCompanys(CompanyType.branch);
		
		int c = 0;
		for (CompanySearchVO vo : list) {
			log.debug("list["+(c++)+"] = "+vo);
		}
		
		assertTrue(!list.isEmpty());
	}
	
	@Test
	@Disabled
	public void searchCompany() {
		// 공급업체에서 '프리미엄'이 들어간 회사 검색
		List<CompanySearchVO> list = comSearchService.searchCompanys(CompanyType.valueOf("supplier"), "프리미엄");		

		int c = 0;
		for (CompanySearchVO vo : list) {
			log.debug("list["+(c++)+"] = "+vo);
		}
		
		assertTrue(!list.isEmpty());
	}
}
