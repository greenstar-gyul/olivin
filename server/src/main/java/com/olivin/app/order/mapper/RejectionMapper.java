package com.olivin.app.order.mapper;

import java.util.List;

import com.olivin.app.order.service.RejectionVO;

/**
 * 발주서 반려에 관련된 mapper를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.08<br>
 * 수정이력:<br>
 * - 2025.08.08 : 최초 작성<br>
 */
public interface RejectionMapper {
  //발주서 반려사유 조회
  public List<RejectionVO> selectAllList();
  public RejectionVO selectOneByOrderId(String orderId);
}
