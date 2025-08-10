package com.olivin.app.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.example.service.EmpService;
import com.olivin.app.example.service.EmpVO;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class EmpController {

  private final EmpService empService;

  // 전체 조회
  @GetMapping("/public/emps/search")
  public List<EmpVO> empList() {
      return empService.selectAllList();
  }
  
    // 조건 검색 (검색어 파라미터 기반)
    @GetMapping("/public/emps")
    public List<EmpVO> searchEmps(@RequestParam String searchValue) {
        return empService.searchEmps(searchValue);
    }

    // 등록
    @PostMapping("/public/emps")
    public int insertEmp(@RequestBody EmpVO empVO) {
        return empService.insertEmp(empVO);
    }

    // 수정
    @PutMapping("/public/emps")
    public int updateEmp(@RequestBody EmpVO empVO) {
        return empService.updateEmp(empVO);
    }

    // 삭제
    @DeleteMapping("/public/emps")
    public int deleteEmp(@RequestParam String employeeId) {
        return empService.deleteEmp(employeeId);
    }
  
}
