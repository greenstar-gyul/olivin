package com.olivin.app.auth.mapper;

import com.olivin.app.auth.service.RefreshTokenVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper {
    
    // Refresh Token 저장
    int insertRefreshToken(RefreshTokenVO refreshToken);
    
    // 사용자 ID로 기존 토큰 삭제
    int deleteByUserId(Long userId);
    
    // 토큰으로 삭제
    int deleteByToken(String token);
    
    // 토큰 존재 여부 확인
    int countByToken(String token);
    
    // 만료된 토큰 삭제
    int deleteExpiredTokens();
}