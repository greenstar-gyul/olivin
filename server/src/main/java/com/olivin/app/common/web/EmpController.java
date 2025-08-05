package com.olivin.app.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.example.service.EmpService;
import com.olivin.app.example.service.EmpVO;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class EmpController {
  private final EmpService empService;

  @GetMapping("/public/emps/search")
  public List<EmpVO> EmpList() {
      return empService.selectAllList();
  }
  
  // @PostMapping("path")
  // public String postMethodName(@RequestBody String entity) {
  //     //TODO: process POST request
      
  //     return entity;
  // }
  
}
