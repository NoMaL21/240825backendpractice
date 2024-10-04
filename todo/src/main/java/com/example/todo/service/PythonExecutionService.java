package com.example.todo.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.todo.controller.UserController;
import com.example.todo.model.TodoEntity;
import com.example.todo.model.TodoState;
import com.example.todo.model.TokenEntity;
import com.example.todo.persistence.TodoRepository;
import com.example.todo.persistence.TokenRepository;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PythonExecutionService {

	@Autowired
    private TodoRepository todoRepository;
	
	@Autowired
    private TokenRepository tokenRepository;
	
	@Scheduled(fixedRate = 1000)//60000 = 1분
    public void checkAndExecuteTodos() {
        // 현재 시간보다 실행 시간이 이전이고 대기 중인 Todo를 조회
        List<TodoEntity> todos = todoRepository.findByExecutionTimeBeforeAndState(LocalTime.now(), TodoState.PENDING);
        log.info("Schedule Running.. ");
        for (TodoEntity todo : todos) {
        	log.info(todo.getTitle() + " is excuting.. ");
    		String targetname = todo.getTarget_name();
    		String title = todo.getTitle();
    		String userId = todo.getUserId();
    		String taskId = todo.getId();
    		
    		log.info("taskId : " + taskId );
    		log.info(" userId :" + userId);
    		log.info(" title : "+  title);
    		log.info(" targetname : " + targetname);
            executeTodo(todo);
        }
    }
	
	private void executeTodo(TodoEntity todo) {
		String targetname = todo.getTarget_name();
		String title = todo.getTitle();
		String userId = todo.getUserId();
		String taskId = todo.getId();
		
		log.info("taskId : " + taskId );
		log.info(" userId :" + userId);
		log.info(" title : " + title);
		log.info(" targetname : " + targetname);
		
		TokenEntity token = tokenRepository.findByUserId(UUID.fromString(userId));
		
		if (token != null) {
			byte[] AccessToken_decodedBytes = Base64.getDecoder().decode(token.getAccessToken());
			byte[] RefreshToken_decodedBytes = Base64.getDecoder().decode(token.getRefreshToken());
			
			String accessToken = new String(AccessToken_decodedBytes);
			String tokenType = token.getTokenType();
			String refreshToken = new String(RefreshToken_decodedBytes);
			String expiresIn = Integer.toString(token.getExpiresIn());
			String refreshTokenExpiresIn = Integer.toString(token.getRefreshTokenExpiresIn());
			
			log.info("accessToken : " + accessToken );
			log.info("tokenType :" + tokenType);
			log.info("refreshToken : " + refreshToken);
			log.info("expiresIn : " + expiresIn);
			log.info("refreshTokenExpiresIn : " + refreshTokenExpiresIn);
			
	        try {
	            // Python 스크립트를 실행
	            ProcessBuilder processBuilder = new ProcessBuilder(
	            	    "python", 
	            	    "src/main/resources/Instar_to_Kakaotalk.py", 
	            	    "--target_username", targetname,
	            	    "--task_id", taskId,
	            	    "--access_token", accessToken,
	            	    "--token_type", tokenType,
	            	    "--refresh_token", refreshToken,
	            	    "--expires_in", expiresIn,
	            	    "--refresh_token_expires_in", refreshTokenExpiresIn
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
	            
	            log.info("Instar_to_Kakaotalk.py script output: " + output.toString());
	            
	            // 스크립트 실행 후 프로세스 종료 대기
	            int exitCode = process.waitFor();
	            if (exitCode != 0) {
	            	log.error("Python script failed with exit code: " + exitCode);
	                throw new RuntimeException("Failed to execute Python script");
	            }
	            
	            // JSON 형식으로 토큰 정보 파싱 (예: JSONObject json = new JSONObject(output.toString()))
	            JSONObject json = new JSONObject(output.toString());
	            String newAccessToken = json.getString("access_token");
	            String newRefreshToken = json.getString("refresh_token");
	            int newExpiresIn = json.getInt("expires_in");
	            int newRefreshTokenExpiresIn = json.getInt("refresh_token_expires_in");
	            
	            // 갱신된 토큰 정보를 Base64로 인코딩하여 저장
	            token.setAccessToken(Base64.getEncoder().encodeToString(newAccessToken.getBytes()));
	            token.setRefreshToken(Base64.getEncoder().encodeToString(newRefreshToken.getBytes()));
	            token.setExpiresIn(newExpiresIn);
	            token.setRefreshTokenExpiresIn(newRefreshTokenExpiresIn);
	            tokenRepository.save(token); // 토큰 정보 저장
	            
	            // Todo의 상태를 완료 상태로 변경
	            todo.setState(TodoState.COMPLETED);
	            todoRepository.save(todo); // Todo 상태 저장
	            
	        } catch (Exception e) {
	        	log.error("Error while executing task: {}", e.getMessage());
	        }
		} else {
			log.error("Token for userId {} not found", userId);
	    }
	}
}
