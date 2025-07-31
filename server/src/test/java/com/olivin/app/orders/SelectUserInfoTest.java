package com.olivin.app.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.olivin.app.order.service.OrdersService;
import com.olivin.app.order.service.UserCompanyVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class SelectUserInfoTest {
	@Autowired
	private OrdersService ordersService;
	
	@Test
	void getUserCompany() {
		UserCompanyVO compVO = ordersService.getCompInfo("olivin40004");
		log.debug(compVO.toString());
	}
}
