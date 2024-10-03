package com.example.todo.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.dto.ResponseDTO;
import com.example.todo.dto.UserDTO;
import com.example.todo.model.TokenEntity;
import com.example.todo.model.UserEntity;
import com.example.todo.security.TokenProvider;
import com.example.todo.service.TokenService;
import com.example.todo.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController {
	@Autowired
	private UserService userService;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private TokenProvider tokenProvider;
	
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	@PostMapping("/signup")
	public ResponseEntity<?>registerUser(@RequestBody UserDTO userDTO){
		try {
			UserEntity user = UserEntity.builder()
					.email(userDTO.getEmail())
					.username(userDTO.getUsername())
					.password(passwordEncoder.encode(userDTO.getPassword()))
					.kakaoauthtoken(null)
					.build();
			
			UserEntity registeredUser = userService.create(user);
			
			log.warn(registeredUser.getId().toString());
			
			UserDTO responseUserDTO = userDTO.builder()
					.email(registeredUser.getEmail())
					.id(registeredUser.getId().toString())
					//만약 여기서 다시 uuid로 변환하려면 UUID userId = UUID.fromString(claims.getSubject()); 이렇게 한다
					.username(registeredUser.getUsername())
					.kakaoauthtoken(null)
					.build();
			return ResponseEntity.ok().body(responseUserDTO);
		}
		catch(Exception e) {
			ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();
			return ResponseEntity.badRequest().body(responseDTO);
		}
	}
	
	@PostMapping("/signin")
	public ResponseEntity<?>authenticate(@RequestBody UserDTO userDTO){
		UserEntity user = userService.getByCredentials(userDTO.getEmail(), userDTO.getPassword(), passwordEncoder);
		
		if(user != null) {
			final String token = tokenProvider.create(user);
			
			log.warn(token);
			
			final UserDTO responseUserDTO = UserDTO.builder()
					.email(user.getEmail())
					.id(user.getId().toString())
					//만약 여기서 다시 uuid로 변환하려면 UUID userId = UUID.fromString(claims.getSubject()); 이렇게 한다
					.token(token)
					.build();
			
			return ResponseEntity.ok().body(responseUserDTO);
		}else {
			ResponseDTO responseDTO = ResponseDTO.builder()
					.error("Login failed")
					.build();
			
			return ResponseEntity.badRequest().body(responseDTO);
		}
	}
	
	@PostMapping("/kakao")
	public ResponseEntity<?> handleKakaoAuth(@AuthenticationPrincipal String userId, @RequestBody UserDTO userDTO) {
	    UserEntity user = userService.getById(UUID.fromString(userId)); // UUID로 사용자 조회
	    if (user != null) {
	    	String encodedKakaoAuthToken = Base64.getEncoder().encodeToString(userDTO.getKakaoauthtoken().getBytes());
	    	user.setKakaoauthtoken(encodedKakaoAuthToken);
	        userService.update(user); // 사용자 정보 업데이트
	        
	        log.info(user.getEmail());
	        // Kakao 인증 코드를 이용해 Python 스크립트를 실행하는 로직 추가
	        try {
	            // Kakao 인증 토큰을 기반으로 Python 스크립트 실행
	        	byte[] decodedBytes = Base64.getDecoder().decode(user.getKakaoauthtoken());
	            String decodedToken = new String(decodedBytes); // 실제 토큰 문자열로 변환
	        	
	            log.info(decodedToken);
	            
	            ProcessBuilder processBuilder = new ProcessBuilder(
	            	    "python", 
	            	    "src/main/resources/get_kakao_code.py", 
	            	    "--authorize_code", 
	            	    decodedToken
	            	);
	            processBuilder.redirectErrorStream(true);
	            Process process = processBuilder.start();

	            // Python 스크립트의 출력 읽기
	            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	            StringBuilder output = new StringBuilder();
	            String line;
	            while ((line = reader.readLine()) != null) {
	                output.append(line);
	            }
	            
	            log.info("Python script output: " + output.toString());
	            
	            // 스크립트 실행 후 프로세스 종료 대기
	            int exitCode = process.waitFor();
	            if (exitCode != 0) {
	            	log.error("Python script failed with exit code: " + exitCode);
	                throw new RuntimeException("Failed to execute Python script");
	            }

	            // JSON 형식으로 토큰 정보 파싱 (예: JSONObject json = new JSONObject(output.toString()))
	            JSONObject json = new JSONObject(output.toString());
	            String accessToken = passwordEncoder.encode(json.getString("access_token"));
	            String refreshToken = passwordEncoder.encode(json.getString("refresh_token"));
	            int expiresIn = json.getInt("expires_in");
	            int refreshTokenExpiresIn = json.getInt("refresh_token_expires_in");

	            // TokenEntity를 생성하고 저장
	            TokenEntity tokenEntity = TokenEntity.builder()
	                    .userId(user.getId())
	                    // 필요한 토큰 정보를 여기에 세팅
	                    .accessToken(accessToken)
	                    .refreshToken(refreshToken)
	                    .expiresIn(expiresIn)
	                    .refreshTokenExpiresIn(refreshTokenExpiresIn)
	                    .build();

	            // 토큰 저장
	            tokenService.save(tokenEntity);

	            return ResponseEntity.ok("Kakao auth token saved successfully.");
	        } catch (Exception e) {
	            log.error("Error while handling Kakao auth: {}", e.getMessage());
	            return ResponseEntity.badRequest().body("Error while processing Kakao auth.");
	        }
	    } else {
	        return ResponseEntity.badRequest().body("User not found.");
	    }
	}

}
