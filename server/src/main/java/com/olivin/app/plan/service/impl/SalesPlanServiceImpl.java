package com.olivin.app.plan.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.plan.mapper.SalesPlanMapper;
import com.olivin.app.plan.service.SalesPlanService;
import com.olivin.app.plan.service.SalesPlanVO;

import lombok.RequiredArgsConstructor;

/**
 * 매출계획에 관련된 service 인터페이스의 구현 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.04<br>
 * 수정이력:<br>
 * - 2025.08.04 : 최초 작성<br>
 * @see SalesPlanService
 */
@Service
@RequiredArgsConstructor
public class SalesPlanServiceImpl implements SalesPlanService {
	private final SalesPlanMapper salesPlanMapper;
	
	@Override
	public List<SalesPlanVO> getAllSalesPlans(SalesPlanVO salesPlanVO) {
		return salesPlanMapper.selectAllList(salesPlanVO);
	}
}
