package com.olivin.app.plan;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.olivin.app.plan.service.SalesPlanService;
import com.olivin.app.plan.service.SalesPlanVO;
import com.olivin.app.plan.service.SearchSalesPlanVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class SelectPlanTest {
	@Autowired
	private SalesPlanService salesPlanService;
	
	@Test
	@Disabled
	void allSelect() {
		List<SalesPlanVO> list = salesPlanService.getAllSalesPlans(SearchSalesPlanVO.builder().build());
		
		int c = 0;
		for (SalesPlanVO vo : list) {
			log.debug("list["+(c++)+"] = "+vo);
		}
		
		assertTrue(!list.isEmpty());
	}
}
