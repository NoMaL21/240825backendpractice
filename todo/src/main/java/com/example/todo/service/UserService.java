package com.example.todo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.todo.model.UserEntity;
import com.example.todo.persistence.UserRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserEntity create(final UserEntity userEntity) {
        if (userEntity == null || userEntity.getEmail() == null) {
            throw new RuntimeException("Invalid arguments");
        }

        final String email = userEntity.getEmail();

        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists {}", email);
            throw new RuntimeException("Email already exists");
        }

        return userRepository.save(userEntity);
    }

    public UserEntity getByCredentials(final String email, final String password, final PasswordEncoder encoder) {
        final UserEntity originalUser = userRepository.findByEmail(email);

        if (originalUser != null && encoder.matches(password, originalUser.getPassword())) {
            return originalUser;
        }
        return null;
    }

    // 유저 정보를 업데이트하는 메서드
    public UserEntity update(UserEntity userEntity) {
        if (userEntity == null || userEntity.getId() == null) {
            throw new RuntimeException("Invalid arguments");
        }
        
        // 기존 유저가 존재하는지 확인
        Optional<UserEntity> existingUser = userRepository.findById(userEntity.getId());
        if (existingUser.isPresent()) {
            return userRepository.save(userEntity);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    // UUID로 유저 정보를 조회하는 메서드
    public UserEntity getById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // 유저가 존재하지 않을 경우 예외 처리
    }
}
