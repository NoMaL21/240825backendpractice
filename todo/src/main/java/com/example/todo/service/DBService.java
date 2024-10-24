package com.example.todo.service;

import org.springframework.stereotype.Service;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.todo.model.TodoEntity;
import com.example.todo.persistence.TodoRepository;

@Service
public class DBService {

    @Autowired
    private TodoRepository todoRepository;

    @PostConstruct
    public void initializeDatabase() {
        // 필요한 초기화 로직 수행
        // 예: 기존 TODO 목록을 조회하거나, 특정 데이터를 로드
        List<TodoEntity> todos = todoRepository.findAll();
        if (todos.isEmpty()) {
            // 데이터베이스가 비어있으면 초기 데이터를 넣는 로직
            System.out.println("데이터베이스가 비어있습니다. 초기화 중...");
            // 초기화 작업 수행
        } else {
            System.out.println("기존 데이터 로드 완료.");
        }
    }
}
