package com.yappyd.userservice.component;

import com.yappyd.userservice.dto.responce.BatchUsersResponse;
import com.yappyd.userservice.dto.responce.CurrentUserProfileResponse;
import com.yappyd.userservice.dto.responce.PublicUserProfileResponse;
import com.yappyd.userservice.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public PublicUserProfileResponse toPublicUserProfile(User user) {
        return new PublicUserProfileResponse(
                user.getId(),
                user.isPhoneIsVisible() ? user.getPhoneNumber() : null,
                user.getTag(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public CurrentUserProfileResponse toCurrentUserProfile(User user) {
        return new CurrentUserProfileResponse(
                user.getId(),
                user.getPhoneNumber(),
                user.isPhoneIsVisible(),
                user.isProfileCompleted(),
                user.getFirstName(),
                user.getLastName(),
                user.getTag(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public BatchUsersResponse toBatch(List<User> users) {
        Map<UUID, PublicUserProfileResponse> map = users.stream()
                .collect(Collectors.toMap(User::getId, this::toPublicUserProfile));
        return new BatchUsersResponse(map);
    }
}