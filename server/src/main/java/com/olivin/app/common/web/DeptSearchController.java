package com.olivin.app.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.example.service.DeptSearchService;
import com.olivin.app.common.service.ProductSearchVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class DeptSearchController {
  private final DeptSearchService DeptSearchService;

  // 전체 목록을 조회하는 API 엔드포인트를 정의합니다.
  // 클라이언트에서 요청한 전체 목록을 반환합니다.
  @GetMapping("/search/depts/all")
  public List<DeptVO> selectAllDepts(){
      return deptSearchService.selectAllDepts();
  }

  // 상품 검색 API 엔드포인트를 정의합니다.
  // 클라이언트에서 요청한 상품 조건에 맞는 상품 목록을 반환합니다.
  @GetMapping("/search/depts")
  public List<DeptVO> searchDepts(@RequestParam String searchValue) {
      return deptSearchService.searchDepts(searchValue);
  }
}

