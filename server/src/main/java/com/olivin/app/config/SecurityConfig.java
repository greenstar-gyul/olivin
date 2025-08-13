// SecurityConfig.java
package com.olivin.app.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.olivin.app.auth.service.UserService;
import com.olivin.app.security.JwtAuthenticationEntryPoint;
import com.olivin.app.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final UserService userService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 경로
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/outbndMgmt/**").permitAll()
                .requestMatchers("/outbndMgmt/**").permitAll()
                .requestMatchers("/api/inbndMgmt/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/dashboard/supplier/debug-**").permitAll()
                .requestMatchers("/api/dashboard/**").authenticated()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/test**").permitAll()  
                // 권한 기반 접근 제어로 변경
                .requestMatchers("/uikit/**").hasAnyAuthority("/uikit/formlayout", "/uikit/input", "/uikit/button", "/uikit/table", "/uikit/list", "/uikit/tree", "/uikit/panel", "/uikit/overlay", "/uikit/media", "/uikit/message", "/uikit/file", "/uikit/menu", "/uikit/charts", "/uikit/misc", "/uikit/timeline")
                .requestMatchers("/standard/**").hasAnyAuthority("/standard/supplier", "/standard/branch", "/standard/product", "/standard/productapproval")
                .requestMatchers("/roles/**").hasAnyAuthority("/roles/empmanage", "/roles/rolemanage")
                // WebSocket 연결
                .requestMatchers("/ws/**").permitAll()
                
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("http://localhost:3049");
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}