package com.example.todo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.dto.ResponseDTO;
import com.example.todo.dto.TodoDTO;
import com.example.todo.service.TodoService;
import com.example.todo.model.TodoEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("todo")
public class TodoController {
	
	@Autowired
	private TodoService service;
	
	@PostMapping
	public ResponseEntity<?>createTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto){
		
		try {
			TodoEntity entity = TodoDTO.toEntity(dto);
			
			entity.setId(null);;
			entity.setUserId(userId);
			
			List<TodoEntity> entities = service.create(entity);
			
			List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
			log.info("Log:entities => dtos ok!");
			
			ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
			log.info("Log:responsedto ok!");
			
			return ResponseEntity.ok().body(response);
			} catch(Exception e) {
				String error = e.getMessage();
				ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();
				return ResponseEntity.badRequest().body(response);
			}
	}
	
	@GetMapping
	public ResponseEntity<?> retrieveTodo(@AuthenticationPrincipal String userId){
		List<TodoEntity> entities = service.retrieve(userId);
		List<TodoDTO> dtos =
				entities.stream().map(TodoDTO::new).collect(Collectors.toList());
		
		ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
		
		return ResponseEntity.ok().body(response);
	}
	
	@PutMapping
	public ResponseEntity<?>updateTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto){
		try {
			//dto를 이용해 테이블에 저장하기 위한 entity를 생성한다.
			TodoEntity entity = TodoDTO.toEntity(dto);
			
			//entity userId를 저장한다.
			entity.setUserId(userId);
			
			List<TodoEntity> entities = service.update(entity);
			
			//entities를 tos로 스트림 변환한다.
			List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
			
			//ResponseDTO를 생성한다.
			ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
			
			//HTTP Status 200 상태로 response를 전송한다.
			return ResponseEntity.ok().body(response);
			}
		catch(Exception e) {
			String error = e.getMessage();
			ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	@DeleteMapping
	public ResponseEntity<?> deleteTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto){
		try {
			TodoEntity entity = TodoDTO.toEntity(dto);
			
			entity.setUserId(userId);
			
			List<TodoEntity> entities = service.delete(entity);
			
			List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
			
			//ResponseDTO를 생성한다.
			ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
			return ResponseEntity.ok().body(response);
		}catch (Exception e) {
			String error = e.getMessage();
			ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();
			return ResponseEntity.badRequest().body(response);
		}
	}

}
