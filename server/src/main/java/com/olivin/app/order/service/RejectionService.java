package com.olivin.app.order.service;

import java.util.List;

/**
 * 발주서 반려에 관련된 service를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.08<br>
 * 수정이력:<br>
 * - 2025.08.08 : 최초 작성<br>
 */
public interface RejectionService {
	//1. 전체조회
	List<RejectionVO> getAllRejection();
	//2. 단건조회
	RejectionVO getRejectionByOrderId(String orderId);
}
