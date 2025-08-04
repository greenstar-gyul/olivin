package com.olivin.app.plan.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.plan.service.SalesPlanService;
import com.olivin.app.plan.service.SalesPlanVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 매출계획에 관련된 컨트롤러 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.04<br>
 * 수정이력:<br>
 * - 2025.08.04 : 최초 작성<br>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SalesPlanController {
	private final SalesPlanService salesPlanService;
	
	@GetMapping("/salesPlan")
	public List<SalesPlanVO> ordersList(@ModelAttribute SalesPlanVO search) {
		log.debug("salesPlan = {}", search);
		return salesPlanService.getAllSalesPlans(search);
	}
}
