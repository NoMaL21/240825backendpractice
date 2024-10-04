package com.example.todo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.todo.model.TodoEntity;
import com.example.todo.persistence.TodoRepository;

import java.util.List;

@Service
public class TodoService {

    @Autowired
    private TodoRepository repository;

    // CREATE
    public List<TodoEntity> create(final TodoEntity entity) {
        repository.save(entity);
        return repository.findByUserId(entity.getUserId());
    }

    // RETRIEVE
    public List<TodoEntity> retrieve(final String userId) {
        return repository.findByUserId(userId);
    }

    // UPDATE
    public List<TodoEntity> update(final TodoEntity entity) {
        final TodoEntity original = repository.findById(entity.getId()).orElse(null);

        if (original != null) {
            original.setTitle(entity.getTitle());
            original.setDone(entity.isDone());
            original.setTarget_name(entity.getTarget_name());
            original.setExecutionTime(entity.getExecutionTime());
            original.setState(entity.getState());
            repository.save(original);
        }

        return repository.findByUserId(entity.getUserId());
    }

    // DELETE
    public List<TodoEntity> delete(final TodoEntity entity) {
        try {
            repository.delete(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting entity", e);
        }

        return repository.findByUserId(entity.getUserId());
    }
}
