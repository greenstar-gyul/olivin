package com.olivin.app.auth.service;

import com.olivin.app.auth.mapper.RefreshTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Transactional
public class RefreshTokenService {
    
    @Autowired
    private RefreshTokenMapper refreshTokenMapper;
    
    @Autowired
    private JwtService jwtService;
    
    public void saveRefreshToken(Long userId, String token) {
        try {
            refreshTokenMapper.deleteByUserId(userId);
            
            RefreshTokenVO refreshToken = new RefreshTokenVO();
            refreshToken.setUserId(userId);
            refreshToken.setToken(token);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date expiryDate = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L);
            refreshToken.setExpiresAt(sdf.format(expiryDate));
            
            refreshTokenMapper.insertRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new RuntimeException("Refresh Token 저장 실패", e);
        }
    }
    
    public boolean validateRefreshToken(String token) {
        return refreshTokenMapper.countByToken(token) > 0 && 
               jwtService.validateRefreshToken(token);
    }
    
    public String getUsernameFromRefreshToken(String token) {
        return jwtService.getUsernameFromRefreshToken(token);
    }
    
    public void deleteRefreshToken(String token) {
        refreshTokenMapper.deleteByToken(token);
    }
    
    public void deleteExpiredTokens() {
        refreshTokenMapper.deleteExpiredTokens();
    }
}