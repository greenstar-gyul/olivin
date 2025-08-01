package com.olivin.app.auth.service;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenVO {
    private Long id;
    private Long userId;
    private String token;
    private String expiresAt;
    private String createdAt;
}