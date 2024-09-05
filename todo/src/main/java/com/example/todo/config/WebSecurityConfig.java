package com.example.todo.config;

import org.springframework.web.filter.CorsFilter;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.example.todo.security.JwtAuthenticationFilter;

import lombok.extern.slf4j.Slf4j;

@EnableWebSecurity
@Slf4j
public class WebSecurityConfig{


    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 생성자를 통해 JwtAuthenticationFilter 주입
    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors()
            .and()
            .csrf().disable()
            .httpBasic().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/auth/**").permitAll()
                .anyRequest().authenticated()
            );

        // JwtAuthenticationFilter를 CorsFilter 다음에 추가
        http.addFilterAfter(jwtAuthenticationFilter, CorsFilter.class);

        return http.build();
    }
}
