package com.example.todo.persistence;

import com.example.todo.model.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<TokenEntity, UUID> {
    TokenEntity findByUserId(UUID userId);
}
