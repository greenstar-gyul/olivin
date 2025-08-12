package com.olivin.app.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MenuMapper {

    /**
     * 사용자 역할 조회
     */
    String getUserRole(@Param("userId") String userId);

    /**
     * 사용자의 권한(메뉴) 목록 조회
     */
    List<Map<String, Object>> getUserPermissions(@Param("userId") String userId);

    /**
     * 모든 권한(메뉴) 목록 조회 (관리자용)
     */
    List<Map<String, Object>> getAllPermissions();
}