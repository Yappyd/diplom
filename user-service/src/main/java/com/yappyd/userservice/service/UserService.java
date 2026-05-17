package com.yappyd.userservice.service;

import com.yappyd.userservice.component.UserEventPublisher;
import com.yappyd.userservice.dto.event.UserCreatedEvent;
import com.yappyd.userservice.exception.UserNotFoundException;
import com.yappyd.userservice.model.User;
import com.yappyd.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    public void saveCreatedUser(UserCreatedEvent event) {
        int inserted = userRepository.saveCreatedUser(event.userId(), event.phoneNumber(), event.createdAt());

        if (inserted == 1) {
            userEventPublisher.publishUserCreated(event.userId());
        }
    }

    public User completeProfile(UUID userId, String firstName) {
        User user = findUserById(userId);
        user.completeProfile(firstName);
        return user;
    }

    public User updateProfile(UUID userId, String firstName, String lastName, String tag, boolean phoneIsVisible) {
        User user = findUserById(userId);
        user.updateProfile(firstName, lastName, tag, phoneIsVisible);
        return user;
    }

    public User getUserById(UUID userId) {
        return findUserById(userId);
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() -> new UserNotFoundException("User not found with phone number: " + phoneNumber));
    }

    public User getUserByTag(String tag) {
        return userRepository.findByTag(tag).orElseThrow(() -> new UserNotFoundException("User not found with tag: " + tag));
    }

    public List<User> getBatchUsers(List<UUID> userIds) {
        return userRepository.findByIdInAndProfileCompletedTrue(userIds);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }
}