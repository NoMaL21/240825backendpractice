package com.example.todo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.todo.model.TokenEntity;
import com.example.todo.persistence.TokenRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    public TokenEntity save(TokenEntity tokenEntity) {
        if (tokenEntity == null) {
            throw new RuntimeException("Invalid TokenEntity");
        }

        log.info("Saving token for userId: {}", tokenEntity.getUserId());
        return tokenRepository.save(tokenEntity);
    }
}
