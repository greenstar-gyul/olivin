package com.olivin.app.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.example.service.DeptService;
import com.olivin.app.example.service.DeptVO;
import com.olivin.app.example.service.EmpVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class DeptSearchController {
  private final DeptService deptService;

  // 전체 목록을 조회하는 API 엔드포인트를 정의합니다.
  // 클라이언트에서 요청한 전체 목록을 반환합니다.
  @GetMapping("/depts/search")
  public List<DeptVO> selectAllDepts(){
      return deptService.selectAllDepts();
  }

      // 등록
    @PostMapping("/public/depts")
    public int insertDept(@RequestBody DeptVO deptVO) {
        return deptService.insertDept(deptVO);
    }

    // 수정
    @PutMapping("/public/depts")
    public int updateDept(@RequestBody DeptVO deptVO) {
        return deptService.updateDept(deptVO);
    }

    // 삭제
    @DeleteMapping("/public/depts")
    public int deleteDept(@RequestParam String departmentId) {
        return deptService.deleteDept(departmentId);    }
  

}