package com.example.todo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;

import org.hibernate.annotations.UuidGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Todo")
public class TodoEntity {
	@Id
	@GeneratedValue(generator="system-uuid")
	@UuidGenerator(style = UuidGenerator.Style.RANDOM)
	private String id;
	
	private String userId;
	private String title;
	private boolean done;
	
	private String target_name;
	private LocalTime executionTime;
	
	@Enumerated(EnumType.STRING)
	private TodoState state;
}
