package com.example.todo.config;

import org.springframework.web.filter.CorsFilter;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.MediaType;

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.todo.security.JwtAuthenticationFilter;
import com.example.todo.config.WebMvcConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            .cors()
            .and()
            .csrf().disable()
            .httpBasic().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/auth/**").permitAll()
                .anyRequest().authenticated()
            );

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
}


/*
package com.example.todo.config;

import org.springframework.web.filter.CorsFilter;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletResponse;

import org.h2.util.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.MediaType;

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.todo.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig{

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	//@Override
    protected void configure(HttpSecurity http) throws Exception{
    	http.cors()
    	.and()
    	.csrf()
    	.disable()
    	.httpBasic()
    	.disable()
    	.httpBasic()
    	.disable()
    	.sessionManagement()
    	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    	.and()
    	.authorizeRequests()
    	.requestMatchers("/","/auth/**").permitAll()
    	.anyRequest()
    	.authenticated();
    	
    	http.exceptionHandling()
    	.authenticationEntryPoint((request, response, e) ->
    	{
    		Map<String,Object< data = new HashMap<String, Object>();
    		data.put("status", HttpServletResponse.SC_FORBIDDEN);
    		data.put("message", e.getMessage());
    		
    		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    		
    		objectMapper.writeValue(response.getOutputStream(), data);
    	
    	});
    	
    	http.addFilterAfter(jwtAuthenticationFilter, CorsFilter.class);
    }
    
}*/