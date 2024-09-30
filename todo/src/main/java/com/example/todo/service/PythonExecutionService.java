package com.example.todo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.todo.model.TodoEntity;
import com.example.todo.model.TodoState;
import com.example.todo.persistence.TodoRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PythonExecutionService {

	@Autowired
    private TodoRepository todoRepository;
	
	@Scheduled(fixedRate = 60000)
    public void checkAndExecuteTodos() {
        // 현재 시간보다 실행 시간이 이전이고 대기 중인 Todo를 조회
        List<TodoEntity> todos = todoRepository.findByExecutionTimeBeforeAndState(LocalDateTime.now(), TodoState.PENDING);
        
        for (TodoEntity todo : todos) {
            executeTodo(todo);
        }
    }
	
	private void executeTodo(TodoEntity todo) {
        try {
            // Python 스크립트를 실행
            Process process = Runtime.getRuntime().exec("python3 /path/to/your/script.py " + todo.getTarget_name() + " " + todo.getId());

            // 결과 출력
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            // Todo 상태를 진행 중으로 변경
            todo.setState(TodoState.IN_PROGRESS);
            todoRepository.save(todo);

            // Task가 완료된 후 다시 대기 상태로 되돌리기 위해 일정 시간 후에 처리할 수 있습니다.
            // 예: 실행 완료 후 대기 상태로 되돌리기
            // (여기서는 간단하게 IN_PROGRESS 상태로 남겨두겠습니다.)
            // 이 부분은 별도의 비즈니스 로직에 따라 조정할 수 있습니다.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
