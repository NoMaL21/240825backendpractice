package com.example.todo.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.example.todo.model.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenProvider {
	 private String SECRET_KEY;

	    public TokenProvider() {
	        try (InputStream input = getClass().getClassLoader().getResourceAsStream("jwt-secret.properties")) {
	            Properties prop = new Properties();
	            if (input == null) {
	                log.error("Sorry, unable to find jwt-secret.properties");
	                return;
	            }
	            // Load the properties file
	            prop.load(input);
	            SECRET_KEY = prop.getProperty("jwt.secret");
	        } catch (IOException ex) {
	            log.error("Error loading secret key", ex);
	        }
	    }
	
	public String create(UserEntity userEntity) {
		Date expireDate = Date.from(
				Instant.now()
				.plus(1,ChronoUnit.DAYS)
				);
		
		return Jwts.builder()
				.signWith(SignatureAlgorithm.HS512, SECRET_KEY)
				.setSubject(userEntity.getId().toString())
				//만약 여기서 다시 uuid로 변환하려면 UUID userId = UUID.fromString(claims.getSubject()); 이렇게 한다
				.setIssuer("todo app")
				.setIssuedAt(new Date())
				.setExpiration(expireDate)
				.compact();
	}
	
	public String validateAndGetUserId(String token) {
		Claims claims = Jwts.parser()
				.setSigningKey(SECRET_KEY)
				.parseClaimsJws(token)
				.getBody();
		
		return claims.getSubject();
	}
}
