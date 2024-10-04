package com.example.todo.dto;

import com.example.todo.model.TodoEntity;
import com.example.todo.model.TodoState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TodoDTO {

    private String id;
    private String userId;
    private String title;
    private boolean done;

    private String target_name;
    private LocalTime executionTime;
    private TodoState state;

    // Entity to DTO 변환
    public TodoDTO(TodoEntity entity) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.title = entity.getTitle();
        this.done = entity.isDone();
        this.target_name = entity.getTarget_name();
        this.executionTime = entity.getExecutionTime();
        this.state = entity.getState();
    }

    // DTO to Entity 변환
    public static TodoEntity toEntity(final TodoDTO dto) {
        return TodoEntity.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .done(dto.isDone())
                .target_name(dto.getTarget_name())
                .executionTime(dto.getExecutionTime())
                .state(dto.getState())
                .build();
    }
}
