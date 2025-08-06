package com.olivin.app.sales;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.olivin.app.sales.service.SalesOrdersDetailVO;
import com.olivin.app.sales.service.SalesOrdersService;
import com.olivin.app.sales.service.SalesOrdersVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class SelectSalesOrdersTest {
	private final String SO_ID = "SOR20250806001";
	@Autowired
	private SalesOrdersService salesOrdersService;
	
	@Test
	void allSelect() {
		List<SalesOrdersVO> list = salesOrdersService.getAllOrders(SalesOrdersVO.builder().build());
		
		int c = 0;
		for (SalesOrdersVO vo : list) {
			log.debug("list["+(c++)+"] = "+vo);
		}
		
		assertTrue(!list.isEmpty());
	}
	
	@Test
	@Disabled
	void oneSelect() {
		SalesOrdersVO vo = salesOrdersService.getOneOrders(SO_ID);
		log.debug(vo.toString());
		
		assertTrue(vo.getSoId() != "");
	}
	
	@Test
	@Disabled
	void detailSelect() {
		List<SalesOrdersDetailVO> detail = salesOrdersService.getDetailOrders(SO_ID);
		
		int c = 0;
		for (SalesOrdersDetailVO vo : detail) {
			log.debug("detail["+(c++)+"] = "+vo);
		}
		
		assertTrue(!detail.isEmpty());
	}
	
	@Test
	void orderInfoSelect() {
		SalesOrdersVO master = salesOrdersService.getOneOrders(SO_ID);
		List<SalesOrdersDetailVO> detail = salesOrdersService.getDetailOrders(SO_ID);
		log.debug("master: "+master.toString());

		int c = 0;
		for (SalesOrdersDetailVO vo : detail) {
			log.debug("detail["+(c++)+"] = "+vo);
		}
		
		assertTrue(master.getSoId() != "");
	}
}
