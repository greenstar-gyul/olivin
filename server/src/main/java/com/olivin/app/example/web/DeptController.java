package com.olivin.app.example.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.example.service.DeptService;
import com.olivin.app.example.service.DeptVO;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class DeptController {
  private final DeptService deptService;

  @GetMapping("/depts/search")
  public List<DeptVO> DeptList() {
    return deptService.selectAllDepts();
  }
}
