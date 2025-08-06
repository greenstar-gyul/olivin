package com.olivin.app.inbnd.service;

import java.util.List;

public interface InbndService {
  List<InbndVO> getAllInbnd();
  List<InbndProdDVO> getProdByLot();
  List<InbndProdDVO> test(String productId);
}
