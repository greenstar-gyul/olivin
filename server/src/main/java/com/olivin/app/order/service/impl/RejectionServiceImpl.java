package com.olivin.app.order.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.order.mapper.RejectionMapper;
import com.olivin.app.order.service.RejectionService;
import com.olivin.app.order.service.RejectionVO;

import lombok.RequiredArgsConstructor;

/**
 * 발주서 반려에 관련된 service 인터페이스의 구현 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.08<br>
 * 수정이력:<br>
 * - 2025.08.08 : 최초 작성<br>
 * @see RejectionService
 */
@Service
@RequiredArgsConstructor
public class RejectionServiceImpl implements RejectionService {
	private final RejectionMapper rejectionMapper;
	
	@Override
	public List<RejectionVO> getAllRejection() {
		return rejectionMapper.selectAllList();
	}

	@Override
	public RejectionVO getRejectionByOrderId(String orderId) {
		return rejectionMapper.selectOneByOrderId(orderId);
	}

}
