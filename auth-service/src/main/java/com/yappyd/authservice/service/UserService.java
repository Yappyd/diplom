package com.yappyd.authservice.service;

import com.yappyd.authservice.model.AuthUser;
import com.yappyd.authservice.repository.AuthUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final AuthUserRepository authUserRepository;

    public UserService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    public Optional<AuthUser> getUserByPhoneNumber(String phoneNumber) {
        return authUserRepository.findByPhoneNumber(phoneNumber);
    }

    public Optional<AuthUser> getUserById(UUID id) {
        return authUserRepository.findById(id);
    }

    @Transactional
    public AuthUser saveVerifiedUser(String phoneNumber) {
        AuthUser authUser = new AuthUser(UUID.randomUUID(), phoneNumber);
        return authUserRepository.save(authUser);
    }

}