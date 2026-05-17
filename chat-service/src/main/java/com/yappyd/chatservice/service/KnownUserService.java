package com.yappyd.chatservice.service;

import com.yappyd.chatservice.exception.UnknownUserException;
import com.yappyd.chatservice.model.KnownUser;
import com.yappyd.chatservice.repository.KnownUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class KnownUserService {

    private final KnownUserRepository knownUserRepository;

    public void upsertUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        if (!knownUserRepository.existsById(userId)) {
            knownUserRepository.save(new KnownUser(userId));
        }
    }

    public void validateUserKnown(UUID userId) {
        if (userId == null) {
            throw new UnknownUserException(null);
        }

        if (!knownUserRepository.existsById(userId)) {
            throw new UnknownUserException(userId);
        }
    }

    public void validateUsersKnown(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        Set<UUID> uniqueUserIds = new HashSet<>(userIds);

        List<UUID> knownUserIds = knownUserRepository.findAllById(uniqueUserIds)
                .stream()
                .map(KnownUser::getUserId)
                .toList();

        Set<UUID> knownUserIdSet = new HashSet<>(knownUserIds);

        for (UUID userId : uniqueUserIds) {
            if (!knownUserIdSet.contains(userId)) {
                throw new UnknownUserException(userId);
            }
        }
    }
}