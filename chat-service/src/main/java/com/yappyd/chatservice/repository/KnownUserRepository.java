package com.yappyd.chatservice.repository;

import com.yappyd.chatservice.model.KnownUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KnownUserRepository extends JpaRepository<KnownUser, UUID> {
}