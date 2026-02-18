package com.yappyd.userservice.service;

import com.yappyd.userservice.dto.rabbitmq.UserCreatedEvent;
import com.yappyd.userservice.model.User;
import com.yappyd.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    void saveCreatedUser(UserCreatedEvent event) {
        userRepository.saveCreatedUser(event.userId(), event.phoneNumber(), event.createdAt());
    }
}