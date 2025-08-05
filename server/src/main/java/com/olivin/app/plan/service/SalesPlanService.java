package com.olivin.app.plan.service;

import java.util.List;

/**
 * 매출계획에 관련된 service를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.04<br>
 * 수정이력:<br>
 * - 2025.08.04 : 최초 작성<br>
 */
public interface SalesPlanService {
	List<SalesPlanVO> getAllSalesPlans(SalesPlanVO salesPlanVO);
}
