package com.example.todo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenEntity {

    @Id
    private UUID userId;  // userId를 기본 키로 사용

    @Column(nullable = true)
    private String accessToken;

    @Column(nullable = true)
    private String refreshToken;

    @Column(nullable = true)
    private String tokenType;

    @Column(nullable = true)
    private int expiresIn;

    @Column(nullable = true)
    private int refreshTokenExpiresIn;

    @Column(nullable = true)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
