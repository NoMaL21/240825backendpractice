package com.example.todo.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import com.example.todo.model.TodoEntity;
import com.example.todo.model.TodoState;

@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, String> {
	
	@Query("select t from TodoEntity t where t.userId = ?1")
	List<TodoEntity>findByUserId(String userId);

	List<TodoEntity> findByExecutionTimeBeforeAndState(LocalDateTime now, TodoState pending);

}
