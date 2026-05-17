package com.yappyd.chatservice.service;

import com.yappyd.chatservice.dto.request.UpdateChatParticipantRequest;
import com.yappyd.chatservice.dto.response.ChatListResponse;
import com.yappyd.chatservice.dto.response.ChatParticipantResponse;
import com.yappyd.chatservice.dto.response.ChatParticipantsResponse;
import com.yappyd.chatservice.dto.response.ChatResponse;
import com.yappyd.chatservice.enums.ChatType;
import com.yappyd.chatservice.exception.ChatAccessDeniedException;
import com.yappyd.chatservice.exception.ChatNotFoundException;
import com.yappyd.chatservice.exception.InvalidChatException;
import com.yappyd.chatservice.mapper.ChatMapper;
import com.yappyd.chatservice.model.Chat;
import com.yappyd.chatservice.model.ChatParticipant;
import com.yappyd.chatservice.model.PrivateChat;
import com.yappyd.chatservice.repository.ChatParticipantRepository;
import com.yappyd.chatservice.repository.ChatRepository;
import com.yappyd.chatservice.repository.PrivateChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final PrivateChatService privateChatService;
    private final GroupChatService groupChatService;
    private final ChatParticipantService chatParticipantService;
    private final ChatRepository chatRepository;
    private final PrivateChatRepository privateChatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMapper chatMapper;

    public ChatResponse createPrivateChat(UUID currentUserId, UUID targetUserId) {
        return privateChatService.createPrivateChat(currentUserId, targetUserId);
    }

    public ChatResponse createGroupChat(UUID currentUserId, String title, List<UUID> participantIds) {
        return groupChatService.createGroupChat(currentUserId, title, participantIds);
    }

    public ChatListResponse getChats(UUID currentUserId) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        List<PrivateChat> privateChats = privateChatRepository.findByUserAOrUserB(currentUserId, currentUserId);
        List<ChatParticipant> currentUserGroupParticipants = chatParticipantRepository.findByIdUserId(currentUserId);

        List<UUID> privateChatIds = privateChats.stream()
                .map(PrivateChat::getChatId)
                .toList();

        List<UUID> groupChatIds = currentUserGroupParticipants.stream()
                .map(participant -> participant.getId().getChatId())
                .toList();

        Set<UUID> chatIds = new LinkedHashSet<>();

        chatIds.addAll(privateChatIds);
        chatIds.addAll(groupChatIds);

        if (chatIds.isEmpty()) {
            return new ChatListResponse(List.of());
        }

        Map<UUID, PrivateChat> privateChatByChatId = privateChats.stream()
                .collect(Collectors.toMap(PrivateChat::getChatId, Function.identity()));

        Map<UUID, List<ChatParticipant>> groupParticipantsByChatId = groupChatIds.isEmpty()
                ? Map.of()
                : chatParticipantRepository.findByIdChatIdIn(groupChatIds)
                .stream()
                .collect(Collectors.groupingBy(participant -> participant.getId().getChatId()));

        List<Chat> chats = chatRepository.findByIdInOrderByUpdatedAtDesc(chatIds);

        List<ChatResponse> chatResponses = chats.stream()
                .map(chat -> {
                    if (chat.getType() == ChatType.PRIVATE) {
                        return chatMapper.toChatResponse(chat, privateChatByChatId.get(chat.getId()));
                    }

                    if (chat.getType() == ChatType.GROUP) {
                        List<ChatParticipant> participants = groupParticipantsByChatId.getOrDefault(chat.getId(), List.of());
                        List<UUID> participantIds = participants.stream().map(participant -> participant.getId().getUserId()).toList();
                        return chatMapper.toChatResponse(chat, participantIds);
                    }

                    throw new IllegalStateException("Unsupported chat type: " + chat.getType());
                })
                .toList();

        return new ChatListResponse(chatResponses);
    }

    public ChatResponse getChat(UUID currentUserId, UUID chatId) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        if (chatId == null) {
            throw new InvalidChatException("chatId must not be null");
        }

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));

        if (chat.getType() == ChatType.PRIVATE) {
            return getPrivateChatResponse(currentUserId, chat);
        }

        if (chat.getType() == ChatType.GROUP) {
            return getGroupChatResponse(currentUserId, chat);
        }

        throw new InvalidChatException("Unsupported chat type: " + chat.getType());
    }

    public ChatParticipantsResponse getChatParticipants(UUID currentUserId, UUID chatId) {
        return chatParticipantService.getChatParticipants(currentUserId, chatId);
    }

    public ChatParticipantsResponse addParticipant(UUID currentUserId, UUID chatId, UUID userId) {
        return chatParticipantService.addParticipant(currentUserId, chatId, userId);
    }

    public Optional<ChatParticipantsResponse> removeParticipant(UUID currentUserId, UUID chatId, UUID userId, UUID newOwnerId) {
        return chatParticipantService.removeParticipant(currentUserId, chatId, userId, newOwnerId);
    }

    public ChatParticipantResponse updateParticipant(UUID currentUserId, UUID chatId, UUID userId, UpdateChatParticipantRequest request) {
        return chatParticipantService.updateParticipant(currentUserId, chatId, userId, request);
    }

    private ChatResponse getPrivateChatResponse(UUID currentUserId, Chat chat) {
        PrivateChat privateChat = privateChatRepository.findById(chat.getId()).orElseThrow(() -> new IllegalStateException("PrivateChat not found for chatId: " + chat.getId()));

        if (!privateChat.getUserA().equals(currentUserId) && !privateChat.getUserB().equals(currentUserId)) {
            throw new ChatAccessDeniedException(chat.getId());
        }

        return chatMapper.toChatResponse(chat, privateChat);
    }

    private ChatResponse getGroupChatResponse(UUID currentUserId, Chat chat) {
        List<ChatParticipant> participants = chatParticipantRepository.findByIdChatId(chat.getId());

        if (participants.isEmpty()) {
            throw new IllegalStateException("Group participants not found for chatId: " + chat.getId());
        }

        boolean currentUserIsParticipant = participants.stream()
                .anyMatch(participant -> participant.getId().getUserId().equals(currentUserId));

        if (!currentUserIsParticipant) {
            throw new ChatAccessDeniedException(chat.getId());
        }

        List<UUID> participantIds = participants.stream().map(participant -> participant.getId().getUserId()).toList();

        return chatMapper.toChatResponse(chat, participantIds);
    }
}