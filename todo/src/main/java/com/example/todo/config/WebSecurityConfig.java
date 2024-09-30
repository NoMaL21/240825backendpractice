package com.example.todo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.example.todo.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors()  // CORS 설정을 활성화 (아래의 CorsConfigurationSource와 연결)
            .and()
            .csrf().disable()
            .httpBasic().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            );
        
        // H2 콘솔을 위한 설정 (Frame 방지 비활성화)
        http.headers().frameOptions().disable();
        
        // Exception handling
        http.exceptionHandling()
            .authenticationEntryPoint((request, response, authException) -> {
                Map<String, Object> data = new HashMap<>();
                data.put("status", HttpServletResponse.SC_FORBIDDEN);
                data.put("message", authException.getMessage());

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                objectMapper.writeValue(response.getOutputStream(), data);
            });

        // JWT 필터 추가
        http.addFilterAfter(jwtAuthenticationFilter, CorsFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:3000");  // 허용할 도메인 설정
        configuration.addAllowedMethod("*");  // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*");  // 모든 헤더 허용
        configuration.setAllowCredentials(true);  // 인증 정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 대해 CORS 적용
        return source;
    }
}


