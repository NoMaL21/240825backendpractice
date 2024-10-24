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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class PythonExecutionService {

	private final ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	@Autowired
    private TodoRepository todoRepository;
	
	@Autowired
    private TokenRepository tokenRepository;
	
	@Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
	public void resetTodoStates() {
	    List<TodoEntity> completedTodos = todoRepository.findByState(TodoState.COMPLETED);
	    for (TodoEntity todo : completedTodos) {
	        todo.setState(TodoState.PENDING);
	        todoRepository.save(todo);
	    }
	    log.info("모든 COMPLETED TODO가 PENDING 상태로 변경되었습니다.");
	}
	
	@Scheduled(fixedRate = 5000)//60000 = 1분
    public void checkAndExecuteTodos() {
        // 현재 시간보다 실행 시간이 이전이고 대기 중인 Todo를 조회
        List<TodoEntity> todos = todoRepository.findByExecutionTimeBeforeAndState(LocalTime.now(), TodoState.PENDING);
        LocalTime now = LocalTime.now();
        
        log.info("Schedule Running.. ");
        
        for (TodoEntity todo : todos) {
            if (isExecutionTimeClose(todo.getExecutionTime(), now)) {
                // 실행 시간이 임박한 경우에만 실행
            	log.info(todo.getTitle() + " 를 실행합니다. ");
                executeTodoIfEligible(todo);
                log.info("completed. State is : " + todo.getState().toString());
            } else {
                log.info(todo.getTitle() + "는 아직 실행 시간이 아님.");
            }
        }
    }
	
    private boolean isExecutionTimeClose(LocalTime executionTime, LocalTime now) {
        return executionTime.isBefore(now.plusMinutes(1)) && executionTime.isAfter(now.minusMinutes(1));
    } 
    //실행시간이 임박했는지 확인하는 메소드
	
	private void executeTodoIfEligible(TodoEntity todo) {
	    LocalDate today = LocalDate.now();
	    
	    // 오늘 실행된 적이 없고, 실행 시간이 현재 시간에 근접한 경우에만 실행
	    if ((todo.getLastExecutionDate() == null || !todo.getLastExecutionDate().equals(today))) {
	    	
	        log.info(todo.getTitle() + " 실행 중...");
	        todo.setLastExecutionDate(today);
	        todo.setState(TodoState.IN_PROGRESS);
	        todoRepository.save(todo);  // 상태 업데이트
	        
	        executorService.submit(() -> {
		        try {
		            executeTodo(todo);  // Python 스크립트 실행
		        } catch (Exception e) {
		            log.error("Todo 실행 중 오류 발생: {}", e.getMessage());
		            todo.setState(TodoState.ERROR);  // 실패 시 ERROR로 변경
		            todoRepository.save(todo);  // 상태 업데이트
		        }
	        });
	        
	    } else {
	        log.info(todo.getTitle() + "는 아직 실행 시간이 아니거나 이미 오늘 실행되었습니다.");
	    }
	}
	
	private void executeTodo(TodoEntity todo) {
		
    	String targetname = todo.getTarget_name();
		//String title = todo.getTitle();
		String userId = todo.getUserId();
		String taskId = todo.getId();
		
		//log.info("taskId : " + taskId );
		//log.info(" userId :" + userId);
		//log.info(" title : " + title);
		//log.info(" targetname : " + targetname);
		
		TokenEntity token = tokenRepository.findByUserId(UUID.fromString(userId));
		
		if (token != null) {
			String accessToken = decodeBase64(token.getAccessToken());
            String refreshToken = decodeBase64(token.getRefreshToken());
			String tokenType = token.getTokenType();
			String expiresIn = Integer.toString(token.getExpiresIn());
			String refreshTokenExpiresIn = Integer.toString(token.getRefreshTokenExpiresIn());
			
			//log.info("accessToken : " + accessToken );
			//log.info("tokenType :" + tokenType);
			//log.info("refreshToken : " + refreshToken);
			//log.info("expiresIn : " + expiresIn);
			//log.info("refreshTokenExpiresIn : " + refreshTokenExpiresIn);
			
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
	            String lastLine = null;
	            while ((line = reader.readLine()) != null) {
	            	output.append(line);  // 전체 출력을 저장
	                lastLine = line;  // 마지막 출력 라인을 저장
	            }
	            
	            log.info("Instar_to_Kakaotalk.py script output: " + output.toString());
	            
	            // 스크립트 실행 후 프로세스 종료 대기
	            int exitCode = process.waitFor();
	            if (exitCode != 0) {
	            	log.error("Python script failed with exit code: " + exitCode);
	                throw new RuntimeException("Failed to execute Python script");
	            }
	            
	            // 마지막 줄을 JSON 형식으로 토큰 정보 파싱
	            if (lastLine != null) {
	            	JSONObject json = new JSONObject(lastLine);
                    updateToken(token, json);
                    tokenRepository.save(token);  // 토큰 갱신 후 저장

	                // Todo의 상태를 완료 상태로 변경
	                todo.setState(TodoState.COMPLETED);
	                todoRepository.save(todo); // Todo 상태 저장
	                log.info("2. complete State is : " + todo.getState().toString());
	                
	            } else {
	                log.error("No output from Python script.");
	            }
	        } catch (Exception e) {
	        	log.error("Error while executing task: {}", e.getMessage());
	        }
		} else {
			log.error("Token for userId {} not found", userId);
	    }
	}
	
	private String decodeBase64(String encodedString) {
        return new String(Base64.getDecoder().decode(encodedString));
    }
	
    private void updateToken(TokenEntity token, JSONObject json) {
        String newAccessToken = json.getString("access_token");
        String newRefreshToken = json.getString("refresh_token");
        int newExpiresIn = json.getInt("expires_in");
        int newRefreshTokenExpiresIn = json.getInt("refresh_token_expires_in");

        token.setAccessToken(Base64.getEncoder().encodeToString(newAccessToken.getBytes()));
        token.setRefreshToken(Base64.getEncoder().encodeToString(newRefreshToken.getBytes()));
        token.setExpiresIn(newExpiresIn);
        token.setRefreshTokenExpiresIn(newRefreshTokenExpiresIn);
    }
	
}


