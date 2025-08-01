package com.olivin.app.orders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.olivin.app.order.service.OrdersDetailVO;
import com.olivin.app.order.service.OrdersService;
import com.olivin.app.order.service.OrdersVO;
import com.olivin.app.order.service.SearchOrdersVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class SelectOrdersTest {
	private final String ORDER_ID = "POR20250725001";
	@Autowired
	private OrdersService ordersService;
	
	@Test
	@Disabled
	void allSelect() {
		List<OrdersVO> list = ordersService.getAllOrders(SearchOrdersVO.builder().build());
		
		int c = 0;
		for (OrdersVO vo : list) {
			log.debug("list["+(c++)+"] = "+vo);
		}
		
		assertTrue(!list.isEmpty());
	}
	
	@Test
	@Disabled
	void oneSelect() {
		OrdersVO vo = ordersService.getOneOrders(ORDER_ID);
		log.debug(vo.toString());
		
		assertTrue(vo.getOrderId() != "");
	}
	
	@Test
	void detailSelect() {
		List<OrdersDetailVO> detail = ordersService.getDetailOrders(ORDER_ID);
		
		int c = 0;
		for (OrdersDetailVO vo : detail) {
			log.debug("detail["+(c++)+"] = "+vo);
		}
		
		assertTrue(!detail.isEmpty());
	}
	
	@Test
	@Disabled
	void orderInfoSelect() {
		OrdersVO master = ordersService.getOneOrders(ORDER_ID);
		List<OrdersDetailVO> detail = ordersService.getDetailOrders(ORDER_ID);
		log.debug("master: "+master.toString());

		int c = 0;
		for (OrdersDetailVO vo : detail) {
			log.debug("detail["+(c++)+"] = "+vo);
		}
		
		assertTrue(master.getOrderId() != "");
	}
	
	@Test
	@Disabled
	void createOrders() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		OrdersVO master = OrdersVO.builder()
				.orderTitle("20250728 재고부족 발주건")
				.creatorId("이창현")
				.orderType("OH")
				.reason("OT001")
				.orderDate(new Date())
				.orderStatus("OS001")
				.totalAmount(1500000d)
				.dueDate(sdf.parse("2025-08-08"))
				.orderFromId("COM001")
				.orderFrom("올리빈 본사")
				.orderToId("COM002")
				.orderTo("네이처코스메틱")
				.build();
//		log.debug(master.toString());
		
		List<OrdersDetailVO> detail = new ArrayList<>();
		detail.add(OrdersDetailVO.builder()
				.productId("PRD24001")
				.productName("화장품1")
				.quantity(100)
				.unit("U001")
				.price(15000d)
				.category("PC001")
				.vendorId("COM002")
				.vendorName("네이처코스메틱")
				.build());
		detail.add(OrdersDetailVO.builder()
				.productId("PRD24002")
				.productName("화장품2")
				.quantity(50)
				.unit("U001")
				.price(13500d)
				.category("PC001")
				.vendorId("COM002")
				.vendorName("네이처코스메틱")
				.build());
//		log.debug(detail.toString());
		
		//생성
		ordersService.createOrders(master, detail);
		
		//조회
		OrdersVO masterVO = ordersService.getOneOrders(master.getOrderId());
		List<OrdersDetailVO> detailVO = ordersService.getDetailOrders(master.getOrderId());
		log.debug("master: "+masterVO.toString());

		int c = 0;
		for (OrdersDetailVO vo : detailVO) {
			log.debug("detail["+(c++)+"] = "+vo);
		}
		
		//삭제
		ordersService.deleteOrder(master.getOrderId());
	}
}
